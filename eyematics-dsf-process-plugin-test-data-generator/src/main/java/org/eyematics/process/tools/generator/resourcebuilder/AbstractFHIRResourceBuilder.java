package org.eyematics.process.tools.generator.resourcebuilder;

import org.apache.commons.codec.binary.Base64;
import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.hl7.fhir.r4.model.Identifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractFHIRResourceBuilder<R, B> {

    protected String id;

    public AbstractFHIRResourceBuilder() {
        this.id = "";
    }

    public B setId(String id) {
        this.id = id;
        return (B) this;
    }

    public B randomizeId() {
        return this.setId(this.getRandomId());
    }

    protected String getRandomId() {
        return UUID.randomUUID().toString();
    }

    protected String getRandomDateTimeString() {
        long randomMillis = this.getRandomDateTimeLong();
        LocalDateTime randomDate = Instant.ofEpochMilli(randomMillis).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return randomDate.format(formattedDate);
    }

    protected long getRandomDateTimeLong() {
        long startMillis = LocalDateTime.of(2000, 1, 1, 0, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis =  LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ThreadLocalRandom.current().nextLong(startMillis, endMillis);
    }

    protected Date getRandomDate() {
        return new Date(this.getRandomDateTimeLong());
    }

    protected long getRandomLong(long minimum, long maximum) {
        if (maximum == minimum) return maximum;
        if (maximum < minimum) return ThreadLocalRandom.current().nextLong(maximum, minimum);
        return ThreadLocalRandom.current().nextLong(minimum, maximum);
    }

    protected Date getRandomDate(Date minimum, Date maximum) {
        if (maximum.compareTo(minimum) == 0) return maximum;
        if (maximum.before(minimum)) return new Date(ThreadLocalRandom.current().nextLong(maximum.getTime(), minimum.getTime()));
        return new Date(ThreadLocalRandom.current().nextLong(minimum.getTime(), maximum.getTime()));
    }

    protected String getRandomString(String choiceOne, String choiceTwo) {
        return (new Random().nextBoolean()) ? choiceOne : choiceTwo;
    }

    protected double getRandomDouble(double minimum, double maximum) {
        double randomNumber = 0.0d;
        if (maximum == minimum) randomNumber = maximum;
        if (maximum < minimum) randomNumber = ThreadLocalRandom.current().nextDouble(maximum, minimum);
        if (maximum > minimum) randomNumber = ThreadLocalRandom.current().nextDouble(minimum, maximum);
        return Math.round(randomNumber * 100.0d) / 100.0d;
    }

    protected int getRandomInteger(int minimum, int maximum) {
        if (maximum == minimum) return maximum;
        if (maximum < minimum) return ThreadLocalRandom.current().nextInt(maximum, minimum);
        return ThreadLocalRandom.current().nextInt(minimum, maximum);
    }

    protected String getRandomBloomfilter() {
        String bloomfilter = "eyematics-bloomfilter-uuid-" + this.getRandomId();
        return Base64.encodeBase64String(bloomfilter.getBytes());
    }

    protected Identifier getRandomIdentifier() {
        if (this.getRandomBoolean()) {
            return new Identifier().setSystem(ConsentConstant.CHARACTERISTIC_TO_DELETE)
                    .setValue(String.valueOf(UUID.randomUUID().toString()));
        }
        return new Identifier().setValue(ConsentConstant.CHARACTERISTIC_TO_DELETE);
    }

    protected boolean getRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public abstract B randomize();

    public abstract R build();
}
