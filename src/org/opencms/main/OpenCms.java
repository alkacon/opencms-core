/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/OpenCms.java,v $
 * Date   : $Date: 2004/03/07 19:21:28 $
 * Version: $Revision: 1.30 $
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

package org.opencms.main;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRegistry;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.importexport.CmsImportExportManager;
import org.opencms.loader.CmsLoaderManager;
import org.opencms.lock.CmsLockManager;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.search.CmsSearchManager;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.workplace.CmsWorkplaceManager;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * The OpenCms "operating system" that provides 
 * public static methods which can be used by other classes to access 
 * basic system features of OpenCms like logging etc.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.30 $
 */
public final class OpenCms {
    
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
     * Notify all container event listeners that a particular event has
     * occurred for this Container.<p>  
     * 
     * The default implementation performs
     * this notification synchronously using the calling thread.<p>
     *
     * @param event a CmsEvent
     */
    public static void fireCmsEvent(CmsEvent event) {
        OpenCmsCore.getInstance().fireCmsEvent(event);
    }

    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.<p>  
     * 
     * The default implementation performs
     * this notification synchronously using the calling thread.<p>
     *
     * @param cms an initialized CmsObject
     * @param type event type
     * @param data event data
     */
    public static void fireCmsEvent(CmsObject cms, int type, java.util.Map data) {
        OpenCms.fireCmsEvent(new CmsEvent(cms, type, data));
    }
    
    /**
     * Returns the configured list of default directory file names.<p>
     *  
     * Caution: This list can not be modified.<p>
     * 
     * @return the configured list of default directory file names
     */
    public static List getDefaultFilenames() {
        return OpenCmsCore.getInstance().getDefaultFilenames();
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
     * Returns the loader manager used for loading individual resources.<p>
     * 
     * @return the loader manager used for loading individual resources
     */
    public static CmsLoaderManager getLoaderManager() {
        return OpenCmsCore.getInstance().getLoaderManager();
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
        return OpenCmsCore.getInstance().getLog(obj);
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
     * Returns the mime type for a specified file.<p>
     * 
     * @param filename the file name to check the mime type for
     * @param encoding default encoding in case of mime types is of type "text"
     * @return the mime type for a specified file
     */
    public static String getMimeType(String filename, String encoding) {        
        return OpenCmsCore.getInstance().getMimeType(filename, encoding);              
    }

    /**
     * Returns the Class that is used for the password validation.<p>
     * 
     * @return the Class that is used for the password validation
     */
    public static String getPasswordValidatingClass() {
        return OpenCmsCore.getInstance().getPasswordValidatingClass();
    }

    /**
     * Returns the registry to read values from it.<p>
     * 
     * You don't have the permissions to write values. 
     * This is useful for modules to read module-parameters.<p>
     *
     * @return the registry
     */
    public static CmsRegistry getRegistry() {
        return OpenCmsCore.getInstance().getRegistry();
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
     * Returns the initialized search manager,
     * which provides indexing and searching operations.<p>
     * 
     * @return the initialized search manager
     */
    public static CmsSearchManager getSearchManager() {
        return OpenCmsCore.getInstance().getSearchManager();
    }

    /**
     * Returns the session info storage for all active users.<p>
     * 
     * @return the session info storage for all active users
     */
    public static CmsSessionInfoManager getSessionInfoManager() {
        return OpenCmsCore.getInstance().getSessionInfoManager();
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
     * Returns the cron manager.<p>
     * 
     * @return the cron manager
     */
    /*
    public static CmsCronManager getCronManager() {
        // TODO enable the cron manager
        return OpenCmsCore.getInstance().getCronManager();
    }
    */
    
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
     * Returns an initialized CmsObject with the user initialized as provided,
     * with the "online" project selected and "/" set as the current site root.<p>
     * 
     * Note: Only the default users 'Guest' and 'Export' can initialized with 
     * this method, all other user names will throw a RuntimeException.<p>
     * 
     * @param user the user name to initialize, can only be 
     *        {@link org.opencms.db.CmsDefaultUsers#getUserGuest()} or
     *        {@link org.opencms.db.CmsDefaultUsers#getUserExport()}
     * @return an initialized CmsObject with "Guest" user permissions
     * @see org.opencms.db.CmsDefaultUsers#getUserGuest()
     * @see org.opencms.db.CmsDefaultUsers#getUserExport()
     * @throws RuntimeException in case an invalid user name is provided
     */     
    public static CmsObject initCmsObject(String user) {
        return OpenCmsCore.getInstance().initCmsObject(user);
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
}
