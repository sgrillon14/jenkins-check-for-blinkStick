package com.github.sgrillon14.jenkins.watch;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xml.sax.SAXException;

import com.github.sgrillon14.blinkstick.java.BlinkStick;
import com.github.sgrillon14.jenkins.watch.service.JenkinsService;

@SpringBootApplication
public class Application implements CommandLineRunner {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Autowired
    private JenkinsService jenkinsService;
    
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        BlinkStick blinkStick;
        do {
            try {
                blinkStick = BlinkStick.findFirst();
                jenkinsService.jenkinsProcessor(blinkStick);
                blinkStick.closeFirst();
            } catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException | InterruptedException e) {
                LOGGER.error("Exception", e);
            }
        } while (true);
    }
    
}
