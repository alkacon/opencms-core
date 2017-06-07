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

package org.opencms.db.jpa;

import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsVfsException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;

import javax.persistence.EntityManager;

/**
 * Wraps context information to access the OpenCms database.<p>
 *
 * @since 8.0.0
 */
public final class CmsDbContext extends org.opencms.db.CmsDbContext {

    /** The EntityManager instance for this context. */
    protected EntityManager m_entityManager;

    /** Tells when the current transaction should be commit or rollback. */
    protected boolean m_shouldCommit;

    /**
     * Creates a new, empty database context.<p>
     */
    public CmsDbContext() {

        this(null);
    }

    /**
     * Creates a new database context initialized with the given user request context.<p>
     *
     * @param context the current users request context
     */
    public CmsDbContext(CmsRequestContext context) {

        super(context);

        m_entityManager = CmsSqlManager.getEntityManager();
        m_shouldCommit = true;
        m_entityManager.getTransaction().begin();
    }

    /**
     * Clears this database context.<p>
     */
    @Override
    public void clear() {

        if (m_shouldCommit) {
            commitAndClose();
        } else {
            rollbackAndClose();
        }
        CmsSqlManager.returnEntityManager(m_entityManager);

        m_requestContext = null;
        m_flexRequestContextInfo = null;
    }

    /**
     * Returns the entity manager of this db context.<p>
     *
     * @return the entity manager of this db context
     */
    public EntityManager getEntityManager() {

        return m_entityManager;
    }

    /**
     * Reports an error to the given report (if available) and to the OpenCms log file.<p>
     *
     * @param report the report to write the error to
     * @param message the message to write to the report / log
     * @param throwable the exception to write to the report / log
     *
     * @throws CmsException if the throwable parameter is not null and a CmsException
     * @throws CmsVfsException if the throwable parameter is not null and no CmsException
     */
    @Override
    public void report(I_CmsReport report, CmsMessageContainer message, Throwable throwable)
    throws CmsVfsException, CmsException {

        m_shouldCommit = false;
        if (report != null) {
            if (message != null) {
                report.println(message, I_CmsReport.FORMAT_ERROR);
            }
            if (throwable != null) {
                report.println(throwable);
            }
        }

        throwException(message, throwable);
    }

    /**
     * Rollback current transaction and starts new.<p>
     */
    @Override
    public void rollback() {

        if ((m_entityManager.getTransaction() != null) && m_entityManager.getTransaction().isActive()) {
            m_entityManager.getTransaction().rollback();
            m_entityManager.getTransaction().begin();
            m_shouldCommit = true;
        }
    }

    /**
     * Commits current database transaction.<p>
     */
    private void commitAndClose() {

        if ((m_entityManager.getTransaction() != null) && m_entityManager.getTransaction().isActive()) {
            m_entityManager.getTransaction().commit();
        }
    }

    /**
     * Rolls back current database transaction.<p>
     */
    private void rollbackAndClose() {

        if ((m_entityManager.getTransaction() != null) && m_entityManager.getTransaction().isActive()) {
            m_entityManager.getTransaction().rollback();
        }
    }
}
