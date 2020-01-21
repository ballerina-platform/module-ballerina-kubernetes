## Sample11: Kubernetes Hello World with Ballerina function

- This sample runs ballerina main function in kubernetes as a job. 
- The function is annotated with @kubernetes:Job{}. 
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_job:latest
    
    $> tree
    ├── README.md
    ├── hello_world_job.bal
    ├── hello_world_job.jar
    ├── docker
        └── Dockerfile
    └── kubernetes
        └── hello_world_job.yaml
    ```
### How to run:

1. Compile the  hello_world_job.bal file. Command to deploy kubernetes artifacts will be printed on build success.
```bash
$> ballerina build hello_world_job.bal
Compiling source
        hello_world_job.bal

Generating executables
        hello_world_job.jar

Generating artifacts...

        @kubernetes:Job                          - complete 1/1
        @kubernetes:Docker                       - complete 2/2 

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample11/kubernetes

        Run the following command to install the application using Helm: 
        helm install --name hello-world-job-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample11/kubernetes/hello-world-job-deployment
```

2. hello_world_job.jar, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── hello_world_job.bal
├── hello_world_job.jar
├── docker
    └── Dockerfile
└── kubernetes
    └── hello_world_job.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY             TAG                 IMAGE ID            CREATED             SIZE
hello_world_job       latest              df83ae43f69b        2 minutes ago        103MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample11/kubernetes
job.batch/hello-world-job-job created
```

5. Verify kubernetes pod is created.
```bash
$> kubectl get pods --show-all
NAME                        READY     STATUS      RESTARTS   AGE
hello-world-job-job-r4xvk   0/1       Completed   0          31s

```

6. Access the logs of completed logs.

Note that the pod name is derived from the above output.
```bash
$> kubectl logs hello-world-job-job-r4xvk
hello world
```


7. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample11/kubernetes/
job "hello-world-job-job" deleted
$> docker rmi hello_world_job

```
