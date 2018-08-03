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

documentation {External file type for docker
    F{{source}} - source path of the file (in your machine)
    F{{target}} - target path (inside container)
}
public type FileConfig record {
    string source;
    string target;
};

documentation {Kubernetes deployment configuration
    F{{name}} - Name of the deployment
    F{{namespce}} - Kubernetes namespace
    F{{labels}} - Map of labels for deployment
    F{{replicas}} - Number of replicas
    F{{enableLiveness}} - Enable/Disable liveness probe
    F{{livenessPort}} - Port to check the liveness
    F{{initialDelaySeconds}} - Initial delay in seconds before performing the first probe
    F{{periodSeconds}} - Liveness probe interval
    F{{imagePullPolicy}} - Kubernetes image pull policy
    F{{image}} - Docker image with tag
    F{{env}} - Environment varialbe map for containers
    F{{buildImage}} - Docker image to be build or not
    F{{dockerHost}} - Docker host IP and docker PORT. (e.g minikube IP and docker PORT)
    F{{username}} - Username for docker registry
    F{{password}} - Password for docker registry
    F{{baseImage}} - Base image for docker image building
    F{{push}} - Push to remote registry
    F{{dockerCertPath}} - Docker certificate path
    F{{copyFiles}} - Array of [External files](kubernetes#FileConfig) for docker image
    F{{singleYAML}} - Generate a single yaml file with all kubernetes artifacts (services,deployment,ingress,)
    F{{dependsOn}} - Services this deployment depends on
    F{{imagePullSecret}} - Image pull secrets
}
public type DeploymentConfiguration record {
    string name;
    string namespace;
    map labels;
    int replicas;
    boolean enableLiveness;
    int livenessPort;
    int initialDelaySeconds;
    int periodSeconds;
    string imagePullPolicy;
    string image;
    map env;
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

documentation {@kubernetes:Deployment annotation to configure deplyoment yaml
}
public annotation<service, endpoint> Deployment DeploymentConfiguration;


documentation {Kubernetes service configuration
    F{{name}} - Name of the service
    F{{labels}} - Map of labels for deployment
    F{{serviceType}} - Service type of the service
}
public type ServiceConfiguration record {
    string name;
    map labels;
    string serviceType;
};

documentation {@kubernetes:Service annotation to configure service yaml
}
public annotation<endpoint, service> Service ServiceConfiguration;

documentation{Kubernetes ingress configuration
    F{{name}} - Name of the ingress
    F{{endpointName}} - Name of the endpoint ingress attached
    F{{labels}} - Label map for ingress
    F{{annotations}} - Map of additional annotations
    F{{hostname}} - Host name of the ingress
    F{{path}} - Resource path
    F{{targetPath}} - Target path for url rewrite
    F{{ingressClass}} - Ingress class
    F{{enableTLS}} - Enable/Disable ingress TLS
}
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

documentation {@kubernetes:Ingress annotation to configure ingress yaml
}
public annotation<service, endpoint> Ingress IngressConfiguration;

documentation {Kubernetes Horizontal Pod Autoscaler configuration
    F{{name}} - Name of the Autoscaler
    F{{labels}} - Labels for Autoscaler
    F{{minReplicas}} - Minimum number of replicas
    F{{maxReplicas}} - Maximum number of replicas
    F{{cpuPercentage}} - CPU percentage to start scaling
}
public type PodAutoscalerConfig record {
    string name;
    map labels;
    int minReplicas;
    int maxReplicas;
    int cpuPercentage;
};

documentation {@kubernetes:HPA annotation to configure horizontal pod autoscaler yaml
}
public annotation<service> HPA PodAutoscalerConfig;


documentation {Kubernetes secret volume mount
    F{{name}} - Name of the volume mount
    F{{mountPath}} - Mount path
    F{{readOnly}} - Is mount read only
    F{{data}} - Paths to data files as an array
}
public type Secret record {
    string name;
    string mountPath;
    boolean readOnly;
    string[] data;
};

documentation {Secret volume mount configurations for kubernetes
    F{{secrets}} - Array of [Secret](kubernetes.html#Secret)
}
public type SecretMount record {
    Secret[] secrets;
};

documentation {@kubernetes:Secret annotation to configure secrets
}
public annotation<service> Secret SecretMount;

documentation {Kubernetes Config Map volume mount
    F{{name}} - Name of the volume mount
    F{{mountPath}} - Mount path
    F{{readOnly}} - Is mount read only
    F{{data}} - Paths to data files
}
public type ConfigMap record {
    string name;
    string mountPath;
    boolean readOnly;
    string[] data;
};

documentation {Secret volume mount configurations for kubernetes
    F{{ballerinaConf}} - path to ballerina configuration file
    F{{configMaps}} - Array of [ConfigMap](kubernetes.html#ConfigMap)
}
public type ConfigMapMount record {
    string ballerinaConf;
    ConfigMap[] configMaps;
};

documentation {@kubernetes:ConfigMap annotation to configure config maps
}
public annotation<service> ConfigMap ConfigMapMount;

documentation {Kubernetes Persistent Volume Claim
    F{{name}} - Name of the volume claim
    F{{mountPath}} - Mount Path
    F{{accessMode}} - Access mode
    F{{volumeClaimSize}} - Size of the volume claim
    F{{annotations}} - Map of annotation values
    F{{readOnly}} - Is mount read only
}
public type PersistentVolumeClaimConfig record {
    string name;
    string mountPath;
    string accessMode;
    string volumeClaimSize;
    map annotations;
    boolean readOnly;
};

documentation {Persistent Volume Claims configurations for kubernetes
    F{{volumeClaims}} - Array of [PersistentVolumeClaimConfig](kubernetes.html#PersistentVolumeClaimConfig)
}
public type PersistentVolumeClaims record {
    PersistentVolumeClaimConfig[] volumeClaims;
};

documentation {@kubernetes:PersistentVolumeClaim annotation to configure Persistent Volume Claims
}
public annotation<service> PersistentVolumeClaim PersistentVolumeClaims;

documentation {value:"Kubernetes job configuration
    F{{name}} - Name of the job
    F{{labels}} - Labels for job
    F{{restartPolicy}} - Restart policy
    F{{backoffLimit}} - Backoff limit
    F{{activeDeadlineSeconds}} - Active deadline seconds
    F{{schedule}} - Schedule for cron jobs
    F{{image}} - Docker image with tag
    F{{env}} - Environment varialbes for container
    F{{buildImage}} - Docker image to be build or not
    F{{dockerHost}} - Docker host IP and docker PORT. (e.g minikube IP and docker PORT)
    F{{username}} - Username for docker registry
    F{{password}} - Password for docker registry
    F{{baseImage}} - Base image for docker image building
    F{{push}} - Push to remote registry
    F{{dockerCertPath}} - Docker cert path
}
public type JobConfig record {
    string name;
    map labels;
    string restartPolicy;
    string backoffLimit;
    string activeDeadlineSeconds;
    string schedule;
    map env;
    string imagePullPolicy;
    string image;
    boolean buildImage;
    string dockerHost;
    string username;
    string password;
    string baseImage;
    boolean push;
    string dockerCertPath;
};

documentation {@kubernetes:Job annotation to configure kubernetes jobs
}
public annotation<function> Job JobConfig;
