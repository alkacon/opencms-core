CREATE OR REPLACE
PACKAGE BODY opencmsresource IS
--------------------------------------------------------------------------------------------------------------
-- declare variables/procedures/functions which are used in this package
--------------------------------------------------------------------------------------------------------------
  bAnyList VARCHAR2(32767);
  bResourceList VARCHAR2(32767) := '';
  bPathList userTypes.nameTable;
  bResList userTypes.resourceTable;
  --FUNCTION addInList(pName VARCHAR2) RETURN BOOLEAN;
  FUNCTION addPathInList(pName VARCHAR2) RETURN BOOLEAN;
--------------------------------------------------------------------------------------------------------------
-- this procedure is called from DbAccess. It calls the second lockResource-procedure and returns a resultset
-- which is needed to update the resource-cache
--------------------------------------------------------------------------------------------------------------
  PROCEDURE lockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2, pForce IN VARCHAR2, pResource OUT userTypes.anyCursor) IS
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    vTableName VARCHAR2(20);
  BEGIN
    bResourceList := '';
    -- first lock the resources
    lockResource(pUserId, pProjectId, vOnlineProject, pFolderName, pForce);
    -- now build the cursor which contains the locked resources to return the resultset
    IF pProjectId = vOnlineProject THEN
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    OPEN pResource FOR 'select '||vTableName||'.*, '||vTableName||'.project_id locked_in_project from '||
                        vTableName||' where project_id = '||pProjectId||
                        ' and resource_name like '''||pFolderName||'%'' and locked_by = '||pUserId;
    bResourceList := '';
  END;
--------------------------------------------------------------------------------------------------------------
-- procedure which locks the resource and if the resource is folder all subresources
--------------------------------------------------------------------------------------------------------------
  PROCEDURE lockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProject IN NUMBER, pFolderName IN VARCHAR2, pForce IN VARCHAR2) IS
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vFolderName cms_resources.resource_name%TYPE;
    tableResource userTypes.resourceTable;
    vTableName VARCHAR2(20);
  BEGIN
    IF pProjectId = pOnlineProject THEN
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    -- if pFolderName is the id and not the path then read the resource_name for the resource
    -- read all Information about this resource
    IF instr(pFolderName,'/') = 0 THEN
      IF pProjectId = pOnlineProject THEN
        select resource_name into vFolderName from cms_online_resources where resource_id = pFolderName;
      ELSE
        select resource_name into vFolderName from cms_resources where resource_id = pFolderName;
      END IF;
    ELSE
      vFolderName := pFolderName;
    END IF;
    IF substr(vFolderName, -1) = '/' THEN
      curResource := readFolderAcc(pUserId, pProjectId, vFolderName);
    ELSE
      curResource := readFileHeader(pUserId, pProjectId, vFolderName);
    END IF;
    FETCH curResource INTO recResource;
    CLOSE curResource;
    -- throw exception if the resource could not be found
    IF recResource.resource_id IS NULL THEN
      userErrors.raiseUserError(userErrors.C_NOT_FOUND);
    END IF;
    -- belongs this resource to the project?
    IF recResource.project_id != pProjectId THEN
      RETURN;
    END IF;
    -- has the user access to lock the resource?
    IF opencmsAccess.accessLock(pUserId, pProjectId, recResource.resource_id) = 1 THEN
      -- is the resource unlocked?
      IF recResource.locked_by != opencmsConstants.C_UNKNOWN_ID THEN
        IF pForce = 'FALSE' THEN
          userErrors.raiseUserError(userErrors.C_LOCKED);
        END IF;
      END IF;
      -- lock the resource for this user
      EXECUTE IMMEDIATE 'update '||vTableName||' set locked_by = '||pUserId||
                        ', project_id = '||pProjectId||
                        ' where resource_id = '||recResource.resource_id;
      -- put only one resource_id into the resource-list to mark that there was something locked
      bResourceList := to_char(recResource.resource_id);
      -- if the resource is folder then lock all subresources
      IF substr(vFolderName, -1) = '/' THEN
        -- all files in folder
        tableResource := getFilesInFolder(pUserId, pProjectId, vFolderName);
        FOR i IN 1..tableResource.COUNT LOOP
          recResource := tableResource(i);
          IF recResource.state != opencmsConstants.C_STATE_DELETED THEN
            lockResource(pUserId, pProjectId, pOnlineProject, recResource.resource_name, 'TRUE');
          END IF;
        END LOOP;
        -- all folders in the folder and their files and folders etc.
        curResource := getFoldersInFolder(pUserId, pProjectId, vFolderName);
        LOOP
  		  BEGIN
            FETCH curResource INTO recResource;
            EXIT WHEN curResource%NOTFOUND;
            IF recResource.state != opencmsConstants.C_STATE_DELETED THEN
              lockResource(pUserId, pProjectId, pOnlineProject, recResource.resource_name, 'TRUE');
            END IF;
          EXCEPTION
            WHEN invalid_cursor THEN
              exit;
          END;
        END LOOP;
        IF curResource%ISOPEN THEN
          CLOSE curResource;
        END IF;
      END IF;
    ELSE
      userErrors.raiseUserError(userErrors.C_NO_ACCESS);
    END IF;
    commit;
    EXCEPTION
      WHEN OTHERS THEN
        IF curResource%ISOPEN THEN
          CLOSE curResource;
        END IF;
        rollback;
        RAISE;
  END lockResource;
--------------------------------------------------------------------------------------------------------------
-- this procedure is called from DbAccess. It calls the second unlockResource-procedure and returns a resultset
-- which is needed to update the resource-cache
--------------------------------------------------------------------------------------------------------------
  PROCEDURE unlockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2, pResource OUT userTypes.anyCursor) IS
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    vTableName VARCHAR2(20);
  BEGIN
    bResourceList := '';
    -- first unlock the resources
    unlockResource(pUserId, pProjectId, vOnlineProject, pFolderName);
    -- now build the cursor which contains the unlocked resources to return the resultset
    IF pProjectId = vOnlineProject THEN
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    OPEN pResource FOR 'select '||vTableName||'.*, '||vTableName||'.project_id locked_in_project from '||
                       vTableName||' where project_id='||pProjectId||
                       ' and resource_name like '''||pFolderName||'%'' and locked_by='||opencmsConstants.C_UNKNOWN_ID;
    bResourceList := '';
  END unlockResource;
