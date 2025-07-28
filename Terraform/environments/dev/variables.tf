variable "region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}
variable "subnet_config" {
  description = "List of subnet configurations (both public and private)"
  type = list(object({
    name              = string
    cidr_block        = string
    availability_zone = string
    type              = string
  }))
}
variable "db_username" {}
variable "db_name" {
  default = "dev_db"
}
variable "db_password" {}
variable "cluster_name" {
  default = "dev-cluster"
}
variable "dynamo_db_name" {
  default = "state-lock"
}
variable "bucket_name" {
  default = "s3statebackendbenjamindev"
}
