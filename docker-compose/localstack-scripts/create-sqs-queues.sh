#!/bin/bash

localstack_url=http://localhost:4566
declare -a queues

# ORDER QUEUES
queues+=(order-received-events)
queues+=(order-create-events)
queues+=(order-failed-events)
queues+=(order-notification-events)
queues+=(order-take-products-result-events)

# PRODUCT QUEUES
queues+=(product-create-events)
queues+=(product-update-events)
queues+=(product-notification-events)
queues+=(product-take-events)

# Profile
aws configure set aws_access_key_id localstack --profile=localstack
aws configure set aws_secret_access_key localstack --profile=localstack
aws configure set region sa-east-1 --profile=localstack
export PROFILE=localstack

# Queue Creation
for queue in "${queues[@]}"
do
        aws sqs --endpoint-url=$localstack_url create-queue --queue-name "$queue" --profile=localstack
done



