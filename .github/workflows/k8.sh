#!/bin/bash

set -e

echo "✅ k8.sh: "
if [[ -z "$ENV" ]]; then
  echo "❌ ENV variable not set. pass it into the script."
  exit 1
fi

cd Terraform/env/"$ENV"
CLUSTER_NAME=$(terraform output -raw cluster_name)

echo " Updating kubeconfig for cluster: $CLUSTER_NAME"
aws eks update-kubeconfig --region eu-west-1 --name "$CLUSTER_NAME"

echo " Extracting Terraform outputs..."
RDS_ENDPOINT=$(terraform output -raw rds_endpoint | cut -d ':' -f1)
DB_NAME=$(terraform output -raw db_name)

echo " Injecting variables into config file..."
export RDS_ENDPOINT
export DB_NAME
envsubst < ../../../Kubernetes/banking-app-config.yaml | kubectl apply -f -

echo " Preparing DB secrets..."
if [[ "$ENV" == "prod" ]]; then
  export SPRING_DATASOURCE_USERNAME="${DB_USERNAME}"
  export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD}"
else
  export SPRING_DATASOURCE_USERNAME="${DEV_DB_USERNAME}"
  export SPRING_DATASOURCE_PASSWORD="${DEV_DB_PASSWORD}"
fi

envsubst < ../../../Kubernetes/db-secret.yaml | kubectl apply -f -

echo " Deploying banking app to Kubernetes..."
cd ../../../Kubernetes/
kubectl apply -f banking-app-service.yaml
kubectl apply -f banking-app-deployment.yaml

echo " ✅✅✅✅Full Deployment complete."
