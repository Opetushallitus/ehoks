#!/usr/bin/env bash

cat <<END
artifactId=$ARTIFACT_NAME
buildNumber=$BUILD_ID
vcsRevision=$(git rev-parse HEAD)
buildTime=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
END
