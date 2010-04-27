/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/CmsPublishProvider.java,v $
 * Date   : $Date: 2010/04/27 07:07:19 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.shared.I_CmsPublishProviderConstants;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * The provider class for the publish module.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public final class CmsPublishProvider extends JavaScriptObject implements I_CmsPublishProviderConstants {

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsPublishProvider INSTANCE;

    /** The publish service instance. */
    private static I_CmsPublishServiceAsync SERVICE;

    /**
     * Prevent instantiation.<p> 
     */
    protected CmsPublishProvider() {

        // empty
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsPublishProvider get() {

        if (INSTANCE == null) {
            INSTANCE = init();
        }
        return INSTANCE;
    }

    /**
     * Returns the publish service instance.<p>
     * 
     * @return the publish service instance
     */
    public static I_CmsPublishServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsPublishService.class);
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
     * 
     * @return the client publish provider instance 
     */
    private static native CmsPublishProvider init() /*-{
        return $wnd[@org.opencms.ade.publish.client.CmsPublishProvider::getDictName()()];
    }-*/;

    /**
     * Returns true if the current user can publish resources even if it leads to broken relations.<p>
     *
     * @return true if the user can publish resources if it leads to broken relations 
     */
    public native boolean canPublishBrokenRelations() /*-{
        return this[@org.opencms.ade.publish.shared.I_CmsPublishProviderConstants::KEY_CAN_PUBLISH_BROKEN_RELATIONS];
    }-*/;
}
