package com.bbn.bue.common.temporal;

import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a time value according to "TIDES Instruction Manual for the Annotation of Temporal
 * Expressions", Lisa Ferro, 2001. Please refer to that document for details.
 *
 * Beware: full validity checking is not done, so it is possible to construct invalid Timex
 * expressions.
 *
 * Equality is determined strictly by field equality and not by any comparison of the time periods
 * denoted.
 *
 * This currently only supports durations of the form \dE, \dC, \DL, or ISO 8601 periods. Durations
 * which mix and match these (which should be incredibly rare) will act as if they have no duration.
 * See TIMEX2 specification section 3.2.4.1.
 *
 * @author Ryan Gabbard, Feb. 2014
 */
public final class Timex2Time {

  private static final Logger log = LoggerFactory.getLogger(Timex2Time.class);

  public static final ImmutableSet<Symbol> nonISOMarkers = SymbolUtils.setFrom("WE", "MO", "MI",
      "AF", "EV", "NI", "PM", "DT", "FA", "WI", "SP", "SU", "FY", "Q1", "Q2", "Q3", "Q4", "QX",
      "H1", "H2", "HX", "BC", "KA", "MA", "GA");
  private static final Pattern nonISOMarkersPattern = Pattern.compile("("
      + Joiner.on("|").join(nonISOMarkers) + ")");

  public enum Modifier {
    BEFORE, AFTER, ON_OR_BEFORE, ON_OR_AFTER,
    LESS_THAN, MORE_THAN, EQUAL_OR_LESS, EQUAL_OR_MORE,
    START, MID, END, APPROX;
  }

  public enum AnchorDirection {
    STARTING, ENDING, WITHIN, BEFORE, AFTER, AS_OF;
  }

  private static final Symbol PRESENT_REF = Symbol.from("PRESENT_REF");
  private static final Symbol PAST_REF = Symbol.from("PAST_REF");
  private static final Symbol FUTURE_REF = Symbol.from("FUTURE_REF");

  // keeping for backward compatibility
  public static final Symbol BEFORE = Symbol.from(AnchorDirection.BEFORE.name());
  public static final Symbol AFTER = Symbol.from(AnchorDirection.AFTER.name());
  public static final Symbol AS_OF = Symbol.from(AnchorDirection.AS_OF.name());

  private final Symbol val;
  private final Modifier mod;
  private final boolean set;
  private final Symbol granularity;
  private final Symbol periodicity;
  private final Symbol anchorVal;
  private final AnchorDirection anchorDir;
  private final boolean nonSpecific;

  // Joda-time interpetation
  // This is a derived field (derived from val if val denotes a period)
  private final Period duration;

  public boolean isNonSpecific() {
    return nonSpecific;
  }

  public Optional<Symbol> value() {
    return Optional.fromNullable(val);
  }

  public Optional<Modifier> modifier() {
    return Optional.fromNullable(mod);
  }

  public boolean isSet() {
    return set;
  }

  public Optional<Symbol> granularity() {
    return Optional.fromNullable(granularity);
  }

  public Optional<Symbol> periodicity() {
    return Optional.fromNullable(periodicity);
  }

  public Optional<Symbol> anchorValue() {
    return Optional.fromNullable(anchorVal);
  }

  public Optional<AnchorDirection> anchorDirection() {
    return Optional.fromNullable(anchorDir);
  }

  public boolean isReferenceToPresent() {
    return val == PRESENT_REF;
  }

  public boolean isReferenceToPast() {
    return val == PAST_REF;
  }

  public boolean isReferenceToFuture() {
    return val == FUTURE_REF;
  }

  public boolean isDuration() {
    return duration != null;
  }

  public Optional<Period> duration() {
    return Optional.fromNullable(duration);
  }

  public Optional<Interval> anchorAsInterval() {
    if (anchorVal != null) {
      return parseValueAsInterval(anchorVal.toString());
    }
    return Optional.absent();
  }

  public Optional<Interval> valueAsInterval() {
    if (val != null) {
      return parseValueAsInterval(val.toString());
    }
    return Optional.absent();
  }

