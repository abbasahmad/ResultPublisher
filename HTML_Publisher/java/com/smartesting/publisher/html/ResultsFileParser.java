package com.smartesting.publisher.html;


import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ResultsFileParser {
	
	private final static Logger LOGGER = Logger.getLogger(ResultsFileParser.class.getName());
   
	/**
	 * Prints the between tags.
	 *
	 * @param inputFilePath the input file path
	 * @param testCaseTagName the test case tag name
	 * @param testStepTagName the test step tag name
	 * @param out the out
	 * @param testCaseAttributeName the test case attribute name
	 * @param testStepAttributeName the test step attribute name
	 * @param testCaseAttributeValue the test case attribute value
	 * @param testStepAttributeValue the test step attribute value
	 * @return the string
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String printBetweenTags(String inputFilePath, String testCaseTagName, String testStepTagName,
		String out, String testCaseAttributeName, String testStepAttributeName,
		String testCaseAttributeValue, String testStepAttributeValue)
				throws ParserConfigurationException, SAXException, IOException{
		LOGGER.log(Level.INFO, "Print between Tags method is being executed");
		File inputFile = new File(inputFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        /** Gets all test cases*/
        NodeList testCases = doc.getElementsByTagName(testCaseTagName);
        
        for (int i = 0; i < testCases.getLength(); i++) {
        	Element testCaseElements = (Element) testCases.item(i);
        	Element testStepElements = null;
        	/** For each test case, get the test case having as Id the testCaseAttributeValue*/
        	if(testCaseElements.getAttribute(testCaseAttributeName).contains(testCaseAttributeValue)){
        		NodeList testSteps = testCaseElements.getElementsByTagName(testStepTagName);
        		for (int stepIndex = 0; stepIndex < testSteps.getLength(); stepIndex++) {
        			 testStepElements = (Element) testSteps.item(stepIndex);
        			 /** For each test step, get the test case having as name the testStepAttributeValue*/
        			 if(testStepElements.getAttribute(testStepAttributeName).contains(testStepAttributeValue)){
        				/**Returns the result requested (something between a specified tag)*/
        				return testStepElements.getElementsByTagName(out).item(0).getTextContent();
        			}
        		}
        		
        		
        	}
        }
		return "";
	
}
		

}

