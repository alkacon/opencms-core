CREATE OR REPLACE
PACKAGE BODY OpenCmsProject IS
   -- variable/funktions/procedures which are used only in this package
   bList userTypes.numberTable;
   FUNCTION addInList(pId NUMBER) RETURN BOOLEAN;
   PROCEDURE backupProject (pProjectId NUMBER, pVersionId NUMBER, pPublishDate DATE, pUserId NUMBER);
--------------------------------------------------------------------
-- return all project which the user has access
-- this function calls the function getGroupsOfUser
-- and returns the Cursor with the projects
--------------------------------------------------------------------
	FUNCTION getAllAccessibleProjects(pUserID IN NUMBER) RETURN userTypes.anyCursor IS
      CURSOR cProjUser IS
             select * from cms_projects
                    where user_id = pUserID
                    and project_flags = 0
                    order by project_name;
      CURSOR cProjAdmin IS
             select * from cms_projects
                    where project_flags = 0
                    order by project_name;
      CURSOR cProjGroup(cGroupID NUMBER) IS
             select * from cms_projects
                    where (group_id = cGroupId or managergroup_id = cGroupId)
                    and project_flags = 0
                    order by project_name;

      vCursor userTypes.anyCursor := opencmsgroup.getGroupsOfUser (pUserID);
      recAllAccProjects userTypes.anyCursor;
      recGroup cms_groups%ROWTYPE;
      recProject cms_projects%ROWTYPE;
      vQueryStr VARCHAR2(32767) := '';
	BEGIN
      -- all projects where the user is owner
      FOR recProject IN cProjUser LOOP
        -- remember each project-id => no duplicates
        IF addInList(recProject.project_id) THEN
          null;
        END IF;
      END LOOP;
      -- all projects where the groups, which the user belongs to, have access
	  LOOP
	    FETCH vCursor INTO recGroup;
	    EXIT WHEN vCursor%NOTFOUND;
        IF recGroup.group_name = opencmsConstants.C_GROUP_ADMIN THEN
          -- if the user is member of the group administrators then list all projects
          FOR recProject IN cProjAdmin LOOP
            IF addInList(recProject.project_id) THEN
              vQueryStr := vQueryStr||' union select * from cms_projects where project_flags = 0 ';
            END IF;
          END LOOP;
        ELSE
          FOR recProject IN cProjGroup(recGroup.group_id) LOOP
            IF addInList(recProject.project_id) THEN
              vQueryStr := vQueryStr||' union select * from cms_projects where project_flags = 0'||
                                      ' and (group_id = '||to_char(recGroup.group_id)||' or managergroup_id = '||
                                      to_char(recGroup.group_id)||')';
            END IF;
          END LOOP;
        END IF;
	  END LOOP;
      CLOSE vCursor;
      -- return the cursor
      bList.DELETE;
      OPEN recAllAccProjects FOR 'select * from (select * from cms_projects where user_id = '||to_char(pUserID)||' and project_flags = 0 '||
                                  vQueryStr||') order by project_name';
      RETURN recAllAccProjects;
	END getAllAccessibleProjects;
------------------------------------------------------------------------------------
-- funktion checks if the ID is already in list, if not it edits the list
-- and returns boolean
------------------------------------------------------------------------------------
  FUNCTION addInList(pId NUMBER) RETURN BOOLEAN IS
    newIndex NUMBER;
    element NUMBER;
  BEGIN
    FOR element IN 1..bList.COUNT
    LOOP
      IF bList(element) = pId THEN
        RETURN FALSE;
      END IF;
	END LOOP;
	IF element > 1 THEN
	  newIndex := element+1;
	ELSE
	  newIndex := 1;
	END IF;
    bList(newIndex) := pId;
	RETURN TRUE;
  END addInList;
