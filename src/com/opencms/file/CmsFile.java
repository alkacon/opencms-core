package com.opencms.file;

import java.io.*;

/**
 * This class describes a file in the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 1999/12/17 14:37:04 $
 */
public class CmsFile extends CmsResource { 	

    /**
     * The content of the file.
     */
    private byte[] m_fileContent;
    
    /**
     * The size of the file content.
     */
    private int m_size;
    
    
     /**
      * Constructor, creates a new CmsFile object.
      * 
      * @param resourceName The name (including complete path) of the resouce.
      * @param resourceType The type of this resource.
      * @param rescourceFlags The flags of thei resource.
      * @param userId The id of the user of this resource.
      * @param groupId The id of the group of this resource.
      * @param projectId The project id this resource belongs to.
      * @param accessFlags The access flags of this resource.
      * @param state The state of this resource. 
      * @param lockedBy The user id of the user who has locked this resource.
      * @param dateCreated The creation date of this resource.
      * @param dateLastModified The date of the last modification of the resource.
      * @param fileContent Then content of the file.
      */
     public CmsFile(String resourceName, int resourceType, int resourceFlags,
                        int userId, int groupId, int projectId,
                        int accessFlags, int state, int lockedBy,
                        long dateCreated, long dateLastModified,
                        byte[] fileContent){
        
        // create the CmsResource.
        super(resourceName,resourceType,resourceFlags,
              userId,groupId,projectId,
              accessFlags,state,lockedBy,
              dateCreated,dateLastModified);
        
        // set content and size.
        m_fileContent=fileContent;
        m_size=m_fileContent.length;
            
   }
    
	/**
	 * Sets the content of this file.
	 * 
	 * @param value the content of this file.
	 */
    public void setContents(byte[] value) {
        m_fileContent=value;
        m_size=m_fileContent.length;    
    }
        	
	/**
	 * Gets the content of this file.
	 * 
	 * @return the content of this file.
	 */
    public byte[] getContents() {
      return m_fileContent;
    }
	
	/**
	 * Gets the length of the content (filesize).
	 * 
	 * @return the length of the content.
	 */
    public int getLength() {
        return m_size;
    }
	
	/**
	 * Gets the file-extension.
	 * 
	 * @return the file extension. If this file has no extension, it returns 
	 * a empty string ("").
	 */
    public String getExtension(){
        String name=null;
        String extension="";
        int dot;
        
        name=this.getName();
        // check if this file has an extension.
        dot=name.lastIndexOf(".");
        if (dot> 0) {
            extension=name.substring(dot,name.length());
        }
        return extension;          
    }
        
    

}
