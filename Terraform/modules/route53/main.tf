resource "aws_route53_zone" "private" {
  name = var.zone_name
  vpc {
    vpc_id = var.vpc_id
  }

  comment       = "Private hosted zone"
  force_destroy = true

  tags = {
    Name = var.zone_name
  }
}
