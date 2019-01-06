package com.kremlovskyi.combinationsgenerator.xml;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ParametersParser {

   private static final String FILE_WITH_PARAMETERS_BASE = "./src/main/resources/%s.xml";
   private Document document;

   public ParametersParser(String parametersFileName) {
      try {
         SAXBuilder saxBuilder = new SAXBuilder();
         File inputFile = new File(createParametersFilePath(parametersFileName));
         document = saxBuilder.build(inputFile);
      } catch (JDOMException | IOException e) {
         e.printStackTrace();
      }
   }

   List<Element> parse(String childName) {
      return document.getRootElement().getChildren(childName);
   }

   private String createParametersFilePath(String combinationsFileName) {
      return String.format(FILE_WITH_PARAMETERS_BASE, combinationsFileName);
   }
}
