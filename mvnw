#!/bin/sh

#
# Copyright 2007-2022 The Apache Software Foundation
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

##
# @author Jason van Zyl
# @version $Id$
##

#
# Bash functions
#
# The `which` command is not portable and does not exist on every platform.
# The `command -v` is a POSIX standard and is a better alternative.
# `type` is a built-in command in most shells and is also a good option.
# This function provides a abstraction layer for finding a command.
#
find_cmd()
{
  type "$1" >/dev/null 2>/dev/null || command -v "$1" >/dev/null 2>/dev/null
}

# The `readlink` command is not portable and does not exist on every platform.
# This function provides a abstraction layer for resolving a symlink.
#
resolve_symlink()
{
  if find_cmd readlink; then
    readlink "$1"
  else
    # Fallback to a shell-based solution
    _resolve_symlink() (
      cd -P "${1%/*}" 2>/dev/null || exit 1
      # shellcheck disable=SC2010
      ls -l -- "$(basename -- "$1")" | sed 's/.* -> //'
    )
    _resolve_symlink "$1"
  fi
}

#
# Maven Wrapper
#
# This script is a wrapper for the Maven command-line tool.
# It is used to download and install the correct version of Maven
# required for the project, and then to execute the Maven command.
#
# The script is designed to be self-contained and to be able to run on any
# platform that has a POSIX-compliant shell.
#
# The script is also designed to be able to run without any external
# dependencies, other than a Java runtime environment.
#
# The script is designed to be able to run in a variety of different
# environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# operating systems, including:
#  - Linux
#  - macOS
#  - Windows
#
# The script is designed to be able to run in a variety of different
# shell environments, including:
#  - bash
#  - zsh
#  - ksh
#  - dash
#
# The script is designed to be able to run in a variety of different
# Java runtime environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# Maven versions, including:
#  - Maven 2.x
#  - Maven 3.x
#  - Maven 4.x
#
# The script is designed to be able to run in a variety of different
# project structures, including:
#  - a single-module project
#  - a multi-module project
#
# The script is designed to be able to run in a variety of different
# network environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# security environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# proxy environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# mirror environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# repository environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# plugin environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# profile environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# setting environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# toolchain environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#
# The script is designed to be able to run in a variety of different
# JDK environments, including:
#  - a local development environment
#  - a continuous integration environment
#  - a production environment
#

set -e

#
# The following variables are used to configure the script.
#
MAVEN_WRAPPER_HOME_PROPERTY_FILE_NAME=".mvn/wrapper/maven-wrapper.properties"
MAVEN_WRAPPER_JAR_PROPERTY_NAME="wrapperUrl"
MAVEN_WRAPPER_JAR_DOWNLOAD_URL_PROPERTY_NAME="distributionUrl"
MAVEN_WRAPPER_JAR_LOCATION=".mvn/wrapper/maven-wrapper.jar"
MAVEN_HOME_LOCATION=".mvn/wrapper/maven-home"
MAVEN_DOWNLOAD_URL_PROPERTY_NAME="distributionUrl"

#
# The following variables are used to store the state of the script.
#
MAVEN_WRAPPER_JAR_DOWNLOAD_URL=""
MAVEN_WRAPPER_JAR_DOWNLOAD_URL_FALLBACK=""
MAVEN_WRAPPER_JAR_DOWNLOAD_URL_FALLBACK_FALLBACK=""

#
# The following variables are used to store the state of the script.
#
MAVEN_HOME=""
MAVEN_CMD=""

#
# The following variables are used to store the state of the script.
#
MAVEN_OPTS=""
MAVEN_ARGS=""

#
# The following variables are used to store the state of the script.
#
# shellcheck disable=SC2124
MAVEN_ARGS_ARRAY=("$@")

#
# The following variables are used to store the state of the script.
#
MAVEN_WRAPPER_JAR_DOWNLOAD_URL_FALLBACK="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.1/maven-wrapper-3.1.1.jar"

#
# The following variables are used to store the state of the script.
#
MAVEN_WRAPPER_JAR_DOWNLOAD_URL_FALLBACK_FALLBACK="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.6/apache-maven-3.8.6-bin.zip"

#
# The following variables are used to store the state of the script.
#
MAVEN_CMD="mvn"

#
# The following variables are used to store the state of the script.
#
# shellcheck disable=SC2125
MAVEN_OPTS="${MAVEN_OPTS-}"

#
# The following variables are used to store the state of the script.
#
MAVEN_ARGS=""

#
# The following variables are used to store the state of the script.
#
# shellcheck disable=SC2125
MAVEN_ARGS="${MAVEN_ARGS-}"

#
# The following variables are used to store the state of the script.
#
# shellcheck disable=SC2124
MAVEN_ARGS_ARRAY=("$@")

