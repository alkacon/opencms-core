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

package org.opencms.ui.apps.modules.edit;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.fileselect.CmsPathSelectField;

import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

/**
 * Widget used to edit a module export point.<p>
 */
public class CmsExportPointWidget extends FormLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The source. */
    private CmsPathSelectField m_source;

    /** The target. */
    private TextField m_target;

    /**
     * Creates a new instance.
     */
    public CmsExportPointWidget() {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
    }

    /**
     * Creates a new instance.<p>
     *
     * @param source the export point source
     * @param target the export point target
     */
    public CmsExportPointWidget(String source, String target) {
        this();
        m_source.setUseRootPaths(true);
        m_source.setValue(source);
        m_target.setValue(target);
    }

    /**
     * Gets the export point target.<p>
     *
     * @return the export point target
     */
    public String getDestination() {

        return m_target.getValue();
    }

    /**
     * Gets the export point source.<p>
     *
     * @return the export point source
     */
    public String getUri() {

        return m_source.getValue();
    }

}
