variable "vpc_id" {
  description = "The VPC ID to associate with the route table"
  type        = string
}

variable "name" {
  description = "Name of the route table"
  type        = string
}

variable "gateway_id" {
  description = "ID of the internet gateway to associate with the route table"
  type        = string
}

variable "public_subnet_ids" {}
