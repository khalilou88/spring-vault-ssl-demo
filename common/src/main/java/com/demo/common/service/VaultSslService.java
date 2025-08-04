package com.demo.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class VaultSslService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSslService.class);

    private final VaultTemplate vaultTemplate;

    public VaultSslService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public SSLContext createSslContext(String secretPath) {
        try {
            VaultResponse response = vaultTemplate.read(secretPath);

            if (response == null || response.getData() == null) {
                throw new RuntimeException("No data found at path: " + secretPath);
            }

            String certificatePem = (String) response.getData().get("certificate");
            String privateKeyPem = (String) response.getData().get("private-key");
            String caCertificatePem = (String) response.getData().get("ca-certificate");

            logger.info("Retrieved SSL certificates from Vault for path: {}", secretPath);

            // Create KeyStore for client certificate and private key
            KeyStore keyStore = createKeyStore(certificatePem, privateKeyPem);

            // Create TrustStore for CA certificate
            KeyStore trustStore = createTrustStore(caCertificatePem);

            // Initialize SSL context
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "changeit".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            return sslContext;

        } catch (Exception e) {
            logger.error("Failed to create SSL context from Vault", e);
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    private KeyStore createKeyStore(String certificatePem, String privateKeyPem) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        // Parse certificate
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        String cleanCert = certificatePem.replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", "").replaceAll("\\s", "");
        byte[] certBytes = Base64.getDecoder().decode(cleanCert);
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));

        // Parse private key
        String cleanPrivateKey = privateKeyPem.replaceAll("-----BEGIN PRIVATE KEY-----", "").replaceAll("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanPrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Add to keystore
        keyStore.setKeyEntry("client", privateKey, "changeit".toCharArray(), new java.security.cert.Certificate[]{certificate});

        return keyStore;
    }

    private KeyStore createTrustStore(String caCertificatePem) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(null, null);

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        String cleanCaCert = caCertificatePem.replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", "").replaceAll("\\s", "");
        byte[] caCertBytes = Base64.getDecoder().decode(cleanCaCert);
        X509Certificate caCertificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(caCertBytes));

        trustStore.setCertificateEntry("ca", caCertificate);

        return trustStore;
    }
}