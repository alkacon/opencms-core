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

package org.opencms.ui.apps.modules;

import org.opencms.module.CmsModule;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.util.table.Column;

import com.google.common.base.Strings;
import com.vaadin.server.Resource;

/**
 * Represents a row of the modules overview table.<p>
 *
 * The equals() and hashCode() methods are overridden to only compare/hash the module name.
 */
public class CmsModuleRow {

    /** The module which this row represents. */
    private CmsModule m_module;

    /**
     * Creates a new instance.<p>
     *
     * @param module the module for which this is a row
     */
    public CmsModuleRow(CmsModule module) {

        m_module = module;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {

        return (other instanceof CmsModuleRow) && ((CmsModuleRow)other).getName().equals(getName());
    }

    /**
     * Gets the module group.<p>
     *
     * @return the module group
     */
    @Column(header = Messages.GUI_MODULES_HEADER_GROUP_0, order = 40, width = 200)
    public String getGroup() {

        return Strings.nullToEmpty(m_module.getGroup());
    }

    /**
     * Gets the icon to display.<p>
     *
     * @return the icon to display
     */
    public Resource getIcon() {

        return CmsModuleApp.Icons.LIST_ICON;
    }

    /**
     * Gets the module.<p>
     *
     * @return the module
     */
    public CmsModule getModule() {

        return m_module;
    }

    /**
     * Gets the name of the module.<p>
     *
     * @return the module name
     */
    @Column(header = Messages.GUI_MODULES_HEADER_NAME_0, styleName = OpenCmsTheme.HOVER_COLUMN, width = 350, order = 10)
    public String getName() {

        return m_module.getName();
    }

    /**
     * Gets the nice name of the module.<p>
     *
     * @return the nice name of the module
     */
    @Column(header = Messages.GUI_MODULES_HEADER_TITLE_0, expandRatio = 1.0f, order = 20)
    public String getTitle() {

        return Strings.nullToEmpty(m_module.getNiceName());
    }

    /**
     * Gets the number of resource types defined.<p>
     *
     * @return the number of resource types
     */
    @Column(header = Messages.GUI_MODULES_HEADER_TYPES_0, order = 50)
    public int getTypes() {

        return m_module.getResourceTypes().size();
    }

    /**
     * Gets the version.<p>
     *
     * @return the module version
     */
    @Column(header = Messages.GUI_MODULES_HEADER_VERSION_0, width = 80, order = 30)
    public String getVersion() {

        return m_module.getVersion().toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getName().hashCode();
    }

}
