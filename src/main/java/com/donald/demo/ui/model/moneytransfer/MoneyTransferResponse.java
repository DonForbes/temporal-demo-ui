package com.donald.demo.ui.model.moneytransfer;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoneyTransferResponse {
    private String chargeId = "Not yet set";
    private String withdrawId = "Not Set";
}
