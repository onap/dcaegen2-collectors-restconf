#!/bin/sh

usage() {
        echo "restConfCollector.sh <start/stop>"
}

BASEDIR=/opt/app/restconfcollector
rm -rf /opt/app/restconfcollector/logs
mkdir /opt/app/restconfcollector/logs
cd /opt/app/restconfcollector/logs
touch console.txt
cd -

restConfCollector_start() {
        echo `date +"%Y%m%d.%H%M%S%3N"` - restConfCollector_start | tee -a ${BASEDIR}/logs/console.txt
        collectorPid=`pgrep -f org.onap.restconf.common`

        if [ ! -z "$collectorPid" ]; then
                echo  "WARNING: restConf Collector already running as PID $collectorPid" | tee -a ${BASEDIR}/logs/console.txt
                echo  "Startup Aborted!!!" | tee -a ${BASEDIR}/logs/console.txt
                exit 1
        fi


        # run java. The classpath is the etc dir for config files, and the lib dir
        # for all the jars.

        cd ${BASEDIR}
        echo "192.168.17.11 onap-message-router" >> /etc/hosts
        nohup $JAVA -cp "etc${PATHSEP}lib/*" $JAVA_OPTS -Dhttps.protocols=TLSv1.1,TLSv1.2 $MAINCLASS $* &
        if [ $? -ne 0 ]; then
                echo "restConf Collector has been started!!!" | tee -a ${BASEDIR}/logs/console.txt
        fi


}

## Pre-setting
JAVA_HOME=/usr/bin/java

# use JAVA_HOME if provided
if [ -z "$JAVA_HOME" ]; then
        echo "ERROR: JAVA_HOME not setup"
        echo "Startup Aborted!!"
        exit 1
else
        JAVA=$JAVA_HOME
fi

MAINCLASS=org.onap.dcae.collectors.restconf.common.RestConfCollector

# determine a path separator that works for this platform
PATHSEP=":"
case "$(uname -s)" in

        Darwin)
                ;;

         Linux)
                ;;

         CYGWIN*|MINGW32*|MSYS*)
                PATHSEP=";"
                ;;

        *)
                ;;
esac

restConfCollector_stop() {
         echo `date +"%Y%m%d.%H%M%S%3N"` - collector_stop
         collectorPid=`pgrep -f org.onap.dcae.collectors.restconf.common`
         if [ ! -z "$collectorPid" ]; then
                echo "Stopping PID $collectorPid"

                kill -9 $collectorPid
                sleep 5
                if [ ! "$(pgrep -f org.onap.restconf.common)" ]; then
                         echo "restConf Collector has been stopped!!!"
                else
                         echo "restConf Collector is being stopped!!!"
                fi
         else
                echo  "WARNING: No restConf Collector instance is currently running";
                exit 1
         fi

}

case $1 in
        "start")
                restConfCollector_start | tee -a ${BASEDIR}/logs/console.txt
                ;;
        "stop")
                restConfCollector_stop | tee -a ${BASEDIR}/logs/console.txt
                ;;
        *)
                usage
                ;;
esac
