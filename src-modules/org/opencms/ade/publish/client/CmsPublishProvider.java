/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/CmsPublishProvider.java,v $
 * Date   : $Date: 2010/04/15 08:12:39 $
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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishServiceAsync;

import com.google.gwt.core.client.GWT;

/**
 * The provider class for the publish module.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public final class CmsPublishProvider {

    /** The name of the dictionary to use. */
    public static final String DICT_NAME = "org.opencms.ade.publish";

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsPublishProvider INSTANCE;

    /** The publish service instance. */
    private I_CmsPublishServiceAsync m_publishSvc;

    /**
     * Prevent instantiation.<p> 
     */
    private CmsPublishProvider() {

        // empty
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsPublishProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsPublishProvider();
        }
        return INSTANCE;
    }

    /**
     * Returns the publish service instance.<p>
     * 
     * @return the publish service instance
     */
    public I_CmsPublishServiceAsync getPublishService() {

        if (m_publishSvc == null) {
            m_publishSvc = GWT.create(I_CmsPublishService.class);
        }
        return m_publishSvc;
    }
}
