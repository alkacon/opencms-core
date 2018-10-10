/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data which needs to be passed to the edit handler when using the 'new' button for an editable list element.<p>
 */
public class CmsEditHandlerData implements IsSerializable {

    /** The client id of the editable element. */
    private String m_clientId;

    /** The option chosen by the user. */
    private String m_option;

    /** The container page's structure id. */
    private CmsUUID m_pageContextId;

    /** The string containing the request parameters. */
    private String m_requestParams;

    /**
     * Default constructor for serialization.<p>
     */
    public CmsEditHandlerData() {

        // hidden default constructor for serialization
    }

    /**
     * Creates a new instance.<p>
     *
     * @param clientId the client id of the editable element
     * @param option the option chosen by the user
     * @param pageContextId the structure id of the container page
     * @param requestParams the string containing the request parameters
     */
    public CmsEditHandlerData(String clientId, String option, CmsUUID pageContextId, String requestParams) {

        m_clientId = clientId;
        m_option = option;
        m_pageContextId = pageContextId;
        m_requestParams = requestParams;

    }

    /**
     * Gets the client id of the element.<p>
     *
     * @return the client id of the element
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * Gets the option chosen by the user.<p>
     *
     * @return the option chosen by the user
     */
    public String getOption() {

        return m_option;
    }

    /**
     * Gets the container page's structure id.<p>
     *
     * @return the container page's structure id
     */
    public CmsUUID getPageContextId() {

        return m_pageContextId;
    }

    /**
     * Gets the string containing the request parameters from the URL.<p>
     *
     * @return the string with the request parameters from the URL
     */
    public String getRequestParams() {

        return m_requestParams;
    }

}
