package org.eyematics.process.utils.client;

import java.util.Optional;

public interface FttpClient {
    Optional<String> getCrrPseudonym(String dicSourceAndPseudonym);
    Optional<String> getDicPseudonym(String bloomFilter);
    Optional<String> getDicPseudonymForLocalPseudonym(String localPseudonym);
    void testConnection();
}