------------------------------------------------------------------------------------------
-- insert a new project and return of the project-id
------------------------------------------------------------------------------------------
  PROCEDURE createProject(pUserId IN NUMBER, pProjectName IN VARCHAR2, pProjectDescription IN VARCHAR2,
                         pGroupName IN VARCHAR2, pManagerGroupName IN VARCHAR2, pTaskID IN NUMBER,
                         pProject OUT userTypes.anyCursor) IS

    vGroupId cms_groups.group_id%TYPE;
    vManagerGroupId cms_groups.group_id%TYPE;
    vProjectID CMS_PROJECTS.project_id%TYPE;
  BEGIN
    -- select the ID of the group
    BEGIN
      select group_id into vGroupID from CMS_GROUPS where group_name = pGroupName;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        vGroupId := -1;
    END;
    BEGIN
      select group_id into vManagerGroupID from CMS_GROUPS where group_name = pManagerGroupName;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        vManagerGroupId := -1;
    END;
    --
    -- insert in CMS_PROJECTS
    vProjectId := getNextId(opencmsConstants.C_TABLE_PROJECTS);
    insert into cms_projects
           (project_id, user_id, group_id, managergroup_id, task_id, project_name, project_description,
            project_flags, project_createdate, project_type)
    values (vProjectId, pUserId, vGroupId, vManagerGroupId, pTaskId, pProjectName,
            pProjectDescription, 0, sysdate, 0);
    commit;
    --
    -- return the project
    OPEN pProject FOR select * from cms_projects where project_id = vProjectId;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      RAISE;
  END createProject;
