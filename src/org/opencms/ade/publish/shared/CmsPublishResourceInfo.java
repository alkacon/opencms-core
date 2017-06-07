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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A publish resource additional information bean.<p>
 *
 * @since 7.6
 */
public class CmsPublishResourceInfo implements IsSerializable {

    /** Reason value constants, when resources can not be published. */
    public enum Type {

        /** The resource is still used in the online project. */
        BROKENLINK, /** Resource is locked by another user. */
        LOCKED, /** The resource is missing in the online project. */
        MISSING, /** User does not have enough permissions. */
        PERMISSIONS, /** Resource has been already published. */
        PUBLISHED, /** Changed related resource can not be published. */
        RELATED, /** Resource is already in the workflow. */
        WORKFLOW;
    }

    /** Flag to hide the publish resource. */
    private boolean m_hidden;

    /** The additional info type.*/
    private Type m_type;

    /** The additional info.*/
    private String m_value;

    /**
     * Creates a new publish resource additional information bean.<p>
     *
     * @param value the additional info
     * @param type the additional info type
     **/
    public CmsPublishResourceInfo(String value, Type type) {

        m_type = type;
        m_value = value;
    }

    /**
     * Creates a new publish resource additional information bean.<p>
     *
     * @param value the additional info
     * @param type the additional info type
     * @param hidden flag to hide the publish resource
     **/
    public CmsPublishResourceInfo(String value, Type type, boolean hidden) {

        m_type = type;
        m_value = value;
        m_hidden = hidden;
    }

    /**
     * For serialization.<p>
     */
    protected CmsPublishResourceInfo() {

        // for serialization
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public Type getType() {

        return m_type;
    }

    /**
     * Returns the value.<p>
     *
     * @return the value
     */
    public String getValue() {

        return m_value;
    }

    /**
     * Returns if there is a problem type set.<p>
     *
     * @return <code>true</code> if the problem type is set
     */
    public boolean hasProblemType() {

        return m_type != null;
    }

    /**
     * Returns true if the publish resource should be hidden.<p>
     *
     * @return true if the publish resource should be hidden
     */
    public boolean isHidden() {

        return m_hidden;
    }
}
