/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsException.java,v $
 * Date   : $Date: 2003/08/03 15:11:59 $
 * Version: $Revision: 1.52 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.core;

import java.io.*;
import java.util.*;

/**
 * Master exception type for all exceptions caused in OpenCms.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.52 $
 */
public class CmsException extends Exception {

    /** Error code for bad name exception */
    public static final int C_BAD_NAME = 3;

    /** Error code for launcher errors */
    public static final int C_CLASSLOADER_ERROR = 29;

    /** Default prefix for a CmsException message */
    public static final String C_CMS_EXCEPTION_PREFIX = "com.opencms.core.CmsException";

    /**
     * This array provides descriptions for the error codes stored as
     * constants in the CmsExeption class.
     */
    public static final String[] C_ERROR_DESCRIPTION =
        {
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
            "(code 13: unused)",
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
            "(code 31: unused)",
            "Resource deleted",
            "DriverManager init error",
            "Registry error",
            "(code 35: unused)",
            "(code 36: unused)",
            "Wrong scheme for http resource",
            "Wrong scheme for https resource",
            "Error in Flex cache",
            "Error in Flex loader" };

    /** Error code for file exists exception */
    public static final int C_FILE_EXISTS = 12;

    /** Error code filesystem error */
    public static final int C_FILESYSTEM_ERROR = 14;

    /** Error code for Flex cache */
    public static final int C_FLEX_CACHE = 39;

    /** Error code for Flex loader */
    public static final int C_FLEX_LOADER = 40;

    /** Error code for group not empty exception */
    public static final int C_GROUP_NOT_EMPTY = 9;

    /** Error code for HTTP sreaming error */
    public static final int C_HTTPS_PAGE_ERROR = 37;

    /** Error code for HTTP sreaming error */
    public static final int C_HTTPS_REQUEST_ERROR = 38;

    /** Error code internal file */
    public static final int C_INTERNAL_FILE = 15;

    /** Error code for error"Password too short" */
    public static final int C_INVALID_PASSWORD = 30;

    /** Error code for launcher errors */
    public static final int C_LAUNCH_ERROR = 28;

    /** 
     * Error code for access denied exception for vfs resources
     * @deprecated use a {@link org.opencms.security.CmsSecurityException} instead
     */    
    public static final int C_NO_ACCESS = 1;

    /** Error code for no default group exception */
    public static final int C_NO_DEFAULT_GROUP = 11;

    /** Error code for no group exception */
    public static final int C_NO_GROUP = 8;

    /** Error code for no user exception */
    public static final int C_NO_USER = 10;

    /** Error code for no admin exception */
    public static final int C_NOT_ADMIN = 6;

    /** Error code for not empty exception */
    public static final int C_NOT_EMPTY = 5;

    /** Error code for not found exception */
    public static final int C_NOT_FOUND = 2;

    /** Error code for RB-INIT-ERRORS */
    public static final int C_RB_INIT_ERROR = 33;

    /** Error code for Registry exception */
    public static final int C_REGISTRY_ERROR = 34;

    /** Error code for accessing a deleted resource */
    public static final int C_RESOURCE_DELETED = 32;

    /** Error code for serialization exception */
    public static final int C_SERIALIZATION = 7;

    /** Error code service unavailable */
    public static final int C_SERVICE_UNAVAILABLE = 17;

    /** Error code for sql exception */
    public static final int C_SQL_ERROR = 4;

    /** Error code for unknown exception */
    public static final int C_UNKNOWN_EXCEPTION = 0;

    /** Error code for corrupt internal structure */
    public static final int C_XML_CORRUPT_INTERNAL_STRUCTURE = 19;

    /** Error code for XML process method not found */
    public static final int C_XML_NO_PROCESS_METHOD = 24;

    /** Error code for no XML template class */
    public static final int C_XML_NO_TEMPLATE_CLASS = 27;

    /** Error code for XML user method not found */
    public static final int C_XML_NO_USER_METHOD = 23;

    /** Error code for XML parsing error */
    public static final int C_XML_PARSING_ERROR = 21;

    /** Error code for XML processing error */
    public static final int C_XML_PROCESS_ERROR = 22;

    /** Error code for missing XML tag */
    public static final int C_XML_TAG_MISSING = 25;

    /** Error code for unknown XML datablocks */
    public static final int C_XML_UNKNOWN_DATA = 18;

    /** Error code for wrong XML content type */
    public static final int C_XML_WRONG_CONTENT_TYPE = 20;

    /** Error code for wrong XML template class */
    public static final int C_XML_WRONG_TEMPLATE_CLASS = 26;

    /** A string message describing the CmsEception */
    protected String m_message = "NO MESSAGE";

    /** Stores a forwared exception */
    protected Throwable m_rootCause = null;

    /** Stores the error code of the CmsException */
    protected int m_type = 0;

    /** Flag to set processing of a saved forwared root exception */
    protected boolean m_useRootCause = false;

    /**
     * Constructs a simple CmsException
     */
    public CmsException() {
        this("", 0, null, false);
    }

    /**
     * Contructs a CmsException with the provided error code, 
     * the error codes used should be the constants from the CmsEception class or subclass.<p>
     *
     * @param type exception error code
     */
    public CmsException(int type) {
        this("CmsException ID: " + type, type, null, false);
    }

    /**
     * Contructs a CmsException with the provided error code and
     * a given root cause.<p>
     * 
     * The error codes used should be the constants from the CmsEception class or subclass.<p>
     *
     * @param type exception error code
     * @param rootCause root cause exception
     */
    public CmsException(int type, Throwable rootCause) {
        this("CmsException ID: " + type, type, rootCause, false);
    }

