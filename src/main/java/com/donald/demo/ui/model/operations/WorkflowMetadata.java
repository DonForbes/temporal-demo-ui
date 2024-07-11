package com.donald.demo.ui.model.operations;

import lombok.Data;

@Data
public class WorkflowMetadata {
    private String apiKey;
    private Boolean isNewNamespace;
    private int manageNamespaceTimeoutMins=11;
}