  /**
   * @deprecated Use {@link #builder()} and {@link Builder#withVal(Symbol)} instead
   */
  @Deprecated
  public static Timex2Time from(final String value) {
    return new Timex2Time(Symbol.from(value), null, false, null, null, null, null, false);
  }

  /**
   * @deprecated Use {@link #builder()} and {@link Builder#withGranularity(Symbol)} and {@link
   * Builder#withPeriodicity(Symbol)}
   */
  @Deprecated
  public static Timex2Time createSet(final String frequency, final String granularity) {
    return new Timex2Time(null, null, true, Symbol.from(frequency), Symbol.from(granularity), null,
        null, false);
  }

  public static Timex2Time createEmpty() {
    return new Timex2Time(null, null, false, null, null, null, null, false);
  }

  public static Timex2Time present() {
    return new Timex2Time(PRESENT_REF, null, false, null, null, null, null, false);
  }

  public static Timex2Time past() {
    return new Timex2Time(PAST_REF, null, false, null, null, null, null, false);
  }

  public static Timex2Time future() {
    return new Timex2Time(FUTURE_REF, null, false, null, null, null, null, false);
  }

  /**
   * Returns a copy of this Timex which is the same except with the anchor attributes set as
   * specified.
   *
   * @deprecated Use {@link #copyBuilder()} and {@link Builder#withAnchorValue(Symbol)} and {@link
   * Builder#withAnchorDirection(Symbol)} instead
   */
  @Deprecated
  public Timex2Time anchoredCopy(final Symbol anchorVal, final Symbol anchorDir) {
    checkNotNull(anchorDir);
    AnchorDirection anchorDirEnum = AnchorDirection.valueOf(anchorDir.asString());
    return new Timex2Time(val, mod, set, granularity, periodicity, checkNotNull(anchorVal),
        anchorDirEnum, nonSpecific);
  }

  /**
   * Returns a copy of this Timex which is the same except with the anchor attributes set as
   * specified.
   *
   * @deprecated Use {@link #copyBuilder()} and {@link Builder#withAnchorValue(Symbol)} and {@link
   * Builder#withAnchorDirection(Symbol)} instead
   */
  @Deprecated
  public Timex2Time anchoredCopy(final String anchorVal, final Symbol anchorDir) {
    return anchoredCopy(Symbol.from(checkNotNull(anchorVal)), checkNotNull(anchorDir));
  }

  /**
   * Returns a copy of this Timex which is the same except with the specified modifier
   *
   * @deprecated Use {@link #copyBuilder()} and {@link Builder#withModifier(Modifier)} instead
   */
  @Deprecated
  public Timex2Time modifiedCopy(final Modifier modifier) {
    return new Timex2Time(val, modifier, set, granularity, periodicity, anchorVal, anchorDir,
        nonSpecific);
  }

  public Builder copyBuilder() {
    Builder builder = new Builder();
    if (this.set) {
      builder.withIsSet(true);
    }
    if (this.periodicity().isPresent()) {
      builder.withPeriodicity(this.periodicity);
    }
    if (this.granularity().isPresent()) {
      builder.withGranularity(this.granularity);
    }
    if (this.isNonSpecific()) {
      builder.setNonSpecific(true);
    }
    if (this.anchorValue().isPresent()) {
      builder.withAnchorValue(this.anchorVal);
    }
    if (this.anchorDirection().isPresent()) {
      builder.withAnchorDirectionFromEnum(this.anchorDir);
    }
    if (this.value().isPresent()) {
      builder.withVal(this.val);
    }
    return builder;
  }

  // Timex stores boolean values as YES or empty
  private static final Symbol YES = Symbol.from("YES");

  private static boolean parseYES(final Symbol s) throws Timex2Exception {
    if (s == YES) {
      return true;
    }
    throw new Timex2Exception(String.format("Invalid boolean flag %s. Must be YES or absent.", s));
  }

