package com.bbn.bue.common.parameters;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.converters.StrictStringToBoolean;
import com.bbn.bue.common.converters.StringConverter;
import com.bbn.bue.common.converters.StringToDouble;
import com.bbn.bue.common.converters.StringToEnum;
import com.bbn.bue.common.converters.StringToFile;
import com.bbn.bue.common.converters.StringToInteger;
import com.bbn.bue.common.converters.StringToOSFile;
import com.bbn.bue.common.converters.StringToStringList;
import com.bbn.bue.common.converters.StringToStringSet;
import com.bbn.bue.common.converters.StringToSymbolList;
import com.bbn.bue.common.converters.StringToSymbolSet;
import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.exceptions.InvalidEnumeratedPropertyException;
import com.bbn.bue.common.parameters.exceptions.MissingRequiredParameter;
import com.bbn.bue.common.parameters.exceptions.ParameterConversionException;
import com.bbn.bue.common.parameters.exceptions.ParameterException;
import com.bbn.bue.common.parameters.exceptions.ParameterValidationException;
import com.bbn.bue.common.parameters.serifstyle.SerifStyleParameterFileLoader;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;
import com.bbn.bue.common.validators.AlwaysValid;
import com.bbn.bue.common.validators.And;
import com.bbn.bue.common.validators.FileExists;
import com.bbn.bue.common.validators.IsDirectory;
import com.bbn.bue.common.validators.IsFile;
import com.bbn.bue.common.validators.IsInRange;
import com.bbn.bue.common.validators.IsNonNegative;
import com.bbn.bue.common.validators.IsPositive;
import com.bbn.bue.common.validators.ValidationException;
import com.bbn.bue.common.validators.Validator;

import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;


/**
 * Represents a set of parameters passed into a program.  The parameters are assumed to originate as
 * key-value pairs of <code>String</code>s, which can then be accessed in various validated ways.
 * This class is immutable. Keys will never be null or empty. Values will never be null.
 *
 * For all methods to get parameters, looking up a missing parameter throws an unchecked {@link
 * MissingRequiredParameter} exception.
 *
 * @author rgabbard
 * @author clignos
 */
@Beta
public final class Parameters {

  public static final String DO_OS_CONVERSION_PARAM = "os_filepath_conversion";

  private static final String DELIM = ".";
  private static final Joiner JOINER = Joiner.on(DELIM);
  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

  /**
   * Constructs a Parameters object from a <code>Map</code>.  The Map may contain neither null keys,
   * empty keys, or null values.
   *
   * @deprecated Prefer fromMap()
   */
  @Deprecated
  public Parameters(final Map<String, String> params) {
    this(params, ImmutableList.<String>of());
  }

  private Parameters(final Map<String, String> params, final List<String> namespace) {
    this.namespace = ImmutableList.copyOf(namespace);
    this.params = ImmutableMap.copyOf(params);
    for (final Map.Entry<String, String> param : params.entrySet()) {
      checkNotNull(param.getKey());
      checkNotNull(param.getValue());
      checkArgument(!param.getKey().isEmpty());
    }
  }

  /**
   * Creates a new set of parameters with only those parameters in the specified namespace (that is,
   * prefixed by "namespace.". The namespace prefix and period will be removed from parameter names
   * in the new {@code Parameters}.  The name space name should *not* have a
   * trailing ".".
   */
  public Parameters copyNamespace(final String requestedNamespace) {
    checkArgument(!requestedNamespace.isEmpty());
    checkArgument(!requestedNamespace.endsWith(DELIM));
    final ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();
    final String dottedNamespace = requestedNamespace + DELIM;
    for (final Map.Entry<String, String> param : params.entrySet()) {
      if (param.getKey().startsWith(dottedNamespace)) {
        ret.put(param.getKey().substring(dottedNamespace.length()), param.getValue());
      }
    }
    final List<String> newNamespace = Lists.newArrayList();
    newNamespace.addAll(namespace);
    newNamespace.add(requestedNamespace);
    final Parameters paramsRet = new Parameters(ret.build(), newNamespace);
    // our children inherit our listeners
    for (final Listener listener : listeners) {
      paramsRet.registerListener(listener);
    }
    return paramsRet;
  }

  /**
   * If the specified namespace is present, return a copy of that namespace as a parameter set.
   * Otherwise, return a copy of this parameter set. The name space name should *not* have a
   * trailing ".".
   */
  public Parameters copyNamespaceIfPresent(final String requestedNamespace) {
    // checkArgument ensures namespaces are specified consistently
    checkArgument(!requestedNamespace.isEmpty());
    checkArgument(!requestedNamespace.endsWith(DELIM));
    if (isNamespacePresent(requestedNamespace)) {
      return copyNamespace(requestedNamespace);
    } else {
      return copy();
    }
  }

  /**
   * Returns if any parameter in this parameter set begins the the specified string, followed by a
   * dot. The argument may not be empty.  The name space name should *not* have a
   * trailing ".".
   */
  public boolean isNamespacePresent(final String requestedNamespace) {
    checkArgument(requestedNamespace.length() > 0);
    checkArgument(!requestedNamespace.endsWith(DELIM));
    final String probe = requestedNamespace + DELIM;
    return Iterables.any(params.keySet(), StringUtils.startsWith(probe));
  }

  /**
   * Creates a copy of this parameter set.
   */
  public Parameters copy() {
    return new Parameters(params, namespace);
  }

  public String dump() {
    return dump(true, true);
  }

  public String dumpWithoutNamespacePrefix() {
    return dump(true, false);
  }

  public String dump(final boolean printDateTime) {
    return dump(printDateTime, true);
  }

