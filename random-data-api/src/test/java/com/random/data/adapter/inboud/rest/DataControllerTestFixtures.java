package com.random.data.adapter.inboud.rest;

import java.util.List;

public class DataControllerTestFixtures {

    private DataControllerTestFixtures() {}

    public static List<String> sampleRecords() {
        return List.of("alice", "bob");
    }

    public static String sampleJson() {
        return "[\"alice\",\"bob\"]";
    }

    public static String sampleXml() {
        return """
               <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
               <wrapper>
                 <item>alice</item>
                 <item>bob</item>
               </wrapper>
               """;
    }

}
