CREATE OR REPLACE
PACKAGE BODY opencmsAccess IS
---------------------------------------------------------------------------------------------------
-- function checks if user has access to create the resource, return binary number: 1=true, 0=false
---------------------------------------------------------------------------------------------------
  FUNCTION accessCreate(pUserID NUMBER, pProjectID NUMBER, pResourceID NUMBER) RETURN NUMBER IS
    vResProjectID NUMBER;
    curNextResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vNextResource NUMBER;
    vNextPath cms_resources.resource_name%TYPE;
  BEGIN
    -- project = online-project => false
    IF pProjectID = opencmsConstants.C_PROJECT_ONLINE_ID THEN
      RETURN 0;
    END IF;
    -- no access for projekt => false
    IF accessProject(pUserID, pProjectID) = 0 THEN
      RETURN 0;
    END IF;
    -- resource does not belong to the projekt with project_id = pProjectId => false
    select project_id, resource_name into vResProjectID, vNextPath
           from cms_resources
           where resource_id = pResourceID;
    IF vResProjectID != pProjectId THEN
      RETURN 0;
    END IF;
    -- access for resource and super resources
    vNextResource := pResourceID;
    --WHILE vNextPath IS NOT NULL
    LOOP
    -- for resource and all super resources
      IF (accessOwner(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_OWNER_WRITE) = 1
          OR accessGroup(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_GROUP_WRITE) = 1
          OR accessOther(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_PUBLIC_WRITE) = 1) THEN
        curNextResource := opencmsResource.readFolder(pUserId, pProjectID, vNextPath);
        FETCH curNextResource INTO recResource;
        IF curNextResource%NOTFOUND THEN
          recResource := NULL;
        END IF;
        CLOSE curNextResource;
        -- resource locked by another user => false
        IF recResource.locked_by NOT IN (opencmsConstants.C_UNKNOWN_ID, pUserID) THEN
          RETURN 0;
        END IF;
        -- search next folder
        vNextResource := recResource.parent_id;
        vNextPath := opencmsResource.getParent(recResource.resource_name);
      ELSE
        RETURN 0;
      END IF;
      IF (opencmsResource.getParent(vNextPath)) IS NULL THEN
        -- don't check the access for the root-folder
        EXIT;
      END IF;
    END LOOP;
    RETURN 1;
  END accessCreate;
---------------------------------------------------------------------------------------------------
-- function checks if user has access to lock the resource, return binary number: 1=true, 0=false
---------------------------------------------------------------------------------------------------
  FUNCTION accessLock(pUserID NUMBER, pProjectID NUMBER, pResourceID NUMBER) RETURN NUMBER IS
    vResProject NUMBER;
    vResPath cms_resources.resource_name%TYPE;
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
  BEGIN
    -- project = online-Project => false
    IF pProjectID = opencmsConstants.C_PROJECT_ONLINE_ID THEN
      RETURN 0;
    END IF;
    -- not accessProject => false
    IF accessProject(pUserID, pProjectID) = 0 THEN
      RETURN 0;
    END IF;
    BEGIN
      select project_id, resource_name into vResProject, vResPath
           from cms_resources
           where resource_id = pResourceID;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        vResProject := NULL;
        vResPath := NULL;
    END;
    -- resource.project_id != project_id => false
    IF vResProject != pProjectID THEN
      RETURN 0;
    END IF;
    -- if resource.getParent = NULL => true
    vResPath := opencmsResource.getParent(vResPath);
    IF vResPath IS NULL THEN
      RETURN 1;
    ELSE
      -- for resource and all super resources
      --WHILE vResPath IS NOT NULL
      LOOP
        curResource := opencmsResource.readFolder(pUserID, pProjectId, vResPath);
        FETCH curResource INTO recResource;
        IF curResource%NOTFOUND THEN
          recResource := NULL;
        END IF;
        CLOSE curResource;
        -- resource.locked_by not in (C_UNKNOWN_ID, pUserID) => false
        IF recResource.locked_by NOT IN (opencmsConstants.C_UNKNOWN_ID, pUserID) THEN
          RETURN 0;
        END IF;
        vResPath := opencmsResource.getParent(vResPath);
        IF opencmsResource.getParent(vResPath) IS NULL THEN
          -- don't check the access for the root-folder
          EXIT;
        END IF;
      END LOOP;
    END IF;
    RETURN 1;
  END accessLock;
