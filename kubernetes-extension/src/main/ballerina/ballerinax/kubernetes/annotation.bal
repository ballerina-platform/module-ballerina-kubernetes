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

# Metadata for artifacts
#
# + name - Name of the resource
# + namespace - Kubernetes namespace to be used
# + labels - Map of labels for the resource
# + annotations - Map of annotations for resource
public type Metadata record {
    string name?;
    string namespace?;
    map<string> labels?;
    map<string> annotations?;
    !...;
};

# External file type for docker.
#
# + source - source path of the file (in your machine)
# + target - target path (inside container)
public type FileConfig record {
    string source;
    string target;
    !...;
};

# Value for a field.
#
# + fieldPath - Path of the field
public type FieldValue record {
    string fieldPath;
    !...;
};

# Value for a secret key.
#
# + name - Name of the secret.
# + key - Key of the secret.
public type SecretKeyValue record {
    string name;
    string key;
    !...;
};

# Value for resource field.
#
# + containerName - Name of the container.
# + resource - Resource field
public type ResourceFieldValue record {
    string containerName?;
    string ^"resource";
    !...;
};

# Value for config map key.
#
# + name - name of the config.
# + key - key of the config.
public type ConfigMapKeyValue record {
    string name;
    string key;
    !...;
};

# Value from field.
#
# + fieldRef - Reference for a field.
public type FieldRef record {
    FieldValue fieldRef;
    !...;
};

# Value from secret key.
#
# + secretKeyRef - Reference for secret key.
public type SecretKeyRef record {
    SecretKeyValue secretKeyRef;
    !...;
};

# Value from resource field.
#
# + resourceFieldRef - Reference for resource field.
public type ResourceFieldRef record {
    ResourceFieldValue resourceFieldRef;
    !...;
};

# Value from config map key.
#
# + configMapKeyRef - Reference for config map key.
public type ConfigMapKeyRef record {
    ConfigMapKeyValue configMapKeyRef;
    !...;
};

public const string IMAGE_PULL_POLICY_IF_NOT_PRESENT = "IfNotPresent";
public const string IMAGE_PULL_POLICY_ALWAYS = "Always";
public const string IMAGE_PULL_POLICY_NEVER = "Never";

# Image pull policy type field for kubernetes deployment and jobs.
public type ImagePullPolicy "IfNotPresent"|"Always"|"Never";

# Kubernetes deployment configuration.
#
# + podAnnotations - Map of annotations for pods
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
    *Metadata;
    map<string> podAnnotations?;
    int replicas?;
    boolean enableLiveness?;
    int livenessPort?;
    int initialDelaySeconds?;
    int periodSeconds?;
    ImagePullPolicy imagePullPolicy = IMAGE_PULL_POLICY_IF_NOT_PRESENT;
    string image?;
    map<string|FieldRef|SecretKeyRef|ResourceFieldRef|ConfigMapKeyRef> env?;
    boolean buildImage?;
    string dockerHost?;
    string username?;
    string password?;
    string baseImage?;
    boolean push?;
    string dockerCertPath?;
    FileConfig[] copyFiles?;
    boolean singleYAML = true;
    string[] dependsOn?;
    string[] imagePullSecrets?;
    !...;
};

# @kubernetes:Deployment annotation to configure deplyoment yaml.
public annotation<service, function, listener> Deployment DeploymentConfiguration;

public const string SESSION_AFFINITY_NONE = "None";
public const string SESSION_AFFINITY_CLIENT_IP = "ClientIP";

# Session affinity field for kubernetes services.
public type SessionAffinity "None"|"ClientIP";

public const string SERVICE_TYPE_NORD_PORT = "NodePort";
public const string SERVICE_TYPE_CLUSTER_IP = "ClusterIP";
public const string SERVICE_TYPE_LOAD_BALANCER = "LoadBalancer";

