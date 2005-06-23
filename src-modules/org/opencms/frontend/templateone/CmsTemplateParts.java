/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateParts.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.12 $
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

import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

/**
 * Stores static Strings to generate HTML output parts for the template in a Map.<p>
 * 
 * An instance of this class is stored in the OpenCms runtime properties.<p> 
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsTemplateParts implements I_CmsEventListener {

    /** Name of the runtime property to store the class instance.<p> */
    public static final String C_RUNTIME_PROPERTY_NAME = "__templateone_parts";

    /** Key suffix for a stored object in the offline project.<p> */
    private static final String C_PROJECT_OFFLINE = "off";
    /** Key suffix for a stored object in the online project.<p> */
    private static final String C_PROJECT_ONLINE = "on";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateParts.class);

    private CmsJspActionElement m_jsp;
    private Map m_parts;
    private String m_project;

    /**
     * Hidden constructor.<p>
     * 
     * Use the getInstance(CmsJspActionElement) method to get an initialized instance of this class.<p> 
     */
    private CmsTemplateParts() {

        // create new Map
        LRUMap cacheParts = new LRUMap(256);
        m_parts = Collections.synchronizedMap(cacheParts);
        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            if ((OpenCms.getMemoryMonitor() != null) && OpenCms.getMemoryMonitor().enabled()) {
                // map must be of type "LRUMap" so that memory monitor can access all information
                OpenCms.getMemoryMonitor().register(CmsTemplateParts.class.getName() + "." + "m_parts", cacheParts);
            }
        }

        // add an event listener
        OpenCms.addCmsEventListener(this);
    }

    /**
     * Returns an instance of the class fetched from the application context attribute.<p>
     * 
     * @param jsp the action element to access the application context
     * @return an instance of the class
     */
    public static CmsTemplateParts getInstance(CmsJspActionElement jsp) {

        CmsTemplateParts parts = (CmsTemplateParts)OpenCms.getRuntimeProperty(C_RUNTIME_PROPERTY_NAME);
        if (parts == null) {
            // instance not found in runtime properties, create new instance
            parts = new CmsTemplateParts();
            OpenCms.setRuntimeProperty(C_RUNTIME_PROPERTY_NAME, parts);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_CMSTEMPLATEPARTS_NOT_FOUND_0));
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_CMSTEMPLATEPARTS_FOUND_0));
        }
        // set the project String depending on the current project (offline or online)
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
                // flush Map
                m_parts.clear();
                // set the new runtime property
                OpenCms.setRuntimeProperty(C_RUNTIME_PROPERTY_NAME, this);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_CMSTEMPLATEPARTS_CLEARED_0));
                }
                break;
            default:
        // no operation
        }
    }

    /**
     * Returns a previously cached part of template one with the specified key, or null, if no part is found.<p>
     * 
     * @param partKey the key to identify the part
     * @return a previously cached part of template one with the specified key
     */
    public Object getPart(String partKey) {

        return m_parts.get(partKey);
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
                if (part != null && !part.startsWith(CmsMessages.C_UNKNOWN_KEY_EXTENSION)) {
                    // only add part to map if a valid content was found
                    m_parts.put(partKey, part);
                    // save modified class to runtime properties
                    OpenCms.setRuntimeProperty(C_RUNTIME_PROPERTY_NAME, this);
                } else {
                    // prevent displaying rubbish
                    part = "";
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_INCLUDE_PART_NOT_FOUND_1, partKey));
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_INCLUDE_PART_FOUND_1, partKey));
            }
        } catch (Throwable t) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_INCLUDE_PART_ERR_2, partKey, t));
            }
        }
        return part;
    }

    /**
     * Sets a part in the cache with the specified key and value.<p>
     * 
     * @param partKey the key to identify the part
     * @param value the value to cache
     */
    public void setPart(String partKey, Object value) {

        m_parts.put(partKey, value);
        // save modified class to runtime properties
        OpenCms.setRuntimeProperty(C_RUNTIME_PROPERTY_NAME, this);
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
