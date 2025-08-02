output "rds_endpoint" {
  value = module.rds.rds_endpoint
}

output "db_name" {
  value = module.rds.db_name
}

output "cluster_name" {
  value = module.eks.cluster_name
}
