package com.opencms.file;

import java.util.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class describes the access to files and folders in the Cms.<BR/>
 * This implementation is the acces module to a MySql database.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2000/01/13 12:13:39 $
 */
 class CmsAccessFileMySql implements I_CmsAccessFile, I_CmsConstants  {

    /**
    * This is the connection object to the database
    */
    private Connection m_Con  = null;
    
    /**
    * This is the mountpoint of this filesystem module.
    */
    private CmsMountPoint m_mountpoint  = null;
    
     /**
     * SQL Command for writing a new resource. 
     * A resource includes all data of the fileheader.
     */   
    private static final String C_RESOURCE_WRITE = "INSERT INTO RESOURCES VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
     /**
     * SQL Command for reading a resource. 
     * A resource includes all data of the fileheader.
     */   
    private static final String C_RESOURCE_READ = "SELECT * FROM RESOURCES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
  
     /**
     * SQL Command for reading a file and its content from the online project. 
     */   
    private static final String C_FILE_READ_ONLINE = "SELECT RESOURCE_TYPE,"
                                                     +"RESOURCE_FLAGS,USER_ID,"
                                                     +"GROUP_ID,ACCESS_FLAGS,STATE,"
                                                     +"LOCKED_BY,LAUNCHER_TYPE,LAUNCHER_CLASSNAME,"
                                                     +"DATE_CREATED,DATE_LASTMODIFIED,SIZE, "
                                                     +"FILES.FILE_CONTENT FROM RESOURCES,FILES "
                                                     +"WHERE RESOURCES.RESOURCE_NAME = FILES.RESOURCE_NAME "
                                                     +"AND RESOURCES.PROJECT_ID = FILES.PROJECT_ID "
                                                     +"AND RESOURCES.RESOURCE_NAME = ? "
                                                     +"AND RESOURCES.PROJECT_ID = ?";
    
    /**
     * SQL Command for reading a file content. 
     */   
    private static final String C_FILE_READ = "SELECT FILE_CONTENT FROM FILES "
                                              +"WHERE RESOURCE_NAME = ? "                                           
                                              +"AND PROJECT_ID = ?";
                                              
     /**
     * SQL Command for reading all headers of a resource. 
     * A resource includes all data of the fileheader.
     */   
    private static final String C_RESOURCE_READ_ALL = "SELECT * FROM RESOURCES WHERE RESOURCE_NAME = ?";

    
     /**
     * SQL Command for writing a new file.
     */   
    private static final String C_FILE_WRITE = "INSERT INTO FILES VALUES(?,?,?)";
     

    /**
     * SQL Command for updating a resource. 
     * A resource includes all data of the fileheader.
     */   
    private static final String C_RESOURCE_UPDATE ="UPDATE RESOURCES SET "
                                               +"RESOURCE_TYPE = ? , "
                                               +"RESOURCE_FLAGS = ? , "
                                               +"USER_ID = ? , "
                                               +"GROUP_ID = ? , "
                                               +"ACCESS_FLAGS = ? ,"
                                               +"STATE = ? , "
                                               +"LOCKED_BY = ? , "
                                               +"LAUNCHER_TYPE = ? , "
                                               +"LAUNCHER_CLASSNAME = ? ," 
                                               +"DATE_LASTMODIFIED = ? ,"
                                               +"SIZE = ? "
                                               +"WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";

     /**
     * SQL Command for updating a file. 
     */   
    private static final String C_FILE_UPDATE ="UPDATE FILES SET "
                                               +"FILE_CONTENT = ? "                                     
                                               +"WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
    
     /**
     * SQL Command for removing a resource.
     * This resource is NOT delete from the database, it is only flaged as
     * deleted!
     */   
    private static final String C_RESOURCE_REMOVE = "UPDATE RESOURCES SET "
                                                   +"STATE = ? "
                                                   +"WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";

     /**
     * SQL Command for reading all files of a project. 
     */   
    private static final String C_PROJECT_READ_FILES= "SELECT RESOURCES.RESOURCE_NAME, "
                                                     +"RESOURCE_TYPE,"
                                                     +"RESOURCE_FLAGS,USER_ID,"
                                                     +"GROUP_ID,ACCESS_FLAGS,STATE,"
                                                     +"LOCKED_BY,LAUNCHER_TYPE,LAUNCHER_CLASSNAME,"
                                                     +"DATE_CREATED,DATE_LASTMODIFIED,SIZE,"
                                                     +"FILES.FILE_CONTENT FROM RESOURCES,FILES "
                                                     +"WHERE RESOURCES.RESOURCE_NAME = FILES.RESOURCE_NAME "
                                                     +"AND RESOURCES.PROJECT_ID = FILES.PROJECT_ID "
                                                     +"AND RESOURCES.PROJECT_ID = ?";
    
    /**
     * SQL Command for reading all folders of a project. 
     */   
    private static final String C_PROJECT_READ_FOLDER= "SELECT RESOURCE_NAME, "
                                                     +"RESOURCE_TYPE,"
                                                     +"RESOURCE_FLAGS,USER_ID,"
                                                     +"GROUP_ID,ACCESS_FLAGS,STATE,"
                                                     +"LOCKED_BY,LAUNCHER_TYPE,LAUNCHER_CLASSNAME,"
                                                     +"DATE_CREATED,DATE_LASTMODIFIED,SIZE "
                                                     +"FROM RESOURCES "
                                                     +"WHERE  RESOURCES.PROJECT_ID = ? "
                                                     +"AND RESOURCE_TYPE = ?";
                                                                                                                                    
     /**
     * SQL Command for deleteing a resource.
     */   
    private static final String C_RESOURCE_DELETE = "DELETE FROM RESOURCES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";

     /**
     * SQL Command for deleteing a file.
     */   
    private static final String C_FILE_DELETE = "DELETE FROM FILES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
            
  
     /**
     * SQL Command for getting all subfolders.
     * Because of the regular expression in this SQL command, this cannot be made with 
     * a prepared statement.
     */   
    private static final String C_RESOURCE_GETSUBFOLDERS1 = "SELECT * FROM RESOURCES WHERE RESOURCE_NAME RLIKE '^";
     
     /**
     * SQL Command for getting all subfolders.
     * Because of the regular expression in this SQL command, this cannot be made with 
     * a prepared statement.
     */   
    private static final String C_RESOURCE_GETSUBFOLDERS2 = "[^/]+/$' AND PROJECT_ID = ";
    
     /**
     * SQL Command for getting all subfolders.
     * Because of the regular expression in this SQL command, this cannot be made with 
     * a prepared statement.
     */   
    private static final String C_RESOURCE_GETSUBFOLDERS3 = " ORDER BY RESOURCE_NAME ASC";
                                          
            
      /**
     * SQL Command for getting all files of a folder.
     * Because of the regular expression in this SQL command, this cannot be made with 
     * a prepared statement.
     */   
    private static final String C_RESOURCE_GETFILESINFOLDER1 = "SELECT * FROM RESOURCES WHERE RESOURCE_NAME RLIKE '^";
     
     /**
     * SQL Command for getting all files of a folder.
     * Because of the regular expression in this SQL command, this cannot be made with 
     * a prepared statement.
     */   
    private static final String C_RESOURCE_GETFILESINFOLDER2 = "[^/]+$' AND PROJECT_ID = ";
    
     /**
     * SQL Command for getting all files of a folder.
     * Because of the regular expression in this SQL command, this cannot be made with 
     * a prepared statement.
     */   
    private static final String C_RESOURCE_GETFILESINFOLDER3 = " ORDER BY RESOURCE_NAME ASC";
                                          

     /**
     * Name of the column RESOURCE_NAME in the SQL tables RESOURCE and FILES.
     */
    private static final String C_RESOURCE_NAME="RESOURCE_NAME";
    
     /**
     * Name of the column RESOURCE_TYPE in the SQL table RESOURCE.
     */
    private static final String C_RESOURCE_TYPE="RESOURCE_TYPE";
    
     /**
     * Name of the column RESOURCE_FLAGS in the SQL table RESOURCE.
     */
    private static final String C_RESOURCE_FLAGS="RESOURCE_FLAGS";
    
     /**
     * Name of the column USER_ID in the SQL table RESOURCE.
     */
    private static final String C_USER_ID="USER_ID";
    
      /**
     * Name of the column GROUP_ID in the SQL table RESOURCE.
     */
    private static final String C_GROUP_ID="GROUP_ID";
    
      /**
     * Name of the column PROJECT_ID in the SQL tables RESOURCE
     */
    private static final String C_PROJECT_ID_RESOURCES="RESOURCES.PROJECT_ID";
 
     /**
     * Name of the column PROJECT_ID 
     */
    private static final String C_PROJECT_ID="PROJECT_ID";
    
    /**
     * Name of the column ACCESS_FLAGS in the SQL table RESOURCE.
     */
    private static final String C_ACCESS_FLAGS="ACCESS_FLAGS";
    
     /**
     * Name of the column STATE in the SQL table RESOURCE.
     */
    private static final String C_STATE="STATE";
    
     /**
     * Name of the column LOCKED_BY in the SQL table RESOURCE.
     */
    private static final String C_LOCKED_BY="LOCKED_BY";
    
     /**
     * Name of the column LAUNCHER_TYPE in the SQL table RESOURCE.
     */
    private static final String C_LAUNCHER_TYPE="LAUNCHER_TYPE";
    
     /**
     * Name of the column LAUNCHER_CLASSNAME in the SQL table RESOURCE.
     */
    private static final String C_LAUNCHER_CLASSNAME="LAUNCHER_CLASSNAME";    
   
     /**
     * Name of the column DATE_CREATED in the SQL table RESOURCE.
     */
    private static final String C_DATE_CREATED="DATE_CREATED";    
      
     /**
     * Name of the column DATE_LASTMODIFIED in the SQL table RESOURCE.
     */
    private static final String C_DATE_LASTMODIFIED="DATE_LASTMODIFIED";    
      
     /**
     * Name of the column SIZE in the SQL table RESOURCE.
     */
    private static final String C_SIZE="SIZE";
   
     /**
     * Name of the column FILE_CONTENT in the SQL table FILE.
     */
    private static final String C_FILE_CONTENT="FILE_CONTENT";
    
    /**
    * Prepared SQL Statement for writing a resource.
    */
    private PreparedStatement m_statementResourceWrite;

    /**
    * Prepared SQL Statement for reading a resource.
    */
    private PreparedStatement m_statementResourceRead;
    
    /**
    * Prepared SQL Statement for reading all headers of a resource.
    */
    private PreparedStatement m_statementResourceReadAll;

    /**
    * Prepared SQL Statement for reading a file from the online project.
    */
    private PreparedStatement m_statementFileReadOnline;
      
    /**
    * Prepared SQL Statement for reading a file.
    */
    private PreparedStatement m_statementFileRead;
    
    /**
    * Prepared SQL Statement for writing a resource.
    */
    private PreparedStatement m_statementFileWrite;
        
    /**
    * Prepared SQL Statement for updating a resource.
    */
    private PreparedStatement m_statementResourceUpdate;

    /**
    * Prepared SQL Statement for updating a file.
    */
    private PreparedStatement m_statementFileUpdate;
    
    /**
    * Prepared SQL Statement for deleting a resource.
    */
    private PreparedStatement m_statementResourceDelete;
 
    /**
    * Prepared SQL Statement for removing a resource.
    */
    private PreparedStatement m_statementResourceRemove;
       
    /**
    * Prepared SQL Statement for deleting a file.
    */
    private PreparedStatement m_statementFileDelete;
    
    /**
    * Prepared SQL Statement for getting all files of a project.
    */
    private PreparedStatement m_statementProjectReadFiles;    
    
    /**
    * Prepared SQL Statement for getting all folders of a project.
    */
    private PreparedStatement m_statementProjectReadFolders;    
        
    /**
     * Constructor, creartes a new CmsAccessFileMySql object and connects it to the
     * user information database.
     *
     * @param driver Name of the mySQL JDBC driver.
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
    public CmsAccessFileMySql(A_CmsMountPoint mountpoint)	
        throws CmsException, ClassNotFoundException {
        
        m_mountpoint= (CmsMountPoint) mountpoint;
        Class.forName(mountpoint.getDriver());
        initConnections(mountpoint.getConnect());
        initStatements();
        
    }
    
	 /**
	 * Creates a new file with the given content and resourcetype.
     *
	 * @param user The user who wants to create the file.
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param flags The flags of this resource.
	 * @param contents The contents of the new file.
	 * @param resourceType The resourceType of the new file.
	 * 
	 * @return file The created file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public CmsFile createFile(A_CmsUser user,
                               A_CmsProject project,
                               A_CmsProject onlineProject,
                               String filename, int flags,
							   byte[] contents, A_CmsResourceType resourceType)
							
         throws CmsException {
               
           try {   
               synchronized ( m_statementResourceWrite) {
                // write new resource to the database
                //RESOURCE_NAME
                m_statementResourceWrite.setString(1,absoluteName(filename));
                //RESOURCE_TYPE
                m_statementResourceWrite.setInt(2,resourceType.getResourceType());
                //RESOURCE_FLAGS
                m_statementResourceWrite.setInt(3,flags);
                //USER_ID
                m_statementResourceWrite.setInt(4,user.getId());
                //GROUP_ID
                m_statementResourceWrite.setInt(5,user.getDefaultGroupId());
                //PROJECT_ID
                m_statementResourceWrite.setInt(6,project.getId());
                //ACCESS_FLAGS
                m_statementResourceWrite.setInt(7,C_ACCESS_DEFAULT_FLAGS);
                //STATE
                m_statementResourceWrite.setInt(8,C_STATE_NEW);
                //LOCKED_BY
                m_statementResourceWrite.setInt(9,C_UNKNOWN_ID);
                //LAUNCHER_TYPE
                m_statementResourceWrite.setInt(10,resourceType.getLauncherType());
                //LAUNCHER_CLASSNAME
                m_statementResourceWrite.setString(11,resourceType.getLauncherClass());
                //DATE_CREATED
                m_statementResourceWrite.setLong(12,System.currentTimeMillis());
                //DATE_LASTMODIFIED
                m_statementResourceWrite.setLong(13,System.currentTimeMillis());
                //SIZE
                m_statementResourceWrite.setInt(14,contents.length);
                m_statementResourceWrite.executeUpdate();
               }
               synchronized (m_statementFileWrite) {
                //RESOURCE_NAME
                m_statementFileWrite.setString(1,absoluteName(filename));
                //PROJECT_ID
                m_statementFileWrite.setInt(2,project.getId());
                //FILE_CONTENT
                m_statementFileWrite.setBytes(3,contents);
                m_statementFileWrite.executeUpdate();
                   
               }
         } catch (SQLException e){                        
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         return readFile(project,onlineProject,filename);
     }
	
     /**
	 * Creates a new file from an given CmsFile object and a new filename.
     *
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param file The file to be written to the Cms.
	 * @param filename The complete new name of the file (including pathinformation).
	 * 
	 * @return file The created file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public CmsFile createFile(A_CmsProject project,
                               A_CmsProject onlineProject,
                               CmsFile file,String filename)
         throws CmsException {
            try {   
               synchronized ( m_statementResourceWrite) {
                // write new resource to the database
                //RESOURCE_NAME
                m_statementResourceWrite.setString(1,absoluteName(filename));
                //RESOURCE_TYPE
                m_statementResourceWrite.setInt(2,file.getType());
                //RESOURCE_FLAGS
                m_statementResourceWrite.setInt(3,file.getFlags());
                //USER_ID
                m_statementResourceWrite.setInt(4,file.getOwnerId());
                //GROUP_ID
                m_statementResourceWrite.setInt(5,file.getGroupId());
                //PROJECT_ID
                m_statementResourceWrite.setInt(6,project.getId());
                //ACCESS_FLAGS
                m_statementResourceWrite.setInt(7,file.getAccessFlags());
                //STATE
                m_statementResourceWrite.setInt(8,C_STATE_NEW);
                //LOCKED_BY
                m_statementResourceWrite.setInt(9,file.isLockedBy());
                //LAUNCHER_TYPE
                m_statementResourceWrite.setInt(10,file.getLauncherType());
                //LAUNCHER_CLASSNAME
                m_statementResourceWrite.setString(11,file.getLauncherClassname());
                //DATE_CREATED
                m_statementResourceWrite.setLong(12,file.getDateCreated());
                //DATE_LASTMODIFIED
                m_statementResourceWrite.setLong(13,System.currentTimeMillis());
                //SIZE
                m_statementResourceWrite.setInt(14,file.getContents().length);
                m_statementResourceWrite.executeUpdate();
               }
               synchronized (m_statementFileWrite) {
                //RESOURCE_NAME
                m_statementFileWrite.setString(1,absoluteName(filename));
                //PROJECT_ID
                m_statementFileWrite.setInt(2,project.getId());
                //FILE_CONTENT
                m_statementFileWrite.setBytes(3,file.getContents());
                m_statementFileWrite.executeUpdate();
                   
               }
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         return readFile(project,onlineProject,filename);
      }
     
    
     /**
	 * Creates a new resource from an given CmsResource object.
     *
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resource The resource to be written to the Cms.
	 * 
	 * @return The created resource.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public A_CmsResource createResource(A_CmsProject project,
                                         A_CmsProject onlineProject,
                                         A_CmsResource resource)
         throws CmsException {
            try {   
               synchronized ( m_statementResourceWrite) {
                // write new resource to the database
                //RESOURCE_NAME
                m_statementResourceWrite.setString(1,absoluteName(resource.getAbsolutePath()));
                //RESOURCE_TYPE
                m_statementResourceWrite.setInt(2,resource.getType());
                //RESOURCE_FLAGS
                m_statementResourceWrite.setInt(3,resource.getFlags());
                //USER_ID
                m_statementResourceWrite.setInt(4,resource.getOwnerId());
                //GROUP_ID
                m_statementResourceWrite.setInt(5,resource.getGroupId());
                //PROJECT_ID
                m_statementResourceWrite.setInt(6,project.getId());
                //ACCESS_FLAGS
                m_statementResourceWrite.setInt(7,resource.getAccessFlags());
                //STATE
                m_statementResourceWrite.setInt(8,resource.getState());
                //LOCKED_BY
                m_statementResourceWrite.setInt(9,resource.isLockedBy());
                //LAUNCHER_TYPE
                m_statementResourceWrite.setInt(10,resource.getLauncherType());
                //LAUNCHER_CLASSNAME
                m_statementResourceWrite.setString(11,resource.getLauncherClassname());
                //DATE_CREATED
                m_statementResourceWrite.setLong(12,resource.getDateCreated());
                //DATE_LASTMODIFIED
                m_statementResourceWrite.setLong(13,System.currentTimeMillis());
                //SIZE
                m_statementResourceWrite.setInt(14,resource.getLength());
                m_statementResourceWrite.executeUpdate();
               }
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         return readResource(project,resource.getAbsolutePath());
      } 
     
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile readFile(A_CmsProject project,
                             A_CmsProject onlineProject,
                             String filename)
         throws CmsException {
          
         CmsFile file=null;
         int projectId;
         
         ResultSet res =null;
         try {
             // if the actual prject is the online project read file header and content
             // from the online project
             if (project.equals(onlineProject)) {
                 synchronized(m_statementFileReadOnline) {
                    m_statementFileReadOnline.setString(1,absoluteName(filename));
                    m_statementFileReadOnline.setInt(2,onlineProject.getId());
                    res = m_statementFileReadOnline.executeQuery();  
                    if(res.next()) {
                         file = new CmsFile(filename,
                                            res.getInt(C_RESOURCE_TYPE),
                                            res.getInt(C_RESOURCE_FLAGS),
                                            res.getInt(C_USER_ID),
                                            res.getInt(C_GROUP_ID),
                                            onlineProject.getId(),
                                            res.getInt(C_ACCESS_FLAGS),
                                            res.getInt(C_STATE),
                                            res.getInt(C_LOCKED_BY),
                                            res.getInt(C_LAUNCHER_TYPE),
                                            res.getString(C_LAUNCHER_CLASSNAME),
                                            res.getLong(C_DATE_CREATED),
                                            res.getLong(C_DATE_LASTMODIFIED),
                                            res.getBytes(C_FILE_CONTENT),
                                            res.getInt(C_SIZE)
                                           );
                     } else {
                       throw new CmsException(filename,CmsException.C_NOT_FOUND);  
                    }
                 }                 
             } else {
               // reading a file from an offline project must be done in two steps:
               // first read the file header from the offline project, then get either
               // the file content of the offline project (if it is already existing)
               // or form the online project.
               
               // get the file header
               file=readFileHeader(project,filename);
               // check if the file is marked as deleted
               if (file.getState() == C_STATE_DELETED) {
                   throw new CmsException(CmsException.C_NOT_FOUND); 
               }
               
               
               
               // test if the file content of this file is already existing in the
               // offline project. This is done by checking if the file state is
               // set to UNCHANGED. If it is, the file content must be read from the 
               // online project.
               if (file.getState()==C_STATE_UNCHANGED) {
                   projectId=onlineProject.getId();
               } else {
                   projectId=project.getId();
               }
               // read the file content
               synchronized (m_statementFileRead) {
                   m_statementFileRead.setString(1,absoluteName(filename));
                   m_statementFileRead.setInt(2,projectId);
                   res = m_statementFileRead.executeQuery();
                   if (res.next()) {
                       file.setContents(res.getBytes(C_FILE_CONTENT));
                   } else {
                         throw new CmsException(filename,CmsException.C_NOT_FOUND);  
                   }
               }               
             }                
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         
         return file;
     }
	
	 /**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile readFileHeader(A_CmsProject project, String filename)
         throws CmsException {
                 
         CmsFile file=null;
         ResultSet res =null;
           
         try {  
              synchronized ( m_statementResourceRead) {
                   // read file data from database
                   m_statementResourceRead.setString(1,absoluteName(filename));
                   m_statementResourceRead.setInt(2,project.getId());
                   res = m_statementResourceRead.executeQuery();
               }
               // create new file
               if(res.next()) {
                        file = new CmsFile(res.getString(C_RESOURCE_NAME),
                                           res.getInt(C_RESOURCE_TYPE),
                                           res.getInt(C_RESOURCE_FLAGS),
                                           res.getInt(C_USER_ID),
                                           res.getInt(C_GROUP_ID),
                                           res.getInt(C_PROJECT_ID_RESOURCES),
                                           res.getInt(C_ACCESS_FLAGS),
                                           res.getInt(C_STATE),
                                           res.getInt(C_LOCKED_BY),
                                           res.getInt(C_LAUNCHER_TYPE),
                                           res.getString(C_LAUNCHER_CLASSNAME),
                                           res.getLong(C_DATE_CREATED),
                                           res.getLong(C_DATE_LASTMODIFIED),
                                           new byte[0],
                                           res.getInt(C_SIZE)
                                           );
                         // check if this resource is marked as deleted
                        if (file.getState() == C_STATE_DELETED) {
                            throw new CmsException(file.getAbsolutePath(),CmsException.C_NOT_FOUND);  
                        }
               } else {
                 throw new CmsException(filename,CmsException.C_NOT_FOUND);  
               }
 
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
        return file;
       }
	 
     /**
	 * Reads all file headers of a file in the OpenCms.<BR>
	 * The reading excludes the filecontent.
	 * 
     * @param filename The name of the file to be read.
	 * 
	 * @return Vector of file headers read from the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public Vector readAllFileHeaders(String filename)
         throws CmsException {
          
         CmsFile file=null;
         ResultSet res =null;
         Vector allHeaders = new Vector();
         
         try {  
              synchronized ( m_statementResourceReadAll) {
                   // read file header data from database
                   m_statementResourceReadAll.setString(1,absoluteName(filename));
                   res = m_statementResourceReadAll.executeQuery();
               }
               // create new file headers
               while(res.next()) {
                        file = new CmsFile(res.getString(C_RESOURCE_NAME),
                                           res.getInt(C_RESOURCE_TYPE),
                                           res.getInt(C_RESOURCE_FLAGS),
                                           res.getInt(C_USER_ID),
                                           res.getInt(C_GROUP_ID),
                                           res.getInt(C_PROJECT_ID_RESOURCES),
                                           res.getInt(C_ACCESS_FLAGS),
                                           res.getInt(C_STATE),
                                           res.getInt(C_LOCKED_BY),
                                           res.getInt(C_LAUNCHER_TYPE),
                                           res.getString(C_LAUNCHER_CLASSNAME),
                                           res.getLong(C_DATE_CREATED),
                                           res.getLong(C_DATE_LASTMODIFIED),
                                           new byte[0],
                                           res.getInt(C_SIZE)
                                           );
                       
                        allHeaders.addElement(file);
               }
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         return allHeaders;
     }
     
	/**
	 * Writes a file to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFile(A_CmsProject project,
                           A_CmsProject onlineProject,
                           CmsFile file)
       throws CmsException {
     
           try {   
             // update the file header in the RESOURCE database.
             writeFileHeader(project,onlineProject,file);
             // update the file content in the FILES database.
             synchronized ( m_statementFileUpdate) {
               //FILE_CONTENT
                m_statementFileUpdate.setBytes(1,file.getContents());
                // set query parameters
                m_statementFileUpdate.setString(2,absoluteName(file.getAbsolutePath()));
                m_statementFileUpdate.setInt(3,file.getProjectId());
                m_statementFileUpdate.executeUpdate();
              }
             } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }

      }
	
	 /**
	 * Writes the fileheader to the Cms.
     * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFileHeader(A_CmsProject project,
                                 A_CmsProject onlineProject,
                                 CmsFile file)
         throws CmsException {
         
           ResultSet res;
           byte[] content;
           
           try {  
                // check if the file content for this file is already existing in the
                // offline project. If not, load it from the online project and add it
                // to the offline project.
                if (file.getState() == C_STATE_UNCHANGED) {
                    // read file content form the online project
                   synchronized (m_statementFileRead) {
                        m_statementFileRead.setString(1,absoluteName(file.getAbsolutePath()));
                        m_statementFileRead.setInt(2,onlineProject.getId());     
                        res = m_statementFileRead.executeQuery();
                        if (res.next()) {
                          content=res.getBytes(C_FILE_CONTENT);
                        } else {
                          throw new CmsException(file.getAbsolutePath(),CmsException.C_NOT_FOUND);  
                        }
                   }
                   // add the file content to the offline project.
                   synchronized (m_statementFileWrite) {
                        m_statementFileWrite.setString(1,absoluteName(file.getAbsolutePath()));
                        m_statementFileWrite.setInt(2,project.getId());     
                        m_statementFileWrite.setBytes(3,content);
                        m_statementFileWrite.executeUpdate();
                   }             
                  }             
                
               synchronized ( m_statementResourceUpdate) {
                    // update resource in the database
          
                    //RESOURCE_TYPE
                    m_statementResourceUpdate.setInt(1,file.getType());
                    //RESOURCE_FLAGS
                    m_statementResourceUpdate.setInt(2,file.getFlags());
                    //USER_ID
                    m_statementResourceUpdate.setInt(3,file.getOwnerId());
                    //GROUP_ID
                    m_statementResourceUpdate.setInt(4,file.getGroupId());
                    //ACCESS_FLAGS
                    m_statementResourceUpdate.setInt(5,file.getAccessFlags());
                    //STATE
                    m_statementResourceUpdate.setInt(6,C_STATE_CHANGED);
                    //LOCKED_BY
                    m_statementResourceUpdate.setInt(7,file.isLockedBy());
                    //LAUNCHER_TYPE
                    m_statementResourceUpdate.setInt(8,file.getLauncherType());
                    //LAUNCHER_CLASSNAME
                    m_statementResourceUpdate.setString(9,file.getLauncherClassname());
                    //DATE_LASTMODIFIED
                    m_statementResourceUpdate.setLong(10,System.currentTimeMillis());
                    //SIZE
                    m_statementResourceUpdate.setInt(11,file.getContents().length);
                
                    // set query parameters
                    m_statementResourceUpdate.setString(12,absoluteName(file.getAbsolutePath()));
                    m_statementResourceUpdate.setInt(13,file.getProjectId());
                    m_statementResourceUpdate.executeUpdate();               
                  }
               } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
     }
     
	/**
	 * Renames the file to the new name.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */		
	 public void renameFile(A_CmsProject project,
                            A_CmsProject onlineProject,
                            String oldname, String newname)
         throws CmsException {
         
         // copy the file to the new name
         copyFile(project,onlineProject,oldname,newname);
         // delete the file with the old name
         deleteFile(project,oldname);
     }
	
	/**
	 * Deletes the file.
	 * 
     * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteFile(A_CmsProject project, String filename)
         throws CmsException {
         try { 
            synchronized ( m_statementResourceRemove) {
                   // mark the file as deleted       
                    m_statementResourceRemove.setInt(1,C_STATE_DELETED);
                    m_statementResourceRemove.setString(2,absoluteName(filename));
                    m_statementResourceRemove.setInt(3,project.getId());
                    m_statementResourceRemove.executeUpdate();               
                  }
               } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }        
     }
	
		
	 /**
	 * Copies the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void copyFile(A_CmsProject project,
                          A_CmsProject onlineProject,
                          String source, String destination)
         throws CmsException {
         CmsFile file;
         
         // read sourcefile
         file=readFile(project,onlineProject,source);
         // create destination file
         createFile(project,onlineProject,file,destination);
     }
	
     /**
	 * Creates a new folder 
	 * 
	 * @param user The user who wants to create the folder.
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path to the folder in which the new folder will 
	 * be created.
	 * @param flags The flags of this resource.
	 * 
	 * @return The created folder.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder createFolder(A_CmsUser user,
                                   A_CmsProject project, 
                                   String foldername,
                                   int flags)
         throws CmsException {
           try {   
               synchronized ( m_statementResourceWrite) {
                // write new resource to the database
                //RESOURCE_NAME
                m_statementResourceWrite.setString(1,absoluteName(foldername));
                //RESOURCE_TYPE
                m_statementResourceWrite.setInt(2,C_TYPE_FOLDER);
                //RESOURCE_FLAGS
                m_statementResourceWrite.setInt(3,flags);
                //USER_ID
                m_statementResourceWrite.setInt(4,user.getId());
                //GROUP_ID
                m_statementResourceWrite.setInt(5,user.getDefaultGroupId());
                //PROJECT_ID
                m_statementResourceWrite.setInt(6,project.getId());
                //ACCESS_FLAGS
                m_statementResourceWrite.setInt(7,C_ACCESS_DEFAULT_FLAGS);
                //STATE
                m_statementResourceWrite.setInt(8,C_STATE_NEW);
                //LOCKED_BY
                m_statementResourceWrite.setInt(9,C_UNKNOWN_ID);
                //LAUNCHER_TYPE
                m_statementResourceWrite.setInt(10,C_UNKNOWN_LAUNCHER_ID);
                //LAUNCHER_CLASSNAME
                m_statementResourceWrite.setString(11,C_UNKNOWN_LAUNCHER);
                //DATE_CREATED
                m_statementResourceWrite.setLong(12,System.currentTimeMillis());
                //DATE_LASTMODIFIED
                m_statementResourceWrite.setLong(13,System.currentTimeMillis());
                //SIZE
                m_statementResourceWrite.setInt(14,0);
                m_statementResourceWrite.executeUpdate();
               }
            } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         return readFolder(project,foldername);
     }
     
     /**
	 * Creates a new folder from an existing folder object.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param folder The folder to be written to the Cms.
	 * @param foldername The complete path of the new name of this folder.
	 * 
	 * @return The created folder.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder createFolder(A_CmsProject project,
                                   CmsFolder folder,
                                   String foldername)
         throws CmsException {
             try {   
               synchronized ( m_statementResourceWrite) {
                // write new resource to the database
                //RESOURCE_NAME
                m_statementResourceWrite.setString(1,absoluteName(folder.getAbsolutePath()));
                //RESOURCE_TYPE
                m_statementResourceWrite.setInt(2,folder.getType());
                //RESOURCE_FLAGS
                m_statementResourceWrite.setInt(3,folder.getFlags());
                //USER_ID
                m_statementResourceWrite.setInt(4,folder.getOwnerId());
                //GROUP_ID
                m_statementResourceWrite.setInt(5,folder.getGroupId());
                //PROJECT_ID
                m_statementResourceWrite.setInt(6,project.getId());
                //ACCESS_FLAGS
                m_statementResourceWrite.setInt(7,folder.getAccessFlags());
                //STATE
                m_statementResourceWrite.setInt(8,C_STATE_NEW);
                //LOCKED_BY
                m_statementResourceWrite.setInt(9,folder.isLockedBy());
                //LAUNCHER_TYPE
                m_statementResourceWrite.setInt(10,folder.getLauncherType());
                //LAUNCHER_CLASSNAME
                m_statementResourceWrite.setString(11,folder.getLauncherClassname());
                //DATE_CREATED
                m_statementResourceWrite.setLong(12,folder.getDateCreated());
                //DATE_LASTMODIFIED
                m_statementResourceWrite.setLong(13,System.currentTimeMillis());
                //SIZE
                m_statementResourceWrite.setInt(14,0);
                m_statementResourceWrite.executeUpdate();
               }
            } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         return readFolder(project,folder.getAbsolutePath());
     }

	 /**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The name of the folder to be read.
	 * 
	 * @return The read folder.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder readFolder(A_CmsProject project, String foldername)
         throws CmsException {
         
         CmsFolder folder=null;
         ResultSet res =null;
           
         try {  
              synchronized ( m_statementResourceRead) {
                   // read resource data from database
                   m_statementResourceRead.setString(1,absoluteName(foldername));
                   m_statementResourceRead.setInt(2,project.getId());
                   res = m_statementResourceRead.executeQuery();
               }
               // create new resource
               if(res.next()) {
                        folder = new CmsFolder(res.getString(C_RESOURCE_NAME),
                                               res.getInt(C_RESOURCE_TYPE),
                                               res.getInt(C_RESOURCE_FLAGS),
                                               res.getInt(C_USER_ID),
                                               res.getInt(C_GROUP_ID),
                                               res.getInt(C_PROJECT_ID_RESOURCES),
                                               res.getInt(C_ACCESS_FLAGS),
                                               res.getInt(C_STATE),
                                               res.getInt(C_LOCKED_BY),
                                               res.getLong(C_DATE_CREATED),
                                               res.getLong(C_DATE_LASTMODIFIED)
                                               );
                        // check if this resource is marked as deleted
                        if (folder.getState() == C_STATE_DELETED) {
                            throw new CmsException(folder.getAbsolutePath(),CmsException.C_NOT_FOUND);  
                        }
                   }else {
                 throw new CmsException(foldername,CmsException.C_NOT_FOUND);  
               }
 
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
        return folder;
        
    }
	
     	
     /**
	 * Writes a folder to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete name of the folder (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFolder(A_CmsProject project, CmsFolder folder)
         throws CmsException {
         
           try {   
               synchronized ( m_statementResourceUpdate) {
                // update resource in the database
          
                //RESOURCE_TYPE
                m_statementResourceUpdate.setInt(1,folder.getType());
                //RESOURCE_FLAGS
                m_statementResourceUpdate.setInt(2,folder.getFlags());
                //USER_ID
                m_statementResourceUpdate.setInt(3,folder.getOwnerId());
                //GROUP_ID
                m_statementResourceUpdate.setInt(4,folder.getGroupId());
                //ACCESS_FLAGS
                m_statementResourceUpdate.setInt(5,folder.getAccessFlags());
                //STATE
                m_statementResourceUpdate.setInt(6,C_STATE_CHANGED);
                //LOCKED_BY
                m_statementResourceUpdate.setInt(7,folder.isLockedBy());
                //LAUNCHER_TYPE
                m_statementResourceUpdate.setInt(8,folder.getLauncherType());
                //LAUNCHER_CLASSNAME
                m_statementResourceUpdate.setString(9,folder.getLauncherClassname());
                //DATE_LASTMODIFIED
                m_statementResourceUpdate.setLong(10,System.currentTimeMillis());
                //SIZE
                m_statementResourceUpdate.setInt(11,0);
                
                // set query parameters
                m_statementResourceUpdate.setString(12,absoluteName(folder.getAbsolutePath()));
                m_statementResourceUpdate.setInt(13,folder.getProjectId());
                m_statementResourceUpdate.executeUpdate();
                }
               } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
     }

	 /**
	 * Deletes the folder.
	 * 
	 * Only empty folders can be deleted yet.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path of the folder.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * This parameter is not used yet as only empty folders can be deleted!
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteFolder(A_CmsProject project, String foldername, boolean force)
         throws CmsException {
         
         // the current implementation only deletes empty folders
         // check if the folder has any files in it
         Vector files=getFilesInFolder(project,foldername);
         if (files.size()==0) {
             // check if the folder has any folders in it
             Vector folders=getSubFolders(project,foldername);
             if (folders.size()==0) {
                 //this folder is empty, delete it
                 try { 
                 synchronized ( m_statementResourceRemove) {
                   // mark the folder as deleted       
                    m_statementResourceRemove.setInt(1,C_STATE_DELETED);
                    m_statementResourceRemove.setString(2,absoluteName(foldername));
                    m_statementResourceRemove.setInt(3,project.getId());
                    m_statementResourceRemove.executeUpdate();               
                  }
                } catch (SQLException e){
                 throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
                }        
              } else {
                 throw new CmsException(foldername,CmsException.C_NOT_EMPTY);  
              }
         } else {
                 throw new CmsException(foldername,CmsException.C_NOT_EMPTY);  
         }
     }
	
	/**
	 * Returns a Vector with all subfolders.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return Vector with all subfolders for the given folder.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getSubFolders(A_CmsProject project, String foldername)
         throws CmsException {
         Vector folders=new Vector();
         CmsFolder folder=null;
         ResultSet res =null;
         
           try {
            //  get all subfolders
             Statement s = m_Con.createStatement();		
  
             res =s.executeQuery(C_RESOURCE_GETSUBFOLDERS1+absoluteName(foldername)
                                +C_RESOURCE_GETSUBFOLDERS2+project.getId()+
                                 C_RESOURCE_GETSUBFOLDERS3);
            // create new folder objects
		    while ( res.next() ) {
               folder = new CmsFolder(res.getString(C_RESOURCE_NAME),
                                               res.getInt(C_RESOURCE_TYPE),
                                               res.getInt(C_RESOURCE_FLAGS),
                                               res.getInt(C_USER_ID),
                                               res.getInt(C_GROUP_ID),
                                               res.getInt(C_PROJECT_ID_RESOURCES),
                                               res.getInt(C_ACCESS_FLAGS),
                                               res.getInt(C_STATE),
                                               res.getInt(C_LOCKED_BY),
                                               res.getLong(C_DATE_CREATED),
                                               res.getLong(C_DATE_LASTMODIFIED)
                                               );
             
             }

         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);		
         }
         return folders;
     }
	
	/**
	 * Returns a Vector with all file headers of a folder.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all file headers of the folder.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getFilesInFolder(A_CmsProject project, String foldername)
         throws CmsException {
         Vector files=new Vector();
         CmsResource file=null;
         ResultSet res =null;
         
           try {
            //  get all files in folder
             Statement s = m_Con.createStatement();		
  
             res =s.executeQuery(C_RESOURCE_GETFILESINFOLDER1+absoluteName(foldername)
                                +C_RESOURCE_GETFILESINFOLDER2+project.getId()+
                                 C_RESOURCE_GETFILESINFOLDER3);
            // create new file objects
		    while ( res.next() ) {
                     file = new CmsFile(res.getString(C_RESOURCE_NAME),
                                           res.getInt(C_RESOURCE_TYPE),
                                           res.getInt(C_RESOURCE_FLAGS),
                                           res.getInt(C_USER_ID),
                                           res.getInt(C_GROUP_ID),
                                           res.getInt(C_PROJECT_ID_RESOURCES),
                                           res.getInt(C_ACCESS_FLAGS),
                                           res.getInt(C_STATE),
                                           res.getInt(C_LOCKED_BY),
                                           res.getInt(C_LAUNCHER_TYPE),
                                           res.getString(C_LAUNCHER_CLASSNAME),
                                           res.getLong(C_DATE_CREATED),
                                           res.getLong(C_DATE_LASTMODIFIED),
                                           new byte[0],
                                           res.getInt(C_SIZE)
                                           );
                     
             }

         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);		
         }
           return files;
     }

     /**
     * Copies a resource from the online project to a new, specified project.<br>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resourcename The name of the resource.
 	 * @exception CmsException  Throws CmsException if operation was not succesful.
     */
     public void copyResourceToProject(A_CmsProject project,
                                       A_CmsProject onlineProject,
                                       String resourcename) 
         throws CmsException {
         A_CmsResource resource=readResource(onlineProject,resourcename);
         resource.setState(C_STATE_UNCHANGED);
         createResource(project,onlineProject,resource);
     }
     
     /**
     * Publishes a specified project to the online project. <br>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @return Vector of all resource names that are published.
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector publishProject(A_CmsProject project, A_CmsProject onlineProject)
        throws CmsException {
        
        Vector resources = new Vector();
        CmsFile file;
        CmsFolder folder;
        
        ResultSet res;
        
        try {
            // read all files that are in the requested project
            synchronized (m_statementProjectReadFiles) {
                   //get all files from the actual project
                    m_statementProjectReadFiles.setInt(1,project.getId());
                    res=m_statementProjectReadFiles.executeQuery();               
                  }
            // create new file objects
		    while ( res.next() ) {
                     file = new CmsFile(res.getString(C_RESOURCE_NAME),
                                           res.getInt(C_RESOURCE_TYPE),
                                           res.getInt(C_RESOURCE_FLAGS),
                                           res.getInt(C_USER_ID),
                                           res.getInt(C_GROUP_ID),
                                           onlineProject.getId(),
                                           res.getInt(C_ACCESS_FLAGS),
                                           res.getInt(C_STATE),
                                           res.getInt(C_LOCKED_BY),
                                           res.getInt(C_LAUNCHER_TYPE),
                                           res.getString(C_LAUNCHER_CLASSNAME),
                                           res.getLong(C_DATE_CREATED),
                                           res.getLong(C_DATE_LASTMODIFIED),
                                           res.getBytes(C_FILE_CONTENT),
                                           res.getInt(C_SIZE)
                                           );
             // check the state of the file
             // new or changed files are copied to the online project, those files
             // marked as deleted are deleted in the online project.
             if ((file.getState()== C_STATE_CHANGED) || 
                 (file.getState() == C_STATE_NEW)) {
                 // delete an exitsing old file in the online project
                 deleteFile(file);
                 // write the new file
                 createFile(onlineProject,onlineProject,file,file.getAbsolutePath());
                 resources.addElement(file.getAbsolutePath()); 
             } else if (file.getState() == C_STATE_DELETED) {
                  deleteFile(file);
                  resources.addElement(file.getAbsolutePath()); 
             }
             
             }
            // read all folders that are in the requested project
            synchronized (m_statementProjectReadFolders) {
                   //get all folders from the actual project
                    m_statementProjectReadFolders.setInt(1,project.getId());
                    m_statementProjectReadFolders.setInt(2,C_TYPE_FOLDER);
                    res=m_statementProjectReadFolders.executeQuery();               
                  }
            // create new folder objects
		    while ( res.next() ) {
                     folder = new CmsFolder(res.getString(C_RESOURCE_NAME),
                                               res.getInt(C_RESOURCE_TYPE),
                                               res.getInt(C_RESOURCE_FLAGS),
                                               res.getInt(C_USER_ID),
                                               res.getInt(C_GROUP_ID),
                                               onlineProject.getId(),
                                               res.getInt(C_ACCESS_FLAGS),
                                               res.getInt(C_STATE),
                                               res.getInt(C_LOCKED_BY),
                                               res.getLong(C_DATE_CREATED),
                                               res.getLong(C_DATE_LASTMODIFIED)
                                               );
                     // check the state of the folder
                     // Any folder in the offline project is written to the online project
                     // to keep the filesystem structure consistant.
                     // Folders that are marked as DELETED are physically deleted 
                     // in the online project.
                    if ((folder.getState()== C_STATE_CHANGED) || 
                        (folder.getState() == C_STATE_NEW) ||
                        (folder.getState() == C_STATE_UNCHANGED )){
                        // delete an exitsing old folder in the online project
                        deleteFolder(folder);
                        // write the new folder
                        createFolder(onlineProject,folder,folder.getAbsolutePath());
                        resources.addElement(folder.getAbsolutePath()); 
                    } else if (folder.getState() == C_STATE_DELETED) {
                        deleteFolder(folder);
                        resources.addElement(folder.getAbsolutePath()); 
                }
            }
         
            
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);		
         }
           return resources;
    }
     
     /**
	 * Reads a resource from the Cms.<BR/>
	 * A resource is either a file header or a folder.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return The resource read.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 private A_CmsResource readResource(A_CmsProject project, String filename)
         throws CmsException {
                 
         CmsResource file=null;
         ResultSet res =null;
           
         try {  
              synchronized ( m_statementResourceRead) {
                   // read resource data from database
                   m_statementResourceRead.setString(1,absoluteName(filename));
                   m_statementResourceRead.setInt(2,project.getId());
                   res = m_statementResourceRead.executeQuery();
               }
               // create new resource
               if(res.next()) {
                        file = new CmsResource(res.getString(C_RESOURCE_NAME),
                                           res.getInt(C_RESOURCE_TYPE),
                                           res.getInt(C_RESOURCE_FLAGS),
                                           res.getInt(C_USER_ID),
                                           res.getInt(C_GROUP_ID),
                                           res.getInt(C_PROJECT_ID_RESOURCES),
                                           res.getInt(C_ACCESS_FLAGS),
                                           res.getInt(C_STATE),
                                           res.getInt(C_LOCKED_BY),
                                           res.getInt(C_LAUNCHER_TYPE),
                                           res.getString(C_LAUNCHER_CLASSNAME),
                                           res.getLong(C_DATE_CREATED),
                                           res.getLong(C_DATE_LASTMODIFIED),
                                           res.getInt(C_SIZE)
                                           );
               } else {
                 throw new CmsException(filename,CmsException.C_NOT_FOUND);  
               }
 
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
        return file;
       }
    
     
     /**
      * Deletes a file in the database. 
      * This method is used to physically remove a file form the database, therefore it
      * can only be accessed from within the access module.
      * 
      * @param file The file to be deleted.
      * @exception CmsException Throws CmsException if operation was not succesful
      */
     private void deleteFile(CmsFile file) 
        throws CmsException{
            try { 
            // delete the file header
            synchronized ( m_statementResourceDelete) {
                    m_statementResourceDelete.setString(1,absoluteName(file.getAbsolutePath()));
                    m_statementResourceDelete.setInt(2,file.getProjectId());
                    m_statementResourceDelete.executeUpdate();               
                  }
            // delete the file content
            synchronized ( m_statementFileDelete) {
                    m_statementFileDelete.setString(1,absoluteName(file.getAbsolutePath()));
                    m_statementFileDelete.setInt(2,file.getProjectId());
                    m_statementFileDelete.executeUpdate();               
                  }
               } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }         
     }
     
      /**
      * Deletes a folder in the database. 
      * This method is used to physically remove a folder form the database, therefore it
      * can only be accessed from within the access module.
      * 
      * @param folder The folder to be deleted.
      * @exception CmsException Throws CmsException if operation was not succesful
      */
     private void deleteFolder(CmsFolder folder) 
        throws CmsException{
            try { 
            // delete the folder
            synchronized ( m_statementResourceDelete) {
                    m_statementResourceDelete.setString(1,absoluteName(folder.getAbsolutePath()));
                    m_statementResourceDelete.setInt(2,folder.getProjectId());
                    m_statementResourceDelete.executeUpdate();               
                  }
              } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }         
     }
    
     /**
     * This method creates all preparted SQL statements required in this class.
     * 
     * @exception CmsException Throws CmsException if something goes wrong.
     */
     private void initStatements()
       throws CmsException{
         try{
            m_statementResourceWrite=m_Con.prepareStatement(C_RESOURCE_WRITE);
            m_statementResourceRead=m_Con.prepareStatement(C_RESOURCE_READ);
            m_statementResourceReadAll=m_Con.prepareStatement(C_RESOURCE_READ_ALL);
            m_statementFileReadOnline=m_Con.prepareStatement(C_FILE_READ_ONLINE);
            m_statementFileRead=m_Con.prepareStatement(C_FILE_READ);
            m_statementFileWrite=m_Con.prepareStatement(C_FILE_WRITE);
            m_statementResourceRemove=m_Con.prepareStatement(C_RESOURCE_REMOVE);        
            m_statementResourceUpdate=m_Con.prepareStatement(C_RESOURCE_UPDATE);
            m_statementFileUpdate=m_Con.prepareStatement(C_FILE_UPDATE);
            m_statementProjectReadFiles=m_Con.prepareStatement(C_PROJECT_READ_FILES);
            m_statementProjectReadFolders=m_Con.prepareStatement(C_PROJECT_READ_FOLDER);
            m_statementResourceDelete=m_Con.prepareStatement(C_RESOURCE_DELETE);
            m_statementFileDelete=m_Con.prepareStatement(C_FILE_DELETE);
  
           } catch (SQLException e){
           
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
     }
    
     /**
     * Connects to the property database.
     * 
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     */
    private void initConnections(String conUrl)	
      throws CmsException {
      
        try {
        	m_Con = DriverManager.getConnection(conUrl);
       	} catch (SQLException e)	{
         	throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
    }
    
    /**
	 * Calculates the absolute path to a file mounted in this database.
	 * 
	 * @param filename Name of a file in the MhtCms system.
	 * @return absolute path of a the file in the database.
	 */
	private String absoluteName(String filename){
        
               
	   int pos=filename.indexOf(m_mountpoint.getMountpoint());
	   int len=m_mountpoint.getMountpoint().length();
	   String name=null;
       
       // extract the filename after the mountpoint.
	   if (pos != -1) {
		    name="/"+filename.substring(pos+len);
	   } else {
			name=filename;
	   }
 
	   return name;
	}
}
