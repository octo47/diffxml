#!/bin/sh

set +x
binDir=$(dirname $0)
homeDir=${binDir}/..
cpath=${homeDir}
for f in $(find ${homeDir}/lib -type f); do
  cpath="$cpath:$f"
done
clz=$1
shift
java -cp ${cpath} ${clz} $@