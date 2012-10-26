#!/bin/sh

base_dir=$(dirname $0)/..
CLASSPATH=$(echo $base_dir/target/*.jar $base_dir/lib/*.jar | tr " " :)

USER_ARGS="$@"
JVM_ARGS="-Xms1024m -Xmx2048m"
EXTERNAL_LIBS="/usr/local/lib"
MAIN="com.voidsearch.topicbox.server.TopicboxServer"

java -server -cp $CLASSPATH -Djava.library.path=${EXTERNAL_LIBS} ${JVM_ARGS} ${MAIN} ${USER_ARGS}