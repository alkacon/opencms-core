CREATE OR REPLACE
PACKAGE opencmsResource IS
  PROCEDURE lockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2, pForce IN VARCHAR2);
  PROCEDURE unlockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2);

  FUNCTION readFolder(pUserId NUMBER, pProjectID NUMBER, pFolderName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION readFileHeader(pUserId NUMBER, pProjectID NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION readFile(pUserId NUMBER, pProjectID NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor;

  PROCEDURE createFolder(pUserId NUMBER, pProjectId NUMBER, pParentResId NUMBER, pFileId NUMBER,
                         pFolderName VARCHAR2, pFlags NUMBER);
  PROCEDURE createFolder(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProjectId IN NUMBER,
                         pResource IN cms_resources%ROWTYPE, pParentId IN NUMBER, pFolderName IN VARCHAR2,
                         oResource OUT userTypes.anyCursor);
  PROCEDURE createFile(pProjectId IN NUMBER, pOnlineProjectId IN NUMBER, pResource IN userTypes.fileRecord, pUserId IN NUMBER,
                       pParentId IN NUMBER,  pFileName IN VARCHAR2, pCopy IN VARCHAR2, oResource OUT userTypes.anyCursor);
                       
  PROCEDURE removeFolder(pUserId NUMBER, pProjectId NUMBER, pResourceID NUMBER, pResourceName VARCHAR2);

  PROCEDURE writeFolder(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2);
  PROCEDURE writeFileHeader(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2);
  PROCEDURE writeFile(pProjectID IN NUMBER, pResource IN userTypes.fileRecord, pChange IN VARCHAR2);

  PROCEDURE chstate(pUserId IN NUMBER, pProjectId IN NUMBER, pResourceName IN VARCHAR2, pState IN NUMBER);
  PROCEDURE copyResource(pToProjectID IN NUMBER, pFromProjectID IN NUMBER, pResourceName IN VARCHAR2);

  FUNCTION getFilesInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION getFoldersInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION getParent(pResourceName VARCHAR2) RETURN VARCHAR2;
  FUNCTION getParentId(pProjectID NUMBER, pResourceId NUMBER) RETURN NUMBER;
END;
/
