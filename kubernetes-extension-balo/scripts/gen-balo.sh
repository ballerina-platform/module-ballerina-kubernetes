#
# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

DISTRIBUTION_PATH=${1}
KUBERNETES_BALO_MAVEN_PROJECT_ROOT=${2}

# TEMP
#rm -rf ${DISTRIBUTION_PATH}/*
#cp -r /Users/hemikak/ballerina/dev/ballerina/distribution/zip/jballerina-tools/build/distributions/jballerina-tools-0.992.0-m2-SNAPSHOT/* ${DISTRIBUTION_PATH}

EXECUTABLE="${DISTRIBUTION_PATH}/bin/ballerina"
KUBERNETES_BALLERINA_PROJECT="${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/src/main/ballerina/ballerinax"
BALLERINAX_BIR_CACHE="${DISTRIBUTION_PATH}/bir-cache/ballerinax/"
BALLERINAX_SYSTEM_LIB="${DISTRIBUTION_PATH}/bre/lib/"

mkdir -p ${BALLERINAX_BIR_CACHE}
mkdir -p ${BALLERINAX_SYSTEM_LIB}

rm -rf ${KUBERNETES_BALLERINA_PROJECT}/target

if ! hash pushd 2>/dev/null
then
    cd ${KUBERNETES_BALLERINA_PROJECT}
    ${EXECUTABLE} compile --skip-tests
    cd -
else
    pushd ${KUBERNETES_BALLERINA_PROJECT} /dev/null 2>&1
        ${EXECUTABLE} compile --skip-tests
    popd > /dev/null 2>&1
fi

cp -r ${KUBERNETES_BALLERINA_PROJECT}/target/* ${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/target

cp -r ${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/target/caches/bir_cache/ballerinax/kubernetes ${BALLERINAX_BIR_CACHE}
cp -r ${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/target/caches/bir_cache/ballerinax/istio ${BALLERINAX_BIR_CACHE}
cp -r ${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/target/caches/bir_cache/ballerinax/openshift ${BALLERINAX_BIR_CACHE}

cp ${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/target/caches/jar_cache/ballerinax/kubernetes/0.0.0/kubernetes.jar ${BALLERINAX_SYSTEM_LIB}
cp ${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/target/caches/jar_cache/ballerinax/istio/0.0.0/istio.jar ${BALLERINAX_SYSTEM_LIB}
cp ${KUBERNETES_BALO_MAVEN_PROJECT_ROOT}/target/caches/jar_cache/ballerinax/openshift/0.0.0/openshift.jar ${BALLERINAX_SYSTEM_LIB}

