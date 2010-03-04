/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/Attic/CmsListInfoBean.java,v $
 * Date   : $Date: 2010/03/04 15:19:01 $
 * Version: $Revision: 1.1 $
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

package org.opencms.gwt.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * A bean holding all info to be displayed in CmsListItems.<p>
 * 
 * @see org.opencms.gwt.client.ui.CmsListItem
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsListInfoBean {

    /** The title. */
    private String m_title;

    /** The sub-title. */
    private String m_subTitle;

    /** The additional info. */
    private Map<String, String> m_additionalInfo;

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Returns the sub-title.<p>
     *
     * @return the sub-title
     */
    public String getSubTitle() {

        return m_subTitle;
    }

    /**
     * Sets the sub-title.<p>
     *
     * @param subTitle the sub-title to set
     */
    public void setSubTitle(String subTitle) {

        m_subTitle = subTitle;
    }

    /**
     * Returns the additional info.<p>
     *
     * @return the additional info
     */
    public Map<String, String> getAdditionalInfo() {

        return m_additionalInfo;
    }

    /**
     * Sets the additional info.<p>
     *
     * @param additionalInfo the additional info to set
     */
    public void setAdditionalInfo(Map<String, String> additionalInfo) {

        m_additionalInfo = additionalInfo;
    }

    /**
     * Adds another info to the item.<p>
     * 
     * @param title the info title
     * @param value the info value
     */
    public void addAdditionalInfo(String title, String value) {

        if (m_additionalInfo == null) {
            m_additionalInfo = new HashMap<String, String>();
        }
        m_additionalInfo.put(title, value);
    }

}
