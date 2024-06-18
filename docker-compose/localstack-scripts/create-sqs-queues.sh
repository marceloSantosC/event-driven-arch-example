#!/bin/bash

localstack_url=http://localhost:4566

aws configure set aws_access_key_id localstack --profile=localstack
aws configure set aws_secret_access_key localstack --profile=localstack
aws configure set region sa-east-1 --profile=localstack

export PROFILE=localstack

# ORDER
aws sqs --endpoint-url=$localstack_url create-queue --queue-name order-received-events --profile=localstack
aws sqs --endpoint-url=$localstack_url create-queue --queue-name order-create-events --profile=localstack
aws sqs --endpoint-url=$localstack_url create-queue --queue-name order-failed-events --profile=localstack
aws sqs --endpoint-url=$localstack_url create-queue --queue-name order-notification-events --profile=localstack


# PRODUCT
aws sqs --endpoint-url=$localstack_url create-queue --queue-name product-create-events --profile=localstack
aws sqs --endpoint-url=$localstack_url create-queue --queue-name product-update-events --profile=localstack
aws sqs --endpoint-url=$localstack_url create-queue --queue-name product-notification-events --profile=localstack

# APP
aws sqs --endpoint-url=$localstack_url create-queue --queue-name app-notification-events --profile=localstack

echo ########################LISTING QUEUES########################
aws sqs list-queues --profile=localstack