# Service type field for kubernetes services.
public type ServiceType "NodePort"|"ClusterIP"|"LoadBalancer";

# Kubernetes service configuration.
#
# + port - Service port
# + sessionAffinity - Session affinity for pods
# + serviceType - Service type of the service
public type ServiceConfiguration record {
    *Metadata;
    int port?;
    SessionAffinity sessionAffinity = SESSION_AFFINITY_NONE;
    ServiceType serviceType = SERVICE_TYPE_CLUSTER_IP;
    !...;
};

# @kubernetes:Service annotation to configure service yaml.
public annotation<listener, service> Service ServiceConfiguration;

# Kubernetes ingress configuration.
#
# + listenerName - Name of the listener ingress attached
# + hostname - Host name of the ingress
# + path - Resource path
# + targetPath - Target path for url rewrite
# + ingressClass - Ingress class
# + enableTLS - Enable/Disable ingress TLS
public type IngressConfiguration record {
    *Metadata;
    string listenerName?;
    string hostname;
    string path?;
    string targetPath?;
    string ingressClass?;
    boolean enableTLS?;
    !...;
};

# @kubernetes:Ingress annotation to configure ingress yaml.
public annotation<service, listener> Ingress IngressConfiguration;

# Kubernetes Horizontal Pod Autoscaler configuration
#
# + minReplicas - Minimum number of replicas
# + maxReplicas - Maximum number of replicas
# + cpuPercentage - CPU percentage to start scaling
public type PodAutoscalerConfig record {
    *Metadata;
    int minReplicas?;
    int maxReplicas?;
    int cpuPercentage?;
    !...;
};

# @kubernetes:HPA annotation to configure horizontal pod autoscaler yaml.
public annotation<service> HPA PodAutoscalerConfig;

# Kubernetes secret volume mount.
#
# + mountPath - Mount path
# + readOnly - Is mount read only
# + data - Paths to data files as an array
public type Secret record {
    *Metadata;
    string mountPath;
    boolean readOnly = true;
    string[] data;
    !...;
};

#Secret volume mount configurations for kubernetes.
#
# + secrets - Array of [Secret](kubernetes.html#Secret)
public type SecretMount record {
    Secret[] secrets;
    !...;
};

# @kubernetes:Secret annotation to configure secrets.
public annotation<service> Secret SecretMount;

# Kubernetes Config Map volume mount.
#
# + mountPath - Mount path
# + readOnly - Is mount read only
# + data - Paths to data files
public type ConfigMap record {
    *Metadata;
    string mountPath;
    boolean readOnly = true;
    string[] data;
    !...;
};

# Secret volume mount configurations for kubernetes.
#
# + conf - path to ballerina configuration file
# + configMaps - Array of [ConfigMap](kubernetes.html#ConfigMap)
public type ConfigMapMount record {
    string conf;
    ConfigMap[] configMaps?;
    !...;
};

# @kubernetes:ConfigMap annotation to configure config maps.
public annotation<service> ConfigMap ConfigMapMount;

# Kubernetes Persistent Volume Claim.
#
# + mountPath - Mount Path
# + accessMode - Access mode
# + volumeClaimSize - Size of the volume claim
# + readOnly - Is mount read only
public type PersistentVolumeClaimConfig record {
    *Metadata;
    string mountPath;
    string accessMode;
    string volumeClaimSize;
    boolean readOnly;
    !...;
};

# Persistent Volume Claims configurations for kubernetes.
#
# + volumeClaims - Array of [PersistentVolumeClaimConfig](kubernetes.html#PersistentVolumeClaimConfig)
public type PersistentVolumeClaims record {
    PersistentVolumeClaimConfig[] volumeClaims;
    !...;
};

# @kubernetes:PersistentVolumeClaim annotation to configure Persistent Volume Claims.
public annotation<service> PersistentVolumeClaim PersistentVolumeClaims;

