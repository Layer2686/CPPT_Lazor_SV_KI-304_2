#!/bin/sh
# Maven Wrapper launcher. The wrapper JAR is intentionally kept in .mvn/wrapper.
[ -z "$MAVEN_PROJECTBASEDIR" ] && MAVEN_PROJECTBASEDIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec java ${MAVEN_OPTS:-} ${MAVEN_DEBUG_OPTS:-} -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain "$@"
