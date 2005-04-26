/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/OpenCms.java,v $
 * Date   : $Date: 2005/04/26 11:47:30 $
 * Version: $Revision: 1.50 $
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

import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsSqlManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.importexport.CmsImportExportManager;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLockManager;
import org.opencms.module.CmsModuleManager;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.search.CmsSearchManager;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.xml.CmsXmlContentTypeManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * The OpenCms "operating system" that provides 
 * public static methods which can be used by other classes to access 
 * basic system features of OpenCms like logging etc.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.50 $
 */
public final class OpenCms {

    /** Runlevel 0: System is offline. */
    public static final int RUNLEVEL_0_OFFLINE = 0;

    /** Runlevel 1: Core object created, no database (some test cases run in this level). */
    public static final int RUNLEVEL_1_CORE_OBJECT = 1;

    /** Runlevel 2: Initializing the system, required since this may take some seconds because of database connections. */
    public static final int RUNLEVEL_2_INITIALIZING = 2;

    /** Runlevel 3: Shell access to the database possible, but no servlet context available. */
    public static final int RUNLEVEL_3_SHELL_ACCESS = 3;

    /** Runlevel 4: Final runlevel where database and servlet are initialized. */
    public static final int RUNLEVEL_4_SERVLET_ACCESS = 4;

    /**
     * The public contructor is hidden to prevent generation of instances of this class.<p> 
     */
    private OpenCms() {

        // empty
    }

    /**
     * Add a cms event listener that listens to all events.<p>
     *
     * @param listener the listener to add
     */
    public static void addCmsEventListener(I_CmsEventListener listener) {

        OpenCmsCore.getInstance().addCmsEventListener(listener);
    }

