FROM amazon/aws-cli

COPY scripts/lampi-export/pgdg.repo /etc/yum.repos.d/pgdg.repo
RUN <<EOF
yum update -y
yum install -y jq
yum install -y curl
yum makecache
yum repolist
yum install -y libzstd
yum --enablerepo="pgdg15" install -y postgresql15 postgresql15-server
yum clean all
aws --version
psql --version
EOF

ARG ENV_NAME
WORKDIR /opt/export

COPY scripts/lampi-export/lampi-export.sh /opt/export
COPY scripts/lampi-export/send-error-message.sh /opt/export

ENTRYPOINT exec ./lampi-export.sh || ./send-error-message.sh
