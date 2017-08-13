#!/bin/bash

cd `dirname $0`/..
BASE_HOME="`pwd`"
PROJECT_NAME=`basename $BASE_HOME`
PROJECT_HOME="/data/project/${PROJECT_NAME}"
TMP_HOME="$PROJECT_HOME/tmp"
PIDFILE="$TMP_HOME/service.pid"
if [ ! -f "$PIDFILE" ];then
    echo "PIDFILE:$PIDFILE not found. exists"
    exit
fi
PID=`cat $PIDFILE`
if [ "$PID" == "" ] ; then
    echo "can not find process pid. exists"
    rm $PIDFILE
    exit
fi

jstack -F ${PID} > ${PROJECT_HOME}/`date +%Y%m%d%H%M%S`.stack

echo -e "`hostname`: stopping $PROJECT_NAME $PID ... "
i=0
max_retry=10
while [ 1 ]
do
    if [ ! -d /proc/$PID  ];then
        echo -e "`hostname`: $PROJECT_NAME $PID Stopp Success."
        rm $PIDFILE
        exit 0
    else
        kill $PID
    fi
    sleep 6s
    i=`expr $i + 1`
    if [ $i -ge $max_retry ];then
        #退出并返回错误
        echo -e "`hostname`: $PROJECT_NAME $PID Stopp Failed."
        exit 1
        #或强制关闭并返回成功
        #echo -e "kill -9 $PID"
        #kill -9 $PID
         #rm $PIDFILE
    fi
done
