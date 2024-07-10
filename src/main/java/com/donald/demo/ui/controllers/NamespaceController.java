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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.donald.demo.ui.model.operations.CloudOpsConfig;
import com.donald.demo.ui.model.operations.CloudOpsServerConfig;
import com.donald.demo.ui.model.operations.CloudOperationsNamespace;

@Controller
public class NamespaceController {
  @Autowired
  CloudOpsServerConfig cloudOpsServerConfig;
  private static final Logger logger = LoggerFactory.getLogger(NamespaceController.class);
  private RestClient restClient = RestClient.create();

  @GetMapping("/namespace-management-details/{namepaceName}")
  public String getNamespaceDetails(@RequestParam(required = false, value = "apiKey") String apiKey, 
                                    @PathVariable(required = false, value = "namespaceName") String namespaceName,
                                    Model model) {
    model.addAttribute("title", "Namespace Management");
    logger.debug("getNamespaceDetails method entry - namepace[{}]", namespaceName);

    CloudOperationsNamespace cloudOpsNS = new CloudOperationsNamespace();
    cloudOpsNS.setName("Hello");
    cloudOpsNS.setState("Active");
    cloudOpsNS.setRetentionPeriod(10);
    cloudOpsNS.setActiveRegion("Active-region");
    cloudOpsNS.setCertAuthorityPublicCert("BLa BLa");

    model.addAttribute("namespace", cloudOpsNS);
    
    return new String("namespace-management-details");
  }

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
            .onStatus( HttpStatusCode::is4xxClientError, (request,response) -> {
              logger.info("Got an error back from operations.  Status[{}], Headers [{}]", response.getStatusCode().toString(), response.getHeaders().toString() );
              model.addAttribute("status", response.getStatusCode() + "-" + response.getHeaders().get("opsresponse"));
            })
            .onStatus( HttpStatusCode::is5xxServerError, (request,response) -> {
              logger.info("Got an error back from operations.  Status[{}], Headers [{}]", response.getStatusCode().toString(), response.getHeaders().toString() );
              model.addAttribute("status", response.getStatusCode() + "-" + response.getHeaders().toString());
            })
            .body(List.class);

        model.addAttribute("namespaces", namespaces);

      } catch (HttpServerErrorException e) {
        logger.info("Error from the server [{}]", e.getMessage());
      } 
      catch (HttpClientErrorException e) {
        logger.info("Error from cloud ops service [{}]", e.getMessage());
      }
      catch (Exception e) {
        logger.info("Exception from cloud ops service [{}]", e.getMessage());
      }

    }
    else
      model.addAttribute("status", "Please enter a valid API key to access Temporal Operations");

    return "namespace-management";

  }

}
