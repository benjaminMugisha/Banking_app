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
variable "db_username" {
  type        = string
  default     = "postgres"
  description = "Database username"
}
variable "db_name" {
  default = "banking"
}
variable "db_password" {
  type = string
  default = "benjamindb12345"
  description = "Database password"
}
variable "cluster_name" {
  default = "my-eks-cluster"
}
variable "dynamo_db_name" {
  default = "state-lock"
}
variable "bucket_name" {
  default = "s3statebackendbenjamin12233"
}