---------------------------------------------------------------------------------------------------
-- function checks if user has access to unlock the resource, return binary number: 1=true, 0=false
---------------------------------------------------------------------------------------------------
  FUNCTION accessUnlock(pUserID NUMBER, pProjectID NUMBER, pResourceID NUMBER) RETURN NUMBER IS
    vResProject NUMBER;
    vResPath cms_resources.resource_name%TYPE;
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
  BEGIN
    -- project = online-Project => false
    IF pProjectID = opencmsConstants.C_PROJECT_ONLINE_ID THEN
      RETURN 0;
    END IF;
    -- not accessProject => false
    IF accessProject(pUserID, pProjectID) = 0 THEN
      RETURN 0;
    END IF;
    BEGIN
      select project_id, resource_name into vResProject, vResPath
           from cms_resources
           where resource_id = pResourceID;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        vResProject := NULL;
        vResPath := NULL;
    END;
    -- resource.project_id != project_id => false
    IF vResProject != pProjectID THEN
      RETURN 0;
    END IF;
    -- if resource.getParent = NULL => true
    vResPath := opencmsResource.getParent(vResPath);
    IF vResPath IS NULL THEN
      RETURN 1;
    ELSE
      -- for resource and all super resources
      --WHILE vResPath IS NOT NULL
      LOOP
        curResource := opencmsResource.readFolder(pUserID, pProjectId, vResPath);
        FETCH curResource INTO recResource;
        IF curResource%NOTFOUND THEN
          recResource := NULL;
        END IF;
        CLOSE curResource;
        -- resource.locked_by not in (C_UNKNOWN_ID, pUserID) => false
        IF recResource.locked_by != opencmsConstants.C_UNKNOWN_ID THEN
          RETURN 0;
        END IF;
        vResPath := opencmsResource.getParent(vResPath);
        IF opencmsResource.getParent(vResPath) IS NULL THEN
          -- don't check the access for the root-folder
          EXIT;
        END IF;
      END LOOP;
    END IF;
    RETURN 1;
  END accessUnlock;
---------------------------------------------------------------------------------------------------
-- function checks if user has access for the project, return binary number: 1=true, 0=false
---------------------------------------------------------------------------------------------------
  FUNCTION accessProject(pUserID NUMBER, pProjectID NUMBER) RETURN NUMBER IS
    vProjFlags NUMBER;
    vProjOwner NUMBER;
    vProjGroup NUMBER;
    vProjManager NUMBER;
    vAdminId NUMBER;
    curGroups userTypes.anyCursor;
    recGroupID cms_groups.group_id%TYPE;
    recGroupName cms_groups.group_name%TYPE;
  BEGIN
    -- project_id = online-project => true
    IF pProjectID = opencmsConstants.C_PROJECT_ONLINE_ID THEN
      RETURN 1;
    END IF;
    BEGIN
      select project_flags, user_id, group_id, managergroup_id
           into vProjFlags, vProjOwner, vProjGroup, vProjManager
           from cms_projects
           where project_id = pProjectID;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        RETURN 0;
    END;
    -- project_flags != C_PROJECT_STATE_UNLOCKED => false
    IF vProjFlags != opencmsConstants.C_PROJECT_STATE_UNLOCKED THEN
      RETURN 0;
    END IF;
    -- user = owner or user isAdmin => true
    select group_id into vAdminId from cms_groups where group_name = opencmsConstants.C_GROUP_ADMIN;
    IF vProjOwner = pUserID OR opencmsGroup.userInGroup(pUserID, vAdminId) = 1 THEN
      RETURN 1;
    END IF;
    -- for all groups from getGroupsOfUser:
    -- group_id in (project.group_id, project.manager_group_id) => true
    curGroups := opencmsGroup.getGroupsOfUser(pUserID);
    LOOP
      FETCH curGroups INTO recGroupID, recGroupName;
      EXIT WHEN curGroups%NOTFOUND;
      IF recGroupID IN (vProjGroup, vProjManager) THEN
        RETURN 1;
      END IF;
    END LOOP;
    CLOSE curGroups;
    RETURN 0;
  END accessProject;
