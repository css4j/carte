#!/usr/bin/bash
CARTE_VERSION=0.1.0-SNAPSHOT
java -jar ../build/libs/carte-jmh-${CARTE_VERSION}-all.jar --config=dom-benchmark.xml "$@"
