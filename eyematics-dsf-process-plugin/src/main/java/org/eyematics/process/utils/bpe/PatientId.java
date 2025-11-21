package org.eyematics.process.utils.bpe;

public class PatientId {

    public static String extract(String reference) {
        if (reference.contains("Patient/")) return reference.substring(reference.lastIndexOf("/") + 1);
        return null;
    }

}
