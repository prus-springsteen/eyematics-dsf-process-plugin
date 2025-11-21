package org.eyematics.process.utils.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public interface FTTPClient {
    Optional<HashMap<String, String>> getGlobalPseudonym(HashSet<String> patientBloomFilter) throws Exception;
    Optional<String> getGlobalPseudonym(String patientBloomFilter) throws Exception;
}
