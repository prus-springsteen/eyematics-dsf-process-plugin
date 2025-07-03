/**
 * @see    https://github.com/medizininformatik-initiative/mii-processes-common/blob/main/src/main/java/de/medizininformatik_initiative/processes/common/util/ConstantsBase.java
 */

package org.eyematics.process.constant;

import static org.eyematics.process.EyeMaticsProcessPluginDefinition.VERSION;

public interface EyeMaticsConstants {

	String PROCESS_VERSION = VERSION.substring(4, 7);
	String RESOURCE_VERSION = VERSION.substring(0, 3);

	String PROCESS_EYEMATICS_NAME_BASE = "eyematicsorg_";
	String NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_EYEMATICS = "eyematics.org";

	String PROCESS_MII_NAME_BASE = "medizininformatik-initiativede_";
	String PROCESS_MII_URI_BASE = "http://medizininformatik-initiative.de/bpe/Process/";

	String NAMINGSYSTEM_MII_PROJECT_IDENTIFIER = "http://medizininformatik-initiative.de/sid/project-identifier";
	String NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_MEDICAL_INFORMATICS_INITIATIVE_CONSORTIUM = "medizininformatik-initiative.de";

	String CODESYSTEM_DSF_ORGANIZATION_ROLE = "http://dsf.dev/fhir/CodeSystem/organization-role";
	String CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_DMS = "DMS";
	String CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_DIC = "DIC";
	String CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_HRP = "HRP";

	String CODESYSTEM_MII_CRYPTOGRAPHY = "http://medizininformatik-initiative.de/fhir/CodeSystem/cryptography";
	String CODESYSTEM_MII_CRYPTOGRAPHY_VALUE_PUBLIC_KEY = "public-key";

	String CODESYSTEM_GENERIC_DATA_SET_STATUS = "http://eyematics.org/fhir/CodeSystem/eyematics-generic-process-data-set-status-code-system";
	String CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS  = "data-set-status";
	String CODESYSTEM_DATA_SET_STATUS = "http://eyematics.org/fhir/CodeSystem/eyematics-generic-process-data-set-status-codes-code-system";
	String EXTENSION_DATA_SET_STATUS_ERROR_URL = "http://eyematics.org/fhir/StructureDefinition/eyematics-generic-data-set-status-error-extension";
	String CODESYSTEM_DATA_SET_STATUS_VALUE_NOT_ALLOWED = "not-allowed";
	String CODESYSTEM_DATA_SET_STATUS_VALUE_NOT_REACHABLE = "not-reachable";
	String CODESYSTEM_DATA_SET_STATUS_VALUE_RECEIPT_MISSING = "receipt-missing";
	String CODESYSTEM_DATA_SET_STATUS_VALUE_RECEIPT_OK = "receipt-ok";
	String CODESYSTEM_DATA_SET_STATUS_VALUE_RECEIPT_ERROR = "receipt-error";

	String CODESYSTEM_DATA_SET_STATUS_VALUE_PROVIDE_OK = "provide-ok";
	String CODESYSTEM_DATA_SET_STATUS_VALUE_PROVIDE_ERROR = "provide-error";

	String CODESYSTEM_DATA_SET_STATUS_VALUE_FTTP_NOT_REACHABLE = "fttp-not-reachable";


	int DSF_CLIENT_RETRY_6_TIMES = 6;
	long DSF_CLIENT_RETRY_INTERVAL_10SEC = 10000;
	long DSF_CLIENT_RETRY_INTERVAL_5MIN = 300000;

	String MEDIA_TYPE_APPLICATION_FHIR_XML = "application/fhir+xml";


	String PSEUDONYM_PATTERN_STRING = "(?<source>[^/]+)/(?<original>[^/]+)";



}