package com.kremlovskyi.combinationsgenerator;

import com.kremlovskyi.combinationsgenerator.xml.*;
import edu.uta.cse.fireeye.common.*;
import edu.uta.cse.fireeye.service.engine.BaseChoice;
import edu.uta.cse.fireeye.service.engine.IpoEngine;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.kremlovskyi.combinationsgenerator.xml.Parameters.BASE_CHOICE_ATTRIBUTE_NAME;
import static com.kremlovskyi.combinationsgenerator.xml.Parameters.VALID_ATTRIBUTE_NAME;

public class CombinationsGenerator {
   private static final String OUTPUT_FILE_NAME_BASE = "%s-output.txt";
   private static final Logger LOG = LogManager.getLogger(CombinationsGenerator.class);
   private static final String SUT_NAME = "TCAS";
   private File outPutFile;
   private SUT sut = new SUT(SUT_NAME);
   private AbstractElement parser;
   private ParametersParser parametersParser;
   private List<Parameter> parameterList = new ArrayList<>();
   private AtomicBoolean isBaseChoice = new AtomicBoolean();
   private int positiveBaseChoiceValuesCount = 0;
   private ArrayList<Parameter> parametersWithInvalidValues = new ArrayList<>();

   public CombinationsGenerator(String parametersFilepath) {
      File parametersFile;
      URL url = getClass().getClassLoader().getResource(parametersFilepath);
      if (url == null) {
         throw new BuildError("File with rules is not found.");
      }
      parametersFile = new File(url.getPath());
      parametersParser = new ParametersParser(parametersFile);
      createOutPutFileName(parametersFile);
   }

   private void createOutPutFileName(File parametersFile) {
      outPutFile = new File(parametersFile.getParent(), String.format(OUTPUT_FILE_NAME_BASE,
            FilenameUtils.removeExtension(parametersFile.getName())));
   }

   /**
    * @return path to the file with combinations.
    */
   public File getOutPutFile() {
      return outPutFile;
   }

   /**
    * Create .csv file with combinations.
    * If all attributes "basicChoice" are true then Base-Choice algorithm is used,
    * other way IPOG algorithm is used.
    */
   public void createFileWithCombinations() {
      setParams();
      setRelation();
      setConstraints();
      // set the test generation profile
      // randomize don't care values
      TestGenProfile.instance().setRandstar(TestGenProfile.ON);
      // not ignoring constraints
      TestGenProfile.instance().setIgnoreConstraints(false);
      TestGenProfile.instance().setConstraintMode(TestGenProfile.ConstraintMode.solver);

      TestSet ts;
      String algorithmName;
      if (isBaseChoice.get()) {
         algorithmName = AlgorithmsNames.BASE_CHOICE.getName();
         ts = getTSForBaseChoice();
      } else {
         algorithmName = AlgorithmsNames.IPOG.getName();
         ts = getTSForIPOG();
      }
      LOG.info(algorithmName + " algorithm is used");
      // print out the test set
      TestSetWrapper wrapper = new TestSetWrapper(ts, sut);
      wrapper.outputInCSVFormat(outPutFile.getPath());
      LOG.info("File with combinations is created");
   }

   private TestSet getTSForIPOG() {
      TestSet ts;
      // Create an IPO engine object
      IpoEngine engine = new IpoEngine(sut);
      engine.build();
      ts = engine.getTestSet();
      return ts;
   }

   private TestSet getTSForBaseChoice() {
      TestSet ts;
      // Create a base choice object
      BaseChoice baseChoice = new BaseChoice(sut);
      ts = baseChoice.build();
      return ts;
   }

   private void setParams() {
      parser = new Parameters(parametersParser);
      List<Element> elements = parser.parseElements();
      elements.forEach(element -> {
               Parameter parameter = sut.addParam(element.getAttribute(parser.getAttributesNames().get(0)).getValue());
               LOG.info("Parameter is added: " + parameter.getName());
               List<Element> children = element.getChildren(parser.getValueName());
               processValuesType(children, parameter);
               processParameterValues(parameter, children);
               parameterList.add(parameter);
            }
      );
      verifyEachParameterHasBaseChoiceValue();
   }