# Scopes for kubernetes resource quotas
public type ResourceQuotaScope "Terminating"|"NotTerminating"|"BestEffort"|"NotBestEffort";

# Kubernetes Resource Quota
#
# + hard - Quotas for the resources
# + scopes - Scopes of the quota
public type ResourceQuotaConfig record {
    *Metadata;
    map<string> hard;
    ResourceQuotaScope?[] scopes = [];
    !...;
};

# Resource Quota configuration for kubernetes.
#
# + resourceQuotas - Array of [ResourceQuotaConfig](kubernetes.html#ResourceQuotaConfig)
public type ResourceQuotas record {
    ResourceQuotaConfig[] resourceQuotas;
    !...;
};

# @kubernetes:ResourcesQuotas annotation to configure Resource Quotas.
public annotation<service, function, listener> ResourceQuota ResourceQuotas;

public const string RESTART_POLICY_ON_FAILURE = "OnFailure";
public const string RESTART_POLICY_ALWAYS = "Always";
public const string RESTART_POLICY_NEVER = "Never";

# Restart policy type field for kubernetes jobs.
public type RestartPolicy "OnFailure"|"Always"|"Never";

# Kubernetes job configuration.
#
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
    *Metadata;
    RestartPolicy restartPolicy = RESTART_POLICY_NEVER;
    string backoffLimit?;
    string activeDeadlineSeconds?;
    string schedule?;
    map<string|FieldRef|SecretKeyRef|ResourceFieldRef|ConfigMapKeyRef> env?;
    ImagePullPolicy imagePullPolicy = IMAGE_PULL_POLICY_IF_NOT_PRESENT;
    string image?;
    boolean buildImage = true;
    string dockerHost?;
    string username?;
    string password?;
    string baseImage?;
    boolean push = false;
    string dockerCertPath?;
    FileConfig[] copyFiles?;
    string[] imagePullSecrets?;
    boolean singleYAML = true;
    !...;
};

# @kubernetes:Job annotation to configure kubernetes jobs.
public annotation<function> Job JobConfig;

# Types of protocols of a port.
public type IstioPortProtocol "HTTP"|"HTTPS"|"GRPC"|"HTTP2"|"MONGO"|"TCP"|"TLS";

# Port of a service.
#
# + number - The port number.
# + protocol - The protocol exposed by the port.
# + name - Label for the port.
public type IstioPortConfig record {
    int number;
    IstioPortProtocol protocol;
    string name;
    !...;
};

# TLS mode enforced by the proxy.
public type IstioTLSOptionMode "PASSTHROUGH"|"SIMPLE"|"MUTUAL";

# Istio gateway server tls option configurations.
#
# + httpsRedirect - If set to true, the load balancer will send a 301 redirect for all http connections, asking the clients to use HTTPS.
# + mode - Indicates whether connections to this port should be secured using TLS. The value of this field determines how TLS is enforced.
# + serverCertificate - REQUIRED if mode is SIMPLE or MUTUAL. The path to the file holding the server-side TLS certificate to use.
# + privateKey - REQUIRED if mode is SIMPLE or MUTUAL. The path to the file holding the server’s private key.
# + caCertificates - REQUIRED if mode is MUTUAL. The path to a file containing certificate authority certificates to use in verifying a presented client side certificate.
# + subjectAltNames - A list of alternate names to verify the subject identity in the certificate presented by the client.
public type IstioTLSOptionConfig record {
    boolean httpsRedirect = false;
    IstioTLSOptionMode mode?;
    string serverCertificate?;
    string privateKey?;
    string caCertificates?;
    string[] subjectAltNames?;
    !...;
};

# Istio gateway server configuration to describe the properties of the proxy on a given load balancer.
#
# + port - The port of the proxy.
# + hosts - List of hosts exposed by the gateway.
# + tls - TLS options.
public type IstioServerConfig record {
    IstioPortConfig port;
    string[] hosts;
    IstioTLSOptionConfig tls?;
    !...;
};

