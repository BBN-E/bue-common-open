package com.bbn.bue.common.temporal;

import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.temporal.Timex2Time.Modifier;

import org.joda.time.Interval;

import static com.bbn.bue.common.temporal.Timex2Time.AS_OF;
import static com.bbn.bue.common.temporal.Timex2Time.BEFORE;

// examples withAdditionalJustifications "Timex2 Quick Guide: http://www.timeml.org/site/terqas/documentation/timex2-quick-guide_10may02.doc
@SuppressWarnings("deprecation")
public final class TestTimex {

  // <TIMEX2 VAL="1991-10-06">October 6, 1991</TIMEX2>
  static final Timex2Time October61991 = Timex2Time.from("1991-10-06");
  //<TIMEX2 VAL="1993">1993</TIMEX2>
  static final Timex2Time year1993 = Timex2Time.from("1993");
  //<TIMEX2 VAL="1993-08-01T17:00">5:00 p.m.</TIMEX2>
  static final Timex2Time time19930801at5PM = Timex2Time.from("1993-08-01T17:00");
  //<TIMEX2 VAL="1992-10">last October</TIMEX2>
  static final Timex2Time lastOctober = Timex2Time.from("1992-10");
  // <TIMEX2 VAL="1992-FA">last autumn</TIMEX2>
  static final Timex2Time lastAutumn = Timex2Time.from("1992-FA");
  //<TIMEX2 VAL="P9M" ANCHOR_VAL="1993-08" ANCHOR_DIR="BEFORE">the last nine months</TIMEX2>
  static final Timex2Time theLastNineMonthsSpanningYear = Timex2Time.from("P9M")
      .anchoredCopy("1993-08", BEFORE);
  //<TIMEX2 VAL="P9M" ANCHOR_VAL="1993-10" ANCHOR_DIR="BEFORE">the last nine months</TIMEX2>
  static final Timex2Time theLastNineMonthsSameYear = Timex2Time.from("P9M")
      .anchoredCopy("1993-10", BEFORE);
  // <TIMEX2 SET="YES" PERIODICITY="F1D" GRANULARITY="G1D">daily</TIMEX2>
  static final Timex2Time daily = Timex2Time.createSet("F1D", "G1D");
  // <TIMEX2 VAL="P1Y" MOD="LESS_THAN">less than a year</TIMEX2>
  static final Timex2Time lessThanAYear = Timex2Time.from("P1Y").modifiedCopy(Modifier.LESS_THAN);
  // <TIMEX2 VAL="PXY" ANCHOR_VAL="1993" ANCHOR_DIR="BEFORE">recent
  // years</TIMEX2>
  static final Timex2Time recentYears = Timex2Time.from("PXY").anchoredCopy("1993", BEFORE);
  // <TIMEX2>two days after assassination attempt</TIMEX2>
  static final Timex2Time twoDaysAfterAssasinationAttempt = Timex2Time.createEmpty();
  // <TIMEX2 VAL="1994-01-20TEV">Thursday evening</TIMEX2>
  static final Timex2Time ThursdayEvening = Timex2Time.from("1994-01-20TEV");
  // <TIMEX2 VAL="1992" MOD="MID">mid-1992</TIMEX2>
  static final Timex2Time mid1992 = Timex2Time.from("1992").modifiedCopy(Modifier.MID);
  // <TIMEX2 VAL="PRESENT_REF" ANCHOR_VAL="1994-01-21T08:29"
  // ANCHOR_DIR="AS_OF">now</TIMEX2>
  static final Timex2Time nowInstantIn1994 =
      Timex2Time.present().anchoredCopy("1994-01-21T08:29", AS_OF);
  // <TIMEX2 VAL="1994-08-10" MOD="END">late Wednesday</TIMEX2>
  static final Timex2Time lateWednesday = Timex2Time.from("1994-08-10").modifiedCopy(Modifier.END);
  // <TIMEX2 VAL="P25Y">25-year</TIMEX2>
  static final Timex2Time time25Year = Timex2Time.from("P25Y");
  // <TIMEX2 VAL="PAST_REF" ANCHOR_VAL="1995-03-03T06:23"
  // ANCHOR_DIR="BEFORE">recently</TIMEX2>
  static final Timex2Time recently = Timex2Time.past().anchoredCopy("1995-03-03T06:23", BEFORE);
  // <TIMEX2 VAL="1995-W10">this week</TIMEX2>
  static final Timex2Time thisWeek = Timex2Time.from("1995-W10");
  // <TIMEX2 VAL="P1M" ANCHOR_VAL="1995-03" ANCHOR_DIR="BEFORE">the past
  // month</TIMEX2>
  static final Timex2Time pastMonth = Timex2Time.from("P1M").anchoredCopy("1995-03", BEFORE);
  // <TIMEX2 VAL="PRESENT_REF" ANCHOR_VAL="1995-03-09T02:35"
  // ANCHOR_DIR="AS_OF">current</TIMEX2>
  static final Timex2Time current = Timex2Time.present().anchoredCopy("1995-03-09T02:35", AS_OF);
  // <TIMEX2 VAL="1996-11-17T17:07">November 17, 1996 17:07 GMT</TIMEX2>
  static final Timex2Time November1719961707GMT = Timex2Time.from("1996-11-17T17:07");
  // <TIMEX2 VAL="P5D">five</TIMEX2>
  static final Timex2Time five = Timex2Time.from("P5D");
  // <TIMEX2 VAL="1996-11-16TNI">the night</TIMEX2>
  static final Timex2Time night = Timex2Time.from("1996-11-16TNI");
  // <TIMEX2 VAL="1996-11-17TDT">day light Sunday</TIMEX2>
  static final Timex2Time day = Timex2Time.from("1996-11-17TDT");
  // <TIMEX2 VAL="P7.5Y">seven-and-a-half years in prison</TIMEX2>
  static final Timex2Time sevenAndAHalfYears = Timex2Time.from("P7.5Y");
  // <TIMEX2 VAL="PT4H">four-hour</TIMEX2>
  static final Timex2Time fourHour = Timex2Time.from("PT4H");
  // <TIMEX2 SET="YES" PERIODICITY="F1Y" GRANULARITY="G1Y">a year</TIMEX2>
  static final Timex2Time aYear = Timex2Time.createSet("F1Y", "G1Y");
  // <TIMEX2 SET="YES" PERIODICITY="F.5M"
  // GRANULARITY="G.5M">twice-monthly</TIMEX2>
  static final Timex2Time twiceMonthly = Timex2Time.createSet("F.5M", "G1Y");
  //Timex value with FUTURE_REF
  static final Timex2Time futureRef = Timex2Time.future();
  //Timex value with omitted year, e.g. "199"
  static final Interval intervalWithOmittedYear =
      Timex2Time.builder().withVal(Symbol.from("199")).build().valueAsInterval().get();
  //Timex value with season marker, e.g. "1999-FA"
  static final Interval intervalWithSeason =
      Timex2Time.builder().withVal(Symbol.from("1999-FA")).build().valueAsInterval().get();
}