  /**
   * Dumps the parameters object as colon-separated key-value pairs. If {@code printDateTime} is
   * true, will put a #-style comment with the current date and time at the top. If
   * includeNamespacePrefix is true, will prefix its parameter with its full namespace instead of
   * writing all keys relative to the current namespace.
   */
  public String dump(final boolean printDateTime, final boolean includeNamespacePrefix) {
    final StringWriter sOut = new StringWriter();
    final PrintWriter out = new PrintWriter(sOut);

    if (printDateTime) {
      // output a timestamp comment
      final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      out.format("#%s\n", timeFormat.format(new Date()));
    }
    List<String> keys = new ArrayList<>(params.keySet());
    Collections.sort(keys);
    for (final String rawKey : keys) {
      final String key;
      if (includeNamespacePrefix) {
        key = fullString(rawKey);
      } else {
        key = rawKey;
      }
      out.format("%s: %s\n", key, params.get(rawKey));
    }

    out.close();
    return sOut.toString();
  }


  public static Parameters loadSerifStyle(final File f) throws IOException {
    return new SerifStyleParameterFileLoader.Builder().build().load(f);
  }

  public static Parameters fromMap(Map<String, String> map) {
    return new Parameters(map, ImmutableList.<String>of());
  }

  public static Parameters fromMap(Map<String, String> map, List<String> namespace) {
    return new Parameters(map, namespace);
  }

  /**
   * Creates a {@code Parameters} from a {@link java.util.Properties} by turning each key and value
   * in the {@code Properties} into a string. If multiple keys in the properties object have the
   * same string representation or if any key or value is null, an {@link
   * java.lang.IllegalArgumentException} or {@link java.lang.NullPointerException} will be thrown.
   */
  public static Parameters fromProperties(Properties properties) {
    final ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();
    for (final Map.Entry<Object, Object> property : properties.entrySet()) {
      ret.put(property.getKey().toString(), property.getValue().toString());
    }
    return fromMap(ret.build());
  }

  /**
   * Combines these parameters with the others supplied to make a new <code>Parameters</code>. The new parameters
   * will contain all mappings present in either. If a mapping is present in both, the <code>other</code> argument
   * parameters take precedence.
   */
  // this is currently unused anywhere, and it will require a little
  // thought how best to make it interact with namespacing
        /*public Parameters compose(final Parameters other) {
                checkNotNull(other);
		final Map<String, String> newMap = Maps.newHashMap();
		newMap.putAll(params);
		newMap.putAll(other.params);
		return new Parameters(newMap);
	}*/

  /**
   * Returns true iff the key <code>param</code> is assigned a value.
   */
  public boolean isPresent(final String param) {
    return params.containsKey(checkNotNull(param));
  }

  /**
   * Gets the value for a parameter as a raw string.
   */
  public String getString(final String param) {
    checkNotNull(param);
    checkArgument(!param.isEmpty());

    final String ret = params.get(param);
    observeWithListeners(param);

    if (ret != null) {
      return ret;
    } else {
      throw new MissingRequiredParameter(fullString(param));
    }
  }

  public Symbol getSymbol(final String param) {
    return Symbol.from(getString(param));
  }

  public Optional<Symbol> getOptionalSymbol(final String param) {
    if (isPresent(param)) {
      return Optional.of(getSymbol(param));
    } else {
      return Optional.absent();
    }
  }

  public Optional<List<Symbol>> getOptionalSymbolList(final String param) {
    if (isPresent(param)) {
      return Optional
          .of(getList(param, SymbolUtils.StringToSymbol(), new AlwaysValid<Symbol>(), "Symbol"));
    } else {
      return Optional.absent();
    }
  }

  public Optional<Set<Symbol>> getOptionalSymbolSet(final String param) {
    if (isPresent(param)) {
      // we know get() will succeed because of isPresent
      //noinspection OptionalGetWithoutIsPresent
      return Optional.<Set<Symbol>>of(ImmutableSet.copyOf(getOptionalSymbolList(param).get()));
    } else {
      return Optional.absent();
    }
  }

  private String fullString(final String param) {
    if (namespace.isEmpty()) {
      return param;
    } else {
      return joinNamespace(namespace) + DELIM + param;
    }
  }

  /**
   * Gets the parameter string for the key <code>param</code>, then runs it throught the converter
   * and checks it with the validator.
   *
   * @param expectation What we expected to see, for produceing error messages.  e.g. "integer" or
   *                    "comma-separated list of strings"
   */
  public <T> T get(final String param, final StringConverter<T> converter,
      final Validator<T> validator,
      final String expectation) {
    checkNotNull(param);
    checkNotNull(converter);
    checkNotNull(validator);
    checkNotNull(expectation);

    final String value = getString(param);

    T ret;

    try {
      ret = converter.decode(value);
    } catch (final Exception e) {
      throw new ParameterConversionException(fullString(param), value, e, expectation);
    }

    try {
      validator.validate(ret);
    } catch (final ValidationException e) {
      throw new ParameterValidationException(fullString(param), value, e);
    }

    if (ret == null) {
      throw new RuntimeException(
          "Parameter converters not allowed to return null for non-null input.");
    }

    return ret;
  }

