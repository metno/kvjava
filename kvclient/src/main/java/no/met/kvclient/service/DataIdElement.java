package no.met.kvclient.service;

import no.met.kvclient.datasource.Data;
import no.met.kvutil.DateTimeUtil;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;

/**
 * Created by borgem on 11.11.16.
 */
public class DataIdElement {
    public long stationID;
    public long typeID;
    public Instant obstime;

    public static Comparator<DataIdElement> cmp = (DataIdElement d1,DataIdElement d2) -> {
        int t=d1.obstime.compareTo(d2.obstime);
        if( t==0 ) {
            if( d1.stationID == d2.stationID )
                return Long.compare(d1.typeID, d2.typeID);
            else
                return Long.compare(d1.stationID,d2.stationID);
        } else {
            return t;
        }
    };

    public DataIdElement(long stationID, long typeID, Instant obstime) {
        this.stationID=stationID;
        this.typeID=typeID;
        this.obstime=obstime;
    }


    public String getObsTime() {
        OffsetDateTime tb=obstime.atOffset(ZoneOffset.UTC);
        return DateTimeUtil.toString(tb, DateTimeUtil.FMT_PARSE_xx);
    }


    public String getQuotedObsTime() {
        return "'" + getObsTime()+"'";
    }

}
