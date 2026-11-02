#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
javac -encoding UTF-8 -cp 'lib/*' -d build src/*.java
exec java -cp 'build:lib/*' ATMApp
