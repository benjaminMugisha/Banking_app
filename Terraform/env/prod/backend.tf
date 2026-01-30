#############################################################
### AFTER RUNNING TERRAFORM APPLY (WITH LOCAL BACKEND),
### UNCOMMENT THIS CODE THEN RERUN "terraform init"
### TO SWITCH FROM LOCAL BACKEND TO REMOTE AWS BACKEND
##############################################################

terraform {
  backend "s3" {
    bucket         = "s3benstatefvpsvrf"
    dynamodb_table = "state_lock"
    key            = "global/s3/terraform.tfstate"
    region         = "eu-west-1"
    encrypt        = true
  }
}
