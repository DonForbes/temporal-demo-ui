package com.donald.demo.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@SpringBootApplication
public class TemporalDemoUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TemporalDemoUiApplication.class, args);
	}

}
