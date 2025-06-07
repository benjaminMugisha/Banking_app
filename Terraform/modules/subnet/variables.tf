variable "vpc_id" {
  description = "VPC ID to associate subnets with"
  type        = string
}

variable "subnet_config" {
  type = list(object({
    name              = string
    cidr_block        = string
    availability_zone = string
    type              = string
  }))
}
