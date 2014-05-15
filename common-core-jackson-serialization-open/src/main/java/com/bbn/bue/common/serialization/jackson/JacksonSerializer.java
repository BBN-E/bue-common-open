package com.bbn.bue.common.serialization.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JacksonSerializer {
    private final ObjectMapper mapper;

    private JacksonSerializer(ObjectMapper mapper) {
        this.mapper = checkNotNull(mapper);
    }

    private static JacksonSerializer fromJSONFactory(JsonFactory jsonFactory) {
        final ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.findAndRegisterModules();
        return new JacksonSerializer(mapper);
    }

    public static JacksonSerializer forNormalJSON() {
        return fromJSONFactory(new JsonFactory());
    }

    private static JacksonSerializer normalJSONCached;
    public static JacksonSerializer forNormalJSONUncached() {
        if (normalJSONCached == null) {
            normalJSONCached = forNormalJSONUncached();
        }
        return normalJSONCached;
    }

    private static JacksonSerializer smileCached;
    public static JacksonSerializer forSmile() {
        if (smileCached == null) {
            smileCached = forSmileUncached();
        }
        return smileCached;
    }

    public static JacksonSerializer forSmileUncached() {
        return fromJSONFactory(new SmileFactory());
    }


    public void serializeTo(final Object o, final ByteSink out) throws IOException {
        final RootObject rootObj = RootObject.forObject(o);
        mapper.writeValue(out.openBufferedStream(), rootObj);
    }

    public Object deserializeFrom(final ByteSource source) throws IOException {
        final RootObject rootObj = mapper.readValue(source.openStream(), RootObject.class);
        return rootObj.object();
    }

    private static final class RootObject {
        @JsonCreator
        public RootObject(@JsonProperty("obj") final Object obj) {
            this.obj = checkNotNull(obj);
        }

        public static RootObject forObject(final Object obj) {
            return new RootObject(obj);
        }

        @JsonProperty("obj")
        public Object object() { return obj; }

        private final Object obj;
    }
}
