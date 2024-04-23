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

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsResourceListInfo;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Information about how an element is reused, to be displayed in a reuse warning dialog before editing.
 */
public class CmsReuseInfo implements IsSerializable {

    /** The number of places where the element is used. */
    private int m_count;

    /** The element info for the container element itself. */
    private CmsListInfoBean m_elementInfo;

    /** The message to display. */
    private String m_message;

    /** The dialog title. */
    private String m_title;

    /** The resources infos for the places where the element is used. */
    private List<CmsResourceListInfo> m_usageInfos;

    /**
     * Creates a new instance.
     */
    public CmsReuseInfo() {

        // default constructor for serialization
    }

    /**
     * Creates a new instance.
     * @param elementInfo the resource info for the container element itself
     * @param pageInfos the resource infos for the pages where the element is used
     * @param message the message to display
     * @param title the title for the dialog
     * @param count the number of places where the element is used
     */
    public CmsReuseInfo(
        CmsListInfoBean elementInfo,
        List<CmsResourceListInfo> pageInfos,
        String message,
        String title,
        int count) {

        m_elementInfo = elementInfo;
        m_usageInfos = pageInfos;
        m_message = message;
        m_title = title;
        m_count = count;
    }

    /**
     * Gets the number of places where the element is used.
     *
     * @return the number of places where the element is used
     */
    public int getCount() {

        return m_count;
    }

    /**
     * Gets the element info for the container element itself.
     *
     * @return the element info for the container element
     */
    public CmsListInfoBean getElementInfo() {

        return m_elementInfo;
    }

    /**
     * Gets the message to display.
     *
     * @return the message to display
     */
    public String getMessage() {

        return m_message;
    }


    /**
     * Gets the dialog title.
     *
     * @return the dialog title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Gets the resource info beans for the places where the element is used.
     *
     * @return the resource info beans for the places where the element is used
     */
    public List<CmsResourceListInfo> getUsageInfos() {

        return m_usageInfos;
    }

}
