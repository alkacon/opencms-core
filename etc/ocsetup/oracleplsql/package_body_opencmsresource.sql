CREATE OR REPLACE
PACKAGE BODY opencmsresource IS
--------------------------------------------------------------------------------------------------------------
-- declare variables/procedures/functions which are used in this package
--------------------------------------------------------------------------------------------------------------
  bAnyList VARCHAR2(32767);
  bResourceList VARCHAR2(32767) := '';
  FUNCTION addInList(pName VARCHAR2) RETURN BOOLEAN;
--------------------------------------------------------------------------------------------------------------
-- this procedure is called from DbAccess. It calls the second lockResource-procedure and returns a resultset
-- which is needed to update the resource-cache
--------------------------------------------------------------------------------------------------------------
  PROCEDURE lockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2, pForce IN VARCHAR2, pResource OUT userTypes.anyCursor) IS
  BEGIN
    bResourceList := '';
    -- first lock the resources
    lockResource(pUserId, pProjectId, pFolderName, pForce);
    -- now build the cursor which contains the locked resources to return the resultset
    --IF length(bResourceList) > 0 THEN
      -- open the cursor with the locked resources
      OPEN pResource FOR 'select * from cms_resources where project_id = '||pProjectId||
                         ' and resource_name like '''||pFolderName||'%'' and locked_by = '||pUserId;
    --END IF;
    bResourceList := '';
  END;
--------------------------------------------------------------------------------------------------------------
-- procedure which locks the resource and if the resource is folder all subresources
--------------------------------------------------------------------------------------------------------------
  PROCEDURE lockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2, pForce IN VARCHAR2) IS
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vFolderName cms_resources.resource_name%TYPE;
  BEGIN
   -- if pFolderName is the id and not the path then read the resource_name for the resource
   -- read all Information about this resource
    IF instr(pFolderName,'/') = 0 THEN
      select resource_name into vFolderName from cms_resources where resource_id = pFolderName;
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
      update cms_resources set locked_by = pUserId
             where resource_id = recResource.resource_id;
      -- put only one resource_id into the resource-list to mark that there was something locked
      bResourceList := to_char(recResource.resource_id);
      -- if the resource is folder then lock all subresources
      IF substr(vFolderName, -1) = '/' THEN
        -- all files in folder
        curResource := getFilesInFolder(pUserId, pProjectId, vFolderName);
        LOOP
		  BEGIN
            FETCH curResource INTO recResource;
            EXIT WHEN curResource%NOTFOUND;
            IF recResource.state != opencmsConstants.C_STATE_DELETED THEN
              lockResource(pUserId, pProjectId, recResource.resource_name, 'TRUE');
            END IF;
          EXCEPTION
            WHEN invalid_cursor THEN
              exit;
          END;
        END LOOP;
        IF curResource%ISOPEN THEN
          CLOSE curResource;
        END IF;
        -- all folders in the folder and their files and folders etc.
        curResource := getFoldersInFolder(pUserId, pProjectId, vFolderName);
        LOOP
  		  BEGIN
            FETCH curResource INTO recResource;
            EXIT WHEN curResource%NOTFOUND;
            IF recResource.state != opencmsConstants.C_STATE_DELETED THEN
              lockResource(pUserId, pProjectId, recResource.resource_name, 'TRUE');
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
  BEGIN
    bResourceList := '';
    -- first unlock the resources
    unlockResource(pUserId, pProjectId, pFolderName);
    -- now build the cursor which contains the unlocked resources to return the resultset
    --IF length(bResourceList) > 0 THEN
      OPEN pResource FOR 'select * from cms_resources where project_id='||pProjectId||
                         ' and resource_name like '''||pFolderName||'%'' and locked_by='||opencmsConstants.C_UNKNOWN_ID;
    --END IF;
    bResourceList := '';
  END unlockResource;
