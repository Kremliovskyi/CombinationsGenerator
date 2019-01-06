package com.kremlovskyi.combinationsgenerator.xml;

import java.util.Collections;
import java.util.List;

public class Parameters extends AbstractElement {

   public static final String VALID_ATTRIBUTE_NAME = "valid";
   public static final String BASE_CHOICE_ATTRIBUTE_NAME = "baseChoice";

   public Parameters(ParametersParser parametersParser) {
      super(parametersParser);
   }

   @Override
   public String getElementName() {
      return "parameter";
   }

   @Override
   public List<String> getAttributesNames() {
      return Collections.singletonList("name");
   }

   @Override
   public String getValueName() {
      return "value";
   }
}
