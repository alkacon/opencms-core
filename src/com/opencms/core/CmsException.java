/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsException.java,v $
 * Date   : $Date: 2000/04/06 12:39:03 $
 * Version: $Revision: 1.24 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.core;

/**
 * This exception is thrown for security reasons in the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.24 $ $Date: 2000/04/06 12:39:03 $
 */
public class CmsException extends Exception {
	
    /**
     * Stores the error code of the CmsException.
     */
    private int m_Type = 0;
    
    private String m_message="NO MESSAGE";
	
    /**
     * Stores a forwared exception.
     */
    private Exception m_Exception = null;
	
    /**
     * Definition of error code for unknown exception.
     */
	public final static int C_UNKNOWN_EXCEPTION = 0;
    
    /**
    * Definition of error code for access denied exception for non file resources.
    */
	public final static int C_NO_ACCESS = 1;
    
    /**
    * Definition of error code for not found exception.
    */
	public final static int C_NOT_FOUND = 2;
    
    /**
    * Definition of error code for bad name exception.
    */
	public final static int C_BAD_NAME = 3;
    
    /**
    * Definition of error code for sql exception.
    */
	public final static int C_SQL_ERROR = 4;
    
    /**
    * Definition of error code for not empty exception.
    */
	public final static int C_NOT_EMPTY = 5;
    
    /**
    * Definition of error code for no admin exception.
    */
	public final static int C_NOT_ADMIN = 6;
    
    /**
    * Definition of error code for serialization exception.
    */
	public final static int C_SERIALIZATION = 7;
    
    /**
    * Definition of error code for no group exception.
    */
    public final static int C_NO_GROUP = 8;
    
    /**
    * Definition of error code for group not empty exception.
    */ 
    public final static int C_GROUP_NOT_EMPTY = 9;
    
    /**
    * Definition of error code for no user exception.
    */
    public final static int C_NO_USER= 10;
    
    /**
    * Definition of error code for no default group exception.
    */
    public final static int C_NO_DEFAULT_GROUP= 11;

    /**
    * Definition of error code for file exists exception.
    */
    public final static int C_FILE_EXISTS= 12;
    
    /**
    * Definition of error code for locked resource.
    */
    public final static int C_LOCKED= 13;

     /**
    * Definition of error code filesystem error.
    */
    public final static int C_FILESYSTEM_ERROR= 14;
    
    /**
    * Definition of error code internal file.
    */
    public final static int C_INTERNAL_FILE= 15;
    
    /**
    * Definition of error code mandatory-property.
    */
    public final static int C_MANDATORY_PROPERTY= 16;

    /**
    * Definition of error code service unavailable.
    */
    public final static int C_SERVICE_UNAVAILABLE= 17;
    
    /**
     * Definition of error code for unknown XML datablocks
     */
    public final static int C_XML_UNKNOWN_DATA = 18;
    
    /**
     * Definition of error code for corrupt internal structure.
     */
    public final static int C_XML_CORRUPT_INTERNAL_STRUCTURE = 19;
    
    /**
     * Definition of error code for wrong XML content type.
     */
    public final static int C_XML_WRONG_CONTENT_TYPE = 20;
    
    /**
     * Definition of error code for XML parsing error.
     */
    public final static int C_XML_PARSING_ERROR = 21;
    
    /**
     * Definition of error code for XML processing error.
     */
    public final static int C_XML_PROCESS_ERROR = 22;
    
    /**
     * Definition of error code for XML user method not found.
     */
    public final static int C_XML_NO_USER_METHOD = 23;

    /**
     * Definition of error code for XML process method not found.
     */
    public final static int C_XML_NO_PROCESS_METHOD = 24;

    /**
     * Definition of error code for missing XML tag.
     */
    public final static int C_XML_TAG_MISSING = 25;
    
    /**
     * Definition of error code for wrong XML template class.
     */
    public final static int C_XML_WRONG_TEMPLATE_CLASS = 26;

    /**
     * Definition of error code for no XML template class.
     */
    public final static int C_XML_NO_TEMPLATE_CLASS = 27;

    /**
     * Definition of error code for launcher errors.
     */
    public final static int C_LAUNCH_ERROR = 28;

    /**
     * Definition of error code for launcher errors.
     */
    public final static int C_CLASSLOADER_ERROR = 29;
    
