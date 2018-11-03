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
    ├── hello_world_job.balx
    └── kubernetes
        ├── docker
        │   └── Dockerfile
        └── hello_world_job_job.yaml
    ```
### How to run:

1. Compile the  hello_world_job.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_job.bal
@kubernetes:Job 			 - complete 1/1
@kubernetes:Docker 			 - complete 3/3

Run following command to deploy kubernetes artifacts: 
kubectl apply -f /Users/lakmal/ballerina/kubernetes/samples/sample11/kubernetes/
```

2. hello_world_job.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── hello_world_job.bal
├── hello_world_job.balx
└── kubernetes
    ├── docker
    │   └── Dockerfile
    └── hello_world_job_job.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY             TAG                 IMAGE ID            CREATED             SIZE
hello_world_job       latest              df83ae43f69b        2 minutes ago        103MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/lakmal/ballerina/kubernetes/samples/sample11/kubernetes/
job.batch "hello-world-job-job" created
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
$> kubectl delete -f kubernetes/
job "hello-world-job-job" deleted
```
