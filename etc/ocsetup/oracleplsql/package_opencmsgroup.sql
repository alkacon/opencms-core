CREATE OR REPLACE
PACKAGE OpenCmsGroup IS
  FUNCTION userInGroup(pUserId NUMBER, pGroupId NUMBER) RETURN NUMBER;
  FUNCTION getGroupsOfUser (pUserID IN NUMBER) RETURN userTypes.anyCursor;
  FUNCTION getParent (pGroupId NUMBER) RETURN NUMBER;
  FUNCTION isManagerOfProject(pUserID NUMBER, pProjectId NUMBER) RETURN NUMBER;
  FUNCTION getUsersOfGroup(pUserID NUMBER, pGroupName VARCHAR2, pType NUMBER) RETURN userTypes.anyCursor;
END;
/
