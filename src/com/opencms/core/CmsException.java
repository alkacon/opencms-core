/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsException.java,v $
* Date   : $Date: 2003/05/21 14:35:55 $
* Version: $Revision: 1.49 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import java.io.*;
import java.util.*;

/**
 * <p>This class provides OpenCms interal Exception handling.
 * A CmsExeption thrown will result in a default OpenCms error dialog box.</p> 
 * <p>Note: Most Exceptions thrown in templates and other classes will be converted
 * to a CmsException by the template launcher.</p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.49 $ $Date: 2003/05/21 14:35:55 $
 */
public class CmsException extends Exception {

    /** Stores the error code of the CmsException */
    protected int m_type = 0;
    
    /** A string message describing the CmsEception */
    protected String m_message = "NO MESSAGE";

    /** Stores a forwared exception */
    protected Throwable m_rootCause = null;
    
    /** Flag to set processing of a saved forwared root exception */
    protected boolean m_useRootCause = false;
    
    /** Definition of error code for unknown exception */
    public final static int C_UNKNOWN_EXCEPTION = 0;

    /** Definition of error code for access denied exception for non file resources */
    public final static int C_NO_ACCESS = 1;

    /** Definition of error code for not found exception */
    public final static int C_NOT_FOUND = 2;

    /** Definition of error code for bad name exception */
    public final static int C_BAD_NAME = 3;

    /** Definition of error code for sql exception */
    public final static int C_SQL_ERROR = 4;

    /** Definition of error code for not empty exception */
    public final static int C_NOT_EMPTY = 5;

    /** Definition of error code for no admin exception */
    public final static int C_NOT_ADMIN = 6;

    /** Definition of error code for serialization exception */
    public final static int C_SERIALIZATION = 7;

    /** Definition of error code for no group exception */
    public final static int C_NO_GROUP = 8;

    /** Definition of error code for group not empty exception */
    public final static int C_GROUP_NOT_EMPTY = 9;

    /** Definition of error code for no user exception */
    public final static int C_NO_USER = 10;

    /** Definition of error code for no default group exception */
    public final static int C_NO_DEFAULT_GROUP = 11;

    /** Definition of error code for file exists exception */
    public final static int C_FILE_EXISTS = 12;

    /** Definition of error code for locked resource */
    public final static int C_LOCKED = 13;

    /** Definition of error code filesystem error */
    public final static int C_FILESYSTEM_ERROR = 14;

    /** Definition of error code internal file */
    public final static int C_INTERNAL_FILE = 15;

    /** Definition of error code service unavailable */
    public final static int C_SERVICE_UNAVAILABLE = 17;

    /** Definition of error code for unknown XML datablocks */
    public final static int C_XML_UNKNOWN_DATA = 18;

    /** Definition of error code for corrupt internal structure */
    public final static int C_XML_CORRUPT_INTERNAL_STRUCTURE = 19;

    /** Definition of error code for wrong XML content type */
    public final static int C_XML_WRONG_CONTENT_TYPE = 20;

    /** Definition of error code for XML parsing error */
    public final static int C_XML_PARSING_ERROR = 21;

    /** Definition of error code for XML processing error */
    public final static int C_XML_PROCESS_ERROR = 22;

    /** Definition of error code for XML user method not found */
    public final static int C_XML_NO_USER_METHOD = 23;

    /** Definition of error code for XML process method not found */
    public final static int C_XML_NO_PROCESS_METHOD = 24;

    /** Definition of error code for missing XML tag */
    public final static int C_XML_TAG_MISSING = 25;

    /** Definition of error code for wrong XML template class */
    public final static int C_XML_WRONG_TEMPLATE_CLASS = 26;

    /** Definition of error code for no XML template class */
    public final static int C_XML_NO_TEMPLATE_CLASS = 27;

    /** Definition of error code for launcher errors */
    public final static int C_LAUNCH_ERROR = 28;

    /** Definition of error code for launcher errors */
    public final static int C_CLASSLOADER_ERROR = 29;

    /** Definition of error code for error"Password too short" */
    public final static int C_SHORT_PASSWORD = 30;

    /** 
     * Definition of error code for error"Password not valid".
     * For comptibility reasons the same like for short password.
     */
    public final static int C_INVALID_PASSWORD = C_SHORT_PASSWORD;

    /**
     * Definition of error code for access denied exception for file resources.
     * This exception causes a login-screen.
     */
    public final static int C_ACCESS_DENIED = 31;

    /** Definition of error code for accessing a deleted resource */
    public final static int C_RESOURCE_DELETED = 32;

