# --------------------------------------------------------------------
# Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# -----------------------------------------------------------------------

FROM openjdk:8-jre-alpine
LABEL maintainer="dev@ballerina.io"

# Ballerina runtime distribution filename.
ARG BALLERINA_DIST

# Add Ballerina runtime.
COPY ${BALLERINA_DIST} /root/

# Create folders, unzip distribution, create users, & set permissions.
RUN mkdir -p /ballerina/files \
    && addgroup troupe \
    && adduser -S -s /bin/bash -g 'ballerina' -G troupe -D ballerina \
    && apk add --update --no-cache bash \
    && unzip /root/${BALLERINA_DIST} -d /ballerina/ > /dev/null 2>&1 \
    && mv /ballerina/ballerina* /ballerina/runtime \
    && mkdir -p /ballerina/runtime/logs \
    && chown -R ballerina:troupe /ballerina \
    && rm -rf /root/${BALLERINA_DIST} > /dev/null 2>&1 \
    && rm -rf /var/cache/apk/*

ENV BALLERINA_HOME /ballerina/runtime
ENV PATH $BALLERINA_HOME/bin:$PATH

WORKDIR /home/ballerina
VOLUME /home/ballerina

USER ballerina
