@echo off

REM ---------------------------------------------------------------------------
REM   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM   Licensed under the Apache License, Version 2.0 (the "License");
REM   you may not use this file except in compliance with the License.
REM   You may obtain a copy of the License at
REM
REM   http://www.apache.org/licenses/LICENSE-2.0
REM
REM   Unless required by applicable law or agreed to in writing, software
REM   distributed under the License is distributed on an "AS IS" BASIS,
REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM   See the License for the specific language governing permissions and
REM   limitations under the License.

set DISTRIBUTION_PATH=%1
set KUBERNETES_ANNOTATIONS_MAVEN_PROJECT_ROOT=%2

set EXECUTABLE=%DISTRIBUTION_PATH%\bin\ballerina
set KUBERNETES_BALLERINA_PROJECT=%KUBERNETES_ANNOTATIONS_MAVEN_PROJECT_ROOT%\src\main\ballerina\
set DISTRIBUTION_BIR_CACHE=%DISTRIBUTION_PATH%\bir-cache\ballerina\
set DISTRIBUTION_SYSTEM_LIB=%DISTRIBUTION_PATH%\bre\lib\

if not exist "%DISTRIBUTION_BIR_CACHE%" mkdir "%DISTRIBUTION_BIR_CACHE%"
if not exist "%DISTRIBUTION_SYSTEM_LIB%" mkdir "%DISTRIBUTION_SYSTEM_LIB%"

pushd %KUBERNETES_BALLERINA_PROJECT%
cmd /C %EXECUTABLE% clean
set JAVA_OPTS="-DBALLERINA_DEV_COMPILE_BALLERINA_ORG=true"
cmd /C %EXECUTABLE% build -c -a --skip-tests
ren %KUBERNETES_BALLERINA_PROJECT%target\caches\jar_cache\ballerina\kubernetes\ballerina-kubernetes-.jar kubernetes.jar
ren %KUBERNETES_BALLERINA_PROJECT%target\caches\jar_cache\ballerina\istio\ballerina-istio-.jar istio.jar
ren %KUBERNETES_BALLERINA_PROJECT%target\caches\jar_cache\ballerina\openshift\ballerina-openshift-.jar openshift.jar
ren %KUBERNETES_BALLERINA_PROJECT%target\caches\jar_cache\ballerina\knative\ballerina-knative-.jar knative.jar
popd

if not exist "%DISTRIBUTION_BIR_CACHE%kubernetes\0.0.0\" mkdir "%DISTRIBUTION_BIR_CACHE%kubernetes\0.0.0\"
if not exist "%DISTRIBUTION_BIR_CACHE%istio\0.0.0\" mkdir "%DISTRIBUTION_BIR_CACHE%istio\0.0.0\"
if not exist "%DISTRIBUTION_BIR_CACHE%openshift\0.0.0\" mkdir "%DISTRIBUTION_BIR_CACHE%openshift\0.0.0\"
if not exist "%DISTRIBUTION_BIR_CACHE%knative\0.0.0\" mkdir "%DISTRIBUTION_BIR_CACHE%knative\0.0.0\"

COPY "%KUBERNETES_BALLERINA_PROJECT%target\caches\bir_cache\ballerina\kubernetes\kubernetes.bir" "%DISTRIBUTION_BIR_CACHE%kubernetes\0.0.0\"
COPY "%KUBERNETES_BALLERINA_PROJECT%target\caches\bir_cache\ballerina\istio\istio.bir" "%DISTRIBUTION_BIR_CACHE%istio\0.0.0\"
COPY "%KUBERNETES_BALLERINA_PROJECT%target\caches\bir_cache\ballerina\openshift\openshift.bir" "%DISTRIBUTION_BIR_CACHE%openshift\0.0.0\"
COPY "%KUBERNETES_BALLERINA_PROJECT%target\caches\bir_cache\ballerina\knative\knative.bir" "%DISTRIBUTION_BIR_CACHE%knative\0.0.0\"

COPY "%KUBERNETES_BALLERINA_PROJECT%Ballerina.toml" "%DISTRIBUTION_BIR_CACHE%kubernetes\0.0.0\"
COPY "%KUBERNETES_BALLERINA_PROJECT%Ballerina.toml" "%DISTRIBUTION_BIR_CACHE%istio\0.0.0\"
COPY "%KUBERNETES_BALLERINA_PROJECT%Ballerina.toml" "%DISTRIBUTION_BIR_CACHE%openshift\0.0.0\"
COPY "%KUBERNETES_BALLERINA_PROJECT%Ballerina.toml" "%DISTRIBUTION_BIR_CACHE%knative\0.0.0\"

COPY "%KUBERNETES_BALLERINA_PROJECT%target\caches\jar_cache\ballerina\kubernetes\kubernetes.jar" "%DISTRIBUTION_SYSTEM_LIB%"
COPY "%KUBERNETES_BALLERINA_PROJECT%target\caches\jar_cache\ballerina\istio\istio.jar" "%DISTRIBUTION_SYSTEM_LIB%"
COPY "%KUBERNETES_BALLERINA_PROJECT%target\caches\jar_cache\ballerina\openshift\openshift.jar" "%DISTRIBUTION_SYSTEM_LIB%"
COPY "%KUBERNETES_BALLERINA_PROJECT%target\caches\jar_cache\ballerina\knative\knative.jar" "%DISTRIBUTION_SYSTEM_LIB%"
