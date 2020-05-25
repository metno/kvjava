package no.met.kvalobs.kl;

import no.met.kvutil.dbutil.DbConnection;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;


public class FilterCache {

    static class Kv2KlimaKey {
        long stationid;
        long typeid;

        Kv2KlimaKey( long sid, long tid ) {
            stationid = sid;
            typeid=tid;
        }

        @Override
        public int hashCode(){
            int sid =(int) (stationid % 999983); // 999983 is a prime
            int tid =(int) (typeid % 997);  // 997 is a prime
            return sid*1000+tid;
        }

        @Override
        public boolean equals(Object o) {
            if( o == null )
                return false;

            if( !(o instanceof Kv2KlimaKey))
                return false;

            Kv2KlimaKey k = (Kv2KlimaKey) o;

            if( k.stationid==stationid && k.typeid == typeid)
                return true;
            else
                return false;
        }

    }

    //We clear the cache each 6'th hour. This to
    //take changes in the filter tables in use.
    final int clearCacheIntervalInHours=6;  // Should be less than 24 hour.
    OffsetDateTime clearCacheAt;
    Map<Kv2KlimaKey, Kv2KlimaFilter> kv2klimaFilters;
    Map<Long, ParamFilter> paramFilters;
    Matrics matrics;

    void initCleanCache() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int h = now.getHour();
        h = clearCacheIntervalInHours - (h % clearCacheIntervalInHours);
        clearCacheAt = now.plusHours(h).truncatedTo(ChronoUnit.HOURS);
        System.err.println("Init ClearCacheAt: " + clearCacheAt.toString());
    }

    void cleanCache() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if( now.isAfter(clearCacheAt)) {
            clearCacheAt = clearCacheAt.plusHours(6);
            kv2klimaFilters.clear();
            paramFilters.clear();
            matrics.clear();
        }
    }


    public FilterCache( int cacheSize, Path matricsFile ) {
        kv2klimaFilters = new LRUCache<Kv2KlimaKey, Kv2KlimaFilter>(cacheSize);
        paramFilters=new LRUCache<Long,ParamFilter>(cacheSize);
        matrics=new Matrics(matricsFile);
        initCleanCache();
    }

    // For test
    Kv2KlimaFilter getKv2KlimaFilter(long stationid, long typeid) {
        return kv2klimaFilters.get(new Kv2KlimaKey(stationid,typeid));
    }


    synchronized public Kv2KlimaFilter getKv2KlimaFilter(long stationid, long typeid, DbConnection con) {
        Kv2KlimaKey key = new Kv2KlimaKey(stationid,typeid);
        Kv2KlimaFilter f = kv2klimaFilters.get(key);
        if( f == null ) {
            f = new Kv2KlimaFilter(stationid, typeid, con);

            if( f.isOk() ) {//Only add it to the cache if it is loaded from the database.
                kv2klimaFilters.put(key, f);
                matrics.filterCount(false);
            }
        } else {
            matrics.filterCount(true);
        }
        cleanCache();
        return f;
    }

    synchronized  public ParamFilter getParamFilter(long stationID) {
        ParamFilter f = paramFilters.get(stationID);
        if( f == null ) {
            f = new ParamFilter(stationID, matrics);
            paramFilters.put(stationID, f);
        }
        cleanCache();
        return f;
    }
}
