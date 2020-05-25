-- Table that holds original data from kvalobs in klimadb. 

CREATE TABLE T_ORIGINALDATA
(
  STNR         NUMBER(12),
  DATO         DATE,
  TYPEID       NUMBER(4),
  PARAMID      NUMBER(5),
  SENSOR       NUMBER(1),
  XLEVEL       NUMBER(2),
  ORIGINAL     NUMBER(13,1),
  CORRECTED    NUMBER(13,1),
  KVSTAMP      DATE,
  KLSTAMP      DATE,                -- bare i klimabasen
  USEINFO      VARCHAR2(16 BYTE),
  CONTROLINFO  VARCHAR2(16 BYTE),
  CFAILED      VARCHAR2(200 BYTE)
)
