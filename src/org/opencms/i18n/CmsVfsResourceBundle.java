/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.i18n;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsVfsBundleLoaderXml;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Iterators;

/**
 * Resource bundle which loads its data from a VFS resource.<p>
 */
public class CmsVfsResourceBundle extends ResourceBundle implements I_CmsResourceBundle {

    /**
     * Implementors of this interface are responsible for actually loading the data from the VFS.<p>
     */
    public interface I_Loader {

        /**
         * Loads the data from the VFS.<p>
         * 
         * @param cms the CMS context to use 
         * @param params the VFS bundle parameters 
         * 
         * @return the message bundle data 
         * 
         * @throws Exception if something goes wrong 
         */
        Map<Locale, Map<String, String>> loadData(CmsObject cms, CmsVfsBundleParameters params) throws Exception;
    }

    /** Name constant for the 'properties' vfs bundle type. */
    public static final String TYPE_PROPERTIES = "properties";

    /** Name constant for the 'xml content' vfs bundle type. */
    public static final String TYPE_XML = "xml";

    /** The CMS context to use. */
    protected static CmsObject m_cms;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsResourceBundle.class);

    /** The cache instance used for caching the resource bundle data. */
    private static CmsVfsMemoryObjectCache m_cache = new CmsVfsMemoryObjectCache();

    /** The bundle loader instance to use. */
    protected I_Loader m_loader;

    /** The VFS bundle parameters. */
    protected CmsVfsBundleParameters m_parameters;

    /**
     * Creates a new VFS bundle instance.<p>
     * 
     * @param params the VFS bundle parameters
     */
    public CmsVfsResourceBundle(CmsVfsBundleParameters params) {

        m_parameters = params;
        m_loader = initLoader(params.getType());
    }

    /**
     * Sets the CMS context used by this class.<p>
     * 
     * This can be never called more than once, and is usually called on startup.<p>
     * 
     * @param cms the CMS context to set 
     */
    public static void setCmsObject(CmsObject cms) {

        m_cms = cms;
    }

    /** 
     * Initializes the type given the string value of the type.<p>
     * 
     * @param type a string representation of the type 
     * 
     * @return the actual type object 
     */
    private static I_Loader initLoader(String type) {

        if (TYPE_PROPERTIES.equals(type)) {
            return new CmsVfsBundleLoaderProperties();
        } else if (TYPE_XML.equals(type)) {
            return new CmsVfsBundleLoaderXml();
        } else {
            return new CmsVfsBundleLoaderXml();
        }
    }

    /**
     * @see org.opencms.i18n.I_CmsResourceBundle#getClone()
     */
    public CmsVfsResourceBundle getClone() {

        return new CmsVfsResourceBundle(m_parameters);
    }

    /**
     * @see java.util.ResourceBundle#getKeys()
     */
    @Override
    public Enumeration<String> getKeys() {

        Iterator<String> myKeyIter = handleKeySet().iterator();
        Iterator<String> result = myKeyIter;
        if (parent != null) {
            Iterator<String> parentKeyIter = Iterators.forEnumeration(parent.getKeys());
            result = Iterators.concat(myKeyIter, parentKeyIter);
        }
        return Iterators.asEnumeration(result);
    }

    /**
     * @see java.util.ResourceBundle#getLocale()
     */
    @Override
    public Locale getLocale() {

        return m_parameters.getLocale();
    }

    /**
     * @see org.opencms.i18n.I_CmsResourceBundle#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {

        // ignore 
    }

    /**
     * @see java.util.ResourceBundle#setParent(java.util.ResourceBundle)
     */
    @Override
    public void setParent(ResourceBundle p) {

        super.setParent(p);
    }

    /**
     * Returns the path of the file to read the message data from.<p>
     * 
     * @return the root path of the file containing the message data 
     */
    protected String getFilePath() {

        return m_parameters.getBasePath();
    }

    /**
     * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
     */
    @Override
    protected Object handleGetObject(String key) {

        Map<String, String> messages = getMessagesForLocale();
        return messages.get(key);
    }

    /**
     * @see java.util.ResourceBundle#handleKeySet()
     */
    @Override
    protected Set<String> handleKeySet() {

        Map<String, String> messages = getMessagesForLocale();
        return messages.keySet();
    }

    /**
     * Actually loads the message data from the VFS.<p>
     * 
     * @return a map from locales to message maps 
     * 
     * @throws Exception if something goes wrong 
     */

    /** 
     * Gets the (possibly already cached) message data.<p>
     * 
     * @return the message data 
     */
    private Map<Locale, Map<String, String>> getData() {

        @SuppressWarnings("unchecked")
        Map<Locale, Map<String, String>> result = (Map<Locale, Map<String, String>>)m_cache.getCachedObject(
            m_cms,
            getFilePath());
        if (result == null) {
            try {
                result = m_loader.loadData(m_cms, m_parameters);
                m_cache.putCachedObject(m_cms, getFilePath(), result);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns the message data for this bundle's locale.<p>
     * 
     * @return the message data for this bundle's locale 
     */
    private Map<String, String> getMessagesForLocale() {

        Map<Locale, Map<String, String>> data = getData();
        if (data == null) {
            return Collections.emptyMap();
        }
        Map<String, String> bundleForLocale = data.get(m_parameters.getLocale());
        if (bundleForLocale == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(bundleForLocale);
        }
    }
}
