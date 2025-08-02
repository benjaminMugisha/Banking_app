variable "vpc_id" {
  description = "The VPC ID to associate with the route table"
  type        = string
}

variable "zone_name" {
  description = "The domain name of the private hosted zone "
  type        = string
}
variable "env" {}
