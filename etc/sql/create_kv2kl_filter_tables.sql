-- Creates the tables T_KV2KLIMA_FILTER, T_KV2KLIMA_PARAM_FILTER and
-- T_KV2KLIMA_TYPEID_PARAM_FILTER. This tables is used to filter the
-- data form kvalobs to klima. 
--
-- The filter function is implemented
-- in met.no.kvalobs.kl.Kv2KlimaFilter, met.no.kvalobs.kl.ParamFilter and
-- met.no.kvalobs.kl.Filter.
--
-- This file is to be used wit hsqldb
-- The file must be kept in sync with create_kv2klima.sql.


-- Creates the tables used by kv2kl to insert and filter data from kvalobs.
-- The data tables are KV2KLIMA and T_TEXT_DATA. The filter tables
-- used to filter data from kvalobs before inserting into the data tables
-- are T_KV2KLIMA_FILTER, T_KV2KLIMA_PARAM_FILTER and
-- T_KV2KLIMA_TYPEID_PARAM_FILTER.
--
-- The filter function is implemented
-- in metno.kvalobs.kl.Kv2KlimaFilter, metno.kvalobs.kl.ParamFilter and
-- metno.kvalobs.kl.Filter.
--
-- NB NB NB !!!!!!!!!
-- This is the schemas for HSQLDB.
-- For test: All Audit information is filled with default values.
--

-- As defined in the oracle database.
-- CREATE TABLE KV2KLIMA (
--  STNR        NUMBER(12),
--  DATO        DATE,
--  TYPEID      NUMBER(4),
--  PARAMID     NUMBER(5),
--  SENSOR      NUMBER(1),
--  XLEVEL      NUMBER(6),
--  ORIGINAL    NUMBER(13,3),
--  CORRECTED   NUMBER(13,3),
--  USEINFO     VARCHAR2(16),
--  PARA_NAME   VARCHAR2(15),
--  YEAR        NUMBER(4),
--  KVSTAMP     DATE,
--  KLSTAMP     DATE,
--  OLD_NAME    VARCHAR2(15),
--  CONTROLINFO VARCHAR2(16),
--  CFAILED     VARCHAR2(300)
-- )


-- Standard SQL
CREATE TABLE KV2KLIMA (
 STNR        NUMERIC(12),
 DATO        TIMESTAMP,
 TYPEID      NUMERIC(4),
 PARAMID     NUMERIC(5),
 SENSOR      NUMERIC(1),
 XLEVEL      NUMERIC(6),
 ORIGINAL    NUMERIC(13,3),
 CORRECTED   NUMERIC(13,3),
 USEINFO     VARCHAR(16),
 PARA_NAME   VARCHAR(15),
 YEAR        NUMERIC(4),
 KVSTAMP     TIMESTAMP,
 KLSTAMP     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 OLD_NAME    VARCHAR(15),
 CONTROLINFO VARCHAR(16),
 CFAILED     VARCHAR(300)
);

-- CREATE INDEX I_KV2KLIMA ON KV2KLIMA (STNR, DATO, TYPEID, PARAMID, SENSOR,
--                                     XLEVEL, YEAR);


--
-- As defined in the oracle database.
--
-- CREATE TABLE T_TEXT_DATA (
--  STATIONID NUMBER(38),
--  OBSTIME   DATE,
--  ORIGINAL  VARCHAR2(200),
--  PARAMID   NUMBER(15),
--  TBTIME    DATE,
--  TYPEID    NUMBER(5)
-- )


-- Standard SQL
CREATE TABLE T_TEXT_DATA (
 STATIONID NUMERIC(38),
 OBSTIME   TIMESTAMP,
 ORIGINAL  VARCHAR(200),
 PARAMID   NUMERIC(15),
 TBTIME    TIMESTAMP,
 TYPEID    NUMERIC(5)
);


-- CREATE INDEX I_TEXT_DATA ON T_TEXT_DATA (STATIONID, OBSTIME, PARAMID, TYPEID);

CREATE TABLE T_KV2KLIMA_FILTER (
 STNR       NUMERIC(10) NOT NULL,
 STATUS     CHARACTER(1) NOT NULL,
 FDATO      TIMESTAMP,
 TDATO      TIMESTAMP,
 TYPEID     NUMERIC(4) NOT NULL,
 NYTT_STNR  NUMERIC(5),
 AUDIT_DATO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 AUDIT_USER VARCHAR(50) DEFAULT 'test',
 ID         NUMERIC(10) DEFAULT 0
);

CREATE TABLE T_KV2KLIMA_PARAM_FILTER (
 STNR       NUMERIC(10) NOT NULL,
 TYPEID     NUMERIC(4) NOT NULL,
 PARAMID    NUMERIC(5) NOT NULL,
 SENSOR     NUMERIC(1) NOT NULL,
 XLEVEL     NUMERIC(3) NOT NULL,
 FDATO      TIMESTAMP  DEFAULT '1900-01-01 00:00:00',
 TDATO      TIMESTAMP,
 AUDIT_DATO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 AUDIT_USER VARCHAR(15) DEFAULT 'test',
 ID         NUMERIC(10) DEFAULT 0
);

-- CREATE INDEX T_KV2KLIMA_PARAM_FILTER_TYPEID_INDEX ON T_KV2KLIMA_PARAM_FILTER (stnr,typeid);


CREATE TABLE T_KV2KLIMA_TYPEID_PARAM_FILTER (
 TYPEID  NUMERIC(4) NOT NULL,
 PARAMID NUMERIC(5) NOT NULL,
 SENSOR  NUMERIC(1) NOT NULL,
 XLEVEL  NUMERIC(1) NOT NULL,
 FDATO   TIMESTAMP,
 TDATO   TIMESTAMP
);

-- CREATE INDEX T_KV2KLIMA_TYPEID_PARAM_FILTER_TYPEID_INDEX ON T_KV2KLIMA_TYPEID_PARAM_FILTER (typeid);
