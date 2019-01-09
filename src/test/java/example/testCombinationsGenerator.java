package example;

import com.kremlovskyi.combinationsgenerator.CombinationsGenerator;
import com.kremlovskyi.combinationsgenerator.utils.CombinationsReader;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

/**
 * Only for demonstration purposes. Not a test actually)
 */
public class testCombinationsGenerator {

   @Test(dataProvider = "dataProvider")
   public void Name(String manufacturer, int ram, int screen, String processor, String SSD, boolean backlit) {
      System.out.println("Manufacturer: "+ manufacturer + ", RAM: "+ ram + ", Screen: " +
            screen + ", Processor: " + processor + ", SSD: " + SSD + ", Backlit Keyboard: " + backlit);
   }

   @DataProvider
   public Iterator<Object[]> dataProvider(){
      CombinationsGenerator generator = new CombinationsGenerator("combinations/combinations-ipog.xml");
      generator.createFileWithCombinations();
      CombinationsReader reader = new CombinationsReader(generator);
      return reader.getAllRecordsIterator();
   }
}
