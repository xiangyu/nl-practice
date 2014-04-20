#!/bin/sh

rm -f npylm/*.class
#javac -cp lib npylm/NPYLM.java
javac -cp .:./lib/commons-math3-3.2.jar npylm/NPYLM.java
