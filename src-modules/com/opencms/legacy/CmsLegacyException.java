/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsLegacyException.java,v $
 * Date   : $Date: 2005/09/11 13:27:06 $
 * Version: $Revision: 1.13 $
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

package com.opencms.legacy;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsThrowable;

import java.util.Locale;

/**
 * Exception to keep the legacy packages com.opencms.* and com.opencms.*
 * with the old exception handling mechanism running.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * 
 * @version $Revision: 1.13 $
 */
public class CmsLegacyException extends CmsException implements I_CmsThrowable {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -276358443570364537L;

    /** 
     * MessageContainer with string-constructor, to be used only in this class.
     * CmsLegacyMessageContainers are not localized. <p>
     */
    private static class CmsLegacyMessageContainer extends CmsMessageContainer {
        
        /**
         * Creates a new message container with the specified message.<p>
         * 
         * @param message the message to use
         */
        public CmsLegacyMessageContainer(String message) {

            super(Messages.get(), message);
        }
        
        /**
         * Returns the message described by this container.<p>
         * 
         * @return the message described by this container
         */
        public String key() {

            return getKey();
        }

        /**
         * Returns the message described by this container. The message is not localized and the locale argument will
         * not be taken into account.<p>
         * 
         * @param locale the locale to use
         * @return the message described by this container
         */
        public String key(Locale locale) {

            return getKey();
        }

    }
    
    /**
     * This array provides descriptions for the error codes stored as
     * constants in the CmsLegacyExeption class.
     */
    public static final String[] C_ERROR_DESCRIPTION = {
    /*  0 */"Unknown exception",
    /*  1 */"Access denied",
    /*  2 */null,
    /*  3 */"Bad name",
    /*  4 */null,
    /*  5 */null,
    /*  6 */"Admin access required",
    /*  7 */null,
    /*  8 */"Unknown User Group",
    /*  9 */"Group not empty",
    /* 10 */"Unknown User",
    /* 11 */"No removal from Default Group",
    /* 12 */null,
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
    /* 28 */null,
    /* 29 */"OpenCms class loader error",
    /* 30 */"New password is too short",
    /* 31 */null,
    /* 32 */null,
    /* 33 */"DriverManager init error",
    /* 34 */"Registry error",
    /* 35 */"Security Manager initialization error",
    /* 36 */null,
    /* 37 */"Wrong scheme for http resource",
    /* 38 */"Wrong scheme for https resource",
    /* 39 */"Error in Flex cache",
    /* 40 */"Error in Flex loader",
    /* 41 */"Group already exists",
    /* 42 */"User already exists",
    /* 43 */"Import error",
    /* 44 */"Export error",
    /* 45 */"Resource is not locked",
    /* 46 */"Insufficient lock to edit content of resource",
    /* 47 */"Resource locked by another user",
    /* 48 */"Administrator priviledges are required to perform this operation",
    /* 49 */"Project manager priviledges are required to perform this operation",
    /* 50 */"Modify operation not allowed in 'Online' project",
    /* 51 */"No permissions to perform this operation",
    /* 52 */"Invalid password",
    /* 53 */"OpenCms login validation failed",
    /* 54 */"Error while loading invoking resource loader",
    /* 55 */"Resource loader not template enabled",
    /* 56 */"Unknown resource type requested!"};


    /** Error code for unknown exception.*/
    public static final int C_UNKNOWN_EXCEPTION = 0;

    /** Error code for not found exception.*/
    public static final int C_NOT_FOUND = 2;

    /** Error code for bad name exception. */
    public static final int C_BAD_NAME = 3;

    /** Error code for sql exception.*/
    public static final int C_SQL_ERROR = 4;

    /** Error code for not empty exception.*/
    public static final int C_NOT_EMPTY = 5;

    /** Error code for no admin exception. */
    public static final int C_NOT_ADMIN = 6;

    /** Generic error code for loader errors. */
    public static final int C_LOADER_GENERIC_ERROR = 54;

    /** Error code for no group exception. */
    public static final int C_NO_GROUP = 8;

    /** Error code for no user exception. */
    public static final int C_NO_USER = 10;

    /** Error code service unavailable. */
    public static final int C_SERVICE_UNAVAILABLE = 17;

    /** Error code for unknown XML datablocks. */
    public static final int C_XML_UNKNOWN_DATA = 18;

    /** Error code for corrupt internal structure. */
    public static final int C_XML_CORRUPT_INTERNAL_STRUCTURE = 19;

    /** Error code for wrong XML content type. */
    public static final int C_XML_WRONG_CONTENT_TYPE = 20;