  /**
   * Gets the parameter string *list* for the key <code>param</code>, then runs each element
   * throught the converter and checks it with the validator.
   *
   * @param expectation What we expected to see, for produceing error messages.  e.g. "integer" or
   *                    "comma-separated list of strings"
   */
  public <T> List<T> getList(final String param, final StringConverter<T> converter,
      final Validator<T> validator,
      final String expectation) {
    checkNotNull(param);
    checkNotNull(converter);
    checkNotNull(validator);
    checkNotNull(expectation);

    final List<String> values = getStringList(param);

    final ImmutableList.Builder<T> retList = ImmutableList.builder();

    for (final String value : values) {
      T ret;

      try {
        ret = converter.decode(value);
      } catch (final Exception e) {
        throw new ParameterConversionException(fullString(param), value, e,
            expectation);
      }

      try {
        validator.validate(ret);
      } catch (final ValidationException e) {
        throw new ParameterValidationException(fullString(param), value, e);
      }

      if (ret == null) {
        throw new RuntimeException(
            "Parameter converters not allowed to return null for non-null input.");
      }

      retList.add(ret);
    }
    return retList.build();
  }

  /**
   * Looks up a parameter.  If the value is not in <code>possibleValues</code>, throws and
   * exception.
   *
   * @param possibleValues May not be null. May not be empty.
   * @throws InvalidEnumeratedPropertyException if the parameter value is not on the list.
   */
  public String getStringOf(final String param, final List<String> possibleValues) {
    checkNotNull(possibleValues);
    checkArgument(!possibleValues.isEmpty());

    final String value = getString(param);

    if (possibleValues.contains(value)) {
      return value;
    } else {
      throw new InvalidEnumeratedPropertyException(fullString(param), value, possibleValues);
    }
  }

  /**
   * Looks up a parameter, then uses the value as a key in a map lookup.  If the value is not a key
   * in the map, throws an exception.
   *
   * @param possibleValues May not be null. May not be empty.
   * @throws InvalidEnumeratedPropertyException if the parameter value is not on the list.
   */
  public <T> T getMapped(final String param, final Map<String, T> possibleValues) {
    checkNotNull(possibleValues);
    checkArgument(!possibleValues.isEmpty());

    final String value = getString(param);
    final T ret = possibleValues.get(value);

    if (ret == null) {
      throw new InvalidEnumeratedPropertyException(fullString(param), value,
          possibleValues.keySet());
    }
    return ret;
  }

  public <T extends Enum<T>> T getEnum(final String param, final Class<T> clazz) {
    return this.get(param, new StringToEnum<>(clazz), new AlwaysValid<T>(), "enumeration");
  }

  public <T extends Enum<T>> Optional<T> getOptionalEnum(final String param, final Class<T> clazz) {
    if (isPresent(param)) {
      return Optional
          .of(this.get(param, new StringToEnum<>(clazz), new AlwaysValid<T>(), "enumeration"));
    } else {
      return Optional.absent();
    }
  }

  /**
   * Gets a parameter whose value is a (possibly empty) list of enums.
   */
  public <T extends Enum<T>> List<T> getEnumList(final String param, final Class<T> clazz) {
    return this.getList(param, new StringToEnum<>(clazz), new AlwaysValid<T>(), "enumeration");
  }

  public Class<?> getClassObjectForString(final String className) throws ClassNotFoundException {
    if (!className.contains(" ")) {
      return Class.forName(className);
    } else {
      throw new ParameterException("Class names cannot contain spaces: " + className);
    }
  }

  public Class<?> getClassObject(final String param) {
    final String className = getString(param);
    try {
      return getClassObjectForString(className);
    } catch (final ClassNotFoundException e) {
      throw new ParameterConversionException(fullString(param), className, e, "existing class");
    }
  }

  /**
   * Turns a parameter whose value is a comma-separated list of class names into a list of the
   * corresponding {@link Class} objects.
   */
  public ImmutableList<Class<?>> getClassObjects(final String param) {
    final ImmutableList.Builder<Class<?>> ret = ImmutableList.builder();
    for (final String className : getStringList(param)) {
      try {
        ret.add(getClassObjectForString(className));
      } catch (ClassNotFoundException e) {
        throw new ParameterConversionException(fullString(param), className, e, "class");
      }
    }
    return ret.build();
  }

  @SuppressWarnings("unchecked")
  public <T> T getParameterInitializedObject(final String param, final Class<T> superClass) {
    final Class<?> clazz = getClassObject(param);

    return parameterInitializedObjectForClass(clazz, param, superClass);
  }

  public <T> Optional<T> getOptionalParameterInitializedObject(final String param,
      final Class<T> superClass) {
    if (isPresent(param)) {
      return Optional.of(getParameterInitializedObject(param, superClass));
    } else {
      return Optional.absent();
    }
  }


  private <T> T parameterInitializedObjectForClass(final Class<?> clazz,
      final String param, final Class<T> superClass) {
    Optional<Object> ret;

    try {

      ret = createViaParamConstructor(clazz, param);
      if (!ret.isPresent()) {
        ret = createViaStaticFactoryMethod(clazz, param);
      }
      if (!ret.isPresent()) {
        ret = createViaZeroArgConstructor(clazz, param);
      }
    } catch (IllegalAccessException | InstantiationException | InvocationTargetException iae) {
      throw new ParameterException("While attempting to load parameter-initialized object from "
          + param + " :", iae);
    }

    if (!ret.isPresent()) {
      throw new ParameterValidationException(fullString(param), getString(param),
          new RuntimeException(String.format("Class %s has neither fromParameters(params) "
                  + "static factory method or constructor which takes params",
              clazz.getName())));
    }

    if (superClass.isInstance(ret.get())) {
      return (T) ret.get();
    } else {
      throw new ParameterValidationException(fullString(param), getString(param),
          new RuntimeException(
              String.format("Can't cast %s to %s", clazz.getName(), superClass.getName())));
    }
  }