    /**
     * Definition of error code for error "Password too short".
     */
    public final static int C_SHORT_PASSWORD = 30;
	
	/**
	 * Definition of error code for access denied exception for file resources.
	 */
	public final static int C_ACCESS_DENIED = 31;
	
	public final static String C_EXTXT[] = {
							"Unknown exception ",
							"Access denied ",
							"Not found ",
							"Bad name ",
							"Sql exception ",
							"Folder not empty ",
							"Admin access required ",
							"Serialization/Deserialization failed ",
                            "Unknown User Group ",
                            "Group not empty ",
                            "Unknown User ",
                            "No removal from Default Group ",
                            "Resource already exists",
                            "Locked Resource ",
                            "Filesystem exception ",
                            "Internal use only ",
                            "File-property is mandatory ",
                            "Service unavailable ",
                            "Unknown XML datablock ",
                            "Corrupt internal structure ",
                            "Wrong XML content type ",
                            "XML parsing error ",
                            "Could not process OpenCms special XML tag ",
                            "Could not call user method ",
                            "Could not call process method ",
                            "XML tag missing ",
                            "Wrong XML template class ",
                            "No XML template class ",
                            "Error while launching template class ",
                            "OpenCms class loader error ",
                            "New password is too short ",
							"Access denied to resource ",
};
    
	/** 
	 * Constructs a simple CmsException
	 */
	public CmsException() {
		super();
	}	
	
	 /** 
	 * Constructs a CmsException with a specified description.
	 * 
	 * @param s Exception description 
	 */
	public CmsException(String s) {
		super(s);
        m_message=s;
	}
	
	 /** 
	 * Contructs a CmsException with reserved error code
	 * <p>
	 * 
	 * @param i Exception code
	 */
	public CmsException(int i) {
		super("CmsException ID: " + i);
		m_Type = i;
	}

	 /** 
	 * Constructs a  CmsException with reserved error code and additional information
	 * <p>
	 * 
	 * @param s Exception description
	 * @param i Exception code
	 */
	public CmsException(String s, int i) {
		super(s);
		m_Type = i;
        m_message=s;
	}	
	
	/** 
	 * Construtcs a CmsException  with a detail message and a forwarded other exception
	 * 
	 * @param s Exception description 
	 * @param e Forwaarded general exception
	 */
	public CmsException(String s, Exception e){
		super(s);
		m_Exception = e;
        m_message=s;
	}
	
	 /** 
	 * Creates a CmsException with reserved error code and a forwarded other exception
	 * <p>
	 * 
	 * @param i Exception code
	 * @param e Forawarded general exception
	 */
	public CmsException(int i, Exception e)	{
		super("CmsException ID: " + i);
		m_Type = i;
		m_Exception = e;
	}

	 /** 
	 * Creates a CmsException with reserved error code, a forwarded other exception and a detail message
	 * <p>
	 * 
	 * @param s Exception description 
	 * @param i Exception code
	 * @param e Forawarded general exception
	 */
	public CmsException(String s, int i, Exception e) {
		super(s);
		m_Type = i;
		m_Exception = e;
        m_message=s;
	}		
	
	/**
	 * Get the type of the CmsException.
	 * 
	 * @return Type of CmsException
	 */
	public int getType() {
		return m_Type;
	}
	
	/**
	 * Set an exception value.
	 * 
	 * @param value Exception
	 */
	public void setException(Exception value){
		m_Exception = value;
	}
	
	/**
	 * Get the exeption.
	 * 
	 * @return Exception.
	 */
	public Exception getException()	{
		return m_Exception;
	}
    
     /**
	 * Get the exeption message
	 * 
	 * @return Exception messge.
	 */
	public String getMessage()	{
		return m_message;
	}
    
    
    /**
     * Overwrites the standart toString method.
     */
    public String toString(){
         StringBuffer output=new StringBuffer();
         output.append("[CmsException]: ");
         output.append(m_Type+" ");
         output.append(CmsException.C_EXTXT[m_Type]+"\n");
         output.append("Detailed Error: ");
         output.append(m_message+"\n");
         if (m_Exception != null){
         output.append("Caught Exception: ");
         output.append(m_Exception+"\n");
         }

         return output.toString();
    }
  
}
