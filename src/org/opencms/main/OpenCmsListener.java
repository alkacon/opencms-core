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

package org.opencms.main;

import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;

/**
 * Provides the OpenCms system with information from the servlet context.<p>
 *
 * Used for the following purposes:<ul>
 * <li>Starting up OpenCms when the servlet container is started.</li>
 * <li>Shutting down OpenCms when the servlet container is shut down.</li>
 * <li>Informing the <code>{@link org.opencms.main.CmsSessionManager}</code> if a new session is created.</li>
 * <li>Informing the <code>{@link org.opencms.main.CmsSessionManager}</code> session is destroyed or invalidated.</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class OpenCmsListener implements ServletContextListener, HttpSessionListener {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(OpenCmsListener.class);

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event) {

        try {
            // destroy the OpenCms instance
            OpenCmsCore.getInstance().shutDown();
            shutDownSqlDrivers();
        } catch (CmsInitException e) {
            if (e.isNewError()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } catch (Throwable t) {
            // make sure all other errors are displayed in the OpenCms log
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERROR_GENERIC_0), t);
        }
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event) {

        try {
            // upgrade the OpenCms runlevel
            OpenCmsCore.getInstance().upgradeRunlevel(event.getServletContext());
        } catch (CmsInitException e) {
            if (e.isNewError()) {
                // only log new init errors
                LOG.error(e.getLocalizedMessage(), e);
            }
        } catch (Throwable t) {
            // make sure all other errors are displayed in the OpenCms log
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERROR_GENERIC_0), t);
            // throw a new init Exception to make sure a "context destroyed" event is triggered
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_GENERIC_1, t.getMessage()));
        }
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent event) {

        try {
            // inform the OpenCms session manager
            OpenCmsCore.getInstance().getSessionManager().sessionCreated(event);
        } catch (CmsInitException e) {
            if (e.isNewError()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } catch (Throwable t) {
            // make sure all other errors are displayed in the OpenCms log
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERROR_GENERIC_0), t);
        }
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent event) {

        try {
            // inform the OpenCms session manager
            OpenCmsCore.getInstance().getSessionManager().sessionDestroyed(event);
        } catch (CmsInitException e) {
            if (e.isNewError()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } catch (Throwable t) {
            // make sure all other errors are displayed in the OpenCms log
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERROR_GENERIC_0), t);
        }
    }

    /**
     * De-registers the SQL drivers in order to prevent potential memory leaks.<p>
     */
    private void shutDownSqlDrivers() {

        // This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (Throwable e) {
                System.out.println(
                    Messages.get().getBundle().key(
                        Messages.ERR_DEREGISTERING_JDBC_DRIVER_1,
                        driver.getClass().getName()));
                e.printStackTrace(System.out);
            }
        }

        try {
            Class<?> cls = Class.forName("com.mysql.jdbc.AbandonedConnectionCleanupThread");
            Method shutdownMethod = (cls == null ? null : cls.getMethod("shutdown"));
            if (shutdownMethod != null) {
                shutdownMethod.invoke(null);
            }
        } catch (Throwable e) {
            System.out.println("Failed to shutdown MySQL connection cleanup thread: " + e.getMessage());
        }
    }
}