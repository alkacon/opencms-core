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

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains the data on which a workflow action should act (usually, a list of resources).<p>
 */
public class CmsWorkflowActionParams implements IsSerializable {

    /** The list of structure ids of resources to publish .*/
    private List<CmsUUID> m_publishIds = Lists.newArrayList();

    /** The list of structure ids of resources to remove. */
    private List<CmsUUID> m_removeIds = Lists.newArrayList();

    /** The publish list token. */
    private CmsPublishListToken m_token;

    /**
     * Creates a new instance based on a publish list token.<p>
     *
     * @param token the publish list token
     */
    public CmsWorkflowActionParams(CmsPublishListToken token) {

        m_token = token;
    }

    /**
     * Creates a new instance based on lists of resources.<p>
     *
     * @param publishIds the list of structure ids of resources to publish
     * @param removeIds the list of structure ids of resources to remove
     */
    public CmsWorkflowActionParams(List<CmsUUID> publishIds, List<CmsUUID> removeIds) {

        m_publishIds = publishIds;
        m_removeIds = removeIds;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsWorkflowActionParams() {

        // do nothing
    }

    /**
     * Returns the publishIds.<p>
     *
     * @return the publishIds
     */
    public List<CmsUUID> getPublishIds() {

        return m_publishIds;
    }

    /**
     * Returns the removeIds.<p>
     *
     * @return the removeIds
     */
    public List<CmsUUID> getRemoveIds() {

        return m_removeIds;
    }

    /**
     * Gets the publish list token,  if it is set, else returns null.<p>
     *
     * The publish list token is a bean which can be used to reconstruct a publish list on the server side.
     * It is used instead of sending the list of resources when that list is deemed to long to be displayed.<p>
     *
     * @return the publish list token
     */
    public CmsPublishListToken getToken() {

        return m_token;
    }

}
