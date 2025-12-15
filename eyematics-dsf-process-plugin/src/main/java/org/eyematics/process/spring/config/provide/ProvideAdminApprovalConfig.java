package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProvideAdminApprovalConfig {

    @ProcessDocumentation(
            required = true,
            processNames = {"eyematicsorg_eyematicsProvideProcess" },
            description = "Whether an admin is needed to approve the requested EyeMatics data transfer `true` or not `false`."
    )
    @Value("${org.eyematics.provide.admin.approval:true}")
    private String isAdminApproval;

    public boolean isAdminApproval() {
        return Boolean.parseBoolean(isAdminApproval);
    }
}
