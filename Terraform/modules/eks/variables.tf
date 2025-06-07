variable "cluster_name" {
  default = "my-eks-cluster"
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "vpc_id" {}