---------------------------------------------------------------------------------------------------
-- function checks if user has access to read the resource, return binary number: 1=true, 0=false
---------------------------------------------------------------------------------------------------
  FUNCTION accessRead(pUserID NUMBER, pProjectID NUMBER, pResourceID NUMBER) RETURN NUMBER IS
    curNextResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vNextResource NUMBER;
    vNextPath cms_resources.resource_name%TYPE;
  BEGIN
    IF pResourceId IS NULL THEN
      RETURN 0;
    END IF;
    -- NOT accessProject => false
    IF accessProject(pUserID, pProjectID) = 0 THEN
      RETURN 0;
    END IF;
    BEGIN
      select resource_name into vNextPath
           from cms_resources
           where resource_id = pResourceID;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        vNextPath := NULL;
    END;
    vNextResource := pResourceID;
    -- for resource and all super resources
    WHILE vNextPath IS NOT NULL LOOP
      -- NOT (accessOther or accessOwner or accessGroup (read)) => false
      IF (accessOwner(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_OWNER_READ) = 1
          OR accessGroup(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_GROUP_READ) = 1
          OR accessOther(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_PUBLIC_READ) = 1) THEN
        curNextResource := opencmsResource.readFolder(pUserId, pProjectID, vNextPath);
        FETCH curNextResource INTO recResource;
        IF curNextResource%NOTFOUND THEN
          recResource := NULL;
        END IF;
        CLOSE curNextResource;
        -- search next folder
        vNextResource := recResource.parent_id;
        vNextPath := opencmsResource.getParent(recResource.resource_name);
      ELSE
        RETURN 0;
      END IF;
    END LOOP;
    RETURN 1;
  END accessRead;
---------------------------------------------------------------------------------------------------
-- function checks if user has access to write the resource, return binary number: 1=true, 0=false
---------------------------------------------------------------------------------------------------
  FUNCTION accessWrite(pUserID NUMBER, pProjectID NUMBER, pResourceID NUMBER) RETURN NUMBER IS
    vResProjectID NUMBER;
    curNextResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vNextResource NUMBER;
    vNextPath cms_resources.resource_name%TYPE;
    vLockedBy NUMBER;
  BEGIN
    -- project = online-project => false
    IF pProjectID = opencmsConstants.C_PROJECT_ONLINE_ID THEN
      RETURN 0;
    END IF;
    -- NOT accessProject => false
    IF accessProject(pUserID, pProjectID) = 0 THEN
      RETURN 0;
    END IF;
    select project_id, resource_name, parent_id, locked_by
           into vResProjectID, vNextPath, vNextResource, vLockedBy
           from cms_resources
           where resource_id = pResourceID;
