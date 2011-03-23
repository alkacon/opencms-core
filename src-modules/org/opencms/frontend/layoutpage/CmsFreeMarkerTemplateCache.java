/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/layoutpage/CmsFreeMarkerTemplateCache.java,v $
 * Date   : $Date: 2011/03/23 14:50:00 $
 * Version: $Revision: 1.7 $
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

package org.opencms.frontend.layoutpage;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import java.io.IOException;

import org.apache.commons.logging.Log;

/**
 * Cache implementation that provides access to cached FreeMarker templates.<p>
 * 
 * Use this class with caution! It might be moved to the OpenCms core packages in the future.<p>
 * 
 * @author Andreas Zahner
 * 
 * @since 6.2.0
 */
public final class CmsFreeMarkerTemplateCache implements I_CmsEventListener {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFreeMarkerTemplateCache.class);

    /** The singleton instance. */
    private static CmsFreeMarkerTemplateCache m_instance;

    /** The FreeMarker configuration object used for caching. */
    private Configuration m_configuration;

    /**
     * Hidden constructor.<p>
     */
    private CmsFreeMarkerTemplateCache() {

        // initialize the FreeMarker configuration 
        initConfiguration();

        // add an event listener to clear the cache on clear events
        OpenCms.addCmsEventListener(this);
    }

    /**
     * Returns an instance of the class fetched from the application context attribute.<p>
     * 
     * @return an instance of the class
     */
    public static CmsFreeMarkerTemplateCache getInstance() {

        if (m_instance == null) {
            // initialize the Singleton instance
            m_instance = new CmsFreeMarkerTemplateCache();
        }
        return m_instance;
    }

    /**
     * Implements the CmsEvent interface, clears the cached template parts on publish and clear cache events.<p>
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
            case I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR:
            case I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY:
                // flush cache
                clearTemplateCache();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_CMSMACROMANAGER_CLEARED_0));
                }
                break;
            default: // no operation
        }
    }

    /**
     * Returns the template with the given key.<p>
     * 
     * @param cms the OpenCms user context to use
     * @param fileName the absolute path of the template file in the VFS of OpenCms
     * @return the template with the given key
     * @throws CmsException if the specified template file can not be found
     * @throws IOException if the template with the given key can not be found in the cache
     */
    public Template getTemplate(CmsObject cms, String fileName) throws CmsException, IOException {

        if (hasTemplate(fileName)) {
            return m_configuration.getTemplate(getCacheKey(fileName));
        } else {
            String encoding = cms.getRequestContext().getEncoding();
            // get the VFS file content with the FreeMarker macros
            byte[] content = cms.readFile(fileName).getContents();
            StringBuffer fTemplate = new StringBuffer(new String(content, encoding));
            // append the code to execute a single, specified macro of the template file
            fTemplate.append("<@");
            fTemplate.append(CmsMacroWrapperFreeMarker.MACRO_NAME);
            fTemplate.append(" />");
            // put the template to the template cache and return it
            return putTemplate(fileName, fTemplate.toString());
        }
    }

    /**
     * Returns true if the template with the given key exists in the cache, otherwise false.<p>
     * 
     * @param fileName the absolute path of the template file in the VFS of OpenCms
     * @return true if the template with the given key exists in the cache, otherwise false
     */
    public boolean hasTemplate(String fileName) {

        try {
            m_configuration.getTemplate(getCacheKey(fileName));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Clears the template cache, removes all previously cached templates.<p>
     */
    private void clearTemplateCache() {

        m_configuration.clearTemplateCache();
        // the loader has to be reset, too, otherwise clearing the cache will have no effect
        m_configuration.setTemplateLoader(new StringTemplateLoader());
    }

    /**
     * Returns a cache key that is used for the template cache.<p>
     * 
     * @param fileName the absolute path of the template file in the VFS of OpenCms to get the cache key from
     * @return a cache key that is used for the template cache
     */
    private String getCacheKey(String fileName) {

        return String.valueOf(fileName.hashCode());
    }

    /**
     * Initializes the cache objects.<p>
     */
    private void initConfiguration() {

        m_configuration = new Configuration();
        m_configuration.setDefaultEncoding(OpenCms.getSystemInfo().getDefaultEncoding());
        m_configuration.setTemplateLoader(new StringTemplateLoader());
    }

    /**
     * Puts the template with the given key and source code to the template cache.<p>
     * 
     * @param fileName the absolute path of the template file in the VFS of OpenCms
     * @param templateSource the source code of the template
     * @return the template that was stored in the cache
     */
    private Template putTemplate(String fileName, String templateSource) {

        String cacheKey = getCacheKey(fileName);
        ((StringTemplateLoader)m_configuration.getTemplateLoader()).putTemplate(cacheKey, templateSource);
        try {
            return m_configuration.getTemplate(cacheKey);
        } catch (IOException e) {
            return null;
        }
    }

}
