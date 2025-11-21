package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ProvideMailConfig {

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "Mail service recipient addresses for retrieval of Bloomfilter; comma separated list",
            example = "recipient@localhost"
    )
    @Value("${org.eyematics.provide.mail.toAddresses:#{null}}")
    private String provideInformationEmailAddresses;

    public List<String> getProvideInformationEmailAddresses() {
        String raw = provideInformationEmailAddresses == null ? "" : provideInformationEmailAddresses;
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }
}