  private Optional<Object> createViaZeroArgConstructor(final Class<?> clazz, final String param)
      throws IllegalAccessException, InvocationTargetException, InstantiationException {
    try {
      return Optional.of(clazz.getConstructor().newInstance());
    } catch (NoSuchMethodException nsme) {
      return Optional.absent();
    }
  }

  private Optional<Object> createViaParamConstructor(Class<?> clazz, String param)
      throws IllegalAccessException, InvocationTargetException, InstantiationException {
    try {
      return Optional.of(clazz.getConstructor(Parameters.class).newInstance(this));
    } catch (NoSuchMethodException nsme) {
      return Optional.absent();
    }
  }

  private Optional<Object> createViaStaticFactoryMethod(Class<?> clazz, String param)
      throws InvocationTargetException, IllegalAccessException {
    try {
      return Optional.of(clazz.getMethod("fromParameters", Parameters.class).invoke(null, this));
    } catch (NoSuchMethodException e) {
      return Optional.absent();
    }
  }

  @SuppressWarnings("unchecked")
  public <T, S> ImmutableList<T> getParameterInitializedObjects(
      final String param, final Class<S> superClass) {
    final List<String> classNames = getStringList(param);
    final ImmutableList.Builder<T> ret = ImmutableList.builder();

    for (final String className : classNames) {
      Class<?> clazz;
      try {
        clazz = getClassObjectForString(className);
      } catch (final ClassNotFoundException e) {
        throw new ParameterValidationException(fullString(param), getString(param), e);
      }

      ret.add((T) parameterInitializedObjectForClass(clazz, param, superClass));
    }

    return ret.build();
  }

  /**
   * Gets a parameter whose value is a (possibly empty) list of integers.
   */
  public List<Integer> getIntegerList(final String param) {
    return getList(param, new StringToInteger(),
        new AlwaysValid<Integer>(), "integer");
  }

  /**
   * Gets a "true/false" parameter.
   */
  public boolean getBoolean(final String param) {
    return get(param, new StrictStringToBoolean(),
        new AlwaysValid<Boolean>(), "boolean");
  }

  public Optional<Boolean> getOptionalBoolean(final String param) {
    if (isPresent(param)) {
      return Optional.of(getBoolean(param));
    } else {
      return Optional.absent();
    }
  }

  /**
   * Gets a parameter whose value is a (possibly empty) list of booleans.
   */
  public List<Boolean> getBooleanList(final String param) {
    return getList(param, new StrictStringToBoolean(),
        new AlwaysValid<Boolean>(), "boolean");
  }

  public Optional<String> getOptionalString(final String param) {
    if (isPresent(param)) {
      return Optional.of(getString(param));
    } else {
      return Optional.absent();
    }
  }


  /**
   * Gets an integer parameter.
   */
  public int getInteger(final String param) {
    return get(param, new StringToInteger(),
        new AlwaysValid<Integer>(), "integer");
  }

  public Optional<Integer> getOptionalInteger(final String param) {
    if (isPresent(param)) {
      return Optional.of(getInteger(param));
    } else {
      return Optional.absent();
    }
  }

  public Optional<Integer> getOptionalPositiveInteger(final String param) {
    if (isPresent(param)) {
      return Optional.of(get(param, new StringToInteger(),
          new IsPositive<Integer>(), "positive integer"));
    } else {
      return Optional.absent();
    }
  }

  /**
   * Gets an positive integer parameter.
   */
  public int getPositiveInteger(final String param) {
    return get(param, new StringToInteger(),
        new IsPositive<Integer>(), "positive integer");
  }

  /**
   * Gets a parameter whose value is a (possibly empty) list of positive integers.
   */
  public List<Integer> getPositiveIntegerList(final String param) {
    return getList(param, new StringToInteger(),
        new IsPositive<Integer>(), "positive integer");
  }

  /**
   * Gets a positive double parameter.
   */
  public double getPositiveDouble(final String param) {
    return get(param, new StringToDouble(),
        new IsPositive<Double>(), "positive double");
  }

  public Optional<Double> getOptionalPositiveDouble(final String param) {
    if (isPresent(param)) {
      return Optional.of(getPositiveDouble(param));
    }
    return Optional.absent();
  }

  /**
   * Gets a parameter whose value is a (possibly empty) list of positive doubles.
   */
  public List<Double> getPositiveDoubleList(final String param) {
    return getList(param, new StringToDouble(),
        new IsPositive<Double>(), "positive double");
  }

  /**
   * Gets a non-negative double parameter.
   */
  public double getNonNegativeDouble(final String param) {
    return get(param, new StringToDouble(),
        new IsNonNegative<Double>(), "non-negative double");
  }

  /**
   * Gets a parameter whose value is a (possibly empty) list of non-negative doubles.
   */
  public List<Double> getNonNegativeDoubleList(final String param) {
    return getList(param, new StringToDouble(),
        new IsNonNegative<Double>(), "non-negative double");
  }

  /**
   * Gets a non-negative integer number parameter.
   */
  public int getNonNegativeInteger(final String param) {
    return get(param, new StringToInteger(),
        new IsNonNegative<Integer>(), "non-negative integer");
  }

  /**
   * Gets a double parameter.
   */
  public double getDouble(final String param) {
    return get(param, new StringToDouble(),
        new AlwaysValid<Double>(), "double");
  }

  /**
   * Gets a double between 0.0 and 1.0, inclusive.
   */
  public double getProbability(final String param) {
    return get(param, new StringToDouble(),
        new IsInRange<>(Range.closed(0.0, 1.0)),
        "probability");
  }

  private StringConverter<File> getFileConverter() {
    if (isPresent(DO_OS_CONVERSION_PARAM) &&
        getBoolean(DO_OS_CONVERSION_PARAM)) {
      return new StringToOSFile();
    } else {
      return new StringToFile();
    }
  }