--------------------------------------------------------------------------------------------------------------
-- function which unlocks the resource and if the resource is folder all subresources
--------------------------------------------------------------------------------------------------------------
  PROCEDURE unlockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2) IS
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    vFolderName cms_resources.resource_name%TYPE;
  BEGIN
   -- if pFolderName is the id and not the path then read the resource_name for the resource
   -- read all Information about this resource
    IF instr(pFolderName,'/') = 0 THEN
      select resource_name into vFolderName from cms_resources where resource_id = pFolderName;
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
          update cms_resources set locked_by = opencmsConstants.C_UNKNOWN_ID
                 where resource_id = recResource.resource_id;
          -- need only one resource-id to mark that there was something unlocked
          bResourceList := to_char(recResource.resource_id);
        ELSE
          userErrors.raiseUserError(userErrors.C_LOCKED);
        END IF;
      END IF;
      -- if the resource is folder then lock all subresources
      IF substr(vFolderName, -1) = '/' THEN
        -- all files in folder
        curResource := getFilesInFolder(pUserId, pProjectId, vFolderName);
        LOOP
          BEGIN
            FETCH curResource INTO recResource;
            EXIT WHEN curResource%NOTFOUND;
            IF recResource.state != opencmsConstants.C_STATE_DELETED THEN
              unlockResource(pUserId, pProjectId, recResource.resource_name);
            END IF;
          EXCEPTION
            WHEN invalid_cursor THEN
              exit;
          END;
        END LOOP;
        IF curResource%ISOPEN THEN
          CLOSE curResource;
        END IF;
        -- all folders in the folder and their files and folders etc.
        curResource := getFoldersInFolder(pUserId, pProjectId, vFolderName);
        LOOP
          BEGIN
            FETCH curResource INTO recResource;
            EXIT WHEN curResource%NOTFOUND;
            IF recResource.state != opencmsConstants.C_STATE_DELETED THEN
              unlockResource(pUserId, pProjectId, recResource.resource_name);
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
    recFolder cms_resources%ROWTYPE;
  BEGIN
    curFolder := readFolder(pUserId, pProjectID, pFolderName);
    FETCH curFolder INTO recFolder;
    CLOSE curFolder;
    -- check the access for the existing file
    IF recFolder.resource_id IS NOT NULL THEN
      IF opencmsAccess.accessRead(pUserId, pProjectId, recFolder.resource_id) = 0 THEN
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
    curOnlineProject userTypes.anyCursor;
    recOnlineProject cms_projects%ROWTYPE;
    curFolder userTypes.anyCursor;
  BEGIN
    curOnlineProject := opencmsProject.onlineProject(pProjectId);
    FETCH curOnlineProject INTO recOnlineProject;
    CLOSE curOnlineProject;
    -- read the resource from offline project or the online project, the first resource is used
    OPEN curFolder FOR select * from cms_resources
                       where resource_name = pFolderName
                       and project_id in (pProjectId, recOnlineProject.project_id)
                       order by project_id desc;
    RETURN curFolder;
  END readFolder;
