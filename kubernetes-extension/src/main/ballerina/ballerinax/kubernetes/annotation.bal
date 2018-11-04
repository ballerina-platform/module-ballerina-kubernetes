// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# External file type for docker.
#
# + source - source path of the file (in your machine)
# + target - target path (inside container)
public type FileConfig record {
    string source;
    string target;
};

# Value for a field.
#
# + fieldPath - Path of the field
public type FieldValue record {
    string fieldPath;
    !...
};

# Value for a secret key.
#
# + name - Name of the secret.
# + key - Key of the secret.
public type SecretKeyValue record {
    string name;
    string key;
    !...
};

# Value for resource field.
#
# + containerName - Name of the container.
# + resource - Resource field
public type ResourceFieldValue record {
    string containerName;
    string ^"resource";
    !...
};

# Value for config map key.
#
# + name - name of the config.
# + key - key of the config.
public type ConfigMapKeyValue record {
    string name;
    string key;
    !...
};

# Value from field.
#
# + fieldRef - Reference for a field.
public type FieldRef record {
    FieldValue fieldRef;
    !...
};

# Value from secret key.
#
# + secretKeyRef - Reference for secret key.
public type SecretKeyRef record {
    SecretKeyValue secretKeyRef;
    !...
};

# Value from resource field.
#
# + resourceFieldRef - Reference for resource field.
public type ResourceFieldRef record {
    ResourceFieldValue resourceFieldRef;
    !...
};

# Value from config map key.
#
# + configMapKeyRef - Reference for config map key.
public type ConfigMapKeyRef record {
    ConfigMapKeyValue configMapKeyRef;
    !...
};

# Image pull policy type field for kubernetes deployment and jobs.
public type ImagePullPolicy "IfNotPresent"|"Always"|"Never";

# Restart policy type field for kubernetes jobs.
public type RestartPolicy "OnFailure"|"Always"|"Never";

# Kubernetes deployment configuration.
#
# + name - Name of the deployment
# + namespace - Kubernetes namespace
# + labels - Map of labels for deployment
# + replicas - Number of replicas
# + enableLiveness - Enable/Disable liveness probe
# + livenessPort - Port to check the liveness
# + initialDelaySeconds - Initial delay in seconds before performing the first probe
# + periodSeconds - Liveness probe interval
# + imagePullPolicy - Kubernetes image pull policy
# + image - Docker image with tag
# + env - Environment varialbe map for containers
# + buildImage - Docker image to be build or not
# + dockerHost - Docker host IP and docker PORT. (e.g minikube IP and docker PORT)
# + username - Username for docker registry
# + password - Password for docker registry
# + baseImage - Base image for docker image building
# + push - Push to remote registry
# + dockerCertPath - Docker certificate path
# + copyFiles - Array of [External files](kubernetes#FileConfig) for docker image
# + singleYAML - Generate a single yaml file with all kubernetes artifacts (services,deployment,ingress,)
# + dependsOn - Services this deployment depends on
# + imagePullSecrets - Image pull secrets
public type DeploymentConfiguration record {
    string name;
    string namespace;
    map labels;
    int replicas;
    boolean enableLiveness;
    int livenessPort;
    int initialDelaySeconds;
    int periodSeconds;
    ImagePullPolicy? imagePullPolicy;
    string image;
    map<string|FieldRef|SecretKeyRef|ResourceFieldRef|ConfigMapKeyRef> env;
    boolean buildImage;
    string dockerHost;
    string username;
    string password;
    string baseImage;
    boolean push;
    string dockerCertPath;
    FileConfig[] copyFiles;
    boolean singleYAML;
    string[] dependsOn;
    string[] imagePullSecrets;
};

# @kubernetes:Deployment annotation to configure deplyoment yaml.
public annotation<service, function, endpoint> Deployment DeploymentConfiguration;

# Session affinity field for kubernetes services.
public type SessionAffinity "None"|"ClientIP";

# Service type field for kubernetes services.
public type ServiceType "NodePort"|"ClusterIP"|"LoadBalancer";

# Kubernetes service configuration.
#
# + name - Name of the service
# + labels - Map of labels for deployment
# + port - Service port
# + sessionAffinity - Session affinity for pods
# + serviceType - Service type of the service
public type ServiceConfiguration record {
    string name;
    map<string>? labels;
    int? port;
    SessionAffinity? sessionAffinity;
    ServiceType? serviceType;
    !...
};

# @kubernetes:Service annotation to configure service yaml.
public annotation<endpoint, service> Service ServiceConfiguration;

# Kubernetes ingress configuration.
#
# + name - Name of the ingress
# + endpointName - Name of the endpoint ingress attached
# + labels - Label map for ingress
# + annotations - Map of additional annotations
# + hostname - Host name of the ingress
# + path - Resource path
# + targetPath - Target path for url rewrite
# + ingressClass - Ingress class
# + enableTLS - Enable/Disable ingress TLS
public type IngressConfiguration record {
    string name;
    string endpointName;
    map labels;
    map annotations;
    string hostname;
    string path;
    string targetPath;
    string ingressClass;
    boolean enableTLS;
};

