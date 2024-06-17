package com.donald.demo.ui.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.donald.demo.ui.model.moneytransfer.MoneyTransferModel;
import com.donald.demo.ui.model.moneytransfer.MoneyTransferState;
import com.donald.demo.ui.util.TemporalClient;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;

@Controller
public class MoneyTransferController {
    @Autowired
    private MoneyTransferModel toAccounts;
    @Autowired
    WorkflowClient client;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @PostMapping("/approveTransfer")
    public ResponseEntity postApproveTransfer(@RequestBody MoneyTransferState moneyTransferState) {
        // TODO: process POST request
        System.out.println("postapproveTransfer method entry.");

        System.out.println(moneyTransferState.toString());

        try {
            WorkflowStub workflowStub = client.newUntypedWorkflowStub(moneyTransferState.getWorkflowId());

            workflowStub.signal("approveTransfer");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return new ResponseEntity<>("/money-transfer-details?workflowID=" + moneyTransferState.getWorkflowId(), HttpStatus.OK);
    }
}
