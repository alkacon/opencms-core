/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsCoreProvider.java,v $
 * Date   : $Date: 2010/04/15 08:11:16 $
 * Version: $Revision: 1.4 $
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
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Client side implementation for {@link org.opencms.gwt.CmsCoreProvider}.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsCoreProvider
 */
public final class CmsCoreProvider extends JavaScriptObject implements I_CmsCoreProviderConstants {

    /** Path to the editor. */
    public static final String VFS_PATH_EDITOR = "/system/workplace/editors/editor.jsp";

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsCoreProvider INSTANCE;

    /** The core service instance. */
    private I_CmsCoreServiceAsync m_coreSvc;

    /**
     * Prevent instantiation.<p> 
     */
    protected CmsCoreProvider() {

        // empty
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsCoreProvider get() {

        if (INSTANCE == null) {
            INSTANCE = init();
        }
        return INSTANCE;
    }

    /**
     * Returns the json object name.<p>
     * 
     * @return the json object name
     */
    // only used in native code
    @SuppressWarnings("unused")
    private static String getDictName() {

        return DICT_NAME.replace('.', '_');
    }

    /**
     * Initializes the data from the host page.<p>
     */
    private static native CmsCoreProvider init() /*-{
        return $wnd[@org.opencms.gwt.client.util.CmsCoreProvider::getDictName()()];
    }-*/;

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
    public native String getContext() /*-{
        return this[@org.opencms.gwt.shared.I_CmsCoreProviderConstants::KEY_CONTEXT];
    }-*/;

    /**
     * Returns the core service instance.<p>
     * 
     * @return the core service instance
     */
    public I_CmsCoreServiceAsync getCoreService() {

        if (m_coreSvc == null) {
            m_coreSvc = GWT.create(I_CmsCoreService.class);
        }
        return m_coreSvc;
    }

    /**
     * Returns the current locale.<p>
     *
     * @return the current locale
     */
    public native String getLocale() /*-{
        return this[@org.opencms.gwt.shared.I_CmsCoreProviderConstants::KEY_LOCALE];
    }-*/;

    /**
     * Returns the current site root.<p>
     *
     * @return the current site root
     */
    public native String getSiteRoot() /*-{
        return this[@org.opencms.gwt.shared.I_CmsCoreProviderConstants::KEY_SITE_ROOT];
    }-*/;

    /**
     * Returns the current uri.<p>
     *
     * @return the current uri
     */
    public native String getUri() /*-{
        return this[@org.opencms.gwt.shared.I_CmsCoreProviderConstants::KEY_URI];
    }-*/;

    /**
     * Returns the current workplace locale.<p>
     *
     * @return the current workplace locale
     */
    public native String getWpLocale() /*-{
        return this[@org.opencms.gwt.shared.I_CmsCoreProviderConstants::KEY_WP_LOCALE];
    }-*/;

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