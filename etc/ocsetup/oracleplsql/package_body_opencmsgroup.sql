CREATE OR REPLACE
PACKAGE BODY OpenCmsGroup IS
------------------------------------------------------------------------------------
-- declare variables/procedures/functions which are used in this package
------------------------------------------------------------------------------------
  bAnyList VARCHAR2(100) := ''; -- string with IDs of selected groups
  FUNCTION addInList(pAnyId NUMBER) RETURN BOOLEAN;

------------------------------------------------------------------------------------
-- returns true if user is member of group with group_name = pGroupName
------------------------------------------------------------------------------------
  FUNCTION userInGroup(pUserId NUMBER, pGroupId NUMBER) RETURN NUMBER IS
      CURSOR cUserGroups IS
             select g.group_id, g.group_name
                    from cms_groupusers gu, cms_users u, cms_groups g
                    where u.user_id = pUserId
                    and u.user_id = gu.user_id
                    and gu.group_id = g.group_id;

      vSubId  NUMBER;
  BEGIN
	FOR vUserGroups IN cUserGroups
	LOOP
      IF vUserGroups.group_id != pGroupId THEN
	    vSubId := getParent(vUserGroups.group_id);
        WHILE vSubId IS NOT NULL LOOP
	        IF vSubId != pGroupId THEN
              vSubId := getParent(vSubId);
            ELSE
              RETURN 1;
            END IF;
          END LOOP;
      ELSE
        RETURN 1;
      END IF;
    END LOOP;
    RETURN 0;
  END userInGroup;
---------------------------------------------------------------------------------
-- returns cursor for all groups the user belongs to directly and all subgroups
---------------------------------------------------------------------------------
   FUNCTION getGroupsOfUser (pUserID IN NUMBER) RETURN userTypes.anyCursor IS
      CURSOR curUserGroups IS
             select g.group_id, g.group_name
                    from cms_groupusers gu, cms_users u, cms_groups g
                    where u.user_id = pUserID
                    and u.user_id = gu.user_id
                    and gu.group_id = g.group_id;

      recUserGroup curUserGroups%ROWTYPE;
      curGroups userTypes.anyCursor;
      vSubId  NUMBER;
      vQueryStr VARCHAR2(32767) := '';
   BEGIN
     -- create query-String dynamicly
     -- first select with groups the user belongs to directly
     OPEN curUserGroups;
	 LOOP
	   FETCH curUserGroups INTO recUserGroup;
       -- loop for all groups the user belongs to directly
	   EXIT WHEN curUserGroups%NOTFOUND;
       IF addInList(recUserGroup.group_id) THEN
         -- if the group wasn't called already => find subgroup
	     vSubId := opencmsgroup.getParent(recUserGroup.group_id);
         WHILE vSubId IS NOT NULL LOOP
           IF addInList(vSubId) THEN
             -- if the group wasn't called already => find subgroup and edit query-string
             vQueryStr := vQueryStr||' union select * from cms_groups where group_id='||to_char(vSubId);
           END IF;
           vSubId := opencmsgroup.getParent(vSubId);           
         END LOOP;
       END IF;
     END LOOP;
     CLOSE curUserGroups;
     bAnyList := '';
     -- open and return the cursor
     OPEN curGroups FOR 'select g.* from cms_groupusers gu, cms_users u, cms_groups g'||
                        ' where u.user_id = '||to_char(pUserID)||
                        ' and u.user_id = gu.user_id and gu.group_id = g.group_id'||vQueryStr;
     RETURN curGroups;
   END getGroupsOfUser;
