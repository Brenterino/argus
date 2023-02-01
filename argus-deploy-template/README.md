# Argus Deployment Template

This template can be used to spin up an Argus instance using
[Docker Compose](https://docs.docker.com/compose/).
It is **not** recommended to use this exact configuration in a production
environment.

## JWT Signing/Verifying Key Pair

A private key is required to sign and the public key can then be used to verify
the origin of the JWT.

```bash
openssl genrsa -out rsaPrivateKey.pem 2048
openssl rsa -pubout -in rsaPrivateKey.pem -out public.pem
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out private.pem
```

**Note**: The `rsaPrivateKey.pem` is not required and can be deleted if desired.

## Booting Up

After the JWT Signing/Verifying Key Pair have been created, the application can
be booted up via:

```bash
docker-compose up -d
```

**Note**: For more consistent boots, it is recommended to boot up `postgres`
ahead of any other service so the DB can come up in time to be connected to.

## Changes for Production

[docker-compose-prod.yml](docker-compose-prod.yml) is set up to represent a
configuration similar to what would be used in production. This file will
be referred to as `docker-compose.yml` for the rest of this section.

This is not an exhaustive list of all changes that will make the configuration
**production-ready**, but they are the minimum required/recommended changes.

### Certificate from a Certificate Authority
It is **strongly** recommended to get a certificate from a valid CA to avoid
having to have users enable allowance of self-signed certificates in their
client configuration.

[Let's Encrypt](https://letsencrypt.org/)
is a good option for this purpose as it is free. Documentation
for setting up automatic renewal can be found
[here](https://certbot.eff.org/).

Once a certificate is obtained, make sure to verify the mappings within
[docker-compose.yml](docker-compose.yml) are updated.

### Database Configuration Changes

- It is **strongly** recommended to remove the port mapping from
  [docker-compose.yml](docker-compose.yml) so the database can only
  be accessed from the host VM.
- If remote access is desired, update the password for the Postgres
  instance as well as any references to it in
  [docker-compose.yml](docker-compose.yml)

### Nginx Changes

Referenced changes should be applied to [nginx.conf](nginx.conf):
- Update the `server_name` property to use the proper domain.
- If the API service containers are being hosted on another VM, make sure to
  update the `proxy_pass` variable appropriately.

### API Services Configuration

- Update all references for the `ARGUS_AUTH_ISSUER` to the proper domain.

**WIP**
