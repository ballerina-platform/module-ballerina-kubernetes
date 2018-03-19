## Ballerina Kubernetes samples


### Prerequisites
 1. Install a recent version of Docker for Mac and [enable Kubernetes](https://docs.docker.com/docker-for-mac/#kubernetes)
 2. Nginx backend controllers deployed.

#### Setting up nginx-ingress

1. Run the following command to deploy nginx backend.

```
kubectl apply -f nginx-ingress/namespaces/nginx-ingress.yaml -Rf nginx-ingress
```

## Try kubernetes annotation samples:

1. [Sample1: Kubernetes Hello World](sample1/)
1. [Sample2: Kubernetes Hello World with enableLiveness and hostname mapping](sample2/)
1. [Sample3: Ballerina program with multiple services with different ports](sample3/)
1. [Sample4: Ballerina program with multiple services running in multiple ports](sample4/)
1. [Sample5: Kubernetes Hello World in Google Cloud Environment](sample5/)
1. [Sample6: Kubernetes Hello World Secured](sample6/)
1. [Sample7: Mount secret volumes to deployment](sample7)
