package com.donald.demo.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;

import com.donald.demo.ui.model.Demo;
import com.donald.demo.ui.model.Demonstration;



@Controller
public class HomeController {
    @Autowired
    private Demonstration theDemos;

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
    
}

