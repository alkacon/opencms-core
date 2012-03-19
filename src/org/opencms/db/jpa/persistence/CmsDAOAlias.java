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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.Index;

import com.google.common.base.Objects;

/**
 * JPA entity class for the 'CMS_ALIASES' table.<p>
 */
@Entity
@Table(name = "CMS_ALIASES")
@IdClass(CmsDAOAlias.CmsDAOAliasPK.class)
public class CmsDAOAlias {

    /**
     * The primary key for the aliases table.<p>
     */
    public static class CmsDAOAliasPK {

        /** The alias path. */
        private String m_aliasPath;

        /** The site root. */
        private String m_siteRoot;

        /**
         * The default constructor.<p>
         */
        public CmsDAOAliasPK() {

            // do nothing
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {

            if (!(o instanceof CmsDAOAliasPK)) {
                return false;
            }
            CmsDAOAliasPK other = (CmsDAOAliasPK)o;
            return Objects.equal(m_aliasPath, other.m_aliasPath) && Objects.equal(m_siteRoot, other.m_siteRoot);
        }

        /**
         * Gets the alias path.
         *
         * @return the alias path
         */
        public String getAliasPath() {

            return m_aliasPath;
        }

        /**
         * Gets the site root.<p>
         *
         * @return the site root
         */
        public String getSiteRoot() {

            return m_siteRoot;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return Objects.hashCode(m_aliasPath, m_siteRoot);
        }

        /**
         * Sets the alias path.<p>
         *
         * @param aliasPath the new alias path
         */
        public void setAliasPath(String aliasPath) {

            m_aliasPath = aliasPath;
        }

        /**
         * Sets the site root.<p>
         *
         * @param siteRoot the site root
         */
        public void setSiteRoot(String siteRoot) {

            m_siteRoot = siteRoot;
        }
    }

    /**
     * The alias path.
     */
    @Id
    @Column(name = "path", nullable = false, length = 256)
    protected String m_aliasPath;

    /** The alias mode. */
    @Basic
    @Column(name = "alias_mode", nullable = false)
    protected int m_mode;

    /** The alias site root. */
    @Id
    @Column(name = "site_root", nullable = false, length = 64)
    protected String m_siteRoot;

    /** The alias structure id. */
    @Basic
    @Index(name = "CMS_ALIASES_IDX_1")
    @Column(name = "structure_id", nullable = false, length = 36)
    protected String m_structureId;

    /**
     * Returns the aliasPath.<p>
     *
     * @return the aliasPath
     */
    public String getAliasPath() {

        return m_aliasPath;
    }

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public int getMode() {

        return m_mode;
    }

    /**
     * Returns the siteRoot.<p>
     *
     * @return the siteRoot
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the structureId.<p>
     *
     * @return the structureId
     */
    public String getStructureId() {

        return m_structureId;
    }

    /**
     * Sets the aliasPath.<p>
     *
     * @param aliasPath the aliasPath to set
     */
    public void setAliasPath(String aliasPath) {

        m_aliasPath = aliasPath;
    }

    /**
     * Sets the mode.<p>
     *
     * @param mode the mode to set
     */
    public void setMode(int mode) {

        m_mode = mode;
    }

    /**
     * Sets the siteRoot.<p>
     *
     * @param siteRoot the siteRoot to set
     */
    public void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }

    /**
     * Sets the structureId.<p>
     *
     * @param structureId the structureId to set
     */
    public void setStructureId(String structureId) {

        m_structureId = structureId;
    }

}