    /** Definition of error code for RB-INIT-ERRORS */
    public final static int C_RB_INIT_ERROR = 33;

    /** Definition of error code for Registry exception */
    public final static int C_REGISTRY_ERROR = 34;

    /** Definition of error code for user exists */
    public final static int C_USER_EXISTS = 35;
    
    /** Definition of error code for HTTP sreaming error */
    public final static int C_STREAMING_ERROR = 36;

    /** Definition of error code for HTTP sreaming error */
    public final static int C_HTTPS_PAGE_ERROR = 37;

    /** Definition of error code for HTTP sreaming error */
    public final static int C_HTTPS_REQUEST_ERROR = 38;

    /** Error code for Flex cache */
    public final static int C_FLEX_CACHE = 39;
    
    /** Error code for Flex loader */
    public final static int C_FLEX_LOADER = 40;

    /** Unspecified Flex error code */
    public final static int C_FLEX_OTHER = 41;

    /** Default prefix for a CmsException message */
    public static final String C_CMS_EXCEPTION_PREFIX = "com.opencms.core.CmsException";

    /**
     * This array provides descriptions for the error codes stored as
     * constants in the CmsExeption class.
     */
    public final static String C_EXTXT[] =  {
        "Unknown exception", 
        "Access denied", 
        "Not found",
        "Bad name", 
        "Sql exception", 
        "Folder not empty", 
        "Admin access required",
        "Serialization/Deserialization failed", 
        "Unknown User Group",
        "Group not empty", 
        "Unknown User", 
        "No removal from Default Group",
        "Resource already exists", 
        "Locked Resource", 
        "Filesystem exception",
        "Internal use only", 
        "Deprecated exception: File-property is mandatory",
        "Service unavailable", 
        "Unknown XML datablock", 
        "Corrupt internal structure",
        "Wrong XML content type", 
        "XML parsing error", 
        "Could not process OpenCms special XML tag",
        "Could not call user method", 
        "Could not call process method",
        "XML tag missing", 
        "Wrong XML template class", 
        "No XML template class",
        "Error while launching template class", 
        "OpenCms class loader error",
        "New password is too short", 
        "Access denied to resource",
        "Resource deleted", 
        "DriverManager init error", 
        "Registry error",
        "User already exists", 
        "HTTP streaming error",
        "Wrong scheme for http resource", 
        "Wrong scheme for https resource",
        "Error in Flex cache", 
        "Error in Flex loader", 
        "Error in Flex engine"
    };

    /**
     * Constructs a simple CmsException
     */
    public CmsException() {
        this("", 0, null, false);
    }

    /**
     * Contructs a CmsException with the provided error code, 
     * The error codes used should be the constants from the CmsEception class.
     *
     * @param i Exception error code
     */
    public CmsException(int type) {
        this("CmsException ID: " + type, type, null, false);
    }

    /**
     * Contructs a CmsException with the provided error code and
     * a given root cause.
     * The error codes used should be the constants from the CmsEception class.
     *
     * @param i Exception code
     * @param e Forwarded root cause exception
     */
    public CmsException(int type, Throwable rootCause) {
        this("CmsException ID: " + type, type, rootCause, false);
    }

    /**
     * Constructs a CmsException with the provided description.
     *
     * @param s Exception message
     */
    public CmsException(String message) {
        this(message, 0, null, false);
    }

    /**
     * Contructs a CmsException with the provided description and error code.
     * 
     * @param s Exception message
     * @param i Exception code
     */
    public CmsException(String message, int type) {
        this(message, type, null, false);
    }

    /**
     * Construtcs a CmsException with a detail message and a forwarded 
     * root cause exception
     *
     * @param s Exception message
     * @param e Forwarded root cause exception
     */
    public CmsException(String message, Throwable rootCause) {
        this(message, 0, rootCause, false);
    }

    /**
     * Creates a CmsException with the provided error code, 
     * a forwarded root cause exception and a detail message.
     *
     * @param s Exception message
     * @param i Exception code
     * @param e Forwarded root cause exception
     */
    public CmsException(String message, int type, Throwable rootCause) {
        this(message, type, rootCause, false);
    }
    
    /**
     * Creates a CmsException with a provided error code, 
     * a forwarded root cause exception and a detail message.
     * The further processing of the exception can be controlled 
     * with the <code>useRoot</code> parameter.
     *
     * @param s Exception message
     * @param i Exception code
     * @param e Forwarded root cause exception
     * @param useRoot If true, use 
     */    
    public CmsException(String message, int type, Throwable rootCause, boolean useRoot) {
        super(C_CMS_EXCEPTION_PREFIX + ": " + message);
        this.m_message = message;
        this.m_type = type;
        this.m_rootCause = rootCause;
        this.m_useRootCause = useRoot;
    }
        
