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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.donald.demo.ui.model.namespace.CloudOpsConfig;
import com.donald.demo.ui.model.namespace.CloudOpsServerConfig;
import com.donald.demo.ui.model.namespace.Namespace;


@Controller
public class NamespaceController {
    @Autowired
    CloudOpsServerConfig cloudOpsServerConfig;
    private static final Logger logger = LoggerFactory.getLogger(NamespaceController.class);
    private RestClient restClient = RestClient.create();


    @GetMapping("namespace-management-details")
    public String getNamespaceDetails(@RequestParam(required=false, value = "namespace-name") String namespaceName, Model model) {
        model.addAttribute("title", "Namespace Management");
        logger.debug("getNamespaceDetails method entry - namepace[{}]", namespaceName);
        return new String("namespace-management-details");
    }
    

    @GetMapping("namespace-management")
    public String getNamespaces(@RequestParam (required = false) String apiKey, Model model) {
        model.addAttribute("title", "Namespace Management");

        logger.debug("getNamespaces method entry");

        if ((apiKey != null) && (apiKey.length() != 0)) {
          logger.debug("ApiKey to use is [{}] ", apiKey);
          // Retain the API key in the model for ease of user interaction.
          model.addAttribute("apiKey", apiKey);
         
          logger.debug(cloudOpsServerConfig.toString());
          List<Namespace> namespaces;

          try {
             namespaces =  restClient.get()
            .uri(cloudOpsServerConfig.getBaseURI()  + "/get-namespaces?apiKey=" + apiKey)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve() 
            .body(List.class);

            model.addAttribute("namespaces", namespaces);

          } catch (HttpClientErrorException e) {
            logger.debug("Error from cloud ops service [{}]", e.getMessage());
            model.addAttribute("status", "Error, please check the API key is valid - " + e.getMessage());
          }

        } 
    
        return "namespace-management";
        
    }
    
}