    /**
     * Add a cms event listener that listens only to particular events.<p>
     *
     * @param listener the listener to add
     * @param eventTypes the events to listen for
     */
    public static void addCmsEventListener(I_CmsEventListener listener, int[] eventTypes) {

        OpenCmsCore.getInstance().addCmsEventListener(listener, eventTypes);
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     *
     * @param event a CmsEvent
     */
    public static void fireCmsEvent(CmsEvent event) {

        OpenCmsCore.getInstance().fireCmsEvent(event);
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     * 
     * The event will be given to all registered <code>{@link I_CmsEventListener}</code> objects.<p>
     * 
     * @param type event type
     * @param data event data
     */
    public static void fireCmsEvent(int type, Map data) {

        OpenCms.fireCmsEvent(new CmsEvent(type, data));
    }

    /**
     * Returns the configured list of default directory file names (instances of <code>{@link String}</code>).<p>
     *  
     * Caution: This list can not be modified.<p>
     * 
     * @return the configured list of default directory file names
     */
    public static List getDefaultFiles() {

        return OpenCmsCore.getInstance().getDefaultFiles();
    }

    /**
     * Returns the default user and group name configuration.<p>
     * 
     * @return the default user and group name configuration
     */
    public static CmsDefaultUsers getDefaultUsers() {

        return OpenCmsCore.getInstance().getDefaultUsers();
    }

    /**
     * Returns the configured export points,
     * the returned set being an unmodifiable set.<p>
     * 
     * @return an unmodifiable set of the configured export points
     */
    public static Set getExportPoints() {

        return OpenCmsCore.getInstance().getExportPoints();
    }

    /**
     * Returns the initialized import/export manager, 
     * which contains information about how to handle imported resources.<p> 
     * 
     * @return the initialized import/export manager
     */
    public static CmsImportExportManager getImportExportManager() {

        return OpenCmsCore.getInstance().getImportExportManager();
    }

    /**
     * Returns the link manager to resolve links in &lt;link&gt; tags.<p>
     * 
     * @return  the link manager to resolve links in &lt;link&gt; tags
     */
    public static CmsLinkManager getLinkManager() {

        return OpenCmsCore.getInstance().getLinkManager();
    }

    /**
     * Returns the locale manager used for obtaining the current locale.<p>
     * 
     * @return the locale manager
     */
    public static CmsLocaleManager getLocaleManager() {

        return OpenCmsCore.getInstance().getLocaleManager();
    }

    /**
     * Returns the lock manager used for the locking mechanism.<p>
     * 
     * @return the lock manager used for the locking mechanism
     */
    public static CmsLockManager getLockManager() {

        return OpenCmsCore.getInstance().getLockManager();
    }

    /**
     * Returns the log for the selected object.<p>
     * 
     * If the provided object is a String, this String will
     * be used as channel name. Otherwise the objects 
     * class name will be used as channel name.<p>
     *  
     * @param obj the object channel to use
     * @return the log for the selected object channel
     */
    public static Log getLog(Object obj) {

        return CmsLog.getLog(obj);
    }

    /**
     * Returns the memory monitor.<p>
     * 
     * @return the memory monitor
     */
    public static CmsMemoryMonitor getMemoryMonitor() {

        return OpenCmsCore.getInstance().getMemoryMonitor();
    }

    /**
     * Returns the module manager.<p>
     * 
     * @return the module manager
     */
    public static CmsModuleManager getModuleManager() {

        return OpenCmsCore.getInstance().getModuleManager();
    }

    /**
     * Returns the password handler.<p>
     * 
     * @return the password handler
     */
    public static I_CmsPasswordHandler getPasswordHandler() {

        return OpenCmsCore.getInstance().getPasswordHandler();
    }

    /**
     * Returns the resource manager.<p>
     * 
     * @return the resource manager
     */
    public static CmsResourceManager getResourceManager() {

        return OpenCmsCore.getInstance().getResourceManager();
    }

    /**
     * Returns the current OpenCms run level.<p>
     * 
     * The following runlevels are defined:
     * <dl>
     * <dt>Runlevel {@link OpenCms#RUNLEVEL_0_OFFLINE}:</dt><dd>
     * OpenCms is in the process of being shut down, the system is offline.</dd>
     * 
     * <dt>Runlevel {@link OpenCms#RUNLEVEL_1_CORE_OBJECT}:</dt><dd>
     * OpenCms instance available, but configuration has not been processed. 
     * No database or VFS available.</dd>
     * 
     * <dt>Runlevel {@link OpenCms#RUNLEVEL_2_INITIALIZING}:</dt><dd>
     * OpenCms is initializing, but the process is not finished.
     * The database with the VFS is currently being connected but can't be accessed.</dd>
     * 
     * <dt>Runlevel {@link OpenCms#RUNLEVEL_3_SHELL_ACCESS}:</dt><dd>
     * OpenCms database and VFS available, but http processing (i.e. servlet) not initialized.
     * This is the runlevel the OpenCms shell operates in.</dd>
     * 
     * <dt>Runlevel {@link OpenCms#RUNLEVEL_4_SERVLET_ACCESS}:</dt><dd>
     * OpenCms fully initialized, servlet and database available.
     * This is the "default" when OpenCms is in normal operation.</dd>
     * </dl>
     * 
     * @return the OpenCms run level
     */
    public static int getRunLevel() {

        return OpenCmsCore.getInstance().getRunLevel();
    }

    /** 
     * Looks up a value in the runtime property Map.<p>
     *
     * @param key the key to look up in the runtime properties
     * @return the value for the key, or null if the key was not found
     */
    public static Object getRuntimeProperty(Object key) {

        return OpenCmsCore.getInstance().getRuntimeProperty(key);
    }

    /**
     * Returns the configured schedule manager.<p>
     *
     * @return the configured schedule manager
     */
    public static CmsScheduleManager getScheduleManager() {

        return OpenCmsCore.getInstance().getScheduleManager();
    }

    /**
     * Returns the initialized search manager,
     * which provides indexing and searching operations.<p>
     * 
     * @return the initialized search manager
     */
    public static CmsSearchManager getSearchManager() {

        return OpenCmsCore.getInstance().getSearchManager();
    }

    /**
     * Returns the session manager that keeps track of the active users.<p>
     * 
     * @return the session manager that keeps track of the active users
     */
    public static CmsSessionManager getSessionManager() {

        return OpenCmsCore.getInstance().getSessionManager();
    }

    /**
     * Returns the initialized site manager, 
     * which contains information about all configured sites.<p> 
     * 
     * @return the initialized site manager
     */
    public static CmsSiteManager getSiteManager() {

        return OpenCmsCore.getInstance().getSiteManager();
    }

    /**
     * Returns an instance of the common sql manager.<p>
     * 
     * @return an instance of the common sql manager
     */
    public static CmsSqlManager getSqlManager() {

        return OpenCmsCore.getInstance().getSqlManager();
    }

    /**
     * Returns the properties for the static export.<p>
     * 
     * @return the properties for the static export
     */
    public static CmsStaticExportManager getStaticExportManager() {

        return OpenCmsCore.getInstance().getStaticExportManager();
    }

    /**
     * Returns the system information storage.<p> 
     * 
     * @return the system information storage
     */
    public static CmsSystemInfo getSystemInfo() {

        return OpenCmsCore.getInstance().getSystemInfo();
    }

    /**
     * Returns the list of system defined roles (instances of <code>{@link CmsRole}</code>).<p> 
     * 
     * Caution: This list can not be modified.<p>
     * 
     * @return the list of system defined roles
     */
    public static List getSystemRoles() {

        return CmsRole.getSystemRoles();
    }

    /**
     * Returns the OpenCms Thread store.<p>
     * 
     * @return the OpenCms Thread store
     */
    public static CmsThreadStore getThreadStore() {

        return OpenCmsCore.getInstance().getThreadStore();
    }

    /**
     * Returns the initialized workplace manager, 
     * which contains information about the global workplace settings.<p> 
     * 
     * @return the initialized workplace manager
     */
    public static CmsWorkplaceManager getWorkplaceManager() {

        return OpenCmsCore.getInstance().getWorkplaceManager();
    }

    /**
     * Returns the XML content type manager.<p>
     * 
     * @return the XML content type manager
     */
    public static CmsXmlContentTypeManager getXmlContentTypeManager() {

        return OpenCmsCore.getInstance().getXmlContentTypeManager();
    }

    /**
     * Returns an initialized CmsObject with the user and context initialized as provided.<p>
     * 
     * Note: Only if the provided <code>adminCms</code> CmsObject has admin permissions, 
     * this method allows the creation a CmsObject for any existing user. Otherwise
     * only the default users 'Guest' and 'Export' can initialized with 
     * this method, all other user names will throw an Exception.<p>
     * 
     * @param adminCms must either be initialized with "Admin" permissions, or null
     * @param contextInfo the context info to create a CmsObject for
     * 
     * @return an initialized CmsObject with the given users permissions
     * 
     * @throws CmsException if an invalid user name was provided, or if something else goes wrong
     * 
     * @see org.opencms.db.CmsDefaultUsers#getUserGuest()
     * @see org.opencms.db.CmsDefaultUsers#getUserExport()
     * @see OpenCms#initCmsObject(CmsObject, CmsContextInfo)
     * @see #initCmsObject(String)
     */
    public static CmsObject initCmsObject(CmsObject adminCms, CmsContextInfo contextInfo) throws CmsException {

        return OpenCmsCore.getInstance().initCmsObject(adminCms, contextInfo);
    }

    /**
     * Returns an initialized CmsObject with the user initialized as provided,
     * with the "Online" project selected and "/" set as the current site root.<p>
     * 
     * Note: Only the default users 'Guest' and 'Export' can initialized with 
     * this method, all other user names will throw an Exception.<p>
     * 
     * @param user the user name to initialize, can only be 
     *        {@link org.opencms.db.CmsDefaultUsers#getUserGuest()} or
     *        {@link org.opencms.db.CmsDefaultUsers#getUserExport()}
     * 
     * @return an initialized CmsObject with the given users permissions
     * 
     * @throws CmsException if an invalid user name was provided, or if something else goes wrong
     * 
     * @see org.opencms.db.CmsDefaultUsers#getUserGuest()
     * @see org.opencms.db.CmsDefaultUsers#getUserExport()
     * @see OpenCms#initCmsObject(String)
     * @see #initCmsObject(CmsObject, CmsContextInfo)
     */
    public static CmsObject initCmsObject(String user) throws CmsException {

        return OpenCmsCore.getInstance().initCmsObject(user);
    }

    /**
     * Reads the requested resource from the OpenCms VFS,
     * and in case a directory name is requested, the default files of the 
     * directory will be looked up and the first match is returned.<p>
     *
     * The resource that is returned is always a <code>{@link org.opencms.file.CmsFile}</code>,
     * even though the content will usually not be loaded in the result. Folders are never returned since
     * the point of this method is really to load the default file if just a folder name is requested.<p>
     *
     * The URI stored in the given OpenCms user context will be changed to the URI of the resource 
     * that was found and returned.<p>
     * 
     * Implementing and configuring an <code>{@link I_CmsResourceInit}</code> handler 
     * allows to customize the process of default resouce selection.<p>
     *
     * @param cms the current users OpenCms context
     * @param resourceName the path of the requested resource in the OpenCms VFS
     * @param req the current http request
     * @param res the current http response
     * @return the requested resource read from the VFS
     * 
     * @throws CmsException in case the requested file does not exist or the user has insufficient access permissions 
     */
    public static CmsResource initResource(
        CmsObject cms,
        String resourceName,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException {

        return OpenCmsCore.getInstance().initResource(cms, resourceName, req, res);
    }

    /**
     * Removes a cms event listener.<p>
     *
     * @param listener the listener to remove
     */
    public static void removeCmsEventListener(I_CmsEventListener listener) {

        OpenCmsCore.getInstance().removeCmsEventListener(listener);
    }

    /**       
     * This method adds an Object to the OpenCms runtime properties.
     * The runtime properties can be used to store Objects that are shared
     * in the whole system.<p>
     *
     * @param key the key to add the Object with
     * @param value the value of the Object to add
     */
    public static void setRuntimeProperty(Object key, Object value) {

        OpenCmsCore.getInstance().setRuntimeProperty(key, value);
    }

    /**
     * Writes the XML configuration for the provided configuration class.<p>
     * 
     * @param clazz the configuration class to write the XML for
     */
    public static void writeConfiguration(Class clazz) {

        OpenCmsCore.getInstance().writeConfiguration(clazz);
    }
}