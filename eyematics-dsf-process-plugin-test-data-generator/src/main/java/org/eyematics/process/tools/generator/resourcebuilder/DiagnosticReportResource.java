package org.eyematics.process.tools.generator.resourcebuilder;

import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Date;


public class DiagnosticReportResource extends AbstractFHIRResourceBuilder<DiagnosticReport, DiagnosticReportResource> {

    private DiagnosticReport.DiagnosticReportStatus diagnosticReportStatus;
    private Date effectiveDate;
    private String patientReference;
    private ArrayList<String> resultsReferences;

    public DiagnosticReportResource() {
        super();
        this.diagnosticReportStatus = DiagnosticReport.DiagnosticReportStatus.FINAL;
        this.effectiveDate = new Date();
        this.patientReference = "";
        this.resultsReferences = new ArrayList<>();
    }

    @Override
    public DiagnosticReportResource randomize() {
        this.randomizeId();
        this.randomizeEffectiveDate();
        this.patientReference = this.getRandomId();
        return this.clearResultsReferences()
                .addResultReference(this.getRandomId())
                .setRandomDiagnosticReportStatus();
    }

    @Override
    public DiagnosticReport build() {
        DiagnosticReport dr = new DiagnosticReport();
        dr.setId(this.id);
        dr.getMeta()
                .getProfile()
                .add(new CanonicalType("https://eyematics.org/fhir/eyematics-kds/StructureDefinition/OphthalmicDiagnosticReport"));
        dr.getMeta()
                .getProfile()
                .add(new CanonicalType(ConsentConstant.CHARACTERISTIC_TO_DELETE));
        dr.getMeta().setLastUpdated(this.effectiveDate);
        dr.setEffective(new DateTimeType(this.effectiveDate));
        dr.setStatus(this.diagnosticReportStatus);
        dr.getIdentifier().add(this.getRandomIdentifier());
        dr.getCategory()
                .add(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category",
                        "exam",
                        null)));
        dr.getCode().addCoding(new Coding("http://loinc.org",
                "78573-3",
                "Ophthalmology Diagnostic study note"));
        for (String resultReference : this.resultsReferences) {
            dr.getResult().add(new Reference("Observation/" + resultReference));
        }
        dr.setSubject(new Reference("Patient/" + this.patientReference));
        dr.setConclusion("Test");
        return dr;
    }

    public DiagnosticReportResource setPatientReference(String patientReference) {
        this.patientReference = patientReference;
        return this;
    }

    public DiagnosticReportResource addResultReference(String resultReference) {
       this.resultsReferences.add(resultReference);
        return this;
    }

    public DiagnosticReportResource addResultReference(Observation resultReference) {
        return this.addResultReference(resultReference.getId());
    }

    public DiagnosticReportResource clearResultsReferences() {
        this.resultsReferences.clear();
        return this;
    }

    public DiagnosticReportResource setDiagnosticReportStatus(DiagnosticReport.DiagnosticReportStatus diagnosticReportStatus) {
        if (diagnosticReportStatus != null) this.diagnosticReportStatus = diagnosticReportStatus;
        return this;
    }

    public DiagnosticReportResource setRandomDiagnosticReportStatus() {
        this.diagnosticReportStatus = DiagnosticReport.DiagnosticReportStatus
                .values()[this.getRandomInteger(0, Consent.ConsentState.values().length - 1)];
        return this;
    }

    public DiagnosticReportResource setEffectiveDate(Date date) {
        this.effectiveDate = date;
        return this;
    }

    public DiagnosticReportResource randomizeEffectiveDate() {
        return this.setEffectiveDate(this.getRandomDate());
    }
}
