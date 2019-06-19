#!/bin/sh
###
# ============LICENSE_START=======================================================
# org.onap.dcaegen2.restconfcollector
# ================================================================================
# Copyright (C) 2018-2019 Huawei. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
###

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
        collectorPid=`pidof java`

        if [ ! -z "$collectorPid" ]; then
                echo  "WARNING: restConf Collector already running as PID $collectorPid" | tee -a ${BASEDIR}/logs/console.txt
                echo  "Startup Aborted!!!" | tee -a ${BASEDIR}/logs/console.txt
                exit 1
        fi


        # run java. The classpath is the etc dir for config files, and the lib dir
        # for all the jars.

        cd ${BASEDIR}
        nohup $JAVA -cp "etc${PATHSEP}lib/*" $JAVA_OPTS -Dhttps.protocols=TLSv1.1,TLSv1.2 $MAINCLASS $* &
        if [ $? -ne 0 ]; then
                echo "restConf Collector has been started!!!" | tee -a ${BASEDIR}/logs/console.txt
        fi
}


# use JAVA_HOME if provided
if [ -z "$JAVA_HOME" ]; then
        echo "ERROR: JAVA_HOME not setup"
        echo "Startup Aborted!!"
        exit 1
else
        echo "$JAVA_HOME"
        JAVA=$JAVA_HOME/bin/java
fi

MAINCLASS=org.onap.dcae.RestConfCollector

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
         collectorPid=`pidof java`
         if [ ! -z "$collectorPid" ]; then
                echo "Stopping PID $collectorPid"

                kill -9 $collectorPid
                sleep 5
                if [ ! $(pidof java) ]; then
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
