CREATE OR REPLACE
PACKAGE BODY OpenCmsProject IS
   -- variable/funktions/procedures which are used only in this package
   bAnyList VARCHAR2(100) := '';
   PROCEDURE helperCopyResourceToProject(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProjectId IN NUMBER, pResourceName IN VARCHAR2);
   FUNCTION addInList(pAnyId NUMBER) RETURN BOOLEAN;
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
      recGroupId cms_groups.group_id%TYPE;
      recGroupName cms_groups.group_name%TYPE;
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
	    FETCH vCursor INTO recGroupId, recGroupName;
	    EXIT WHEN vCursor%NOTFOUND;
        IF recGroupName = opencmsConstants.C_GROUP_ADMIN THEN
          -- if the user is member of the group administrators then list all projects
          FOR recProject IN cProjAdmin LOOP
            IF addInList(recProject.project_id) THEN
              vQueryStr := vQueryStr||' union select * from cms_projects where project_flags = 0 ';
            END IF;
          END LOOP;
        ELSE
          FOR recProject IN cProjGroup(recGroupID) LOOP
            IF addInList(recProject.project_id) THEN
              vQueryStr := vQueryStr||' union select * from cms_projects where project_flags = 0'||
                                      ' and (group_id = '||to_char(recGroupID)||' or managergroup_id = '||
                                      to_char(recGroupID)||')';
            END IF;
          END LOOP;
        END IF;
	  END LOOP;
      CLOSE vCursor;
      bAnyList := '';
      -- return the cursor
      OPEN recAllAccProjects FOR 'select * from (select * from cms_projects where user_id = '||to_char(pUserID)||' and project_flags = 0 '||
                                  vQueryStr||') order by project_name';
      RETURN recAllAccProjects;
	END getAllAccessibleProjects;
