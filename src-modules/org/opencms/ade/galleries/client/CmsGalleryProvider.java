/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleryProvider.java,v $
 * Date   : $Date: 2010/04/28 10:25:46 $
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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;

import com.google.gwt.i18n.client.Dictionary;

/**
 * Client side implementation for {@link org.opencms.ade.galleries.CmsGalleryProvider}.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.galleries.CmsGalleryProvider
 */
public final class CmsGalleryProvider implements I_CmsGalleryProviderConstants {

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsGalleryProvider INSTANCE;

    /** The dialogmode of the gallery dialog (view, widget, ade, editor, property). */
    private String m_dialogMode;

    /** The gallery path to the selected gallery. */
    private String m_galleryPath;

    /** The gallery tab id to be selected when gallery is opened. */
    private String m_galleryTabId;

    /** The configured tabs as comma separated string. */
    private String m_tabs;

    /** The available resource types as comma separated string. */
    private String m_types;

    /**
     * Prevent instantiation.<p> 
     */
    private CmsGalleryProvider() {

        Dictionary dict = Dictionary.getDictionary(DICT_NAME.replace('.', '_'));
        // gallery path
        m_galleryPath = dict.get(ReqParam.gallerypath.name());
        // gallery path
        m_galleryTabId = dict.get(ReqParam.gallerytabid.name());
        // configured tabs
        m_tabs = dict.get(ReqParam.tabs.name());
        // available resource types
        m_types = dict.get(ReqParam.types.name());
        // dialog mode
        m_dialogMode = dict.get(ReqParam.dialogmode.name());
    }

    //TODO: add gallery relevant members

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsGalleryProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsGalleryProvider();
        }
        return INSTANCE;
    }

    /**
     * Returns the dialogMode.<p>
     *
     * @return the dialogMode
     */
    public String getDialogMode() {

        return m_dialogMode;
    }

    /**
     * Returns the path to the gallery to display.<p>
     *
     * @return the galleryPath
     */
    public String getGalleryPath() {

        return m_galleryPath;
    }

    /**
     * Returns the galleryTabId.<p>
     *
     * @return the galleryTabId
     */
    public String getGalleryTabId() {

        return m_galleryTabId;
    }

    /**
     * Returns the tabs configuration.<p>
     *
     * @return the tabs
     */
    public String getTabs() {

        return m_tabs;
    }

    /**
     * Returns the resource types.<p>
     *
     * @return the types
     */
    public String getTypes() {

        return m_types;
    }
}