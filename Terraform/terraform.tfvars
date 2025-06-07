subnet_config = [
  { name = "public-1", cidr_block = "10.0.1.0/24", availability_zone = "eu-west-1a", type = "public" },
  { name = "public-2", cidr_block = "10.0.2.0/24", availability_zone = "eu-west-1b", type = "public" },
  { name = "private-1", cidr_block = "10.0.3.0/24", availability_zone = "eu-west-1a", type = "private" },
  { name = "private-2", cidr_block = "10.0.4.0/24", availability_zone = "eu-west-1b", type = "private" }
]