    /**
     * Get the root cause Exception which was provided
     * when this exception was thrown.
     *
     * @return The root cause Exception.
     */
    public Exception getException() {        
        if (m_useRootCause) return null;
        try {
            return (Exception)getRootCause();
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Get the root cause Throwable which was provided
     * when this exception was thrown.
     *
     * @return The root cause Throwable.
     */
    public Throwable getRootCause() {
        return m_rootCause;
    }

    /**
     * Set the root cause Exception value.
     *
     * @param value The root cause Exception
     */
    public void setException(Throwable value) {
        m_rootCause = value;
    }
        
    /**
     * Get the throwable.
     *
     * @return Exception.
     */
    public Throwable getThrowable() {
        return m_rootCause;
    }    

    /**
     * Get the exeption message
     *
     * @return Exception messge.
     */
    public String getMessage() {
        return C_CMS_EXCEPTION_PREFIX + ": " + m_message;
    }

    /**
     * Get the exeption message
     *
     * @return Exception messge.
     */
    public String getShortException() {
        return C_CMS_EXCEPTION_PREFIX + ": " + getType() + " " + C_EXTXT[getType()]
                + ". Detailed Error: " + m_message + ".";
    }

    /**
	 * Return a string with the stacktrace. for this exception
	 * and for all encapsulated exceptions.
	 * Creation date: (10/23/00 %r)
	 * @return java.lang.String
	 */
	public String getStackTraceAsString() {
	    java.io.StringWriter sw = new java.io.StringWriter();
	    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
		
	    if (m_useRootCause && (m_rootCause != null)) {
	        // use stack trace of root cause
	        m_rootCause.printStackTrace(pw);
	    } else {            
	        // use stack trace of this eception and add the root case 
	        super.printStackTrace(pw);
	
	        // if there are any encapsulated exceptions, write them also.
	        if(m_rootCause != null) {
	            StringWriter _sw = new StringWriter();
	            PrintWriter _pw = new PrintWriter(_sw);
                _pw.println("-----------");                
                _pw.println("Root cause:");                
	            m_rootCause.printStackTrace(_pw);
	            _pw.close();
	            try {
	                _sw.close();
	            }
	            catch(Exception exc) {
	    
	            // ignore the exception
	            }
	            StringTokenizer st = new StringTokenizer(_sw.toString(), "\n");
	            while(st.hasMoreElements()) {
                    String s = ">" + (String)st.nextElement();
                    while ( (s != null) && (! "".equals(s)) && ((s.endsWith("\r") || s.endsWith("\n") || s.endsWith(">"))) ) {
                        s = s.substring(0, s.length()-1);
                    } 
                    if ((s != null) && (! "".equals(s))) pw.println(s);
	            }
	        }
	    }
	    pw.close();
	    try {
	        sw.close();
	    } catch(Exception exc) {
	        // ignore the exception
	    }
	    return sw.toString();
	}

    /**
     * Get the type of the CmsException.
     *
     * @return Type of CmsException
     */
    public int getType() {
        return m_type;
    }

    /**
     * Gets the exception type as text.
     *
     * @return Exception type in a text-version.
     */
    public String getTypeText() {
        return C_CMS_EXCEPTION_PREFIX + ": " + getType() + " " + C_EXTXT[getType()];
    }

    /**
     * Print the exception stack trace to System.out.
     */
    public void printStackTrace() {
        printStackTrace(System.out);
    }

    /**
     * Prints this <code>Throwable</code> and its backtrace to the
     * specified print stream.
     */
    public void printStackTrace(java.io.PrintStream s) {
        s.println(getStackTraceAsString());
    }

    /**
     * Prints this <code>Throwable</code> and its backtrace to the specified
     * print writer.
     */
    public void printStackTrace(java.io.PrintWriter s) {
        s.println(getStackTraceAsString());
    }
    
    /**
     * Overwrites the standart toString method.
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append(C_CMS_EXCEPTION_PREFIX + ": ");
        output.append(m_type + " ");
        output.append(CmsException.C_EXTXT[m_type] + ". ");
        if (m_message != null && (!"".equals(m_message))) {
            output.append("Detailed error: ");
            output.append(m_message + ". ");
        }
        if(m_rootCause != null) {
            output.append("\nroot cause was ");
            output.append(m_rootCause);
        }
        return output.toString();
    }
}
