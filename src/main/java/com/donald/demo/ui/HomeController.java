package com.donald.demo.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;


import com.donald.demo.ui.model.Demonstration;
import com.donald.demo.ui.model.moneytransfer.MoneyTransferModel;
import com.donald.demo.ui.model.moneytransfer.MoneyTransferResponse;
import com.donald.demo.ui.model.moneytransfer.MoneyTransferState;
import com.donald.demo.ui.model.moneytransfer.WorkflowStatus;
import com.donald.demo.ui.util.TemporalClient;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.filter.v1.StartTimeFilter;
import io.temporal.api.filter.v1.WorkflowTypeFilter;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.api.workflowservice.v1.ListClosedWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListClosedWorkflowExecutionsResponse;
import io.temporal.api.workflowservice.v1.ListOpenWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListOpenWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class HomeController {
    @Autowired
    private Demonstration theDemos;
    @Autowired
    private MoneyTransferModel toAccounts;
    @Autowired
    WorkflowClient client;

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    public String welcome(Model model) {
        System.out.println(theDemos.toString());
        model.addAttribute("listDemos", theDemos.getDemo());
        return "welcome";
    }

    @GetMapping("/hello-world")
    public String getHelloWorld(Model model) {
        model.addAttribute("sample", "Hello World");
        return new String("hello-world");
    }

    @GetMapping("/money-transfer-welcome")
    public String getMoneyTransferWelcome(Model model) {
        model.addAttribute("appName","Money Transfer");
        model.addAttribute("accounts", toAccounts.getDestinationAccounts());
        model.addAttribute("options", toAccounts.getWorkflowOptions());
        return new String("money-transfer-welcome");
    }

    @GetMapping("/money-transfer-details")
    public String getMoneyTransferDetails (@RequestParam(required = false) String workflowID, Model model) {
        System.out.println("money-transfer-details method entry.");
        System.out.println("Request parameter is -" + workflowID);
        model.addAttribute("appName","Money Transfer");
        model.addAttribute("selectedWorkflow", workflowID);
        // Setup a new model state object that is returned if workflow identified is not found.
        MoneyTransferState moneyTransferState = new MoneyTransferState();
        MoneyTransferResponse moneyTransferResp = new MoneyTransferResponse();
        moneyTransferResp.setChargeId("Not yet set");
        moneyTransferState.setMoneyTransferResponse(moneyTransferResp);


        if ((workflowID != null) && (workflowID.length() > 1))
        {
               WorkflowStub wfStub = client.newUntypedWorkflowStub(workflowID);
               moneyTransferState = wfStub.query("transferStatus", MoneyTransferState.class);
               logger.debug("moneyTransferState Returned: " +moneyTransferState.toString());
        }

        model.addAttribute("moneyTransferState", moneyTransferState);
        
        WorkflowServiceStubs wfStubs = client.getWorkflowServiceStubs();
        ListOpenWorkflowExecutionsResponse openResponse =
            wfStubs.blockingStub()
                   .listOpenWorkflowExecutions(
                    ListOpenWorkflowExecutionsRequest.newBuilder()
                      //.setStartTimeFilter(StartTimeFilter.newBuilder().setEarliestTime(TemporalClient.getOneHourAgo()).build())
                      .setTypeFilter(WorkflowTypeFilter.newBuilder().setName("TransferMoneyWorkflow").build())
                      .setNamespace(client.getOptions().getNamespace())
                      .build()
                );
             
        ListClosedWorkflowExecutionsResponse closedResponse =
          wfStubs.blockingStub()
                 .listClosedWorkflowExecutions(
                    ListClosedWorkflowExecutionsRequest.newBuilder()
                                    .setStartTimeFilter(StartTimeFilter.newBuilder().setEarliestTime(TemporalClient.getOneHourAgo()).build())
                                    .setTypeFilter(WorkflowTypeFilter.newBuilder().setName("TransferMoneyWorkflow").build())
                                    .setNamespace(client.getOptions().getNamespace())
                                    .build()
                 );


        List<WorkflowStatus> workflowStatii = new ArrayList<>();

        logger.debug("We have "+ openResponse.getExecutionsList().size() + " open workflows and " + closedResponse.getExecutionsList().size() + " Closed");
        WorkflowStatus aWfStatus;
        for (WorkflowExecutionInfo wfExecutionInfo : openResponse.getExecutionsList())  {
            aWfStatus = new WorkflowStatus();
            aWfStatus.setWorkflowId(wfExecutionInfo.getExecution().getWorkflowId());
            aWfStatus.setWorkflowStatus(wfExecutionInfo.getStatus().toString());
            aWfStatus.setUrl(TemporalClient.getWorkflowUrl(aWfStatus.getWorkflowId(), wfStubs.getOptions().getTarget(), client.getOptions().getNamespace()));
            
        workflowStatii.add(aWfStatus);
        }
        for (WorkflowExecutionInfo wfExecutionInfo : closedResponse.getExecutionsList())  {
            aWfStatus = new WorkflowStatus();
            aWfStatus.setWorkflowId(wfExecutionInfo.getExecution().getWorkflowId());
            aWfStatus.setWorkflowStatus(wfExecutionInfo.getStatus().toString());
            aWfStatus.setUrl(TemporalClient.getWorkflowUrl(aWfStatus.getWorkflowId(), wfStubs.getOptions().getTarget(), client.getOptions().getNamespace()));

            workflowStatii.add(aWfStatus);
        }

        for (WorkflowStatus wfStatus : workflowStatii) {
            logger.debug("Workflows " + wfStatus.getWorkflowId() + " " + wfStatus.getWorkflowStatus() + " " + wfStatus.getUrl());

            model.addAttribute("workflows",workflowStatii);
        }
        return new String("money-transfer-details");
    } // End getMoneyTransfer Details

    @GetMapping("/hello")
    public String getHello() {
        System.out.println("Hello running from a get request");
        return new String("/");
    }
    @PostMapping("/post-hello")
    public String postMethodName(@RequestBody String entity) {
        //TODO: process POST request
        System.out.println("post-hello Entered");

        System.out.println(entity.toString());
        
        return "hello-world";
    }
    
}