# @kubernetes:Ingress annotation to configure ingress yaml.
public annotation<service, endpoint> Ingress IngressConfiguration;

# Kubernetes Horizontal Pod Autoscaler configuration
#
# + name - Name of the Autoscaler
# + labels - Labels for Autoscaler
# + minReplicas - Minimum number of replicas
# + maxReplicas - Maximum number of replicas
# + cpuPercentage - CPU percentage to start scaling
public type PodAutoscalerConfig record {
    string name;
    map labels;
    int minReplicas;
    int maxReplicas;
    int cpuPercentage;
};

# @kubernetes:HPA annotation to configure horizontal pod autoscaler yaml.
public annotation<service> HPA PodAutoscalerConfig;

# Kubernetes secret volume mount.
#
# + name - Name of the volume mount
# + mountPath - Mount path
# + readOnly - Is mount read only
# + data - Paths to data files as an array
public type Secret record {
    string name;
    string mountPath;
    boolean readOnly;
    string[] data;
};

#Secret volume mount configurations for kubernetes.
#
# + secrets - Array of [Secret](kubernetes.html#Secret)
public type SecretMount record {
    Secret[] secrets;
};

# @kubernetes:Secret annotation to configure secrets.
public annotation<service> Secret SecretMount;

# Kubernetes Config Map volume mount.
#
# + name - Name of the volume mount
# + mountPath - Mount path
# + readOnly - Is mount read only
# + data - Paths to data files
public type ConfigMap record {
    string name;
    string mountPath;
    boolean readOnly;
    string[] data;
};

# Secret volume mount configurations for kubernetes.
#
# + ballerinaConf - path to ballerina configuration file
# + configMaps - Array of [ConfigMap](kubernetes.html#ConfigMap)
public type ConfigMapMount record {
    string ballerinaConf;
    ConfigMap[] configMaps;
};

# @kubernetes:ConfigMap annotation to configure config maps.
public annotation<service> ConfigMap ConfigMapMount;

# Kubernetes Persistent Volume Claim.
#
# + name - Name of the volume claim
# + mountPath - Mount Path
# + accessMode - Access mode
# + volumeClaimSize - Size of the volume claim
# + annotations - Map of annotation values
# + readOnly - Is mount read only
public type PersistentVolumeClaimConfig record {
    string name;
    string mountPath;
    string accessMode;
    string volumeClaimSize;
    map annotations;
    boolean readOnly;
};

# Persistent Volume Claims configurations for kubernetes.
#
# + volumeClaims - Array of [PersistentVolumeClaimConfig](kubernetes.html#PersistentVolumeClaimConfig)
public type PersistentVolumeClaims record {
    PersistentVolumeClaimConfig[] volumeClaims;
};

# @kubernetes:PersistentVolumeClaim annotation to configure Persistent Volume Claims.
public annotation<service> PersistentVolumeClaim PersistentVolumeClaims;

# Scopes for kubernetes resource quotas
public type ResourceQuotaScope "Terminating"|"NotTerminating"|"BestEffort"|"NotBestEffort";

# Kubernetes Resource Quota
#
# + name - Name of the resource quota
# + labels - Labels for resource quota
# + hard - Quotas for the resources
# + scopes - Scopes of the quota
public type ResourceQuotaConfig record {
    string name;
    map<string>? labels;
    map<string> hard;
    ResourceQuotaScope[] scopes = [];
    !...
};

# Resource Quota configuration for kubernetes.
#
# + resourceQuotas - Array of [ResourceQuotaConfig](kubernetes.html#ResourceQuotaConfig)
public type ResourceQuotas record {
    ResourceQuotaConfig[] resourceQuotas;
    !...
};

# @kubernetes:ResourcesQuotas annotation to configure Resource Quotas.
public annotation<service, function, endpoint> ResourceQuota ResourceQuotas;

# Kubernetes job configuration.
#
# + name - Name of the job
# + labels - Labels for job
# + restartPolicy - Restart policy
# + backoffLimit - Backoff limit
# + activeDeadlineSeconds - Active deadline seconds
# + schedule - Schedule for cron jobs
# + env - Environment varialbes for container
# + image - Docker image with tag
# + imagePullPolicy - Policy for pulling an image
# + buildImage - Docker image to be build or not
# + dockerHost - Docker host IP and docker PORT. (e.g minikube IP and docker PORT)
# + username - Username for docker registry
# + password - Password for docker registry
# + baseImage - Base image for docker image building
# + push - Push to remote registry
# + dockerCertPath - Docker cert path
# + copyFiles - Array of [External files](kubernetes#FileConfig) for docker image
# + imagePullSecrets - Image pull secrets
# + singleYAML - Generate a single yaml file with all kubernetes artifacts (services,deployment,ingress,)
public type JobConfig record {
    string name;
    map labels;
    RestartPolicy? restartPolicy;
    string backoffLimit;
    string activeDeadlineSeconds;
    string schedule;
    map<string|FieldRef|SecretKeyRef|ResourceFieldRef|ConfigMapKeyRef> env;
    ImagePullPolicy? imagePullPolicy;
    string image;
    boolean buildImage;
    string dockerHost;
    string username;
    string password;
    string baseImage;
    boolean push;
    string dockerCertPath;
    FileConfig[] copyFiles;
    string[] imagePullSecrets;
    boolean singleYAML;
};

