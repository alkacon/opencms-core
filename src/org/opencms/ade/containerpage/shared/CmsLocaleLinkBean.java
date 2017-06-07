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

package org.opencms.ade.containerpage.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean representing a link to a different locale variant.<p>
 */
public class CmsLocaleLinkBean implements IsSerializable {

    /** Error message if the link can't be opened. */
    private String m_error;

    /** The link. */
    private String m_link;

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsLocaleLinkBean() {
        // do nothing
    }

    /**
     * Creates a new instance.<p>
     *
     * @param error the error
     * @param link the link
     */
    protected CmsLocaleLinkBean(String error, String link) {
        m_error = error;
        m_link = link;
    }

    /**
     * Creates a new link bean with an error message.<p>
     *
     * @param errorMsg the error message
     *
     * @return the new bean
     */
    public static CmsLocaleLinkBean error(String errorMsg) {

        return new CmsLocaleLinkBean(errorMsg, null);
    }

    /**
     * Creates a new link bean with a link.<p>
     *
     * @param link the link
     *
     * @return the link bean
     */
    public static CmsLocaleLinkBean link(String link) {

        return new CmsLocaleLinkBean(null, link);
    }

    /**
     * Gets the error message.<p>
     *
     * @return the error message
     */
    public String getError() {

        return m_error;
    }

    /**
     * Gets the link.<p>
     *
     * @return the link
     */
    public String getLink() {

        return m_link;
    }

}
