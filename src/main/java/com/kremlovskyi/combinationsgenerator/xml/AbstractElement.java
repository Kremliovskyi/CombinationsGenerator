package com.kremlovskyi.combinationsgenerator.xml;

import org.jdom2.Element;

import java.util.List;

public abstract class AbstractElement {

   public abstract String getElementName();
   public abstract List<String> getAttributesNames();
   public abstract String getValueName();

   private ParametersParser parametersParser;

   AbstractElement(ParametersParser parametersParser) {
      this.parametersParser = parametersParser;
   }

   public List<Element> parseElements() {
      return parametersParser.parse(getElementName());
   }
}