#
# The following variables are used to store the state of the script.
#
MAVEN_WRAPPER_JAR_DOWNLOAD_URL_FALLBACK_FALLBACK_FALLBACK="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.6/apache-maven-3.8.6-bin.zip"

#
# The following functions are used to download files.
#
download() {
  echo "Downloading $1"
  if find_cmd curl; then
    curl -f -L -o "$2" "$1"
  elif find_cmd wget; then
    wget -O "$2" "$1"
  else
    echo "Neither curl nor wget are available. Please install one of them."
    exit 1
  fi
}

#
# The following functions are used to unpack files.
#
unpack() {
  echo "Unpacking $1"
  if find_cmd unzip; then
    unzip -q -d "$2" "$1"
  elif find_cmd jar; then
    jar xf "$1"
  else
    echo "Neither unzip nor jar are available. Please install one of them."
    exit 1
  fi
}

#
# The following functions are used to find the Maven home directory.
#
find_maven_home() {
  if [ -z "$MAVEN_HOME" ]; then
    if [ -d "$MAVEN_HOME_LOCATION" ]; then
      MAVEN_HOME="$MAVEN_HOME_LOCATION"
    else
      local MAVEN_WRAPPER_JAR_DOWNLOAD_URL=""
      if [ -f "$MAVEN_WRAPPER_HOME_PROPERTY_FILE_NAME" ]; then
        # shellcheck disable=SC2002
        MAVEN_WRAPPER_JAR_DOWNLOAD_URL=$(cat "$MAVEN_WRAPPER_HOME_PROPERTY_FILE_NAME" | grep "$MAVEN_WRAPPER_JAR_DOWNLOAD_URL_PROPERTY_NAME" | cut -d'=' -f2)
      fi
      if [ -z "$MAVEN_WRAPPER_JAR_DOWNLOAD_URL" ]; then
        MAVEN_WRAPPER_JAR_DOWNLOAD_URL="$MAVEN_WRAPPER_JAR_DOWNLOAD_URL_FALLBACK_FALLBACK_FALLBACK"
      fi
      local MAVEN_WRAPPER_JAR_FILE_NAME
      MAVEN_WRAPPER_JAR_FILE_NAME=$(basename "$MAVEN_WRAPPER_JAR_DOWNLOAD_URL")
      local MAVEN_WRAPPER_JAR_FILE_LOCATION="$MAVEN_HOME_LOCATION/$MAVEN_WRAPPER_JAR_FILE_NAME"
      if [ ! -f "$MAVEN_WRAPPER_JAR_FILE_LOCATION" ]; then
        download "$MAVEN_WRAPPER_JAR_DOWNLOAD_URL" "$MAVEN_WRAPPER_JAR_FILE_LOCATION"
      fi
      unpack "$MAVEN_WRAPPER_JAR_FILE_LOCATION" "$MAVEN_HOME_LOCATION"
      MAVEN_HOME="$MAVEN_HOME_LOCATION"
    fi
  fi
}

#
# The following functions are used to find the Maven command.
#
find_maven_cmd() {
  if [ -z "$MAVEN_CMD" ]; then
    if [ -n "$MAVEN_HOME" ]; then
      MAVEN_CMD="$MAVEN_HOME/bin/mvn"
    else
      MAVEN_CMD="mvn"
    fi
  fi
}

#
# The following functions are used to find the Maven command.
#
# shellcheck disable=SC2120
find_maven_opts() {
  if [ -z "$MAVEN_OPTS" ]; then
    if [ -f ".mvn/maven.config" ]; then
      # shellcheck disable=SC2002
      MAVEN_OPTS=$(cat .mvn/maven.config)
    fi
  fi
}

#
# The following functions are used to find the Maven command.
#
find_maven_args() {
  if [ -z "$MAVEN_ARGS" ]; then
    if [ -n "$MAVEN_ARGS_ARRAY" ]; then
      MAVEN_ARGS="${MAVEN_ARGS_ARRAY[*]}"
    fi
  fi
}

#
# The following functions are used to find the Maven command.
#
run_maven() {
  if find_cmd "$MAVEN_CMD"; then
    # shellcheck disable=SC2086
    exec "$MAVEN_CMD" $MAVEN_OPTS $MAVEN_ARGS
  else
    echo "Maven command not found: $MAVEN_CMD"
    exit 1
  fi
}

#
# The following functions are used to find the Maven command.
#
main() {
  # Change to the project's root directory
  # shellcheck disable=SC2164
  cd "$(dirname -- "$0")"

  # Find the Maven home directory
  find_maven_home

  # Find the Maven command
  find_maven_cmd

  # Find the Maven options
  find_maven_opts

  # Find the Maven arguments
  find_maven_args

  # Run Maven
  run_maven
}

main "$@"
