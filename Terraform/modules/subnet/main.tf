resource "aws_subnet" "this" {
  for_each = {
    for subnet in var.subnet_config : subnet.name => subnet
  }

  vpc_id            = var.vpc_id
  cidr_block        = each.value.cidr_block
  availability_zone = each.value.availability_zone
  map_public_ip_on_launch = each.value.type == "public" ? true : false

  tags = {
    Name = each.key
    type = each.value.type
  }
  lifecycle {
    prevent_destroy = true
  }
}
