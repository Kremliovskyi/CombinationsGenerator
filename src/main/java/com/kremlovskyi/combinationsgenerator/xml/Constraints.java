package com.kremlovskyi.combinationsgenerator.xml;

import java.util.Arrays;
import java.util.List;

public class Constraints extends AbstractElement {

   public Constraints(ParametersParser parametersParser) {
      super(parametersParser);
   }

   @Override
   public String getElementName() {
      return "constraints";
   }

   @Override
   public List<String> getAttributesNames() {
      return Arrays.asList("param1", "param2");
   }

   @Override
   public String getValueName() {
      return "text";
   }

}
