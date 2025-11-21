package org.eyematics.process.tools.generator.resourcebuilder;

import org.apache.commons.codec.binary.Base64;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractFHIRResourceBuilder<R, B> {

    private R resource;

    public AbstractFHIRResourceBuilder(R resource) {
        this.resource = resource;
        this.init();
        this.randomize();
    }

    protected void init() {}

    protected void setResource(R resource) {
        this.resource = resource;
    }

    protected R getResource() {
        return this.resource;
    }

    protected String getRandomDateTimeString() {
        long randomMillis = this.getRandomDateTimeLong();
        LocalDateTime randomDate = Instant.ofEpochMilli(randomMillis).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return randomDate.format(formattedDate);
    }

    protected long getRandomDateTimeLong() {
        long startMillis = LocalDateTime.of(2000, 1, 1, 0, 0)
                .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        long endMillis =  LocalDateTime.of(2023, 12, 31, 23, 59)
                .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        return ThreadLocalRandom.current().nextLong(startMillis, endMillis);
    }

    protected String getRandomString(String choiceOne, String choiceTwo) {
        return (new Random().nextBoolean()) ? choiceOne : choiceTwo;
    }

    protected double getRandomDouble(double minimum, double maximum) {
        double randomNumber = ThreadLocalRandom.current().nextDouble(minimum, maximum);
        return Math.round(randomNumber * 100.0d) / 100.0d;
    }

    protected int getRandomInteger(int minimum, int maximum) {
        return ThreadLocalRandom.current().nextInt(minimum, maximum);
    }

    protected String getRandomBloomfilter() {
        Random random = ThreadLocalRandom.current();
        byte[] r = new byte[252];
        random.nextBytes(r);
        return Base64.encodeBase64String(r);
    }

    protected boolean getRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public abstract B randomize();

    public abstract R build();
}