-- the following check is disabled because there are problems
    -- not locked by user => false
    IF vLockedBy != pUserId THEN
      RETURN 0;
    END IF;
    -- resource.projectID != project_id => false
    IF vResProjectID != pProjectId THEN
      RETURN 0;
    END IF;
    -- for current resource no accessOther/Owner/Group => false
    IF (accessOwner(pUserID, pProjectID, pResourceId, opencmsConstants.C_ACCESS_OWNER_WRITE) = 0
        AND accessGroup(pUserID, pProjectID, pResourceId, opencmsConstants.C_ACCESS_GROUP_WRITE) = 0
        AND accessOther(pUserID, pProjectID, pResourceId, opencmsConstants.C_ACCESS_PUBLIC_WRITE) = 0) THEN
      RETURN 0;
    END IF;
    -- select super resources
    vNextPath := opencmsResource.getParent(vNextPath);
    -- check access for all super resources
    --WHILE vNextPath IS NOT NULL
    LOOP
       -- no accessOther/Owner/Group => false
      IF (accessOwner(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_OWNER_WRITE) = 1
          OR accessGroup(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_GROUP_WRITE) = 1
          OR accessOther(pUserID, pProjectID, vNextResource, opencmsConstants.C_ACCESS_PUBLIC_WRITE) = 1) THEN
        curNextResource := opencmsResource.readFolder(pUserId, pProjectID, vNextPath);
        FETCH curNextResource INTO recResource;
        IF curNextResource%NOTFOUND THEN
          recResource := NULL;
        END IF;
        CLOSE curNextResource;
        -- resource locked by another user => false
        IF recResource.locked_by NOT IN (opencmsConstants.C_UNKNOWN_ID, pUserID) THEN
          RETURN 0;
        END IF;
        -- search next folder
        vNextResource := recResource.parent_id;
        vNextPath := opencmsResource.getParent(recResource.resource_name);
      ELSE
        RETURN 0;
      END IF;
      IF opencmsResource.getParent(vNextPath) IS NULL THEN
        -- don't check the access for the root-folder
        EXIT;
      END IF;
    END LOOP;
    RETURN 1;
  END accessWrite;
---------------------------------------------------------------------------------------------------
-- access defined by pAccess (read/write) for others return boolean
---------------------------------------------------------------------------------------------------
  FUNCTION accessOther(pUserID NUMBER, pProjectID NUMBER, pResourceID NUMBER, pAccess NUMBER) RETURN NUMBER IS
    vAccessFlag NUMBER;
  BEGIN
    select access_flags into vAccessFlag from cms_resources where resource_id = pResourceID;
    IF bitand(vAccessFlag, pAccess) = pAccess THEN
      RETURN 1;
    END IF;
    RETURN 0;
  END accessOther;
---------------------------------------------------------------------------------------------------
-- access defined by pAccess (read/write) for owner return boolean
---------------------------------------------------------------------------------------------------
  FUNCTION accessOwner(pUserID NUMBER, pProjectID NUMBER, pResourceID NUMBER, pAccess NUMBER) RETURN NUMBER IS
    vAccessFlag NUMBER;
    vOwnerID NUMBER;
    vAdminId NUMBER;
  BEGIN
    select group_id into vAdminId from cms_groups where group_name = opencmsConstants.C_GROUP_ADMIN;
    IF opencmsGroup.userInGroup(pUserId, vAdminId) = 1 THEN
      RETURN 1;
    END IF;
    select user_id into vOwnerId from cms_resources where resource_id = pResourceId;
    IF vOwnerId = pUserId THEN
      select access_flags into vAccessFlag from cms_resources where resource_id = pResourceID;
      IF bitand(vAccessFlag, pAccess) = pAccess THEN
        RETURN 1;
      END IF;
    END IF;
    RETURN 0;
  END accessOwner;
---------------------------------------------------------------------------------------------------
-- access defined by pAccess (read/write) for group return boolean
---------------------------------------------------------------------------------------------------
  FUNCTION accessGroup(pUserID NUMBER, pProjectID NUMBER, pResourceID NUMBER, pAccess NUMBER) RETURN NUMBER IS
    vGroupId NUMBER;
    vAccessFlag NUMBER;
  BEGIN
    select group_id into vGroupId from cms_resources where resource_id = pResourceID;
    IF opencmsGroup.userInGroup(pUserID, vGroupId) = 1 THEN
      select access_flags into vAccessFlag from cms_resources where resource_id = pResourceID;
      IF bitand(vAccessFlag, pAccess) = pAccess THEN
        RETURN 1;
      END IF;
    END IF;
    RETURN 0;
  END accessGroup;
---------------------------------------------------------------------------------------------------
END;
/
