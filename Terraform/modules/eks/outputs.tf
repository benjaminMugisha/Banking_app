output "cluster_endpoint" {
  value = aws_eks_cluster.this.endpoint
}

output "cluster_name" {
  value = aws_eks_cluster.this.name
}
output "eks_node_sg_id" {
  value = data.aws_security_group.eks_cluster_sg.id
}