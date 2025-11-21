package org.eyematics.process.utils.client;

import org.apache.commons.codec.binary.Hex;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FTTPClientSimulatedImpl implements FTTPClient {
    private static final Logger logger = LoggerFactory.getLogger(FTTPClientSimulatedImpl.class);

    @Override
    public Optional<HashMap<String, String>> getGlobalPseudonym(HashSet<String> patientBloomFilter) throws Exception {
        try {
            Map<String, String> pseudonyms = patientBloomFilter.stream()
                    .collect(Collectors.toMap(e -> e,
                            e -> {
                                Optional<String> pseudonym = this.sha256(e);
                                return pseudonym.isPresent() ? EyeMaticsConstants.PROCESS_EYEMATICS_NAME_BASE + pseudonym.get() : null;
                            }));
            logger.warn("Returning simulated DIC pseudonyms '{}' for bloom filter '{}', fTTP connection not configured.",
                    pseudonyms, patientBloomFilter);
            return Optional.of(new HashMap<>(pseudonyms));
        } catch (Exception e) {
            logger.error("Error while getting simulated global pseudonyms: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Optional<String> getGlobalPseudonym(String patientBloomFilter) throws Exception {
        try {
            Optional<String> pseudonym = this.sha256(patientBloomFilter).map(p -> EyeMaticsConstants.PROCESS_EYEMATICS_NAME_BASE + p);
            logger.warn("Returning simulated DIC pseudonym '{}' for bloom filter '{}', fTTP connection not configured.",
                    pseudonym.orElseThrow(), patientBloomFilter);
            return pseudonym;
        } catch (Exception e) {
            logger.error("Error while getting simulated global pseudonym: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    private Optional<String> sha256(String original) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] sha256Hash = digest.digest(original.getBytes(StandardCharsets.UTF_8));
            return Optional.of(Hex.encodeHexString(sha256Hash).substring(0, 16));
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while creating CRR pseudonym");
            throw new RuntimeException(e);
        }
    }
}
