#!/bin/sh

#
#  This script will upload release versions to Maven Central.
#

# Prerun the script to download all the dependent artifacts
mvn help:evaluate -Dexpression=project.version

current_pom_version=`mvn help:evaluate -Dexpression=project.version | grep -v '^\['`

if [[ "${current_pom_version}" == *SNAPSHOT ]]
then
    echo "Snapshot version detected.  Not deployed to Sonatype"
else
    echo "Release version ${current_pom_version} detected"

    # Capture any out-of-date dependencies to the log
    mvn versions:display-dependency-updates

fi

# Push to Sonatype/Maven Central
mvn --settings build-resources/travis-settings.xml -P release deploy
