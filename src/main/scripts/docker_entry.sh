#!/bin/sh

echo "INFO: USING RESTCONF CONTROLLER"

/opt/app/restconfcollector/bin/restConfCollector.sh stop
/opt/app/restconfcollector/bin/restConfCollector.sh start