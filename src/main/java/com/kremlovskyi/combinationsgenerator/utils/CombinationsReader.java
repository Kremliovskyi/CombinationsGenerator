package com.kremlovskyi.combinationsgenerator.utils;

import com.kremlovskyi.combinationsgenerator.CombinationsGenerator;
import edu.uta.cse.fireeye.common.Parameter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CombinationsReader {

    private static final Logger LOG = LogManager.getLogger(CombinationsReader.class);
    private static final int IS_VALID = 1;
    private static final int IS_INVALID = 2;
    private CSVParser csvParser;
    private CombinationsGenerator generator;

    public CombinationsReader(CombinationsGenerator generator) {
        this.generator = generator;
        prepareCSVParser();
    }

    /**
     * @return {@link CSVParser} over the records in the file with combinations.
     */
    public CSVParser getCsvParser() {
        return csvParser;
    }

    /**
     * @return {@link Iterator} with all combinations for TestNG data provider.
     */
    public Iterator<Object[]> getAllRecordsIterator() {
        List<Object[]> result = new ArrayList<>();
        try {
            getCsvParser().getRecords().forEach(record -> parseRecord(result, record));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.iterator();
    }

    /**
     * @return {@link Iterator} with negative combinations for TestNG data provider.
     */
    public Iterator<Object[]> getNegativeRecordsIterator() {
        return getPositiveOrNegativeIterator(IS_INVALID);
    }

    /**
     * @return {@link Iterator} with positive combinations for TestNG data provider.
     */
    public Iterator<Object[]> getPositiveRecordsIterator() {
        return getPositiveOrNegativeIterator(IS_VALID);
    }

    /**
     * @param flag one of the constants {@link CombinationsReader#IS_INVALID}
     *                   or {@link CombinationsReader#IS_VALID}
     * @return negative or positive iterator respectively to the passed value.
     */
    private Iterator<Object[]> getPositiveOrNegativeIterator(int flag) {
        List<Object[]> result = new ArrayList<>();
        List<Parameter> invalidValues = generator.getParametersWithInvalidValues();
        try {
            getCsvParser().getRecords().forEach(record -> {
                int invalidValueInside = isInvalidValueInside(invalidValues, record);
                if ((invalidValueInside & flag) > 0) {
                    parseRecord(result, record);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.iterator();
    }

    private void parseRecord(List<Object[]> result, CSVRecord record) {
        Iterator<String> iterator = record.iterator();
        List<Object> values = new ArrayList<>();
        while (iterator.hasNext()){
            values.add(convertValue(iterator.next()));
        }
        result.add(values.toArray());
    }

    private int isInvalidValueInside(List<Parameter> invalidValues, CSVRecord record) {
        return record.toMap().entrySet().stream().anyMatch(e -> invalidValues.stream().anyMatch(parameter -> {
            if (parameter.getName().equals(e.getKey())) {
                return parameter.getInvalidValues().contains(e.getValue());
            }
            return false;
        })) ? IS_INVALID : IS_VALID;
    }

    private void prepareCSVParser() {
        try {
            csvParser = new CSVParser(new FileReader(generator.getOutPutFile()),
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
