/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsCoreProvider.java,v $
 * Date   : $Date: 2010/03/09 10:31:34 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.util;

import org.opencms.gwt.shared.I_CmsCoreProviderConstants;

import com.google.gwt.i18n.client.Dictionary;

/**
 * Client side implementation for {@link org.opencms.gwt.CmsCoreProvider}.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsCoreProvider
 */
public final class CmsCoreProvider implements I_CmsCoreProviderConstants {

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsCoreProvider INSTANCE;

    /** The current context. */
    private String m_context;

    /** The current locale. */
    private String m_locale;

    /** The current site root. */
    private String m_siteRoot;

    /** The current workplace locale. */
    private String m_wpLocale;

    /**
     * Prevent instantiation.<p> 
     */
    private CmsCoreProvider() {

        Dictionary dict = Dictionary.getDictionary(DICT_NAME.replace('.', '_'));
        m_locale = dict.get(KEY_LOCALE);
        m_wpLocale = dict.get(KEY_WP_LOCALE);
        m_context = dict.get(KEY_CONTEXT);
        m_siteRoot = dict.get(KEY_SITE_ROOT);
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsCoreProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsCoreProvider();
        }
        return INSTANCE;
    }

    /**
     * Adds the current site root of this context to the given resource name.<p>
     * 
     * @param sitePath the resource name
     * 
     * @return the translated resource name including site root
     * 
     * @see #removeSiteRoot(String)
     */
    public String addSiteRoot(String sitePath) {

        if (sitePath == null) {
            return null;
        }
        String siteRoot = getAdjustedSiteRoot(getSiteRoot(), sitePath);
        StringBuffer result = new StringBuffer(128);
        result.append(siteRoot);
        if (((siteRoot.length() == 0) || (siteRoot.charAt(siteRoot.length() - 1) != '/'))
            && ((sitePath.length() == 0) || (sitePath.charAt(0) != '/'))) {
            // add slash between site root and resource if required
            result.append('/');
        }
        result.append(sitePath);
        return result.toString();
    }

    /**
     * Returns the adjusted site root for a resource using the provided site root as a base.<p>
     * 
     * Usually, this would be the site root for the current site.
     * However, if a resource from the <code>/system/</code> folder is requested,
     * this will be the empty String.<p>
     * 
     * @param siteRoot the site root of the current site
     * @param resourcename the resource name to get the adjusted site root for
     * 
     * @return the adjusted site root for the resource
     */
    public String getAdjustedSiteRoot(String siteRoot, String resourcename) {

        if (resourcename.startsWith(VFS_PATH_SYSTEM)) {
            return "";
        } else {
            return siteRoot;
        }
    }

    /**
     * Returns the current OpenCms context.<p>
     *
     * @return the current OpenCms context
     */
    public String getContext() {

        return m_context;
    }

    /**
     * Returns the current locale.<p>
     *
     * @return the current locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the current site root.<p>
     *
     * @return the current site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the current workplace locale.<p>
     *
     * @return the current workplace locale
     */
    public String getWpLocale() {

        return m_wpLocale;
    }

    /**
     * Returns an absolute link given a site path.<p>
     * 
     * @param sitePath the site path
     * 
     * @return the absolute link
     */
    public String link(String sitePath) {

        return getContext() + sitePath;
    }

    /**
     * Removes the current site root prefix from the given root path,
     * that is adjusts the resource name for the current site root.<p> 
     * 
     * If the resource name does not start with the current site root,
     * it is left untouched.<p>
     * 
     * @param rootPath the resource name
     * 
     * @return the resource name adjusted for the current site root
     * 
     * @see #addSiteRoot(String)
     */
    public String removeSiteRoot(String rootPath) {

        String siteRoot = getAdjustedSiteRoot(getSiteRoot(), rootPath);
        if ((siteRoot == getSiteRoot())
            && rootPath.startsWith(siteRoot)
            && ((rootPath.length() == siteRoot.length()) || (rootPath.charAt(siteRoot.length()) == '/'))) {
            rootPath = rootPath.substring(siteRoot.length());
        }
        return rootPath;
    }
}