  private static final ImmutableMap<Pattern, Integer> periodPatternsToYearMultipliers =
      ImmutableMap.<Pattern, Integer>builder().put(
          // Decades-2001 specification
          Pattern.compile("(\\d+)E"), 10).put(
          // Centuries-2001 specification
          Pattern.compile("(\\d+)C"), 100).put(
          // Millenia-2001 specification
          Pattern.compile("(\\d+)L"), 1000).put(
          // Decades-2005 specification
          Pattern.compile("(\\d+)DE"), 10).put(
          // Centuries-2005 specification
          Pattern.compile("(\\d+)CE"), 100).put(
          // Millenia-2005 specification
          Pattern.compile("(\\d+)ML"), 1000).build();

  private static final PeriodFormatter ISO8601_PERIOD_PARSER = ISOPeriodFormat.standard();

  /**
   * This method makes a "best effort" to make a {@link Period} out of a TIMEX2 period string.<br>
   * TIMEX2 periods are superset of ISO 8601 periods. For example, TIMEX2 periods can have strings
   * like SU (for summer), FY (for fiscal year) or X for unspecified numbers (like PXM for
   * unspecified no. of months). This method will return Optional.absent() for any such non-ISO8601
   * Period values.<br>
   * The only special consideration is with decade,century or millenium markers (E,C,L in Timex2001;
   * CE,DE,ML in Timex2005). These will be multiplied by appropriate multipliers to get the right
   * period. For details refer to sections 4.2 and 4.3 of Timex2005 specification.
   *
   * @param periodSym period-value as Symbol
   * @return Optional {@link Period} object (see the description for when Optional.absent will be
   * returned).
   * @throws Timex2Exception if periodSym is not a valid period string (e.g. it doesn't start with
   *                         P)
   * @author rgabbard
   */
  private Optional<Period> parseDuration(final Symbol periodSym) throws Timex2Exception {
    checkNotNull(periodSym);

    final String period = periodSym.toString();
    if (!period.startsWith("P")) {
      return Optional.absent();
    }

    for (final Map.Entry<Pattern, Integer> timexExtension : periodPatternsToYearMultipliers
        .entrySet()) {
      final Matcher m = timexExtension.getKey().matcher(period);
      try {
        if (m.lookingAt()) {
          return Optional
              .of(Period.years(timexExtension.getValue() * Integer.parseInt(m.group(1))));
        }
      } catch (final NumberFormatException nfe) {
        log.warn("Failed to parse duration {}, skipping. Due to {}", period, nfe);
        return Optional.absent();
      }
    }

    try {
      return Optional.of(ISO8601_PERIOD_PARSER.parsePeriod(period));
    } catch (final IllegalArgumentException iae) {
      return Optional.absent();
    }
  }

  private static final Joiner dashJoiner = Joiner.on("-");

  @Override
  public String toString() {
    final List<String> parts = Lists.newArrayList();

    if (val != null) {
      parts.add(val.toString());
    }

    if (mod != null) {
      parts.add(mod.toString());
    }

    if (set) {
      parts.add("SET");
    }

    if (granularity != null) {
      parts.add(granularity.toString());
    }

    if (periodicity != null) {
      parts.add(periodicity.toString());
    }

    if (anchorVal != null) {
      parts.add(anchorVal.toString());
    }

    if (anchorDir != null) {
      parts.add(anchorDir.toString());
    }

    if (nonSpecific) {
      parts.add("NONSPECIFIC");
    }

    return dashJoiner.join(parts);
  }

