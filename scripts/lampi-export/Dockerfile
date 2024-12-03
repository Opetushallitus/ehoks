FROM amazon/aws-cli

RUN <<EOF
yum install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm
yum update -y
yum -y install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
yum install -y jq postgresql15-server
yum clean all
EOF

ARG ENV_NAME
WORKDIR /opt/export

COPY scripts/lampi-export/lampi-export.sh /opt/export

ENTRYPOINT ./lampi-export.sh
