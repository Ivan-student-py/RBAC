#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# Use POSIX sh instead of bash to be more portable

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        echo "Error: JAVA_HOME is set to an invalid directory." >&2
        echo "JAVA_HOME = $JAVA_HOME" >&2
        echo "Please set the JAVA_HOME variable in your environment to match the" >&2
        echo "location of your Java installation." >&2
        exit 1
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || { echo "Error: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2; echo "Please set the JAVA_HOME variable in your environment to match the" >&2; echo "location of your Java installation." >&2; exit 1; }
fi

# Determine the project base directory
if [ -z "$MAVEN_PROJECTBASEDIR" ] ; then
  # Find the project base directory
  if [ -z "$MAVEN_BASEDIR" ] ; then
    MAVEN_PROJECTBASEDIR="$PWD"
  else
    MAVEN_PROJECTBASEDIR="$MAVEN_BASEDIR"
  fi
fi

# Make the project base directory an absolute pathname
save_dir="$PWD"
cd "$MAVEN_PROJECTBASEDIR"
MAVEN_PROJECTBASEDIR="$PWD"
cd "$save_dir"

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

# Check if the wrapper jar exists
if [ ! -f "$WRAPPER_JAR" ] ; then
    echo "Error: Could not find maven-wrapper.jar" >&2
    echo "Maven wrapper jar: $WRAPPER_JAR" >&2
    exit 1
fi

# Build the classpath
CLASSPATH="$WRAPPER_JAR"

# Execute the wrapper
exec "$JAVACMD" \
  -cp "$CLASSPATH" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  $MAVEN_OPTS \
  $WRAPPER_LAUNCHER "$@"