#!/bin/bash

lein deps :tree |
 sed '	/^  /d;
 	s#^ \[\([^/ ]*\) "\([^"]*\)".*#\1/\1/\2/\1-\2#;
	s#^ \[\([^/]*\)/\([^ ]*\) "\([^"]*\)".*#\1/\2/\3/\2-\3#;
	:re;s#^\([^/]*\)\.\([^/]*\)#\1/\2#;tre;
	s#.*#https://repo.clojars.org/&.pom\nhttps://repo1.maven.org/maven2/&.pom#' |
while read url; do
	echo "$url"
	curl -sI "$url" | grep -i last-modified:
done
