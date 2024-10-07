FROM amazon/aws-cli

RUN <<EOF
yum update -y
yum install -y jq postgresql
yum clean all
EOF

ARG ENV_NAME
WORKDIR /opt/export

COPY scripts/lampi-export/lampi-export.sh /opt/export

ENTRYPOINT source ./lampi-export.sh && dump_and_upload_db_to_lampi "ehoks.db.$ENV_NAME.internal" "ehoks" "/$ENV_NAME/postgresqls/ehoks/app-user-password"
