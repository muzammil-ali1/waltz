/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.catalog;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.catalog.ImmutableParseAnalysis;
import com.khartec.waltz.model.catalog.ParseAnalysis;
import org.junit.Test;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


/**
 * Created by dwatkins on 04/12/2015.
 */
public class TabularFileAnalyserTest {

    @Test
    public void foo() throws IOException {

        char[] delimeters = new char[]{',', '|', '\t', ';', '!'};
        char[] quoteChars = new char[]{'"', '\''};


        List<ParseAnalysis> analysisResults = ListUtilities.newArrayList();


        for (char quoteChar : quoteChars) {
            for (char delimeter : delimeters) {

                InputStreamReader simpleReader = getReader();

                CsvPreference prefs = new CsvPreference.Builder(quoteChar, delimeter, "\n")
                        .ignoreEmptyLines(false)
                        .build();

                CsvListReader csvReader = new CsvListReader(simpleReader, prefs);

                List<String> cells = csvReader.read();

                ImmutableParseAnalysis.Builder parseAnalysisBuilder = ImmutableParseAnalysis.builder()
                        .quoteChar(quoteChar)
                        .delimiterChar(delimeter);


                while (cells != null) {
                    parseAnalysisBuilder.addFieldCounts(cells.size());
                    cells = csvReader.read();
                }

                ParseAnalysis parseAnalysis = parseAnalysisBuilder.build();
                analysisResults.add(parseAnalysis);

            }
        }

        analysisResults
                .forEach(r -> {
                    System.out.println(r.quoteChar()
                            + " "
                            + r.delimiterChar()
                            + " => [ "
                            + r.fieldCounts().size()
                            + " ] "
                            + r.fieldCounts());
                });
    }


    private InputStreamReader getReader() {
        InputStream simpleStream = TabularFileAnalyser.class.getClassLoader().getResourceAsStream("simple.csv");
        return new InputStreamReader(simpleStream);
    }
}
