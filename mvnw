#!/usr/bin/env sh
# ----------------------------------------------------------------------------
# Maven Wrapper startup script (Unix)
# ----------------------------------------------------------------------------

WRAPPER_JAR="$(dirname "$0")/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$(dirname "$0")/.mvn/wrapper/maven-wrapper.properties"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper JAR..."
  curl -fsSL "$DOWNLOAD_URL" -o "$WRAPPER_JAR"
fi

JAVA_EXE="java"
command -v java >/dev/null 2>&1 || { echo "Java not found"; exit 1; }

# Set project base dir for multi-module property to silence warning
if [ -z "$MAVEN_PROJECTBASEDIR" ]; then
  MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")"; pwd -P)"
fi
export MAVEN_PROJECTBASEDIR

exec "$JAVA_EXE" $MAVEN_OPTS -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" -classpath "$WRAPPER_JAR" $WRAPPER_LAUNCHER "$@"
