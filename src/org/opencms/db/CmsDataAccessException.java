/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsDataAccessException.java,v $
 * Date   : $Date: 2005/02/17 12:43:47 $
 * Version: $Revision: 1.3 $
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

package org.opencms.db;

import org.opencms.main.CmsException;

/**
 * Used to signal data access related issues, i.e. db or ldap access.<p> 
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.3 $
 * @since 6.0
 */
public class CmsDataAccessException extends CmsException {

    /** Consistency check exception. */
    public static final int C_DA_CONSISTENCY_EXCEPTION = 303;

    // the allowed type range for this exception is >=300 and <400    

    /** general da exception. */
    public static final int C_DA_EXCEPTION = 300;

    /** ldap exception. */
    public static final int C_DA_LDAP_EXCEPTION = 302;

    /** Object not found exception. */
    public static final int C_DA_OBJECT_NOT_FOUND_EXCEPTION = 304;

    /** Serialization exception. */
    public static final int C_DA_SERIALIZATION_EXCEPTION = 305;

    /** sql exception. */
    public static final int C_DA_SQL_EXCEPTION = 301;

    private static final String[] C_ERROR_MESSAGES = {
    /* 300 */"Data Access Exception",
    /* 301 */"SQL Exception",
    /* 302 */"LDAP Exception",
    /* 303 */"Consistency Check Exception",
    /* 304 */"Object Not Found Exception",
    /* 305 */"Serialization Exception"};

    /**
     * Default Ctor.<p>
     */
    public CmsDataAccessException() {

        super(C_DA_EXCEPTION);
    }

    /**
     * Constructs a exception with the specified description message and type.<p>
     * 
     * @param type the type of the exception
     */
    public CmsDataAccessException(int type) {

        super(type);
    }

    /**
     * Constructs a exception with the specified description message and root exception.<p>
     * 
     * @param type the type of the exception
     * @param rootCause root cause exception
     */
    public CmsDataAccessException(int type, Throwable rootCause) {

        super(type, rootCause);
    }

    /**
     * This ctor replaces the some use cases of the 
     * <code>CmsException.C_UNKNOWN_EXCEPTION</code> 
     * exception type.<p>
     * 
     * @param message the error message
     */
    public CmsDataAccessException(String message) {

        super(message, C_DA_EXCEPTION);
    }

    /**
     * Constructs a exception with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     */
    public CmsDataAccessException(String message, int type) {

        super(message, type);
    }

    /**
     * Ctor given the originator, the statement, the type and the exception.<p>
     * 
     * It generates an error log entry, if enabled.<p> 
     * 
     * @param message the description message, may be <code>null</code>
     * @param type the type of the exception, may be <code>null</code> for the default sql error
     * @param rootCause the originating exception, may be <code>null</code>
     */
    public CmsDataAccessException(String message, int type, Throwable rootCause) {

        super(message, type, rootCause);
    }

    /**
     * This ctor replaces the some use cases of the 
     * <code>CmsException.C_UNKNOWN_EXCEPTION</code> 
     * exception type.<p>
     * 
     * @param message the error message
     * @param rootCause the root cause
     */
    public CmsDataAccessException(String message, Throwable rootCause) {

        super(message, C_DA_EXCEPTION, rootCause);
    }

    /**
     * This ctor replaces the most use cases of the 
     * <code>CmsException.C_UNKNOWN_EXCEPTION</code> 
     * exception type.<p>
     * 
     * @param rootCause the root cause
     */
    public CmsDataAccessException(Throwable rootCause) {

        super(C_DA_EXCEPTION, rootCause);
    }

    /**
     * Returns the exception description message.<p>
     *
     * @return the exception description message
     */
    public String getMessage() {

        if (m_message != null) {
            return getClass().getName() + ": " + m_message;
        } else {
            return getClass().getName() + ": " + getErrorDescription(getType());
        }
    }

    /**
     * Returns the description String for the provided CmsException type.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */
    protected String getErrorDescription(int type) {

        if (type >= 300 && type < 400) {
            return C_ERROR_MESSAGES[type - 300];
        }
        return super.getErrorDescription(type);
    }

}