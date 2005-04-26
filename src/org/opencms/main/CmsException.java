/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsException.java,v $
 * Date   : $Date: 2005/04/26 13:20:51 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.i18n.CmsMessageContainer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

/**
 * Master exception type for all exceptions caused in OpenCms.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Michael Moossen (m.moossen@alkacon.com)
 * 
 * @version $Revision: 1.15 $
 */
public class CmsException extends Exception implements I_CmsThrowable {

    /** Error code for bad name exception. */
    public static final int C_BAD_NAME = 3;

    /** Error code for ClassLoader errors. */
    public static final int C_CLASSLOADER_ERROR = 29;

    /**
     * This array provides descriptions for the error codes stored as
     * constants in the CmsExeption class.
     */
    public static final String[] C_ERROR_DESCRIPTION = {
    /*  0 */"Unknown exception",
    /*  1 */"Access denied",
    /*  2 */null, // moved to CmsVfsResourceNotFoundException
    /*  3 */"Bad name",
    /*  4 */null, // moved to and extended by CmsDataAccessException
    /*  5 */null, // moved to CmsVfsException-C_VFS_FOLDER_NOT_EMPTY)
    /*  6 */"Admin access required",
    /*  7 */null, // moved to CmsSerializationException
    /*  8 */"Unknown User Group",
    /*  9 */"Group not empty",
    /* 10 */"Unknown User",
    /* 11 */"No removal from Default Group",
    /* 12 */null, // moved to CmsVfsException-C_VFS_RESOURCE_ALREADY_EXISTS
    /* 13 */"File not found exception",
    /* 14 */"Filesystem exception",
    /* 15 */"Internal use only",
    /* 16 */"Deprecated exception: File-property is mandatory",
    /* 17 */"Service unavailable",
    /* 18 */"Unknown XML datablock",
    /* 19 */"Corrupt internal structure",
    /* 20 */"Wrong XML content type",
    /* 21 */"XML parsing error",
    /* 22 */"Could not process OpenCms special XML tag",
    /* 23 */"Could not call user method",
    /* 24 */"Could not call process method",
    /* 25 */"XML tag missing",
    /* 26 */"Wrong XML template class",
    /* 27 */"No XML template class",
    /* 28 */null, // moved to CmsLoaderException
    /* 29 */"OpenCms class loader error",
    /* 30 */"New password is too short",
    /* 31 */null, // unused
    /* 32 */null, // moved to CmsVfsException-C_VFS_RESOURCE_DELETED
    /* 33 */"DriverManager init error",
    /* 34 */"Registry error",
    /* 35 */"Security Manager initialization error",
    /* 36 */null, // unused
    /* 37 */"Wrong scheme for http resource",
    /* 38 */"Wrong scheme for https resource",
    /* 39 */"Error in Flex cache",
    /* 40 */"Error in Flex loader",
    /* 41 */"Group already exists",
    /* 42 */"User already exists",
    /* 43 */"Import error",
    /* 44 */"Export error"};

    /** Error code for export issues. */
    public static final int C_EXPORT_ERROR = 42;

    /** 
     * Error code for file exists exception.<p>
     * 
     * @deprecated use a <code>{@link org.opencms.file.CmsVfsException}</code> instead
     */
    public static final int C_FILE_EXISTS = 12;

    /** Error code for file not found exception. */
    public static final int C_FILE_NOT_FOUND = 13;

    /** Error code filesystem error. */
    public static final int C_FILESYSTEM_ERROR = 14;

    /** Error code for Flex loader. */
    public static final int C_FLEX_LOADER = 40;

    /** Error code that a group to be created already exists. */
    public static final int C_GROUP_ALREADY_EXISTS = 41;

    /** Error code for group not empty exception. */
    public static final int C_GROUP_NOT_EMPTY = 9;

    /** Error code for HTTP streaming error. */
    public static final int C_HTTPS_PAGE_ERROR = 37;

    /** Error code for HTTPS streaming error. */
    public static final int C_HTTPS_REQUEST_ERROR = 38;

    /** Error code for import issues. */
    public static final int C_IMPORT_ERROR = 41;

    /** Error code internal file. */
    public static final int C_INTERNAL_FILE = 15;

    /** 
     * Error code for access denied exception for vfs resources.
     * @deprecated use a <code>{@link org.opencms.security.CmsSecurityException}</code> instead
     */
    public static final int C_NO_ACCESS = 1;

    /** Error code for no default group exception. */
    public static final int C_NO_DEFAULT_GROUP = 11;

    /** Error code for no group exception. */
    public static final int C_NO_GROUP = 8;

    /** Error code for no user exception. */
    public static final int C_NO_USER = 10;

    /** Error code for no admin exception. */
    public static final int C_NOT_ADMIN = 6;

    /** 
     * Error code for not empty exception.<p>
     * 
     * @deprecated use a <code>{@link org.opencms.file.CmsVfsException}</code> instead
     */
    public static final int C_NOT_EMPTY = 5;

    /** 
     * Error code for not found exception.<p>
     * 
     * @deprecated use a <code>{@link org.opencms.db.CmsObjectNotFoundException}</code> 
     *    or <code>{@link org.opencms.file.CmsVfsResourceNotFoundException}</code> instead
     */
    public static final int C_NOT_FOUND = 2;

    /** Error code for driver manager initialization errors. */
    public static final int C_RB_INIT_ERROR = 33;

    /** Error code for Registry exception. */
    public static final int C_REGISTRY_ERROR = 34;

