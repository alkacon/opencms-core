/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.relations;

import org.opencms.util.CmsUUID;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Immutable bean representing most of the information in a CmsLink.
 *
 */
public class CmsLinkInfo {

    /** Empty link. */
    public static final CmsLinkInfo EMPTY = new CmsLinkInfo(CmsUUID.getNullUUID(), null, null, null, null, true);

    /** The anchor. */
    private String m_anchor;

    /** Cached hash code. */
    private transient int m_hashCode;

    /** Indicates whether the link is internal or not. */
    private boolean m_internal;

    /** The query. */
    private String m_query;

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The link target. */
    private String m_target;

    /** Cached toString() result. */
    private transient String m_toStringRepr;

    /** The relation type. */
    private CmsRelationType m_type;

    /**
     * Creates a new instance.
     *
     * @param structureId the structure id
     * @param target the link target
     * @param query the query
     * @param anchor the anchor
     * @param type the type
     * @param internal true if the link is internal
     */
    public CmsLinkInfo(
        CmsUUID structureId,
        String target,
        String query,
        String anchor,
        CmsRelationType type,
        boolean internal) {

        m_structureId = structureId;
        m_target = target;
        m_query = query;
        m_anchor = anchor;
        m_type = type;
        m_internal = internal;
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        // don't use the type in the hash code
        m_hashCode = hashCodeBuilder.append(m_structureId).append(m_target).append(m_query).append(m_anchor).append(
            m_internal).toHashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        // equals() method auto-generated by Eclipse. Does *not* compare the type.

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CmsLinkInfo other = (CmsLinkInfo)obj;
        if (m_anchor == null) {
            if (other.m_anchor != null) {
                return false;
            }
        } else if (!m_anchor.equals(other.m_anchor)) {
            return false;
        }
        if (m_internal != other.m_internal) {
            return false;
        }
        if (m_query == null) {
            if (other.m_query != null) {
                return false;
            }
        } else if (!m_query.equals(other.m_query)) {
            return false;
        }
        if (m_structureId == null) {
            if (other.m_structureId != null) {
                return false;
            }
        } else if (!m_structureId.equals(other.m_structureId)) {
            return false;
        }
        if (m_target == null) {
            if (other.m_target != null) {
                return false;
            }
        } else if (!m_target.equals(other.m_target)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the anchor.
     *
     * @return the anchor
     */
    public String getAnchor() {

        return m_anchor;
    }

    /**
     * Gets the query
     *
     * @return the query
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Gets the structure id.
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {

        return m_target;
    }

    /**
     * Gets the relation type.
     *
     * @return the type
     */
    public CmsRelationType getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_hashCode;
    }

    /**
     * Checks whether the link is internal.
     *
     * @return true if this is an internal
     */
    public boolean isInternal() {

        return m_internal;
    }

    /**
     * Converts this to a CmsLink.
     *
     * @return a new CmsLink instance with the information from this bean
     */
    public CmsLink toLink() {

        if (this == EMPTY) {
            return null;
        }
        return new CmsLink(this);

    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        if (m_toStringRepr == null) {
            m_toStringRepr = ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
        return m_toStringRepr;
    }

}
