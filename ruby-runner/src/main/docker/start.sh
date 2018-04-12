#!/bin/bash
set -e

IFS=', ' read -r -a array <<< "$WAIT_FOR"
for element in "${array[@]}"
do
    /usr/local/bin/dockerize -wait tcp://$element -wait-retry-interval 2s -timeout 600s
done

if [ ! -z "$SCRIPT" ]
  then
    echo "Writing custom run.rb file from SCRIPT environment variable..."
    rm /opt/run.rb
    touch /opt/run.rb
    echo "$SCRIPT" > /opt/run.rb
fi

echo "All dependencies are online. Starting up this service now."
/usr/local/bin/ruby $1
