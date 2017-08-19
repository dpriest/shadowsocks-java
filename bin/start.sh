#!/bin/bash
HOST="0.0.0.0"
cd `dirname $0`/..
BASE_HOME="`pwd`"
PROJECT_NAME=`basename $BASE_HOME`
PROJECT_HOME="/data/project/${PROJECT_NAME}"
LOG_HOME="/data/logs/$PROJECT_NAME"
CORE_HOME="/data/coredump/$PROJECT_NAME"
TMP_HOME="$PROJECT_HOME/tmp"
PIDFILE="$TMP_HOME/service.pid"
STDOUT_LOG="$LOG_HOME/service.log"

export JAVA_HOME="/opt/java"
export LD_LIBRARY_PATH=$PROJECT_HOME/target:$LD_LIBRARY_PATH
export PATH=${JAVA_HOME}/bin:$PATH

if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
fi
if [ -z "$JAVA" ]; then
    echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.8) in your PATH." 2>&1
    exit 1
fi
MEMORY=`free -m | awk '/Mem/{print $2}'`
if [ $MEMORY -gt 15000 ];then
    JVM_HEAP="8192"
elif [ $MEMORY -gt 7500 ];then
    JVM_HEAP="4096"
elif [ $MEMORY -gt 3500 ];then
    JVM_HEAP="2048"
elif [ $MEMORY -gt 1800 ];then
    JVM_HEAP="1024"
else
    JVM_HEAP="512"
fi
JVM_EDEN=$((JVM_HEAP/2))
if ! netstat -tln 2>&1 | grep ':7777' >/dev/null 2>&1; then
    JMX_PORT=7777
else
    JMX_PORT=$((RANDOM%10+7780))
fi
JAVA_OPTS="-server -Xms${JVM_HEAP}m -Xmx${JVM_HEAP}m -Xss256k -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$LOG_HOME/java.hprof -XX:ErrorFile=$LOG_HOME/hs_err_pid_%p.log"
JAVA_OPTS="${JAVA_OPTS} -verbose:gc -Xloggc:$LOG_HOME/gc.log -XX:+PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintGCApplicationStoppedTime"
JAVA_OPTS="${JAVA_OPTS} -XX:-UseCompressedOops"
JAVA_OPTS="${JAVA_OPTS} -Djava.awt.headless=true -Dfile.encoding=UTF-8"
JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true -Dsun.net.client.defaultConnectTimeout=10000 -Dsun.net.client.defaultReadTimeout=30000"
JAVA_OPTS="${JAVA_OPTS} -Dproject.name=$PROJECT_NAME -Djava.io.tmpdir=$TMP_HOME"
JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
export JAVA_OPTS
ulimit -c unlimited
if [ ! -d "$TMP_HOME" ];then
    mkdir $TMP_HOME
fi
if [ -f "$PIDFILE" ]; then
    PID=`cat $PIDFILE`
    if ps auxfwww | grep -w $PID | grep -v grep >/dev/null 2>&1;then
        echo "service is running. Please run stop.sh first,then start.sh. exists"
        exit 1
    else
       echo "found service.pid, but $PROJECT_NAME isn't runnnging, will start it!"
       rm -f $PIDFILE
    fi
fi
if [ -f "$STDOUT_LOG" ]; then
    mv -f $STDOUT_LOG $STDOUT_LOG.`date '+%Y%m%d%H%M%S'`
fi


MAIN_JAR="${PROJECT_HOME}/target/shadowsocks-1.0-SNAPSHOT.jar"
SS_OPT="-h${HOST}"

echo "cd to ${PROJECT_HOME}/bin for workaround relative path"
cd ${PROJECT_HOME}/bin
nohup $JAVA $JAVA_OPTS -jar $MAIN_JAR $SS_OPT >$STDOUT_LOG 2>&1 &
echo $! > ${PIDFILE}
echo "cd to ${PROJECT_HOME}/bin for continue"
cd ${PROJECT_HOME}/bin

# check dubbo is success!!

PID=`cat ${PIDFILE}`
if ps auxfwww | grep -w $PID | grep -v grep >/dev/null 2>&1;then
    echo "$PROJECT_NAME start Success"
else
   echo "$PROJECT_NAME Start Failed"
   rm -f $PIDFILE
   exit 1
fi