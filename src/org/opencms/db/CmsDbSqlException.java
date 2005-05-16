/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDbSqlException.java,v $
 * Date   : $Date: 2005/05/16 13:46:56 $
 * Version: $Revision: 1.1 $
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

import org.opencms.file.CmsDataAccessException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.sql.Statement;

import org.apache.commons.dbcp.DelegatingPreparedStatement;

/**
 * Used to signal sql related issues.<p> 
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsDbSqlException extends CmsDataAccessException {

    /**
     * Constructor given the originator, the statement and the exception.<p>
     * 
     * It generates an error log entry, if enabled.<p> 
     * 
     * @param originator the object that got the original <code>{@link java.sql.SQLException}</code>, may be <code>null</code>
     * @param stmt the statement that generated the <code>{@link java.sql.SQLException}</code>, may be <code>null</code>
     * @param rootCause the originating exception, may be <code>null</code> 
     */
    public CmsDbSqlException(Object originator, Statement stmt, Exception rootCause) {
        
        super(createMessage(originator, rootCause, stmt), C_DA_SQL_EXCEPTION, rootCause);

        if (OpenCms.getLog(this).isErrorEnabled()) {
            if (rootCause != null) {
                OpenCms.getLog(this).error(getMessage(), rootCause);
            } else {
                OpenCms.getLog(this).error(getMessage());
            }
        }
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsDbSqlException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }   
    
    
    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {
        
        return new CmsDbSqlException(container, cause);
    }   
    
    /**
     * Creates a message given an originating statement and exception.<p> 
     * 
     * @param originator the object that got the original exception, may be <code>null</code>
     * @param rootCause the originating exception, may be <code>null</code>
     * @param stmt the originating statement, may be <code>null</code>
     * 
     * @return the new exception message
     */
    public static String createMessage(Object originator, Throwable rootCause, Statement stmt) {

        int todo = 0;
        // TODO: localize this, also check / compare to org.opencms.db.generic.CmsSqlManager (redundancy?)
        
        String message = "";
        if (originator != null) {
            if (originator instanceof String) {
                message = (String)originator;
            } else {
                message = originator.getClass().getName();
            }
            message = "[" + message + "] ";
        }

        if (rootCause != null) {
            message += createMessage(rootCause);
        }

        if (stmt != null) {
            // unfortunately, DelegatingPreparedStatement has no toString() method implementation
            Statement s = stmt;
            while (s instanceof DelegatingPreparedStatement) {
                s = ((DelegatingPreparedStatement)s).getDelegate();
            }
            if (s != null) {
                // the query that crashed
                message += "query: " + s.toString();
            }
        }

        return message;
    }

    /**
     * Returns a description for a exception.<p>
     * 
     * @param rootCause the root cause
     * 
     * @return a description from the given exception
     */
    public static String createMessage(Throwable rootCause) {

        StackTraceElement[] stackTraceElements = rootCause.getStackTrace();
        String stackTraceElement = "";

        // we want to see only the first stack trace element of 
        // our own OpenCms classes in the log message...
        for (int i = 0; i < stackTraceElements.length; i++) {
            String currentStackTraceElement = stackTraceElements[i].toString();
            if (currentStackTraceElement.indexOf(".opencms.") != -1) {
                stackTraceElement = currentStackTraceElement;
                break;
            }
        }

        // where did we crash?
        String message = "where: " + stackTraceElement + ", ";
        // why did we crash?
        message += "why: " + rootCause.toString();

        return message;
    }
    
    
 
}