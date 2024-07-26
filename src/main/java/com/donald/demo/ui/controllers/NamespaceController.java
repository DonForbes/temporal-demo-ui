package com.donald.demo.ui.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.donald.demo.ui.model.operations.CloudOpsConfig;
import com.donald.demo.ui.model.operations.CloudOpsServerConfig;
import com.donald.demo.ui.model.operations.WorkflowMetadata;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.workflow.ExternalWorkflowStub;
import io.temporal.workflow.Workflow;

import com.donald.demo.ui.model.operations.CloudOperationsNamespace;

@Controller
public class NamespaceController {
  @Autowired
  CloudOpsServerConfig cloudOpsServerConfig;
  @Autowired
  WorkflowClient client;
  private static final Logger logger = LoggerFactory.getLogger(NamespaceController.class);
  private RestClient restClient = RestClient.create();
  private static String MANAGE_WORKFLOW_PREFIX = "manage-namespace-";
  private static String MANAGE_WORKFLOW_TASK_QUEUE = "ManageNamespaceTaskQueue";

  /** 
  @GetMapping("/namespace-management-detailsNOTUSED/{namepaceName}")
  public String getNamespaceDetails(@RequestParam(required = false, value = "apiKey") String apiKey,
      @PathVariable(required = true, value = "namespaceName") String namespaceName,
      Model model) {
        logger.debug("Method Entry - getNamespaceDetails.");

    String workflowId = this.MANAGE_WORKFLOW_PREFIX + namespaceName;

    WorkflowStub untypedWFStub = client.newUntypedWorkflowStub("ManageNamespace",
        WorkflowOptions.newBuilder()
            .setWorkflowId(workflowId)
            .setTaskQueue(this.MANAGE_WORKFLOW_TASK_QUEUE)
            .build());

    CloudOperationsNamespace cloudOpsNS = untypedWFStub.query("getNamespaceDetails", CloudOperationsNamespace.class);
    if (cloudOpsNS == null)
      logger.debug("For some reason our query returned the namespace as null.");
    else
      logger.debug("The namespace returned from the workflow is [{}]", cloudOpsNS.toString());

    model.addAttribute("namespace", cloudOpsNS);

    return new String("/namespace-management-details" + namespaceName);
  }

  **/

  @PostMapping("/namespace-management-details")
  public String namespaceUpdate(@ModelAttribute(value = "namespace") CloudOperationsNamespace cloudOpsNamespace,
      Model model) {
    logger.debug("method Entry: namespaceUpdate");
    logger.debug(cloudOpsNamespace.toString());
    model.addAttribute("title", "Namespace Management");
   
    WorkflowStub wfStub = client.newUntypedWorkflowStub(this.MANAGE_WORKFLOW_PREFIX + cloudOpsNamespace.getName());
    wfStub.signal("setNamespace", cloudOpsNamespace);

    // Having signalled the workflow now get the latest version of the namespace from the workflow to display again.  (Cos display fields are not included in the form data.)        
    cloudOpsNamespace = wfStub.query("getNamespaceDetails", CloudOperationsNamespace.class);
    model.addAttribute("namespace", cloudOpsNamespace);
    model.addAttribute("page", 2);

    // TODO - Need to incorporate a field to show the page that is to be displayed next on screen.  
    // (Think add to the model/form(hidden) and use javascript to change value on each reload/track the page we are on.)
    FAIL_THE_COMPILER_HERE


    return "namespace-management-details";
  }

