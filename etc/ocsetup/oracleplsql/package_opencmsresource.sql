CREATE OR REPLACE
PACKAGE opencmsResource IS
  PROCEDURE lockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2, pForce IN VARCHAR2, pResource OUT userTypes.anyCursor);
  PROCEDURE lockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProject NUMBER, pFolderName IN VARCHAR2, pForce IN VARCHAR2);
  PROCEDURE unlockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pFolderName IN VARCHAR2, pResource OUT userTypes.anyCursor);
  PROCEDURE unlockResource(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProject IN NUMBER, pFolderName IN VARCHAR2);

  FUNCTION readFolderAcc(pUserId NUMBER, pProjectID NUMBER, pFolderName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION readFolder(pUserId NUMBER, pProjectID NUMBER, pFolderName VARCHAR2) RETURN userTypes.anyCursor;
  -- readFileHeader with and without checking access
  FUNCTION readFileHeader(pUserId NUMBER, pProjectID NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION readFileHeader(pUserId NUMBER, pProjectID NUMBER, pOnlineProjectId NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor;
  -- readFile with and without checking access
  FUNCTION readFile(pUserId NUMBER, pProjectID NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION readFile(pUserId NUMBER, pProjectID NUMBER, pOnlineProjectId NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION readFileNoAccess(pUserId NUMBER, pProjectID NUMBER, pOnlineProjectId NUMBER, pFileName VARCHAR2) RETURN userTypes.anyCursor;

  PROCEDURE createFolder(pUserId NUMBER, pProjectId NUMBER, pParentResId NUMBER, pFileId NUMBER,
                         pFolderName VARCHAR2, pFlags NUMBER);
  PROCEDURE createFolder(pUserId IN NUMBER, pProjectId IN NUMBER, pOnlineProjectId IN NUMBER,
                         pResource IN cms_resources%ROWTYPE, pParentId IN NUMBER, pFolderName IN VARCHAR2,
                         oResource OUT userTypes.anyCursor);
  PROCEDURE createFile(pProjectId IN NUMBER, pOnlineProjectId IN NUMBER, pResource IN userTypes.fileRecord, pUserId IN NUMBER,
                       pParentId IN NUMBER,  pFileName IN VARCHAR2, pCopy IN VARCHAR2, oResource OUT userTypes.anyCursor);

  PROCEDURE removeFolder(pUserId NUMBER, pProjectId NUMBER, pResourceID NUMBER, pResourceName VARCHAR2);

  PROCEDURE writeFolder(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2);
  PROCEDURE writeFolder(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2, pUserId IN NUMBER);
  PROCEDURE writeFileHeader(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2);
  PROCEDURE writeFileHeader(pProjectID IN NUMBER, pResource IN cms_resources%ROWTYPE, pChange IN VARCHAR2, pUserId IN NUMBER);
  PROCEDURE writeFile(pProjectID IN NUMBER, pResource IN userTypes.fileRecord, pChange IN VARCHAR2);
  PROCEDURE writeFile(pProjectID IN NUMBER, pResource IN userTypes.fileRecord, pChange IN VARCHAR2, pUserId IN NUMBER);
  PROCEDURE copyFile(pProjectId NUMBER, pUserId NUMBER, pSource VARCHAR2, pDestination VARCHAR2);

  PROCEDURE chstate(pUserId IN NUMBER, pProjectId IN NUMBER, pResourceName IN VARCHAR2, pState IN NUMBER);

  --FUNCTION getFilesInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor;
  FUNCTION getFilesInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.resourceTable;

  FUNCTION getFoldersInFolder(pUserId NUMBER, pProjectId NUMBER, pResourceName VARCHAR2) RETURN userTypes.anyCursor;

  PROCEDURE backupFolder(pProjectId IN NUMBER, pFolder IN cms_resources%ROWTYPE, pVersionId IN NUMBER, pPublishDate IN DATE);
  PROCEDURE backupFile(pProjectId IN NUMBER, pFile IN userTypes.fileRecord, pVersionId IN NUMBER, pPublishDate IN DATE);

  FUNCTION getParent(pResourceName VARCHAR2) RETURN VARCHAR2;
  FUNCTION getParentId(pProjectID NUMBER, pResourceId NUMBER) RETURN NUMBER;
  
  PROCEDURE removeTemporaryFiles(pFilename IN VARCHAR2);
END;
/
