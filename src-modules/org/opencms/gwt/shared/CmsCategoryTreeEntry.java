/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/Attic/CmsCategoryTreeEntry.java,v $
 * Date   : $Date: 2010/05/18 12:31:13 $
 * Version: $Revision: 1.3 $
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

package org.opencms.gwt.shared;

import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive category tree entry.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsCategoryTreeEntry extends CmsCategory {

    /** The serialization id. */
    private static final long serialVersionUID = 3787936028869506095L;

    /** The category's base path. */
    private String m_basePath;

    /** The children. */
    private List<CmsCategoryTreeEntry> m_children;

    /** The path of the category. */
    private String m_path;

    /**
     * Constructor for serialization.<p>
     * 
     * @throws Exception will never happen 
     */
    public CmsCategoryTreeEntry()
    throws Exception {

        this("");
    }

    /**
     * Clone constructor.<p>
     * 
     * @param category the category to clone
     * 
     * @throws Exception will never happen 
     */
    public CmsCategoryTreeEntry(CmsCategory category)
    throws Exception {

        super(category.getId(), category.getRootPath(), category.getTitle(), category.getDescription(), "/");
        m_path = category.getPath();
        m_basePath = category.getBasePath();
    }

    /**
     * Constructor for serialization.<p>
     * 
     * @param path the category path 
     * 
     * @throws Exception will never happen 
     */
    public CmsCategoryTreeEntry(String path)
    throws Exception {

        super(null, CmsCategoryService.CENTRALIZED_REPOSITORY, null, null, null);
        m_path = path;
        m_basePath = CmsCategoryService.CENTRALIZED_REPOSITORY;
    }

    /**
     * Adds a child entry.<p>
     * 
     * @param child the child to add
     */
    public void addChild(CmsCategoryTreeEntry child) {

        if (m_children == null) {
            m_children = new ArrayList<CmsCategoryTreeEntry>();
        }
        m_children.add(child);
    }

    /**
     * Returns the basePath.<p>
     *
     * @return the basePath
     */
    @Override
    public String getBasePath() {

        return m_basePath;
    }

    /**
     * Returns the children.<p>
     *
     * @return the children
     */
    public List<CmsCategoryTreeEntry> getChildren() {

        return m_children;
    }

    /**
     * Returns the path.<p>
     *
     * @return the path
     */
    @Override
    public String getPath() {

        return m_path;
    }

    /**
     * Sets the children.<p>
     *
     * @param children the children to set
     */
    public void setChildren(List<CmsCategoryTreeEntry> children) {

        m_children = children;
    }
}