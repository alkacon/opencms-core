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

package org.opencms.loader;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.I_CmsMessageContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A template context is basically a named path to a template JSP, which 
 * has both an internal name used as a key, and a user-readable, localizable name. It 
 * also has a reference to the template context provider which produced it.<p>
 */
public class CmsTemplateContext {

    /** Map of client variants, with the variant names used as keys. */
    private Map<String, CmsClientVariant> m_clientVariants;

    /** A flag which indicates whether this template context has been manually selected rather than automatically determined. */
    private boolean m_forced;

    /** The key used for identifying the template context. */
    private String m_key;

    /** The message container for the user-readable name. */
    private I_CmsMessageContainer m_messageContainer;

    /** The template context provider which created this context. */
    private I_CmsTemplateContextProvider m_provider;

    /** The path to the template. */
    private String m_templatePath;

    /**
     * Constructor.<p>
     * 
     * @param key the internal name 
     * @param path the template path 
     * @param container the message container for the name
     * @param provider the template context provider
     */
    public CmsTemplateContext(
        String key,
        String path,
        CmsMessageContainer container,
        I_CmsTemplateContextProvider provider) {

        this(key, path, container, provider, new ArrayList<CmsClientVariant>(), false);
    }

    /**
     * Constructor.<p>
     * 
     * @param key the internal name 
     * @param path the template path 
     * @param container the message container for the name
     * @param provider the template context provider
     * @param clientVariants the client variants 
     * @param forced true if the template context is forced to a specific value instead of automatically determined
     * 
     */
    public CmsTemplateContext(
        String key,
        String path,
        I_CmsMessageContainer container,
        I_CmsTemplateContextProvider provider,
        Collection<CmsClientVariant> clientVariants,
        boolean forced) {

        m_key = key;
        m_templatePath = path;
        m_messageContainer = container;
        m_provider = provider;
        m_forced = forced;
        m_clientVariants = new HashMap<String, CmsClientVariant>();
        for (CmsClientVariant variant : clientVariants) {
            m_clientVariants.put(variant.getName(), variant);
        }
    }

    /**
     * Gets the map of client variants.<p>
     * 
     * Client variants are specialized variants of a template context which are only used by the container page editor
     * for preview purposes.
     * 
     * @return the client variants 
     */
    public Map<String, CmsClientVariant> getClientVariants() {

        return m_clientVariants;
    }

    /**
     * Gets the internal name used as a key.<p>
     * 
     * @return the internal name 
     */
    public String getKey() {

        return m_key;
    }

    /** 
     * Gets the localized name for a given locale.<p>
     * 
     * @param locale the locale for which we want the name  
     * 
     * @return the localized name  
     */
    public String getLocalizedName(Locale locale) {

        if (m_messageContainer != null) {
            return m_messageContainer.key(locale);
        }
        return m_key;
    }

    /**
     * Gets the message container for the user-readable name.<p>
     * 
     * @return the message container 
     */
    public I_CmsMessageContainer getMessageContainer() {

        return m_messageContainer;
    }

    /**
     * Gets the template context provider which produced this template context.<p>
     * 
     * @return the template context provider 
     */
    public I_CmsTemplateContextProvider getProvider() {

        return m_provider;
    }

    /** 
     * Gets the path to the template.<p>
     * 
     * @return the path to the template 
     */
    public String getTemplatePath() {

        return m_templatePath;
    }

    /**
     * Return true if the template context was not automatically determined.<p>
     * 
     * @return true if the template context was not automatically determined 
     */
    public boolean isForced() {

        return m_forced;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getKey();
    }
}
