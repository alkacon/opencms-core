CREATE OR REPLACE
PACKAGE BODY userErrors IS
  PROCEDURE raiseUserError(pError IN NUMBER) IS
  BEGIN
    RAISE_APPLICATION_ERROR(pError, getErrMsg(pError));
  END raiseUserError;
---------------------------------------------------------------------------------------------
  FUNCTION getErrMsg(pErrorCode NUMBER) RETURN VARCHAR2 IS
  BEGIN
    IF pErrorCode = -20000 THEN
      RETURN ERROR_20000;
    ELSIF pErrorCode = -20001 THEN
      RETURN ERROR_20001;
    ELSIF pErrorCode = -20002 THEN
      RETURN ERROR_20002;
    ELSIF pErrorCode = -20003 THEN
      RETURN ERROR_20003;
    ELSIF pErrorCode = -20004 THEN
      RETURN ERROR_20004;
    ELSIF pErrorCode = -20005 THEN
      RETURN ERROR_20005;
    ELSIF pErrorCode = -20006 THEN
      RETURN ERROR_20006;
    ELSIF pErrorCode = -20007 THEN
      RETURN ERROR_20007;
    ELSIF pErrorCode = -20008 THEN
      RETURN ERROR_20008;
    ELSIF pErrorCode = -20009 THEN
      RETURN ERROR_20009;
    ELSIF pErrorCode = -20010 THEN
      RETURN ERROR_20010;
    ELSIF pErrorCode = -20011 THEN
      RETURN ERROR_20011;
    ELSIF pErrorCode = -20012 THEN
      RETURN ERROR_20012;
    ELSIF pErrorCode = -20013 THEN
      RETURN ERROR_20013;      
    ELSIF pErrorCode = -20014 THEN
      RETURN ERROR_20014;                  
    ELSE
      RETURN ERROR_20000;
    END IF;                                      
  END getErrMsg;
----------------------------------------------------------------------------------------------
END;
/