# @kubernetes:Job annotation to configure kubernetes jobs.
public annotation<function> Job JobConfig;

public type IstioPortProtocol "HTTP"|"HTTPS"|"GRPC"|"HTTP2"|"MONGO"|"TCP"|"TLS";

public type IstioPortConfig record {
    int number;
    IstioPortProtocol protocol;
    string? name;
    !...
};

public type IstioTLSOptionMode "PASSTHROUGH"|"SIMPLE"|"MUTUAL";

public type IstioTLSOptionConfig record {
    boolean httpsRedirect;
    IstioTLSOptionMode? mode;
    string? serverCertificate;
    string? privateKey;
    string? caCertificates;
    string[]? subjectAltNames;
    !...
};

public type IstioServerConfig record {
    IstioPortConfig port;
    string[] hosts;
    IstioTLSOptionConfig? tls;
    !...
};

public type IstioGatewayConfig record {
    string name;
    string? namespace;
    map<string>? labels;
    map<string>? annotations;
    map<string> selector;
    IstioServerConfig[] servers;
    !...
};

public annotation<service, endpoint> IstioGateway IstioGatewayConfig;

public type StringMatch record {
    string? exact;
    string? prefix;
    string? regex;
    !...
};

public type HTTPMatchRequestConfig record {
    StringMatch? uri;
    StringMatch? scheme;
    StringMatch? method;
    StringMatch? authority;
    map<StringMatch>? headers;
    int? port;
    map<string>? sourceLabels;
    string[]? gateways;
    !...
};

public type PortSelectorConfig record {
    int number;
    !...
};

public type DestinationConfig record {
    string host;
    string subset;
    PortSelectorConfig? port;
    !...
};

public type DestinationWeightConfig record {
    DestinationConfig destination;
    int weight;
    !...
};

public type HTTPRedirectRewriteConfig record {
    string? uri;
    string? authority;
    !...
};


public type HTTPRetryConfig record {
    int attempts;
    string perTryTimeout;
    !...
};

public type HTTPFaultInjectionDelayConfig record {
    int percent;
    string fixedDelay;
    !...
};

public type HTTPFaultInjectionAbortConfig record {
    int percent;
    int httpStatus;
    !...
};

public type HTTPFaultInjectionConfig record {
    HTTPFaultInjectionDelayConfig? delay;
    HTTPFaultInjectionAbortConfig? ^"abort";
    !...
};

public type CorsPolicyConfig record {
    string[]? allowOrigin;
    string[]? allowMethods;
    string[]? allowHeaders;
    string[]? exposeHeaders;
    string? maxAge;
    boolean allowCredentials;
    !...
};

public type HTTPRouteConfig record {
    HTTPMatchRequestConfig[]? ^"match";
    DestinationWeightConfig[]? route;
    HTTPRedirectRewriteConfig? redirect;
    HTTPRedirectRewriteConfig? rewrite;
    string? ^"timeout";
    HTTPRetryConfig? ^"retries";
    HTTPFaultInjectionConfig? fault;
    DestinationConfig? mirror;
    CorsPolicyConfig? corsPolicy;
    map<string>? appendHeaders;
    !...
};

public type TLSMatchAttributesConfig record {
    string[] sniHosts;
    string[] destinationSubnets;
    int? port;
    map<string>? sourceLabels;
    string[] gateways;
    !...
};

public type TLSRouteConfig record {
    TLSMatchAttributesConfig[] ^"match";
    DestinationWeightConfig[] route;
    !...
};

public type L4MatchAttributesConfig record {
    string[] destinationSubnets;
    int? port;
    map<string>? sourceLabels;
    string[] gateways;
    !...
};

public type TCPRoute record {
    L4MatchAttributesConfig[] ^"match";
    DestinationWeightConfig[] route;
    !...
};

public type IstioVirutalServiceConfig record {
    string name;
    string? namespace;
    map<string>? labels;
    map<string>? annotations;
    string[] hosts;
    string[]? gateways;
    HTTPRouteConfig[]? http;
    TLSRouteConfig[]? tls;
    TCPRoute[]? tcp;
    !...
};

public annotation<service, endpoint> IstioVirutalService IstioVirutalServiceConfig;