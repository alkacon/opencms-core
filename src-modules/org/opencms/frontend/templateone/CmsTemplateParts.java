/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateParts.java,v $
 * Date   : $Date: 2005/02/17 12:45:43 $
 * Version: $Revision: 1.3 $
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
package org.opencms.frontend.templateone;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import org.apache.commons.collections.FastHashMap;


/**
 * Stores static Strings to generate HTML output parts for the template in a Map.<p>
 * 
 * An instance of this class is stored in the OpenCms runtime properties.<p> 
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 */
public final class CmsTemplateParts implements I_CmsEventListener {
    
    /** Name of the runtime property to store the class instance.<p> */
    public static final String C_RUNTIME_PROPERTY_NAME = "__templateone_parts";
    
    /** Key suffix for a stored object in the offline project.<p> */
    private static final String C_PROJECT_OFFLINE = "off";
    /** Key suffix for a stored object in the online project.<p> */
    private static final String C_PROJECT_ONLINE = "on";
    
    private CmsJspActionElement m_jsp;
    private FastHashMap m_parts;
    private String m_project;
    
    /**
     * Hidden constructor.<p>
     * 
     * Use the getInstance(CmsJspActionElement) method to get an initialized instance of this class.<p> 
     */
    private CmsTemplateParts() {
        
        // create new Map and add an event listener
        m_parts = new FastHashMap();
        OpenCms.addCmsEventListener(this);
    }
    
    /**
     * Returns an instance of the class fetched from the application context attribute.<p>
     * 
     * @param jsp the action element to access the application context
     * @return an instance of the class
     */
    public static CmsTemplateParts getInstance(CmsJspActionElement jsp) {
        
        CmsTemplateParts parts  = (CmsTemplateParts)OpenCms.getRuntimeProperty(C_RUNTIME_PROPERTY_NAME);
        if (parts == null) {
            // instance not found in runtime properties, create new instance
            parts = new CmsTemplateParts();
            OpenCms.setRuntimeProperty(C_RUNTIME_PROPERTY_NAME, parts);
            if (OpenCms.getLog(CmsTemplateParts.class).isDebugEnabled()) {
                OpenCms.getLog(CmsTemplateParts.class).debug("Instance not found in runtime properties, creating new instance");
            }
        } else if (OpenCms.getLog(CmsTemplateParts.class).isDebugEnabled()) {
            OpenCms.getLog(CmsTemplateParts.class).debug("Instance found in runtime properties");
        }
        // set the projext String depending on the current project (offline or online)
        if (jsp.getRequestContext().currentProject().isOnlineProject()) {
            parts.setProject(C_PROJECT_ONLINE);    
        } else {
            parts.setProject(C_PROJECT_OFFLINE);    
        }
        // set the jsp action element
        parts.setJsp(jsp);
        return parts;
    }
    
    /**
     * Implements the CmsEvent interface, clears the template parts on publish and clear cache events.<p>
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(org.opencms.main.CmsEvent event) {
        
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
            case I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY:
                // create empty Map
                m_parts = new FastHashMap();
                // set the new runtime property
                OpenCms.setRuntimeProperty(C_RUNTIME_PROPERTY_NAME, this);
                if (OpenCms.getLog(CmsTemplateParts.class).isDebugEnabled()) {
                    OpenCms.getLog(CmsTemplateParts.class).debug("Cleared stored template parts from runtime properties");
                }
                break;
            default:
                // no operation
        }
    }
    
    /**
     * Returns the content of the specified JSP target file depending on the element and the layout to display.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param layout the layout type of the template to get
     * @return the content of the JSP target file
     */
    public String includePart(String target, String element, String layout) {
        // generate a unique key for the included part
        String partKey = generateKey(target, element, layout);
        // try to get the part String from the stored Map
        String part = "";
        try {
            part = (String)m_parts.get(partKey);
            if (part == null) {
                // part not found, get the content of the JSP element and put it to the Map store
                part = getJsp().getContent(target, element, getJsp().getRequestContext().getLocale());   
                m_parts.setFast(false);
                m_parts.put(partKey, part);
                m_parts.setFast(true);
                if (OpenCms.getLog(CmsTemplateParts.class).isDebugEnabled()) {
                    OpenCms.getLog(CmsTemplateParts.class).debug("Value for key \"" + partKey + "\" not found, including JSP");
                }
                // save modified class to runtime properties
                OpenCms.setRuntimeProperty(C_RUNTIME_PROPERTY_NAME, this);
            } else if (OpenCms.getLog(CmsTemplateParts.class).isDebugEnabled()) {
                OpenCms.getLog(CmsTemplateParts.class).debug("Retrieved value for key \"" + partKey + "\" from Map");
            }
        } catch (Throwable t) {
            if (OpenCms.getLog(CmsTemplateParts.class).isErrorEnabled()) {
                OpenCms.getLog(CmsTemplateParts.class).error("Error while trying to include part: \"" + partKey + "\" from Map\n" + t);
            }
        }
        return part;
    }
    
    /**
     * Generates a part key depending on the included target, the element and the layout do display.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param layout the layout type of the template to get
     * @return a unique part key
     */
    private String generateKey(String target, String element, String layout) {
        if (element == null) {
            // set element name to empty String for key generation
            element = "";    
        }
        // generate the key to identify the current part
        StringBuffer partKey = new StringBuffer(32);
        partKey.append(target);
        partKey.append("_");
        partKey.append(element);
        partKey.append("_");
        partKey.append(layout);
        partKey.append("_");
        partKey.append(getJsp().getRequestContext().getLocale());
        partKey.append("_");
        partKey.append(getProject());
        return partKey.toString();
    }
    
    /**
     * Returns the action element needed to include elements.<p>
     * 
     * @return the action element needed to include elements
     */
    private CmsJspActionElement getJsp() {
        
        return m_jsp;    
    }
    
    /**
     * Returns the project key suffix for the current project.<p>
     * 
     * @return the project key suffix for the current project
     */
    private String getProject() {
        
        return m_project;    
    }
    
    /**
     * Sets the action element needed to include elements.<p>
     * 
     * @param jsp the action element needed to include elements
     */
    private void setJsp(CmsJspActionElement jsp) {
        
        m_jsp = jsp;    
    }
    
    /**
     * Sets the project key suffix for the current project.<p>
     * 
     * @param project the project key suffix for the current project
     */
    private void setProject(String project) {
        
        m_project = project;
    } 
    
}
