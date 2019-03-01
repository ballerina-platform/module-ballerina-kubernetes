## Sample17: OpenShift Build Configs and Routes

- This sample demonstrates how to build and deploy a ballerina service in OpenShift.
- `@kubernetes:OpenShiftBuildConfig` annotation generates a build config which allows to build docker images of the 
service.
- `@kubernetes:OpenShiftRoute` annotation generates a route to the generated kubernetes service.
- This sample uses minikube and minishift for demonstration purposes. Hence make sure they are started up.
- The `hello_world_oc.bal` file has placeholders that needs to be filled prior building.
- Use the value from `minishift ip` to fill in the `MINISHIFT_IP` placeholder. Make sure you dont remove the `nip.io` 
from the value.
- Use the value from `minishift openshift registry` to fill in the `MINISHIFT_DOCKER_REGISTRY_IP` placeholder. This 
refers to the IP and port of the docker registry which OpenShift uses. 
- Namespace value of `@kubernetes:OpenShiftBuildConfig` and `@kubernetes:OpenShiftRoute` refers to the OpenShift 
project.
- Remove the nginx artifacts added in setting up of the tutorial. Run `kubectl delete -f 
nginx-ingress/namespaces/nginx-ingress.yaml -Rf nginx-ingress` from the `samples` folder.
- Following files will be generated from building this sample.
    ```bash 
    $> tree
    ├── README.md
    ├── hello_world_oc.bal
    ├── hello_world_oc.balx
    └── kubernetes
        ├── docker
        │   ├── Dockerfile
        │   └── hello_world_oc.balx
        ├── hello-world-oc-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       └── hello_world_oc.yaml
        └── hello_world_oc.yaml
  
    ```
### How to run:

1. Compile the ballerina project from `sample17` folder. Command to deploy the artifacts will be printed on success:
```bash
$> ballerina build hello_world_oc.bal
Compiling source
    hello_world_oc.bal
Generating executable
    hello_world_oc.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3
	@kubernetes:Helm 			 - complete 1/1
	@kubernetes:OpenShiftBuildConfig 	 - complete 1/1
	@kubernetes:OpenShiftImageStream 	 - complete 1/1
	@kubernetes:OpenShiftRoute 		 - complete 1/1

	Run the following command to deploy the OpenShift artifacts:
	oc apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/openshift

	Run the following command to start a build:
	oc start-build bc/hello-world-oc-openshift-bc --from-dir=. --follow

	Run the following command to deploy the Kubernetes artifacts:
	kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes
```

2. kubernetes and OpenShift artifacts are generated in the hello_world_oc.yaml: 
```bash
$> tree kubernetes/
kubernetes/
├── docker
│   ├── Dockerfile
│   └── hello_world_oc.balx
├── hello-world-oc-deployment
│   ├── Chart.yaml
│   └── templates
│       └── hello_world_oc.yaml
├── hello_world_oc.yaml
└── openshift
    └── hello_world_oc.yaml

```
3. Login to OpenShift.
```bash
$> oc login -u system:admin
```

4. Create a new OpenShift project for the sample with name `bal-oc`:
```bash
$> oc new-project bal-oc
Now using project "bal-oc" on server "https://192.168.99.101:8443".
```

5. Deploy the OpenShift artifacts on the project. Use the command from the build output.
```bash
$> oc apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/openshift
buildconfig.build.openshift.io/hello-world-oc-openshift-bc created
imagestream.image.openshift.io/hello_world_oc created
route.route.openshift.io/helloep-openshift-route created
```

6. See the created OpenShift Build Config. Note that there hasn't been any builds before:
```bash
$> oc get bc/hello-world-oc-openshift-bc
NAME                          TYPE      FROM      LATEST
hello-world-oc-openshift-bc   Docker    Binary    0
```

