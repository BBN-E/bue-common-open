package com.bbn.bue.common.serialization.jackson;

import com.bbn.bue.common.annotations.MoveToBUECommon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convenience object to build Jackson serializers the way we usually use them.
 *
 * If you want to use Guice-bound values during deserialization,
 * see {@code JacksonSerializationM}.
 */
public final class JacksonSerializer {

  private final ObjectMapper mapper;

  private JacksonSerializer(ObjectMapper mapper) {
    this.mapper = checkNotNull(mapper);
  }

  public static JacksonSerializer forNormalJSON() {
    return builder().build();
  }

  /**
   * @deprecated  Prefer {@link #builder()}.
   */
  @Deprecated
  public static Builder json() {
    return new Builder();
  }

  /**
   * @deprecated Prefer calling {@link Builder#prettyOutput()}.
   */
  @Deprecated
  public static JacksonSerializer forPrettyJSON() {
    return json().prettyOutput().build();
  }

  /**
   * @deprecated Caching is diabled, so this is no longer necessary
   */
  @Deprecated
  public static JacksonSerializer forNormalJSONUncached() {
    return forNormalJSON();
  }

  public static JacksonSerializer forSmile() {
    return builder().forSmile().build();
  }

  /**
   * @deprecated Prefer to call {@link Builder#forSmile()} on a builder.
   */
  @Deprecated
  public static Builder smile() {
    return new Builder().forSmile();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * @deprecated Caching is disabled, so this is no longer necessary
   * @return
   */
  @Deprecated
  public static JacksonSerializer forSmileUncached() {
    return forSmile();
  }


  public void serializeTo(final Object o, final ByteSink out) throws IOException {
    final RootObject rootObj = RootObject.forObject(o);
    final OutputStream bufStream = out.openBufferedStream();
    mapper.writeValue(bufStream, rootObj);
    bufStream.close();
  }

  public Object deserializeFrom(final ByteSource source) throws IOException {
    final InputStream srcStream = source.openStream();
    final RootObject rootObj;
    try {
      rootObj = mapper.readValue(srcStream, RootObject.class);
    } catch (Exception e) {
      throw new IOException("While deserializing from " + source + ", encountered exception:", e);
    }
    srcStream.close();
    return rootObj.object();
  }

  public String writeValueAsString(Object value) throws JsonProcessingException {
    return mapper.writeValueAsString(value);
  }

  public <T> T deserializeFromString(String content, Class<T> valueType) throws IOException {
    return mapper.readValue(content, valueType);
  }

  /**
   * Builder for {@link JacksonSerializer}s.  Every method on this which returns a {@code Builder}
   * returns a new copy.  This allows use to inject builders which already have Guice bindings done
   * and which can be further customized by users.
   *
   * The Builder defaults to using plain, non-pretty-printed JSON output with no injected values
   * and with properties for storing type information.
   */
  public static final class Builder {
    private JsonFactory jsonFactory = new JsonFactory();
    private boolean usePropertyForTypeInformation = true;
    private boolean usePrettyOutput = false;
    // paired lists of annotation inspectors and injectable values needed for deserializing
    // using values provided by a dependency-injection framework
    private AnnotationIntrospector annotationIntrospector = null;
    private InjectableValues injectableValues = null;
    // we order modules by name for determinism
    private ImmutableSet.Builder<Module> modules = ImmutableSortedSet.<Module>orderedBy(
        Ordering.natural().onResultOf(ModuleNameFunction.INSTANCE));

    private Builder copy() {
      final Builder ret = new Builder();
      ret.jsonFactory = jsonFactory;
      ret.usePropertyForTypeInformation = usePropertyForTypeInformation;
      ret.usePrettyOutput = usePrettyOutput;
      ret.annotationIntrospector = annotationIntrospector;
      ret.injectableValues = injectableValues;
      return ret;
    }

    public Builder withJSONFactory(JsonFactory jsonFactory) {
      final Builder ret = copy();
      ret.jsonFactory = checkNotNull(jsonFactory);
      return ret;
    }

    public Builder forJson() {
      return withJSONFactory(new JsonFactory());
    }

    public Builder forSmile() {
      return withJSONFactory(new SmileFactory());
    }

    public Builder prettyOutput() {
      final Builder ret = copy();
      ret.usePrettyOutput = true;
      return ret;
    }

    /**
     * Specifies to use arrays rather than properties to encode type information. You will need to
     * enable this to read any serialized objects from before 28 Oct 2015 / bue-common-open 2.23.2 .  The newer default method
     * of using JSON properties to encode type information avoids certain bad interactions of
     * Jackson and generics.
     */
    public Builder useArraysToEncodeTypeInformation() {
      final Builder ret = copy();
      ret.usePropertyForTypeInformation = false;
      return ret;
    }

    /**
     * This exists only for use by {@link JacksonSerializationM}
     */
    /* package-private */ Builder withInjectionBindings(AnnotationIntrospector annotationIntrospector,
        InjectableValues injectableValues) {
      final Builder ret = copy();
      ret.annotationIntrospector = checkNotNull(annotationIntrospector);
      ret.injectableValues = checkNotNull(injectableValues);
      return ret;
    }

    public Builder registerModule(final Module jacksonModule) {
      modules.add(jacksonModule);
      return this;
    }

    public JacksonSerializer build() {
      final ObjectMapper objectMapper = mapperFromJSONFactory(jsonFactory);

      if (usePropertyForTypeInformation) {
        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      } else {
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
      }

      if (usePrettyOutput) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      }

      if (injectableValues != null) {
        // this is from jackson-module-guice's ObjectMapperModule.get()
        // we do this in a separate method because we do not currently want to inject the ObjectMapper
        objectMapper.setInjectableValues(injectableValues);
        objectMapper.setAnnotationIntrospectors(
            new AnnotationIntrospectorPair(
                annotationIntrospector,
                objectMapper.getSerializationConfig().getAnnotationIntrospector()
            ),
            new AnnotationIntrospectorPair(
                annotationIntrospector,
                objectMapper.getDeserializationConfig().getAnnotationIntrospector()
            )
        );
      }

      return new JacksonSerializer(objectMapper);
    }

    private ObjectMapper mapperFromJSONFactory(JsonFactory jsonFactory) {
      final ObjectMapper mapper = new ObjectMapper(jsonFactory);
      mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      mapper.findAndRegisterModules();
      // modules are ordered by name for determinism, see field declaration
      for (final Module module : modules.build()) {
        mapper.registerModule(module);
      }
      return mapper;
    }
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
    public Object object() {
      return obj;
    }

    private final Object obj;
  }
}

@MoveToBUECommon
enum ModuleNameFunction implements Function<Module, String> {
  INSTANCE;

  @Override
  public String apply(final Module input) {
    return input.getModuleName();
  }
}

