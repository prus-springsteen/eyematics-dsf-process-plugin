package org.eyematics.process.tools.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.Base64;

public class RSAKeyPairGenerator {

    private static final Logger logger = LoggerFactory.getLogger(RSAKeyPairGenerator.class);
    private static final String[] DIC = { "dic-a", "dic-b", "dic-c", "dic-d" };
    private static final Path baseFolder = Paths.get("../eyematics-dsf-process-plugin-test-setup/secrets");

    public void generateRSAKeyPairs() {
        for (String d : DIC) {
            try {
                String dicName = d.replace("-", "_");
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(4096);
                KeyPair pair = generator.generateKeyPair();
                PrivateKey privateKey = pair.getPrivate();
                PublicKey publicKey = pair.getPublic();
                this.saveKey(privateKey, dicName);
                this.saveKey(publicKey, dicName);
            } catch (Exception e) {
                logger.error("Error while creating key pair for {}: {}", d, e.getMessage());
            }
        }
    }

    private void saveKey(Key key, String keyName) throws FileNotFoundException {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null.");
        }

        String pemName;
        String pem;
        if (key instanceof PrivateKey) {
            pemName = keyName + "_private_key.pem";
            pem = this.toPem("PRIVATE KEY", key.getEncoded());
        } else if (key instanceof PublicKey) {
            pemName = keyName + "_public_key.pem";
            pem = this.toPem("PUBLIC KEY", key.getEncoded());
        } else {
            throw new IllegalArgumentException("Unsupported key type: " + key.getClass().getName());
        }

        Path keyPath = baseFolder.resolve(pemName);
        try (FileOutputStream os = new FileOutputStream(keyPath.toFile())) {
            os.write(pem.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toPem(String type, byte[] derBytes) {
        String publicKeyFormatted = "-----BEGIN " + type + "-----" + System.lineSeparator();
        publicKeyFormatted += Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(derBytes) + System.lineSeparator();
        publicKeyFormatted += "-----END " + type + "-----" + System.lineSeparator();
        return publicKeyFormatted;
    }
}
