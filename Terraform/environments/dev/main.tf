module "vpc" {
  source             = "../../modules/vpc"

  cidr_block         = "10.0.0.0/16"
  enable_dns_support = true
  enable_dns_hostnames= true

  tags               = {
    Name             = "dev-vpc"
  }
  vpc_name           = "dev-vpc"
}

module "subnets" {
  source             = "../../modules/subnet"
  vpc_id             = module.vpc.vpc_id
  subnet_config      = var.subnet_config
}
locals {
  prefix             = module.vpc.vpc_name
}
module "internet_gateway" {
  source             = "../../modules/igw"
  vpc_id             = module.vpc.vpc_id
  name               = "${local.prefix}-dev-igw"
}

module "route_table" {
  source             = "../../modules/route_table"
  vpc_id             = module.vpc.vpc_id
  name               = "${local.prefix}-dev-rt"
  gateway_id         = module.internet_gateway.internet_gateway_id
  public_subnet_ids  = module.subnets.public_subnet_ids
}

module "route53" {
  source              = "../../modules/route53"
  zone_name           = "internal.example.com"
  vpc_id              = module.vpc.vpc_id
}

module "nat_gateway" {
  source              = "../../modules/natgw"
  vpc_id              = module.vpc.vpc_id
  public_subnet_id    = module.subnets.public_subnet_ids[0]
  private_subnet_ids  = module.subnets.private_subnet_ids
  name                = "dev-nat-gateway"
}

module "rds" {
  source              = "../../modules/rds"
  identifier          = "banking-db"
  db_name             = "dev-db"
  username            = "dev-username"
  password            = "dev-password"
  private_subnet_ids  = module.subnets.private_subnet_ids
  eks_node_sg_id      = module.eks.eks_node_sg_id
  vpc_id              = module.vpc.vpc_id
  instance_class      = "db.t3.micro"
  allocated_storage   = 5
  storage_type        = "gp2"
  multi_az            = "false"
}

module "eks" {
  source              = "../../modules/eks"
  cluster_name        = "dev-cluster"
  private_subnet_ids  = module.subnets.private_subnet_ids
  vpc_id              = module.vpc.vpc_id
  desired_size        = 1
  max_size            = 1
  min_size            = 2
}

resource "aws_s3_bucket" "tf_state" {
  bucket              = "s3statebackendbenjamin12233"
  force_destroy       = true
}

resource "aws_s3_bucket_versioning" "tf_state_versioning" {
  bucket              = aws_s3_bucket.tf_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "tf_state_sse" {
  bucket               = aws_s3_bucket.tf_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm    = "AES256"
    }
  }
}

resource "aws_dynamodb_table" "state_lock" {
  name                 = var.dynamo_db_name
  billing_mode         = "PAY_PER_REQUEST"
  hash_key             = "LockID"

  attribute {
    name               = "LockID"
    type               = "S"
  }

  lifecycle {
    prevent_destroy    = true
  }
}
