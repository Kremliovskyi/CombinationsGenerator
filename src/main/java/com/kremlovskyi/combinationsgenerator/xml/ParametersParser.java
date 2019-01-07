package com.kremlovskyi.combinationsgenerator.xml;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ParametersParser {

   private Document document;

   public ParametersParser(File parametersFile) {
      try {
         SAXBuilder saxBuilder = new SAXBuilder();
         document = saxBuilder.build(parametersFile);
      } catch (JDOMException | IOException e) {
         e.printStackTrace();
      }
   }

   List<Element> parse(String childName) {
      return document.getRootElement().getChildren(childName);
   }
}
