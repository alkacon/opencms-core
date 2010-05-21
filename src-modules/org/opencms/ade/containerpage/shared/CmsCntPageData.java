/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/shared/Attic/CmsCntPageData.java,v $
 * Date   : $Date: 2010/05/21 13:20:08 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.containerpage.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Convenience class to provide server-side information to the client.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public final class CmsCntPageData implements IsSerializable {

    /** Name of the used dictionary. */
    public static final String DICT_NAME = "org_opencms_ade_containerpage";

    /** The editor back-link URI. */
    private static final String BACKLINK_URI = "/system/modules/org.opencms.ade.containerpage/editor-backlink.html";

    /** The xml-content editor URI. */
    private static final String EDITOR_URI = "/system/workplace/editors/editor.jsp";

    /** The current container page URI. */
    private String m_cntPageUri;

    /** The map of available types and their new resource id's. */
    private Map<String, String> m_newTypes;

    /** The reason why the user is not able to edit the current container page. */
    private String m_noEditReason;

    /** The original request parameters. */
    private String m_requestParams;

    /** The current sitemap URI. */
    private String m_sitemapUri;

    /** Flag to indicate if the toolbar is visible. */
    private boolean m_toolbarVisible;

    /**
     * Constructor.<p>
     * 
     * @param cntPageUri the current container page URI
     * @param noEditReason the reason why the current user is not allowed to edit the current container page
     * @param requestParams the original request parameters
     * @param sitemapUri the current sitemap URI
     * @param newTypes the map of available types and their new resource id's
     * @param toolbarVisible if the toolbar is visible
     */
    public CmsCntPageData(
        String cntPageUri,
        String noEditReason,
        String requestParams,
        String sitemapUri,
        Map<String, String> newTypes,
        boolean toolbarVisible) {

        m_cntPageUri = cntPageUri;
        m_noEditReason = noEditReason;
        m_requestParams = requestParams;
        m_sitemapUri = sitemapUri;
        m_newTypes = newTypes;
        m_toolbarVisible = toolbarVisible;
    }

    /**
     * Serialization constructor.<p> 
     */
    protected CmsCntPageData() {

        // empty
    }

    /**
     * Returns the xml-content editor back-link URI.<p>
     * 
     * @return the back-link URI
     */
    public String getBacklinkUri() {

        return BACKLINK_URI;
    }

    /**
     * Returns the container-page URI.<p>
     * 
     * @return the container-page URI
     */
    public String getContainerpageUri() {

        return m_cntPageUri;
    }

    /**
     * Returns the xml-content editor URI.<p>
     * 
     * @return the xml-content editor URI
     */
    public String getEditorUri() {

        return EDITOR_URI;
    }

    /**
     * Returns the map of available types and their new resource id's.<p>
     * 
     * @return the map of available types and their new resource id's
     */
    public Map<String, String> getNewTypes() {

        return m_newTypes;
    }

    /**
     * Returns the no-edit reason.<p>
     * 
     * @return the no-edit reason, if empty editing is allowed
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Returns the request parameters.<p>
     * 
     * @return the request parameters
     */
    public String getRequestParams() {

        return m_requestParams;
    }

    /**
     * Returns the sitemap URI.<p>
     * 
     * @return the sitemap URI
     */
    public String getSitemapUri() {

        return m_sitemapUri;
    }

    /**
     * Returns the tool-bar visibility.<p>
     * 
     * @return <code>true</code> if the tool-bar is visible
     */
    public boolean isToolbarVisible() {

        return m_toolbarVisible;
    }
}
