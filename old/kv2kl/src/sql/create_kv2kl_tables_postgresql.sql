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
-- This is the schemas for postgresql.
--

CREATE TABLE KV2KLIMA
(
  STNR         NUMERIC(12),
  DATO         TIMESTAMP,
  TYPEID       NUMERIC(4),
  PARAMID      NUMERIC(4),
  SENSOR       NUMERIC(1),
  XLEVEL       NUMERIC(2),
  ORIGINAL     NUMERIC(13,1),
  CORRECTED    NUMERIC(13,1),
  USEINFO      VARCHAR(16),
  PARA_NAME    VARCHAR(15),
  YEAR         NUMERIC(4),
  KVSTAMP      TIMESTAMP,
  KLSTAMP      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  OLD_NAME     VARCHAR(15),
  CONTROLINFO  VARCHAR(16),
  CFAILED      VARCHAR(200)
);

CREATE INDEX I_KV2KLIMA ON KV2KLIMA (STNR, DATO, TYPEID, PARAMID, SENSOR,
                                    XLEVEL, YEAR);


CREATE TABLE T_TEXT_DATA
(
  STATIONID    INTEGER,
  OBSTIME      TIMESTAMP,
  ORIGINAL     VARCHAR(200),
  PARAMID      NUMERIC(15),
  TBTIME       TIMESTAMP,
  TYPEID       NUMERIC(5)
);

CREATE INDEX I_TEXT_DATA ON T_TEXT_DATA (STATIONID, OBSTIME, PARAMID, TYPEID);

CREATE TABLE T_KV2KLIMA_FILTER (
    stnr   numeric(10,0) NOT NULL,
    status character(1) NOT NULL,
    fdato  timestamp(0),  
    tdato  timestamp(0),
    typeid numeric(4,0),
    nytt_stnr numeric(5,0) 
 );


CREATE TABLE T_KV2KLIMA_PARAM_FILTER (
    stnr    numeric(10) NOT NULL,
    typeid  numeric(4) NOT NULL,
    paramid numeric(5) NOT NULL,
    sensor  numeric(1) NOT NULL,
    xlevel  integer    NOT NULL,
    fdato   timestamp(0) DEFAULT NULL,
    tdato   timestamp(0) DEFAULT NULL
 );

CREATE INDEX T_KV2KLIMA_PARAM_FILTER_TYPEID_INDEX ON T_KV2KLIMA_PARAM_FILTER (stnr,typeid);

CREATE TABLE T_KV2KLIMA_TYPEID_PARAM_FILTER (
    typeid  numeric(4,0)  NOT NULL,
    paramid numeric(5,0)  NOT NULL,
    sensor  numeric(1)    NOT NULL,  
    xlevel  integer       NOT NULL,
    fdato   timestamp(0) DEFAULT NULL,
    tdato   timestamp(0) DEFAULT NULL
 );

CREATE INDEX T_KV2KLIMA_TYPEID_PARAM_FILTER_TYPEID_INDEX ON T_KV2KLIMA_TYPEID_PARAM_FILTER (typeid);