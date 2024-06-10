
package com.donald.demo.ui.model.moneytransfer;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoneyTransferState {
    private int approvalTime;
    private String approvedTime;
    private boolean approvalRequired;

    private int progressPercentage;
    private String transferState;
    private String workflowStatus;
    private MoneyTransferResponse moneyTransferResponse = new MoneyTransferResponse();

}
