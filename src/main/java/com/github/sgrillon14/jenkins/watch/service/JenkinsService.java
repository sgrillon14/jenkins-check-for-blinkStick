package com.github.sgrillon14.jenkins.watch.service;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.sgrillon14.blinkstick.java.BlinkStick;
import com.github.sgrillon14.jenkins.watch.config.JenkinsConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class JenkinsService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsService.class);
    
    @Autowired
    private JenkinsConfig jenkinsConfig;
    
    public void jenkinsProcessor(BlinkStick blinkStick) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(jenkinsConfig.getUri() + ":" + jenkinsConfig.getPort() +"/jenkins/rssLatest").build();
        Response response = client.newCall(request).execute();
        String xml = response.body().string();
        LOGGER.debug(xml);
        InputSource inputXML = new InputSource(new StringReader(xml));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(inputXML);

        XPath xPath = XPathFactory.newInstance().newXPath();
        String result = xPath.evaluate("/feed/entry[1]/title", document);

        LOGGER.debug(result);
        String status = result.substring(result.indexOf("(") + 1, result.lastIndexOf(")"));
        System.out.println(status);
        if ("?".equals(status)) {
            if (blinkStick == null) {
                LOGGER.info("BlinkStick Not found...");
                LOGGER.info("Test en cours...");
            } else {
                blinkStick.setColor("blue");
            }
        } else if (status.contains("fail") || status.contains("broken")) {
            if (blinkStick == null) {
                LOGGER.info("BlinkStick Not found...");
                LOGGER.info("Tests fails");
            } else {
                blinkStick.setColor("red");
            }
        } else if ("back to normal".equals(status) || "stable".equals(status)) {
            if (blinkStick == null) {
                LOGGER.info("BlinkStick Not found...");
                LOGGER.info("Tests OK");
            } else {
                blinkStick.setColor("green");
            }
        } else {
            blinkStick.setColor("indigo");
        }
        response.body().close();
        Thread.sleep(60000);
    }
    
}
