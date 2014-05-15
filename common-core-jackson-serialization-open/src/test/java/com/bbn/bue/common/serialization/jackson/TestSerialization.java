package com.bbn.bue.common.serialization.jackson;

import com.bbn.bue.common.evaluation.FMeasureCounts;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestSerialization extends TestCase {
    private ObjectMapper mapper;

    @Override
    protected void setUp() {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Test
    public void testFMeasureCounts() throws IOException {
        final RootObject foo = new RootObject();
        foo.o = ImmutableMap.of("Hello",
                FMeasureCounts.from(1, 2, 3));
        final File tmp = File.createTempFile("foo", "bar");
        tmp.deleteOnExit();
        mapper.writeValue(tmp, foo);
        final Map<String, FMeasureCounts> bar = (Map<String,FMeasureCounts>)mapper.readValue(tmp,
                RootObject.class).o;
        assertEquals(foo.o, bar);
    }

    public static class RootObject {

        public RootObject() {
            o = null;
        }

        @JsonCreator
        public RootObject(@JsonProperty("obj") Object o) {
            this.o = o;
        }

        @JsonProperty("obj")
        public Object obj() {
            return o;
        }

        public Object o;
    }
}