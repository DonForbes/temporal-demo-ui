package com.donald.demo.ui.model.operations;

import java.util.Collection;
import lombok.Data;

@Data
public class CloudOperationsNamespace {
    private String name;
    private String activeRegion;
    private String state;
    private int retentionPeriod;
    private String certAuthorityPublicCert;
    private Collection<CloudOperationsUser> cloudOpsUsers;
}
