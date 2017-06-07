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

package org.opencms.ade.publish.shared;

import org.opencms.util.CmsUUID;

import com.google.common.collect.ComparisonChain;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A project bean.<p>
 *
 * @since 7.6
 */
public class CmsProjectBean implements IsSerializable, Comparable<CmsProjectBean> {

    /** The default group name. */
    private String m_defaultGroupName;

    /** The project description. */
    private String m_description;

    /** The project id.*/
    private CmsUUID m_id;

    /** The project name.*/
    private String m_name;

    /** The rank which is used for sorting projects. */
    private int m_rank = 1000;

    /** The project type. */
    private int m_type;

    /**
     * Creates a new project bean.<p>
     *
     * @param id the project id
     * @param type the project type
     * @param name the project name
     * @param description the project description
     **/
    public CmsProjectBean(CmsUUID id, int type, String name, String description) {

        m_id = id;
        m_name = name;
        m_type = type;
        m_description = description;
    }

    /**
     * For serialization.<p>
     */
    protected CmsProjectBean() {

        // for serialization
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsProjectBean otherProject) {

        return ComparisonChain.start().compare(m_rank, otherProject.getRank()).compare(
            m_name,
            otherProject.getName()).result();

    }

    /**
     * The default name to use for publish groups computed from this project, if no other name is available.<p>
     *
     * @return the default publish group name
     */
    public String getDefaultGroupName() {

        return m_defaultGroupName;
    }

    /**
     * Returns the project description.<p>
     *
     * @return the project description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the sorting rank.<p>
     *
     * @return the sorting rank
     */
    public int getRank() {

        return m_rank;
    }

    /**
     * Returns the project type.<p>
     *
     * @return the project type
     */
    public int getType() {

        return m_type;
    }

    /**
     * Returns if the project is of the type workflow project.<p>
     *
     * @return <code>true</code> if the project is of the type workflow project
     */
    public boolean isWorkflowProject() {

        return m_type == 2;
    }

    /**
     * Sets the default publish group name.<p>
     *
     * @param defaultGroupName the default publish group name
     */
    public void setDefaultGroupName(String defaultGroupName) {

        m_defaultGroupName = defaultGroupName;
    }

    /**
     * Sets the sorting rank.<p>
     *
     * @param rank the sorting rank
     */
    public void setRank(int rank) {

        m_rank = rank;
    }
}
