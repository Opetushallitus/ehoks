FROM amazon/aws-cli

RUN <<EOF
yum update -y
amazon-linux-extras enable postgresql14
yum install -y jq postgresql-server
yum clean all
EOF

ARG ENV_NAME
WORKDIR /opt/export

COPY scripts/lampi-export/lampi-export.sh /opt/export

ENTRYPOINT ./lampi-export.sh
