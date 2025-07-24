output "public_subnet_ids" {
  value = [
    for subnet in aws_subnet.this : subnet.id if subnet.tags["type"] == "public"
  ]
}

output "private_subnet_ids" {
  value = [
    for subnet in aws_subnet.this : subnet.id if subnet.tags["type"] == "private"
  ]
}