  /**
   * Gets a file, which is required to exist.
   */
  public File getExistingFile(final String param) {
    return get(param, getFileConverter(),
        new And<>(new FileExists(), new IsFile()),
        "existing file");
  }

  public File getFirstExistingFile(String param) {
    final List<String> fileStrings = getStringList(param);
    for (final String fileName : fileStrings) {
      final File f = new File(fileName.trim());
      if (f.isFile()) {
        return f;
      }
    }

    throw new ParameterConversionException(fullString(param), fileStrings.toString(),
        "No provided path is an existing file");
  }

  /**
   * Gets a file or directory, which is required to exist.
   */
  public File getExistingFileOrDirectory(final String param) {
    return get(param, getFileConverter(), new FileExists(),
        "existing file or directory");
  }

  /**
   * Gets a directory which is guaranteed to exist after the execution of this method.  If the
   * directory does not already exist, it and its parents are created. If this is not possible, an
   * exception is throws.
   */
  public File getAndMakeDirectory(final String param) {
    final File f = get(param, new StringToFile(),
        new AlwaysValid<File>(), "existing or creatable directory");

    if (f.exists()) {
      if (f.isDirectory()) {
        return f.getAbsoluteFile();
      } else {
        throw new ParameterValidationException(fullString(param), f
            .getAbsolutePath().toString(),
            new ValidationException("Not an existing or creatable directory"));
      }
    } else {
      f.getAbsoluteFile().mkdirs();
      return f.getAbsoluteFile();
    }
  }

  /**
   * Gets a directory which already exists.
   */
  public File getExistingDirectory(final String param) {
    return get(param, new StringToFile(),
        new And<>(new FileExists(), new IsDirectory()),
        "existing directory");
  }

  /**
   * Gets a file or directory parameter without specifying whether it exists. Prefer a more specific
   * parameter accessor when possible.
   */
  public File getFileOrDirectory(final String param) {
    return get(param, new StringToFile(), new AlwaysValid<File>(), "file or directory");
  }

  /**
   * Gets a (possibly empty) list of existing directories. Will throw a {@link
   * com.bbn.bue.common.parameters.exceptions.ParameterValidationException} if any of the supplied
   * paths are not existing directories.
   */
  public ImmutableList<File> getExistingDirectories(String param) {
    final List<String> fileStrings = getStringList(param);
    final ImmutableList.Builder<File> ret = ImmutableList.builder();

    for (final String dirName : fileStrings) {
      final File dir = new File(dirName.trim());
      if (!dir.isDirectory()) {
        throw new ParameterValidationException(fullString(param), dirName,
            "path does not exist or is not a directory");
      }
      ret.add(dir);
    }

    return ret.build();
  }

  /**
   * Gets the first existing directory in a common-separated list. If none exists, throws an {@link
   * com.bbn.bue.common.parameters.exceptions.ParameterValidationException}.
   */
  public File getFirstExistingDirectory(String param) {
    final List<String> directoryStrings = getStringList(param);
    for (final String dirName : directoryStrings) {
      final File dir = new File(dirName.trim());
      if (dir.isDirectory()) {
        return dir;
      }
    }

    throw new ParameterConversionException(fullString(param), directoryStrings.toString(),
        "No provided path is an existing directory");
  }


  public Optional<File> getOptionalExistingDirectory(final String param) {
    if (isPresent(param)) {
      return Optional.of(getExistingDirectory(param));
    }
    return Optional.absent();
  }

  /**
   * Gets a ,-separated set of Strings.
   */
  public Set<String> getStringSet(final String param) {
    return get(param, new StringToStringSet(","),
        new AlwaysValid<Set<String>>(),
        "comma-separated list of strings");
  }

  /**
   * Gets a parameter whose value is a (possibly empty) comma-separated list of Strings.
   */
  public List<String> getStringList(final String param) {
    return get(param, new StringToStringList(","),
        new AlwaysValid<List<String>>(),
        "comma-separated list of strings");
  }

  /**
   * Gets a parameter whose value is a (possibly empty) comma-separated list of Strings, if
   * present.
   */
  public Optional<List<String>> getOptionalStringList(final String param) {
    if (isPresent(param)) {
      return Optional.of(getStringList(param));
    }
    return Optional.absent();
  }

  /**
   * Gets a ,-separated set of Symbols
   */
  public Set<Symbol> getSymbolSet(final String param) {
    return get(param, new StringToSymbolSet(","),
        new AlwaysValid<Set<Symbol>>(),
        "comma-separated list of strings");
  }

  /**
   * Gets a parameter whose value is a (possibly empty) comma-separated list of Symbols.
   */
  public List<Symbol> getSymbolList(final String param) {
    return get(param, new StringToSymbolList(","),
        new AlwaysValid<List<Symbol>>(),
        "comma-separated list of strings");
  }

  public File getCreatableFile(final String param) {
    final String val = getString(param);
    final File ret = new File(val);

    if (ret.exists()) {
      if (ret.isDirectory()) {
        throw new ParameterValidationException(fullString(param), val,
            "Requested a file, but directory exists with that filename");
      }
    } else {
      ret.getAbsoluteFile().getParentFile().mkdirs();
    }

    return ret;
  }

  /**
   * Gets a file, with no requirements about whether it exists or not. if you intend to write to
   * this file, you may prefer {@link #getCreatableFile(String)}, which will create its parent
   * directories.
   */
  public File getPossiblyNonexistentFile(final String param) {
    return new File(getString(param));
  }


