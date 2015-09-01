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

package org.opencms.acacia.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A bean which contains the data for a choice menu entry.<p>
 */
public class CmsChoiceMenuEntryBean {

    /** The child entries of this choice. */
    protected List<CmsChoiceMenuEntryBean> m_children = new ArrayList<CmsChoiceMenuEntryBean>();

    /** The parent of this entry. */
    protected CmsChoiceMenuEntryBean m_parent;

    /** The path component (attribute id) of this menu entry. */
    protected String m_pathComponent;

    /**
     * Creates a new choice menu entry bean.
     *
     * @param pathComponent the path component of the choice (attribute id)
     */
    public CmsChoiceMenuEntryBean(String pathComponent) {

        // path component may be null for dummy root entries
        m_pathComponent = pathComponent;
    }

    /**
     * Adds a new child entry to this bean and returns it.<p>
     *
     * @param pathComponent the path component of the child
     *
     * @return the new child
     */
    public CmsChoiceMenuEntryBean addChild(String pathComponent) {

        assert pathComponent != null;
        CmsChoiceMenuEntryBean child = new CmsChoiceMenuEntryBean(pathComponent);
        m_children.add(child);
        child.m_parent = this;
        return child;
    }

    /**
     * Gets the list of children of this entry.<p>
     *
     * @return the list of children
     */
    public List<CmsChoiceMenuEntryBean> getChildren() {

        return m_children;
    }

    /**
     * Gets the help text for the menu entry.<p>
     *
     * @param widgetService the widget service to ask for labels
     *
     * @return the help text
     */
    public String getHelp(I_CmsWidgetService widgetService) {

        return widgetService.getAttributeHelp(m_pathComponent);
    }

    /**
     * Gets the label for the menu entry.<p>
     *
     * @param widgetService the widget service to ask for label texts
     *
     * @return the entry label
     */
    public String getLabel(I_CmsWidgetService widgetService) {

        return widgetService.getAttributeLabel(m_pathComponent);
    }

    /**
     * Gets the parent entry of this entry.<p>
     *
     * @return the parent entry
     */
    public CmsChoiceMenuEntryBean getParent() {

        return m_parent;
    }

    /**
     * Gets the complete path of this entry, which is a list of attribute ids.<p>
     *
     * @return the path of this entry
     */
    public List<String> getPath() {

        List<String> result = new ArrayList<String>();
        CmsChoiceMenuEntryBean entry = this;
        while (entry != null) {
            String pathComponent = entry.getPathComponent();
            if (pathComponent != null) {
                // pathComponent may be null for a dummy root entry
                result.add(entry.getPathComponent());
            }
            entry = entry.m_parent;
        }
        Collections.reverse(result);
        return result;
    }

    /**
     * Gets the path component of this entry.<p>
     *
     * @return the path component
     */
    public String getPathComponent() {

        return m_pathComponent;
    }

    /**
     * Returns true if this entry has no children.<p>
     *
     * @return true if this entry has no children
     */
    public boolean isLeaf() {

        return m_children.isEmpty();
    }
}
