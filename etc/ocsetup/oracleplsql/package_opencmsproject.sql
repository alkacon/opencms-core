CREATE OR REPLACE
PACKAGE OpenCmsProject IS

  FUNCTION getAllAccessibleProjects(pUserID IN NUMBER) RETURN userTypes.anyCursor;
  PROCEDURE createProject(pUserId IN NUMBER, pProjectName IN VARCHAR2, pProjectDescription IN VARCHAR2,
                         pGroupName IN VARCHAR2, pManagerGroupName IN VARCHAR2, pTaskID IN NUMBER,
                         pProject OUT userTypes.anyCursor);
  PROCEDURE publishProject (pUserId NUMBER, pProjectId NUMBER, pOnlineProjectId NUMBER,
                            pEnableHistory NUMBER, pPublishDate DATE,
                            pCurDelFolders OUT userTypes.anyCursor, pCurWriteFolders OUT userTypes.anyCursor,
                            pCurDelFiles OUT userTypes.anyCursor, pCurWriteFiles OUT userTypes.anyCursor);
  FUNCTION onlineProject RETURN cms_projects%ROWTYPE;
  FUNCTION onlineProject(pProjectId NUMBER) RETURN cms_projects%ROWTYPE;
END ;
/
