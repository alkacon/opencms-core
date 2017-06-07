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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This class represents the result of a "return link" lookup, which is what happens if the user
 * clicks the "go to last page" button in the sitemap editor. It contains a link string (possibly null)
 * and a status to indicate possible errors.<p>
 *
 * @since 8.0.0
 */
public class CmsReturnLinkInfo implements IsSerializable {

    /**
     * The moved status.<p>
     */
    public enum Status {
        /** The link was successfully looked up. */
        ok, /** The resource(s) was not found. */
        notfound
    }

    /** The return link (null if not found). */
    private String m_link;

    /** The status of the lookup operation. */
    private Status m_status;

    /**
     * Creates a new instance.<p>
     *
     * @param link the return link
     * @param status the link lookup status
     */
    public CmsReturnLinkInfo(String link, Status status) {

        m_link = link;
        m_status = status;
    }

    /**
     * Protected default constructor for serialization.<p>
     */
    protected CmsReturnLinkInfo() {

        // do nothing
    }

    /**
     * Returns the return link.<p>
     *
     * @return the return link
     */
    public String getLink() {

        return m_link;
    }

    /**
     * Returns the link lookup status.<p>
     *
     * @return the link lookup status
     */
    public Status getStatus() {

        return m_status;
    }

}
