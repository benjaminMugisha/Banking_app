module "vpc" {
  source              = "../../modules/vpc"

  cidr_block          = "10.0.0.0/16"
  enable_dns_support  = true
  enable_dns_hostnames= true

  tags                = var.tags
  vpc_name            = "prod-vpc"
  env                 = var.env
}

module "subnets" {
  source              = "../../modules/subnet"
  vpc_id              = module.vpc.vpc_id
  subnet_config       = var.subnet_config
}
locals {
  prefix = module.vpc.vpc_name
}
module "internet_gateway" {
  source              = "../../modules/igw"
  vpc_id              = module.vpc.vpc_id
  name                = "${local.prefix}-igw"
  env                 = var.env
}

module "route_table" {
  source              = "../../modules/route_table"
  vpc_id              = module.vpc.vpc_id
  name                = "${local.prefix}-rt"
  env                 = var.env
  gateway_id          = module.internet_gateway.internet_gateway_id
  public_subnet_ids   = module.subnets.public_subnet_ids
}

module "route53" {
  source              = "../../modules/route53"
  zone_name           = "banking.internal.prod.com"
  vpc_id              = module.vpc.vpc_id
  env                 = var.env
}

module "nat_gateway" {
  source              = "../../modules/natgw"
  vpc_id              = module.vpc.vpc_id
  public_subnet_id    = module.subnets.public_subnet_ids[0]
  private_subnet_ids  = module.subnets.private_subnet_ids
  name                = "prod-natgw"
  env                 = var.env
}

module "rds" {
  source              = "../../modules/rds"
  identifier          = "${var.env}-banking-db"
  env                 = var.env
  db_name             = "${var.env}_${var.db_name}"
  username            = var.db_username
  password            = var.db_password
  private_subnet_ids  = module.subnets.private_subnet_ids
  eks_node_sg_id      = module.eks.eks_node_sg_id
  vpc_id              = module.vpc.vpc_id
  instance_class      = "db.t3.medium"
  allocated_storage   = 20
  multi_az            = true
  storage_type        = "gp3"
}

module "eks" {
  source              = "../../modules/eks"
  cluster_name        = "${var.env}-cluster"
  private_subnet_ids  = module.subnets.private_subnet_ids
  vpc_id              = module.vpc.vpc_id
  role_name           = var.node_role_name
  desired_size        = 3
  max_size            = 6
  min_size            = 3
  env                 = var.env
  cluster_role_name   = var.cluster_role_name
  instance_type       = var.instance_type
}

resource "aws_s3_bucket" "tf_state" {
  bucket              = var.bucket_name
#  prevent_destroy     = false
}

resource "aws_s3_bucket_versioning" "tf_state_versioning" {
  bucket              = aws_s3_bucket.tf_state.id
  versioning_configuration {
    status            = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "tf_state_sse" {
  bucket              = aws_s3_bucket.tf_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm   = "AES256"
    }
  }
}

resource "aws_dynamodb_table" "state_lock" {
  name                = var.table
  billing_mode        = "PAY_PER_REQUEST"
  hash_key            = "LockID"

  attribute {
    name              = "LockID"
    type              = "S"
  }

#  lifecycle {
#    prevent_destroy   = true
#  }
}
resource "aws_secretsmanager_secret" "rds_credentials" {
  name        = "rds_credentials"
  description = "RDS database credentials for our banking app"
}

resource "aws_secretsmanager_secret_version" "rds_credentials_version" {
  secret_id     = aws_secretsmanager_secret.rds_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = var.db_password
    db_name  = var.db_name
  })
}

