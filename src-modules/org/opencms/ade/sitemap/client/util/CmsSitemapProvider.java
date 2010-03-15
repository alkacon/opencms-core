/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/util/Attic/CmsSitemapProvider.java,v $
 * Date   : $Date: 2010/03/15 15:12:54 $
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

package org.opencms.ade.sitemap.client.util;

import org.opencms.ade.sitemap.shared.I_CmsSitemapProviderConstants;
import org.opencms.gwt.client.util.CmsStringUtil;

import com.google.gwt.i18n.client.Dictionary;

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
public final class CmsSitemapProvider implements I_CmsSitemapProviderConstants {

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsSitemapProvider INSTANCE;

    /** The current container page type. */
    private int m_cntPageType;

    /** If the current sitemap is editable. */
    private boolean m_editable;

    /** The reason not to be able to edit the sitemap. */
    private String m_noEditReason;

    /** Flag to indicate if the toolbar has to be shown. */
    private boolean m_toolbar;

    /** The current sitemap URI. */
    private String m_uri;

    /**
     * Prevent instantiation.<p> 
     */
    private CmsSitemapProvider() {

        Dictionary dict = Dictionary.getDictionary(DICT_NAME.replace('.', '_'));
        m_toolbar = Boolean.parseBoolean(dict.get(KEY_TOOLBAR));
        m_uri = dict.get(KEY_URI_SITEMAP);
        m_noEditReason = dict.get(KEY_EDIT);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_noEditReason)) {
            m_editable = true;
        }
        m_cntPageType = Integer.parseInt(dict.get(KEY_TYPE_CNTPAGE));
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsSitemapProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsSitemapProvider();
        }
        return INSTANCE;
    }

    /**
     * Returns the cntPageType.<p>
     *
     * @return the cntPageType
     */
    public int getCntPageType() {

        return m_cntPageType;
    }

    /**
     * Returns the reason not to be able to edit the sitemap.<p>
     *
     * @return the reason not to be able to edit the sitemap
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Returns the current sitemap uri.<p>
     *
     * @return the current sitemap uri
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * Checks if the current sitemap is editable.<p>
     *
     * @return <code>true</code> if the current sitemap is editable
     */
    public boolean isEditable() {

        return m_editable;
    }

    /**
     * Checks if the toolbar has to be displayed.<p>
     *
     * @return <code>true</code> if the toolbar has to be displayed
     */
    public boolean showToolbar() {

        return m_toolbar;
    }

}