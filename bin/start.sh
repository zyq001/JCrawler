#!/bin/sh
source ~/.bash_profile

DATE=$(date +%Y%m%d%H%M)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
LOG_DIR=$DIR/logs
DATA_DIR=$DIR/data

start(){
	if [ $# -ne 1 ] 
	then
		echo "Input your service name"
	else
		echo "Starting $1 ..."
		cd $DIR
		nohup mvn exec:java -Dexec.mainClass="com.dict.crawl.${1}Crawler" -Dexec.cleanupDaemonThreads=false -e > ${LOG_DIR}/$1.$DATE.log 2>&1 &

		if [ $? -ne 0 ]
		then
			echo "start failed, please check the log!"
			exit $?
		else
			echo $! > ${DATA_DIR}/$1.pid
			echo "start success"
		fi
	fi
}


stop(){
	if [ $# -ne 1 ] 
	then
		echo "Input your service name"
	else
		echo "Stopping $1...."
		SPID=$(cat ${DATA_DIR}/$1.pid)
		kill -9 $SPID
		if [ $? -ne 0 ]
		then
			echo "${1} has been gone"
		else
			rm -rf ${DATA_DIR}/${1}.pid
			echo "stop ${1} success"
		fi
	fi
}

restart(){
	if [ $# -ne 1 ] 
	then
		echo "Input your service name"
	else
		stop $1 && start $1
	fi
}

case $1 in
	start)	start $2 ;;
	stop)	stop $2 ;;
	restart)	restart $2;;
	*)		echo "Usage: $0 {start|stop|restart}"" [SERVICE NAME] " ;;
esac

exit 0




