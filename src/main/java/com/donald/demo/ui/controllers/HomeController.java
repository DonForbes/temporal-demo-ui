package com.donald.demo.ui.controllers;

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
