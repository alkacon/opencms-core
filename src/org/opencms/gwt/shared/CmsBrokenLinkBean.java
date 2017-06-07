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

import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean which represents either the source or the target of a broken link.<p>
 *
 * @since 8.0.0
 */
public class CmsBrokenLinkBean implements IsSerializable {

    /** The child beans (usually represent link targets). */
    private List<CmsBrokenLinkBean> m_children = new ArrayList<CmsBrokenLinkBean>();

    /** The broken link info. */
    private Map<String, String> m_info = new LinkedHashMap<String, String>();

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The title. */
    private String m_subtitle;

    /** The subtitle. */
    private String m_title;

    /** The resource type. */
    private String m_type;

    /**
     * Constructor without a type parameter.<p>
     * @param structureId the structure id
     * @param title the title
     * @param subtitle the subtitle
     */
    public CmsBrokenLinkBean(CmsUUID structureId, String title, String subtitle) {

        this(structureId, title, subtitle, null);

    }

    /**
     * Constructor.<p>
     *
     * @param structureId the structure id
     * @param title the title
     * @param subtitle the subtitle
     * @param type the resource type
     */
    public CmsBrokenLinkBean(CmsUUID structureId, String title, String subtitle, String type) {

        m_title = title;
        m_subtitle = subtitle;
        m_type = type;
        m_structureId = structureId;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsBrokenLinkBean() {

        // do nothing
    }

    /**
     * Adds a child bean to this bean.<p>
     *
     * The child usually represents a link target.<p>
     *
     * @param bean the bean to add as a sub-bean
     */
    public void addChild(CmsBrokenLinkBean bean) {

        getChildren().add(bean);
    }

    /**
     * Adds optional page information to the broken link bean.<p>
     *
     * @param name the info name
     * @param value the info
     */
    public void addInfo(String name, String value) {

        m_info.put(name, value);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        return (obj instanceof CmsBrokenLinkBean) && ((CmsBrokenLinkBean)obj).m_structureId.equals(m_structureId);
    }

    /**
     * Returns the child beans of this bean.<p>
     *
     * @return the list of child beans
     */
    public List<CmsBrokenLinkBean> getChildren() {

        return m_children;
    }

    /**
     * Returns the additional link info.<p>
     *
     * @return the broken link info
     */
    public Map<String, String> getInfo() {

        return m_info;
    }

    /**
     * Returns the sub-title of the bean.<p>
     *
     * @return the sub-title
     */
    public String getSubTitle() {

        return m_subtitle;

    }

    /**
     * Returns the title of the bean.<p>
     *
     * @return the title of the bean
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the resource type.<p>
     *
     * @return the resource type
     */
    public String getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_structureId.hashCode();
    }

}
