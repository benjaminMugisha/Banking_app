variable "identifier" {}
variable "db_name" {}
variable "username" {}
variable "password" {}
variable "private_subnet_ids" {}
variable "vpc_id" {}
variable "env" {}
variable "eks_node_sg_id" {}

variable "instance_class" {
  description = "instance type for RDS"
  type        = string
}

variable "allocated_storage" {
  type        = number
  default     = 5
}

variable "storage_type" {
  description = "storage type of our rds"
  default     = "gp2"
}

variable "multi_az" {
  type        = bool
  default     = false
}
