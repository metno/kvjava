package no.met.kvutil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import junit.framework.*;



public class DateTimeUtilTest extends TestCase {
    public void setUp(){

    }

    public void tearDown(){
    }


    public void testAddMicrodeconds(){
        String toTest="2017-10-16 13:20:08";
        OffsetDateTime tt=DateTimeUtil.parse(toTest);

        System.out.println("Before: " + DateTimeUtil.toString(tt, DateTimeUtil.ISO_WITH_MICRO_SECOND_xx));
        OffsetDateTime t=DateTimeUtil.plusMicosecond(tt, 1);
        System.out.println("After: " + DateTimeUtil.toString(t, DateTimeUtil.ISO_WITH_MICRO_SECOND_xx));
        long d=tt.until(t, ChronoUnit.MICROS);
        assertEquals(d, 1);
    }

}
