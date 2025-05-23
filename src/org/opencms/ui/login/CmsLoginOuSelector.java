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

package org.opencms.ui.login;

import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.util.CmsFileUtil;

import java.util.List;

import com.vaadin.ui.CustomComponent;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.ComboBox;

/**
 * Widget used to allow the user to search and select an organizational unit.<p>
 */
public class CmsLoginOuSelector extends CustomComponent {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Special value for the 'not selected' option. */
    public static final String OU_NONE = "OU_NONE";

    /** The combo box containing the OU options. */
    private ComboBox m_ouSelect = new ComboBox();

    /** Flag to always hide the selector. */
    private boolean m_alwaysHidden;

    /**
     * Creates a new instance.<P>
     */
    public CmsLoginOuSelector() {

        m_ouSelect.setWidth("100%");
        setCompositionRoot(m_ouSelect);
        m_ouSelect.setFilteringMode(FilteringMode.CONTAINS);
        m_ouSelect.setNullSelectionAllowed(false);
    }

    /**
     * Gets the selected OU.<p<
     *
     * @return the selected OU
     */
    public String getValue() {

        return (String)m_ouSelect.getValue();
    }

    /**
     * Checks if a given OU is available for selection.
     * 
     * @param ou the OU to check
     * @return true if the OU is available
     */
    public boolean hasOrgUnit(String ou) {

        return m_ouSelect.getContainerDataSource().getItem(normalizeOuName(ou)) != null;
    }

    /**
     * Initializes the select options.<p>
     *
     * @param orgUnits the selectable OUs
     * @param addEmptyOption adds empty 'not selected' option with the special value OU_NONE
     */
    public void initOrgUnits(List<CmsOrganizationalUnit> orgUnits, boolean addEmptyOption) {

        if ((orgUnits.size() == 1) && (orgUnits.get(0).getParentFqn() == null)) {
            setVisible(false);
            m_alwaysHidden = true;
        }
        if (addEmptyOption) {
            m_ouSelect.addItem(OU_NONE);
            m_ouSelect.setItemCaption(OU_NONE, CmsVaadinUtils.getMessageText(Messages.GUI_LOGIN_NO_OU_SELECTED_0));
        }
        for (CmsOrganizationalUnit ou : orgUnits) {
            String key = normalizeOuName(ou.getName());
            m_ouSelect.addItem(key);
            m_ouSelect.setItemCaption(key, ou.getDisplayName(A_CmsUI.get().getLocale()));
        }
    }

    /**
     * Returns true if the OU selector should remain hidden.<p>
     *
     * @return true if the OU selector should remain hidden
     */
    public boolean isAlwaysHidden() {

        return m_alwaysHidden;
    }

    /**
     * Sets the selected OU.<p>
     *
     * @param value the OU to select
     */
    public void setValue(String value) {

        m_ouSelect.setValue(normalizeOuName(value));
    }

    /**
     * Normalizes a given OU name.<p>
     *
     * @param ou the OU name
     * @return the normalized version
     */
    String normalizeOuName(String ou) {

        ou = CmsFileUtil.removeLeadingSeparator(ou);
        ou = CmsFileUtil.removeTrailingSeparator(ou);
        return ou;
    }

}
