#!/bin/bash

###############################################################################
# Copyright (c) 2014 1C LLC.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     George Suaridze (1C) - initial API and implementation
#     Vladimir Piskarev (1C) - ongoing maintenance
###############################################################################

# Handly promotion script
#
# This script downloads the specified build from Handly HIPP and
# prepares build artifacts for distribution from Eclipse download facility.
# In particular, it adds additional p2 properties such as p2.mirrorsURL
# and p2.statsURI. The result can be found in the <build-tag> subdirectory
# of the working directory where <build-tag> is a parameter of the script.
# This subdirectory can then be uploaded to Handly downloads area on
# build.eclipse.org.
# 
# To add p2.mirrorsURL and p2.statsURI properties this script uses
# WTP releng tool addRepoProperties.
#
# Requirements:
# * JAVA_HOME properly set
# * ECLIPSE_HOME pointing to an Eclipse installation that contains
#   org.eclipse.wtp.releng.tools.feature (the feature can be installed from
#   http://download.eclipse.org/webtools/releng/repository/).
#   If this variable is not set, and the working directory has a subdirectory
#   named "eclipse", it will be assumed that this subdirectory contains
#   the required Eclipse installation and ECLIPSE_HOME will be set
#   to "./eclipse" by default
# * pwd, curl and unzip utilities
#
# Optional:
# * DOWNLOADS_AREA may specify a directory name so that the path
#   ~/downloads/handly/$DOWNLOADS_AREA/ will point to a directory
#   on build.eclipse.org in which Handly downloads are stored.
#   This is where you will upload the result subdirectory.
#   If this variable is not set, "releases" is assumed.
#   Use "drops" for non-release builds.
# 
# Usage: ./promote.sh <hudson-job-name> <hudson-build-number> [<build-tag>]
#   
# The optional <build-tag> specifies a label under which the build will be
# published, e.g. 0.1, 0.1RC1, 0.1.v20141002-1000. If this parameter is omitted,
# the value from the VERSION file of the specified Hudson build will be used.

#
# Parsing args
#

USAGE="Usage: ./promote.sh <hudson-job-name> <hudson-build-number> [<build-tag>]"

if [[ "$ECLIPSE_HOME" == "" ]]; then
    if [[ -d "./eclipse" ]]; then
        ECLIPSE_HOME="./eclipse"
    else
        echo "ECLIPSE_HOME must be set"
        exit -1
    fi
fi

if [[ "$1" != "" ]]; then
    JOB_NAME="$1"
else
    echo $USAGE
    exit -1
fi

if [[ "$2" != "" ]]; then
    BUILD_ID="$2"
else
    echo $USAGE
    exit -1
fi

if [[ "$3" != "" ]]; then
    REPO_VERSION="$3"
    if [[ -d "$3" ]]; then
        echo "Directory '$REPO_VERSION' already exists"
        echo "Remove it or run this script with a different <build-tag>"
        exit -1
    fi    
else
    echo "<build-tag> is not specified; will use build VERSION value"
fi

#
# Downloading Handly build from HIPP
#

echo "Downloading Handly build from Hudson..."

RESPONSE=$(curl --insecure -s -S -w %{http_code} -o /dev/null https://hudson.eclipse.org/handly/job/$JOB_NAME/$BUILD_ID/artifact/build/VERSION)
if [[ "$RESPONSE" != "200" ]]; then
    echo "Cannot download Handly build (HTTP $RESPONSE)"
    exit -1
fi

curl --insecure -s -S -O https://hudson.eclipse.org/handly/job/$JOB_NAME/$BUILD_ID/artifact/build/VERSION
if [[ "$?" != "0" ]]; then
    rm -f VERSION
    exit -1
fi

BUILD_VERSION=$(head -n 1 VERSION)
rm -f VERSION

if [[ "$REPO_VERSION" == "" ]]; then
    REPO_VERSION=$BUILD_VERSION
fi

if [[ -d "$REPO_VERSION" ]]; then
    echo "Directory '$REPO_VERSION' already exists"
    echo "Remove it or run this script with a different <build-tag>"
    exit -1
fi

mkdir $REPO_VERSION
if [[ "$?" != "0" ]]; then
    exit -1
fi

REPO_PREFIX=${REPO_PREFIX:-"handly-repository-incubation"}
REPO_FILE=$REPO_VERSION/$REPO_PREFIX-$REPO_VERSION.zip

curl --insecure -s -S -o $REPO_FILE https://hudson.eclipse.org/handly/job/$JOB_NAME/$BUILD_ID/artifact/build/packages/$REPO_PREFIX-$BUILD_VERSION.zip
if [[ "$?" != "0" ]]; then
    exit -1
fi

echo "Unzipping Handly p2 repository..."

unzip $REPO_FILE -d $REPO_VERSION/repository > /dev/null
if [[ "$?" != "0" ]]; then
    exit -1
fi

#
# Configuring additional p2 properties
#

DOWNLOADS_AREA=${DOWNLOADS_AREA:-"releases"}
REPOSITORY_PATH="/handly/$DOWNLOADS_AREA/$REPO_VERSION/repository"
P2_MIRRORS_URL="http://www.eclipse.org/downloads/download.php?file=$REPOSITORY_PATH&format=xml"
P2_MIRRORS_ARGS=" -DartifactRepoDirectory=$(pwd)/$REPO_VERSION/repository -Dp2MirrorsURL=$P2_MIRRORS_URL"

P2_STATS_URL="http://download.eclipse.org/stats/handly"
STATS_TRACKED_ARTIFACTS="org.eclipse.handly.ui,org.eclipse.handly.xtext.ui,org.eclipse.handly.examples.basic.ui"
STATS_ARTIFACTS_SUFFIX="-$REPO_VERSION"
P2_STATS_ARGS="-Dp2StatsURI=$P2_STATS_URL -DstatsTrackedArtifacts=$STATS_TRACKED_ARTIFACTS -DstatsArtifactsSuffix=$STATS_ARTIFACTS_SUFFIX"

#
# Updating Handly p2 repository with additional properties
#

echo "Using Eclipse installation at $ECLIPSE_HOME"

if [[ -f "$ECLIPSE_HOME/eclipsec" ]]; then
    ECLIPSE_EXECUTABLE="eclipsec"
elif [[ -f "$ECLIPSE_HOME/eclipse" ]]; then
    ECLIPSE_EXECUTABLE="eclipse"
else
    echo "Cannot find Eclipse executable"
    exit -1
fi

echo "Adding p2 properties..."

$ECLIPSE_HOME/$ECLIPSE_EXECUTABLE -application org.eclipse.wtp.releng.tools.addRepoProperties -nosplash -consoleLog --launcher.suppressErrors -vmargs $P2_MIRRORS_ARGS $P2_STATS_ARGS
if [[ "$?" != "0" ]]; then
    exit -1
fi