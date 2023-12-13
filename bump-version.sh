#!/usr/bin/env bash
mvn versions:set -DnewVersion=$VERSION
find . -name pom.xml.versionsBackup -delete