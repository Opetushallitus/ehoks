#!/bin/bash

set -eo pipefail

lampi_manifest_file="manifest.json"
echo "{}" > $lampi_manifest_file
local_s3_bucket="ehoks-export-$ENV_NAME"
system_name="ehoks"
version="v2" # vanha koko kannan dumppina vievä siirto on v1
reporting_schema_name="reporting"
if [ "$ENV_NAME" = "sade" ]; then
    db_hostname="ehoks.db.opintopolku.fi"
elif [ "$ENV_NAME" = "pallero" ]; then
    db_hostname="ehoks.db.testiopintopolku.fi"
else
    db_hostname="ehoks.db.${ENV_NAME}opintopolku.fi"
fi
db_name="ehoks"
db_user="app"
assume_access_key_id=""
assume_secret_access_key=""
assume_session_token=""

case $ENV_NAME in
  "sade")
    lampi_s3_bucket="oph-lampi-prod"
    ;;
  "pallero")
    lampi_s3_bucket="oph-lampi-qa"
    ;;
  *)
    lampi_s3_bucket="oph-lampi-dev"
esac

refresh_assume_role() {
  local -r credentials_json=$(aws sts assume-role --role-arn "$assume_role_arn" --external-id "$ssm_external_id" --role-session-name "ehoks-lampi-export-$ENV_NAME")
  assume_access_key_id=$(jq -r '.Credentials.AccessKeyId' <<<"${credentials_json}")
  assume_secret_access_key=$(jq -r '.Credentials.SecretAccessKey' <<<"${credentials_json}")
  assume_session_token=$(jq -r '.Credentials.SessionToken' <<<"${credentials_json}")
  local -r assume_expiration=$(jq -r '.Credentials.Expiration' <<<"${credentials_json}")
  log "INFO" "Assumed temporary access key ${assume_access_key_id} (expiration ${assume_expiration})"
}

dump_and_upload_db_to_lampi() {
    log "INFO" "Start dump_and_upload_db_to_lampi"
    log "DEBUG" "Params: db_hostname ${db_hostname}, db_name ${db_name}, ENV_NAME $ENV_NAME, local_s3_bucket $local_s3_bucket, lampi_s3_bucket $lampi_s3_bucket assume_role_arn $assume_role_arn"

    log "INFO" "Starting ${db_name} database data dump"

    local -r db_password="$ssm_app_user_password"

    # aws s3 extension and granting privileges need to be created with master user:
    # CREATE EXTENSION IF NOT EXISTS aws_s3 CASCADE;
    # GRANT ALL ON SCHEMA aws_s3 TO app;
    # GRANT ALL ON ALL FUNCTIONS IN SCHEMA aws_s3 TO app;
    # GRANT ALL ON SCHEMA aws_commons TO app;
    # GRANT ALL ON ALL FUNCTIONS IN SCHEMA aws_commons TO app;

    log "INFO" "Refreshing $reporting_schema_name schema"
    pg_command "$db_password" "SELECT refresh_reporting('${reporting_schema_name}')" > /dev/null

    for db_table in $(pg_command "$db_password" "SELECT table_name FROM information_schema.tables WHERE table_schema = '$reporting_schema_name'" 1); do
      local s3_key="fulldump/$system_name/$version/${db_table}.csv"
      local s3_url="s3://$local_s3_bucket/$s3_key"
      log "INFO" "Exporting table $db_table to local S3 $s3_url"
      local files_uploaded=$(pg_command "$db_password" "SELECT files_uploaded FROM aws_s3.query_export_to_s3('SELECT * FROM ${reporting_schema_name}.${db_table}', aws_commons.create_s3_uri('$local_s3_bucket', '$s3_key', 'eu-west-1'), options := 'format csv, header true')" 1 1)
      log "INFO" "Successfully exported table ${reporting_schema_name}.${db_table} to local S3 $s3_url"
      copy_table_to_lampi "$s3_url" $files_uploaded
    done

    generate_and_upload_schema_file "$db_password"
    upload_file_to_lampi "$lampi_manifest_file" > /dev/null
    log "INFO" "manifest.json" $(cat "$lampi_manifest_file")
}

