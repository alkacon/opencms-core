/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapProvider.java,v $
 * Date   : $Date: 2010/04/19 06:39:10 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.I_CmsSitemapProviderConstants;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.util.CmsStringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Client side implementation for {@link org.opencms.ade.sitemap.CmsSitemapProvider}.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsSitemapProvider
 */
public final class CmsSitemapProvider extends JavaScriptObject implements I_CmsSitemapProviderConstants {

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsSitemapProvider INSTANCE;

    /** The sitemap service instance. */
    private static I_CmsSitemapServiceAsync SERVICE;

    /**
     * Prevent instantiation.<p> 
     */
    protected CmsSitemapProvider() {

        // empty
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsSitemapProvider get() {

        if (INSTANCE == null) {
            INSTANCE = init();
        }
        return INSTANCE;
    }

    /**
     * Returns the sitemap service instance.<p>
     * 
     * @return the sitemap service instance
     */
    public static I_CmsSitemapServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsSitemapService.class);
        }
        return SERVICE;
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
    private static native CmsSitemapProvider init() /*-{
        return $wnd[@org.opencms.ade.sitemap.client.CmsSitemapProvider::getDictName()()];
    }-*/;

    /**
     * Returns the cntPageType.<p>
     *
     * @return the cntPageType
     */
    public native int getCntPageType() /*-{
        return this[@org.opencms.ade.sitemap.shared.I_CmsSitemapProviderConstants::KEY_TYPE_CNTPAGE];
    }-*/;

    /**
     * Returns the reason not to be able to edit the sitemap.<p>
     *
     * @return the reason not to be able to edit the sitemap
     */
    public native String getNoEditReason() /*-{
        return this[@org.opencms.ade.sitemap.shared.I_CmsSitemapProviderConstants::KEY_EDIT];
    }-*/;

    /**
     * Returns the current sitemap uri.<p>
     *
     * @return the current sitemap uri
     */
    public native String getUri() /*-{
        return this[@org.opencms.ade.sitemap.shared.I_CmsSitemapProviderConstants::KEY_URI_SITEMAP];
    }-*/;

    /**
     * Checks if the current sitemap is editable.<p>
     *
     * @return <code>true</code> if the current sitemap is editable
     */
    public boolean isEditable() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(getNoEditReason());
    }

    /**
     * Checks if the toolbar has to be displayed.<p>
     *
     * @return <code>true</code> if the toolbar has to be displayed
     */
    public native boolean showToolbar() /*-{
        return this[@org.opencms.ade.sitemap.shared.I_CmsSitemapProviderConstants::KEY_TOOLBAR];
    }-*/;
}