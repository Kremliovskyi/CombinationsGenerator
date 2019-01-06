package com.kremlovskyi.combinationsgenerator;

public enum AlgorithmsNames {

   IPOG("IPOG"), BASE_CHOICE("Base-Choice");

   private String name;

   AlgorithmsNames(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }
}