    /** 
     * Error code for accessing a deleted resource.<p>
     * 
     * @deprecated use a <code>{@link org.opencms.file.CmsVfsException}</code> instead
     */
    public static final int C_RESOURCE_DELETED = 32;

    /** 
     * Error code for serialization exception. 
     * 
     * @deprecated use a <code>{@link org.opencms.db.CmsSerializationException}</code> instead
     */
    public static final int C_SERIALIZATION = 7;

    /** Error code service unavailable. */
    public static final int C_SERVICE_UNAVAILABLE = 17;

    /** Error code for security manager initialization error. */
    public static final int C_SM_INIT_ERROR = 35;

    /** 
     * Error code for sql exception.<p>
     * 
     * @deprecated use a <code>{@link org.opencms.db.CmsDataAccessException}</code> 
     *      or one of their subclasses instead
     */
    public static final int C_SQL_ERROR = 4;

    /** 
     * Error code for unknown exception.
     *  
     * @deprecated use a <code>{@link org.opencms.db.CmsDataAccessException}</code> 
     *      or one of their subclasses instead
     */
    public static final int C_UNKNOWN_EXCEPTION = 0;

    /** Error code that a user to be created already exists. */
    public static final int C_USER_ALREADY_EXISTS = 42;

    /** Error code for corrupt internal structure. */
    public static final int C_XML_CORRUPT_INTERNAL_STRUCTURE = 19;

    /** Error code for XML process method not found. */
    public static final int C_XML_NO_PROCESS_METHOD = 24;

    /** Error code for no XML template class. */
    public static final int C_XML_NO_TEMPLATE_CLASS = 27;

    /** Error code for XML user method not found. */
    public static final int C_XML_NO_USER_METHOD = 23;

    /** Error code for XML parsing error. */
    public static final int C_XML_PARSING_ERROR = 21;

    /** Error code for XML processing error. */
    public static final int C_XML_PROCESS_ERROR = 22;

    /** Error code for missing XML tag. */
    public static final int C_XML_TAG_MISSING = 25;

    /** Error code for unknown XML datablocks. */
    public static final int C_XML_UNKNOWN_DATA = 18;

    /** Error code for wrong XML content type. */
    public static final int C_XML_WRONG_CONTENT_TYPE = 20;

    /** Error code for wrong XML template class. */
    public static final int C_XML_WRONG_TEMPLATE_CLASS = 26;

    /** The container for the localized message.  */
    protected CmsMessageContainer m_message;

    /** Stores the error code of the CmsException. */
    protected int m_type;

    /**
     * Creates a simple CmsException.<p>
     */
    public CmsException() {

        super();
    }

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param message the localized message container to use
     */
    public CmsException(CmsMessageContainer message) {

        super(message.getKey());
        m_message = message;
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param message the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsException(CmsMessageContainer message, Throwable cause) {

        super(message.getKey(), cause);
        m_message = message;
    }

    /**
     * Creates a CmsException with the provided error code, 
     * the error codes used should be the constants from the CmsEception class or subclass.<p>
     *
     * @param type exception error code
     */
    public CmsException(int type) {

        super();
        m_type = type;
    }

    /**
     * Creates a CmsException with the provided error code and
     * a given root cause.<p>
     * 
     * The error codes used should be the constants from the CmsEception class or subclass.<p>
     *
     * @param type exception error code
     * @param cause root cause exception
     */
    public CmsException(int type, Throwable cause) {

        super(cause);
        m_type = type;
    }

    /**
     * Creates a CmsException with the provided description message.<p>
     *
     * @param message the description message
     */
    public CmsException(String message) {

        super(message);
    }

    /**
     * Creates a CmsException with the provided description message and error code.<p>
     * 
     * @param message the description message
     * @param type exception error code
     */
    public CmsException(String message, int type) {

        super(message);
        m_type = type;
    }

    /**
     * Creates a CmsException with the provided description message, error code and 
     * a given root cause.<p>
     *
     * @param message the description message
     * @param type exception error code
     * @param cause root cause exception
     */
    public CmsException(String message, int type, Throwable cause) {

        super(message, cause);
        m_type = type;
    }

    /**
     * Construtcs a CmsException with the provided description message and 
     * a given root cause.<p>
     *
     * @param message the description message
     * @param cause root cause exception
     */
    public CmsException(String message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Returns the stack trace (including the message) of an exception as a String.<p>
     * 
     * If the exception is a CmsException, 
     * also writes the root cause to the String.<p>
     * 
     * @param e the exception to get the stack trace from
     * @return the stack trace of an exception as a String
     */
    public static String getStackTraceAsString(Throwable e) {

        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage()
     */
    public String getLocalizedMessage() {

        if (m_message == null) {
            return super.getLocalizedMessage();
        }
        return m_message.key();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage(Locale)
     */
    public String getLocalizedMessage(Locale locale) {

        return m_message.key(locale);
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage() {

        if (m_message != null) {
            // localized message is available
            return getLocalizedMessage();
        }

        StringBuffer result = new StringBuffer(256);
        result.append(super.getMessage());

        if (getType() > 0) {
            result.append(' ');
            result.append(getErrorDescription(getType()));
        }

        return result.toString();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getMessageContainer()
     */
    public CmsMessageContainer getMessageContainer() {

        return m_message;
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
     * Returns the description String for the provided CmsException type, subclasses of 
     * CmsException should overwrite this method for the types they define.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */
    protected String getErrorDescription(int type) {

        if ((type < CmsException.C_ERROR_DESCRIPTION.length) && (type > 0)) {
            return CmsException.C_ERROR_DESCRIPTION[type];
        } else {
            return this.getClass().getName();
        }
    }
}