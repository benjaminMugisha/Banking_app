#terraform {
#  backend "s3" {
#    bucket         = "s3statebackendbenjamin12233"
#    dynamodb_table = "state-lock"
#    key            = "global/s3/terraform.tfstate"
#    region         = "eu-west-1"
#    encrypt        = true
#  }
#}
