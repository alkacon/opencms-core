CREATE OR REPLACE
PACKAGE opencmsProperty IS
  FUNCTION ReadAllProperties(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor;
  PROCEDURE writeProperties(pUserId IN NUMBER, pProjectId IN NUMBER, pResourceId IN NUMBER, pResourceType IN NUMBER, pPropertyInfo IN userTypes.anyCursor);
  PROCEDURE writeProperties(pPropertyInfo IN userTypes.anyCursor, pResourceId IN NUMBER, pResourceType IN NUMBER);
  PROCEDURE writeProperty(pMeta IN VARCHAR2, pValue IN VARCHAR2, pResourceId IN NUMBER, pResourceType IN NUMBER);
END;
/