--------------------------------------------------------------------------------------------------------------
-- function which unlocks the resource and if the resource is folder all subresources
--------------------------------------------------------------------------------------------------------------
  PROCEDURE unlockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProject IN NUMBER, pFolderName IN VARCHAR2) IS
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vFolderName cms_resources.resource_name%TYPE;
    tableResource userTypes.resourceTable;
    vTableName VARCHAR2(20);
  BEGIN
    IF pProjectId = pOnlineProject THEN
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    -- if pFolderName is the id and not the path then read the resource_name for the resource
    -- read all Information about this resource
    IF instr(pFolderName,'/') = 0 THEN
      IF pProjectId = pOnlineProject THEN
        select resource_name into vFolderName from cms_online_resources where resource_id = pFolderName;
      ELSE
        select resource_name into vFolderName from cms_resources where resource_id = pFolderName;
      END IF;
    ELSE
      vFolderName := pFolderName;
    END IF;
   -- read all Information about this resource
    IF substr(vFolderName, -1) = '/' THEN
      curResource := readFolderAcc(pUserId, pProjectId, vFolderName);
    ELSE
      curResource := readFileHeader(pUserId, pProjectId, vFolderName);
    END IF;
    FETCH curResource INTO recResource;
    CLOSE curResource;
    -- has the user access to lock the resource?
    IF opencmsAccess.accessUnlock(pUserId, pProjectId, recResource.resource_id) = 1 THEN
      -- is the resource locked?
      IF recResource.locked_by != opencmsConstants.C_UNKNOWN_ID THEN
        -- is the resource locked by this user?
        IF recResource.locked_by = pUserID THEN
          -- unlock the resource
          EXECUTE IMMEDIATE 'update '||vTableName||' set locked_by = '||opencmsConstants.C_UNKNOWN_ID||
                            ' where resource_id = '||recResource.resource_id;
          -- need only one resource-id to mark that there was something unlocked
          bResourceList := to_char(recResource.resource_id);
        ELSE
          userErrors.raiseUserError(userErrors.C_LOCKED);
        END IF;
      END IF;
      -- if the resource is folder then lock all subresources
      IF substr(vFolderName, -1) = '/' THEN
        -- all files in folder
        tableResource := getFilesInFolder(pUserId, pProjectId, vFolderName);
        FOR i IN 1..tableResource.COUNT LOOP
          recResource := tableResource(i);
          IF recResource.state != opencmsConstants.C_STATE_DELETED THEN
            unlockResource(pUserId, pProjectId, pOnlineProject, recResource.resource_name);
          END IF;
        END LOOP;
        -- all folders in the folder and their files and folders etc.
        curResource := getFoldersInFolder(pUserId, pProjectId, vFolderName);
        LOOP
          BEGIN
            FETCH curResource INTO recResource;
            EXIT WHEN curResource%NOTFOUND;
            IF recResource.state != opencmsConstants.C_STATE_DELETED THEN
              unlockResource(pUserId, pProjectId, pOnlineProject, recResource.resource_name);
            END IF;
          EXCEPTION
            WHEN invalid_cursor THEN
              exit;
          END;
        END LOOP;
        IF curResource%ISOPEN THEN
          CLOSE curResource;
        END IF;
      END IF;
    ELSE
      userErrors.raiseUserError(userErrors.C_NO_ACCESS);
    END IF;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      IF curResource%ISOPEN THEN
        CLOSE curResource;
      END IF;
      rollback;
      RAISE;
  END unlockResource;
--------------------------------------------------------------------------------------------------------------
-- returns a folder with foldername = pFolderName
-- for project with project_id = pProjectId or for online-project
--------------------------------------------------------------------------------------------------------------
  FUNCTION readFolderAcc(pUserId NUMBER, pProjectID NUMBER, pFolderName VARCHAR2) RETURN userTypes.anyCursor IS
    curFolder userTypes.anyCursor;
  BEGIN
    IF pFolderName IS NOT NULL THEN
      IF opencmsAccess.accessRead(pUserId, pProjectId, pFolderName) = 0 THEN
          -- error: no access for this file
        userErrors.raiseUserError(userErrors.C_ACCESS_DENIED);
      END IF;
    END IF;
    -- because the cursor was fetched into a record it's necessary to read the file again for returning
    -- the cursor
    curFolder := readFolder(pUserId, pProjectID, pFolderName);
    RETURN curFolder;
  END readFolderAcc;
--------------------------------------------------------------------------------------------------------------
-- returns a folder with foldername = pFolderName
-- for project with project_id = pProjectId or for online-project
--------------------------------------------------------------------------------------------------------------
  FUNCTION readFolder(pUserId NUMBER, pProjectID NUMBER, pFolderName VARCHAR2) RETURN userTypes.anyCursor IS
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    curFolder userTypes.anyCursor;
  BEGIN
    -- read the resource from offline project or the online project, the first resource is used
    IF pProjectID = vOnlineProject THEN
      OPEN curFolder FOR select * from cms_online_resources
                         where resource_name = pFolderName;
    ELSE
      OPEN curFolder FOR select cms_resources.resource_id, cms_resources.parent_id,
                                cms_resources.resource_name, cms_resources.resource_type,
                                cms_resources.resource_flags, cms_resources.user_id,
                                cms_resources.group_id, cms_projectresources.project_id,
                                cms_resources.file_id, cms_resources.access_flags, cms_resources.state,
                                cms_resources.locked_by, cms_resources.launcher_type,
                                cms_resources.launcher_classname, cms_resources.date_created,
                                cms_resources.date_lastmodified, cms_resources.resource_size,
                                cms_resources.resource_lastmodified_by
                         from cms_resources, cms_projectresources
                         where cms_resources.resource_name = pFolderName
                         and cms_resources.resource_name like concat(cms_projectresources.resource_name,'%')
                         and cms_projectresources.project_id in (pProjectID, vOnlineProject)
                         order by cms_projectresources.project_id desc;
    END IF;
    RETURN curFolder;
  END readFolder;
