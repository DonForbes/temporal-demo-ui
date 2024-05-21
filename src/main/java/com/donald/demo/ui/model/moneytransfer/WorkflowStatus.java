package com.donald.demo.ui.model.moneytransfer;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkflowStatus {

    private String workflowId;
    private String workflowStatus;
    private String url;
}
