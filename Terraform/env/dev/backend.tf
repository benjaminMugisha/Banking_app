#############################################################
## AFTER RUNNING TERRAFORM APPLY (WITH LOCAL BACKEND),
## UNCOMMENT THIS CODE THEN RERUN TERRAFORM INIT
## TO SWITCH FROM LOCAL BACKEND TO REMOTE AWS BACKEND
#############################################################

terraform {
  backend "s3" {
    bucket         = "s3statebackendbenjamin123dev"
    dynamodb_table = "dev_state_lock"
    key            = "global/s3/terraform.tfstate"
    region         = "eu-west-1"
    encrypt        = true
  }
}
