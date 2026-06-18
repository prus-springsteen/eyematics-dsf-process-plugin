package org.eyematics.process.tools.generator.resourcebuilder;

import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.hl7.fhir.r4.model.*;

import java.util.Date;


public class MeasureReportResource extends AbstractFHIRResourceBuilder<MeasureReport, MeasureReportResource> {

    private int count;
    private int maxCount;
    private Date start;
    private Date end;

    public MeasureReportResource() {
        super();
        this.count = 0;
        this.maxCount = 0;
        this.start = new Date();
        this.end = new Date();
    }

    @Override
    public MeasureReportResource randomize() {
        this.randomizeId();
        this.maxCount = this.getRandomInteger(0, 100);
        this.count = this.getRandomInteger(0, this.maxCount + 1);
        this.randomizePeriod();
        return this;
    }

    @Override
    public MeasureReport build() {
        MeasureReport mr = new MeasureReport();
        mr.setId(this.id);
        mr.getMeta().getProfile()
                .add(new CanonicalType("https://eyematics.org/fhir/MeasureReport/ivi-patients-summary"));
        mr.getMeta().getProfile()
                .add(new CanonicalType(ConsentConstant.CHARACTERISTIC_TO_DELETE));
        mr.getIdentifier().add(this.getRandomIdentifier());
        mr.setStatus(MeasureReport.MeasureReportStatus.COMPLETE);
        mr.setType(MeasureReport.MeasureReportType.SUMMARY);
        Period period = new Period();
        period.setStart(this.start);
        period.setEnd(this.end);
        mr.setPeriod(period);
        MeasureReport.MeasureReportGroupComponent groupComponent = new MeasureReport.MeasureReportGroupComponent();
        groupComponent.setCode(new CodeableConcept().setText("IVOM-Patienten"));
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/measure-population")
                .setCode("measure-population")
                .setDisplay("Measure Population"));
        codeableConcept.setText("Patienten mit OPS 5-156.9");
        groupComponent.setCode(codeableConcept);
        MeasureReport.MeasureReportGroupPopulationComponent population = new MeasureReport.MeasureReportGroupPopulationComponent();
        population.setCount(this.count);
        groupComponent.addPopulation(population);
        mr.getGroup().add(groupComponent);
        return mr;
    }

    public MeasureReportResource setCount(int count) {
        if (count >= 0) this.count = count;
        return this;
    }

    public MeasureReportResource setMaxRandomCount(int maxCount) {
        if (maxCount >= 0) this.maxCount = maxCount;
        return this;
    }

    public MeasureReportResource setPeriod(Date start, Date end) {
        this.start = start;
        this.end = end;
        return this;
    }

    public MeasureReportResource randomizePeriod() {
        Date start = this.getRandomDate();
        Date end = this.getRandomDate(start, new Date(start.getTime() + this.getRandomLong(1L, 31536000000L)));
        return this.setPeriod(start, end);

    }
}