    /** Error code for XML process method not found. */
    public static final int C_XML_NO_PROCESS_METHOD = 24;

    /** Error code for XML parsing error. */
    public static final int C_XML_PARSING_ERROR = 21;

    /** Error code for XML processing error. */
    public static final int C_XML_PROCESS_ERROR = 22;

    /** Error code for XML user method not found. */
    public static final int C_XML_NO_USER_METHOD = 23;

    /** Error code for missing XML tag. */
    public static final int C_XML_TAG_MISSING = 25;

    /** Error code for wrong XML template class. */
    public static final int C_XML_WRONG_TEMPLATE_CLASS = 26;

    /** Error code for no XML template class. */
    public static final int C_XML_NO_TEMPLATE_CLASS = 27;

    /** Error code for ClassLoader errors. */
    public static final int C_CLASSLOADER_ERROR = 29;

    /** Error code for Registry exception. */
    public static final int C_REGISTRY_ERROR = 34;

    /** A resource is unlocked, but a particular action requires the resource to be locked. */
    public static final int C_RESOURCE_UNLOCKED = 45;

    /** A resource has a non-exclusive lock, but a particular action requires an exclusive lock. */
    public static final int C_RESOURCE_LOCKED_NON_EXCLUSIVE = 46;

    /** A resource is locked by a user different from the current user, but a particular action requires that the resource is locked by the current user. */
    public static final int C_RESOURCE_LOCKED_BY_OTHER_USER = 47;

    /** Invalid password (only for password change and validation of password). */
    public static final int C_SECURITY_INVALID_PASSWORD = 52;

    /** Stores the error code of the CmsLegacyException. */
    protected int m_type;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param message the localized message container to use
     */
    public CmsLegacyException(CmsMessageContainer message) {

        super(message);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param message the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsLegacyException(CmsMessageContainer message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Creates a CmsLegacyException with the provided error code, 
     * the error codes used should be the constants from the CmsEception class or subclass.<p>
     *
     * @param type exception error code
     */
    public CmsLegacyException(int type) {

        super(new CmsLegacyMessageContainer(CmsLegacyException.getErrorDescription(type)));
        m_type = type;
    }

    /**
     * Creates a CmsLegacyException with the provided error code and
     * a given root cause.<p>
     * 
     * The error codes used should be the constants from the CmsLegacyException class or subclass.<p>
     *
     * @param type exception error code
     * @param cause root cause exception
     */
    public CmsLegacyException(int type, Throwable cause) {

        super(new CmsLegacyMessageContainer(CmsLegacyException.getErrorDescription(type)));
        initCause(cause);
        m_type = type;
    }

    /**
     * Creates a CmsLegacyException with the provided description message.<p>
     *
     * @param message the description message
     */
    public CmsLegacyException(String message) {

        super(new CmsLegacyMessageContainer(message));
    }

    /**
     * Creates a CmsLegacyException with the provided description message and error code.<p>
     * 
     * @param message the description message
     * @param type exception error code
     */
    public CmsLegacyException(String message, int type) {

        super(new CmsLegacyMessageContainer(message));
        m_type = type;
    }

    /**
     * Creates a CmsLegacyException with the provided description message, error code and 
     * a given root cause.<p>
     *
     * @param message the description message
     * @param type exception error code
     * @param cause root cause exception
     */
    public CmsLegacyException(String message, int type, Throwable cause) {

        super(new CmsLegacyMessageContainer(message));
        initCause(cause);
        m_type = type;
    }

    /**
     * Construtcs a CmsLegacyException with the provided description message and 
     * a given root cause.<p>
     *
     * @param message the description message
     * @param cause root cause exception
     */
    public CmsLegacyException(String message, Throwable cause) {

        super(new CmsLegacyMessageContainer(message));
        initCause(cause);
    }

    /**
     * Creates a copied instance of this localized exception.<p>
     * 
     * @param container the message container
     * @param cause the root cause
     * 
     * @return a copied instance of this localized exception
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsLegacyException(container, cause);
    }

    /**
     * Returns the type of the CmsLegacyException.<p>
     *
     * @return the type of the CmsLegacyException
     */
    public int getType() {

        return m_type;
    }

    /**
     * Returns the description String for the provided CmsLegacyException type, subclasses of 
     * CmsLegacyException should overwrite this method for the types they define.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsLegacyException type
     */
    protected static String getErrorDescription(int type) {

        if ((type < CmsLegacyException.C_ERROR_DESCRIPTION.length) && (type > 0)) {
            return CmsLegacyException.C_ERROR_DESCRIPTION[type];
        } else {
            return CmsLegacyException.class.getName();
        }
    }
}