--------------------------------------------------------------------------------------------
-- publishes the project: copy (insert/update) the folders, files, properties
-- from the work-project to the online-project
--------------------------------------------------------------------------------------------
  PROCEDURE publishProject (pUserId NUMBER, pProjectId NUMBER, pOnlineProjectId NUMBER,
  							pEnableHistory NUMBER, pPublishDate DATE,
                            pCurDelFolders OUT userTypes.anyCursor, pCurWriteFolders OUT userTypes.anyCursor,
                            pCurDelFiles OUT userTypes.anyCursor, pCurWriteFiles OUT userTypes.anyCursor) IS

    CURSOR curFolders(cProjectId NUMBER) IS
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
                  where cms_projectresources.project_id= cProjectId
                  and cms_resources.project_id = cProjectId
                  and cms_resources.resource_type = opencmsConstants.C_TYPE_FOLDER
                  and cms_resources.resource_name like concat(cms_projectresources.resource_name,'%')
                  and cms_resources.state != opencmsConstants.C_STATE_UNCHANGED
                  order by cms_resources.resource_name;

    CURSOR curFiles(cProjectId NUMBER) IS
           select cms_resources.resource_id, cms_resources.parent_id,
                  cms_resources.resource_name, cms_resources.resource_type,
                  cms_resources.resource_flags, cms_resources.user_id,
                  cms_resources.group_id, cms_projectresources.project_id,
                  cms_resources.file_id, cms_resources.access_flags, cms_resources.state,
                  cms_resources.locked_by, cms_resources.launcher_type,
                  cms_resources.launcher_classname, cms_resources.date_created,
                  cms_resources.date_lastmodified, cms_resources.resource_size,
                  cms_resources.resource_lastmodified_by, cms_files.file_content
                  from cms_resources, cms_projectresources, cms_files
                  where cms_projectresources.project_id = cProjectId
                  and cms_resources.project_id = cProjectId
                  and cms_resources.resource_name like concat(cms_projectresources.resource_name, '%')
                  and cms_resources.file_id = cms_files.file_id (+)
                  and cms_resources.resource_type != opencmsConstants.C_TYPE_FOLDER
                  and cms_resources.state != opencmsConstants.C_STATE_UNCHANGED
                  order by cms_resources.resource_name;

    recFolders cms_resources%ROWTYPE;
    recFiles userTypes.fileRecord;
    vParentId NUMBER;
    curNewFolder userTypes.anyCursor;
    recNewFolder cms_resources%ROWTYPE;
    curNewFile userTypes.anyCursor;
    recNewFile userTypes.fileRecord;
    vOfflineResourceId cms_resources.resource_id%TYPE;
    vResourceId cms_resources.resource_id%TYPE;
    vFileId cms_resources.file_id%TYPE;
    vDeletedFolders userTypes.numberTable;
    element NUMBER;
    vCurDelFolders VARCHAR2(100) := '';
    vCurDelFiles VARCHAR2(100) := '';
    vCurWriteFolders VARCHAR2(100) := '';
    vCurWriteFiles VARCHAR2(100) := '';
    vVersionId NUMBER := 1;
    vResVersionId NUMBER := 1;
  BEGIN
    ---------------------------------------
    -- get the next version id for backup
    -- pEnableHistory = 1 => enable history
    ---------------------------------------
    IF pEnableHistory = 1 THEN
      select nvl(max(version_id),0) + 1 into vVersionId from cms_backup_projects;
      select nvl(max(version_id),0) + 1 into vResVersionId from cms_backup_resources;
      IF vResVersionId > vVersionId THEN
      	vVersionId := vResVersionId;
      END IF;
      -- backup the project
      backupProject(pProjectId, vVersionId, pPublishDate, pUserId);
    END IF;
    ---------------------------------
    -- for all folders of the project
    ---------------------------------
    OPEN curFolders(pProjectId);
    LOOP
      FETCH curFolders INTO recFolders;
      EXIT WHEN curFolders%NOTFOUND;
      -- do not publish folders that are locked in another project
      IF (recFolders.locked_by != opencmsConstants.C_UNKNOWN_ID) THEN
        -- do nothing;
        null;
      -- is the resource marked as deleted?
      ELSIF recFolders.state = opencmsConstants.C_STATE_DELETED THEN
        -- add to list with deleted folders
        vDeletedFolders(vDeletedFolders.COUNT + 1) := recFolders.resource_id;
        IF pEnableHistory = 1 THEN
          -- backup the resource
          opencmsResource.backupFolder(pProjectId, recFolders, vVersionId, pPublishDate);
        END IF;
      -- is the resource marked as new?
      ELSIF recFolders.state = opencmsConstants.C_STATE_NEW THEN
        BEGIN
          select resource_id into vParentId
                 from cms_online_resources
                 where resource_name = opencmsResource.getParent(recFolders.resource_name);
        EXCEPTION
          WHEN NO_DATA_FOUND THEN
            vParentId := opencmsConstants.C_UNKNOWN_ID;
        END;
        BEGIN
          opencmsResource.createFolder(pUserId, pOnlineProjectId, pOnlineProjectId, recFolders,
            	                         vParentId, recFolders.resource_name, curNewFolder);
          FETCH curNewFolder INTO recNewFolder;
          CLOSE curNewFolder;
		  recNewFolder.state := opencmsConstants.C_STATE_UNCHANGED;
		  opencmsResource.writeFolder(pOnlineProjectId, recNewFolder, 'FALSE');
        EXCEPTION
		  WHEN OTHERS THEN
		  	IF sqlcode = userErrors.C_FILE_EXISTS THEN
		  	  curNewFolder := opencmsResource.readFolder(pUserId, pOnlineProjectId, recFolders.resource_name);
			  FETCH curNewFolder INTO recNewFolder;
			  CLOSE curNewFolder;
		  	  -- the folder already exists in the online-project
              -- update the folder in the online-project
              update cms_online_resources set
                     resource_type = recFolders.resource_type,
                     resource_flags = recFolders.resource_flags,
                     user_id = recFolders.user_id,
                     group_id = recFolders.group_id,
                     project_id = pOnlineProjectId,
                     access_flags = recFolders.access_flags,
                     state = opencmsConstants.C_STATE_UNCHANGED,
                     locked_by = opencmsConstants.C_UNKNOWN_ID,
                     launcher_type = recFolders.launcher_type,
                     launcher_classname = recFolders.launcher_classname,
                     date_lastmodified = recFolders.date_lastmodified,
                     resource_lastmodified_by = recFolders.resource_lastmodified_by,
                     resource_size = 0,
                     file_id = recFolders.file_id
                     where resource_id = recNewFolder.resource_id;
              commit;
		    ELSE
			  RAISE;
		    END IF;
        END;
        -- copy properties
        opencmsProperty.writeProperties(pOnlineProjectId, opencmsProperty.readAllProperties(pUserId, pProjectId, recFolders.resource_name),
                                        recNewFolder.resource_id, recFolders.resource_type);
        -- remember only one id for mark
        vCurWriteFolders := recNewFolder.resource_id;
        IF pEnableHistory = 1 THEN
        	-- backup the resource
        	opencmsResource.backupFolder(pProjectId, recFolders, vVersionId, pPublishDate);
        END IF;
      -- is the resource marked as changed?
      ELSIF recFolders.state = opencmsConstants.C_STATE_CHANGED THEN
        -- checkExport ???
        -- does the folder exist in the online-project?
        recNewFolder := NULL;
        curNewFolder := opencmsResource.readFolder(pUserId, pOnlineProjectId, recFolders.resource_name);
        FETCH curNewFolder INTO recNewFolder;
        CLOSE curNewFolder;
        -- folder does not exist in online-project => create folder
        IF recNewFolder.resource_id IS NULL THEN
          BEGIN
            select resource_id into vParentId
                   from cms_online_resources
                   where resource_name = opencmsResource.getParent(recFolders.resource_name);
          EXCEPTION
            WHEN NO_DATA_FOUND THEN
              vParentId := opencmsConstants.C_UNKNOWN_ID;
          END;
          opencmsResource.createFolder(pUserId, pOnlineProjectId, pOnlineProjectId, recFolders,
                                       vParentId, recFolders.resource_name, curNewFolder);
          FETCH curNewFolder INTO recNewFolder;
          CLOSE curNewFolder;
          recNewFolder.state := opencmsConstants.C_STATE_UNCHANGED;
        END IF;
        -- update the folder in the online-project
        update cms_online_resources set
               resource_type = recFolders.resource_type,
               resource_flags = recFolders.resource_flags,
               user_id = recFolders.user_id,
               group_id = recFolders.group_id,
               project_id = pOnlineProjectId,
               access_flags = recFolders.access_flags,
               state = opencmsConstants.C_STATE_UNCHANGED,
               locked_by = opencmsConstants.C_UNKNOWN_ID,
               launcher_type = recFolders.launcher_type,
               launcher_classname = recFolders.launcher_classname,
               date_lastmodified = recFolders.date_lastmodified,
               resource_lastmodified_by = recFolders.resource_lastmodified_by,
               resource_size = 0,
               file_id = recFolders.file_id
               where resource_id = recNewFolder.resource_id;
        commit;
        -- copy the properties
        delete from cms_online_properties where resource_id = recNewFolder.resource_id;
        opencmsProperty.writeProperties(pOnlineProjectId, opencmsProperty.readAllProperties(pUserId, pProjectId, recFolders.resource_name),
                                        recNewFolder.resource_id, recNewFolder.resource_type);
        -- remember only one id for mark
        vCurWriteFolders := recNewFolder.resource_id;
        IF pEnableHistory = 1 THEN
          -- backup the resource
          opencmsResource.backupFolder(pProjectId, recFolders, vVersionId, pPublishDate);
        END IF;
      END IF;
    END LOOP;
    CLOSE curFolders;
    ---------------------------------
    -- for all files of the project
    ---------------------------------
    OPEN curFiles(pProjectId);
    LOOP
      FETCH curFiles INTO recFiles;
      EXIT WHEN curFiles%NOTFOUND;
      -- do not publish files that are locked in another project
      IF (recFiles.locked_by != opencmsConstants.C_UNKNOWN_ID) THEN
        -- do nothing;
        null;
      -- resource of offline-project is marked for delete
      ELSIF substr(recFiles.resource_name,instr(recFiles.resource_name,'/',-1,1)+1,1) = opencmsConstants.C_TEMP_PREFIX THEN
		delete from cms_properties where resource_id = recFiles.resource_id;
        delete from cms_resources where resource_name = recFiles.resource_name;
        delete from cms_files where file_id = recFiles.file_id;
      -- resource is deleted
      ELSIF recFiles.state = opencmsConstants.C_STATE_DELETED THEN
        curNewFile := opencmsResource.readFileNoAccess(pUserId, pOnlineProjectId, pOnlineProjectId, recFiles.resource_name);
        FETCH curNewFile INTO recNewFile;
        CLOSE curNewFile;
        IF pEnableHistory = 1 THEN
          -- backup the resource
          opencmsResource.backupFile(pProjectId, recFiles, vVersionId, pPublishDate);
        END IF;
        -- delete the file from online project
        delete from cms_online_properties where resource_id = recNewFile.resource_id;
        delete from cms_online_resources where resource_id = recNewFile.resource_id;
        delete from cms_online_files where file_id = recNewFile.file_id;
        -- delete file and properties of offline resource
        delete from cms_files where file_id = recFiles.file_id;
        delete from cms_properties where resource_id = recFiles.resource_id;
        commit;
        -- remember only one id for mark
        vCurDelFiles := recNewFile.resource_id;
      -- resource is new
      ELSIF recFiles.state = opencmsConstants.C_STATE_NEW THEN
        -- checkExport ???
        BEGIN
          select resource_id into vParentId
                 from cms_online_resources
                 where resource_name = opencmsResource.getParent(recFiles.resource_name);
        EXCEPTION
          WHEN NO_DATA_FOUND THEN
            vParentId := opencmsConstants.C_UNKNOWN_ID;
        END;
        BEGIN
          opencmsResource.createFile(pOnlineProjectId, pOnlineProjectId, recFiles, pUserId, vParentId,
                                     recFiles.resource_name, 'FALSE', curNewFile);
          FETCH curNewFile INTO recNewFile;
          CLOSE curNewFile;
          update cms_online_resources set state = opencmsConstants.C_STATE_UNCHANGED
                 where resource_id=recNewFile.resource_id;
        EXCEPTION
          WHEN OTHERS THEN
            IF sqlcode = userErrors.C_FILE_EXISTS THEN
              -- the folder already exist in the online-project
              curNewFile := opencmsResource.readFileNoAccess(pUserId, pOnlineProjectId, pOnlineProjectId, recFiles.resource_name);
              FETCH curNewFile INTO recNewFile;
              CLOSE curNewFile;
              -- update the file in the online-project
              update cms_online_resources set
                     resource_type = recFiles.resource_type,
                     resource_flags = recFiles.resource_flags,
                     user_id = recFiles.user_id,
                     group_id = recFiles.group_id,
                     project_id = pOnlineProjectId,
                     access_flags = recFiles.access_flags,
                     state = opencmsConstants.C_STATE_UNCHANGED,
                     locked_by = opencmsConstants.C_UNKNOWN_ID,
                     launcher_type = recFiles.launcher_type,
                     launcher_classname = recFiles.launcher_classname,
                     date_lastmodified = recFiles.date_lastmodified,
                     resource_lastmodified_by = recFiles.resource_lastmodified_by,
                     resource_size = recFiles.resource_size
                     where resource_id = recNewFile.resource_id;
              update cms_online_files set
              		file_content = recFiles.file_content
              		where file_id = recNewFile.file_id;
              commit;
            ELSE
              RAISE;
            END IF;
        END;
        -- copy the properties
        delete from cms_online_properties where resource_id = recNewFile.resource_id;
        opencmsProperty.writeProperties(pOnlineProjectId, opencmsProperty.readAllProperties(pUserId, pProjectId, recFiles.resource_name),
                                        recNewFile.resource_id, recFiles.resource_type);
        -- remember only one id for mark
        vCurWriteFiles := recNewFile.resource_id;
        IF pEnableHistory = 1 THEN
          -- backup the resource
          opencmsResource.backupFile(pProjectId, recFiles, vVersionId, pPublishDate);
        END IF;
      -- resource is changed
      ELSIF recFiles.state = opencmsConstants.C_STATE_CHANGED THEN
      	recNewFile := NULL;
        -- does the file exist in the online-project?
        curNewFile := opencmsResource.readFileNoAccess(pUserId, pOnlineProjectId, pOnlineProjectId, recFiles.resource_name);
        FETCH curNewFile INTO recNewFile;
        CLOSE curNewFile;
        -- file does not exist in online-project => create file
        IF recNewFile.resource_id IS NULL THEN
          BEGIN
            select resource_id into vParentId
                   from cms_online_resources
                   where resource_name = opencmsResource.getParent(recFiles.resource_name);
          EXCEPTION
            WHEN NO_DATA_FOUND THEN
              vParentId := opencmsConstants.C_UNKNOWN_ID;
          END;
          opencmsResource.createFile(pOnlineProjectId, pOnlineProjectId, recFiles, pUserId,
                                     vParentId, recFiles.resource_name, 'FALSE', curNewFile);
          FETCH curNewFile INTO recNewFile;
          CLOSE curNewFile;
        END IF;
        -- update the file in the online-project
        update cms_online_resources set
               resource_type = recFiles.resource_type,
               resource_flags = recFiles.resource_flags,
               user_id = recFiles.user_id,
               group_id = recFiles.group_id,
               project_id = pOnlineProjectId,
               access_flags = recFiles.access_flags,
               state = opencmsConstants.C_STATE_UNCHANGED,
               locked_by = opencmsConstants.C_UNKNOWN_ID,
               launcher_type = recFiles.launcher_type,
               launcher_classname = recFiles.launcher_classname,
               date_lastmodified = recFiles.date_lastmodified,
               resource_lastmodified_by = recFiles.resource_lastmodified_by,
               resource_size = recFiles.resource_size
               where resource_id = recNewFile.resource_id;
        update cms_online_files set
               file_content = recFiles.file_content
               where file_id = recNewFile.file_id;
        commit;
        -- copy the properties
        delete from cms_online_properties where resource_id = recNewFile.resource_id;
        opencmsProperty.writeProperties(pOnlineProjectId, opencmsProperty.readAllProperties(pUserId, pProjectId, recFiles.resource_name),
                                        recNewFile.resource_id, recNewFile.resource_type);
        -- remember only one id for mark
        vCurWriteFiles := recNewFile.resource_id;
        IF pEnableHistory = 1 THEN
          -- backup the resource
          opencmsResource.backupFile(pProjectId, recFiles, vVersionId, pPublishDate);
        END IF;
      END IF;
    END LOOP;
    CLOSE curFiles;
    -- now remove the folders
    IF vDeletedFolders.COUNT > 0 THEN
      -- get the string for the cursor of
      vCurDelFolders := vDeletedFolders.COUNT;
      FOR element IN 1..vDeletedFolders.COUNT
      LOOP
        vOfflineResourceId := vDeletedFolders(element);
        BEGIN
          delete from cms_properties where resource_id = vOfflineResourceId;
          select resource_id into vResourceId
                 from cms_online_resources
          		 where resource_name = (select resource_name from cms_resources where resource_id = vOfflineResourceId);
          delete from cms_online_properties where resource_id = vResourceId;
          delete from cms_online_resources where resource_id = vResourceId;
          commit;
        EXCEPTION
          WHEN NO_DATA_FOUND THEN
            null;
        END;
      END LOOP;
      vDeletedFolders.DELETE;
    END IF;
    -- build the cursors which are used in java for the discAccess
    BEGIN
      IF length(vCurDelFolders) > 0 THEN
        OPEN pCurDelFolders FOR 'select r.resource_name from cms_resources r, cms_projectresources p'||
                                ' where p.project_id = '||pProjectId||
                                ' and r.resource_name like concat(p.resource_name,''%'')'||
                                ' and resource_type = '||opencmsConstants.C_TYPE_FOLDER||
          						' and state = '||opencmsConstants.C_STATE_DELETED;
      ELSE
        -- return a cursor that contains no rows
        OPEN pCurDelFolders FOR 'select resource_name from cms_resources where 1=2';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        raise_application_error(-20999, 'error open cursor pCurDelFolders: '||substr(vCurDelFolders,1,200));
    END;
    BEGIN
      IF length(vCurWriteFolders) > 0 THEN
        OPEN pCurWriteFolders FOR 'select r.resource_name from cms_resources r, cms_projectresources p'||
                                  ' where p.project_id = '||pProjectId||
                                  ' and r.resource_name like concat(p.resource_name,''%'')'||
        					      ' and resource_type = '||opencmsConstants.C_TYPE_FOLDER||
        						  ' and state in ('||opencmsConstants.C_STATE_NEW||', '||
        						                     opencmsConstants.C_STATE_CHANGED||')';

      ELSE
        -- return a cursor that contains no rows
        OPEN pCurWriteFolders FOR 'select resource_name from cms_resources where 1=2';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        raise_application_error(-20999, 'error open cursor pCurWriteFolders: '||substr(vCurWriteFolders,2,200));
    END;
    BEGIN
      IF length(vCurDelFiles) > 0 THEN
        OPEN pCurDelFiles FOR 'select r.resource_name from cms_resources r, cms_projectresources p'||
                              ' where p.project_id = '||pProjectId||
                              ' and r.resource_name like concat(p.resource_name,''%'')'||
                              ' and resource_type != '||opencmsConstants.C_TYPE_FOLDER||
          					  ' and state = '||opencmsConstants.C_STATE_DELETED;
      ELSE
        -- return a cursor that contains no rows
        OPEN pCurDelFiles FOR 'select resource_name from cms_resources where 1=2';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        raise_application_error(-20999, 'error open cursor pCurDelFiles: '||substr(vCurDelFiles,2,200));
    END;
    BEGIN
      IF length(vCurWriteFiles) > 0 THEN
        OPEN pCurWriteFiles FOR 'select r.resource_name, file_id from cms_resources r, cms_projectresources p'||
                                ' where p.project_id = '||pProjectId||
                                ' and r.resource_name like concat(p.resource_name,''%'')'||
                                ' and resource_type != '||opencmsConstants.C_TYPE_FOLDER||
          					    ' and state in ('||opencmsConstants.C_STATE_NEW||', '||
        						                   opencmsConstants.C_STATE_CHANGED||')';
      ELSE
        -- return a cursor that contains no rows
        OPEN pCurWriteFiles FOR 'select resource_name, file_id from cms_resources where 1=2';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        raise_application_error(-20999, 'error open cursor pCurWriteFiles: '||substr(vCurWriteFiles,2,200));
    END;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      IF curFolders%ISOPEN THEN
        CLOSE curFolders;
      END IF;
      IF curFiles%ISOPEN THEN
		CLOSE curFiles;
	  END IF;
      IF curNewFolder%ISOPEN THEN
        CLOSE curNewFolder;
      END IF;
      IF curNewFile%ISOPEN THEN
        CLOSE curNewFile;
      END IF;
      RAISE;
  END publishProject;