7. Submit a new build to build the docker image of the ballerina service. Use the command from the build output.
```bash
$> oc start-build bc/hello-world-oc-openshift-bc --from-dir=. --follow
Uploading directory "." as binary input for the build ...

Uploading finished
build.build.openshift.io/hello-world-oc-openshift-bc-1 started
Receiving source from STDIN as archive ...
Step 1/7 : FROM ballerina/ballerina-runtime:0.990.4-SNAPSHOT
 ---> 4e3a090f6d13
Step 2/7 : LABEL maintainer "dev@ballerina.io"
 ---> Using cache
 ---> e001c11f4184
Step 3/7 : COPY hello_world_oc.balx /home/ballerina
 ---> 5711d88cab70
Removing intermediate container 1c049963ae5f
Step 4/7 : EXPOSE 9090
 ---> Running in e552c74b34f9
 ---> c1bb55ed54e5
Removing intermediate container e552c74b34f9
Step 5/7 : CMD ballerina run  hello_world_oc.balx
 ---> Running in 4c1f594ab5cd
 ---> baac6e1e5ba4
Removing intermediate container 4c1f594ab5cd
Step 6/7 : ENV "OPENSHIFT_BUILD_NAME" "hello-world-oc-openshift-bc-1" "OPENSHIFT_BUILD_NAMESPACE" "bal-oc"
 ---> Running in 564f014ce4ce
 ---> 6b5358161947
Removing intermediate container 564f014ce4ce
Step 7/7 : LABEL "io.openshift.build.name" "hello-world-oc-openshift-bc-1" "io.openshift.build.namespace" "bal-oc"
 ---> Running in 2457d4bebb50
 ---> 15411f9ab2a2
Removing intermediate container 2457d4bebb50
Successfully built 15411f9ab2a2
Pushing image 172.30.1.1:5000/bal-oc/hello_world_oc:latest ...
Push successful
```

8. Check the build count:
```bash
$> oc get bc/hello-world-oc-openshift-bc
NAME                          TYPE      FROM      LATEST
hello-world-oc-openshift-bc   Docker    Binary    1
```

9. Check the newly created image by login to the OpenShift docker registry: 
```bash
$> eval $(minishift docker-env)
$> docker images
REPOSITORY                                     TAG                 IMAGE ID            CREATED             SIZE
172.30.1.1:5000/bal-oc/hello_world_oc          latest              a30b89ffc635        24 seconds ago      135MB
```

10. Deploy the Kubernetes artifacts on the project. Use the command from the build output. 
```bash
$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes
service/helloep-svc created
deployment.apps/hello-world-oc-deployment created
```

11. Find the URL exposed by the OpenShift Route to the Ballerina service:
```bash
$> oc get route
NAME                      HOST/PORT                                       PATH      SERVICES      PORT      TERMINATION   WILDCARD
helloep-openshift-route   helloep-openshift-route-bal-oc.192.168.99.101             helloep-svc   9090                    None
```

12. Access the ballerina service using curl command by using the `HOST/PORT` value shown in the route.
```bash
$> curl http://helloep-openshift-route-bal-oc.192.168.99.101.nip.io/helloWorld/sayHello
Hello, World from service helloWorld !
```

13. Undeploy the Kubernetes artifacts:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes
service "helloep-svc" deleted
deployment.apps "hello-world-oc-deployment" deleted
```

14. Undeploy the OpenShift artifacts:
```bash
$> oc delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/openshift
buildconfig.build.openshift.io "hello-world-oc-openshift-bc" deleted
imagestream.image.openshift.io "hello_world_oc" deleted
route.route.openshift.io "helloep-openshift-route" deleted
```

16. Remove the docker image. Wait until the deployment is removed completely from the previous command:
```bash
$> docker rmi 172.30.1.1:5000/bal-oc/hello_world_oc:latest
Untagged: 172.30.1.1:5000/bal-oc/hello_world_oc:latest
Untagged: 172.30.1.1:5000/bal-oc/hello_world_oc@sha256:70e285e02345852d1dd12c0f6b03f744c138d7c247694ff6f72d381cd02a5856
Deleted: sha256:15411f9ab2a2a89a946d6b2e99b11e0264f0db9a30e21de14a77113fa155dfc3
Deleted: sha256:6b535816194776f84a42ff51ccdd57462aec56fefea3121c0d0a1c3343497edc
Deleted: sha256:baac6e1e5ba44d7494b1f589b174e0a976fcdd57109996dcbd2680d8576a45fd
Deleted: sha256:c1bb55ed54e55dce5f82148b204c7be396f5d4398c118cc88e903e06e1e9f28d
Deleted: sha256:5711d88cab7099db8109dc38f42fb8f4cc2b9d810d4af9f03e0b9d85a76e54fe
Deleted: sha256:33b10aaf211b5b5a97d0912359d3448d6c065c7d1fe92a28a75483a20e5081f8
```

17. Delete the OpenShift project created for the sample.
```bash
oc delete project bal-oc
```
