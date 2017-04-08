/*******************************************************************************
 *    ___                  _   ____  ____
 *   / _ \ _   _  ___  ___| |_|  _ \| __ )
 *  | | | | | | |/ _ \/ __| __| | | |  _ \
 *  | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *   \__\_\\__,_|\___||___/\__|____/|____/
 *
 * Copyright (C) 2014-2017 Appsicle
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.questdb.std.time;

import com.questdb.ex.NumericException;
import com.questdb.test.tools.TestUtils;
import com.questdb.txt.sink.StringSink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DatesTest {

    private final StringSink sink = new StringSink();

    @Test(expected = NumericException.class)
    public void parseBrokenInterval1() throws Exception {
        Dates.parseInterval("2016-01-01T10:25:32.34");
    }

    @Test(expected = NumericException.class)
    public void parseBrokenInterval2() throws Exception {
        Dates.parseInterval("2016-01-01T1");
    }

    @Before
    public void setUp() {
        sink.clear();
    }

    @Test
    public void testAddDaysPrevEpoch() throws Exception {
        long millis = Dates.parseDateTime("1888-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.addDays(millis, 24));
        TestUtils.assertEquals("1888-06-05T23:45:51.045Z", sink);
    }

    @Test
    public void testAddMonths() throws Exception {
        long millis = Dates.parseDateTime("2008-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.addMonths(millis, -10));
        TestUtils.assertEquals("2007-07-12T23:45:51.045Z", sink);
    }

    @Test
    public void testAddMonthsPrevEpoch() throws Exception {
        long millis = Dates.parseDateTime("1888-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.addMonths(millis, -10));
        TestUtils.assertEquals("1887-07-12T23:45:51.045Z", sink);
    }

    @Test
    public void testAddYears() throws Exception {
        long millis = Dates.parseDateTime("1988-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.addYear(millis, 10));
        TestUtils.assertEquals("1998-05-12T23:45:51.045Z", sink);
    }

    @Test
    public void testAddYearsPrevEpoch() throws Exception {
        long millis = Dates.parseDateTime("1888-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.addYear(millis, 10));
        TestUtils.assertEquals("1898-05-12T23:45:51.045Z", sink);
    }

    @Test
    public void testCeilDD() throws Exception {
        long millis = Dates.parseDateTime("2008-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.ceilDD(millis));
        TestUtils.assertEquals("2008-05-12T23:59:59.999Z", sink);
    }

    @Test
    public void testCeilDDPrevEpoch() throws Exception {
        long millis = Dates.parseDateTime("1888-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.ceilDD(millis));
        TestUtils.assertEquals("1888-05-12T23:59:59.999Z", sink);
    }

    @Test
    public void testCeilMM() throws Exception {
        long millis = Dates.parseDateTime("2008-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.ceilMM(millis));
        TestUtils.assertEquals("2008-05-31T23:59:59.999Z", sink);
    }

    @Test
    public void testCeilYYYY() throws Exception {
        long millis = Dates.parseDateTime("2008-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.ceilYYYY(millis));
        TestUtils.assertEquals("2008-12-31T23:59:59.999Z", sink);
    }

    @Test
    public void testFloorDD() throws Exception {
        long millis = Dates.parseDateTime("2008-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.floorDD(millis));
        TestUtils.assertEquals("2008-05-12T00:00:00.000Z", sink);
    }

    @Test
    public void testFloorHH() throws Exception {
        long millis = Dates.parseDateTime("2008-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.floorHH(millis));
        TestUtils.assertEquals("2008-05-12T23:00:00.000Z", sink);
    }

    @Test
    public void testFloorMM() throws Exception {
        long millis = Dates.parseDateTime("2008-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.floorMM(millis));
        TestUtils.assertEquals("2008-05-01T00:00:00.000Z", sink);
    }

    @Test
    public void testFloorYYYY() throws Exception {
        long millis = Dates.parseDateTime("2008-05-12T23:45:51.045Z");
        Dates.appendDateTime(sink, Dates.floorYYYY(millis));
        TestUtils.assertEquals("2008-01-01T00:00:00.000Z", sink);
    }

    @Test
    public void testFormatCalDate1() throws Exception {
        Dates.formatDashYYYYMMDD(sink, Dates.parseDateTime("2008-05-10T12:31:02.008Z"));
        TestUtils.assertEquals("2008-05-10", sink);
    }

    @Test
    public void testFormatCalDate2() throws Exception {
        Dates.formatYYYYMM(sink, Dates.parseDateTime("2008-05-10T12:31:02.008Z"));
        TestUtils.assertEquals("2008-05", sink);
    }

    @Test
    public void testFormatCalDate3() throws Exception {
        Dates.formatYYYYMMDD(sink, Dates.parseDateTime("2008-05-10T12:31:02.008Z"));
        TestUtils.assertEquals("20080510", sink);
    }

    @Test
    public void testFormatDateTime() throws Exception {
        assertTrue("2014-11-30T12:34:55.332Z");
        assertTrue("2008-03-15T11:22:30.500Z");
        assertTrue("1917-10-01T11:22:30.500Z");
        assertTrue("0900-01-01T01:02:00.005Z");
    }

    @Test
    public void testFormatHTTP() throws Exception {
        Dates.formatHTTP(sink, Dates.parseDateTime("2015-12-05T12:34:55.332Z"));
        TestUtils.assertEquals("Sat, 5 Dec 2015 12:34:55 GMT", sink);
    }

    @Test
    public void testFormatHTTP2() throws Exception {
        Dates.formatHTTP(sink, Dates.parseDateTime("2015-12-05T12:04:55.332Z"));
        TestUtils.assertEquals("Sat, 5 Dec 2015 12:04:55 GMT", sink);
    }

    @Test
    public void testFormatMMMDYYYY() throws NumericException {
        Dates.formatMMMDYYYY(sink, Dates.parseDateTime("2008-05-10T12:31:02.008Z"));
        TestUtils.assertEquals("May 10 2008", sink);
    }

    @Test
    public void testIntervalParse() throws Exception {
        TestUtils.assertEquals("Interval{lo=2015-01-01T00:00:00.000Z, hi=2015-12-31T23:59:59.999Z}", Dates.parseInterval("2015").toString());
        TestUtils.assertEquals("Interval{lo=2014-03-01T00:00:00.000Z, hi=2014-03-31T23:59:59.999Z}", Dates.parseInterval("2014-03").toString());
        TestUtils.assertEquals("Interval{lo=2013-05-10T00:00:00.000Z, hi=2013-05-10T23:59:59.999Z}", Dates.parseInterval("2013-05-10").toString());
        TestUtils.assertEquals("Interval{lo=2014-07-10T14:00:00.000Z, hi=2014-07-10T14:59:59.999Z}", Dates.parseInterval("2014-07-10T14").toString());
        TestUtils.assertEquals("Interval{lo=2014-09-22T10:15:00.000Z, hi=2014-09-22T10:15:59.999Z}", Dates.parseInterval("2014-09-22T10:15").toString());
        TestUtils.assertEquals("Interval{lo=2013-08-06T08:25:30.000Z, hi=2013-08-06T08:25:30.999Z}", Dates.parseInterval("2013-08-06T08:25:30").toString());
    }

    @Test(expected = NumericException.class)
    public void testIntervalParseBadValue() throws Exception {
        Dates.parseInterval("2014-31");

    }

    @Test
    public void testLeapYearIntervals() throws Exception {
        TestUtils.assertEquals("Interval{lo=2016-01-01T00:00:00.000Z, hi=2016-12-31T23:59:59.999Z}", Dates.parseInterval("2016").toString());
        TestUtils.assertEquals("Interval{lo=2016-02-01T00:00:00.000Z, hi=2016-02-29T23:59:59.999Z}", Dates.parseInterval("2016-02").toString());
        TestUtils.assertEquals("Interval{lo=2016-02-29T00:00:00.000Z, hi=2016-02-29T23:59:59.999Z}", Dates.parseInterval("2016-02-29").toString());
        TestUtils.assertEquals("Interval{lo=2016-02-29T14:00:00.000Z, hi=2016-02-29T14:59:59.999Z}", Dates.parseInterval("2016-02-29T14").toString());
        TestUtils.assertEquals("Interval{lo=2016-02-29T10:15:00.000Z, hi=2016-02-29T10:15:59.999Z}", Dates.parseInterval("2016-02-29T10:15").toString());
        TestUtils.assertEquals("Interval{lo=2016-02-29T08:25:30.000Z, hi=2016-02-29T08:25:30.999Z}", Dates.parseInterval("2016-02-29T08:25:30").toString());
    }

    @Test
    public void testNExtOrSameDow3() throws Exception {
        // thursday
        long millis = Dates.parseDateTime("2017-04-06T00:00:00.000Z");
        Dates.appendDateTime(sink, Dates.nextOrSameDayOfWeek(millis, 4));
        TestUtils.assertEquals("2017-04-06T00:00:00.000Z", sink);
    }

    @Test
    public void testNextOrSameDow1() throws Exception {
        // thursday
        long millis = Dates.parseDateTime("2017-04-06T00:00:00.000Z");
        Dates.appendDateTime(sink, Dates.nextOrSameDayOfWeek(millis, 3));
        TestUtils.assertEquals("2017-04-12T00:00:00.000Z", sink);
    }

    @Test
    public void testNextOrSameDow2() throws Exception {
        // thursday
        long millis = Dates.parseDateTime("2017-04-06T00:00:00.000Z");
        Dates.appendDateTime(sink, Dates.nextOrSameDayOfWeek(millis, 6));
        TestUtils.assertEquals("2017-04-08T00:00:00.000Z", sink);
    }

    @Test
    public void testOverflowDate() throws Exception {
        Assert.assertEquals("4509540-01-02T00:00:00.000Z", Dates.toString(142245170150400000L));
    }

    @Test
    public void testParseBadISODate() throws Exception {
        expectExceptionDateTime("201");
        expectExceptionDateTime("2014");
        expectExceptionDateTime("2014-");
        expectExceptionDateTime("2014-0");
        expectExceptionDateTime("2014-03");
        expectExceptionDateTime("2014-03-");
        expectExceptionDateTime("2014-03-1");
        expectExceptionDateTime("2014-03-10");
        expectExceptionDateTime("2014-03-10T0");
        expectExceptionDateTime("2014-03-10T01");
        expectExceptionDateTime("2014-03-10T01-");
        expectExceptionDateTime("2014-03-10T01:1");
        expectExceptionDateTime("2014-03-10T01:19");
        expectExceptionDateTime("2014-03-10T01:19:");
        expectExceptionDateTime("2014-03-10T01:19:28.");
        expectExceptionDateTime("2014-03-10T01:19:28.2");
        expectExceptionDateTime("2014-03-10T01:19:28.255K");
    }

    @Test
    public void testParseDateFmt2() throws Exception {
        long date = Dates.parseDateTimeFmt2("02/24/2014");
        TestUtils.assertEquals("2014-02-24T00:00:00.000Z", Dates.toString(date));
    }

    @Test
    public void testParseDateFmt3() throws Exception {
        long date = Dates.parseDateTimeFmt3("24/02/2014");
        TestUtils.assertEquals("2014-02-24T00:00:00.000Z", Dates.toString(date));
    }

    @Test
    public void testParseDateTime() throws Exception {
        String date = "2008-02-29T10:54:01.010Z";
        Dates.appendDateTime(sink, Dates.parseDateTime(date));
        TestUtils.assertEquals(date, sink);
    }

    @Test
    public void testParseDateTimePrevEpoch() throws Exception {
        String date = "1812-02-29T10:54:01.010Z";
        Dates.appendDateTime(sink, Dates.parseDateTime(date));
        TestUtils.assertEquals(date, sink);
    }

    @Test(expected = NumericException.class)
    public void testParseWrongDay() throws Exception {
        Dates.parseDateTime("2013-09-31T00:00:00.000Z");
    }

    @Test(expected = NumericException.class)
    public void testParseWrongHour() throws Exception {
        Dates.parseDateTime("2013-09-30T24:00:00.000Z");
    }

    @Test(expected = NumericException.class)
    public void testParseWrongMillis() throws Exception {
        Dates.parseDateTime("2013-09-30T22:04:34.1024Z");
    }

    @Test(expected = NumericException.class)
    public void testParseWrongMinute() throws Exception {
        Dates.parseDateTime("2013-09-30T22:61:00.000Z");
    }

    @Test(expected = NumericException.class)
    public void testParseWrongMonth() throws Exception {
        Dates.parseDateTime("2013-00-12T00:00:00.000Z");
    }

    @Test(expected = NumericException.class)
    public void testParseWrongSecond() throws Exception {
        Dates.parseDateTime("2013-09-30T22:04:60.000Z");
    }

    @Test
    public void testPreviousOrSameDow1() throws Exception {
        // thursday
        long millis = Dates.parseDateTime("2017-04-06T00:00:00.000Z");
        Dates.appendDateTime(sink, Dates.previousOrSameDayOfWeek(millis, 3));
        TestUtils.assertEquals("2017-04-05T00:00:00.000Z", sink);
    }

    @Test
    public void testPreviousOrSameDow2() throws Exception {
        // thursday
        long millis = Dates.parseDateTime("2017-04-06T00:00:00.000Z");
        Dates.appendDateTime(sink, Dates.previousOrSameDayOfWeek(millis, 6));
        TestUtils.assertEquals("2017-04-01T00:00:00.000Z", sink);
    }

    @Test
    public void testPreviousOrSameDow3() throws Exception {
        // thursday
        long millis = Dates.parseDateTime("2017-04-06T00:00:00.000Z");
        Dates.appendDateTime(sink, Dates.previousOrSameDayOfWeek(millis, 4));
        TestUtils.assertEquals("2017-04-06T00:00:00.000Z", sink);
    }

    @Test
    public void testDayOfWeek() throws Exception {
        long millis = Dates.parseDateTime("1893-03-19T17:16:30.192Z");
        Assert.assertEquals(7, Dates.getDayOfWeek(millis));
        Assert.assertEquals(1, Dates.getDayOfWeekSundayFirst(millis));
        millis = Dates.parseDateTime("2017-04-09T17:16:30.192Z");
        Assert.assertEquals(7, Dates.getDayOfWeek(millis));
        Assert.assertEquals(1, Dates.getDayOfWeekSundayFirst(millis));
    }

    private void assertTrue(String date) throws NumericException {
        Dates.appendDateTime(sink, Dates.parseDateTime(date));
        TestUtils.assertEquals(date, sink);
        sink.clear();
    }

    private void expectExceptionDateTime(String s) {
        try {
            Dates.parseDateTime(s);
            Assert.fail("Expected exception");
        } catch (NumericException ignore) {
        }
    }
}
