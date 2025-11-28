package org.eyematics.process.constant;

import java.util.*;

import static org.eyematics.process.EyeMaticsProcessPluginDefinition.VERSION;


public interface EyeMaticsConstants {

    String PROCESS_VERSION = VERSION.substring(4, 7);
    String RESOURCE_VERSION = VERSION.substring(0, 3);

    String PROCESS_EYEMATICS_NAME_BASE = "eyematicsorg_";
    String PROCESS_EYEMATICS_URI_BASE = "https://eyematics.org/bpe/Process/";
    String NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_EYEMATICS = "eyematics.org";

    String CODESYSTEM_DSF_ORGANIZATION_ROLE = "http://dsf.dev/fhir/CodeSystem/organization-role";
    String CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_DMS = "DMS";
    String CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_DIC = "DIC";
    String CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_HRP = "HRP";

    String CODESYSTEM_MII_CRYPTOGRAPHY = "http://medizininformatik-initiative.de/fhir/CodeSystem/cryptography";
    String CODESYSTEM_MII_CRYPTOGRAPHY_VALUE_PUBLIC_KEY = "public-key";

    String CODESYSTEM_GENERIC_DATA_SET_STATUS = "https://eyematics.org/fhir/CodeSystem/eyematics-generic-process-data-set-status-code-system";
    String CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS = "data-set-status";
    String CODESYSTEM_DATA_SET_STATUS = "https://eyematics.org/fhir/CodeSystem/eyematics-generic-process-data-set-status-codes-code-system";
    String EXTENSION_DATA_SET_STATUS_ERROR_URL = "https://eyematics.org/fhir/StructureDefinition/eyematics-generic-process-data-set-status-error-extension";

    int DSF_CLIENT_RETRY_6_TIMES = 6;
    long DSF_CLIENT_RETRY_INTERVAL_10SEC = 10000;
    long DSF_CLIENT_RETRY_INTERVAL_5MIN = 300000;

    String MEDIA_TYPE_APPLICATION_FHIR_XML = "application/xml;charset=utf-8";
    String MEDIA_TYPE_APPLICATION_FHIR_JSON = "application/fhir+json;charset=utf-8";
    String PSEUDONYM_PATTERN_STRING = "(?<source>[^/]+)/(?<original>[^/]+)";


    /**
     * Constants for EyeMatics FHIR resource configurations.
     *
     * @see <a href="https://imi-ms.github.io/eyematics-kds/artifacts.html">EyeMatics KDS Artifacts Documentation</a>
     * @see <a href="https://simplifier.net/guide/mii-ig-modul-consent-2025/MII-IG-Modul-Consent?version=2025.0.4">Kerndatensatz Modul Consent</a>
     */
    String EYEMATICS_CORE_DATA_SET_URI = "https://eyematics.org/fhir/eyematics-kds/StructureDefinition/";
    ArrayList<String> EYEMATICS_CORE_DATASET_OBSERVATION_PROFILE = new ArrayList<>(Arrays.asList("Angiography",
                                                                                                "AnteriorChamberCells",
                                                                                                "AnteriorChamberFlare",
                                                                                                "mii-eyematics-ivom-hand-movement-perception",
                                                                                                "IOP",
                                                                                                "MacularEdema",
                                                                                                "OphthalmicObservation",
                                                                                                "OpticDiscDiameter",
                                                                                                "PapillEdema",
                                                                                                "RNFLThickness",
                                                                                                "RetinalThickness",
                                                                                                "RetinalVasculitis",
                                                                                                "IrisSynechiae",
                                                                                                "observation-visual-acuity"));
    String EYEMATICS_CORE_DATASET_MEDICATION_PROFILE = "mii-eyematics-ivom-medication";
    String EYEMATICS_CORE_DATASET_MEDICATION_ADMINISTRATION_PROFILE = "mii-eyematics-ivom-medicationadministration";
    String EYEMATICS_CORE_DATASET_MEDICATION_REQUEST_PROFILE = "mii-eyematics-ivi-medicationrequest";

    String MII_IG_MODUL_CONSENT_PROFILE = "https://www.medizininformatik-initiative.de/fhir/modul-consent/StructureDefinition/mii-pr-consent-einwilligung";
    String MII_IG_MODUL_CONSENT_CATEGORY_LOINC_SYSTEM = "http://loinc.org";
    String MII_IG_MODUL_CONSENT_CATEGORY_LOINC_CODE = "57016-8";
    String MII_IG_MODUL_CONSENT_CATEGORY_MII_SYSTEM = "https://www.medizininformatik-initiative.de/fhir/modul-consent/CodeSystem/mii-cs-consent-consent_category";
    String MII_IG_MODUL_CONSENT_CATEGORY_MII_CODE = "2.16.840.1.113883.3.1937.777.24.2.184";
    String MII_IG_MODUL_CONSENT_PROVISION_SYSTEM = "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3";
    String MII_IG_MODUL_CONSENT_PROVISION_CODE = "2.16.840.1.113883.3.1937.777.24.5.3.9";

    String NAMING_SYSTEM_EYEMATICS_DIC_PSEUDONYM = "https://eyematics.org/sid/dic-pseudonym";
    String NAMING_SYSTEM_EYEMATICS_BLOOM_FILTER = "https://eyematics.org/sid/bloom-filter";
    String NAMING_SYSTEM_EYEMATICS_GLOBAL_PSEUDONYM = "https://ths-greifswald.de/gpas";
}