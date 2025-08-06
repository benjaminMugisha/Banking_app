#!/bin/bash

set -e

ENV=$1

echo "🟡 Env is: $ENV"

if [ "$ENV" = "prod" ]; then
  S3_BUCKET="${S3_BUCKET}"
  DB_USERNAME="${DB_USERNAME}"
  DB_PASSWORD="${DB_PASSWORD}"
  DB_NAME="${DB_NAME}"
else
  S3_BUCKET="${DEV_S3_BUCKET}"
  DB_USERNAME="${DEV_DB_USERNAME}"
  DB_PASSWORD="${DEV_DB_PASSWORD}"
  DB_NAME="${DEV_DB_NAME}"
fi

cd Terraform/env/$ENV

if ! aws s3 ls s3://$S3_BUCKET 2>/dev/null; then
  echo "❌❌❌ S3 bucket doesn't exist. Creating with local backend first..."

  [ -f backend.tf ] && mv backend.tf backend.tf.bak

  terraform init
  terraform apply -auto-approve \
    -var="db_username=$DB_USERNAME" \
    -var="db_password=$DB_PASSWORD" \
    -var="db_name=$DB_NAME"

  [ -f backend.tf.bak ] && mv backend.tf.bak backend.tf

  echo "✅ Migrating to remote backend..."
  terraform init -migrate-state -force-copy

else
  echo "✅✅✅ S3 bucket exists. Using remote backend..."
  terraform init
  terraform apply -auto-approve
fi
