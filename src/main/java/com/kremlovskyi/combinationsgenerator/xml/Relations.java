package com.kremlovskyi.combinationsgenerator.xml;

import java.util.Collections;
import java.util.List;

public class Relations extends AbstractElement {


   public Relations(ParametersParser parametersParser) {
      super(parametersParser);
   }

   @Override
   public String getElementName() {
      return "relations";
   }

   @Override
   public List<String> getAttributesNames() {
      return Collections.singletonList("strength");
   }

   @Override
   public String getValueName() {
      return "param";
   }
}
