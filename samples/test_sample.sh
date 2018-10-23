#!/bin/bash
# ---------------------------------------------------------------------------
#  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

kubernetes_sample_dir=$(pwd)
#export DOCKER_USERNAME=<user_name>
#export DOCKER_PASSWORD=<password>

for number in {1..12}
do
	echo "======================== Testing sample-$number ========================"
	pushd "$kubernetes_sample_dir"/sample"$number"
	if [[ number -eq 1 ]]; then
		ballerina build hello_world_k8s.bal
		kubectl apply -f ./kubernetes
		port=$(kubectl get svc helloworld-svc -o go-template='{{range.spec.ports}}{{if .nodePort}}{{.nodePort}}{{"\n"}}{{end}}{{end}}')
		sleep 5
		curl http://localhost:$port/HelloWorld/sayHello
		kubectl delete -f ./kubernetes
	fi
	if [[ number -eq 2 ]]; then
		ballerina build hello_world_k8s_config.bal
		kubectl apply -f ./kubernetes
		sleep 5
		curl http://abc.com/HelloWorld/sayHello
		kubectl delete -f ./kubernetes
	fi
	if [[ number -eq 3 ]]; then
		ballerina build foodstore.bal
		kubectl apply -f ./kubernetes
		sleep 5
		curl http://pizza.com/pizzastore/pizza/menu
		curl http://burger.com/menu		
		kubectl delete -f ./kubernetes
	fi

	if [[ number -eq 4 ]]; then
		ballerina build hello_world_ssl_k8s.bal
		kubectl apply -f ./kubernetes
		sleep 5
		curl https://abc.com/helloWorld/sayHello -k	
		kubectl delete -f ./kubernetes
	fi

	if [[ number -eq 5 ]]; then
		ballerina build pizzashack.bal
		kubectl apply -f ./kubernetes
		sleep 5
		curl http://internal.pizzashack.com/customer
		curl https://pizzashack.com/customer -k	
		kubectl delete -f ./kubernetes
	fi

	if [[ number -eq 6 ]]; then
		ballerina build hello_world_gce.bal
		kubectl apply -f ./kubernetes
		sleep 5
		curl http://abc.com/helloWorld/sayHello	
		kubectl delete -f ./kubernetes
	fi

	if [[ number -eq 7 ]]; then
		ballerina build hello_world_secret_mount_k8s.bal
		kubectl apply -f ./kubernetes
		sleep 5
		curl https://abc.com/helloWorld/secret1 -k		
		curl https://abc.com/helloWorld/secret2 -k		
		curl https://abc.com/helloWorld/secret3 -k
		
		kubectl delete -f ./kubernetes
	fi

	if [[ number -eq 8 ]]; then
		ballerina build hello_world_config_map_k8s.bal
		kubectl apply -f ./kubernetes
		sleep 5
		curl https://abc.com/helloWorld/config/john -k
		curl https://abc.com/helloWorld/config/jane -k
		kubectl delete -f ./kubernetes
	fi
	
	if [[ number -eq 9 ]]; then
		ballerina build hello_world_persistence_volume_k8s.bal
		kubectl apply -f ./kubernetes
		sleep 10
		curl https://abc.com/helloWorld/sayHello -k		
		kubectl delete -f ./kubernetes
	fi

	if [[ number -eq 10 ]]; then
	    ballerina init
		ballerina build
		kubectl apply -f ./target/kubernetes/burger
		kubectl apply -f ./target/kubernetes/pizza
		sleep 10
		curl http://pizza.com/pizzastore/pizza/menu
		curl https://burger.com/menu -k
		kubectl delete -Rf ./target/kubernetes
	fi

	if [[ number -eq 12 ]]; then
		ballerina build hello_world_copy_file.bal
		kubectl apply -f ./kubernetes
		sleep 5
		curl https://abc.com/helloWorld/data -k
		kubectl delete -f ./kubernetes
	fi

	if [[ number -eq 15 ]]; then
		ballerina build hello_world_k8s.bal
		kubectl apply -f ./kubernetes
		sleep 5
		kubectl delete -f ./kubernetes
	fi
	echo "======================== End of sample-$number ========================"
	popd
done
exit 0