  public File getCreatableDirectory(final String param) {
    final String val = getString(param);
    final File ret = new File(val);

    if (ret.exists()) {
      if (!ret.isDirectory()) {
        throw new ParameterValidationException(fullString(param), val,
            "Requested a directory, but a file exists with that filename");
      }
    } else {
      ret.getAbsoluteFile().mkdirs();
    }

    return ret;
  }

  public File getEmptyDirectory(final String param) {
    final File dir = getCreatableDirectory(param);
    final int numFilesContained = dir.list().length;

    if (numFilesContained != 0) {
      throw new ParameterValidationException(fullString(param), getString(param),
          String.format("Requested an empty directory, but directory contains %d files.",
              numFilesContained));
    }
    return dir;
  }

  /**
   * Convenience method to call {@link #getExistingFile(String)} and then apply {@link
   * FileUtils#loadSymbolSet(CharSource)} on it.
   */
  public ImmutableSet<Symbol> getFileAsSymbolSet(String param) throws IOException {
    return FileUtils.loadSymbolSet(Files.asCharSource(getExistingFile(param), Charsets.UTF_8));
  }

  /**
   * Convenience method to call {@link #getExistingFile(String)} and then apply {@link
   * FileUtils#loadSymbolSet(CharSource)} on it, if the param is present. If the param is missing,
   * {@link Optional#absent()} is returned.
   */
  public Optional<ImmutableSet<Symbol>> getOptionalFileAsSymbolSet(String param)
      throws IOException {
    if (isPresent(param)) {
      return Optional
          .of(FileUtils.loadSymbolSet(Files.asCharSource(getExistingFile(param), Charsets.UTF_8)));
    } else {
      return Optional.absent();
    }
  }

  /**
   * Convenience method to call {@link #getExistingFile(String)} and then apply {@link
   * FileUtils#loadSymbolList(CharSource)} on it.
   */
  public ImmutableList<Symbol> getFileAsSymbolList(String param) throws IOException {
    return FileUtils.loadSymbolList(Files.asCharSource(getExistingFile(param), Charsets.UTF_8));
  }

  /**
   * Convenience method to call {@link #getExistingFile(String)} and then apply {@link
   * FileUtils#loadSymbolList(CharSource)} on it, if the param is present. If the param is missing,
   * {@link Optional#absent()} is returned.
   */
  public Optional<ImmutableList<Symbol>> getOptionalFileAsSymbolList(String param)
      throws IOException {
    if (isPresent(param)) {
      return Optional
          .of(FileUtils.loadSymbolList(Files.asCharSource(getExistingFile(param), Charsets.UTF_8)));
    } else {
      return Optional.absent();
    }
  }


  /**
   * Convenience method to call {@link #getExistingFile(String)} and then apply {@link
   * FileUtils#loadStringSet(CharSource)} on it.
   */
  public ImmutableSet<String> getFileAsStringSet(String param) throws IOException {
    return FileUtils.loadStringSet(Files.asCharSource(getExistingFile(param), Charsets.UTF_8));
  }

  /**
   * Convenience method to call {@link #getExistingFile(String)} and then apply {@link
   * FileUtils#loadStringSet(CharSource)} on it, if the param is present. If the param is missing,
   * {@link Optional#absent()} is returned.
   */
  public Optional<ImmutableSet<String>> getOptionalFileAsStringSet(String param)
      throws IOException {
    if (isPresent(param)) {
      return Optional
          .of(FileUtils.loadStringSet(Files.asCharSource(getExistingFile(param), Charsets.UTF_8)));
    } else {
      return Optional.absent();
    }
  }

  /**
   * Convenience method to call {@link #getExistingFile(String)} and then apply {@link
   * FileUtils#loadStringList(CharSource)} on it.
   */
  public ImmutableList<String> getFileAsStringList(String param) throws IOException {
    return FileUtils.loadStringList(Files.asCharSource(getExistingFile(param), Charsets.UTF_8));
  }


  /**
   * Convenience method to call {@link #getExistingFile(String)} and then apply {@link
   * FileUtils#loadStringList(CharSource)} on it, if the param is present. If the param is missing,
   * {@link Optional#absent()} is returned.
   */
  public Optional<ImmutableList<String>> getOptionalFileAsStringList(String param)
      throws IOException {
    if (isPresent(param)) {
      return Optional
          .of(FileUtils.loadStringList(Files.asCharSource(getExistingFile(param), Charsets.UTF_8)));
    } else {
      return Optional.absent();
    }
  }

  public ImmutableMap<Symbol, File> getFileAsSymbolToFileMap(String param) throws IOException {
    return FileUtils
        .loadSymbolToFileMap(Files.asCharSource(getExistingFile(param), Charsets.UTF_8));
  }

  public Parameters getSubParameters(final String param) throws IOException {
    final File paramFile = getExistingFile(param);
    return Parameters.loadSerifStyle(paramFile);
  }

  /**
   * Throws a ParameterException if neither parameter is defined.
   */
  public void assertAtLeastOneDefined(final String param1, final String param2) {
    if (!isPresent(param1) && !isPresent(param2)) {
      throw new ParameterException(
          String.format("At least one of %s and %s must be defined.", param1, param2));
    }
  }

  /**
   * Throws a ParameterException if none of the supplied parameters are defined.
   */
  public void assertAtLeastOneDefined(final String param1, final String... moreParams) {
    if (!isPresent(param1)) {
      for (final String moreParam : moreParams) {
        if (isPresent(moreParam)) {
          return;
        }
      }
      final List<String> paramsForError = Lists.newArrayList();
      paramsForError.add(param1);
      paramsForError.addAll(Arrays.asList(moreParams));
      throw new ParameterException(
          String.format("At least one of %s must be defined.",
              StringUtils.CommaSpaceJoiner.join(paramsForError)));
    }
  }