-------------------------------------------------------------------------------------------------
-- function checks if user is owner/manager of the project and returns boolean as binary integer
-------------------------------------------------------------------------------------------------
  FUNCTION isManagerOfProject(pUserID NUMBER, pProjectId NUMBER) RETURN NUMBER IS
    vProjectOwner NUMBER;
    vProjectManager NUMBER;
    vCursor userTypes.anyCursor;
    vGroupId cms_groups.group_id%TYPE;
    vGroupName cms_groups.group_name%TYPE;
  BEGIN
    select user_id, managergroup_id into vProjectOwner, vProjectManager
           from cms_projects
           where project_id = pProjectId;
    -- is user owner of the project?
    IF vProjectOwner = pUserID THEN
      RETURN 1;
    END IF;
    -- is the user member of the managergroup of this project?
    vCursor := getGroupsOfUser(pUserID);
    LOOP
      FETCH vCursor INTO vGroupID, vGroupName;
      EXIT WHEN vCursor%NOTFOUND;
      IF vGroupID = vProjectManager THEN
        GOTO ENDTRUE;
      END IF;
    END LOOP;
    CLOSE vCursor;
    RETURN 0;
    <<ENDTRUE>>
    CLOSE vCursor;
    RETURN 1;
  END;
-------------------------------------------------------------------------------------------------
-- returns cursor for informations about all user which are member of the group
-------------------------------------------------------------------------------------------------
  FUNCTION getUsersOfGroup(pUserID NUMBER, pGroupName VARCHAR2, pType NUMBER) RETURN userTypes.anyCursor IS
    curUsersOfGroup userTypes.anyCursor;
    CURSOR curGuests IS
           select u.user_id from cms_users u, cms_groups g
                  where u.user_name = opencmsConstants.C_USER_GUEST
                  and u.user_type = opencmsConstants.C_USER_TYPE_SYSTEMUSER
                  and u.user_default_group_id = g.group_id;
  BEGIN
    -- if the user belongs to group guest then return error
    FOR recGuests IN curGuests
    LOOP
      IF recGuests.user_id = pUserID THEN
        GOTO ERROR;
      END IF;
    END LOOP;
--    CLOSE curGuests;
    -- open and return cursor with the users of the group
    OPEN curUsersOfGroup FOR 'SELECT U.USER_INFO, U.USER_ID, U.USER_NAME, U.USER_PASSWORD, U.USER_RECOVERY_PASSWORD, '||
                            'U.USER_DESCRIPTION, U.USER_FIRSTNAME, U.USER_LASTNAME, U.USER_EMAIL, U.USER_LASTLOGIN, '||
                            'U.USER_LASTUSED, U.USER_FLAGS, U.USER_DEFAULT_GROUP_ID, DG.PARENT_GROUP_ID, DG.GROUP_NAME, '||
                            'DG.GROUP_DESCRIPTION, DG.GROUP_FLAGS, U.USER_ADDRESS, U.USER_SECTION, U.USER_TYPE '||
                            'FROM cms_GROUPS G, cms_USERS U, cms_GROUPUSERS GU, cms_GROUPS DG '||
                            'where G.GROUP_NAME = '''||pGroupName||''' AND U.USER_ID=GU.USER_ID AND GU.GROUP_ID = G.GROUP_ID '||
                            'AND U.USER_DEFAULT_GROUP_ID = DG.GROUP_ID AND U.USER_TYPE = '||
                            to_char(pType)||' ORDER BY USER_NAME';

    RETURN curUsersOfGroup;
    <<ERROR>>
    CLOSE curGuests;
    OPEN curUsersOfGroup FOR select 'error' error_type from dual;
    RETURN curUsersOfGroup;
  END;
-------------------------------------------------------------------
-- return the subgroup of the group with group_id = pGroupId
-------------------------------------------------------------------
    FUNCTION getParent(pGroupId NUMBER) RETURN NUMBER IS
      vParentId NUMBER;
    BEGIN
      select decode(parent_group_id, -1, NULL, parent_group_id) into vParentId
             from cms_groups
             where group_id = pGroupId;
      RETURN vParentId;
    EXCEPTION
      WHEN OTHERS THEN
        RETURN NULL;
    END getParent;
------------------------------------------------------------------------------------
-- private function checks if id is already in list, if not edit the list
-- and return boolean
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
---------------------------------------------------------------------------------------
END;
/
