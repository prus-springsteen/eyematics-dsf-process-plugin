/**
 * @author Hauke Hund (https://github.com/hhund)
 * @see    https://github.com/num-codex/codex-processes-ap1/blob/main/codex-process-data-transfer/src/main/java/de/netzwerk_universitaetsmedizin/codex/processes/data_transfer/client/FttpClient.java
 */

package org.eyematics.process.utils.client;

import java.util.Optional;

public interface FttpClient {
    Optional<String> getCrrPseudonym(String dicSourceAndPseudonym);
    Optional<String> getDicPseudonym(String bloomFilter);
    Optional<String> getDicPseudonymForLocalPseudonym(String localPseudonym);
    void testConnection();
}