  /**
   * Throws a ParameterException unless exactly one parameter is defined.
   */
  public void assertExactlyOneDefined(final String param1, final String param2) {
    // Asserting that exactly one is defined is the same as asserting that they do not have the same
    // value for definedness.
    if (isPresent(param1) == isPresent(param2)) {
      throw new ParameterException(
          String.format("Exactly one of %s and %s must be defined.", param1, param2));
    }
  }

  /**
   * Throws a ParameterException unless exactly one parameter is defined.
   */
  public void assertExactlyOneDefined(final String... params) {
    int definedCount = 0;
    for (final String param : params) {
      if (isPresent(param)) {
        if (++definedCount == 2) {
          // No point in going past two
          break;
        }
      }
    }
    if (definedCount != 1) {
      throw new ParameterException(
          String.format("Exactly one of %s must be defined.",
              StringUtils.CommaSpaceJoiner.join(params)));
    }
  }

  public Optional<File> getOptionalExistingFile(final String param) {
    if (isPresent(param)) {
      return Optional.of(getExistingFile(param));
    } else {
      return Optional.absent();
    }
  }

  public Optional<File> getOptionalCreatableFile(final String param) {
    if (isPresent(param)) {
      return Optional.of(getCreatableFile(param));
    } else {
      return Optional.absent();
    }
  }

  public File getExistingFileRelativeTo(final File root, final String param) {
    if (!root.exists()) {
      throw new ParameterException(
          String.format("Cannot resolve parameter %s relative to non-existent directory", param),
          new FileNotFoundException(String.format("Not found: %s", root)));
    }
    final String val = getString(param);
    final File ret = new File(root, val);
    if (!ret.exists()) {
      throw new ParameterValidationException(fullString(param), ret.getAbsolutePath(),
          "Requested existing file, but the file does not exist");
    }
    return ret;
  }

  /**
   * Given a list of strings, returns the first string in the list which exists as a parameter. If
   * none do, a {@link ParameterException} is thrown.
   */
  public String getFirstExistingParamName(String[] paramNames) {
    for (final String paramName : paramNames) {
      if (isPresent(paramName)) {
        return paramName;
      }
    }
    throw new ParameterException("One of " + Arrays.toString(paramNames)
        + " must be present");
  }

  /**
   * Gets the parameter associated with an annotation. Provided with an annotation class, this will
   * check first if it has a {@code String} field called {@code param}.  If it does, its value is
   * returned. If not, it checks for a {@code String} field called {@code params}. If it exists, it
   * is split on ",", the elements are trimmed, and the return value of {@link
   * #getFirstExistingParamName(String[])} on the resulting array is returned. If neither is
   * present, a {@link ParameterException} is thrown.
   *
   * The reason this hack-y thing exists is that it is often convenient for Guice annotations to
   * include on the annotation the parameter typically used to set it when configuring from a param
   * file. However, we cannot specify array valued fields on annotations, so we need to use a
   * comma-separated {@code String}.
   */
  public String getParamForAnnotation(Class<?> clazz) {
    try {
      return (String) clazz.getField("param").get("");
    } catch (NoSuchFieldException e) {
      try {
        return getFirstExistingParamName(
            StringUtils.onCommas().splitToList((String) clazz.getField("params").get(""))
                .toArray(new String[]{}));
      } catch (NoSuchFieldException e1) {
        throw new ParameterException("Annotation " + clazz + " must have param or params field");
      } catch (IllegalAccessException e1) {
        throw new ParameterException("While fetching parameter from annotation " + clazz, e);
      }
    } catch (IllegalAccessException e) {
      throw new ParameterException("While fetching parameter from annotation " + clazz, e);
    }
  }

  /**
   * Returns the dot-separated namespace.
   */
  public String namespace() {
    return joinNamespace(namespace);
  }

  /**
   * Returns the namespace as a list.
   */
  public ImmutableList<String> namespaceAsList() {
    return namespace;
  }

  /**
   * Returns the map of string parameter names to their string values. The namespace is not included
   * in the keys; use {@link #namespace()} to get it.
   */
  public ImmutableMap<String, String> asMap() {
    return params;
  }

  /**
   * package-private
   */
  void registerListener(Listener listener) {
    listeners.add(listener);
  }

  private void observeWithListeners(final String param) {
    // all parameter requests eventually get routed through here,
    // so this is where we observe
    for (final Parameters.Listener listener : listeners) {
      listener.observeParameterRequest(JOINER.join(
          FluentIterable.from(namespace).append(param)));
    }
  }

  private final ImmutableMap<String, String> params;
  private final ImmutableList<String> namespace;
  private final List<Listener> listeners = Lists.newArrayList();

  /**
   * Creates a new {@code Parameters} which has all the parameters in both this one and {@code
   * paramsToAdd}. If there are collisions between this and {@code paramsToAdd}, {@code
   * paramsToAdd}'s value is preferred.
   *
   * The result maintains the namespace of {@code this} and parameters from {@code paramsToAdd} are
   * copied into the current namespace using their names relative to the {@code paramsToAdd}'s
   * working namespace.  For example, suppose {@code this}'s namespace is {@code com.bbn.foo} and
   * {@code paramsToAdd}'s namespace is {@code com.bbn.bar.meep}.  If {code paramsToAdd} has a
   * parameter whose original absolute name was {@code com.bbn.bar.meep.lalala.myParam}, it will
   * become {@code com.bbn.foo.lalala.myParam} in the result.
   *
   * Beware this behavior may be confusing to users because error messages may refer to parameters
   * which do not appear to exist.
   */
  public Parameters copyMergingIntoCurrentNamespace(final Parameters paramsToAdd) {
    // we use the immutable map builder even though it requires tracking seen
    // items separately to maintain determinism
    final ImmutableMap.Builder<String, String> newParamsMap = ImmutableMap.builder();
    final Set<String> seen = Sets.newHashSet();

    newParamsMap.putAll(paramsToAdd.params);
    seen.addAll(paramsToAdd.params.keySet());
    newParamsMap.putAll(Maps.filterKeys(params, not(in(seen))));
    return new Parameters(newParamsMap.build(), namespace);
  }

