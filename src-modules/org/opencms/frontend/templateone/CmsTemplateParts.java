/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateParts.java,v $
 * Date   : $Date: 2011/03/23 14:52:01 $
 * Version: $Revision: 1.26 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Locale;
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
 * @version $Revision: 1.26 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsTemplateParts implements I_CmsEventListener {

    /** Key name for an illegal key. */
    public static final String KEY_ILLEGAL = "illpart";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateParts.class);

    /** The Singleton instance. */
    private static CmsTemplateParts m_instance;

    /** The internal map of cached template parts. */
    private Map m_parts;

    /**
     * Hidden constructor.<p>
     * 
     * Use the getInstance(CmsJspActionElement) method to get an initialized instance of this class.<p> 
     */
    private CmsTemplateParts() {

        // create new Map
        initPartsMap();
        // add an event listener
        OpenCms.addCmsEventListener(this);
    }

    /**
     * Returns an instance of the class fetched from the application context attribute.<p>
     * 
     * @return an instance of the class
     */
    public static CmsTemplateParts getInstance() {

        if (m_instance == null) {
            // initialize the Singleton instance
            m_instance = new CmsTemplateParts();
        }
        return m_instance;
    }

    /**
     * Sets a part in the cache with the specified key and value.<p>
     * 
     * @param partKey the key to identify the part
     * @param value the value to cache
     */
    public void addPart(String partKey, String value) {

        if (!partKey.equals(KEY_ILLEGAL)) {
            // only store part if valid part key was found
            m_parts.put(partKey, value);
        }
    }

    /**
     * Implements the CmsEvent interface, clears the template parts on publish and clear cache events.<p>
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
            case I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR:
            case I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY:
                // flush Map
                initPartsMap();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_CMSTEMPLATEPARTS_CLEARED_0));
                }
                break;
            default: // no operation
        }
    }

    /**
     * Returns a previously cached part of template one with the specified key, or null, if no part is found.<p>
     * 
     * @param partKey the key to identify the part
     * @return a previously cached part of template one with the specified key
     */
    public String getPart(String partKey) {

        return (String)m_parts.get(partKey);
    }

    /**
     * Returns the content of the specified JSP target file depending on the element and the layout to display.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param layout the layout type of the template to get
     * @param jsp the JSP page to generate the content with
     * 
     * @return the content of the JSP target file
     */
    public String includePart(String target, String element, String layout, CmsJspActionElement jsp) {

        if (OpenCms.getRunLevel() < OpenCms.RUNLEVEL_4_SERVLET_ACCESS) {
            // OpenCms is not in servlet based operating mode, return empty String
            return "";
        }
        String part = null;
        String partKey = "";

        try {
            // generate a unique key for the included part
            partKey = generateKey(
                target,
                element,
                layout,
                jsp.getRequestContext().getLocale(),
                jsp.getRequestContext().currentProject().getUuid());
            // try to get the part
            part = (String)m_parts.get(partKey);
            if (part == null) {
                // part not found, get the content of the JSP element and put it to the Map store
                part = jsp.getContent(target, element, jsp.getRequestContext().getLocale());
                if (part != null && !part.startsWith(CmsMessages.UNKNOWN_KEY_EXTENSION)) {
                    // add part to map if a valid content was found
                    addPart(partKey, part);
                } else {
                    // prevent displaying rubbish
                    part = "";
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_INCLUDE_PART_NOT_FOUND_1, partKey));
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_INCLUDE_PART_FOUND_1, partKey));
            }
        } catch (Throwable t) {
            // catch all errors to avoid displaying rubbish
            part = "";
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_INCLUDE_PART_ERR_2, partKey, t));
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
     * @param locale the locale to use
     * @param project the project id
     * 
     * @return a unique part key
     */
    private String generateKey(String target, String element, String layout, Locale locale, CmsUUID project) {

        try {
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
            partKey.append(locale);
            partKey.append("_");
            partKey.append(project);
            return partKey.toString();
        } catch (Exception e) {
            // error creating key
            return KEY_ILLEGAL;
        }
    }

    /**
     * Initializes (also clears) the internal part cache map.<p>
     * 
     * @return the new created part cache map
     */
    private synchronized Map initPartsMap() {

        LRUMap cacheParts = new LRUMap(512);
        Map oldParts = m_parts;
        m_parts = Collections.synchronizedMap(cacheParts);
        if (oldParts != null) {
            oldParts.clear();
            oldParts = null;
        }
        return m_parts;
    }
}