# Istio gateway annotation configuration.
#
# + selector - Specific set of pods/VMs on which this gateway configuration should be applied.
# + servers - List of servers to pass.
public type IstioGatewayConfig record {
    *Metadata;
    map<string> selector?;
    IstioServerConfig?[] servers?;
    !...;
};

# @kubernetes:IstioGateway annotation to generate istio gateways.
public annotation<service, listener> IstioGateway IstioGatewayConfig;

# Configuration for a string match.
#
# + exact - Exact match string.
# + prefix - Prefix to match string.
# + regex - Regex to match string.
public type StringMatch record {
    string exact?;
    string prefix?;
    string regex?;
    !...;
};

# Configuration for a matching requests.
#
# + uri - URI to match values.
# + scheme - URI Scheme values.
# + method - HTTP Method values.
# + authority - HTTP Authority values.
# + headers - HTTP headers to match.
# + port - Port on the host that is being addressed.
# + sourceLabels - One or more labels that constrain the applicability of a rule to workloads with the given labels.
# + gateways - Names of gateways the rules should be applied to.
public type HTTPMatchRequestConfig record {
    StringMatch uri?;
    StringMatch scheme?;
    StringMatch method?;
    StringMatch authority?;
    map<StringMatch> headers?;
    int port?;
    map<string> sourceLabels?;
    string[] gateways?;
    !...;
};

# Configuration for a port selector.
#
# + number - The number of the port.
public type PortSelectorConfig record {
    int number;
    !...;
};

# Configuration to a network addressable service.
#
# + host - Host of a service.
# + subset - Subset within the service.
# + port - The port on the host that is being addressed.
public type DestinationConfig record {
    string host;
    string subset?;
    PortSelectorConfig port?;
    !...;
};

# Configuration for weight for destination to traffic route.
#
# + destination - Destination to forward to.
# + weight - Weight for the destination.
public type DestinationWeightConfig record {
    DestinationConfig destination;
    int weight?;
    !...;
};

# Configuration to rewrite or redirect requests.
#
# + uri - Rewrite the path portion of the URI with this value.
# + authority - Rewrite authority header.
public type HTTPRedirectRewriteConfig record {
    string uri?;
    string authority?;
    !...;
};

# Configuration for retrying http requests.
#
# + attempts - Number of retries.
# + perTryTimeout - Timeout per attempt.
public type HTTPRetryConfig record {
    int attempts;
    string perTryTimeout;
    !...;
};

# Configuration for delaying a request.
#
# + percent - Percentage of requests to delay.
# + fixedDelay - Delay for the request.
public type HTTPFaultInjectionDelayConfig record {
    int percent;
    string fixedDelay;
    !...;
};

# Configuration to abort request prematurely.
#
# + percent - Percentage of requests to abort.
# + httpStatus - Http status when aborted.
public type HTTPFaultInjectionAbortConfig record {
    int percent;
    int httpStatus;
    !...;
};

# Configuration to specify faults to inject while forwarding.
#
# + delay - Delay before forwarding.
# + abort - Abort http request.
public type HTTPFaultInjectionConfig record {
    HTTPFaultInjectionDelayConfig delay?;
    HTTPFaultInjectionAbortConfig ^"abort"?;
    !...;
};

# Configuration for CORS policy.
#
# + allowOrigin - The list of origins that are allowed to perform CORS requests.
# + allowMethods - List of HTTP methods allowed to access the resource.
# + allowHeaders - List of HTTP headers that can be used when requesting the resource.
# + exposeHeaders - A white list of HTTP headers that the browsers are allowed to access.
# + maxAge - Specifies how long the the results of a preflight request can be cached.
# + allowCredentials - Indicates whether the caller is allowed to send the actual request (not the preflight) using credentials.
public type CorsPolicyConfig record {
    string[] allowOrigin?;
    string[] allowMethods?;
    string[] allowHeaders?;
    string[] exposeHeaders?;
    string maxAge?;
    boolean allowCredentials?;
    !...;
};