  @GetMapping("/namespace-management/{namespaceName}")
  public String getNamespace(
      @RequestParam(required = false, value = "apiKey") String apiKey,
      @RequestParam(required = false, value = "isNewNamespace") Boolean isNewNamespace,
      @PathVariable(required = false, value = "namespaceName") String namespaceName,
      Model model) {
    model.addAttribute("title", "Namespace Management");
    logger.debug("getNamespace method entry - namepace[{}]", namespaceName);

    // Setup variables and parameters used to start the workflow.
    WorkflowMetadata wfMetadata = new WorkflowMetadata();
    wfMetadata.setApiKey(apiKey);
    wfMetadata.setIsNewNamespace(isNewNamespace);
    CloudOperationsNamespace cloudOpsNS = new CloudOperationsNamespace();
    cloudOpsNS.setName(namespaceName);

    String workflowId = this.MANAGE_WORKFLOW_PREFIX + cloudOpsNS.getName();

    /**
     * Start a workflow to manage the state for the UI.
     */
    WorkflowStub untypedWFStub = client.newUntypedWorkflowStub("ManageNamespace",
        WorkflowOptions.newBuilder()
            .setWorkflowId(workflowId)
            .setTaskQueue(this.MANAGE_WORKFLOW_TASK_QUEUE)
            .build());

    // blocks until Workflow Execution has been started (not until it completes)
    try {
      untypedWFStub.start(wfMetadata, cloudOpsNS);

      boolean awaitPopulationOfNamespaceDetails = true;
      int counter = 0;
      while (awaitPopulationOfNamespaceDetails) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        counter++;
        WorkflowMetadata wfStatus = untypedWFStub.query("getWFMetadata", WorkflowMetadata.class);
        if (wfStatus == null) {
          logger.debug(
              "The query on the worflow metadata returned null.  Retrying in the hope that the workflow is still initialising itself.");
          if (counter > 100)
            break;
        } else if ((wfStatus.getNsDataGathered() != null) && (wfStatus.getNsDataGathered())) {
          logger.debug("Got the initial information on the namespace to show user.");
          awaitPopulationOfNamespaceDetails = false;
        }
      }
      cloudOpsNS = untypedWFStub.query("getNamespaceDetails", CloudOperationsNamespace.class);
      if (cloudOpsNS == null)
        logger.debug("For some reason our query returned the namespace as null.");
      else
        logger.debug("The namespace returned from the workflow is [{}]", cloudOpsNS.toString());

    } catch (io.temporal.client.WorkflowException e) {
      logger.debug("Cause of error is [{}]", e.getCause().getMessage());
      model.addAttribute("status", e.getCause().getMessage());
      if (e.getCause().getMessage().contains("ALREADY_EXISTS")) {
        cloudOpsNS = untypedWFStub.query("getNamespaceDetails", CloudOperationsNamespace.class);
      }
    }

    model.addAttribute("namespace", cloudOpsNS);
    model.addAttribute("workflowId", workflowId);
    model.addAttribute("page", 1);

    return new String("/namespace-management-details");
  } // End getNamespace (With namespace to manage)

  @GetMapping("namespace-management")
  public String getNamespaces(@RequestParam(required = false) String apiKey, Model model) {
    model.addAttribute("title", "Namespace Management");

    logger.debug("getNamespaces method entry");
    if (apiKey == null) // If no API key parameter then simply show page.
      return "namespace-management";

    if ((apiKey != null) && (apiKey.length() != 0)) {
      logger.debug("ApiKey to use is [{}] ", apiKey);
      // Retain the API key in the model for ease of user interaction.
      model.addAttribute("apiKey", apiKey);
      model.addAttribute("status", "OK");

      logger.debug(cloudOpsServerConfig.toString());
      System.out.println("Just About to make the remote call.");

      try {
        System.out.println("About to make the remote call.");
        List<CloudOperationsNamespace> namespaces = restClient.get()
            .uri(cloudOpsServerConfig.getBaseURI() + "/namespaces?apiKey=" + apiKey)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
              logger.info("Got an error back from operations.  Status[{}], Headers [{}]",
                  response.getStatusCode().toString(), response.getHeaders().toString());
              model.addAttribute("status", response.getStatusCode() + "-" + response.getHeaders().get("opsresponse"));
            })
            .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
              logger.info("Got an error back from operations.  Status[{}], Headers [{}]",
                  response.getStatusCode().toString(), response.getHeaders().toString());
              model.addAttribute("status", response.getStatusCode() + "-" + response.getHeaders().toString());
            })
            .body(List.class);

        model.addAttribute("namespaces", namespaces);

      } catch (HttpServerErrorException e) {
        logger.info("Error from the server [{}]", e.getMessage());
      } catch (HttpClientErrorException e) {
        logger.info("Error from cloud ops service [{}]", e.getMessage());
      } catch (Exception e) {
        logger.info("Exception from cloud ops service [{}]", e.getMessage());
      }

    } else
      model.addAttribute("status", "Please enter a valid API key to access Temporal Operations");

    return "namespace-management";

  }

}
