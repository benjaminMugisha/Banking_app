output "rds_endpoint" { # to be added to the kubernetes config map's url
  value = module.rds.rds_endpoint
}