# Configurations for conditions and actions for routing HTTP.
#
# + match - Conditions to match.
# + route - Route destination.
# + redirect - Rule for redirecting.
# + rewrite - Rewrite URI and headers.
# + timeout - Timeout for requests.
# + retries - Retry policy.
# + fault - Fault injection policy.
# + mirror - Mirror traffic to another destination.
# + corsPolicy - CORS policies.
# + appendHeaders - Additional header to add before forwarding/directing.
public type HTTPRouteConfig record {
    HTTPMatchRequestConfig[] ^"match"?;
    DestinationWeightConfig[] route?;
    HTTPRedirectRewriteConfig redirect?;
    HTTPRedirectRewriteConfig rewrite?;
    string ^"timeout"?;
    HTTPRetryConfig ^"retries"?;
    HTTPFaultInjectionConfig fault?;
    DestinationConfig mirror?;
    CorsPolicyConfig corsPolicy?;
    map<string> appendHeaders?;
    !...;
};

# Configuration to match TLS attributes.
#
# + sniHosts - Server name indicator to match.
# + destinationSubnets - IP address of the destination.
# + port - Port on the host that is being addressed.
# + sourceLabels - One or more labels that constrain the applicability of a rule to workloads with the given labels.
# + gateways - Names of gateways the rules should be applied to.
public type TLSMatchAttributesConfig record {
    string[] sniHosts;
    string[] destinationSubnets?;
    int port?;
    map<string> sourceLabels?;
    string[] gateways?;
    !...;
};

# Configuration for conditions and actions for routing TLS/HTTPS traffic.
#
# + match - Match conditions to satisfy.
# + route - Destination to route.
public type TLSRouteConfig record {
    TLSMatchAttributesConfig[] ^"match";
    DestinationWeightConfig[] route;
    !...;
};

# Configuration for L4 connection match attributes.
#
# + destinationSubnets - IP address of destination.
# + port - Port on the host that is being addressed.
# + sourceLabels - One or more labels that constrain the applicability of a rule to workloads with the given labels.
# + gateways - Names of gateways the rules should be applied to.
public type L4MatchAttributesConfig record {
    string[] destinationSubnets?;
    int port?;
    map<string> sourceLabels?;
    string[] gateways?;
    !...;
};

# Configuration for routing TCP traffic.
#
# + match - Match conditions.
# + route - Destination to route to.
public type TCPRouteConfig record {
    L4MatchAttributesConfig[] ^"match";
    DestinationWeightConfig[] route;
    !...;
};

# Virtual service configuration for @kubernetes:IstioVirtualService annotation.
#
# + hosts - Destination which traffic should be sent.
# + gateways - Names of the gateways which the service should listen to.
# + http - Route rules for HTTP traffic.
# + tls - Route rules for TLS and HTTPS traffic.
# + tcp - Route rules for TCP traffic.
public type IstioVirtualServiceConfig record {
    *Metadata;
    string[] hosts?;
    string[] gateways?;
    HTTPRouteConfig[] http?;
    TLSRouteConfig[] tls?;
    TCPRouteConfig[] tcp?;
    !...;
};

# @kubernetes:IstioVirtualService annotation to generate istio virtual service.
public annotation<service, listener> IstioVirtualService IstioVirtualServiceConfig;

public type OpenShiftBuildConfigConfiguration record {
    *Metadata;
    boolean generateImageStream = true;
    !...;
};

public annotation<service, listener, function> OpenShiftBuildConfig OpenShiftBuildConfigConfiguration;

public type OpenShiftRouteConfiguration record {
    *Metadata;
    string host;
    string domain;
    !...;
};

public annotation<service, listener> OpenShiftRoute OpenShiftRouteConfiguration;
