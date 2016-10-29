export JAVA_HOME='/usr/lib/jvm/jdk-7-oracle-x64'

$JAVA_HOME/jre/bin/java -Xmx1024M -XX:MaxPermSize=512M -Xss2M -jar `dirname $0`/sbt-launch.jar "$@"
