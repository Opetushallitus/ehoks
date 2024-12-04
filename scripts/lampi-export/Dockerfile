FROM amazon/aws-cli

RUN <<EOF
amazon-linux-extras install -y epel
tee /etc/yum.repos.d/pgdg.repo<<END
[pgdg14]
name=PostgreSQL 14 for RHEL/CentOS 7 - ARM64
baseurl=http://download.postgresql.org/pub/repos/yum/14/redhat/rhel-7-aarch64
enabled=1
gpgcheck=0
END
yum makecache
yum update -y
yum install -y jq postgresql14 postgresql14-server
yum clean all
EOF

ARG ENV_NAME
WORKDIR /opt/export

COPY scripts/lampi-export/lampi-export.sh /opt/export

ENTRYPOINT ./lampi-export.sh
