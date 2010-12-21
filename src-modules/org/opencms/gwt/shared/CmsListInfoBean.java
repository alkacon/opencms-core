/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/Attic/CmsListInfoBean.java,v $
 * Date   : $Date: 2010/12/21 13:03:43 $
 * Version: $Revision: 1.7 $
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

import org.opencms.db.CmsResourceState;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean holding all info to be displayed in {@link org.opencms.gwt.client.ui.CmsListItemWidget}s.<p>
 * 
 * @see org.opencms.gwt.client.ui.CmsListItemWidget
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsListInfoBean implements IsSerializable {

    /**
     * Enum for the type of page icon which should be displayed.<p>
     */
    public enum PageIcon {
        /** export page icon. */
        export,
        /** no page icon. */
        none,
        /** redirect status icon. */
        redirect,
        /** secure page icon. */
        secure
    }

    /** The additional info. */
    private Map<String, String> m_additionalInfo;

    /** The page icon information: for the type of page icon which should be displayed. */
    private PageIcon m_pageIcon = PageIcon.none;

    /** Returns the resource state. */
    private CmsResourceState m_resourceState;

    /** The sub-title. */
    private String m_subTitle;

    /** The title. */
    private String m_title;

    /** Styles for the additional info values. */
    private Map<String, String> m_valueStyles = new HashMap<String, String>();

    /**
     * Default constructor.<p> 
     */
    public CmsListInfoBean() {

        // empty
    }

    /**
     * Constructor.<p>
     * 
     * @param title the title
     * @param subtitle the subtitle
     * @param additionalInfo the additional info
     */
    public CmsListInfoBean(String title, String subtitle, Map<String, String> additionalInfo) {

        m_title = title;
        m_subTitle = subtitle;
        m_additionalInfo = additionalInfo;
    }

    /**
     * Sets a new additional info.<p> 
     * 
     * @param name the additional info name
     * @param value the additional info value
     */
    public void addAdditionalInfo(String name, String value) {

        getAdditionalInfo().put(name, value);
    }

    /**
     * Returns the additional info.<p>
     *
     * @return the additional info
     */
    public Map<String, String> getAdditionalInfo() {

        if (m_additionalInfo == null) {
            m_additionalInfo = new LinkedHashMap<String, String>();
        }
        return m_additionalInfo;
    }

    /**
     * Returns the additional info with the given name or <code>null</code> if not found.<p>
     * 
     * @param name the additional info name
     * 
     * @return the additional info value
     */
    public String getAdditionalInfo(String name) {

        return getAdditionalInfo().get(name);
    }

    /**
     * Returns the pageIcon.<p>
     *
     * @return the pageIcon
     */
    public PageIcon getPageIcon() {

        return m_pageIcon;
    }

    /**
     * Returns the resourceState.<p>
     *
     * @return the resourceState
     */
    public CmsResourceState getResourceState() {

        return m_resourceState;
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
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Gets the value style for the given additional info item.<p>
     * 
     * @param key the additional info key
     * 
     * @return the style name
     */
    public String getValueStyle(String key) {

        return m_valueStyles.get(key);
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
     * Sets the pageIcon.<p>
     *
     * @param pageIcon the pageIcon to set
     */
    public void setPageIcon(PageIcon pageIcon) {

        m_pageIcon = pageIcon;
    }

    /**
     * Sets the resourceState.<p>
     *
     * @param resourceState the resourceState to set
     */
    public void setResourceState(CmsResourceState resourceState) {

        m_resourceState = resourceState;
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
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the value style for the given additional info item.<p>
     * 
     * @param key the additional info key
     * 
     * @param style the style name for this key
     */
    public void setValueStyle(String key, String style) {

        m_valueStyles.put(key, style);
    }
}
