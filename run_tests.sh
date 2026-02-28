#!/bin/bash
MAVEN="/c/Program Files/NetBeans-18/netbeans/java/maven/bin/mvn"
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.11.9-hotspot"
cd "/e/AdvancePrgramming/RoomReservationSystem"
"$MAVEN" test 2>&1
