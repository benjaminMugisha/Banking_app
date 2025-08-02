variable "region" {
  description         = "AWS region"
  type                = string
  default             = "eu-west-1"
}
variable "vpc_name" {
  default             = "dev-vpc"
}
variable "subnet_config" {
  description         = "List of subnet configurations (both public and private)"
  type = list(object({
    name              = string
    cidr_block        = string
    availability_zone = string
    type              = string
  }))
}
variable "db_username" {
  default             = "Benjamin"
}
variable "db_name" {
  default             = "proddb"
}
variable "db_password" {
  type                = string
  sensitive           = true
  default             = "Lefhdcvdvgf12345"
}
variable "dynamo_db_name" {
  default             = "dev-state-lock"
}
variable "bucket_name" {
  default             = "s3statebackendbenjamin123dev"
}
variable "env" {
  default             = "dev"
}
variable "node_role_name" {
  default             = "eks-node-role"
}
variable "cluster_role_name" {
  default             = "eks-cluster-role"
}
variable "instance_type" {
  default             = ["t3.micro"]
}
variable "zone_name" {
  default = ""
}
