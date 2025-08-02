resource "aws_iam_role" "eks_cluster_role" {
#  name          = "eks-cluster-role"
  name                    = var.cluster_role_name

  assume_role_policy      = jsonencode({
    Version               = "2012-10-17"
    Statement             = [{
      Effect              = "Allow"
      Principal           = {
          Service         = "eks.amazonaws.com"
      }
      Action              = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "eks_cluster_AmazonEKSClusterPolicy" {
  role                    = aws_iam_role.eks_cluster_role.name
  policy_arn              = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

resource "aws_iam_role" "eks_node_role" {
  name                    = var.role_name

  assume_role_policy = jsonencode({
    Version               = "2012-10-17"
    Statement             = [{
      Effect              = "Allow"
      Principal           = {
        Service           = "ec2.amazonaws.com"
      }
      Action              = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "worker_node_policies" {
  for_each                = toset([
      "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy",
      "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly",
      "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  ])

  role                    = aws_iam_role.eks_node_role.name
  policy_arn              = each.value
}

resource "aws_eks_cluster" "this" {
  name                    = var.cluster_name
  version                 = "1.28"
  role_arn                = aws_iam_role.eks_cluster_role.arn
  tags                    = {
    Name                  = var.cluster_name
    Environment           = var.env
  }

  vpc_config {
    subnet_ids             = var.private_subnet_ids
    endpoint_public_access = true
    endpoint_private_access= true
    public_access_cidrs    = ["0.0.0.0/0"]
  }
  depends_on = [aws_iam_role_policy_attachment.eks_cluster_AmazonEKSClusterPolicy]
}

resource "aws_eks_node_group" "this" {
  cluster_name             = aws_eks_cluster.this.name
  node_group_name          = "${var.env}-eks-node-group"
  node_role_arn            = aws_iam_role.eks_node_role.arn
  subnet_ids               = var.private_subnet_ids
  instance_types           = var.instance_type

  scaling_config {
    desired_size           = var.desired_size
    max_size               = var.max_size
    min_size               = var.min_size
  }

  depends_on               = [
    aws_eks_cluster.this,
    aws_iam_role_policy_attachment.worker_node_policies
  ]
}

data "aws_eks_cluster" "this" {
  name                     = aws_eks_cluster.this.name
}

data "aws_eks_cluster_auth" "this" {
  name                     = aws_eks_cluster.this.name
}

data "aws_security_group" "eks_cluster_sg" {
  id                       = data.aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
}
