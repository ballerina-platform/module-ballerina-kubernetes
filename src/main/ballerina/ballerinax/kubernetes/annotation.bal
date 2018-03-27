package ballerinax.kubernetes;

@Description {value:"Kubernetes deployment configuration"}
@Field {value:"name: Name of the deployment"}
@Field {value:"labels: Labels for deployment"}
@Field {value:"replicas: Number of replicas"}
@Field {value:"enableLiveness: Enable or disable enableLiveness probe"}
@Field {value:"livenessPort: Port to check the enableLiveness"}
@Field {value:"initialDelaySeconds: Initial delay in seconds before performing the first probe"}
@Field {value:"periodSeconds: Liveness probe interval"}
@Field {value:"imagePullPolicy: Docker image pull policy"}
@Field {value:"namespace: Kubernetes namespace"}
@Field {value:"image: Docker image with tag"}
@Field {value:"envVars: Environment varialbes for container"}
@Field {value:"buildImage: Docker image to be build or not"}
@Field {value:"dockerHost: Docker host IP and docker PORT. (e.g minikube IP and docker PORT)"}
@Field {value:"username: Username for docker registry"}
@Field {value:"password: Password for docker registry"}
@Field {value:"baseImage: Base image for docker image building"}
@Field {value:"push: Push to remote registry"}
@Field {value:"dockerCertPath: Docker cert path."}
public struct DeploymentConfiguration {
    string name;
    string labels;
    int replicas;
    string enableLiveness;
    int livenessPort;
    int initialDelaySeconds;
    int periodSeconds;
    string imagePullPolicy;
    string namespace;
    string image;
    string env;
    boolean buildImage;
    string dockerHost;
    string username;
    string password;
    string baseImage;
    boolean push;
    string dockerCertPath;
}

@Description {value:"Deployment annotation for Kubernetes"}
public annotation <service,endpoint> Deployment DeploymentConfiguration;

@Description {value:"Kubernetes service configuration"}
@Field {value:"labels: Labels for service"}
@Field {value:"serviceType: Service type of the service"}
public struct ServiceConfiguration {
    string name;
    string labels;
    string serviceType;
}

@Description {value:"Service annotation for Kubernetes"}
public annotation <endpoint> Service ServiceConfiguration;

@Description {value:"Kubernetes ingress configuration"}
@Field {value:"name: Name of the ingress"}
@Field {value:"labels: Labels for ingress"}
@Field {value:"hostname: Host name of the ingress"}
@Field {value:"path: Resource path"}
@Field {value:"targetPath: Target path for url rewrite"}
@Field {value:"ingressClass: Ingress class"}
@Field {value:"enableTLS: Enable ingress TLS"}
public struct IngressConfiguration {
    string name;
    string labels;
    string hostname;
    string path;
    string targetPath;
    string ingressClass;
    boolean enableTLS;
}
@Description {value:"Ingress annotation for Kubernetes"}
public annotation <service> Ingress IngressConfiguration;

@Description {value:"Kubernetes Horizontal Pod Autoscaler configuration"}
@Field {value:"name: Name of the Autoscaler"}
@Field {value:"labels: Labels for Autoscaler"}
@Field {value:"minReplicas: Minimum number of replicas"}
@Field {value:"maxReplicas: Maximum number of replicas"}
@Field {value:"cpuPercentage: CPU percentage to start scaling"}
public struct PodAutoscalerConfig {
    string name;
    string labels;
    int minReplicas;
    int maxReplicas;
    int cpuPercentage;
}

@Description {value:"Pod Autoscaler annotation for Kubernetes"}
public annotation <service> HPA PodAutoscalerConfig;


@Description {value:"Kubernetes secret volume mount"}
@Field {value:"name: Name of the volume mount"}
@Field {value:"mountPath: Mount path"}
@Field {value:"readOnly: Is mount read only"}
@Field {value:"data: Paths to data files"}
public struct Secret {
    string name;
    string mountPath;
    boolean readOnly;
    string[] data;
}

public struct  SecretMount{
    Secret[] secrets;
}

@Description {value:"Secret volumes annotation for Kubernetes"}
public annotation <service> Secret SecretMount;

@Description {value:"Kubernetes Config Map volume mount"}
@Field {value:"name: Name of the volume mount"}
@Field {value:"mountPath: Mount path"}
@Field {value:"readOnly: Is mount read only"}
@Field {value:"isBallerinaConf: Is file a ballerina config"}
@Field {value:"data: Paths to data files"}
public struct ConfigMap {
    string name;
    string mountPath;
    boolean readOnly;
    boolean isBallerinaConf;
    string[] data;
}
public struct  ConfigMapMount{
    ConfigMap[] configMaps;
}

@Description {value:"ConfigMap volumes annotation for Kubernetes"}
public annotation <service> ConfigMap ConfigMapMount;

@Description {value:"Kubernetes Persistent Volume Claim"}
@Field {value:"name: Name of the volume claim"}
@Field {value:"mountPath: Mount Path"}
@Field {value:"accessMode: Access mode"}
@Field {value:"volumeClaimSize: Size of the volume claim"}
@Field {value:"readOnly: Is mount read only"}
public struct PersistentVolumeClaimConfig {
    string name;
    string mountPath;
    string accessMode;
    string volumeClaimSize;
    boolean readOnly;
}
public struct  PersistentVolumeClaims{
    PersistentVolumeClaimConfig[] volumeClaims;
}

@Description {value:"ConfigMap volumes annotation for Kubernetes"}
public annotation <service> PersistentVolumeClaim PersistentVolumeClaims;
