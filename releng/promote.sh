#!/bin/bash

###############################################################################
# Copyright (c) 2014, 2018 1C-Soft LLC and others.
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     George Suaridze (1C) - initial API and implementation
#     Vladimir Piskarev (1C) - ongoing maintenance
###############################################################################

# Handly promotion script
#
# This script downloads the specified build from Handly JIPP and
# prepares build artifacts for distribution from Eclipse download facility.
# In particular, it adds additional p2 properties such as p2.mirrorsURL
# and p2.statsURI. The result can be found in the <build-tag> subdirectory
# of the working directory where <build-tag> is a parameter of the script.
# This subdirectory can then be uploaded to Handly downloads area on
# build.eclipse.org.
#
# To add p2.mirrorsURL and p2.statsURI properties to the artifact repository
# this script uses xsltproc and the p2.xsl file co-located with this file.
#
# Requirements:
# * JAVA_HOME properly set
# * pwd, curl, xsltproc, xz, zip and unzip utilities
#
# Optional:
# * DOWNLOADS_AREA may specify a directory name so that the path
#   ~/downloads/handly/$DOWNLOADS_AREA/ will point to a directory
#   on build.eclipse.org in which Handly downloads are stored.
#   This is where you will upload the result subdirectory.
#   If this variable is not set, "releases" is assumed.
#   Use "drops" for non-release builds.
#
# Usage: ./promote.sh <ci-job-name> <ci-build-number> [<build-tag>]
#
# The optional <build-tag> specifies a label under which the build will be
# published, e.g. 0.1, 0.1RC1, 0.1.v20141002-1000. If this parameter is omitted,
# the value from the VERSION file of the specified CI build will be used.

#
# Parsing args
#

USAGE="Usage: ./promote.sh <ci-job-name> <ci-build-number> [<build-tag>]"

if [[ "$1" != "" ]]; then
    JOB_NAME="$1"
else
    echo $USAGE
    exit 1
fi

if [[ "$2" != "" ]]; then
    BUILD_ID="$2"
else
    echo $USAGE
    exit 1
fi

if [[ "$3" != "" ]]; then
    REPO_VERSION="$3"
    if [[ -d "$3" ]]; then
        echo "Directory '$REPO_VERSION' already exists"
        echo "Remove it or run this script with a different <build-tag>"
        exit 1
    fi
else
    echo "<build-tag> is not specified; will use build VERSION value"
fi

#
# Downloading p2 repository from JIPP
#

RESPONSE=$(curl --insecure -s -S -w %{http_code} -o /dev/null https://ci.eclipse.org/handly/job/$JOB_NAME/$BUILD_ID/artifact/build/VERSION)
if [[ "$RESPONSE" != "200" ]]; then
    echo "Cannot download $JOB_NAME build #$BUILD_ID (HTTP $RESPONSE)"
    exit 1
fi

curl --insecure -s -S -O https://ci.eclipse.org/handly/job/$JOB_NAME/$BUILD_ID/artifact/build/VERSION
if [[ "$?" != "0" ]]; then
    rm -f VERSION
    exit 1
fi

BUILD_VERSION=$(head -n 1 VERSION)
rm -f VERSION

if [[ "$REPO_VERSION" == "" ]]; then
    REPO_VERSION=$BUILD_VERSION
fi

if [[ -d "$REPO_VERSION" ]]; then
    echo "Directory '$REPO_VERSION' already exists"
    echo "Remove it or run this script with a different <build-tag>"
    exit 1
fi

mkdir $REPO_VERSION
if [[ "$?" != "0" ]]; then
    exit 1
fi

REPO_PREFIX=${REPO_PREFIX:-"handly-repository"}
REPO_FILE=$REPO_VERSION/$REPO_PREFIX-$REPO_VERSION.zip

echo "Downloading p2 repository $BUILD_VERSION..."

curl --insecure -s -S -o $REPO_FILE https://ci.eclipse.org/handly/job/$JOB_NAME/$BUILD_ID/artifact/build/packages/$REPO_PREFIX-$BUILD_VERSION.zip
if [[ "$?" != "0" ]]; then
    exit 1
fi

echo "Unzipping p2 repository..."

unzip $REPO_FILE -d $REPO_VERSION/repository > /dev/null
if [[ "$?" != "0" ]]; then
    exit 1
fi

#
# Configuring additional p2 properties
#

DOWNLOADS_AREA=${DOWNLOADS_AREA:-"releases"}
REPOSITORY_PATH="/handly/$DOWNLOADS_AREA/$REPO_VERSION/repository"
MIRRORS_URL="http://www.eclipse.org/downloads/download.php?file=$REPOSITORY_PATH&format=xml"

#
# Updating p2 repository with additional properties
#

echo "Adding p2 properties..."

unzip -p $REPO_VERSION/repository/artifacts.jar | xsltproc -stringparam mirrorsURL "$MIRRORS_URL" -stringparam statsId "$REPO_VERSION" p2.xsl - > artifacts.xml
if [[ "$?" != "0" ]]; then
    exit 1
fi

zip -q $REPO_VERSION/repository/artifacts.jar artifacts.xml
if [[ "$?" != "0" ]]; then
    exit 1
fi

XZ_EXE=$(which xz)
if [[ "$?" != "0" || -z "${XZ_EXE}" ]]; then
    echo "Cannot locate xz executable"
    exit 1
fi

$XZ_EXE -e --force artifacts.xml
if [[ "$?" != "0" ]]; then
    exit 1
fi

mv -f artifacts.xml.xz $REPO_VERSION/repository/
if [[ "$?" != "0" ]]; then
    exit 1
fi

rm -f artifacts.xml

JAVADOC_PREFIX=${JAVADOC_PREFIX:-"handly-javadoc"}
JAVADOC_FILE=$REPO_VERSION/$JAVADOC_PREFIX-$REPO_VERSION.zip

#
# Downloading Javadoc from JIPP
#

echo "Downloading Javadoc $BUILD_VERSION..."

curl --insecure -s -S -o $JAVADOC_FILE https://ci.eclipse.org/handly/job/$JOB_NAME/$BUILD_ID/javadoc/*zip*/javadoc.zip
if [[ "$?" != "0" ]]; then
    exit 1
fi

echo "OK ./$REPO_VERSION"
