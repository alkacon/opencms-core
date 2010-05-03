/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/util/Attic/CmsContainerpageProvider.java,v $
 * Date   : $Date: 2010/05/03 07:53:47 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.client.util;

import org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Convenience class to provide server-side information to the client.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public final class CmsContainerpageProvider extends JavaScriptObject implements I_CmsContainerpageProviderConstants {

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsContainerpageProvider INSTANCE;

    /**
     * Prevent instantiation.<p> 
     */
    protected CmsContainerpageProvider() {

        // empty
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsContainerpageProvider get() {

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
    private static native CmsContainerpageProvider init() /*-{
        return $wnd[@org.opencms.ade.containerpage.client.util.CmsContainerpageProvider::getDictName()()];
    }-*/;

    /**
     * Returns the xml-content editor back-link URI.<p>
     * 
     * @return the back-link URI
     */
    public native String getBacklinkUri() /*-{
        return this[@org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants::KEY_BACKLINK_URI];
    }-*/;

    /**
     * Returns the container-page URI.<p>
     * 
     * @return the container-page URI
     */
    public native String getContainerpageUri() /*-{
        return this[@org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants::KEY_CURRENT_CONTAINERPAGE_URI];
    }-*/;

    /**
     * Returns the xml-content editor URI.<p>
     * 
     * @return the xml-content editor URI
     */
    public native String getEditorUri() /*-{
        return this[@org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants::KEY_EDITOR_URI];
    }-*/;

    /**
     * Returns the no-edit reason.<p>
     * 
     * @return the no-edit reason, if empty editing is allowed
     */
    public native String getNoEditReason() /*-{
        return this[@org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants::KEY_NO_EDIT_REASON];
    }-*/;

    /**
     * Returns the request parameters.<p>
     * 
     * @return the request parameters
     */
    public native String getRequestParams() /*-{
        return this[@org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants::KEY_REQUEST_PARAMS];
    }-*/;

    /**
     * Returns the sitemap URI.<p>
     * 
     * @return the sitemap URI
     */
    public native String getSitemapUri() /*-{
        return this[@org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants::KEY_SITEMAP_URI];
    }-*/;

    /**
     * Returns the tool-bar visibility.<p>
     * 
     * @return <code>true</code> if the tool-bar is visible
     */
    public native boolean isToolbarVisible() /*-{
        return this[@org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants::KEY_TOOLBAR_VISIBLE];
    }-*/;
}
