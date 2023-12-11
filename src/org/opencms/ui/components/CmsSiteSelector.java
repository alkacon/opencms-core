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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.fileselect.CmsPathSelectField;

import org.apache.commons.logging.Log;

import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.ComboBox;

/**
 * Site selector component providing a select box with all sites available.
 */
@SuppressWarnings("deprecation")
public class CmsSiteSelector extends ComboBox {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsSiteSelector.class);

    /** The folder selector. */
    private CmsPathSelectField m_folderSelector;

    /**
     * Creates a new site selector component.
     */
    public CmsSiteSelector() {

        CmsObject cms = A_CmsUI.getCmsObject();
        setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SITE_0));
        setContainerDataSource(CmsVaadinUtils.getAvailableSitesContainer(cms, CmsVaadinUtils.PROPERTY_LABEL));
        setItemCaptionPropertyId(CmsVaadinUtils.PROPERTY_LABEL);
        setTextInputAllowed(true);
        setNullSelectionAllowed(false);
        setFilteringMode(FilteringMode.CONTAINS);
        setValue(cms.getRequestContext().getSiteRoot());
    }
}
