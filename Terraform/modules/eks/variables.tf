variable "cluster_name" {}
variable "private_subnet_ids" {
  type = list(string)
}
variable "vpc_id" {}
variable "min_size" {}
variable "max_size" {}
variable "desired_size" {}