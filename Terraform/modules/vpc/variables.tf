variable "cidr_block" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}
variable "vpc_name" {
  description = "Name tag for the VPC"
  type        = string
  default     = "benvpc"
}
variable "aws_region" {
  default     = "eu-west-1"
  description = "AWS region"
  type        = string
}
variable "enable_dns_support" {
  type    = bool
  default = true
}
variable "enable_dns_hostnames" {
  type    = bool
  default = true
}
variable "tags" {
  type = map(string)
}