#
# Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#!/bin/bash
set -e
DISTRIBUTION_PATH=${1}
KUBERNETES_BALO_MAVEN_PROJECT_ROOT=${2}

# TEMP
#rm -rf ${DISTRIBUTION_PATH}/*
#cp -r /Users/hemikak/ballerina/dev/ballerina/distribution/zip/jballerina-tools/build/distributions/jballerina-tools-1.0.0-beta-SNAPSHOT/* ${DISTRIBUTION_PATH}

EXECUTABLE="${DISTRIBUTION_PATH}/bin/ballerina"
KUBERNETES_BALLERINA_PROJECT="${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/src/main/ballerina/"
DISTRIBUTION_BIR_CACHE="${DISTRIBUTION_PATH}/bir-cache/ballerina/"
DISTRIBUTION_SYSTEM_LIB="${DISTRIBUTION_PATH}/bre/lib/"

mkdir -p ${DISTRIBUTION_BIR_CACHE}
mkdir -p ${DISTRIBUTION_SYSTEM_LIB}

# build kubernetes, istio, openshift annotation defintions.
if ! hash pushd 2>/dev/null
then
    cd ${KUBERNETES_BALLERINA_PROJECT}
    ${EXECUTABLE} clean
    JAVA_OPTS="-DBALLERINA_DEV_COMPILE_BALLERINA_ORG=true" ${EXECUTABLE} build -c -a --skip-tests
    mv target/caches/jar_cache/ballerina/kubernetes/ballerina-kubernetes-.jar target/caches/jar_cache/ballerina/kubernetes/kubernetes.jar
    mv target/caches/jar_cache/ballerina/istio/ballerina-istio-.jar target/caches/jar_cache/ballerina/istio/istio.jar
    mv target/caches/jar_cache/ballerina/openshift/ballerina-openshift-.jar target/caches/jar_cache/ballerina/openshift/openshift.jar
    mv target/caches/jar_cache/ballerina/knative/ballerina-knative-.jar target/caches/jar_cache/ballerina/knative/knative.jar
    cd -
else
    pushd ${KUBERNETES_BALLERINA_PROJECT} /dev/null 2>&1
        ${EXECUTABLE} clean
        JAVA_OPTS="-DBALLERINA_DEV_COMPILE_BALLERINA_ORG=true" ${EXECUTABLE} build -c -a --skip-tests
        mv target/caches/jar_cache/ballerina/kubernetes/ballerina-kubernetes-.jar target/caches/jar_cache/ballerina/kubernetes/kubernetes.jar
        mv target/caches/jar_cache/ballerina/istio/ballerina-istio-.jar target/caches/jar_cache/ballerina/istio/istio.jar
        mv target/caches/jar_cache/ballerina/openshift/ballerina-openshift-.jar target/caches/jar_cache/ballerina/openshift/openshift.jar
        mv target/caches/jar_cache/ballerina/knative/ballerina-knative-.jar target/caches/jar_cache/ballerina/knative/knative.jar
    popd > /dev/null 2>&1
fi

# update distribution with kubernetes, istio, openshift, knative annotation artifacts.

mkdir -p ${DISTRIBUTION_BIR_CACHE}kubernetes/0.0.0/
mkdir -p ${DISTRIBUTION_BIR_CACHE}istio/0.0.0/
mkdir -p ${DISTRIBUTION_BIR_CACHE}openshift/0.0.0/
mkdir -p ${DISTRIBUTION_BIR_CACHE}knative/0.0.0/

cp ${KUBERNETES_BALLERINA_PROJECT}/target/caches/bir_cache/ballerina/kubernetes/kubernetes.bir ${DISTRIBUTION_BIR_CACHE}kubernetes/0.0.0/
cp ${KUBERNETES_BALLERINA_PROJECT}/target/caches/bir_cache/ballerina/istio/istio.bir ${DISTRIBUTION_BIR_CACHE}istio/0.0.0/
cp ${KUBERNETES_BALLERINA_PROJECT}/target/caches/bir_cache/ballerina/openshift/openshift.bir ${DISTRIBUTION_BIR_CACHE}openshift/0.0.0/
cp ${KUBERNETES_BALLERINA_PROJECT}/target/caches/bir_cache/ballerina/knative/knative.bir ${DISTRIBUTION_BIR_CACHE}knative/0.0.0/

cp ${KUBERNETES_BALLERINA_PROJECT}/Ballerina.toml ${DISTRIBUTION_BIR_CACHE}kubernetes/0.0.0/
cp ${KUBERNETES_BALLERINA_PROJECT}/Ballerina.toml ${DISTRIBUTION_BIR_CACHE}istio/0.0.0/
cp ${KUBERNETES_BALLERINA_PROJECT}/Ballerina.toml ${DISTRIBUTION_BIR_CACHE}openshift/0.0.0/
cp ${KUBERNETES_BALLERINA_PROJECT}/Ballerina.toml ${DISTRIBUTION_BIR_CACHE}knative/0.0.0/

cp ${KUBERNETES_BALLERINA_PROJECT}/target/caches/jar_cache/ballerina/kubernetes/kubernetes.jar ${DISTRIBUTION_SYSTEM_LIB}
cp ${KUBERNETES_BALLERINA_PROJECT}/target/caches/jar_cache/ballerina/istio/istio.jar ${DISTRIBUTION_SYSTEM_LIB}
cp ${KUBERNETES_BALLERINA_PROJECT}/target/caches/jar_cache/ballerina/openshift/openshift.jar ${DISTRIBUTION_SYSTEM_LIB}
cp ${KUBERNETES_BALLERINA_PROJECT}/target/caches/jar_cache/ballerina/knative/knative.jar ${DISTRIBUTION_SYSTEM_LIB}
