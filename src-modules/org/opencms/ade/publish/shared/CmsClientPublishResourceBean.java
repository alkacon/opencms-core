/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/shared/Attic/CmsClientPublishResourceBean.java,v $
 * Date   : $Date: 2010/03/29 08:47:35 $
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

package org.opencms.ade.publish.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean representing a resource in the publish list.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsClientPublishResourceBean implements IsSerializable {

    /** The icon of the resource. */
    private String m_icon;

    /** The id of the resource. */
    private String m_id;

    /** The info type of the resource. */
    private String m_infoType;

    /** The info description of the resource. */
    private String m_infoValue;

    /** The name of the resource. */
    private String m_name;

    /** The problem of the resource. */
    private String m_problem;

    /** The related resources of this resource. */
    private List<CmsClientPublishResourceBean> m_related = new ArrayList<CmsClientPublishResourceBean>();

    /** The state of the resource. */
    private String m_state;

    /** The title of the resource. */
    private String m_title;

    /** The default constructor. */
    public CmsClientPublishResourceBean() {

    }

    /**
     * Adds a new related resource.<p>
     * 
     * @param clientResourceBean the bean for the new related resource
     */
    public void addRelated(CmsClientPublishResourceBean clientResourceBean) {

        m_related.add(clientResourceBean);
    }

    /**
     * Gets the icon of the resource. <p>
     * 
     * @return the URL of the icon
     */
    public String getIcon() {

        return m_icon;
    }

    /**
     * Gets the UUID of the resource.
     *  
     * @return an UUID string
     */
    public String getId() {

        return m_id;
    }

    /**
     * Gets the info type of the resource.<p>
     * 
     * @return the info type of the resource 
     */
    public String getInfoType() {

        return m_infoType;
    }

    /**
     * Gets the info value of the resource.<p>
     * 
     * @return the info value
     */
    public String getInfoValue() {

        return m_infoValue;
    }

    /**
     * Gets the name of the resource. <p>
     * 
     * @return the name of the resource
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the problem of the resource. <p>
     * 
     * @return a problem string
     */
    public String getProblem() {

        return m_problem;
    }

    /**
     * Gets the related resources of this resource.<p> 
     * 
     * @return a list of related resource beans 
     */
    public List<CmsClientPublishResourceBean> getRelated() {

        return m_related;
    }

    /**
     * Returns the state code for the given resource.<p>
     * 
     * The codes are N, D, C for new, deleted, and changed.
     * 
     * @return a state code
     */
    public String getState() {

        return m_state;
    }

    /**
     * Returns the title of the resource. <p>
     * 
     * @return a title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the icon of the resource.<p>
     * 
     * @param icon the new icon URL
     */
    public void setIcon(String icon) {

        m_icon = icon;
    }

    /**
     * Sets the id of the resource.<p>
     * 
     * @param id the new UUID
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * Sets the info type of the resource. <p>
     * 
     * @param infoType the new info type
     */
    public void setInfoType(String infoType) {

        m_infoType = infoType;

    }

    /**
     * Sets the info value of the resource.<p>
     * 
     * @param infoValue the info value of the resource
     */
    public void setInfoValue(String infoValue) {

        m_infoValue = infoValue;
    }

    /**
     * Sets the name of the resource.<p>
     * 
     * @param name the new name of the resource
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the problem of the resource.<p>
     * 
     * @param problem the problem of the resource
     */
    public void setProblem(String problem) {

        m_problem = problem;
    }

    /**
     * Sets the related resources of the resource.<p>
     * 
     * @param related the related resources 
     */
    public void setRelated(List<CmsClientPublishResourceBean> related) {

        m_related = related;
    }

    /**
     * Sets the state of the resource.<p>
     * @param state a state code 
     */
    public void setState(String state) {

        m_state = state;
    }

    /**
     * Sets the title of the resource.<p>
     * 
     * @param title the new title 
     */
    public void setTitle(String title) {

        m_title = title;
    }
}
