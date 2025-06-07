output "zone_id" {
  description = "The ID of the private hosted zone"
  value       = aws_route53_zone.private.zone_id
}