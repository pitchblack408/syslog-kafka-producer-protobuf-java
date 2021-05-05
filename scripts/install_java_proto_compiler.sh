#!/bin/bash -e

# USING PACKAGE MANAGER
# MAC using brew:
brew install protobuf

#LINUX using apt or apt-get, for example:
#$ apt install -y protobuf-compiler
#$ protoc --version  # Ensure compiler version is 3+

# BINARY FROM GOOGLE
# To install protobuf, you need to install the Protocol Compiler
#curl -L -o /tmp/protoc-3.15.8-linux-x86_32.zip https://github.com/protocolbuffers/protobuf/releases/download/v3.15.8/protoc-3.15.8-linux-x86_32.zip
#cd /tmp/ && unzip protoc-3.15.8-linux-x86_32.zip
#mv /tmp/protoc-3.15.8-osx-x86_64 /opt/protoc
#ln -s /opt/protoc/bin/protoc /usr/bin/protoc

# TODO not sure what pick as runtime, just put java here because the project is in java
# Didn't use because used brew package tool
#Protobuf Runtime
#curl -L -o /tmp/protobuf-java-3.15.8.tar.gz https://github.com/protocolbuffers/protobuf/releases/download/v3.15.8/protobuf-java-3.15.8.tar.gz
#cd /tmp/ && tar -xzvf protobuf-java-3.15.8.tar.gz

