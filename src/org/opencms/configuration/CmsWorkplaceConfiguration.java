/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsWorkplaceConfiguration.java,v $
 * Date   : $Date: 2004/03/07 19:22:02 $
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

package org.opencms.configuration;

import org.opencms.db.CmsExportPoint;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceView;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * Import/Export master configuration class.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsWorkplaceConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
   
    /** The node name of the master workplace node */
    protected static final String N_WORKPLACE = "workplace";
    
    /** The main workplace handler node name */
    protected static final String N_DIALOGHANDLERS = "dialoghandlers";
    
    /** Indivividual workplace handler node name */
    protected static final String N_DIALOGHANDLER = "dialoghandler";      
    
    /** The name of the editor handler node */
    protected static final String N_EDITORHANDLER = "editorhandler";
    
    /** The name of the editor action node */
    protected static final String N_EDITORACTION = "editoraction";
    
    /** The name of the autolock node */
    protected static final String N_AUTOLOCK = "autolock";
    
    /** The name of the "user management enabled" node */
    protected static final String N_ENABLEUSERMGMT = "enableusermanagement";
    
    /** The name of the "max file upload size" node */
    protected static final String N_MAXUPLOADSIZE = "maxfileuploadsize";
    
    /** The name of the "labled folders" node */
    protected static final String N_LABLEDFOLDERS = "labledfolders";    
    
    /** The name of the node for the default locale */
    protected static final String N_DEFAULTLOCALE = "defaultlocale";
    
    /** The configured workplace manager */
    private CmsWorkplaceManager m_workplaceManager;
    
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsWorkplaceConfiguration() {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Empty constructor called on " + this);
        }     
    } 

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {                        
        // add factory create method for "real" instance creation
        digester.addFactoryCreate("*/" + N_WORKPLACE, CmsWorkplaceConfiguration.class);                            
        // call this method at the end of the workplace configuration
        digester.addCallMethod("*/" + N_WORKPLACE, "initializeFinished");          
        // add this configuration object to the calling configuration after is has been processed
        digester.addSetNext("*/" + N_WORKPLACE, "addConfiguration");        
        
        // creation of the import/export manager        
        digester.addObjectCreate("*/" + N_WORKPLACE, CmsWorkplaceManager.class);                         
        // import/export manager finished
        digester.addSetNext("*/" + N_WORKPLACE, "setWorkplaceManager");
        
        // add default locale rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_DEFAULTLOCALE, "setDefaultLocale", 0);
        
        // add rules for dialog handlers
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER, "addDialogHandler");

        // add rules for editor handler
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_EDITORHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_EDITORHANDLER, "setEditorHandler");        

        // add rules for editor action handler
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_EDITORACTION, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_EDITORACTION, "setEditorAction");        
        
        // add rules for the workplace views  
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_VIEWS + "/" + N_VIEW, "addView", 3);        
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_VIEWS + "/" + N_VIEW, 0, A_KEY);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_VIEWS + "/" + N_VIEW, 1, A_URI);        
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_VIEWS + "/" + N_VIEW, 2, A_ORDER);
        
        // add rules for the workplace export points 
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, "addExportPoint", 2);        
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, 0, A_URI);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, 1, A_DESTINATION);
        
        // add autolock rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_AUTOLOCK, "setAutoLock", 0);
        
        // add user management enabled rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_ENABLEUSERMGMT, "setUserManagementEnabled", 0);
        
        // add max file upload size rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_MAXUPLOADSIZE, "setFileMaxUploadSize", 0);
        
        // add labled folders rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_LABLEDFOLDERS + "/" + N_RESOURCE, "addLabledFolder", 1);        
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_LABLEDFOLDERS + "/" + N_RESOURCE, 0, A_URI);
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        // generate workplace node and subnodes
        Element workplaceElement = parent.addElement(N_WORKPLACE);
        Iterator i;
        
        // add default locale
        workplaceElement.addElement(N_DEFAULTLOCALE)
            .setText(m_workplaceManager.getDefaultLocale().toString());   
        
        // add <dialoghandlers> subnode
        Element dialogElement = workplaceElement.addElement(N_DIALOGHANDLERS);
        Map dialogs = m_workplaceManager.getDialogHandler();
        i = dialogs.keySet().iterator();
        while (i.hasNext()) {          
            String name = (String)i.next();
            dialogElement.addElement(N_DIALOGHANDLER)
                .addAttribute(A_CLASS, dialogs.get(name).getClass().getName());
        }             
        
        // add miscellaneous editor subnodes
        workplaceElement.addElement(N_EDITORHANDLER)
            .addAttribute(A_CLASS, m_workplaceManager.getEditorHandler().getClass().getName());
        workplaceElement.addElement(N_EDITORACTION)
            .addAttribute(A_CLASS, m_workplaceManager.getEditorActionHandler().getClass().getName());
        
        // add <views> subnode
        Element viewsElement = workplaceElement.addElement(N_VIEWS);
        i = m_workplaceManager.getViews().iterator();
        while (i.hasNext()) {
            CmsWorkplaceView view = (CmsWorkplaceView)i.next();
            viewsElement.addElement(N_VIEW)
            .addAttribute(A_KEY, view.getKey())
            .addAttribute(A_URI, view.getUri())
            .addAttribute(A_ORDER, view.getOrder().toString());                
        }            
                
        // add <exportpoints> subnide
        Element resourceloadersElement = workplaceElement.addElement(N_EXPORTPOINTS);
        Set points = m_workplaceManager.getExportPoints();
        i = points.iterator();
        while (i.hasNext()) {          
            CmsExportPoint point = (CmsExportPoint)i.next();
            resourceloadersElement.addElement(N_EXPORTPOINT)
                .addAttribute(A_URI, point.getUri())
                .addAttribute(A_DESTINATION, point.getDestination());
        }     

        // add miscellaneous configuration nodes
        workplaceElement.addElement(N_AUTOLOCK)
            .setText(new Boolean(m_workplaceManager.autoLockResources()).toString());        
        workplaceElement.addElement(N_ENABLEUSERMGMT)
            .setText(new Boolean(m_workplaceManager.showUserGroupIcon()).toString());   
        workplaceElement.addElement(N_MAXUPLOADSIZE)
            .setText(new Integer(m_workplaceManager.getFileMaxUploadSize()).toString());         
        
        // add <labledfolders> resource list
        Element labledElement = workplaceElement.addElement(N_LABLEDFOLDERS);        
        i =  m_workplaceManager.getLabelSiteFolders().iterator();
        while (i.hasNext()) {
            labledElement.addElement(N_RESOURCE).addAttribute(A_URI, (String)i.next());        
        }
        
        // return the configured node
        return workplaceElement;
    }
    
    /**
     * Returns the initialized workplace manager.<p>
     * 
     * @return the initialized workplace manager
     */
    public CmsWorkplaceManager getWorkplaceManager() {
        return m_workplaceManager;
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#initialize()
     */
    public void initialize() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Workplace config     : starting");
        }           
    }
    
    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Workplace config     : finished");
        }            
    }   
    
    /**
     * Sets the generated workplace manager.<p>
     * 
     * @param manager the workplace manager to set
     */
    public void setWorkplaceManager(CmsWorkplaceManager manager) {
        m_workplaceManager = manager;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Workplace init       : finished");
        }
    }
}
