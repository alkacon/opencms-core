CREATE OR REPLACE
PACKAGE BODY opencmsProperty IS
----------------------------------------------------------------------------------------
-- read all properties of the resource and return as cursor
----------------------------------------------------------------------------------------
  FUNCTION ReadAllProperties(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor IS
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    curProperties userTypes.anyCursor;
  BEGIN
    curResource := opencmsResource.readFileHeader(pUserId, pProjectID, pResourceName);
    FETCH curResource INTO recResource;
    CLOSE curResource;
    IF opencmsAccess.accessRead(pUserId, pProjectId, recResource.resource_id) = 0 THEN
      OPEN curProperties FOR select 'error', '' from dual;
    ELSE
      OPEN curProperties FOR select p.property_value, pd.propertydef_name
                                  from cms_properties p, cms_propertydef pd
                                  where p.propertydef_id = pd.propertydef_id
                                  and p.resource_id = recResource.resource_id
                                  and pd.resource_type = recResource.resource_type;
    END IF;
    RETURN curProperties;
  END ReadAllProperties;
----------------------------------------------------------------------------------------
-- checks the access for writing the properies and
-- calls the second procedure writeProperties
----------------------------------------------------------------------------------------
  PROCEDURE writeProperties(pUserId IN NUMBER, pProjectId IN NUMBER, pResourceId IN NUMBER,
                            pResourceType IN NUMBER, pPropertyInfo IN userTypes.anyCursor) IS
    recPropertyValue cms_properties.property_value%TYPE;
    recPropdefName cms_propertydef.propertydef_name%TYPE;
  BEGIN
    IF opencmsAccess.accessWrite(pUserID, pProjectID, pResourceID) = 0 THEN
      userErrors.raiseUserError(userErrors.C_NO_ACCESS);
    END IF;
    writeProperties(pPropertyInfo, pResourceId, pResourceType);
  END writeProperties;
----------------------------------------------------------------------------------------
-- write properties in pPropertyInfo to resource with resource_id = pResourceId
-- calls writeProperty
----------------------------------------------------------------------------------------
  PROCEDURE writeProperties(pPropertyInfo IN userTypes.anyCursor, pResourceId IN NUMBER,
                            pResourceType IN NUMBER) IS
    recPropertyValue cms_properties.property_value%TYPE;
    recPropdefName cms_propertydef.propertydef_name%TYPE;
  BEGIN
    LOOP
      FETCH pPropertyInfo INTO recPropertyValue, recPropdefName;
      EXIT WHEN pPropertyInfo%NOTFOUND;
      IF recPropdefName != 'error' THEN
        writeProperty(recPropdefName, recPropertyValue, pResourceId, pResourceType);
      END IF;
    END LOOP;
    CLOSE pPropertyInfo;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      RAISE;
  END writeProperties;
----------------------------------------------------------------------------------------
-- write the property of propertydef = pMeta with the value pValue for the resource
-- with resource_id = pResourceId
----------------------------------------------------------------------------------------
  PROCEDURE writeProperty(pMeta IN VARCHAR2, pValue IN VARCHAR2, pResourceId IN NUMBER, pResourceType IN NUMBER) IS
    vPropdefID NUMBER;
    vCount NUMBER;
    vNewPropId NUMBER;
  BEGIN
    BEGIN
      select propertydef_id into vPropdefID
             from cms_propertydef
             where propertydef_name = pMeta
             and resource_type = pResourceType;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        vPropdefId := NULL;
    END;
    IF vPropdefID IS NOT NULL THEN
      select count(*) into vCount from cms_properties p, cms_propertydef pd
             where p.propertydef_id = pd.propertydef_id
             and p.resource_id = pResourceID
             and pd.propertydef_name = pMeta
             and pd.resource_type = pResourceType;
      IF vCount > 0 THEN
        -- update
        update cms_properties set property_value = pValue
               where resource_id = pResourceId
               and propertydef_id = vPropdefId;
        -- newprop = false
      ELSE
        -- insert
        vNewPropId := getNextId(opencmsConstants.C_TABLE_PROPERTIES);
        insert into cms_properties
              (property_id, propertydef_id, resource_id, property_value)
        values
              (vNewPropId, vPropdefId, pResourceId, pValue);
        -- newprop = true
      END IF;
    ELSE
      userErrors.raiseUserError(userErrors.C_NOT_FOUND);
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END writeProperty;
----------------------------------------------------------------------------------------
END;
/
