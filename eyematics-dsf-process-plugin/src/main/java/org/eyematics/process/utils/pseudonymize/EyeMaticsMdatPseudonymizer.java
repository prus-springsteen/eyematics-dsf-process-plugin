package org.eyematics.process.utils.pseudonymize;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class EyeMaticsMdatPseudonymizer {

    private static final String SHA2_ALGORITHM = "SHA-256";
    private final byte[] saltArray;

    public EyeMaticsMdatPseudonymizer(String salt) {
        this.saltArray = salt.getBytes(StandardCharsets.UTF_8);
    }

    public Optional<String> pseudonymize(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(this.saltArray);
            byte[] sha256Hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Optional.of(Hex.encodeHexString(sha256Hash));
        } catch (NoSuchAlgorithmException e) {
            return Optional.empty();
        }
    }
}
