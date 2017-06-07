/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.sql.Statement;

import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.logging.Log;

/**
 * Used to signal sql related issues.<p>
 *
 * @since 6.0.0
 */
public class CmsDbSqlException extends CmsDbException {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDriverManager.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -286617872967617367L;

    /**
     * Creates a new localized Exception.<p>
     *
     * @param container the localized message container to use
     */
    public CmsDbSqlException(CmsMessageContainer container) {

        super(container);
        // log all sql exceptions
        if (LOG.isErrorEnabled()) {
            LOG.error(container.key(), this);
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
        // log all sql exceptions
        if (LOG.isWarnEnabled()) {
            LOG.warn(container.key(), this);
        }
    }

    /**
     * Returns the query that let the statement crash.<p>
     *
     * @param stmt the Statement to get the crashed query from
     * @return the crashed query
     */
    public static String getErrorQuery(Statement stmt) {

        if (stmt != null) {
            // unfortunately, DelegatingPreparedStatement has no toString() method implementation
            Statement s = stmt;
            while (s instanceof DelegatingPreparedStatement) {
                s = ((DelegatingPreparedStatement)s).getInnermostDelegate();
            }
            if (s != null) {
                // the query that crashed
                return s.toString();
            }
        }
        return "";
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    @Override
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsDbSqlException(container, cause);
    }
}