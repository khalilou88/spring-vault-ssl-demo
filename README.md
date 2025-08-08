# spring-vault-ssl-bundle Demo 

This demo shows how to store PEM certificates in HashiCorp Vault and use them with Spring Boot SSL bundles for secure communication between two servers.

## Architecture

```
┌─────────────┐    HTTPS/SSL     ┌─────────────┐
│   Server A  │ ───────────────► │   Server B  │
│  Port 8443  │                  │  Port 8444  │
└─────────────┘                  └─────────────┘
       │                               │
       │        Vault Token Auth       │
       └─────────────┬─────────────────┘
                     │
              ┌─────────────┐
              │    Vault    │
              │  Port 8200  │
              │ (Dev Mode)  │
              └─────────────┘
```

## Project Structure

```
spring-vault-ssl-bundle-demo/
├── pom.xml                 # Root POM
├── common/                 # Shared DTOs and services
│   ├── src/main/java/
│   │   └── com/demo/common/
│   │       └──dto/        # MessageRequest, MessageResponse
│   └── pom.xml
├── server-a/               # Server A (Client + Server)
│   ├── src/main/java/
│   │   └── com/demo/servera/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── server-b/               # Server B (Server only)
│   ├── src/main/java/
│   │   └── com/demo/serverb/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── docker-compose.yml      # Vault setup
├── generate-certs.sh       # Certificate generation
└── certificates/           # Generated certificates
```

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- OpenSSL (for certificate generation)

## Quick Start

### 1. Generate Certificates

```bash
chmod +x generate-certs.sh
./generate-certs.sh
```

This creates:
- `certificates/ca-cert.pem` - Certificate Authority
- `certificates/server-a-cert.pem` & `certificates/server-a-key.pem` - Server A SSL
- `certificates/server-b-cert.pem` & `certificates/server-b-key.pem` - Server B SSL

### 2. Start Vault

```bash
# Start Vault
docker compose up -d

# Make sure the init script is executable
chmod +x vault-init.sh

# Run script and initialize with certificates
./vault-init.sh
```

This will:
- Start Vault in dev mode (port 8200)
- Initialize KV secrets engine
- Store all PEM certificates in Vault at paths:
    - `secret/ssl-certs/server-a`
    - `secret/ssl-certs/server-b`

### 3. Build the Project

```bash
./mvnw clean install
```

### 4. Start the Servers

**Terminal 1 - Server B:**
```bash
./mvnw spring-boot:run -pl server-b
```

**Terminal 2 - Server A:**
```bash
./mvnw spring-boot:run -pl server-a
```

## Testing the Demo

### 1. Check Server Health

```bash
# Server A health (with SSL)
curl -k https://localhost:8443/api/v1/health

# Server B health (with SSL)
curl -k https://localhost:8444/api/v1/health
```

### 2. Test Inter-Server Communication

```bash
# Test Server A → Server B communication
curl -k -X POST "https://localhost:8443/api/v1/test/send-to-server-b?message=Hello%20World"

# Simple ping test
curl -k https://localhost:8443/api/v1/test/ping-server-b
```

### 3. Direct Server Communication

```bash
# Send message directly to Server A
curl -k -X POST https://localhost:8443/api/v1/message \
  -H "Content-Type: application/json" \
  -d '{"message":"Direct message","timestamp":"2024-01-01T10:00:00"}'

# Send message directly to Server B
curl -k -X POST https://localhost:8444/api/v1/message \
  -H "Content-Type: application/json" \
  -d '{"message":"Direct message","timestamp":"2024-01-01T10:00:00"}'
```

## Vault Integration Details

### Certificate Storage Structure

```
secret/
└── ssl-certs/
    ├── server-a/
    │   ├── certificate     # PEM certificate
    │   ├── private-key     # PEM private key
    │   └── ca-certificate  # CA certificate
    └── server-b/
        ├── certificate     # PEM certificate
        ├── private-key     # PEM private key
        └── ca-certificate  # CA certificate
```

### SSL Bundle Configuration

Spring Boot automatically retrieves certificates from Vault using the `vault:` prefix:

```yaml
spring:
  ssl:
    bundle:
      pem:
        server-a-ssl:
          keystore:
            certificate: "vault:secret/ssl-certs/server-a:certificate"
            private-key: "vault:secret/ssl-certs/server-a:private-key"
          truststore:
            certificate: "vault:secret/ssl-certs/server-a:ca-certificate"
```

### Vault Authentication

The demo uses token authentication for simplicity:

```yaml
spring:
  cloud:
    vault:
      authentication: TOKEN
      token: demo-root-token
```

## Key Features Demonstrated

1. **PEM Certificate Storage**: Certificates stored as text in Vault KV store
2. **SSL Bundle Integration**: Spring Boot 3.1+ SSL bundles with Vault backend
3. **Mutual TLS**: Both servers use certificates for client authentication
4. **Multi-Module Maven**: Shared common module for DTOs and services
5. **Inter-Service Communication**: HTTPS communication with client certificates
6. **Dynamic Certificate Loading**: Certificates loaded from Vault at runtime

## Troubleshooting

### Common Issues

1. **Certificate Verification Failed**
    - Ensure certificates are properly generated with correct SAN entries
    - Check Vault connectivity and token validity

2. **SSL Handshake Failures**
    - Verify both keystore and truststore are configured
    - Check certificate validity periods

3. **Vault Connection Issues**
    - Ensure Vault is running: `docker compose ps`
    - Check Vault logs: `docker compose logs vault`

### Debugging

Enable verbose SSL logging:
```yaml
logging:
  level:
    javax.net.ssl: DEBUG
    org.springframework.vault: DEBUG
```

### Vault CLI Access

```bash
# Access Vault CLI
docker exec -it vault-demo vault status

# List secrets
docker exec -it vault-demo vault kv list secret/ssl-certs

# Read certificate
docker exec -it vault-demo vault kv get secret/ssl-certs/server-a
```

## Production Considerations

1. **Use proper Vault authentication** (AppRole, Kubernetes, etc.)
2. **Configure certificate rotation** policies
3. **Use proper CA certificates** (not self-signed)
4. **Implement certificate monitoring** and alerts
5. **Use Vault namespaces** for multi-tenancy
6. **Configure proper RBAC** policies

## Cleanup

```bash
# Stop services
docker compose down

# Remove certificates
rm -rf certificates/

# Clean Maven build
./mvnw clean
```

This demo provides a complete working example of using Vault-stored PEM certificates with Spring Boot SSL bundles in a multi-module Maven project.