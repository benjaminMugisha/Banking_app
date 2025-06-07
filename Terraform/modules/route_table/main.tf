resource "aws_route_table" "this" {
  vpc_id = var.vpc_id

  tags = {
    Name = "public-route-table"
  }
}

resource "aws_route" "internet_access" {
  route_table_id         = aws_route_table.this.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = var.gateway_id
}

resource "aws_route_table_association" "public_subnet_assoc" {
  for_each = {
    for idx, subnet_id in var.public_subnet_ids : idx => subnet_id
  }

  subnet_id      = each.value
  route_table_id = aws_route_table.this.id
}