--------------------------------------------------------------------------------------------------------------
-- returns a cursor for a resource with resource_name = pFileName
-- for the project with project_id = pProjectId or for online-projekt
--------------------------------------------------------------------------------------------------------------
  FUNCTION readFileHeader(pUserId NUMBER, pProjectID NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor IS
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    curResource userTypes.anyCursor;
    recFile cms_resources%ROWTYPE;
  BEGIN
    -- is pFileName a folder? => readFolder
    IF substr(pFileName, -1) = '/' THEN
      curResource := readFolder(pUserId, pProjectId, pFileName);
      RETURN curResource;
    END IF;
    IF pFileName IS NOT NULL THEN
      IF opencmsAccess.accessRead(pUserId, pProjectId, pFileName) = 0 THEN
          -- error: no access for this file
        userErrors.raiseUserError(userErrors.C_ACCESS_DENIED);
      END IF;
    END IF;
    curResource := readFileHeader(pUserId, pProjectId, vOnlineProject, pFileName);
    RETURN curResource;
  END readFileHeader;
--------------------------------------------------------------------------------------------------------------
-- returns a cursor for a resource with resource_name = pFileName
-- for the project with project_id = pProjectId or for online-projekt
--------------------------------------------------------------------------------------------------------------
  FUNCTION readFileHeader(pUserId NUMBER, pProjectID NUMBER, pOnlineProjectId NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor IS
    curFile userTypes.anyCursor;
  BEGIN
    -- read the resource from offline project or the online project, the first resource is used
    IF pProjectId = pOnlineProjectId THEN
      OPEN curFile FOR select * from cms_online_resources
                       where resource_name = pFileName;
    ELSE
      OPEN curFile FOR select cms_resources.resource_id, cms_resources.parent_id,
                                cms_resources.resource_name, cms_resources.resource_type,
                                cms_resources.resource_flags, cms_resources.user_id,
                                cms_resources.group_id, cms_projectresources.project_id,
                                cms_resources.file_id, cms_resources.access_flags, cms_resources.state,
                                cms_resources.locked_by, cms_resources.launcher_type,
                                cms_resources.launcher_classname, cms_resources.date_created,
                                cms_resources.date_lastmodified, cms_resources.resource_size,
                                cms_resources.resource_lastmodified_by
                         from cms_resources, cms_projectresources
                         where cms_resources.resource_name = pFileName
                         and cms_resources.resource_name like concat(cms_projectresources.resource_name,'%')
                         and cms_projectresources.project_id in (pProjectID, pOnlineProjectId)
                         order by cms_projectresources.project_id desc;
    END IF;
    RETURN curFile;
  END readFileHeader;
--------------------------------------------------------------------------------------------------------------
-- returns a cursor for a resource with resource_name = pFileName including the file-content
-- for the project with project_id = pProjectId or for online-projekt
-- without checking access
--------------------------------------------------------------------------------------------------------------
  FUNCTION readFileNoAccess(pUserId NUMBER, pProjectID NUMBER, pOnlineProjectId NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor IS
    curResource userTypes.anyCursor;
  BEGIN
    -- read the resource from offline project or the online project, the first resource is used
    IF pProjectID = pOnlineProjectId THEN
  	  	OPEN curResource FOR select r.*, f.file_content
  	                         from cms_online_resources r, cms_online_files f
                             where r.resource_name = pFileName
                             and r.file_id = f.file_id(+);
    ELSE
        OPEN curResource FOR select resource_id, parent_id, r.resource_name, resource_type, resource_flags,
                             user_id, group_id, p.project_id, f.file_id, access_flags, state, locked_by, launcher_type,
                             launcher_classname, date_created, date_lastmodified,
                             resource_size, resource_lastmodified_by, f.file_content
                             from cms_resources r, cms_files f, cms_projectresources p
                             where r.file_id=f.file_id
                             and r.resource_name=pFileName
                             and r.resource_name like concat(p.resource_name,'%')
                             and p.project_id in (pProjectID, pOnlineProjectId)
                             order by p.project_id desc;
    END IF;
    RETURN curResource;
  END readFileNoAccess;
--------------------------------------------------------------------------------------------------------------
-- returns a cursor for a resource with resource_name = pFileName including the file-content
-- for the project with project_id = pProjectId or for online-projekt
--------------------------------------------------------------------------------------------------------------
  FUNCTION readFile(pUserId NUMBER, pProjectID NUMBER, pOnlineProjectId NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor IS
    curResource userTypes.anyCursor;
    recFile userTypes.fileRecord;
  BEGIN
    -- first read the file-header either from the project or from the onlineProject
    curResource := readFileNoAccess(pUserId, pProjectId, pOnlineProjectId, pFileName);
    FETCH curResource INTO recFile;
    CLOSE curResource;
    -- now create the cursor for the file including the file_content
    IF recFile.resource_id IS NOT NULL THEN
      IF recFile.state = opencmsConstants.C_STATE_DELETED THEN
	    userErrors.raiseUserError(userErrors.C_RESOURCE_DELETED);
      END IF;
      --IF opencmsAccess.accessRead(pUserId, pProjectId, recFile.resource_id) = 0 THEN
      IF opencmsAccess.accessRead(pUserId, pProjectId, pFileName) = 0 THEN
          -- error: no access for this file
        userErrors.raiseUserError(userErrors.C_ACCESS_DENIED);
      END IF;
    END IF;
    -- because the cursor was fetched it has to be read again
    curResource := readFileNoAccess(pUserId, pProjectId, pOnlineProjectId, pFileName);
    RETURN curResource;
  END readFile;
--------------------------------------------------------------------------------------------------------------
-- returns a cursor for a resource with resource_name = pFileName including the file-content
-- for the project with project_id = pProjectId or for online-projekt
--------------------------------------------------------------------------------------------------------------
  FUNCTION readFile(pUserId NUMBER, pProjectID NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor IS
    curResource userTypes.anyCursor;
    recFile userTypes.fileRecord;
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
  BEGIN
    -- first read the file
    curResource := readFileNoAccess(pUserId, pProjectId, vOnlineProject, pFileName);
    FETCH curResource INTO recFile;
    CLOSE curResource;
    -- check the access for the existing file
    IF recFile.resource_id IS NOT NULL THEN
      IF recFile.state = opencmsConstants.C_STATE_DELETED THEN
	    userErrors.raiseUserError(userErrors.C_RESOURCE_DELETED);
      END IF;
      --IF opencmsAccess.accessRead(pUserId, pProjectId, recFile.resource_id) = 0 THEN
      IF opencmsAccess.accessRead(pUserId, pProjectId, pFileName) = 0 THEN
          -- error: no access for this file
        userErrors.raiseUserError(userErrors.C_ACCESS_DENIED);
      END IF;
    END IF;
    -- because the cursor was fetched it has to be read again
    curResource := readFileNoAccess(pUserId, pProjectId, vOnlineProject, pFileName);
    RETURN curResource;
  END readFile;
----------------------------------------------------------------------------------------------
-- creates the folder pResource
----------------------------------------------------------------------------------------------
  PROCEDURE createFolder(pUserId NUMBER, pProjectId NUMBER, pParentResId NUMBER, pFileId NUMBER,
                         pFolderName VARCHAR2, pFlags NUMBER) IS
    curFolder userTypes.anyCursor;
    recFolder cms_resources%ROWTYPE;
    vState NUMBER := opencmsConstants.C_STATE_NEW;
    vUserGroupId NUMBER;
    vNewResourceId NUMBER;
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    vTableName VARCHAR2(20);
    vRootFolder NUMBER;
  BEGIN
    IF pProjectId = vOnlineProject THEN
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    select user_default_group_id into vUserGroupId from cms_users where user_id = pUserId;
    curFolder := readFolder(pUserId, pProjectId, pFolderName);
    FETCH curFolder INTO recFolder;
    CLOSE curFolder;
    IF recFolder.state = opencmsConstants.C_STATE_DELETED THEN
      removeFolder(pUserId, pProjectId, recFolder.resource_id, pFolderName);
      vState := opencmsConstants.C_STATE_CHANGED;
    END IF;
    vNewResourceId := getNextId(vTableName);
    EXECUTE IMMEDIATE 'insert into '||vTableName||
                      ' (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, group_id,'||
                      ' project_id, file_id, access_flags, state, locked_by, launcher_type, launcher_classname,'||
                      ' date_created, date_lastmodified, resource_size, resource_lastmodified_by)'||
                      ' values ('||vNewResourceId||', '||pParentResId||', '''||pFolderName||''', 0, '||pFlags||
                      ', '||pUserId||', '||vUserGroupId||', '||pProjectId||', '||opencmsConstants.C_UNKNOWN_ID||
                      ', '||opencmsConstants.C_ACCESS_DEFAULT_FLAGS||', '||vState||', '||
                      opencmsConstants.C_UNKNOWN_ID||', '||opencmsConstants.C_UNKNOWN_LAUNCHER_ID||', '''||
                      opencmsConstants.C_UNKNOWN_LAUNCHER||''', to_date('''||to_char(sysdate,'dd.mm.yyyy hh24:mi:ss')||
                      ''',''dd.mm.yyyy hh24:mi:ss''), to_date('''||to_char(sysdate,'dd.mm.yyyy hh24:mi:ss')||
                      ''',''dd.mm.yyyy hh24:mi:ss''), 0, '||pUserId||')';
    -- if this is the rootfolder or if the parentfolder is the rootfolder
    -- try to create the projectresource
    IF ((pParentResId = opencmsConstants.C_UNKNOWN_ID) OR getParent(pFolderName) = opencmsConstants.C_ROOT) AND
       (pProjectId != vOnlineProject) THEN
      BEGIN
        select count(*) into vRootFolder from cms_projectresources
               where project_id = pProjectId;
        IF vRootFolder = 0 THEN
          insert into cms_projectresources (project_id, resource_name)
          values (pProjectId, pFolderName);
        END IF;
      EXCEPTION
        WHEN OTHERS THEN
          null;
      END;
    END IF;
    commit;
  EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
      rollback;
      userErrors.raiseUserError(userErrors.C_FILE_EXISTS);
    WHEN OTHERS THEN
      IF curFolder%ISOPEN THEN
        CLOSE curFolder;
      END IF;
      rollback;
      raise_application_error(-20004, 'error when create folder',true);
  END createFolder;
----------------------------------------------------------------------------------------------
-- creates the folder pResource
----------------------------------------------------------------------------------------------
  PROCEDURE createFolder(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProjectId IN NUMBER,
                         pResource IN cms_resources%ROWTYPE, pParentId IN NUMBER, pFolderName IN VARCHAR2,
                         oResource OUT userTypes.anyCursor) IS
    curResources userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vState NUMBER := opencmsConstants.C_STATE_NEW;
    vNewResourceId cms_resources.resource_id%TYPE;
    vTableName VARCHAR2(20);
    vRootFolder NUMBER;
  BEGIN
    IF pProjectId = pOnlineProjectId THEN
      vState := pResource.state;
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    -- if the folder in destination-project exists and is marked as deleted then delete the folder
    curResources := readFolder(pUserId, pProjectId, pFolderName);
    FETCH curResources INTO recResource;
    CLOSE curResources;
    IF recResource.state = opencmsConstants.C_STATE_DELETED THEN
      removeFolder(pUserId, pProjectId, recResource.resource_id, pFolderName);
      vState := opencmsConstants.C_STATE_CHANGED;
    END IF;
    vNewResourceId := getNextId(vTableName);
    EXECUTE IMMEDIATE 'insert into '||vTableName||
                      ' (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, group_id,'||
                      ' project_id, file_id, access_flags, state, locked_by, launcher_type, launcher_classname,'||
                      ' date_created, date_lastmodified, resource_size, resource_lastmodified_by)'||
                      ' values ('||vNewResourceId||', '||pParentId||', '''||pFolderName||''', '||
                      pResource.resource_type||', '||pResource.resource_flags||', '||pResource.user_id||', '||
                      pResource.group_id||', '||pProjectId||', -1, '||pResource.access_flags||', '||vState||', '||
                      pResource.locked_by||', '||pResource.launcher_type||', '''||pResource.launcher_classname||
                      ''', to_date('''||to_char(pResource.date_created,'dd.mm.yyyy hh24:mi:ss')||
                      ''',''dd.mm.yyyy hh24:mi:ss''), to_date('''||to_char(pResource.date_lastmodified,'dd.mm.yyyy hh24:mi:ss')||
                      ''',''dd.mm.yyyy hh24:mi:ss''), 0, '||pUserId||')';
    -- if this is the rootfolder or if the parentfolder is the rootfolder
    -- try to create the projectresource
    IF ((pParentId = opencmsConstants.C_UNKNOWN_ID) OR getParent(pFolderName) = opencmsConstants.C_ROOT) AND
       (pProjectId != pOnlineProjectId) THEN
      BEGIN
        select count(*) into vRootFolder from cms_projectresources
               where project_id = pProjectId;
        IF vRootFolder = 0 THEN
          insert into cms_projectresources (project_id, resource_name)
          values (pProjectId, pFolderName);
        END IF;
      EXCEPTION
        WHEN OTHERS THEN
          null;
      END;
    END IF;
    commit;
    OPEN oResource FOR 'select * from '||vTableName||' where resource_id = '||vNewResourceId;
  EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
      rollback;
      userErrors.raiseUserError(userErrors.C_FILE_EXISTS);
    WHEN OTHERS THEN
      IF curResources%ISOPEN THEN
        CLOSE curResources;
      END IF;
      IF oResource%ISOPEN THEN
        CLOSE oResource;
      END IF;
      rollback;
      raise_application_error(-20004, 'error when create folder',true);
  END createFolder;
----------------------------------------------------------------------------------------------
-- create a resource for a file, if pCopy = 'TRUE' then copy the file-content (cms_files),
-- return the new resource as cursor
----------------------------------------------------------------------------------------------
  PROCEDURE createFile(pProjectId IN NUMBER, pOnlineProjectId IN NUMBER, pResource IN userTypes.fileRecord, pUserId IN NUMBER,
                       pParentId IN NUMBER,  pFileName IN VARCHAR2, pCopy IN VARCHAR2, oResource OUT userTypes.anyCursor) IS
    curResources userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vState NUMBER := opencmsConstants.C_STATE_NEW;
    vFileId NUMBER;
    vNewResourceId cms_resources.resource_id%TYPE;
    vResourceTable VARCHAR2(20);
    vFileTable VARCHAR2(20);
  BEGIN
    IF pProjectId = pOnlineProjectId THEN
      vState := pResource.state;
      vResourceTable := 'CMS_ONLINE_RESOURCES';
      vFileTable := 'CMS_ONLINE_FILES';
    ELSE
      vResourceTable := 'CMS_RESOURCES';
      vFileTable := 'CMS_FILES';
    END IF;
    -- if the folder in destination-project exists and is marked as deleted then delete the folder
    curResources := readFileHeader(pUserId, pProjectId, pOnlineProjectId, pFileName);
    FETCH curResources INTO recResource;
    CLOSE curResources;
    IF recResource.state = opencmsConstants.C_STATE_DELETED THEN
      EXECUTE IMMEDIATE 'delete from '||vResourceTable||' where resource_name = '''||pFileName||'''';
      vState := opencmsConstants.C_STATE_CHANGED;
    END IF;
    --vFileId := pResource.file_id;
    --IF pCopy = 'TRUE' THEN
    vFileId := getNextId(vFileTable);
    IF pProjectId = pOnlineProjectId THEN
      insert into cms_online_files (file_id, file_content)
      values (vFileId, pResource.file_content);
    ELSE
      insert into cms_files (file_id, file_content)
      values (vFileId, pResource.file_content);
    END IF;
    --END IF;
    vNewResourceId := getNextId(vResourceTable);
    BEGIN
      EXECUTE IMMEDIATE 'insert into '||vResourceTable||
                        ' (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, group_id,'||
                        ' project_id, file_id, access_flags, state, locked_by, launcher_type, launcher_classname,'||
                        ' date_created, date_lastmodified, resource_size, resource_lastmodified_by)'||
                        ' values ('||vNewResourceId||', '||pParentId||', '''||pFileName||''', '||
                        pResource.resource_type||', '||pResource.resource_flags||', '||pResource.user_id||', '||
                        pResource.group_id||', '||pProjectId||', '||vFileId||', '||pResource.access_flags||', '||
                        vState||', '||pResource.locked_by||', '||pResource.launcher_type||', '''||
                        pResource.launcher_classname||''', to_date('''||to_char(pResource.date_created,'dd.mm.yyyy hh24:mi:ss')||
                        ''',''dd.mm.yyyy hh24:mi:ss''), to_date('''||to_char(sysdate,'dd.mm.yyyy hh24:mi:ss')||
                        ''',''dd.mm.yyyy hh24:mi:ss''), '||pResource.resource_size||', '||pUserId||')';
    EXCEPTION
      WHEN DUP_VAL_ON_INDEX THEN
        userErrors.raiseUserError(userErrors.C_FILE_EXISTS);
    END;
    commit;
    OPEN oResource FOR 'select r.*, f.file_content from '||vResourceTable||' r, '||vFileTable||' f'||
                       ' where r.resource_id = '||vNewResourceId||
                       ' and r.file_id = f.file_id(+)';
  EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
      rollback;
      userErrors.raiseUserError(userErrors.C_FILE_EXISTS);
    WHEN OTHERS THEN
      IF curResources%ISOPEN THEN
        CLOSE curResources;
      END IF;
      IF oResource%ISOPEN THEN
        CLOSE oResource;
      END IF;
      rollback;
      raise_application_error(-20004, 'error when create file',true);
  END createFile;
----------------------------------------------------------------------------------------------
-- removes the folder pResource
----------------------------------------------------------------------------------------------
  PROCEDURE removeFolder(pUserId NUMBER, pProjectId NUMBER, pResourceID NUMBER, pResourceName VARCHAR2) IS
    curSubResource userTypes.anyCursor;
    recSubResource cms_resources%ROWTYPE;
    tableResource userTypes.resourceTable;
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    vTableName VARCHAR2(20);
  BEGIN
    IF pProjectId = vOnlineProject THEN
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    tableResource := getFilesInFolder(pUserId, pProjectId, pResourceName);
    FOR i IN 1..tableResource.COUNT LOOP
      recSubResource := tableResource(i);
      IF recSubResource.state != opencmsConstants.C_STATE_DELETED THEN
        userErrors.raiseUserError(userErrors.C_NOT_EMPTY);
      END IF;
    END LOOP;
    curSubResource := getFoldersInFolder(pUserId, pProjectId, pResourceName);
    LOOP
      FETCH curSubResource INTO recSubResource;
      EXIT WHEN curSubResource%NOTFOUND;
      IF recSubResource.state != opencmsConstants.C_STATE_DELETED THEN
        userErrors.raiseUserError(userErrors.C_NOT_EMPTY);
      END IF;
    END LOOP;
    CLOSE curSubResource;
    EXECUTE IMMEDIATE 'delete from '||vTableName||' where resource_id = '||pResourceId;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      IF curSubResource%ISOPEN THEN
      	CLOSE curSubResource;
      END IF;
      rollback;
      raise_application_error(-20004, 'error when create file',true);
  END removeFolder;
----------------------------------------------------------------------------------------------
-- updates the folder pResource
----------------------------------------------------------------------------------------------
  PROCEDURE writeFolder(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2) IS
    vState NUMBER;
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    vTableName VARCHAR2(20);
  BEGIN
    IF pProjectID = vOnlineProject THEN
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    IF pResource.state NOT IN (opencmsConstants.C_STATE_NEW, opencmsConstants.C_STATE_CHANGED)
       AND pChange = 'TRUE' THEN
      vState := opencmsConstants.C_STATE_CHANGED;
    ELSE
      vState := pResource.state;
    END IF;
    BEGIN
      EXECUTE IMMEDIATE 'update '||vTableName||
                        ' set resource_type = '||pResource.resource_type||', '||
                        ' resource_flags = '||pResource.resource_flags||', '||
                        ' user_id = '||pResource.user_id||', '||
                        ' group_id = '||pResource.group_id||', '||
                        ' project_id = '||pResource.project_id||', '||
                        ' access_flags = '||pResource.access_flags||', '||
                        ' state = '||vState||', '||
                        ' locked_by = '||pResource.locked_by||', '||
                        ' launcher_type = '||pResource.launcher_type||', '||
                        ' launcher_classname = '''||pResource.launcher_classname||''', '||
                        ' date_lastmodified = to_date('''||to_char(sysdate,'dd.mm.yyyy hh24:mi:ss')||
                        ''',''dd.mm.yyyy hh24:mi:ss''), '||
                        ' resource_lastmodified_by = '||pResource.resource_lastmodified_by||', '||
                        ' resource_size = '||pResource.resource_size||', '||
                        ' file_id = '||pResource.file_id||
                        ' where resource_id = '||pResource.resource_id;
    EXCEPTION
      WHEN OTHERS THEN
        rollback;
        raise_application_error(-20004, 'error when write folder',true);
    END;
    commit;
  END writeFolder;
----------------------------------------------------------------------------------------------
-- updates the file pResource
----------------------------------------------------------------------------------------------
  PROCEDURE writeFileHeader(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2) IS
    vState NUMBER := pResource.state;
    vNewFileId NUMBER := pResource.file_id;
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    vTableName VARCHAR2(20);
  BEGIN
    IF pProjectId = vOnlineProject THEN
      vTableName := 'CMS_ONLINE_RESOURCES';
    ELSE
      vTableName := 'CMS_RESOURCES';
    END IF;
    IF pResource.state NOT IN (opencmsConstants.C_STATE_NEW, opencmsConstants.C_STATE_CHANGED)
       AND pChange = 'TRUE' THEN
      vState := opencmsConstants.C_STATE_CHANGED;
    ELSE
      vState := pResource.state;
    END IF;
    BEGIN
      EXECUTE IMMEDIATE 'update '||vTableName||
                        ' set resource_type = '||pResource.resource_type||', '||
                        ' resource_flags = '||pResource.resource_flags||', '||
                        ' user_id = '||pResource.user_id||', '||
                        ' group_id = '||pResource.group_id||', '||
                        ' project_id = '||pResource.project_id||', '||
                        ' access_flags = '||pResource.access_flags||', '||
                        ' state = '||vState||', '||
                        ' locked_by = '||pResource.locked_by||', '||
                        ' launcher_type = '||pResource.launcher_type||', '||
                        ' launcher_classname = '''||pResource.launcher_classname||''', '||
                        ' date_lastmodified = to_date('''||to_char(sysdate,'dd.mm.yyyy hh24:mi:ss')||
                        ''',''dd.mm.yyyy hh24:mi:ss''), '||
                        ' resource_lastmodified_by = '||pResource.resource_lastmodified_by||', '||
                        ' resource_size = '||pResource.resource_size||', '||
                        ' file_id = '||vNewFileId||
                        ' where resource_id = '||pResource.resource_id;
    EXCEPTION
      WHEN OTHERS THEN
        rollback;
        raise_application_error(-20004, 'error when write file header',true);
    END;
    commit;
  END writeFileHeader;
----------------------------------------------------------------------------------------------
-- updates the file pResource including cms_files
----------------------------------------------------------------------------------------------
  PROCEDURE writeFile(pProjectID IN NUMBER, pResource IN userTypes.fileRecord, pChange IN VARCHAR2) IS
    curFileHeader userTypes.anyCursor;
    recFileHeader cms_resources%ROWTYPE;
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
  BEGIN
    recFileHeader.resource_id := pResource.resource_id;
    recFileHeader.parent_id := pResource.parent_id;
    recFileHeader.resource_name := pResource.resource_name;
    recFileHeader.resource_type := pResource.resource_type;
    recFileHeader.resource_flags := pResource.resource_flags;
    recFileHeader.user_id := pResource.user_id;
    recFileHeader.group_id := pResource.group_id;
    recFileHeader.project_id := pResource.project_id;
    recFileHeader.file_id := pResource.file_id;
    recFileHeader.access_flags := pResource.access_flags;
    recFileHeader.state := pResource.state;
    recFileHeader.locked_by := pResource.locked_by;
    recFileHeader.launcher_type := pResource.launcher_type;
    recFileHeader.launcher_classname := pResource.launcher_classname;
    recFileHeader.date_created := pResource.date_created;
    recFileHeader.date_lastmodified := pResource.date_lastmodified;
    recFileHeader.resource_size := pResource.resource_size;
    recFileHeader.resource_lastmodified_by := pResource.resource_lastmodified_by;
    writeFileHeader(pProjectId, recFileHeader, pChange);
    IF pProjectId = vOnlineProject THEN
      update cms_online_files set file_content = pResource.file_content where file_id = pResource.file_id;
    ELSE
      update cms_files set file_content = pResource.file_content where file_id = pResource.file_id;
    END IF;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      raise_application_error(-20004, 'error when write file',true);
  END writeFile;
--------------------------------------------------------------------------------------------------
-- Copy the file
--------------------------------------------------------------------------------------------------
  PROCEDURE copyFile(pProjectId NUMBER, pUserId NUMBER, pSource VARCHAR2, pDestination VARCHAR2) IS
    curFileHeader userTypes.anyCursor;
    curFolder userTypes.anyCursor;
    curFile userTypes.anyCursor;
    recFileHeader cms_resources%ROWTYPE;
    recFolder cms_resources%ROWTYPE;
    recFile userTypes.fileRecord;
    vFolderName VARCHAR2(1000) := '';
    vFileName VARCHAR2(100) := '';
    curNewResource userTypes.anyCursor;
    recNewResource userTypes.fileRecord;
    vOnlineProjectId NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
  BEGIN
	-- read the source-file, to check readaccess
	curFileHeader := readFileHeader(pUserId, pProjectId, pSource);
	FETCH curFileHeader INTO recFileHeader;
    CLOSE curFileHeader;
	-- split the destination into file and foldername
	IF substr(pDestination,-1) = '/' THEN
	  vFileName := substr(recFileHeader.resource_name,instr(recFileHeader.resource_name,'/',-1,1)+1);
	  vFolderName := pDestination;
	ELSE
	  vFolderName := substr(pDestination, 1, instr(pDestination,'/',-1,1));
	  vFileName := substr(pDestination, instr(pDestination,'/',-1,1)+1);
	END IF;
	curFolder := readFolder(pUserId, pProjectId, vFolderName);
	FETCH curFolder INTO recFolder;
	CLOSE curFolder;
	IF recFolder.resource_id IS NOT NULL THEN
	  IF opencmsAccess.accessCreate(pUserId, pProjectId, recFolder.resource_id) = 1 THEN
	    -- write-access was granted - copy the file and the metainfos
	    curFile := readFileNoAccess(pUserId, pProjectId, vOnlineProjectId, pSource);
        FETCH curFile INTO recFile;
        CLOSE curFile;
        IF recFile.resource_id IS NOT NULL THEN
          createFile(pProjectId, vOnlineProjectId, recFile, pUserId, recFolder.resource_id, vFolderName||vFileName, 'TRUE', curNewResource);
          FETCH curNewResource INTO recNewResource;
          CLOSE curNewResource;
	      -- copy the metainfos
	      lockResource(pUserId, pProjectId, vOnlineProjectId, recNewResource.resource_name, 'TRUE');
	      opencmsProperty.writeProperties(pUserId, pProjectId, recNewResource.resource_id, recNewResource.resource_type,
	                                      opencmsProperty.readAllProperties(pUserId, pProjectId, recFile.resource_name));
        ELSE
          userErrors.raiseUserError(userErrors.C_NOT_FOUND);
        END IF;
      ELSE
        userErrors.raiseUserError(userErrors.C_NO_ACCESS);
      END IF;
    ELSE
      userErrors.raiseUserError(userErrors.C_NOT_FOUND);
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      IF curFileHeader%ISOPEN THEN
        CLOSE curFileHeader;
      END IF;
      IF curFolder%ISOPEN THEN
        CLOSE curFolder;
      END IF;
      IF curFile%ISOPEN THEN
        CLOSE curFile;
      END IF;
      IF curNewResource%ISOPEN THEN
        CLOSE curNewResource;
      END IF;
      raise_application_error(-20004, 'error when copy file',true);
  END copyFile;
----------------------------------------------------------------------------------------------
-- returns a cursor for the files in this folder
-- => same procedure as getFoldersInFolder but with resource_type != C_TYPE_FOLDER
----------------------------------------------------------------------------------------------
  FUNCTION getFilesInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.resourceTable IS

    CURSOR curFilesOffline(cParentId NUMBER, cOnlinePRoject NUMBER) IS
           select cms_resources.resource_id, cms_resources.parent_id,
                  cms_resources.resource_name, cms_resources.resource_type,
                  cms_resources.resource_flags, cms_resources.user_id,
                  cms_resources.group_id, cms_projectresources.project_id,
                  cms_resources.file_id, cms_resources.access_flags, cms_resources.state,
                  cms_resources.locked_by, cms_resources.launcher_type,
                  cms_resources.launcher_classname, cms_resources.date_created,
                  cms_resources.date_lastmodified, cms_resources.resource_size,
                  cms_resources.resource_lastmodified_by
                  from cms_resources, cms_projectresources
                  where cms_resources.parent_id=cParentId
                  and cms_resources.resource_name like concat(cms_projectresources.resource_name,'%')
                  and cms_resources.resource_type != opencmsConstants.C_TYPE_FOLDER
                  and cms_projectresources.project_id IN (pProjectId, cOnlineProject)
                  order by cms_resources.resource_name, cms_projectresources.project_id;

     CURSOR curFilesOnline(cParentId NUMBER) IS
            select * from cms_online_resources
                   where parent_id=cParentId
                   and resource_type != opencmsConstants.C_TYPE_FOLDER
                   and project_id = pProjectId
                   order by resource_name;

    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    recFiles    cms_resources%ROWTYPE;
    vQueryString VARCHAR2(32767) := '';
    retResources userTypes.resourceTable;
    newIndex NUMBER;
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
  BEGIN
    bAnyList := '';
    curResource := readFolder(pUserId, pProjectId, pResourceName);
    FETCH curResource INTO recResource;
    IF curResource%NOTFOUND THEN
      -- error reading the folder: open cursor with select that returns no rows
      CLOSE curResource;
      RETURN retResources;
    END IF;
    CLOSE curResource;
    -- has the user access for the folder
    IF opencmsAccess.accessRead(pUserId, pProjectId, recResource.resource_name) = 1 THEN
      IF pProjectId = vOnlineProject THEN
        OPEN curFilesOnline(recResource.resource_id);
      ELSE
        OPEN curFilesOffline(recResource.resource_id, vOnlineProject);
      END IF;
      LOOP
        IF pProjectId = vOnlineProject THEN
          FETCH curFilesOnline INTO recFiles;
          EXIT WHEN curFilesOnline%NOTFOUND;
        ELSE
          FETCH curFilesOffline INTO recFiles;
          EXIT WHEN curFilesOffline%NOTFOUND;
        END IF;
        -- has the user access for this resource
        IF (opencmsAccess.accessOwner(pUserId, pProjectId, recFiles.resource_name, opencmsConstants.C_ACCESS_OWNER_READ) = 1
            OR opencmsAccess.accessOther(pUserId, pProjectId, recFiles.resource_name, opencmsConstants.C_ACCESS_PUBLIC_READ) = 1
            OR opencmsAccess.accessGroup(pUserId, pProjectId, recFiles.resource_name, opencmsConstants.C_ACCESS_GROUP_READ) = 1) THEN
          IF addPathInList(recFiles.resource_name) THEN
          	newIndex := retResources.COUNT + 1;
            retResources(newIndex) := recFiles;
          END IF;
        END IF;
      END LOOP;
      IF pProjectId = vOnlineProject THEN
        CLOSE curFilesOnline;
      ELSE
        CLOSE curFilesOffline;
      END IF;
    END IF;
    bAnyList := '';
    RETURN retResources;
  END getFilesInFolder;
---------------------------------------------------------------------------------------------
-- returns a cursor for the folders in this folder
-- => same procedure as getFilesInFolder but with resource_type = C_TYPE_FOLDER
---------------------------------------------------------------------------------------------
  FUNCTION getFoldersInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor IS

    CURSOR curFoldersOffline(cParentId NUMBER, cOnlineProject NUMBER) IS
           select cms_resources.resource_id, cms_resources.parent_id,
                  cms_resources.resource_name, cms_resources.resource_type,
                  cms_resources.resource_flags, cms_resources.user_id,
                  cms_resources.group_id, cms_projectresources.project_id,
                  cms_resources.file_id, cms_resources.access_flags, cms_resources.state,
                  cms_resources.locked_by, cms_resources.launcher_type,
                  cms_resources.launcher_classname, cms_resources.date_created,
                  cms_resources.date_lastmodified, cms_resources.resource_size,
                  cms_resources.resource_lastmodified_by
                  from cms_resources, cms_projectresources
                  where cms_resources.parent_id= cParentId
                  and cms_resources.resource_name like concat(cms_projectresources.resource_name,'%')
                  and cms_resources.resource_type = opencmsConstants.C_TYPE_FOLDER
                  and cms_projectresources.project_id IN (pProjectId, cOnlineProject)
                  order by cms_resources.resource_name;

    CURSOR curFoldersOnline(cParentId NUMBER) IS
           select * from cms_online_resources
                  where parent_id=cParentId
                  and resource_type = opencmsConstants.C_TYPE_FOLDER
                  and project_id = pProjectId
                  order by resource_name;

    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    recFiles    cms_resources%ROWTYPE;
    vQueryString VARCHAR2(32767) := '';
    vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    vTableName VARCHAR2(20);
  BEGIN
    bAnyList := '';
    curResource := readFolder(pUserId, pProjectId, pResourceName);
    FETCH curResource INTO recResource;
    IF curResource%NOTFOUND THEN
      -- error reading the folder: open cursor with select that returns no rows
      CLOSE curResource;
      --OPEN curResource FOR select * from cms_resources where resource_id = -1;
      RETURN curResource;
    END IF;
    CLOSE curResource;
    -- has the user access for the folder
    IF opencmsAccess.accessRead(pUserId, pProjectId, recResource.resource_name) = 1 THEN
      IF pProjectId = vOnlineProject THEN
        vTableName := 'CMS_ONLINE_RESOURCES';
        OPEN curFoldersOnline(recResource.resource_id);
      ELSE
        vTableName := 'CMS_RESOURCES';
        OPEN curFoldersOffline(recResource.resource_id, vOnlineProject);
      END IF;
      LOOP
        IF pProjectId = vOnlineProject THEN
          FETCH curFoldersOnline INTO recFiles;
          EXIT WHEN curFoldersOnline%NOTFOUND;
        ELSE
          FETCH curFoldersOffline INTO recFiles;
          EXIT WHEN curFoldersOffline%NOTFOUND;
        END IF;
        -- has the user access for this resource
        IF (opencmsAccess.accessOwner(pUserId, pProjectId, recFiles.resource_name, opencmsConstants.C_ACCESS_OWNER_READ) = 1
            OR opencmsAccess.accessOther(pUserId, pProjectId, recFiles.resource_name, opencmsConstants.C_ACCESS_PUBLIC_READ) = 1
            OR opencmsAccess.accessGroup(pUserId, pProjectId, recFiles.resource_name, opencmsConstants.C_ACCESS_GROUP_READ) = 1) THEN
          IF addPathInList(recFiles.resource_name) THEN
            vQueryString := vQueryString||' union select * from '||vTableName||' where resource_id = '||to_char(recFiles.resource_id);
          END IF;
        END IF;
      END LOOP;
      IF pProjectId = vOnlineProject THEN
        CLOSE curFoldersOnline;
      ELSE
        CLOSE curFoldersOffline;
      END IF;
      -- open cursor with the string vQueryString without the first "union"
      vQueryString := substr(vQueryString, 8);
      IF substr(vQueryString,1,6) = 'select' THEN
        OPEN curResource FOR 'select * from ('||vQueryString||') order by resource_name';
      END IF;
    END IF;
    bAnyList := '';
    RETURN curResource;
  END getFoldersInFolder;
-------------------------------------------------------------------------------------------
-- changes the state for the resource
-------------------------------------------------------------------------------------------
  PROCEDURE chstate(pUserId IN NUMBER, pProjectId IN NUMBER, pResourceName IN VARCHAR2, pState IN NUMBER) IS
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
  BEGIN
    IF substr(pResourceName, -1, 1) = '/' THEN
      curResource := readFolder(pUserId, pProjectId, pResourceName);
    ELSE
      curResource := readFileHeader(pUserId, pProjectId, pResourceName);
    END IF;
    FETCH curResource INTO recResource;
    CLOSE curResource;
    recResource.state := pState;
    IF opencmsAccess.accessWrite(pUserId, pProjectId, recResource.resource_id) = 1 THEN
      IF substr(pResourceName, -1, 1) = '/' THEN
        writeFolder(pProjectID, recResource, 'FALSE');
      ELSE
        writeFileHeader(pProjectID, recResource, 'FALSE');
      END IF;
    ELSE
      userErrors.raiseUserError(userErrors.C_NO_ACCESS);
    END IF;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      IF curResource%ISOPEN THEN
        CLOSE curResource;
      END IF;
      rollback;
      raise_application_error(-20004, 'error when change state',true);
  END chstate;
------------------------------------------------------------------------------
-- backup of published folders
------------------------------------------------------------------------------
  PROCEDURE backupFolder(pProjectId IN NUMBER, pFolder IN cms_resources%ROWTYPE, pVersionId IN NUMBER, pPublishDate IN DATE) IS
    CURSOR curProperties(cResourceId NUMBER) IS
           select * from cms_properties
                  where resource_id = cResourceId;

    recProperties cms_properties%ROWTYPE;
    vNewResourceId NUMBER;
    vNewPropertiesId NUMBER;
    vOwnerName VARCHAR2(135);
    vGroupName VARCHAR2(16);
    vLastModifiedByName VARCHAR2(135);
  BEGIN
    BEGIN
      select user_name||' '||user_firstname||' '||user_lastname into vOwnerName
             from cms_users where user_id = pFolder.user_id;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
    	vOwnerName := '';
      WHEN OTHERS THEN
        RAISE;
    END;
    BEGIN
      select user_name||' '||user_firstname||' '||user_lastname into vLastModifiedByName
             from cms_users where user_id = pFolder.resource_lastmodified_by;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
    	vLastModifiedByName := '';
      WHEN OTHERS THEN
        RAISE;
    END;
    BEGIN
      select group_name into vGroupName
             from cms_groups where group_id = pFolder.group_id;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
    	vGroupName := '';
      WHEN OTHERS THEN
        RAISE;
    END;
    vNewResourceId := getNextId('CMS_BACKUP_RESOURCES');
    insert into cms_backup_resources
           (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, user_name,
            group_id, group_name, project_id, file_id, access_flags, state, launcher_type,
            launcher_classname, date_created, date_lastmodified, resource_size,
            resource_lastmodified_by, resource_lastmodified_by_name, version_id)
    values (vNewResourceId, -1, pFolder.resource_name, pFolder.resource_type, pFolder.resource_flags,
            pFolder.user_id, vOwnerName, pFolder.group_id, vGroupName, pProjectId, pFolder.file_id,
            pFolder.access_flags, pFolder.state, pFolder.launcher_type, pFolder.launcher_classname,
            pPublishDate, pFolder.date_lastmodified, pFolder.resource_size, pFolder.resource_lastmodified_by,
            vLastModifiedByName, pVersionId);
    OPEN curProperties(pFolder.resource_id);
    LOOP
      FETCH curProperties INTO recProperties;
      EXIT WHEN curProperties%NOTFOUND;
      vNewPropertiesId := getNextId('CMS_BACKUP_PROPERTIES');
      insert into cms_backup_properties (property_id, propertydef_id, resource_id, property_value)
      values(vNewPropertiesId, recProperties.propertydef_id, vNewResourceId, recProperties.property_value);
    END LOOP;
    CLOSE curProperties;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      IF curProperties%ISOPEN THEN
        CLOSE curProperties;
      END IF;
      rollback;
      raise_application_error(-20004, 'error when backup folder',true);
  END backupFolder;
------------------------------------------------------------------------------
-- backup of published files
------------------------------------------------------------------------------
  PROCEDURE backupFile(pProjectId IN NUMBER, pFile IN userTypes.fileRecord, pVersionId IN NUMBER, pPublishDate IN DATE) IS
    CURSOR curProperties(cResourceId NUMBER) IS
           select * from cms_properties
                  where resource_id = cResourceId;

    recProperties cms_properties%ROWTYPE;
    vNewFileId NUMBER;
    vNewResourceId NUMBER;
    vNewPropertiesId NUMBER;
    vOwnerName VARCHAR2(135);
    vGroupName VARCHAR2(16);
    vLastModifiedByName VARCHAR2(135);
  BEGIN
    BEGIN
      select user_name||' '||user_firstname||' '||user_lastname into vOwnerName
             from cms_users where user_id = pFile.user_id;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
    	vOwnerName := '';
      WHEN OTHERS THEN
        RAISE;
    END;
    BEGIN
      select user_name||' '||user_firstname||' '||user_lastname into vLastModifiedByName
             from cms_users where user_id = pFile.resource_lastmodified_by;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
    	vLastModifiedByName := '';
      WHEN OTHERS THEN
        RAISE;
    END;
    BEGIN
      select group_name into vGroupName
             from cms_groups where group_id = pFile.group_id;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
    	vGroupName := '';
      WHEN OTHERS THEN
        RAISE;
    END;
    vNewFileId := getNextId('CMS_BACKUP_FILES');
    insert into cms_backup_files (file_id, file_content) values(vNewFileId, pFile.file_content);
    vNewResourceId := getNextId('CMS_BACKUP_RESOURCES');
    insert into cms_backup_resources
           (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, user_name,
            group_id, group_name, project_id, file_id, access_flags, state, launcher_type,
            launcher_classname, date_created, date_lastmodified, resource_size,
            resource_lastmodified_by, resource_lastmodified_by_name, version_id)
    values (vNewResourceId, -1, pFile.resource_name, pFile.resource_type, pFile.resource_flags,
            pFile.user_id, vOwnerName, pFile.group_id, vGroupName, pProjectId, vNewFileId, pFile.access_flags,
            pFile.state, pFile.launcher_type, pFile.launcher_classname, pPublishDate,
            pFile.date_lastmodified, pFile.resource_size, pFile.resource_lastmodified_by,
            vLastModifiedByName, pVersionId);
    OPEN curProperties(pFile.resource_id);
    LOOP
      FETCH curProperties INTO recProperties;
      EXIT WHEN curProperties%NOTFOUND;
      vNewPropertiesId := getNextId('CMS_BACKUP_PROPERTIES');
      insert into cms_backup_properties (property_id, propertydef_id, resource_id, property_value)
      values(vNewPropertiesId, recProperties.propertydef_id, vNewResourceId, recProperties.property_value);
    END LOOP;
    CLOSE curProperties;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      IF curProperties%ISOPEN THEN
        CLOSE curProperties;
      END IF;
      rollback;
      raise_application_error(-20004, 'error when backup file',true);
  END backupFile;
------------------------------------------------------------------------------
-- returns the superpath for the resource with resource_name = pResourceName
------------------------------------------------------------------------------
    FUNCTION getParent(pResourceName VARCHAR2) RETURN VARCHAR2 IS
      vParentName cms_resources.resource_name%TYPE := NULL;
    BEGIN
      IF pResourceName != opencmsConstants.C_ROOT THEN
        vParentName := substr(pResourceName, 1, length(pResourceName)-1);
        vParentName := substr(vParentName, 1, instr(vParentName, '/', -1));
      ELSE
        RETURN NULL;
      END IF;
      RETURN vParentName;
    END getParent;
-------------------------------------------------------------------------
-- returns the superid for the resource with resource_id = pResourceID
-------------------------------------------------------------------------
    FUNCTION getParentId(pProjectID NUMBER, pResourceId NUMBER) RETURN NUMBER IS
      vParentId NUMBER;
      vOnlineProject NUMBER := opencmsconstants.C_PROJECT_ONLINE_ID;
    BEGIN
      IF pProjectId = vOnlineProject THEN
        select decode(parent_id, -1, NULL, parent_id) into vParentId
               from cms_online_resources
               where resource_id = pResourceId;
      ELSE
        select decode(parent_id, -1, NULL, parent_id) into vParentId
               from cms_resources
               where resource_id = pResourceId;
      END IF;
      RETURN vParentId;
    EXCEPTION
      WHEN OTHERS THEN
         RETURN NULL;
    END getParentId;
---------------------------------------------------------------------------------------
-- private function checks if this path is already in the list, if not => edit the list
-- and returns boolean
---------------------------------------------------------------------------------------
  FUNCTION addPathInList(pName VARCHAR2) RETURN BOOLEAN IS
    newIndex NUMBER;
    element NUMBER;
  BEGIN
    FOR element IN 1..bPathList.COUNT
    LOOP
      IF bPathList(element) = pName THEN
        RETURN FALSE;
      END IF;
	END LOOP;
	IF element > 1 THEN
	  newIndex := element+1;
	ELSE
	  newIndex := 1;
	END IF;
    bPathList(newIndex) := pName;
	RETURN TRUE;
  END addPathInList;
-------------------------------------------------------------------------
END;
/