pg_command() {
    local -r db_password="$1"
    local -r sql="$2"
    local -r tuples_only="$3"
    local -r csv="$4"
    cmd='PGPASSWORD="$db_password" psql -h "$db_hostname" -p 5432 -U "$db_user" -d "$db_name" -c "$sql"'
    if [[ $tuples_only ]]
        then
            cmd="$cmd -t"
    fi
    if [[ $csv ]]
        then
            cmd="$cmd --csv"
    fi
    echo $(eval "$cmd")
}

copy_table_to_lampi() {
    local -r source_url="$1"
    local -r file_parts=$2
    refresh_assume_role
    local -r s3_key="fulldump/$system_name/$version/$(basename "${source_url}").gz"
    local -r target_url="s3://$lampi_s3_bucket/$s3_key"

    log "INFO" "Concatenating and compressing $source_url ($file_parts part(s)) to $target_url"
    (aws s3 cp $source_url - ;
    if (( $file_parts > 1 )); then
        for i in $(seq 2 "$file_parts"); do
            aws s3 cp "${source_url}_part${i}" -
        done
    fi) | gzip | AWS_ACCESS_KEY_ID="$assume_access_key_id" AWS_SECRET_ACCESS_KEY="$assume_secret_access_key" AWS_SESSION_TOKEN="$assume_session_token" aws s3 cp - "$target_url"

    log "INFO" "Fetch object version for $s3_key in $lampi_s3_bucket"
    local -r obj_version=$(AWS_ACCESS_KEY_ID="$assume_access_key_id" AWS_SECRET_ACCESS_KEY="$assume_secret_access_key" AWS_SESSION_TOKEN="$assume_session_token" aws s3api head-object --region eu-west-1 --bucket "$lampi_s3_bucket" --key "$s3_key" --output json | jq -r .VersionId)

    log "INFO" "Update received object version to manifest: $s3_key = $obj_version"
    local -r item=$(lampi_manifest_item "$s3_key" "$obj_version")
    add_to_manifest ".tables += [$item]"
}

add_to_manifest() {
    local -r expr="$1"
    jq "$expr" < "$lampi_manifest_file" > "$lampi_manifest_file.tmp"
    cp "$lampi_manifest_file.tmp" $lampi_manifest_file
}

log() {
    echo "[$(date +"%Y-%m-%d"+"%T")]: $*"
}

lampi_manifest_item() {
    local -r s3_key="$1"
    local -r obj_version="$2"
    echo "{}" | jq ".key += \"$s3_key\"" | jq ".s3Version += \"$obj_version\""
}

generate_and_upload_schema_file() {
    local -r db_password="$1"
    refresh_assume_role
    local -r schema_file="ehoks-$reporting_schema_name.schema"
    touch "$schema_file"
    log "INFO" "Generating schema file $schema_file"
    PGPASSWORD="${db_password}" pg_dump -Fc --section=pre-data --section=post-data --no-comments --no-privilege --no-owner --host "$db_hostname" --username "$db_user" --schema-only --file "$schema_file" --schema "$reporting_schema_name" -t "$reporting_schema_name.*" "$db_name"
    local -r schema=$(upload_file_to_lampi "$schema_file")
    add_to_manifest ".schema += $schema"
}

upload_file_to_lampi() {
    local -r file="$1"
    refresh_assume_role > /dev/null
    local -r file_s3_key="fulldump/$system_name/$version/$file"
    local -r file_s3_url="s3://$local_s3_bucket/$file_s3_key"
    local -r put_result=$(aws s3api put-object --region eu-west-1 --body "$file" --bucket "$local_s3_bucket" --key "$file_s3_key" --output json)
    local -r cp_result=$(aws s3 cp "$file_s3_url" - | AWS_ACCESS_KEY_ID="$assume_access_key_id" AWS_SECRET_ACCESS_KEY="$assume_secret_access_key" AWS_SESSION_TOKEN="$assume_session_token" aws s3 cp - "s3://$lampi_s3_bucket/$file_s3_key")
    local -r obj_version=$(AWS_ACCESS_KEY_ID="$assume_access_key_id" AWS_SECRET_ACCESS_KEY="$assume_secret_access_key" AWS_SESSION_TOKEN="$assume_session_token" aws s3api head-object --region eu-west-1 --bucket "$lampi_s3_bucket" --key "$file_s3_key" --output json | jq -r .VersionId)
    echo $(lampi_manifest_item "$file_s3_key" "$obj_version")
}

dump_and_upload_db_to_lampi
