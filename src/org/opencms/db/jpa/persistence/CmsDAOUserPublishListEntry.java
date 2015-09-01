/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db.jpa.persistence;

import org.opencms.util.CmsUUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.Index;

import com.google.common.base.Objects;

/**
 * Entity class for JPA access to user publish list entries.<p>
 */
@Entity
@Table(name = "CMS_USER_PUBLISH_LIST")
@IdClass(value = CmsDAOUserPublishListEntry.PK.class)
public class CmsDAOUserPublishListEntry {

    /** The primary key class for user publish list entries. */
    public static class PK {

        /** The structure id. */
        protected String m_structureId;

        /** The user id. */
        protected String m_userId;

        /**
         * Empty default constructor.<p>
         */
        public PK() {

            // do nothing
        }

        /**
         * Creates a new instance.<p>
         *
         * @param userId the user id
         * @param structureId the structure id
         */
        public PK(CmsUUID userId, CmsUUID structureId) {

            m_userId = userId.toString();
            m_structureId = structureId.toString();
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object other) {

            if (!(other instanceof PK)) {
                return false;
            }
            PK otherPK = (PK)other;
            return Objects.equal(m_userId, otherPK.m_userId) && Objects.equal(m_structureId, otherPK.m_structureId);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return Objects.hashCode(m_userId, m_structureId);
        }
    }

    /** The dateChanged field. */
    @Column(name = "DATE_CHANGED", nullable = false)
    protected long m_dateChanged;

    /** The structure id field. */
    @Id
    @Column(name = "STRUCTURE_ID", nullable = false, length = 36)
    @Index(name = "CMS_USERPUBLIST_IDX_02")
    protected String m_structureId;

    /** The user id field. */
    @Id
    @Column(name = "USER_ID", nullable = false, length = 36)
    @Index(name = "CMS_USERPUBLIST_IDX_01")
    protected String m_userId;

    /**
     * Empty default constructor.<p>
     */
    public CmsDAOUserPublishListEntry() {

        // do nothing
    }

    /**
     * Creates a new entry.<p>
     *
     * @param userId the user id
     * @param structureId the structure id
     * @param dateChanged the modification date
     */
    public CmsDAOUserPublishListEntry(CmsUUID userId, CmsUUID structureId, long dateChanged) {

        m_userId = userId.toString();
        m_structureId = structureId.toString();
        m_dateChanged = dateChanged;
    }

    /**
     * Gets the modification date.<p>
     *
     * @return the modification date
     */
    public long getDateChanged() {

        return m_dateChanged;
    }

    /**
     * Gets the structure id of the resource.<p>
     *
     * @return the structure id
     */
    public String getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the user id of the publish list entry.<p>
     *
     * @return the user id
     */
    public String getUserId() {

        return m_userId;
    }

}
