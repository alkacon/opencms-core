/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceManager.java,v $
 * Date   : $Date: 2004/02/13 13:45:33 $
 * Version: $Revision: 1.4 $
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

import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.I_CmsLocaleHandler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;
import org.opencms.workplace.editor.CmsWorkplaceEditorManager;
import org.opencms.workplace.editor.I_CmsEditorActionHandler;
import org.opencms.workplace.editor.I_CmsEditorHandler;

import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Manages the global OpenCms workplace settings for all users.<p>
 * 
 * This class reads the settings from the "opencms.properties" and stores them in member variables.
 * For each setting one or more get methods are provided.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @since 5.3.1
 */
public final class CmsWorkplaceManager implements I_CmsLocaleHandler {
    
    private boolean m_autoLockResources;
    private Map m_dialogHandler;
    private boolean m_directPublishSiblings;
    private I_CmsEditorHandler m_editorHandler;
    private I_CmsEditorActionHandler m_editorActionHandler;
    private int m_fileMaxUploadSize;
    private List m_labelSiteFolders;
    private boolean m_showUserGroupIcon;
    private CmsWorkplaceEditorManager m_editorManager;
    
    /** Set of installed workplace locales */
    private Set m_locales;
    
    /**
     * Creates an initialized instance and fills all member variables with their configuration values.<p>
     * 
     * @param configuration the OpenCms configuration
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @throws Exception if a configuration goes wrong
     */
    private CmsWorkplaceManager(ExtendedProperties configuration, CmsObject cms) throws Exception {
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
        
        initHandler(cms);
    }

    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#initHandler(org.opencms.file.CmsObject)
     */
    public void initHandler(CmsObject cms) {
        // initialize the workplace locale set
        m_locales = initWorkplaceLocales(cms);
    }    
    
    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#getLocale(org.opencms.file.CmsRequestContext)
     */
    public Locale getLocale(CmsRequestContext context) {
        
        // try to read locale from session
        HttpServletRequest req = (HttpServletRequest)context.getRequest().getOriginalRequest();
        HttpSession session = req.getSession(false);
        if (session != null) {
            CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
            if (settings != null) {
                return settings.getUserSettings().getLocale();
            }
        }        
        
        // no session available, try to read the locale form the user additional info
        Locale locale = null;
        if (! context.currentUser().isGuestUser()) {
            // check user settings only for "real" users
            Hashtable userInfo = (Hashtable)context.currentUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS);  
            if (userInfo != null) {
                locale = CmsLocaleManager.getLocale((String)userInfo.get(I_CmsConstants.C_START_LOCALE));
            }    
        }
        List acceptedLocales = (new CmsAcceptLanguageHeaderParser(req)).getAcceptedLocales();
        if ((locale != null) && (! acceptedLocales.contains(locale))) {
            acceptedLocales.add(0, locale);
        }
        locale = OpenCms.getLocaleManager().getFirstMatchingLocale(acceptedLocales, m_locales);
        
        // if no locale was found, use the default
        if (locale == null) {
            locale = getDefaultLocale();
        }
        return locale;
    }
    
    /**
     * Initilizes the workplace locale set.<p>
     * 
     * Currently, this is defined by the existence of a special folder 
     * <code>/system/workplace/locales/{locale-name}/".
     * This is likley to change in future implementations.
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @return the workplace locale set
     */
    private Set initWorkplaceLocales(CmsObject cms) {
        m_locales = new HashSet();
        List localeFolders;
        try {
            localeFolders = cms.getSubFolders(I_CmsWpConstants.C_VFS_PATH_LOCALES);
        } catch (CmsException e) {
            OpenCms.getLog(this).error("Unable to read locales folder " + I_CmsWpConstants.C_VFS_PATH_LOCALES, e);
            localeFolders = new ArrayList();
        }
        Iterator i = localeFolders.iterator();
        while (i.hasNext()) {
            CmsFolder folder = (CmsFolder)i.next();
            Locale locale = CmsLocaleManager.getLocale(folder.getName());
            // add locale
            m_locales.add(locale);
            // TODO: Check if it's a good idea to add the general locales here automatically like this: 
            // add less specialized locale
            m_locales.add(new Locale(locale.getLanguage(), locale.getCountry()));    
            // add even less specialized locale            
            m_locales.add(new Locale(locale.getLanguage()));         
        }        
        return m_locales;
    }
    
    /**
     * Returns the set of available workplace locales.<p>
     * 
     * Please note: Be careful not to modify the returned Set as it is not a clone.<p>
     * 
     * @return the set of available workplace locales
     */
    public Set getLocales() {
        return m_locales;        
    }
    
    /**
     * Returns the Workplace default locale.<p>
     * 
     * @return  the Workplace default locale
     */
    public Locale getDefaultLocale() {
        // TODO: This should be made configurable
        return I_CmsWpConstants.C_DEFAULT_LOCALE;
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
