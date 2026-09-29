@REM Maven Wrapper launcher
@echo off
set "MAVEN_PROJECTBASEDIR=%~dp0"
if not defined MAVEN_PROJECTBASEDIR set MAVEN_PROJECTBASEDIR=.
"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
