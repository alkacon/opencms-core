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

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A class which represents a list of publish groups to display to the user for selection.<p>
 *
 * It may be the case that there are too many resources to display. In this case, the instance
 * will not contain the publish groups, but instead a publish list token which can be used later
 * to reconstruct the publish list.<p>
 *
 */
public class CmsPublishGroupList implements IsSerializable {

    /** The list of publish groups. */
    private List<CmsPublishGroup> m_groups = Lists.newArrayList();

    /** Workflow ID which, if not null, is used to override the workflow selected by default in the publish dialog. */
    private String m_overrideWorkflowId;

    /** The publish token which can be used to reconstruct the publish list. */
    private CmsPublishListToken m_token;

    /** The message to display if the publish list token is being used instead of the publish groups list. */
    private String m_tooManyResourcesMessage = "";

    /**
     * Default constructor.<p>
     */
    public CmsPublishGroupList() {

        // do nothing
    }

    /**
     * Constructs a new instance with a publish list token and an empty group list.<p<
     *      *
     * @param token the publish list token to use
     */
    public CmsPublishGroupList(CmsPublishListToken token) {

        m_token = token;
    }

    /**
     * Gets the list of publish groups.<p>
     *
     * @return the publish groups
     */
    public List<CmsPublishGroup> getGroups() {

        return m_groups;
    }

    /**
     * Gets the override workflow id.<p>
     *
     * If this is not null, this indicates that the publish groups were fetched for a different workflow than that selected by default
     * in the publish dialog, and that the publish dialog should change its selected workflow accordingly.<p>
     *
     * @return the override workflow id
     */
    public String getOverrideWorkflowId() {

        return m_overrideWorkflowId;
    }

    /**
     * Returns the token.<p>
     *
     * @return the token
     */
    public CmsPublishListToken getToken() {

        return m_token;
    }

    /**
     * Gets the message which should be displayed if the token is being used instead of the list of publish groups.<p>
     *
     * @return the message
     */
    public String getTooManyResourcesMessage() {

        return m_tooManyResourcesMessage;
    }

    /**
     * Sets the publish groups.<p>
     *
     * @param groups the list of publish groups
     */
    public void setGroups(List<CmsPublishGroup> groups) {

        m_groups = groups;
    }

    /**
     * Sets the Override workflow.<p>
     *
     * @param id the id of the override workflow
     */
    public void setOverrideWorkflowId(String id) {

        m_overrideWorkflowId = id;
    }

    /**
     * Sets the tooManyResourcesMessage.<p>
     *
     * @param tooManyResourcesMessage the tooManyResourcesMessage to set
     */
    public void setTooManyResourcesMessage(String tooManyResourcesMessage) {

        m_tooManyResourcesMessage = tooManyResourcesMessage;
    }

}