   private void processParameterValues(Parameter parameter, List<Element> children) {
      ArrayList<String> positiveBaseChoiceValues = new ArrayList<>();
      children.forEach(childElement -> {
         String value = childElement.getValue();
         Attribute validAttribute = childElement.getAttribute(VALID_ATTRIBUTE_NAME);
         Attribute baseChoiceAttribute = childElement.getAttribute(BASE_CHOICE_ATTRIBUTE_NAME);
         if (baseChoiceAttribute != null && Boolean.valueOf(baseChoiceAttribute.getValue())) {
            positiveBaseChoiceValues.add(value);
            positiveBaseChoiceValuesCount++;
         }
         if (validAttribute != null && !Boolean.valueOf(validAttribute.getValue())) {
            parameter.addInvalidValue(value);
            LOG.info("Value of invalid parameter is: " + value);
         } else {
            parameter.addValue(value);
            LOG.info("Value of valid parameter is: " + value);
         }
      });
      if (!positiveBaseChoiceValues.isEmpty()) {
         isBaseChoice.compareAndSet(false, true);
         parameter.setBaseChoiceValues(new ArrayList<>(positiveBaseChoiceValues));
         positiveBaseChoiceValues.clear();
      }
      if (!parameter.getInvalidValues().isEmpty()) {
         parametersWithInvalidValues.add(parameter);
      }
   }

   private void verifyEachParameterHasBaseChoiceValue() {
      if (positiveBaseChoiceValuesCount > 0 && parameterList.size() != positiveBaseChoiceValuesCount) {
         throw new BuildError("Not all parameters has base choice values!");
      }
   }

   public ArrayList<Parameter> getParametersWithInvalidValues() {
      return parametersWithInvalidValues;
   }

   private void processValuesType(List<Element> children, Parameter parameter) {
      int firstValueType = detectType(children.get(0).getValue());
      if (!children.stream().allMatch(child -> detectType(child.getValue()) == firstValueType)) {
         throw new BuildError("Values of the single parameter should have the same type: " +
               "String, int or boolean");
      }
      parameter.setType(firstValueType);
   }

   private void setRelation() {
      if (!isBaseChoice.get()) {
         parser = new Relations(parametersParser);
         List<Element> elements = parser.parseElements();
         AtomicInteger defaultStrength = new AtomicInteger(1);
         elements.forEach(element -> {
            int strength = Integer.valueOf(element.getAttribute(parser.getAttributesNames().get(0)).getValue());
            int parametersCount = sut.getParameters().size();
            checkThatStrengthIsNotTooHigh(strength, parametersCount);
            List<Element> children = element.getChildren(parser.getValueName());
            checkThatStrengthIsNotTooHigh(strength, children.size());
            Relation relation = new Relation(strength);
            if (children.isEmpty()) {
               LOG.info("Default strength is used: " + strength);
               defaultStrength.set(strength);
            }
            children.forEach(childElement -> {
               try {
                  relation.addParam(parameterList.stream().filter(p -> p.getName().equals(childElement.getValue()))
                        .findFirst().orElseThrow((Supplier<Throwable>) () -> new Exception("No such parameter: "
                              + childElement.getValue() + " was found for Relation")));
               } catch (Throwable throwable) {
                  throwable.printStackTrace();
               }
            });
            // add this relation into the sut
            sut.addRelation(relation);
            LOG.info(relation.toString());
         });
         // add the default relation
         sut.addDefaultRelation(defaultStrength.get());
      }
   }

   private void checkThatStrengthIsNotTooHigh(int strength, int parametersCount) {
      if (parametersCount != 0 && parametersCount < strength) {
         throw new BuildError("Build strength " + strength + " is higher then the number of parameters to combine " +
               parametersCount + ". \n Please choose a smaller strength to build.");
      }
   }

   private void setConstraints() {
      parser = new Constraints(parametersParser);
      ArrayList<Parameter> params = new ArrayList<>();
      List<Element> elements = parser.parseElements();
      elements.forEach(element -> {
         String constraintText = element.getChildren(parser.getValueName()).get(0).getValue();
         parser.getAttributesNames().forEach(name -> {
            try {
               params.add(parameterList.stream().filter(p -> p.getName().equals(element.getAttribute(name).getValue()))
                     .findFirst().orElseThrow((Supplier<Throwable>) () -> new Exception("No such parameter: "
                           + name + " was found for Constraints")));
            } catch (Throwable throwable) {
               throwable.printStackTrace();
            }
         });
         Constraint constraint = new Constraint(constraintText, new ArrayList<>(params));
         params.clear();
         sut.addConstraint(constraint);
         LOG.info(constraint.toString());
      });
   }

   public static int detectType(String s) {
      int result = Parameter.PARAM_TYPE_ENUM;
      Scanner scanner = new Scanner(s);
      if (scanner.hasNextBigInteger()) {
         result = Parameter.PARAM_TYPE_INT;
      } else if (scanner.hasNextBoolean()) {
         result = Parameter.PARAM_TYPE_BOOL;
      }
      return result;
   }
}
