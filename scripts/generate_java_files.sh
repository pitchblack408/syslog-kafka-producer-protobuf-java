#!/bin/bash
#ls ../proto
SRC_DIR="../proto/"
DST_DIR="../src/main/java/"
protoc -I=$SRC_DIR --java_out=$DST_DIR $SRC_DIR/syslog.proto
