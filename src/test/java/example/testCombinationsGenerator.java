package example;

import com.kremlovskyi.combinationsgenerator.CombinationsGenerator;
import com.kremlovskyi.combinationsgenerator.utils.CombinationsReader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

/**
 * Only for demonstration purposes. Not a test actually)
 */
public class testCombinationsGenerator {

   private CombinationsGenerator generator;

   @BeforeClass
   public void setUp() {
      generator = new CombinationsGenerator("combinations-ipog");
      generator.createFileWithCombinations();
   }

   @Test(dataProvider = "dataProvider")
   public void Name(String manufacturer, int ram, int screen, String processor, String SSD, boolean backlit) {
      System.out.println("Manufacturer: "+ manufacturer + ", RAM: "+ ram + ", Screen: " +
            screen + ", Processor: " + processor + ", SSD: " + SSD + ", Backlit Keyboard: " + backlit);
   }

   @DataProvider
   public Iterator<Object[]> dataProvider(){
      CombinationsReader reader = new CombinationsReader(generator);
      return reader.getAllRecordsIterator();
   }
}
