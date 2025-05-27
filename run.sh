#!/bin/bash

export JAVA_HOME=/home/hoota/jdk/jdk-17.0.9

if [ -f ../config.env ]; then
  export $(grep -v '^#' ../config.env | xargs)
fi

if [ -f local/local.env ]; then
  export $(grep -v '^#' local/local.env | xargs)
fi

CLASSPATH=$(/usr/local/bin/mvn -q exec:exec -Dexec.classpathScope="compile" -Dexec.executable="echo" -Dexec.args="%classpath")

WITH_SCHEDULER="-Dapp.scheduling.enable=true"

$JAVA_HOME/bin/java -Xmx1G $WITH_SCHEDULER -Dfile.encoding=UTF-8 -classpath $CLASSPATH kcms.WebApplicationKt