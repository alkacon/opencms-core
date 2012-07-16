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

@Entity
@Table(name = "CMS_REWRITES")
public class CmsDAORewriteAlias {

    @Id
    @Column(name = "ID", nullable = false, length = 36)
    protected String m_id;

    @Column(name = "ALIAS_MODE", nullable = false)
    protected int m_mode;

    @Column(name = "PATTERN", nullable = false, length = 255)
    protected String m_pattern;

    @Column(name = "REPLACEMENT", nullable = false, length = 255)
    protected String m_replacement;

    @Index(name = "CMS_REWRITES_IDX_01")
    @Column(name = "SITE_ROOT", nullable = false, length = 64)
    protected String m_siteRoot;

    public String getId() {

        return m_id;
    }

    public int getMode() {

        return m_mode;
    }

    public String getPattern() {

        return m_pattern;
    }

    public String getReplacement() {

        return m_replacement;
    }

    public String getSiteRoot() {

        return m_siteRoot;
    }

    public void setId(String id) {

        m_id = id;
    }

    public void setMode(int mode) {

        m_mode = mode;
    }

    public void setPattern(String pattern) {

        m_pattern = pattern;
    }

    public void setReplacement(String replacement) {

        m_replacement = replacement;
    }

    public void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }
}