  public Builder modifiedCopyBuilder() {
    final Builder ret = new Builder(namespace);
    ret.putAll(params);
    return ret;
  }

  /**
   * Creates a new builder with the default (empty) namespace.
   */
  public static Builder builder() {
    return new Builder(ImmutableList.<String>of());
  }

  /**
   * Creates a new builder with the specified namespace.
   */
  public static Builder builder(List<String> namespace) {
    return new Builder(namespace);
  }

  /**
   * Returns the specified namespace split into a list, for example {@code ["foo", "bar"]} for
   * {@code "foo.bar"}.
   */
  public static List<String> splitNamespace(final String namespace) {
    return StringUtils.onDots().splitToList(namespace);
  }

  /**
   * Returns the specified namespace joined into a string, for example {@code "foo.bar"} for {@code
   * ["foo", "bar"]}. The namespace may consist of any number of elements, including none at all.
   * No element in the namespace may begin or end with a period.
   */
  public static String joinNamespace(final List<String> namespace) {
    for (final String element : namespace) {
      checkArgument(!element.startsWith(DELIM),
          "Namespace element may not begin with a period: " + element);
      checkArgument(!element.endsWith(DELIM),
          "Namespace element may not end with a period: " + element);
    }
    return JOINER.join(namespace);
  }

  /**
   * Returns the specified namespace joined into a string, for example {@code "foo.bar"} for
   * arguments {@code ["foo", "bar"]}. To match the behavior of {@link #joinNamespace(List)},
   * the namespace may consist of any number of elements, including none at all. No element in the
   * namespace may begin or end with a period.
   */
  public static String joinNamespace(final String... elements) {
    return JOINER.join(elements);
  }

  public static final class Builder {

    private final Map<String, String> params = Maps.newHashMap();
    private final List<String> namespace;

    private Builder(final List<String> namespace) {
      this.namespace = ImmutableList.copyOf(namespace);
    }

    public Builder set(String key, String value) {
      checkNotNull(key);
      checkArgument(!key.isEmpty(), "Key must be non-empty");
      checkArgument(!WHITESPACE_PATTERN.matcher(key).find(), "Key cannot contain whitespace");
      checkNotNull(value);
      // Medial whitespace is allowed, but we remove initial/final whitespace as it will not
      // preserved in loading.
      value = value.trim();
      checkArgument(!value.isEmpty(), "Value cannot be empty or only whitespace");
      params.put(key, value);
      return this;
    }

    public Builder putAll(Map<String, String> data) {
      params.putAll(data);
      return this;
    }

    public Parameters build() {
      return new Parameters(params, namespace);
    }
  }


  @Beta
  interface Listener {

    void observeParameterRequest(String param);
  }

  /**
   * Gets objects defined by namespaces. It frequently happens that a program needs to have
   * specified some set of objects to use, where each object is defined by some namespace. For
   * example, in the name finder, we might need to use a number of different name set groups, where
   * each group is defined by either a single name list or a map of name list names to name lists.
   * Using this we would have a parameter file like this:
   * <pre>
   *   # note foo is not used
   *   com.bbn.serif.names.lists.activeListGroups: standard,geonames,single
   *   com.bbn.serif.names.lists.standard.mapFile: /nfs/.....
   *   com.bbn.serif.names.lists.geonames.mapFile: /nfs/....
   *   com.bbn.serif.names.lists.foo.listPath: /nfs/....
   *   com.bbn.serif.names.lists.foo.listName: single
   *   com.bbn.serif.names.lists.foo.listPath: /nfs/...
   *   com.bbn.serif.names.lists.foo.listName: foo
   * </pre>
   *
   * The user could load these by {@code objectsFromNameSpace("com.bbn.serif.names.lists",
   * "activeListGroups", aNameSpaceToObjectMapperImplementation)}.
   *
   * If {@code activeNameSpacesFeature} is absent this will thrown a {@link ParameterException}.
   */
  public <T> ImmutableSet<T> objectsFromNamespaces(String baseNamespace,
      String activeNamespacesFeature,
      NamespaceToObjectMapper<? extends T> nameSpaceToObjectMapper) {
    final Parameters subNamespace = copyNamespace(baseNamespace);
    final ImmutableSet.Builder<T> ret = ImmutableSet.builder();
    for (final String activeNamespace : subNamespace.getStringList(activeNamespacesFeature)) {
      if (subNamespace.isNamespacePresent(activeNamespace)) {
        ret.add(nameSpaceToObjectMapper.fromNameSpace(subNamespace.copyNamespace(activeNamespace)));
      } else {
        throw new ParameterException("Expected namespace " + baseNamespace + DELIM + activeNamespace
            + "to exist because of value of " + activeNamespacesFeature + " but "
            + "it did not");
      }
    }
    return ret.build();
  }

  @Beta
  public interface NamespaceToObjectMapper<T> {

    T fromNameSpace(Parameters params);
  }
}
