package com.opencms.file;

import java.util.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class describes the access to files and folders in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 1999/12/21 18:40:56 $
 */
 class CmsAccessFileMySql extends A_CmsAccessFile implements I_CmsConstants  {

    /**
    * This is the connection object to the database
    */
    private Connection m_Con  = null;
    
     /**
     * SQL Command for writing a new resource. 
     * A resource includes all data of the fileheader.
     */   
    private static final String C_RESOURCE_WRITE = "INSERT INTO RESOURCES VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
     /**
     * SQL Command for writing a new file.
     */   
    private static final String C_FILE_WRITE = "INSERT INTO FILES VALUES(?,?,?)";
     
     /**
     * SQL Command for reading a resource. 
     * A resource includes all data of the fileheader.
     */   
    private static final String C_RESOURCE_READ = "SELECT * FROM RESOURCES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
  
     /**
     * SQL Command for reading a new file.
     */   
    private static final String C_FILE_READ = "SELECT * FROM FILES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
      
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
     * SQL Command for renaming a resource. 
     */   
    private static final String C_RESOURCE_RENAME  ="UPDATE RESOURCES SET "
                                                   +"RESOURCE_NAME = ? , "
                                                   +"DATE_LASTMODIFIED = ? "
                                                   +"WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
    
     /**
     * SQL Command for renaming a file. 
     */   
    private static final String C_FILE_RENAME  ="UPDATE FILES SET "
                                               +"RESOURCE_NAME = ? "
                                               +"WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
                                                                                          
     /**
     * SQL Command for deleteing a resource.
     */   
    private static final String C_RESOURCE_DELETE = "DELETE FROM RESOURCES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";

     /**
     * SQL Command for deleteing a file.
     */   
    private static final String C_FILE_DELETE = "DELETE FROM FILES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
            
    
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
     * Name of the column PROJECT_ID in the SQL tables RESOURCE and FILES.
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
    * Prepared SQL Statement for writing a resource.
    */
    private PreparedStatement m_statementFileWrite;
    
    /**
    * Prepared SQL Statement for reading a resource.
    */
    private PreparedStatement m_statementResourceRead;

    /**
    * Prepared SQL Statement for reading a file.
    */
    private PreparedStatement m_statementFileRead;
    
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
    * Prepared SQL Statement for deleting a file.
    */
    private PreparedStatement m_statementFileDelete;
    
    /**
    * Prepared SQL Statement for renaming a resource.
    */
    private PreparedStatement m_statementResourceRename;
    
    /**
    * Prepared SQL Statement for renameing a file.
    */
    private PreparedStatement m_statementFileRename;


    
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
    public CmsAccessFileMySql(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
        initStatements();
    }
	/**
	 * Creates a new file with the overgiven content and resourcetype.
     *
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * 
	 * @param user The user who wants to create the file.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param contents The contents of the new file.
	 * @param type The resourcetype of the new file.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 CmsFile createFile(A_CmsUser user, A_CmsProject project, 
                        String filename,int flags,
                        byte[] contents, A_CmsResourceType resourceType) 
							
         throws CmsException {
               
           try {   
               synchronized ( m_statementResourceWrite) {
                // write new resource to the database
                //RESOURCE_NAME
                m_statementResourceWrite.setString(1,filename);
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
                m_statementFileWrite.setString(1,filename);
                //PROJECT_ID
                m_statementFileWrite.setInt(2,project.getId());
                //FILE_CONTENT
                m_statementFileWrite.setBytes(3,contents);
                m_statementFileWrite.executeUpdate();
                   
               }
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         return readFile(project,filename);
     }
	
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 *  
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 CmsFile readFile(A_CmsProject project, String filename)
         throws CmsException {
              
         CmsFile file=null;
         ResultSet res =null;
              
         try { 
              // read the file header
             System.err.println("read file header");
              file=(CmsFile)readFileHeader(project,filename);
             System.err.println("done");   
              // file was loaded, so get the content
              if(file != null) {
                   // read the file content form the database
                   synchronized (m_statementFileRead) {
                     m_statementFileRead.setString(1,filename);
                     m_statementFileRead.setInt(2,project.getId());
                     res = m_statementFileRead.executeQuery();  
                   }
                   //put content into file object
                   file.setContents(res.getBytes(C_FILE_CONTENT));
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
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 A_CmsResource readFileHeader(A_CmsProject project, String filename)
         throws CmsException {
                 
         CmsResource file=null;
         ResultSet res =null;
           
         try {  
              synchronized ( m_statementResourceRead) {
                   // read resource data from database
                   m_statementResourceRead.setString(1,filename);
                   m_statementResourceRead.setInt(2,project.getId());
                   res = m_statementResourceRead.executeQuery();
               }
               // create new resource
               if(res.next()) {
                        file = new CmsFile(res.getString(C_RESOURCE_NAME),
                                           res.getInt(C_RESOURCE_TYPE),
                                           res.getInt(C_RESOURCE_FLAGS),
                                           res.getInt(C_USER_ID),
                                           res.getInt(C_GROUP_ID),
                                           res.getInt(C_PROJECT_ID),
                                           res.getInt(C_ACCESS_FLAGS),
                                           res.getInt(C_STATE),
                                           res.getInt(C_LOCKED_BY),
                                           res.getInt(C_LAUNCHER_TYPE),
                                           res.getString(C_LAUNCHER_CLASSNAME),
                                           res.getLong(C_DATE_CREATED),
                                           res.getLong(C_DATE_LASTMODIFIED),
                                           new byte[0]
                                           );
                   }
 
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
        return file;
       }
	
	/**
	 * Writes a file to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
     void writeFile(A_CmsProject project, CmsFile file)
       throws CmsException {
     
           try {   
                //write the file header
                writeFileHeader(project,file);
                //write the file content
                synchronized ( m_statementFileUpdate) {
                //FILE_CONTENT
                m_statementFileUpdate.setBytes(1,file.getContents());
                // set query parameters
                m_statementFileUpdate.setString(2,file.getAbsolutePath());
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
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 void writeFileHeader(A_CmsProject project, CmsFile file)
         throws CmsException {
         
           try {   
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
                m_statementResourceUpdate.setInt(6,file.getState());
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
                m_statementResourceUpdate.setString(12,file.getAbsolutePath());
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
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */		
	 void renameFile(A_CmsProject project, String oldname, String newname)
         throws CmsException {
          try { 
             // delete the resource
              synchronized (m_statementResourceRename) {
                m_statementResourceRename.setString(1,newname);
                m_statementResourceRename.setLong(2,System.currentTimeMillis());
                m_statementResourceRename.setString(3,oldname);
                m_statementResourceRename.setInt(4,project.getId());
                m_statementResourceRename.executeQuery();  
              }
             // delete the file content
             synchronized (m_statementFileRename) {
                m_statementFileRename.setString(1,newname);
                m_statementFileRename.setString(2,oldname);
                m_statementFileRename.setInt(3,project.getId());
                m_statementFileRename.executeQuery();  
              }
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
     }
	
	/**
	 * Deletes the file.
	 * 
     * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 void deleteFile(A_CmsProject project, String filename)
         throws CmsException {
         try { 
             // delete the resource
              synchronized (m_statementResourceDelete) {
                m_statementResourceDelete.setString(1,filename);
                m_statementResourceDelete.setInt(2,project.getId());
                m_statementResourceDelete.executeQuery();  
              }
             // delete the file content
             synchronized (m_statementFileDelete) {
                m_statementFileDelete.setString(1,filename);
                m_statementFileDelete.setInt(2,project.getId());
                m_statementFileDelete.executeQuery();  
              }
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
     }
	
		
	/**
	 * Copies the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 void copyFile(A_CmsProject project, String source, String destination)
         throws CmsException {
         CmsFile file;
         
         // read sourcefile
         file=readFile(project,source);
         // create the new file
           try {   
               synchronized (m_statementResourceWrite) {
                // write new resource to the database
                //RESOURCE_NAME
                m_statementResourceWrite.setString(1,destination);
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
                m_statementResourceWrite.setInt(9,C_UNKNOWN_ID);
                //LAUNCHER_TYPE
                m_statementResourceWrite.setInt(10,file.getLauncherType());
                //LAUNCHER_CLASSNAME
                m_statementResourceWrite.setString(11,file.getLauncherClassname());
                //DATE_CREATED
                m_statementResourceWrite.setLong(12,System.currentTimeMillis());
                //DATE_LASTMODIFIED
                m_statementResourceWrite.setLong(13,System.currentTimeMillis());
                //SIZE
                m_statementResourceWrite.setInt(14,file.getContents().length);
                m_statementResourceWrite.executeUpdate();
               }
               synchronized (m_statementFileWrite) {
                //RESOURCE_NAME
                m_statementFileWrite.setString(1,destination);
                //PROJECT_ID
                m_statementFileWrite.setInt(2,project.getId());
                //FILE_CONTENT
                m_statementFileWrite.setBytes(3,file.getContents());
                m_statementFileWrite.executeUpdate();
               }
               
             } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
     }
	
	/**
	 * Moves the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 void moveFile(A_CmsProject project, String source,  String destination)
         throws CmsException {
         
         renameFile(project,source,destination);
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
	 CmsFolder createFolder(A_CmsUser user,
                            A_CmsProject project, String foldername,
                            int flags)
         throws CmsException {
           try {   
               synchronized ( m_statementResourceWrite) {
                // write new resource to the database
                //RESOURCE_NAME
                m_statementResourceWrite.setString(1,foldername);
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
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The name of the folder to be read.
	 * 
	 * @return folder The read folder.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 CmsFolder readFolder(A_CmsProject project, String foldername)
         throws CmsException {
         
         CmsFolder folder=null;
         ResultSet res =null;
           
         try {  
              synchronized ( m_statementResourceRead) {
                   // read resource data from database
                   m_statementResourceRead.setString(1,foldername);
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
                                               res.getInt(C_PROJECT_ID),
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
        return folder;
        
    }
	
	/**
	 * Renames the folder to the new name.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * renamed, too.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (, No path information allowed).
	 * @param force If force is set to true, all sub-resources will be renamed.
	 * If force is set to false, the folder will be renamed only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	 void renameFolder(String project, String oldname, 
							 String newname, boolean force)
         throws CmsException {
     }
	
	/**
	 * Deletes the folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * delted, too.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path of the folder.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	 void deleteFolder(String project, String foldername, boolean force)
         throws CmsException {
     }
	
	/**
	 * Copies a folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * copied, too.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path of the destinationfolder.
	 * @param force If force is set to true, all sub-resources will be copied.
	 * If force is set to false, the folder will be copied only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be copied. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination foldername.
	 */	
	 void copyFolder(String project, String source, String destination, 
						    boolean force)
		throws CmsException, CmsDuplicateKeyException 
     {
     }
	
	/**
	 * Moves a folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * moved, too.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * @param force If force is set to true, all sub-resources will be moved.
	 * If force is set to false, the folder will be moved only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be moved. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination filename.
	 */	
	 void moveFolder(String project, String source, 
						   String destination, boolean force)
         throws CmsException, CmsDuplicateKeyException {
     }

	/**
	 * Returns a abstract Vector with all subfolders.<BR/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A abstract Vector with all subfolders for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	 Vector getSubFolders(String project, String foldername)
         throws CmsException {
                 return null;
     }
	
	/**
	 * Returns a abstract Vector with all subfiles.<BR/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A abstract Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	 Vector getFilesInFolder(String project, String foldername)
         throws CmsException {
                 return null;
     }
	
	/**
	 * Changes the flags for this resource<BR/>
	 * 
	 * The user may change the flags, if he is admin of the resource.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param flags The new flags for the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	 void chmod(String project, String filename, int flags)
         throws CmsException {
     }
     
	/**
	 * Changes the owner for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param newOwner The name of the new owner for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newOwner doesn't exists.
	 */
	 void chown(String project, String filename, String newOwner)
         throws CmsException {
     }

	/**
	 * Changes the group for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param newGroup The new of the new group for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newGroup doesn't exists.
	 */
	 void chgrp(String project, String filename, String newGroup)
         throws CmsException {
     }

	/**
	 * Locks a resource<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path to the resource to lock.
	 * @param force If force is true, a existing locking will be oberwritten.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if there is a existing lock
	 * and force was set to false.
	 */
	 void lockFile(String project, String resource, boolean force)
         throws CmsException {
     }
	
	/**
	 * Tests, if a resource was locked<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path to the resource.
	 * 
	 * @return true, if the resource is locked else it returns false.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	 boolean isLocked(String project, String resource)
         throws CmsException {
         return true;
     }
	
	/**
	 * Returns the user, who had locked the resource.<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path to the resource.
	 * 
	 * @return true, if the resource is locked else it returns false.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	 A_CmsUser lockedBy(String project, String resource)
         throws CmsException {
                 return null;
     }

	/**
	 * Returns a MetaInformation of a file or folder.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be read.
	 * @param meta The metadefinition-name of which the MetaInformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 String readMetaInformation(String project, String name, String meta)
         throws CmsException {
                 return null;
     }

	/**
	 * Writes a couple of MetaInformation for a file or folder.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be set.
	 * @param metainfos A Hashtable with metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 void writeMetaInformations(String project, String name, 
									  Hashtable metainfos)
         throws CmsException {
     }

	/**
	 * Returns a list of all MetaInformations of a file or folder.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be read
	 * 
	 * @return abstract Vector of MetaInformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 Vector readAllMetaInformations(String project, String name)
         throws CmsException {
                 return null;
     }
	
	/**
	 * Deletes all MetaInformation for a file or folder.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resourcename The resource-name of which the MetaInformation has to be delteted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 void deleteAllMetaInformations(String project, String resourcename)
         throws CmsException {
     }

	/**
	 * Deletes a MetaInformation for a file or folder.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resourcename The resource-name of which the MetaInformation has to be delteted.
	 * @param meta The metadefinition-name of which the MetaInformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 void deleteMetaInformation(String project, String resourcename, 
									  String meta)
         throws CmsException {
     }

	/**
	 * Declines a resource. The resource can be copied to the onlineproject.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The name of the project.
	 * @param resource The full path to the resource, which will be declined.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 void declineResource(String project, String resource)
         throws CmsException {
     }

	/**
	 * Rejects a resource. The resource will be copied to the following project,
	 * at publishing time.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The name of the project.
	 * @param resource The full path to the resource, which will be declined.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 void rejectResource(String project, String resource)
         throws CmsException {
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
            m_statementFileWrite=m_Con.prepareStatement(C_FILE_WRITE);
            m_statementResourceRead=m_Con.prepareStatement(C_RESOURCE_READ);
            m_statementFileRead=m_Con.prepareStatement(C_FILE_READ);
            m_statementResourceUpdate=m_Con.prepareStatement(C_RESOURCE_UPDATE);
            m_statementFileUpdate=m_Con.prepareStatement(C_FILE_UPDATE);
            m_statementResourceDelete=m_Con.prepareStatement(C_RESOURCE_DELETE);
            m_statementFileDelete=m_Con.prepareStatement(C_FILE_DELETE);
            m_statementResourceRename=m_Con.prepareStatement(C_RESOURCE_RENAME);
            m_statementFileRename=m_Con.prepareStatement(C_FILE_RENAME);
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
}
