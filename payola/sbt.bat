set SCRIPT_DIR=%~dp0
set JAVA_PATH_TO_USE=..\..\..\..\..\..\Program Files (x86)\Java\jdk1.7.0_79\bin
"%JAVA_PATH_TO_USE%\java" -Xmx256M -XX:MaxPermSize=256M -Xss2M -jar "%SCRIPT_DIR%sbt-launch.jar" %*