

resource "aws_db_subnet_group" "this" {
  name       = "db-subnet-group2"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "the DB Subnet Group"
  }
}

resource "aws_db_instance" "this" {
  identifier         = var.identifier
  engine             = "postgres"
  engine_version     = "17.4"
  instance_class     = var.instance_class
  allocated_storage  = var.allocated_storage
  storage_type       = var.storage_type

  username           = var.username
  password           = var.password
  db_name            = var.db_name

  db_subnet_group_name = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]

  skip_final_snapshot = true
  publicly_accessible = false
  multi_az            = var.multi_az
  deletion_protection = false
}

resource "aws_security_group" "rds_sg" {
  name                = "rds-sg"
  description         = "Allow PostgreSQL access from EKS node SG"
  vpc_id              = var.vpc_id

  ingress {
    description       = "PostgreSQL from EKS nodes"
    from_port         = 5432
    to_port           = 5432
    protocol          = "tcp"
    security_groups = [var.eks_node_sg_id]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  tags = {
    Name = "rds-sg"
  }
}
