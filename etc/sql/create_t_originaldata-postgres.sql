-- Table that holds original data from kvalobs in klimadb. 

CREATE TABLE T_ORIGINALDATA
(
  STNR         NUMERIC(12,0),
  DATO         TIMESTAMP,
  TYPEID       NUMERIC(4,0),
  PARAMID      NUMERIC(5,0),
  SENSOR       NUMERIC(1,0),
  XLEVEL       NUMERIC(2,0),
  ORIGINAL     NUMERIC(13,1),
  CORRECTED    NUMERIC(13,1),
  KVSTAMP      TIMESTAMP,
  KLSTAMP      TIMESTAMP,                -- bare i klimabasen
  USEINFO      VARCHAR(16),
  CONTROLINFO  VARCHAR(16),
  CFAILED      VARCHAR(200)
)