    /**
     * Constructs a CmsException with the provided description message.<p>
     *
     * @param message the description message
     */
    public CmsException(String message) {
        this(message, 0, null, false);
    }

    /**
     * Contructs a CmsException with the provided description message and error code.<p>
     * 
     * @param message the description message
     * @param type exception error code
     */
    public CmsException(String message, int type) {
        this(message, type, null, false);
    }

    /**
     * Construtcs a CmsException with the provided description message, error code and 
     * a given root cause.<p>
     *
     * @param message the description message
     * @param type exception error code
     * @param rootCause root cause exception
     */
    public CmsException(String message, int type, Throwable rootCause) {
        this(message, type, rootCause, false);
    }

    /**
     * Construtcs a CmsException with the provided description message, error code and 
     * a given root cause, 
     * the further processing of the exception can be controlled 
     * with the <code>useRoot</code> parameter.
     *
     * @param message the description message
     * @param type exception error code
     * @param rootCause root cause exception
     * @param useRoot if true, use root case for exception display  
     */
    public CmsException(String message, int type, Throwable rootCause, boolean useRoot) {
        super(C_CMS_EXCEPTION_PREFIX + ": " + message);
        this.m_message = message;
        this.m_type = type;
        this.m_rootCause = rootCause;
        this.m_useRootCause = useRoot;
    }

    /**
     * Construtcs a CmsException with the provided description message and 
     * a given root cause.<p>
     *
     * @param message the description message
     * @param rootCause root cause exception
     */
    public CmsException(String message, Throwable rootCause) {
        this(message, 0, rootCause, false);
    }

    /**
     * Returns the description String for the provided CmsException type, subclasses of 
     * CmsException should overwrite this method for the types they define.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */
    protected String getErrorDescription(int type) {
        if (CmsException.C_ERROR_DESCRIPTION.length >= type) {
            return CmsException.C_ERROR_DESCRIPTION[type];
        } else {
            return this.getClass().getName();
        }
    }

    /**
     * Get the root cause Exception which was provided
     * when this exception was thrown.<p>
     *
     * @return the root cause Exception
     */
    public Exception getException() {
        if (m_useRootCause)
            return null;
        try {
            return (Exception)getRootCause();
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Returns the exception description message<p>
     *
     * @return the exception description message
     */
    public String getMessage() {
        return C_CMS_EXCEPTION_PREFIX + ": " + m_message;
    }

    /**
     * Get the root cause Throwable which was provided
     * when this exception was thrown.<p>
     *
     * @return the root cause Throwable
     */
    public Throwable getRootCause() {
        return m_rootCause;
    }

    /**
     * Returns a short String describing this exception.<p>
     *
     * @return a short String describing this exception
     */
    public String getShortException() {
        return C_CMS_EXCEPTION_PREFIX + ": " + getType() + " " + getErrorDescription(getType()) + ". Detailed Error: " + m_message + ".";
    }

    /**
     * Return a string with the stacktrace for this exception
     * and for all encapsulated exceptions.<p>
     *
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
            if (m_rootCause != null) {
                StringWriter _sw = new StringWriter();
                PrintWriter _pw = new PrintWriter(_sw);
                _pw.println("-----------");
                _pw.println("Root cause:");
                m_rootCause.printStackTrace(_pw);
                _pw.close();
                try {
                    _sw.close();
                } catch (Exception exc) {

                    // ignore the exception
                }
                StringTokenizer st = new StringTokenizer(_sw.toString(), "\n");
                while (st.hasMoreElements()) {
                    String s = ">" + (String)st.nextElement();
                    while ((s != null) && (!"".equals(s)) && ((s.endsWith("\r") || s.endsWith("\n") || s.endsWith(">")))) {
                        s = s.substring(0, s.length() - 1);
                    }
                    if ((s != null) && (!"".equals(s)))
                        pw.println(s);
                }
            }
        }
        pw.close();
        try {
            sw.close();
        } catch (Exception exc) {
            // ignore the exception
        }
        return sw.toString();
    }

    /**
     * Returns the type of the CmsException.<p>
     *
     * @return the type of the CmsException
     */
    public int getType() {
        return m_type;
    }

    /**
     * Returns the exception type as text.
     *
     * @return the exception type as text
     */
    public String getTypeText() {
        return C_CMS_EXCEPTION_PREFIX + ": " + getType() + " " + getErrorDescription(getType());
    }

    /**
     * Prints the exception stack trace to System.out.<p>
     */
    public void printStackTrace() {
        printStackTrace(System.out);
    }

    /**
     * Prints this CmsException and it's stack trace to the
     * specified print stream.<p>
     * 
     * @param s the stream to print to
     */
    public void printStackTrace(java.io.PrintStream s) {
        s.println(getStackTraceAsString());
    }

    /**
     * Prints this CmsException and it's backtrace to the specified
     * print writer.<p>
     * 
     * @param s the print writer to print to
     */
    public void printStackTrace(java.io.PrintWriter s) {
        s.println(getStackTraceAsString());
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append(C_CMS_EXCEPTION_PREFIX + ": ");
        output.append(m_type + " ");
        output.append(getErrorDescription(m_type) + ". ");
        if (m_message != null && (!"".equals(m_message))) {
            output.append("Detailed error: ");
            output.append(m_message + ". ");
        }
        if (m_rootCause != null) {
            output.append("\nroot cause was ");
            output.append(m_rootCause);
        }
        return output.toString();
    }
}