  private Timex2Time(final Symbol val, final Modifier mod, final boolean set,
      final Symbol granularity, final Symbol periodicity, final Symbol anchorVal,
      final AnchorDirection anchorDir, final boolean nonSpecific) {
    this.val = val;
    this.mod = mod;
    if (mod != null) {
      checkNotNull(val, "Value cannot be null if mod is %s", mod);
    }
    this.set = set;
    this.granularity = granularity;
    if (granularity != null) {
      // granularity always appears in conjunction with set
      checkArgument(set);
    }
    this.periodicity = periodicity;
    if (periodicity != null) {
      // periodicity always appears in conjunction with set
      checkArgument(set);
    }
    this.anchorVal = anchorVal;
    this.anchorDir = anchorDir;
    this.nonSpecific = nonSpecific;

    if (val != null) {
      this.duration = parseDuration(val).orNull();
    } else {
      this.duration = null;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(val, mod, set, granularity, periodicity, anchorVal, anchorDir,
        nonSpecific);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Timex2Time other = (Timex2Time) obj;
    return equal(val, other.val) && equal(mod, other.mod) && equal(set, other.set)
        && equal(granularity, other.granularity) && equal(periodicity, other.periodicity)
        && equal(anchorVal, other.anchorVal) && equal(anchorDir, other.anchorDir)
        && equal(nonSpecific, other.nonSpecific);
  }

  /**
   * This method makes a "best effort" to make an {@link Interval} out of a TIMEX2 value string.<br>
   * TIMEX2 value (or date-time) strings are superset of ISO 8601 periods. For example, TIMEX2
   * values can have seasons (like SU for summer, FA for fall, etc) or periods of day (like MO for
   * morning, NI for night), or unspecified values like 1999-09-XX (See section 4.3 of the
   * specification). For such values that make the intervals fuzzy, we will try to find the interval
   * upto which the timex value is specific. For example, 1999-09-08TNI will return the interval for
   * 1999-09-08, and 1999-FA or 1999-WXX will return the interval for 1999. However, we reserve the
   * right to make specific inferences from non-ISO markers in future implementations (e.g. FA could
   * mean the period from third week of September to third week of December).<br> TIMEX2 values with
   * omissions, like VAL="199" (meaning the decade of 1990s) or VAL="20" (meaning the 21st century)
   * will be converted to approriate interval values (10 years or 100 years respectively, for this
   * example).<br> If no interval can be discerned from the value at all, for example, XXXX-09
   * (September of unspecified year) or XX63 (63rd year of unspecified year), Optional.absent() will
   * be returned.
   *
   * @param valSym timex-value as Symbol
   * @return Optional {@link Interval} object (see the description for when Optional.absent will be
   * returned).
   * @author rgabbard, msrivast
   */
  private Optional<Interval> parseValueAsInterval(Symbol valSym) {
    String val = valSym.asString();

    //first see if time of day is fuzzy (TMO, TNI, TXX etc.), and if so, get rid of that
    String timePart = "";
    if (val.contains("T")) {
      timePart = val.substring(val.indexOf("T") + 1);
      if (timePart.contains("X") || nonISOMarkersPattern.matcher(timePart).find()) {
        timePart = "";
      }
      val = val.substring(0, val.indexOf("T"));
    }
    val = val + (!timePart.equals("") ? "T" + timePart : "");

    //now get rid of unspecified markers (X) or weekend, season, FY etc. markers.
    //Timex2 standard says that it extends ISO 8601 for vals which means basic ISO 8601 format
    //may also be allowed. All the examples that I have seen have always had hyphens tho.
    //Therefore, for simplicity, we will split val on hyphens ~msrivast
    String[] valParts = val.split("-");
    ImmutableList.Builder<String> specificValParts = ImmutableList.builder();
    for (int i = 0; i < valParts.length; i++) {
      String part = valParts[i];
      //if part contains X or is a season, fiscal year, periods of day marker etc. break, since the
      //timex value can be specific only upto here
      if (part.contains("X") || nonISOMarkers.contains(Symbol.from(part))) {
        break;
      }
      specificValParts.add(part);
    }
    val = Joiner.on("-").join(specificValParts.build());

    // it could be a simple date which we can extract
    // directly (very common; needs to come after the above because
    // its regex will match week-based dates, too)
    try {
      return Optional.of(ISODateTimeFormat.yearMonthDay().parseDateTime(val).dayOfMonth()
          .toInterval());
    } catch (final IllegalArgumentException iae) {
      // pass
    }

    // could be YYYY-MM type date
    try {
      return Optional.of(ISODateTimeFormat.yearMonth()
          .parseDateTime(val).monthOfYear().toInterval());
    } catch (final IllegalArgumentException iae) {
      // pass
    }

    // could be a YYYY type date
    try {
      return Optional.of(ISODateTimeFormat.year()
          .parseDateTime(val).year().toInterval());
    } catch (final IllegalArgumentException iae) {
      // pass
    }

    // it could be a 2014-W3-01 style week-based date
    try {
      return Optional.of(ISODateTimeFormat.weekyearWeekDay()
          .parseDateTime(val).dayOfMonth().toInterval());
    } catch (final IllegalArgumentException iae) {
      // it's okay if it's unparseable; just wasn't in this format
    }

    // it would be a 2014-W3 style week-based date
    try {
      return Optional.of(ISODateTimeFormat.weekyearWeek()
          .parseDateTime(val).weekOfWeekyear().toInterval());
    } catch (final IllegalArgumentException iae) {
      // it's okay if it's unparseable; just wasn't in this format
    }

    // this needs to go last or it would short-circuit the week
    // formats
    try {
      return Optional.of(ISODateTimeFormat.dateTimeParser()
          .parseDateTime(val).minuteOfDay().toInterval());
    } catch (final IllegalArgumentException iae) {
      // pass
    }

    // value was not parseable as a joda time Interval--see if the first one, two or three
    // characters of val
    // are digits, which would address the cases like 1,19,199
    String digits = "";
    for (int i = 0; i < 3 && i < val.length() && Character.isDigit(val.charAt(i)); i++) {
      digits += val.substring(i, i + 1);
    }
    if (!digits.equals("")) {
      int period = (int) Math.pow(10, 4 - digits.length());
      int startYear = Integer.parseInt(digits) * period;
      int endYear = startYear + period;
      return Optional.of(Interval.parse(startYear + "/" + endYear));
    }

    log.warn("Value not parseable as interval: {}", val);

    return Optional.absent();
  }

  private Optional<Interval> parseValueAsInterval(String val) {
    return parseValueAsInterval(Symbol.from(val));
  }

  /**
   * @deprecated Use {@link #builder()} and {@link Builder#withVal(Symbol)} instead
   */
  @Deprecated
  public static Builder builderWithValue(Symbol val) {
    return new Builder().withVal(val);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {
    }

    private Symbol val;
    private Modifier mod = null;
    private boolean isSet = false;
    private Symbol granularity = null;
    private Symbol periodicity = null;
    private Symbol anchorVal = null;
    private AnchorDirection anchorDir = null;
    private boolean nonSpecific = false;

    public Timex2Time build() {
      return new Timex2Time(val, mod, isSet, granularity, periodicity, anchorVal, anchorDir,
          nonSpecific);
    }

    public Builder withVal(Symbol val) {
      this.val = checkNotNull(val);
      return this;
    }

    public Builder withModifier(Modifier mod) {
      this.mod = checkNotNull(mod);
      return this;
    }

    public Builder withModifierFromString(String mod) {
      return withModifier(Modifier.valueOf(checkNotNull(mod)));
    }

    public Builder withIsSetFromTimexBoolean(Symbol timexBoolean) {
      this.isSet = parseYES(timexBoolean);
      return this;
    }

    public Builder withIsSet(boolean isSet) {
      this.isSet = isSet;
      return this;
    }

    public Builder withGranularity(Symbol granularity) {
      this.granularity = checkNotNull(granularity);
      return this;
    }

    public Builder withPeriodicity(Symbol periodicity) {
      this.periodicity = checkNotNull(periodicity);
      return this;
    }

    public Builder withAnchorValue(Symbol anchorValue) {
      this.anchorVal = checkNotNull(anchorValue);
      return this;
    }

    public Builder withAnchorDirection(Symbol anchorDir) {
      checkNotNull(anchorDir);
      this.anchorDir = AnchorDirection.valueOf(anchorDir.asString());
      return this;
    }

    public Builder withAnchorDirectionFromEnum(AnchorDirection anchorDir) {
      this.anchorDir = checkNotNull(anchorDir);
      return this;
    }

    public Builder setNonSpecificFromTimexBoolean(Symbol timexBoolean) {
      this.nonSpecific = parseYES(timexBoolean);
      return this;
    }

    public Builder setNonSpecific(boolean nonSpecific) {
      this.nonSpecific = nonSpecific;
      return this;
    }
  }

}
