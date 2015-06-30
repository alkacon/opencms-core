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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.Index;

/**
 * JPA access bean for rewrite aliases.<p>
 */
@Entity
@Table(name = "CMS_REWRITES")
public class CmsDAORewriteAlias {

    /**
     * The id.<p>
     */
    @Id
    @Column(name = "ID", nullable = false, length = 36)
    protected String m_id;

    /** The alias mode. */
    @Column(name = "ALIAS_MODE", nullable = false)
    protected int m_mode;

    /** The alias pattern. */
    @Column(name = "PATTERN", nullable = false, length = 255)
    protected String m_pattern;

    /** The alias replacement. */
    @Column(name = "REPLACEMENT", nullable = false, length = 255)
    protected String m_replacement;

    /** The alias site root. */
    @Index(name = "CMS_REWRITES_IDX_01")
    @Column(name = "SITE_ROOT", nullable = false, length = 64)
    protected String m_siteRoot;

    /**
     * Gets the alias id.<p>
     *
     * @return the alias id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Gets the alias mode.<p>
     *
     * @return the alias mode
     */
    public int getMode() {

        return m_mode;
    }

    /**
     * Gets the alias pattern.<p>
     *
     * @return the alias pattern
     */
    public String getPattern() {

        return m_pattern;
    }

    /**
     * Gets the replacement string.<p>
     *
     * @return the replacement string
     */
    public String getReplacement() {

        return m_replacement;
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
     * Sets the alias id.<p>
     *
     * @param id the alias id
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * Sets the alias mode.<p>
     *
     * @param mode the alias mode
     */
    public void setMode(int mode) {

        m_mode = mode;
    }

    /**
     * Sets the alias pattern.<p>
     *
     * @param pattern the alias pattern
     */
    public void setPattern(String pattern) {

        m_pattern = pattern;
    }

    /**
     * Sets the replacement string.<p>
     *
     * @param replacement the replacement string
     */
    public void setReplacement(String replacement) {

        m_replacement = replacement;
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
