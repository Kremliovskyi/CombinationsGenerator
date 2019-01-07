package com.kremlovskyi.combinationsgenerator.utils;

import com.kremlovskyi.combinationsgenerator.CombinationsGenerator;
import edu.uta.cse.fireeye.common.Parameter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CombinationsReader {

    private static final Logger LOG = LogManager.getLogger(CombinationsReader.class);
    private CSVParser csvParser;
    private CombinationsGenerator generator;

    public CombinationsReader(CombinationsGenerator generator) {
        this.generator = generator;
        prepareCSVParser();
    }

    /**
     * @return {@link CSVParser} over the elements in the file with combinations.
     */
    public CSVParser getCsvParser() {
        return csvParser;
    }

    /**
     * @return {@link Iterator} with data for TestNG data provider.
     */
    public Iterator<Object[]> getAllRecordsIterator() {
        List<Object[]> result = new ArrayList<>();
        try {
            getCsvParser().getRecords().forEach(record -> {
                Iterator<String> iterator = record.iterator();
                List<Object> values = new ArrayList<>();
                while (iterator.hasNext()){
                    values.add(convertValue(iterator.next()));
                }
                result.add(values.toArray());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.iterator();
    }

    private void prepareCSVParser() {
        try {
            csvParser = new CSVParser(new FileReader(generator.getPathToOutPutFile()),
                  CSVFormat.DEFAULT
                        .withCommentMarker('#')
                        .withFirstRecordAsHeader());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private Object convertValue(String value) {
        Object result;
        switch (CombinationsGenerator.detectType(value)) {
            case Parameter.PARAM_TYPE_INT:
                result = Integer.valueOf(value);
                break;
            case Parameter.PARAM_TYPE_BOOL:
                result = Boolean.valueOf(value);
                break;
            default:
                result = value;
        }
        return result;
    }

}