-----------------------------------------------------------------------------------------
-- makes a backup of the published project for history
-----------------------------------------------------------------------------------------
  PROCEDURE backupProject (pProjectId NUMBER, pVersionId NUMBER, pPublishDate DATE, pUserId NUMBER) IS
    vUserName VARCHAR2(135);
  BEGIN
    BEGIN
      select user_name||' '||user_firstname||' '||user_lastname into vUserName
             from cms_users
             where user_id = pUserId;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        vUserName := '';
    END;
    insert into cms_backup_projects
      (version_id, project_id, project_name, project_publishdate, project_published_by,
       project_published_by_name, user_id, user_name, group_id, group_name, managergroup_id,
       managergroup_name, project_description, project_createdate, project_type, task_id)
    select pVersionId, project_id, project_name, pPublishDate, pUserId,
           vUserName, p.user_id, u.user_name||' '||u.user_firstname||' '||u.user_lastname,
           p.group_id, g.group_name, managergroup_id, mg.group_name, project_description,
           project_createdate, project_type, task_id
           from cms_projects p, cms_users u, cms_groups g, cms_groups mg
           where project_id = pProjectId
           and p.user_id = u.user_id(+)
           and p.group_id = g.group_id(+)
           and p.managergroup_id = mg.group_id(+);
    insert into cms_backup_projectresources
      (version_id, project_id, resource_name)
    select pVersionId, project_id, resource_name from cms_projectresources where project_id = pProjectId;
    commit;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      raise_application_error(-20004, '[opencmsproject.backupProject',true);
  END;
-----------------------------------------------------------------------------------------
-- returns a cursor with the online-project
-----------------------------------------------------------------------------------------
  FUNCTION onlineProject RETURN cms_projects%ROWTYPE IS
    recOnlineProject cms_projects%ROWTYPE;
  BEGIN
    select * into recOnlineProject from cms_projects
           where project_id = openCmsConstants.C_PROJECT_ONLINE_ID
           order by project_name;
    RETURN recOnlineProject;
  END onlineProject;
-----------------------------------------------------------------------------------------
-- returns a cursor with the online-project
-----------------------------------------------------------------------------------------
  FUNCTION onlineProject(pProjectId NUMBER) RETURN cms_projects%ROWTYPE IS
    curOnlineProject cms_projects%ROWTYPE;
    vCount NUMBER;
  BEGIN
    select * into curOnlineProject from cms_projects
           where project_id = openCmsConstants.C_PROJECT_ONLINE_ID
           order by project_name;
    RETURN curOnlineProject;
  END onlineProject;
------------------------------------------------------------------------------------------
END ;
/
