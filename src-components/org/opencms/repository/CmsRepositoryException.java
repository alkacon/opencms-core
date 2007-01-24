/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/repository/Attic/CmsRepositoryException.java,v $
 * Date   : $Date: 2007/01/24 14:55:05 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.repository;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Super class for all thrown exceptions in the webdav package.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.5.6
 */
public class CmsRepositoryException extends Exception {

    private static final long serialVersionUID = -2301407362289562282L;

    /** Root failure cause. */
    protected Throwable m_rootCause;

    /**
     * Constructs a new instance of this class with <code>null</code> as its
     * detail message.<p>
     */
    public CmsRepositoryException() {

        super();
    }

    /**
     * Constructs a new instance of this class with the specified detail
     * message.<p>
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public CmsRepositoryException(String message) {

        super(message);
    }

    /**
     * Constructs a new instance of this class with the specified detail
     * message and root cause.<p>
     *
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     * @param rootCause root failure cause
     */
    public CmsRepositoryException(String message, Throwable rootCause) {

        super(message);
        this.m_rootCause = rootCause;
    }

    /**
     * Constructs a new instance of this class with the specified root cause.<p>
     *
     * @param rootCause root failure cause
     */
    public CmsRepositoryException(Throwable rootCause) {

        super();
        this.m_rootCause = rootCause;
    }

    /**
     * Returns the detail message, including the message from the nested
     * exception if there is one.<p>
     *
     * @return the detail message (which may be <code>null</code>)
     */
    public String getMessage() {

        String s = super.getMessage();
        if (m_rootCause == null) {
            return s;
        } else {
            String s2 = m_rootCause.getMessage();
            return s == null ? s2 : s + ": " + s2;
        }
    }

    /**
     * Returns the localized message, including the localized message from the
     * nested exception if there is one.<p>
     *
     * @return The localized description of this exception
     */
    public String getLocalizedMessage() {

        String s = super.getLocalizedMessage();
        if (m_rootCause == null) {
            return s;
        } else {
            String s2 = m_rootCause.getLocalizedMessage();
            return s == null ? s2 : s + ": " + s2;
        }
    }

    /**
     * Returns the cause of this exception or <code>null</code> if the
     * cause is nonexistent or unknown. (The cause is the throwable that
     * caused this exception to get thrown.)<p>
     *
     * @return the cause of this exception or <code>null</code> if the
     *         cause is nonexistent or unknown
     */
    public Throwable getCause() {

        return m_rootCause;
    }

    /**
     * Prints this <code>RepositoryException</code> and its backtrace to the
     * standard error stream.<p>
     */
    public void printStackTrace() {

        printStackTrace(System.err);
    }

    /**
     * Prints this <code>RepositoryException</code> and its backtrace to the
     * specified print stream.<p>
     *
     * @param s <code>PrintStream</code> to use for output
     */
    public void printStackTrace(PrintStream s) {

        synchronized (s) {
            super.printStackTrace(s);
            if (m_rootCause != null) {
                m_rootCause.printStackTrace(s);
            }
        }
    }

    /**
     * Prints this <code>RepositoryException</code> and its backtrace to
     * the specified print writer.<p>
     *
     * @param s <code>PrintWriter</code> to use for output
     */
    public void printStackTrace(PrintWriter s) {

        synchronized (s) {
            super.printStackTrace(s);
            if (m_rootCause != null) {
                m_rootCause.printStackTrace(s);
            }
        }
    }
}
