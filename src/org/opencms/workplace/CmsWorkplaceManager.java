/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceManager.java,v $
 * Date   : $Date: 2004/02/04 15:48:16 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace;

import com.opencms.file.CmsObject;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;
import org.opencms.workplace.editor.CmsWorkplaceEditorManager;
import org.opencms.workplace.editor.I_CmsEditorActionHandler;
import org.opencms.workplace.editor.I_CmsEditorHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Manages the global OpenCms workplace settings for all users.<p>
 * 
 * This class reads the settings from the "opencms.properties" and stores them in member variables.
 * For each setting one or more get methods are provided.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.1
 */
public final class CmsWorkplaceManager {
    
    private boolean m_autoLockResources;
    private Map m_dialogHandler;
    private boolean m_directPublishSiblings;
    private I_CmsEditorHandler m_editorHandler;
    private I_CmsEditorActionHandler m_editorActionHandler;
    private int m_fileMaxUploadSize;
    private List m_labelSiteFolders;
    private boolean m_showUserGroupIcon;
    private CmsWorkplaceEditorManager m_editorManager;
    
    /**
     * Creates an initialized instance and fills all member variables with their configuration values.<p>
     * 
     * @param configuration the OpenCms configuration
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @throws Exception if a configuration goes wrong
     */
    public CmsWorkplaceManager(ExtendedProperties configuration, CmsObject cms) throws Exception {
        // initialize "dialoghandler" registry classes
        try {
            List dialogHandlerClasses = OpenCms.getRegistry().getDialogHandler();
            Iterator i = dialogHandlerClasses.iterator();
            m_dialogHandler = new HashMap();
            while (i.hasNext()) {
                String currentClass = (String)i.next();                
                I_CmsDialogHandler handler = (I_CmsDialogHandler)Class.forName(currentClass).newInstance();            
                m_dialogHandler.put(handler.getDialogHandler(), handler);
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Dialog handler class : " + currentClass + " instanciated");
                }
            }
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "7", e);
            }
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }
        
        // initialize "editorhandler" registry class
        try {
            List editorHandlerClasses = OpenCms.getRegistry().getEditorHandler();
            String currentClass = (String)editorHandlerClasses.get(0);                
            m_editorHandler = (I_CmsEditorHandler)Class.forName(currentClass).newInstance();            
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Editor handler class : " + currentClass + " instanciated");
            }
            
        } catch (Exception e) {
            if (OpenCms.getLog(this).isInfoEnabled()) {
                //getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "8", e);
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Editor handler class : non-critical error initializing editor handler");
            }
            // any exception here is fatal and will cause a stop in processing
            // TODO: activate throwing exception: throw e;
        }
        
        // initialize "editoraction" registry class
        try {
            List editorActionClasses = OpenCms.getRegistry().getEditorAction();
            String currentClass = (String)editorActionClasses.get(0);                
            m_editorActionHandler = (I_CmsEditorActionHandler)Class.forName(currentClass).newInstance();            
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Editor action class  : " + currentClass + " instanciated");
            }
        } catch (Exception e) {
            if (OpenCms.getLog(this).isInfoEnabled()) {
                //getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "8", e);
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Editor action class  : non-critical error initializing editor action class");
            }
            // any exception here is fatal and will cause a stop in processing
            // TODO: activate throwing exception: throw e;
        }
        
        // read the maximum file upload size limit
        m_fileMaxUploadSize = configuration.getInteger("workplace.file.maxuploadsize", -1);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". File max. upload size: " + (m_fileMaxUploadSize > 0 ? (m_fileMaxUploadSize + " KB") : "unlimited"));
        }
        
        // Determine if the user/group icons are displayed in the Administration view
        m_showUserGroupIcon = configuration.getBoolean("workplace.administration.showusergroupicon", true);
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Show user/group icon : " + (m_showUserGroupIcon ? "yes" : "no"));
        }
        
        // site folders for which links should be labeled specially in the explorer
        String[] labelSiteFolderString = configuration.getStringArray("site.labeled.folders");
        if (labelSiteFolderString == null) {
            labelSiteFolderString = new String[0];
        }
        List labelSiteFoldersOri = java.util.Arrays.asList(labelSiteFolderString);
        m_labelSiteFolders = new ArrayList();
        for (int i = 0; i < labelSiteFoldersOri.size(); i++) {
            // remove possible white space
            String name = ((String)labelSiteFoldersOri.get(i)).trim();
            if (name != null && !"".equals(name)) {
                m_labelSiteFolders.add(name);
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Label links in folder: " + (i + 1) + " - " + name);
                }
            }
        }
        
        // set the property whether siblings should get published if a file gets published directly
        m_directPublishSiblings = configuration.getBoolean("workplace.directpublish.siblings", false);
        
        // set the property if the automatic locking of resources is enabled in explorer view
        m_autoLockResources = configuration.getBoolean("workplace.autolock.resources", false);
        
        // initialize the workplace editor manager
        m_editorManager = new CmsWorkplaceEditorManager(cms);
    }
    
    /**
     * Initializes the workplace manager with the OpenCms system configuration.<p>
     * 
     * @param configuration the OpenCms configuration
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @return the initialized workplace manager
     * @throws Exception if a configuration goes wrong
     */
    public static CmsWorkplaceManager initialize(ExtendedProperties configuration, CmsObject cms) throws Exception {              
        // create and return the workplace manager
        return new CmsWorkplaceManager(configuration, cms);
    }
    
    /**
     * Returns if the autolock resources feature is enabled.<p>
     * 
     * @return true if the autolock resources feature is enabled, otherwise false
     */
    public boolean autoLockResources() {
        return m_autoLockResources;
    }
    
    /**
     * Returns if the direct publishing of siblings is enabled per default.<p>
     * 
     * @return true if the direct publishing of siblings is enabled per default, otherwise false
     */
    public boolean directPublishSiblings() {
        return m_directPublishSiblings;
    }
    
    /**
     * Returns all instanciated dialog handlers for the workplace.<p>
     * 
     * @return all instanciated dialog handlers for the workplace
     */
    public Map getDialogHandler() {
        return m_dialogHandler;
    }
    
    /**
     * Returns the instanciated dialog handler class for the key or null, if there is no mapping for the key.<p>
     *  
     * @param key the key whose associated value is to be returned
     * @return the instanciated dialog handler class for the key
     */
    public Object getDialogHandler(String key) {
        return m_dialogHandler.get(key);
    }
    
    /**
     * Returns the instanciated editor action handler class.<p>
     * 
     * @return the instanciated editor action handler class
     */
    public I_CmsEditorActionHandler getEditorActionHandler() {
        return m_editorActionHandler;
    }
    
    /**
     * Returns the instanciated editor handler class.<p>
     * 
     * @return the instanciated editor handler class
     */
    public I_CmsEditorHandler getEditorHandler() {
        return m_editorHandler;
    }
    
    /**
     * Returns the value (in kb) for the maximum file upload size.<p>
     * 
     * @return the value (in kb) for the maximum file upload size
     */
    public int getFileMaxUploadSize() {
        return m_fileMaxUploadSize;
    }
    
    /**
     * Returns a list of site folders which generate labeled links.<p>
     * 
     * @return a list of site folders which generate labeled links
     */
    public List getLabelSiteFolders() {
        return m_labelSiteFolders;
    }
    
    /**
     * Returns the instanciated workplace editor manager class.<p>
     * 
     * @return the instanciated workplace editor manager class
     */
    public CmsWorkplaceEditorManager getWorkplaceEditorManager() {
        return m_editorManager;
    }
    
    /**
     * Returns if the user/group icon in the administration view should be shown.<p>
     * @return true if the user/group icon in the administration view should be shown, otherwise false
     */
    public boolean showUserGroupIcon() {
        return m_showUserGroupIcon;
    }

}
