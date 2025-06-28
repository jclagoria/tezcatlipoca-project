package com.random.data.adapter.outbound.serialiazer;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class TestFixtures {
    /** “Happy-path” simple strings. */
    public static List<String> simpleCVSStrings() {
        return List.of("alice", "bob", "carol");
    }

    /** Strings requiring CSV escaping per RFC 4180. */
    public static List<String> specialCVSStrings() {
        return List.of(
                "has,comma",
                "has\"quote",
                "has\nnewline",
                "has\rcarriage"
        );
    }

    /** A record whose toString() always throws, to test error propagation. */
    public static Object badCVSRecord() {
        return new Object() {
            @Override
            public String toString() {
                throw new IllegalStateException("oops!");
            }
        };
    }

    /** A simple list of strings. */
    public static List<String> stringJSONList() {
        return List.of("foo", "bar");
    }

    /** A LinkedHashMap so iteration order is guaranteed. */
    public static List<LinkedHashMap<String, Object>> mapJSONList() {
        var map = new LinkedHashMap<String, Object>();
        map.put("k1", "v1");
        map.put("k2", 42);
        return List.of(map);
    }

    /** An “empty” placeholder (not strictly needed here, but shows you can centralize). */
    public static List<?> emptyList() {
        return Collections.emptyList();
    }

    @XmlRootElement(name = "item")
    public static class SimpleItem {
        private String name;

        // Required by JAXB
        public SimpleItem() { }

        public SimpleItem(String name) {
            this.name = name;
        }

        @XmlElement
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Returns a LinkedList of two SimpleItem instances: "alpha" and "beta".
     */
    public static LinkedList<SimpleItem> simpleXMLItemList() {
        LinkedList<SimpleItem> list = new LinkedList<>();
        list.add(new SimpleItem("alpha"));
        list.add(new SimpleItem("beta"));
        return list;
    }

    /**
     * Returns an empty LinkedList of SimpleItem.
     */
    public static LinkedList<SimpleItem> emptyXMLSimpleItemList() {
        return new LinkedList<>();
    }

}