------------------------------------------------------------------------------------
-- funktion checks if the ID is already in list, if not it edits the list
-- and returns boolean
------------------------------------------------------------------------------------
  FUNCTION addInList(pAnyId NUMBER) RETURN BOOLEAN IS
    vCount NUMBER;
  BEGIN
    vCount := nvl(Instr(bAnyList, ''''||to_char(pAnyId)||''''),0);
    IF vCount = 0 THEN
      bAnyList := bAnyList||','''||to_char(pAnyId)||'''';
      RETURN TRUE;
    ELSE
      RETURN FALSE;
	END IF;
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
            project_flags, project_createdate, project_publishdate, project_published_by, project_type)
    values (vProjectId, pUserId, vGroupId, vManagerGroupId, pTaskId, pProjectName,
            pProjectDescription, 0, sysdate, NULL, -1, 0);
    commit;
    --
    -- return the project
    OPEN pProject FOR select * from cms_projects where project_id = vProjectId;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      RAISE;
  END createProject;
--------------------------------------------------------------------------------------------------------------
-- copy the resource pResource from the online-project to the project with pProjectId
--------------------------------------------------------------------------------------------------------------
  PROCEDURE copyResourceToProject(pUserID IN NUMBER, pProjectId IN NUMBER, pResource IN VARCHAR2) IS
    curResource userTypes.anyCursor;
    curProject userTypes.anyCursor;
    curProperties userTypes.anyCursor;
    recProject cms_projects%ROWTYPE;
    recOnlineProject cms_projects%ROWTYPE;
    recResource cms_resources%ROWTYPE;
    recPropValue cms_properties.property_value%TYPE;
    recPropName  cms_propertydef.propertydef_name%TYPE;
    vParent cms_resources.resource_name%TYPE;
    vResourceName cms_resources.resource_name%TYPE;
    i NUMBER;
    vCount NUMBER;
  BEGIN
    IF instr(pResource,'/') = 0 THEN
      select resource_name into vResourceName from cms_resources where resource_id = to_number(pResource);
    ELSE
      vResourceName := pResource;
    END IF;
    -- project != online-project and project.user_id = pUserId and project.flags = C_PROJECT_STATE_UNLOCKED
    OPEN curProject FOR select * from cms_projects where project_id = pProjectId;
    FETCH curProject INTO recProject;
    CLOSE curProject;
    curProject := onlineProject(pProjectId);
    FETCH curProject INTO recOnlineProject;
    CLOSE curProject;
    IF (pProjectID != recOnlineProject.project_id
        AND recProject.user_id = pUserId
        AND recProject.project_flags = opencmsConstants.C_PROJECT_STATE_UNLOCKED) THEN
      -- get parentfolders of pResourceName from online-project
      i := 1;
      LOOP
        -- get all parent-resources from the absolute path of pResourceName beginning with root "/"
        vParent := substr(vResourceName, 1, instr(vResourceName, '/', 1, i));
        IF (vParent IS NULL OR vParent = vResourceName) THEN
          -- no more folders or parent-folder = folder to copy
          EXIT;
        END IF;
        i := i+1;
        -- dont't copy the resource if it already exists
        select count(*) into vCount from cms_resources where resource_name = vParent and project_id = pProjectId;
        IF vCount = 0 THEN
          -- copy the parent-resource from online-project to project
          opencmsResource.copyResource(pProjectId, recOnlineProject.project_id, vParent);
          -- copy meta-information
          curResource := opencmsResource.readFileHeader(pUserId, pProjectId, vParent);
          FETCH curResource INTO recResource;
          CLOSE curResource;
          curProperties := opencmsProperty.readAllProperties(pUserId, recOnlineProject.project_id, vParent);
          FETCH curProperties INTO recPropValue, recPropName;
          CLOSE curProperties;
          IF recPropValue != 'error' THEN
            --opencmsProperty.writeProperties(pUserId, pProjectId, recResource.resource_id, recResource.resource_type, curProperties);
            -- write Properties without checking accessWrite
			opencmsProperty.writeProperties(curProperties, recResource.resource_id, recResource.resource_type);
          END IF;
          -- change state of the resource
          --!!!!!! Error ACCESS DENIED with next statement
          -- opencmsResource.chstate(pUserId, pProjectId, vParent, opencmsConstants.C_STATE_UNCHANGED);
          update cms_resources set state = opencmsConstants.C_STATE_UNCHANGED
                 where resource_name = vParent
                 and project_id = pProjectId;
        END IF;
      END LOOP;
      -- now copy the resource pResourceName and its subfolders from online-project to project
      helperCopyResourceToProject(pUserId, pProjectId, recOnlineProject.project_id, vResourceName);
    ELSE
      userErrors.raiseUserError(userErrors.C_NO_ACCESS);
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      RAISE;
  END copyResourceToProject;
-----------------------------------------------------------------------------------------
-- subfunction for function copyResourceToProject, helps to copy the subresources of a
-- folder to a project
-----------------------------------------------------------------------------------------
  PROCEDURE helperCopyResourceToProject(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProjectId IN NUMBER, pResourceName IN VARCHAR2) IS
    curResource userTypes.anyCursor;
    curUpdResource userTypes.anyCursor;
    curSubResource userTypes.anyCursor;
    curProperties userTypes.anyCursor;
    recPropValue cms_properties.property_value%TYPE;
    recPropName cms_propertydef.propertydef_name%TYPE;
    recResource cms_resources%ROWTYPE;
    recUpdResource cms_resources%ROWTYPE;
    recSubResource cms_resources%ROWTYPE;
  BEGIN
    opencmsResource.copyResource(pProjectId, pOnlineProjectId, pResourceName);
    -- copy meta-information
    curResource := opencmsResource.readFileHeader(pUserId, pProjectId, pResourceName);
    FETCH curResource INTO recResource;
    CLOSE curResource;
    curProperties := opencmsProperty.readAllProperties(pUserId, pOnlineProjectId, pResourceName);
    FETCH curProperties INTO recPropValue, recPropName;
    CLOSE curProperties;
    IF recPropValue != 'error' THEN
      curProperties := opencmsProperty.readAllProperties(pUserId, pOnlineProjectId, pResourceName);
      --opencmsProperty.writeProperties(pUserId, pProjectId, recResource.resource_id, recResource.resource_type, curProperties);
      -- write Properties without checking accessWrite
      opencmsProperty.writeProperties(curProperties, recResource.resource_id, recResource.resource_type);
    END IF;
    -- change state of the resource
    --opencmsResource.chstate(pUserId, pProjectId, pResourceName, opencmsConstants.C_STATE_UNCHANGED);
    IF substr(pResourceName, -1, 1) = '/' THEN
      curUpdResource := opencmsResource.readFolder(pUserId, pProjectId, pResourceName);
    ELSE
      curUpdResource := opencmsResource.readFileHeader(pUserId, pProjectId, pResourceName);
    END IF;
    FETCH curUpdResource INTO recUpdResource;
    CLOSE curUpdResource;
    recUpdResource.state := opencmsConstants.C_STATE_UNCHANGED;
    IF substr(pResourceName, -1, 1) = '/' THEN
      opencmsResource.writeFolder(pProjectID, recUpdResource, 'FALSE');
    ELSE
      opencmsResource.writeFileHeader(pProjectID, recUpdResource, 'FALSE');
    END IF;
    commit;
    -- now the subfolders/files of the folder
    IF substr(pResourceName, -1, 1) = '/'  THEN
      -- all files in the folder
      curSubResource := opencmsResource.getFilesInFolder(pUserId, pOnlineProjectId, pResourceName);
      IF curSubResource IS NOT NULL THEN
        LOOP
          FETCH curSubResource INTO recSubResource;
          EXIT WHEN curSubResource%NOTFOUND;
          helperCopyResourceToProject(pUserId, pProjectId, pOnlineProjectId, recSubResource.resource_name);
        END LOOP;
      END IF;
      CLOSE curSubResource;
      -- all folder in the folder
      curSubResource := opencmsResource.getFoldersInFolder(pUserId, pOnlineProjectId, pResourceName);
      IF curSubResource IS NOT NULL THEN
        LOOP
          FETCH curSubResource INTO recSubResource;
          EXIT WHEN curSubResource%NOTFOUND;
          helperCopyResourceToProject(pUserId, pProjectId, pOnlineProjectId, recSubResource.resource_name);
        END LOOP;
      END IF;
      CLOSE curSubResource;
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      RAISE;
  END helperCopyResourceToProject;
--------------------------------------------------------------------------------------------
-- publishes the project: copy (insert/update) the folders, files, properties
-- from the work-project to the online-project
--------------------------------------------------------------------------------------------
  PROCEDURE publishProject (pUserId NUMBER, pProjectId NUMBER, pOnlineProjectId NUMBER,
                            pCurDelFolders OUT userTypes.anyCursor, pCurWriteFolders OUT userTypes.anyCursor,
                            pCurDelFiles OUT userTypes.anyCursor, pCurWriteFiles OUT userTypes.anyCursor) IS
    CURSOR curFolders(cProjectId NUMBER) IS
           select * from cms_resources
                where project_id = cProjectId
                and resource_type = opencmsConstants.C_TYPE_FOLDER
                order by resource_name;

    CURSOR curFiles(cProjectId NUMBER) IS
           select r.*, f.file_content from cms_resources r, cms_files f
                where r.project_id = cProjectId
                and r.resource_type != opencmsConstants.C_TYPE_FOLDER
                and r.file_id = f.file_id(+)
                order by r.resource_name;

    recFolders cms_resources%ROWTYPE;
    recFiles userTypes.fileRecord;
    vParentId NUMBER;
    curNewFolder userTypes.anyCursor;
    recNewFolder cms_resources%ROWTYPE;
    curNewFile userTypes.anyCursor;
    recNewFile userTypes.fileRecord;
    vResourceId cms_resources.resource_id%TYPE;
    vDeletedFolders VARCHAR2(32767) := '';
    vCurDelFolders VARCHAR2(32767) := '';
    vCurDelFiles VARCHAR2(32767) := '';
    vCurWriteFolders VARCHAR2(32767) := '';
    vCurWriteFiles VARCHAR2(32767) := '';
  BEGIN
    ---------------------------------
    -- for all folders of the project
    ---------------------------------
    OPEN curFolders(pProjectId);
    LOOP
      FETCH curFolders INTO recFolders;
      EXIT WHEN curFolders%NOTFOUND;
      -- is the resource marked as deleted?
      IF recFolders.state = opencmsConstants.C_STATE_DELETED THEN
        -- add to list with deleted folders
        vDeletedFolders := vDeletedFolders||'/'||to_char(recFolders.resource_id);
      -- is the resource marked as new?
      ELSIF recFolders.state = opencmsConstants.C_STATE_NEW THEN
        BEGIN
          select resource_id into vParentId
                 from cms_resources
                 where project_id = pOnlineProjectId
                 and resource_name = opencmsResource.getParent(recFolders.resource_name);
        EXCEPTION
          WHEN NO_DATA_FOUND THEN
            vParentId := opencmsConstants.C_UNKNOWN_ID;
        END;
        opencmsResource.createFolder(pUserId, pOnlineProjectId, pOnlineProjectId, recFolders,
                                     vParentId, recFolders.resource_name, curNewFolder);
        FETCH curNewFolder INTO recNewFolder;
        CLOSE curNewFolder;
        recNewFolder.state := opencmsConstants.C_STATE_UNCHANGED;
        opencmsResource.writeFolder(pProjectId, recNewFolder, 'FALSE');
        -- copy properties
        opencmsProperty.writeProperties(opencmsProperty.readAllProperties(pUserId, pProjectId, recFolders.resource_name),
                                        recNewFolder.resource_id, recNewFolder.resource_type);
        -- cursor for checkExport/discAccess
        vCurWriteFolders := vCurWriteFolders||','||recNewFolder.resource_id;
      -- is the resource marked as changed?
      ELSIF recFolders.state = opencmsConstants.C_STATE_CHANGED THEN
        -- checkExport ???
        -- does the folder exist in the online-project?
        curNewFolder := opencmsResource.readFolder(pUserId, pOnlineProjectId, recFolders.resource_name);
        FETCH curNewFolder INTO recNewFolder;
        CLOSE curNewFolder;
        -- folder does not exist in online-project => create folder
        IF recNewFolder.resource_id IS NULL THEN
          BEGIN
            select resource_id into vParentId
                   from cms_resources
                   where project_id = pOnlineProjectId
                   and resource_name = opencmsResource.getParent(recFolders.resource_name);
          EXCEPTION
            WHEN NO_DATA_FOUND THEN
              vParentId := opencmsConstants.C_UNKNOWN_ID;
          END;
          opencmsResource.createFolder(pUserId, pOnlineProjectId, pOnlineProjectId, recFolders,
                                       vParentId, recFolders.resource_name, curNewFolder);
          FETCH curNewFolder INTO recNewFolder;
          CLOSE curNewFolder;
          recNewFolder.state := opencmsConstants.C_STATE_UNCHANGED;
          opencmsResource.writeFolder(pProjectId, recNewFolder, 'FALSE');
        END IF;
        -- update the folder in the online-project
        update cms_resources set
               resource_type = recFolders.resource_type,
               resource_flags = recFolders.resource_flags,
               user_id = recFolders.user_id,
               group_id = recFolders.group_id,
               project_id = pOnlineProjectId,
               access_flags = recFolders.access_flags,
               state = opencmsConstants.C_STATE_UNCHANGED,
               locked_by = recFolders.locked_by,
               launcher_type = recFolders.launcher_type,
               launcher_classname = recFolders.launcher_classname,
               date_lastmodified = sysdate,
               resource_lastmodified_by = recFolders.resource_lastmodified_by,
               resource_size = 0,
               file_id = recFolders.file_id
               where resource_id = recNewFolder.resource_id;
        commit;
        -- copy the properties
        delete from cms_properties where resource_id = recNewFolder.resource_id;
        opencmsProperty.writeProperties(opencmsProperty.readAllProperties(pUserId, pProjectId, recFolders.resource_name),
                                        recNewFolder.resource_id, recNewFolder.resource_type);
        -- cursor for checkExport/discAccess
        vCurWriteFolders := vCurWriteFolders||','||recNewFolder.resource_id;
      -- is the resource unchanged?
      ELSIF recFolders.state = opencmsConstants.C_STATE_UNCHANGED THEN
        curNewFolder := opencmsResource.readFolder(pUserId, pOnlineProjectId, recFolders.resource_name);
        FETCH curNewFolder INTO recNewFolder;
        CLOSE curNewFolder;
        -- folder does not exist in online-project => create folder
        IF recNewFolder.resource_id IS NULL THEN
          BEGIN
            select resource_id into vParentId
                   from cms_resources
                   where project_id = pOnlineProjectId
                   and resource_name = opencmsResource.getParent(recFolders.resource_name);
          EXCEPTION
            WHEN NO_DATA_FOUND THEN
               vParentId := opencmsConstants.C_UNKNOWN_ID;
          END;
          opencmsResource.createFolder(pUserId, pOnlineProjectId, pOnlineProjectId, recFolders,
                                       vParentId, recFolders.resource_name, curNewFolder);
          FETCH curNewFolder INTO recNewFolder;
          CLOSE curNewFolder;
          recNewFolder.state := opencmsConstants.C_STATE_UNCHANGED;
          opencmsResource.writeFolder(pProjectId, recNewFolder, 'FALSE');
          opencmsProperty.writeProperties(opencmsProperty.readAllProperties(pUserId, pProjectId, recFolders.resource_name),
                                          recNewFolder.resource_id, recNewFolder.resource_type);
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
      -- resource of offline-project is marked for delete
      IF substr(recFiles.resource_name,1,1) = opencmsConstants.C_TEMP_PREFIX THEN
        delete from cms_resources where project_id = pProjectId and resource_name = recFiles.resource_name;
      -- resource is deleted
      ELSIF recFiles.state = opencmsConstants.C_STATE_DELETED THEN
        --checkExport ???
        curNewFile := opencmsResource.readFile(pUserId, pOnlineProjectId, recFiles.resource_name);
        FETCH curNewFile INTO recNewFile;
        CLOSE curNewFile;
        delete from cms_properties where resource_id = recNewFile.resource_id;
        delete from cms_resources where resource_id = recNewFile.resource_id;
        -- cursor for checkExport/discAccess
        vCurDelFiles := vCurDelFiles||','||recNewFile.resource_id;
      -- resource is new
      ELSIF recFiles.state = opencmsConstants.C_STATE_NEW THEN
        -- checkExport ???
        BEGIN
          select resource_id into vParentId
                 from cms_resources
                 where project_id = pOnlineProjectId
                 and resource_name = opencmsResource.getParent(recFiles.resource_name);
        EXCEPTION
          WHEN NO_DATA_FOUND THEN
            vParentId := opencmsConstants.C_UNKNOWN_ID;
        END;
        opencmsResource.createFile(pOnlineProjectId, pOnlineProjectId, recFiles, pUserId, vParentId,
                                   recFiles.resource_name, 'FALSE', curNewFile);
        FETCH curNewFile INTO recNewFile;
        CLOSE curNewFile;
        recNewFile.state := opencmsConstants.C_STATE_UNCHANGED;
        opencmsResource.writeFile(pOnlineProjectId, recNewFile, 'FALSE');
        -- copy the properties
        opencmsProperty.writeProperties(opencmsProperty.readAllProperties(pUserId, pProjectId, recFiles.resource_name),
                                        recNewFile.resource_id, recNewFile.resource_type);
        -- cursor for checkExport/discAccess
        vCurWriteFiles := vCurWriteFiles||','||recNewFile.resource_id;
      -- resource is changed
      ELSIF recFiles.state = opencmsConstants.C_STATE_CHANGED THEN
        -- does the folder exist in the online-project?
        curNewFile := opencmsResource.readFile(pUserId, pOnlineProjectId, recFiles.resource_name);
        FETCH curNewFile INTO recNewFile;
        CLOSE curNewFile;
        -- folder does not exist in online-project => create folder
        IF recNewFile.resource_id IS NULL THEN
          BEGIN
            select resource_id into vParentId
                   from cms_resources
                   where project_id = pOnlineProjectId
                   and resource_name = opencmsResource.getParent(recFiles.resource_name);
          EXCEPTION
            WHEN NO_DATA_FOUND THEN
              vParentId := opencmsConstants.C_UNKNOWN_ID;
          END;
          opencmsResource.createFile(pOnlineProjectId, pOnlineProjectId, recFiles, pUserId,
                                     vParentId, recFiles.resource_name, 'FALSE', curNewFile);
          FETCH curNewFile INTO recNewFile;
          CLOSE curNewFile;
          recNewFile.state := opencmsConstants.C_STATE_UNCHANGED;
          opencmsResource.writeFile(pProjectId, recNewFile, 'FALSE');
        END IF;
        -- update the file in the online-project
        update cms_resources set
               resource_type = recFiles.resource_type,
               resource_flags = recFiles.resource_flags,
               user_id = recFiles.user_id,
               group_id = recFiles.group_id,
               project_id = pOnlineProjectId,
               access_flags = recFiles.access_flags,
               state = opencmsConstants.C_STATE_UNCHANGED,
               locked_by = recFiles.locked_by,
               launcher_type = recFiles.launcher_type,
               launcher_classname = recFiles.launcher_classname,
               date_lastmodified = sysdate,
               resource_lastmodified_by = recFiles.resource_lastmodified_by,
               resource_size = recFiles.resource_size,
               file_id = recFiles.file_id
               where resource_id = recNewFolder.resource_id;
        commit;
        -- copy the properties
        delete from cms_properties where resource_id = recNewFile.resource_id;
        opencmsProperty.writeProperties(opencmsProperty.readAllProperties(pUserId, pProjectId, recFiles.resource_name),
                                        recNewFile.resource_id, recNewFile.resource_type);
        -- cursor for checkExport/discAccess
        vCurWriteFiles := vCurWriteFiles||','||recNewFile.resource_id;
      END IF;
    END LOOP;
    CLOSE curFiles;
    -- now remove the folders
    IF length(vDeletedFolders) > 0 THEN
      -- get the string for the cursor of
      vCurDelFolders := replace(substr(vDeletedFolders,2),'/',',');
      vDeletedFolders := vDeletedFolders||'/';
      LOOP
        vResourceId := substr(vDeletedFolders, instr(vDeletedFolders, '/', 1, 1)+1,
                       (instr(vDeletedFolders, '/', 1, 2) - (instr(vDeletedFolders, '/', 1, 1)+1)));
        vDeletedFolders := substr(vDeletedFolders, (instr(vDeletedFolders, '/', 1, 2)));
        delete from cms_properties where resource_id = vResourceId;
        delete from cms_resources where resource_id = vResourceId;
        IF length(vDeletedFolders) <= 1 THEN
          EXIT;
        END IF;
      END LOOP;
      commit;
    END IF;
    -- build the cursors which are used in java for the discAccess
    BEGIN
      IF length(vCurDelFolders) > 0 THEN
        OPEN pCurDelFolders FOR 'select resource_name from cms_resources where resource_id in ('||vCurDelFolders||')';
      ELSE
        -- return a cursor that contains no rows
        OPEN pCurDelFolders FOR 'select resource_name from cms_resources where resource_id = -1';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        raise_application_error(-20999, 'error open cursor pCurDelFolders: '||substr(vCurDelFolders,1,200));
    END;
    BEGIN
      IF length(vCurWriteFolders) > 0 THEN
        OPEN pCurWriteFolders FOR 'select resource_name from cms_resources where resource_id in ('||substr(vCurWriteFolders,2)||')';
      ELSE
        -- return a cursor that contains no rows
        OPEN pCurWriteFolders FOR 'select resource_name from cms_resources where resource_id = -1';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        raise_application_error(-20999, 'error open cursor pCurWriteFolders: '||substr(vCurWriteFolders,2,200));
    END;
    BEGIN
      IF length(vCurDelFiles) > 0 THEN
        OPEN pCurDelFiles FOR 'select resource_name from cms_resources where resource_id in ('||substr(vCurDelFiles,2)||')';
      ELSE
        -- return a cursor that contains no rows
        OPEN pCurDelFiles FOR 'select resource_name from cms_resources where resource_id = -1';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        raise_application_error(-20999, 'error open cursor pCurDelFolders: '||substr(vCurDelFiles,2,200));
    END;
    BEGIN
      IF length(vCurWriteFiles) > 0 THEN
        OPEN pCurWriteFiles FOR 'select resource_name, file_id from cms_resources where resource_id in ('||substr(vCurWriteFiles,2)||')';
      ELSE
        -- return a cursor that contains no rows
        OPEN pCurWriteFiles FOR 'select resource_name, file_id from cms_resources where resource_id = -1';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        raise_application_error(-20999, 'error open cursor pCurWriteFiles: '||substr(vCurWriteFiles,2,200));
    END;
  EXCEPTION
    WHEN OTHERS THEN
      rollback;
      RAISE;
  END publishProject;
-----------------------------------------------------------------------------------------
-- returns a cursor with the online-project
-----------------------------------------------------------------------------------------
  FUNCTION onlineProject RETURN userTypes.anyCursor IS
    recOnlineProject userTypes.anyCursor;
  BEGIN
    OPEN recOnlineProject FOR select * from cms_projects
                                     where project_id = openCmsConstants.C_PROJECT_ONLINE_ID order by project_name;
    RETURN recOnlineProject;
  END onlineProject;
-----------------------------------------------------------------------------------------
-- returns a cursor with the online-project
-----------------------------------------------------------------------------------------
  FUNCTION onlineProject(pProjectId NUMBER) RETURN userTypes.anyCursor IS
    curOnlineProject userTypes.anyCursor;
    vCount NUMBER;
  BEGIN
/* for multisite now disabled
    -- read Online Project
    select count(*) into vCount from cms_projects p, cms_sites s
                                     where p.project_id = pProjectId
                                     and s.onlineproject_id = p.project_id;
    IF vCount > 0 THEN
      OPEN curOnlineProject FOR select p.* from cms_projects p, cms_sites s
                                         where p.project_id = pProjectId
                                         and s.onlineproject_id = p.project_id;
    ELSE
      -- read Parent Project
      select count(*) into vCount from cms_projects pp, cms_projects cp
                                  where cp.parent_id = pp.project_id
                                  and cp.project_id = pProjectId;
      IF vCount > 0 THEN
        OPEN curOnlineProject FOR select pp.* from cms_projects pp, cms_projects cp
                                           where cp.parent_id = pp.project_id
                                           and cp.project_id = pProjectId;
      ELSE
        -- read current Project
        OPEN curOnlineProject FOR select * from cms_projects where project_id = pProjectId;
      END IF;
    END IF;
*/ 
    OPEN curOnlineProject FOR select * from cms_projects
                              where project_id = openCmsConstants.C_PROJECT_ONLINE_ID order by project_name;   
    RETURN curOnlineProject;
  END onlineProject;
------------------------------------------------------------------------------------------
END ;
/
