variable "region" {
  description         = "AWS region"
  type                = string
  default             = "eu-west-1"
}
variable "vpc_name" {
  default             = "prod-vpc"
}
variable "subnet_config" {
  description         = "List of subnet configurations (both public and private)"
  type                = list(object({
    name              = string
    cidr_block        = string
    availability_zone = string
    type              = string
  }))
}
variable "db_username" {
  description         = "Database username"
  type                = string
  sensitive           = true
}
variable "db_password" {
  description          = "Database password"
  type                 = string
  sensitive            = true
}
variable "db_name" {
  description          = "Database name"
  type                 = string
}
variable "cluster_name" {
  default              = "prod-cluster"
}
variable "bucket_name" {
  default              = "s3statebackendbenjamin1222333"
}
variable "env" {
  default              = "prod"
}
variable "table" {
  default              = "state_lock"
}
variable "node_role_name" {
  default              = "eks-node-role"
}
variable "cluster_role_name" {
  default              = "eks-cluster-role"
}
variable "instance_type" {
  default              = ["t3.micro"]
}
variable "tags" {
  description          = "Environment-level tags"
  type                 = map(string)
  default              = {
    Owner              = "Benjamin"
    Environment        = "prod"
    Project            = "Banking_app"
  }
}
