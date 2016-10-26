JAVA_DIR="/usr/lib/jvm/java-1.7.0-openjdk-amd64"
SCRIPT_DIR=$(dirname "$0")
LIB_DIR="$SCRIPT_DIR"/lib
LIB_PATH="$LIB_DIR"/libjnotify.so

if [ ! -f "$LIB_PATH" ]; then
  #check existence of make command
  command -v make >/dev/null 2>&1 || { echo >&2 'Missing "make", please install build-essentials package by running "apt-get install build-essentials" as system administrator'; exit 1; }
  
  ARCHIVE_PATH=$SCRIPT_DIR/lib/jnotify-lib-0.94.zip
  if [ ! -f "$ARCHIVE_PATH" ]; then
    echo "Missing JNotify library archive at $ARCHIVE_PATH"
    exit 1
  fi
  LIB_TEMP_DIR=$SCRIPT_DIR/JNotify
  mkdir $LIB_TEMP_DIR

  echo 'Extracting library'
  unzip -d "$LIB_TEMP_DIR" "$ARCHIVE_PATH" >/dev/null
  
  echo 'Compiling library'
  COMPILE_DIR=$LIB_TEMP_DIR/compile
  SRC_ARCHIVE_PATH=$LIB_TEMP_DIR/jnotify-native-linux-0.94-src.zip
  mkdir $COMPILE_DIR
  unzip -d "$COMPILE_DIR" "$SRC_ARCHIVE_PATH" >/dev/null
  export C_INCLUDE_PATH="$JAVA_DIR"/include/:"$JAVA_DIR"/include/linux/

  make -C "$COMPILE_DIR"/Release
  
  echo 'Cleaning up'
  cp "$COMPILE_DIR"/Release/libjnotify.so "$LIB_PATH"
  rm -rf "$LIB_TEMP_DIR"

  echo 'Launching SBT'
fi

"$JAVA_DIR"/bin/java -Xmx1024M -XX:MaxPermSize=512M -Xss2M -Djava.library.path="$LIB_DIR"/ -jar "$SCRIPT_DIR"/sbt-launch.jar "$@"
