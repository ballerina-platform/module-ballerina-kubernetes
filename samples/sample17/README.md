## Sample17: OpenShift Build Configs and Routes

- This sample demonstrates how to build and deploy a ballerina service in OpenShift.
- `@kubernetes:OpenShiftBuildConfig` annotation generates a build config which allows to build docker images of the 
service.
- `@kubernetes:OpenShiftRoute` annotation generates a route to the generated kubernetes service.
- This sample uses minikube and minishift for demonstration purposes. Hence make sure they are started up.
- The `hello_world_oc.bal` file has placeholders that needs to be filled prior building.
- Use the value from `minishift openshift registry` to fill in the `MINISHIFT_DOCKER_REGISTRY_IP` placeholder. This 
refers to the IP and port of the docker registry which OpenShift uses. 
- Use the value from `minishift ip` to fill in the `MINISHIFT_IP` placeholder.
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

	Run the following command to deploy the Kubernetes artifacts:
	kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/

	Run the following command to install the application using Helm:
	helm install --name hello-world-oc-deployment /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/hello-world-oc-deployment

	Run the following command to deploy the OpenShift artifacts:
	oc apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/

	Run the following command to start a build:
	oc start-build bc/helloep-openshift-bc --from-dir=. --follow
	oc apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/
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

5. Deploy the artifacts on the project.
```bash
$> oc apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/
service/helloep-svc created
deployment.extensions/hello-world-oc-deployment created
buildconfig.build.openshift.io/helloep-openshift-bc created
imagestream.image.openshift.io/hello_world_oc created
route.route.openshift.io/helloep-openshift-route created
```

6. See the created OpenShift Build Config. Note that there hasn't been any builds before:
```bash
$> oc get bc/helloep-openshift-bc
NAME                   TYPE      FROM      LATEST
helloep-openshift-bc   Docker    Binary    0
```

7. Submit a new build to build the docker image of the ballerina service.
```bash
$> oc start-build bc/helloep-openshift-bc --from-dir=. --follow
Uploading directory "." as binary input for the build ...

Uploading finished
build.build.openshift.io/helloep-openshift-bc-1 started
Receiving source from STDIN as archive ...
Step 1/7 : FROM ballerina/ballerina-runtime:0.990.3-SNAPSHOT
 ---> 18973986b79b
Step 2/7 : LABEL maintainer "dev@ballerina.io"
 ---> Using cache
 ---> 36a7283acbca
Step 3/7 : COPY hello_world_oc.balx /home/ballerina
 ---> f1045a6c3966
Removing intermediate container 7f08ae47b3b6
Step 4/7 : EXPOSE 9090
 ---> Running in 4031bbc5a40e
 ---> f33e9fffd12a
Removing intermediate container 4031bbc5a40e
Step 5/7 : CMD ballerina run  hello_world_oc.balx
 ---> Running in a7df877d774b
 ---> d7a7d95701f7
Removing intermediate container a7df877d774b
Step 6/7 : ENV "OPENSHIFT_BUILD_NAME" "helloep-openshift-bc-1" "OPENSHIFT_BUILD_NAMESPACE" "bal-oc"
 ---> Running in 5b8c506d8591
 ---> 041b552a0366
Removing intermediate container 5b8c506d8591
Step 7/7 : LABEL "io.openshift.build.name" "helloep-openshift-bc-1" "io.openshift.build.namespace" "bal-oc"
 ---> Running in cc18af342e1f
 ---> 97dc9b2118b0
Removing intermediate container cc18af342e1f
Successfully built 97dc9b2118b0
Pushing image 172.30.1.1:5000/bal-oc/hello_world_oc:latest ...
Push successful
```

8. Check the build count:
```bash
$> oc get bc/helloep-openshift-bc
NAME                   TYPE      FROM      LATEST
helloep-openshift-bc   Docker    Binary    1
```

9. Check the newly created image by login to the OpenShift docker registry: 
```bash
$> eval $(minishift docker-env)
$> docker images
REPOSITORY                                     TAG                 IMAGE ID            CREATED             SIZE
172.30.1.1:5000/bal-oc/hello_world_oc          latest              a30b89ffc635        24 seconds ago      135MB
```

10. Redeploy the yaml files so that the kubernetes deployment picks up the newly built image: 
```bash
$> oc apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/
service/helloep-svc configured
deployment.extensions/hello-world-oc-deployment configured
buildconfig.build.openshift.io/helloep-openshift-bc configured
imagestream.image.openshift.io/hello_world_oc configured
route.route.openshift.io/helloep-openshift-route configured
```

11. Find the URL exposed by the OpenShift Route to the Ballerina service:
```bash
$> oc get route
NAME                      HOST/PORT                                              PATH      SERVICES      PORT      TERMINATION   WILDCARD
helloep-openshift-route   helloep-openshift-route-bal-oc.192.168.99.101.nip.io             helloep-svc   9090                    None
```

12. Access the ballerina service using curl command by using the `HOST/PORT` value shown in the route.
```bash
$> curl http://helloep-openshift-route-bal-oc.192.168.99.101.nip.io/helloWorld/sayHello
Hello, World from service helloWorld !
```

13. Undeploy the artifacts:
```bash
$> oc delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample17/kubernetes/
service "helloep-svc" deleted
deployment.extensions "hello-world-oc-deployment" deleted
buildconfig.build.openshift.io "helloep-openshift-bc" deleted
imagestream.image.openshift.io "hello_world_oc" deleted
route.route.openshift.io "helloep-openshift-route" deleted
```

14. Remove the docker image. Wait until the deployment is removed completely from the previous command:
```bash
$> docker rmi 172.30.1.1:5000/bal-oc/hello_world_oc:latest
Untagged: 172.30.1.1:5000/bal-oc/hello_world_oc:latest
Untagged: 172.30.1.1:5000/bal-oc/hello_world_oc@sha256:cd403f0a1cf011ef288628ff0c2d26a463487b4d95a7e90d37df83d397fedae9
```

15. Delete the OpenShift project created for the sample.
```bash
oc delete project bal-oc
```
