#!/bin/sh

java -cp .:./lib/commons-math3-3.2.jar -Xmx4g npylm/NPYLM ../data/train.txt