--------------------------------------------------------------------------------------------------------------
-- returns a cursor for a resource with resource_name = pFileName
-- for the project with project_id = pProjectId or for online-projekt
--------------------------------------------------------------------------------------------------------------
  FUNCTION readFileHeader(pUserId NUMBER, pProjectID NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor IS
    curOnlineProject userTypes.anyCursor;
    recOnlineProject cms_projects%ROWTYPE;
    curResource userTypes.anyCursor;
    recFile cms_resources%ROWTYPE;
  BEGIN
    curOnlineProject := opencmsProject.onlineProject(pProjectId);
    FETCH curOnlineProject INTO recOnlineProject;
    CLOSE curOnlineProject;
    -- is pFileName a folder? => readFolder
    IF substr(pFileName, -1) = '/' THEN
      curResource := readFolder(pUserId, pProjectId, pFileName);
      RETURN curResource;
    END IF;
    curResource := readFileHeader(pUserId, pProjectId, recOnlineProject.project_id, pFileName);
    FETCH curResource INTO recFile;
    CLOSE curResource;
    -- check the access for the existing file
    IF recFile.resource_id IS NOT NULL THEN
      IF opencmsAccess.accessRead(pUserId, pProjectId, recFile.resource_id) = 0 THEN
          -- error: no access for this file
        userErrors.raiseUserError(userErrors.C_ACCESS_DENIED);
      END IF;
    END IF;
    curResource := readFileHeader(pUserId, pProjectId, recOnlineProject.project_id, pFileName);
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
    OPEN curFile FOR select * from cms_resources
                     where resource_name = pFileName
                     and project_id in (pProjectId, pOnlineProjectId)
                     order by project_id desc;
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
  	OPEN curResource FOR select r.*, f.file_content from cms_resources r, cms_files f
                           where r.resource_name = pFileName
                           and r.project_id in (pProjectId, pOnlineProjectId)
                           and r.file_id = f.file_id(+)
                           order by project_id desc;
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
      IF opencmsAccess.accessRead(pUserId, pProjectId, recFile.resource_id) = 0 THEN
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
    curOnlineProject userTypes.anyCursor;
    recOnlineProject cms_projects%ROWTYPE;
  BEGIN
    curOnlineProject := opencmsProject.onlineProject(pProjectId);
    FETCH curOnlineProject INTO recOnlineProject;
    CLOSE curOnlineProject;
    -- first read the file
    curResource := readFileNoAccess(pUserId, pProjectId, recOnlineProject.project_id, pFileName);
    FETCH curResource INTO recFile;
    CLOSE curResource;
    -- check the access for the existing file
    IF recFile.resource_id IS NOT NULL THEN
      IF recFile.state = opencmsConstants.C_STATE_DELETED THEN
	    userErrors.raiseUserError(userErrors.C_RESOURCE_DELETED);
      END IF;
      IF opencmsAccess.accessRead(pUserId, pProjectId, recFile.resource_id) = 0 THEN
          -- error: no access for this file
        userErrors.raiseUserError(userErrors.C_ACCESS_DENIED);
      END IF;
    END IF;
    -- because the cursor was fetched it has to be read again
    curResource := readFileNoAccess(pUserId, pProjectId, recOnlineProject.project_id, pFileName);
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
  BEGIN
    select user_default_group_id into vUserGroupId from cms_users where user_id = pUserId;
    curFolder := readFolder(pUserId, pProjectId, pFolderName);
    FETCH curFolder INTO recFolder;
    CLOSE curFolder;
    IF recFolder.state = opencmsConstants.C_STATE_DELETED THEN
      removeFolder(pUserId, pProjectId, recFolder.resource_id, pFolderName);
      vState := opencmsConstants.C_STATE_CHANGED;
    END IF;
    vNewResourceId := getNextId(opencmsConstants.C_TABLE_RESOURCES);
    insert into cms_resources
          (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, group_id,
           project_id, file_id, access_flags, state, locked_by, launcher_type, launcher_classname,
           date_created, date_lastmodified, resource_size, resource_lastmodified_by)
    values
          (vNewResourceId, pParentResId, pFolderName, 0, pFlags, pUserId, vUserGroupId, pProjectId,
           opencmsConstants.C_UNKNOWN_ID, opencmsConstants.C_ACCESS_DEFAULT_FLAGS, vState,
           opencmsConstants.C_UNKNOWN_ID, opencmsConstants.C_UNKNOWN_LAUNCHER_ID, opencmsConstants.C_UNKNOWN_LAUNCHER,
           sysdate, sysdate, 0, pUserId);
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
      RAISE;
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
  BEGIN
    IF pProjectId = pOnlineProjectId THEN
      vState := pResource.state;
    END IF;
    -- if the folder in destination-project exists and is marked as deleted then delete the folder
    curResources := readFolder(pUserId, pProjectId, pFolderName);
    FETCH curResources INTO recResource;
    CLOSE curResources;
    IF recResource.state = opencmsConstants.C_STATE_DELETED THEN
      removeFolder(pUserId, pProjectId, recResource.resource_id, pFolderName);
      vState := opencmsConstants.C_STATE_CHANGED;
    END IF;
    vNewResourceId := getNextId(opencmsConstants.C_TABLE_RESOURCES);
    insert into cms_resources
          (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, group_id,
           project_id, file_id, access_flags, state, locked_by, launcher_type, launcher_classname,
           date_created, date_lastmodified, resource_size, resource_lastmodified_by)
    values
          (vNewResourceId, pParentId, pFolderName, pResource.resource_type, pResource.resource_flags, pResource.user_id, pResource.group_id, pProjectId,
           -1, pResource.access_flags, vState,
           pResource.locked_by, pResource.launcher_type, pResource.launcher_classname,
           pResource.date_created, sysdate, 0, pUserId);
    commit;
    OPEN oResource FOR select * from cms_resources where resource_id = vNewResourceId;
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
      RAISE;
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
  BEGIN
    IF pProjectId = pOnlineProjectId THEN
      vState := pResource.state;
    END IF;
    -- if the folder in destination-project exists and is marked as deleted then delete the folder
    curResources := readFileHeader(pUserId, pProjectId, pOnlineProjectId, pFileName);
    FETCH curResources INTO recResource;
    CLOSE curResources;
    IF recResource.state = opencmsConstants.C_STATE_DELETED THEN
      delete from cms_resources where project_id = pProjectId and resource_name = pFileName;
      vState := opencmsConstants.C_STATE_CHANGED;
    END IF;
    vFileId := pResource.file_id;
    IF pCopy = 'TRUE' THEN
      vFileId := getNextId(opencmsConstants.C_TABLE_FILES);
      insert into cms_files (file_id, file_content)
      select vFileId, file_content from cms_files where file_id = pResource.file_id;
    END IF;
    vNewResourceId := getNextId(opencmsConstants.C_TABLE_RESOURCES);
    BEGIN
      insert into cms_resources
          (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, group_id,
           project_id, file_id, access_flags, state, locked_by, launcher_type, launcher_classname,
           date_created, date_lastmodified, resource_size, resource_lastmodified_by)
      values
          (vNewResourceId, pParentId, pFileName, pResource.resource_type, pResource.resource_flags,
           pResource.user_id, pResource.group_id, pProjectId, vFileId, pResource.access_flags, vState,
           pResource.locked_by, pResource.launcher_type, pResource.launcher_classname,
           pResource.date_created, sysdate, pResource.resource_size, pUserId);
    EXCEPTION
      WHEN DUP_VAL_ON_INDEX THEN
        userErrors.raiseUserError(userErrors.C_FILE_EXISTS);
    END;
    commit;
    OPEN oResource FOR select r.*, f.file_content from cms_resources r, cms_files f
                              where r.resource_id = vNewResourceId
                              and r.file_id = f.file_id(+);
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
      RAISE;
  END createFile;
----------------------------------------------------------------------------------------------
-- removes the folder pResource
----------------------------------------------------------------------------------------------
  PROCEDURE removeFolder(pUserId NUMBER, pProjectId NUMBER, pResourceID NUMBER, pResourceName VARCHAR2) IS
    curSubResource userTypes.anyCursor;
    recSubResource cms_resources%ROWTYPE;
  BEGIN
    curSubResource := getFilesInFolder(pUserId, pProjectId, pResourceName);
    LOOP
      FETCH curSubResource INTO recSubResource;
      EXIT WHEN curSubResource%NOTFOUND;
      IF recSubResource.state != opencmsConstants.C_STATE_DELETED THEN
        userErrors.raiseUserError(userErrors.C_NOT_EMPTY);
      END IF;
    END LOOP;
    CLOSE curSubResource;
    curSubResource := getFoldersInFolder(pUserId, pProjectId, pResourceName);
    LOOP
      FETCH curSubResource INTO recSubResource;
      EXIT WHEN curSubResource%NOTFOUND;
      IF recSubResource.state != opencmsConstants.C_STATE_DELETED THEN
        userErrors.raiseUserError(userErrors.C_NOT_EMPTY);
      END IF;
    END LOOP;
    CLOSE curSubResource;
    delete from cms_resources
           where resource_id = pResourceId;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      IF curSubResource%ISOPEN THEN
      	CLOSE curSubResource;
      END IF;
      rollback;
      RAISE;
  END removeFolder;
----------------------------------------------------------------------------------------------
-- updates the folder pResource
----------------------------------------------------------------------------------------------
  PROCEDURE writeFolder(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2) IS
    vState NUMBER;
  BEGIN
    IF pResource.state NOT IN (opencmsConstants.C_STATE_NEW, opencmsConstants.C_STATE_CHANGED)
       AND pChange = 'TRUE' THEN
      vState := opencmsConstants.C_STATE_CHANGED;
    ELSE
      vState := pResource.state;
    END IF;
    BEGIN
      update cms_resources
             set resource_type = pResource.resource_type,
                 resource_flags = pResource.resource_flags,
                 user_id = pResource.user_id,
                 group_id = pResource.group_id,
                 project_id = pResource.project_id,
                 access_flags = pResource.access_flags,
                 state = vState,
                 locked_by = pResource.locked_by,
                 launcher_type = pResource.launcher_type,
                 launcher_classname = pResource.launcher_classname,
                 date_lastmodified = sysdate,
                 resource_lastmodified_by = pResource.resource_lastmodified_by,
                 resource_size = pResource.resource_size,
                 file_id = pResource.file_id
                 where resource_id = pResource.resource_id;
    EXCEPTION
      WHEN OTHERS THEN
        rollback;
        RAISE;
    END;
    commit;
  END writeFolder;
----------------------------------------------------------------------------------------------
-- updates the file pResource
----------------------------------------------------------------------------------------------
  PROCEDURE writeFileHeader(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2) IS
    vState NUMBER := pResource.state;
    vNewFileId NUMBER := pResource.file_id;
  BEGIN
    IF pResource.state = opencmsConstants.C_STATE_UNCHANGED AND pChange = 'TRUE' THEN
      -- copy the file from the online-project to the project
      vNewFileId := getNextId(opencmsConstants.C_TABLE_FILES);
      insert into cms_files (file_id, file_content)
      select vNewFileId, file_content from cms_files where file_id = pResource.file_id;
    END IF;
    IF pResource.state NOT IN (opencmsConstants.C_STATE_NEW, opencmsConstants.C_STATE_CHANGED)
       AND pChange = 'TRUE' THEN
      vState := opencmsConstants.C_STATE_CHANGED;
    ELSE
      vState := pResource.state;
    END IF;
    BEGIN
      update cms_resources
             set resource_type = pResource.resource_type,
                 resource_flags = pResource.resource_flags,
                 user_id = pResource.user_id,
                 group_id = pResource.group_id,
                 project_id = pResource.project_id,
                 access_flags = pResource.access_flags,
                 state = vState,
                 locked_by = pResource.locked_by,
                 launcher_type = pResource.launcher_type,
                 launcher_classname = pResource.launcher_classname,
                 date_lastmodified = sysdate,
                 resource_lastmodified_by = pResource.resource_lastmodified_by,
                 resource_size = pResource.resource_size,
                 file_id = vNewFileId
                 where resource_id = pResource.resource_id;
    EXCEPTION
      WHEN OTHERS THEN
        rollback;
        RAISE;
    END;
    commit;
  END writeFileHeader;
----------------------------------------------------------------------------------------------
-- updates the file pResource including cms_files
----------------------------------------------------------------------------------------------
  PROCEDURE writeFile(pProjectID IN NUMBER, pResource IN userTypes.fileRecord, pChange IN VARCHAR2) IS
    curFileHeader userTypes.anyCursor;
    recFileHeader cms_resources%ROWTYPE;
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
    update cms_files set file_content = pResource.file_content where file_id = pResource.file_id;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      RAISE;
  END writeFile;
--------------------------------------------------------------------------------------------------
-- Copy the file
--------------------------------------------------------------------------------------------------
  PROCEDURE copyFile(pProjectId NUMBER, pUserId NUMBER, pSource VARCHAR2, pDestination VARCHAR2) IS
    curOnlineProject userTypes.anyCursor;
    curFileHeader userTypes.anyCursor;
    curFolder userTypes.anyCursor;
    curFile userTypes.anyCursor;
    recOnlineProject cms_projects%ROWTYPE;
    recFileHeader cms_resources%ROWTYPE;
    recFolder cms_resources%ROWTYPE;
    recFile userTypes.fileRecord;
    vFolderName VARCHAR2(1000) := '';
    vFileName VARCHAR2(100) := '';
    curNewResource userTypes.anyCursor;
    recNewResource userTypes.fileRecord;
  BEGIN
    curOnlineProject := opencmsProject.onlineProject(pProjectId);
    FETCH curOnlineProject INTO recOnlineProject;
    CLOSE curOnlineProject;
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
	    curFile := readFile(pUserId, pProjectId, recOnlineProject.project_id, pSource);
        FETCH curFile INTO recFile;
        CLOSE curFile;
        IF recFile.resource_id IS NOT NULL THEN
          createFile(pProjectId, recOnlineProject.project_id, recFile, pUserId, recFolder.resource_id, vFolderName||vFileName, 'TRUE', curNewResource);
          FETCH curNewResource INTO recNewResource;
          CLOSE curNewResource;
	      -- copy the metainfos
	      lockResource(pUserId, pProjectId, recNewResource.resource_name, 'TRUE');
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
      IF curOnlineProject%ISOPEN THEN
        CLOSE curOnlineProject;
      END IF;
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
      RAISE;
  END copyFile;
----------------------------------------------------------------------------------------------
-- returns a cursor for the files in this folder
-- => same procedure as getFoldersInFolder but with resource_type != C_TYPE_FOLDER
----------------------------------------------------------------------------------------------
  FUNCTION getFilesInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor IS
    CURSOR curFilesProject(cParentId NUMBER) IS
           select * from cms_resources
                    where parent_id = cParentId
                    and resource_type != opencmsConstants.C_TYPE_FOLDER
                    order by resource_name;
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    recFiles    cms_resources%ROWTYPE;
    vQueryString VARCHAR2(32767) := '';
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
    IF opencmsAccess.accessRead(pUserId, pProjectId, recResource.resource_id) = 1 THEN
      OPEN curFilesProject(recResource.resource_id);
      LOOP
        FETCH curFilesProject INTO recFiles;
        EXIT WHEN curFilesProject%NOTFOUND;
        -- has the user access for this resource
        IF (opencmsAccess.accessOwner(pUserId, pProjectId, recFiles.resource_id, opencmsConstants.C_ACCESS_OWNER_READ) = 1
            OR opencmsAccess.accessOther(pUserId, pProjectId, recFiles.resource_id, opencmsConstants.C_ACCESS_PUBLIC_READ) = 1
            OR opencmsAccess.accessGroup(pUserId, pProjectId, recFiles.resource_id, opencmsConstants.C_ACCESS_GROUP_READ) = 1) THEN
          IF addInList(recFiles.resource_name) THEN
            vQueryString := vQueryString||' union select * from cms_resources where resource_id = '||to_char(recFiles.resource_id);
          END IF;
        END IF;
      END LOOP;
      CLOSE curFilesProject;
      -- project != online-project then compare if there are more files in online-project
      IF pProjectId != opencmsConstants.C_PROJECT_ONLINE_ID THEN
        curResource := readFolder(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, pResourceName);
        FETCH curResource INTO recResource;
        CLOSE curResource;
        IF recResource.resource_id IS NOT NULL THEN
          -- has the user access for this file?
          IF opencmsAccess.accessRead(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, recResource.resource_id) = 1 THEN
            OPEN curFilesProject(recResource.resource_id);
            LOOP
              FETCH curFilesProject INTO recFiles;
              EXIT WHEN curFilesProject%NOTFOUND;
              -- has the user access for this resource?
              IF (opencmsAccess.accessOwner(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, recFiles.resource_id, opencmsConstants.C_ACCESS_OWNER_READ) = 1
                 OR opencmsAccess.accessOther(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, recFiles.resource_id, opencmsConstants.C_ACCESS_PUBLIC_READ) = 1
                 OR opencmsAccess.accessGroup(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, recFiles.resource_id, opencmsConstants.C_ACCESS_GROUP_READ) = 1) THEN
                IF addInList(recFiles.resource_name) THEN
                  vQueryString := vQueryString||' union select * from cms_resources where resource_id = '||to_char(recFiles.resource_id);
                END IF;
              END IF;
            END LOOP;
            CLOSE curFilesProject;
          END IF;
        END IF;
      END IF;
      -- open cursor with the string vQueryString without the first "union"
      vQueryString := substr(vQueryString, 8);
      IF substr(vQueryString,1,6) = 'select' THEN
        OPEN curResource FOR 'select * from ('||vQueryString||') order by resource_name';
      END IF;
    END IF;
    bAnyList := '';
    RETURN curResource;
  END getFilesInFolder;
---------------------------------------------------------------------------------------------
-- returns a cursor for the folders in this folder
-- => same procedure as getFilesInFolder but with resource_type = C_TYPE_FOLDER
---------------------------------------------------------------------------------------------
  FUNCTION getFoldersInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor IS
    CURSOR curFilesProject(cParentId NUMBER) IS
           select * from cms_resources
                    where parent_id = cParentId
                    and resource_type = opencmsConstants.C_TYPE_FOLDER
                    order by resource_name;
    curResource userTypes.anyCursor;
    recResource cms_resources%ROWTYPE;
    recFiles    cms_resources%ROWTYPE;
    vQueryString VARCHAR2(32767) := '';
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
    IF opencmsAccess.accessRead(pUserId, pProjectId, recResource.resource_id) = 1 THEN
      OPEN curFilesProject(recResource.resource_id);
      LOOP
        FETCH curFilesProject INTO recFiles;
        EXIT WHEN curFilesProject%NOTFOUND;
        -- has the user access for this resource
        IF (opencmsAccess.accessOwner(pUserId, pProjectId, recFiles.resource_id, opencmsConstants.C_ACCESS_OWNER_READ) = 1
            OR opencmsAccess.accessOther(pUserId, pProjectId, recFiles.resource_id, opencmsConstants.C_ACCESS_PUBLIC_READ) = 1
            OR opencmsAccess.accessGroup(pUserId, pProjectId, recFiles.resource_id, opencmsConstants.C_ACCESS_GROUP_READ) = 1) THEN
          IF addInList(recFiles.resource_name) THEN
            vQueryString := vQueryString||' union select * from cms_resources where resource_id = '||to_char(recFiles.resource_id);
          END IF;
        END IF;
      END LOOP;
      CLOSE curFilesProject;
      -- project != online-project then compare if there are more folders in online-project
      IF pProjectId != opencmsConstants.C_PROJECT_ONLINE_ID THEN
        curResource := readFolder(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, pResourceName);
        FETCH curResource INTO recResource;
        CLOSE curResource;
        IF recResource.resource_id IS NOT NULL THEN
          -- has the user access for this folder?
          IF opencmsAccess.accessRead(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, recResource.resource_id) = 1 THEN
            OPEN curFilesProject(recResource.resource_id);
            LOOP
              FETCH curFilesProject INTO recFiles;
              EXIT WHEN curFilesProject%NOTFOUND;
              -- has the user access for this resource?
              IF (opencmsAccess.accessOwner(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, recFiles.resource_id, opencmsConstants.C_ACCESS_OWNER_READ) = 1
                 OR opencmsAccess.accessOther(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, recFiles.resource_id, opencmsConstants.C_ACCESS_PUBLIC_READ) = 1
                 OR opencmsAccess.accessGroup(pUserId, opencmsConstants.C_PROJECT_ONLINE_ID, recFiles.resource_id, opencmsConstants.C_ACCESS_GROUP_READ) = 1) THEN
                IF addInList(recFiles.resource_name) THEN
                  vQueryString := vQueryString||' union select * from cms_resources where resource_id = '||to_char(recFiles.resource_id);
                END IF;
              END IF;
            END LOOP;
            CLOSE curFilesProject;
          END IF;
        END IF;
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
      RAISE;
  END chstate;
-------------------------------------------------------------------------------------------
-- copy the resource pResourceName from project pFromProjectID to project pToProjectID
-------------------------------------------------------------------------------------------
  PROCEDURE copyResource(pToProjectID IN NUMBER, pFromProjectID IN NUMBER, pResourceName IN VARCHAR2) IS
    vCount NUMBER;
    vParentID NUMBER := opencmsConstants.C_UNKNOWN_ID;
    vParentName cms_resources.resource_name%TYPE;
    vNewResourceId NUMBER;
  BEGIN
    -- does this resource already exist in project pToProjectID?
    select count(*) into vCount
           from cms_resources
           where project_id = pToProjectID
           and resource_name = pResourceName;
    IF vCount > 0 THEN
      userErrors.raiseUserError(userErrors.C_FILE_EXISTS);
    END IF;
    -- does this resource exist in project pFromProjectID?
    select count(*) into vCount
           from cms_resources
           where project_id = pFromProjectID
           and resource_name = pResourceName;
    IF vCount = 0 THEN
      userErrors.raiseUserError(userErrors.C_NOT_FOUND);
    END IF;
    -- get the parent_id for the new resource
    vParentName := nvl(getParent(pResourceName),'');
    BEGIN
      select resource_id into vParentId
             from cms_resources
             where project_id = pToProjectId
             and resource_name = vParentName;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        vParentId := -1;
    END;
    -- insert new resource for project
    vNewResourceId := getNextId(opencmsConstants.C_TABLE_RESOURCES);
    insert into cms_resources
            (resource_id, parent_id, resource_name, resource_type, resource_flags, user_id, group_id,
             project_id, file_id, access_flags, state, locked_by, launcher_type, launcher_classname,
             date_created, date_lastmodified, resource_size, resource_lastmodified_by)
    select vNewResourceId, vParentId, resource_name, resource_type, resource_flags, user_id,
             group_id, pToProjectId, file_id, access_flags, opencmsConstants.C_STATE_UNCHANGED, locked_by,
             launcher_type, launcher_classname, date_created, sysdate, resource_size, resource_lastmodified_by
             from cms_resources where project_id = pFromProjectID and resource_name = pResourceName;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      RAISE;
  END copyResource;
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
    BEGIN
      select decode(parent_id, -1, NULL, parent_id) into vParentId
             from cms_resources
             where resource_id = pResourceId
             and project_id = pProjectID;
      RETURN vParentId;
    EXCEPTION
      WHEN OTHERS THEN
         RETURN NULL;
    END getParentId;
---------------------------------------------------------------------------------------
-- private function checks if this path is already in the list, if not => edit the list
-- and returns boolean
---------------------------------------------------------------------------------------
  FUNCTION addInList(pName VARCHAR2) RETURN BOOLEAN IS
    vCount NUMBER;
  BEGIN
    vCount := nvl(Instr(bAnyList, ''''||pName||''''),0);
    IF vCount = 0 THEN
      bAnyList := bAnyList||','''||pName||'''';
      RETURN TRUE;
    ELSE
      RETURN FALSE;
	END IF;
  END addInList;
-------------------------------------------------------------------------
END;
/
