package org.eyematics.process.utils.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Optional;

public class FTTPClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(FTTPClientFactory.class);
    private final String fttpServerBase;
    private final Path fttpClientCertificate;
    private final char[] fttpClientCertificatePassword;
    private final String fttpBasicAuthUsername;
    private final String fttpBasicAuthPassword;
    private final String fttpStudy;
    private final String fttpTarget;
    private final String fttpApiKey;
    private final int fttpConnectTimeout;

    public FTTPClientFactory(String fttpServerBase, Path fttpClientCertificate, char[] fttpClientCertificatePassword,
                             String fttpBasicAuthUsername, String fttpBasicAuthPassword, String fttpStudy,
                             String fttpTarget, String fttpApiKey, int fttpConnectTimeout) {
        this.fttpServerBase = fttpServerBase;
        this.fttpClientCertificate = fttpClientCertificate;
        this.fttpClientCertificatePassword = fttpClientCertificatePassword;
        this.fttpBasicAuthUsername = fttpBasicAuthUsername;
        this.fttpBasicAuthPassword = fttpBasicAuthPassword;
        this.fttpStudy = fttpStudy;
        this.fttpTarget = fttpTarget;
        this.fttpApiKey = fttpApiKey;
        this.fttpConnectTimeout = fttpConnectTimeout;
    }

    private boolean isConfigured() {
        return this.fttpServerBase != null && this.fttpClientCertificate != null &&
                this.fttpClientCertificatePassword != null && this.fttpBasicAuthUsername != null &&
                this.fttpBasicAuthPassword != null && this.fttpStudy != null && this.fttpTarget != null &&
                this.fttpApiKey != null && this.fttpConnectTimeout > 0;
    }

    public FTTPClient getFTTPClient() {
        Optional<SSLContext> sslContext = this.getSSLContext(this.fttpClientCertificate, this.fttpClientCertificatePassword);
        if (!this.isConfigured() || sslContext.isEmpty()) {
            logger.warn("FTTP client not configured. Creating simulated client.");
            return new FTTPClientSimulatedImpl();
        }
        return new FTTPClientImpl(this.fttpServerBase, sslContext.get(), this.fttpBasicAuthUsername,
                this.fttpBasicAuthPassword, this.fttpStudy, this.fttpTarget, this.fttpApiKey, this.fttpConnectTimeout);
    }

    private Optional<SSLContext> getSSLContext(Path fttpClientCertificate, char[] fttpClientCertificatePassword) {
        try {
            KeyStore keyStore = this.getKeyStore().orElseThrow();
            KeyManagerFactory kmf = this.getKeyManagerFactory(keyStore).orElseThrow();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            return Optional.of(sslContext);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<KeyStore> getKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(this.fttpClientCertificate.toFile()), this.fttpClientCertificatePassword);
            return Optional.of(keyStore);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<KeyManagerFactory> getKeyManagerFactory(KeyStore keyStore) {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, fttpClientCertificatePassword);
            return Optional.of(kmf);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
