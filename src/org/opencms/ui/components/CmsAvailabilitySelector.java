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

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsPair;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.ComboBox;

/**
 * Expired/unreleased resources selection component.
 */
public class CmsAvailabilitySelector extends ComboBox<CmsPair<String, String>> {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The "all" option. */
    private static final String OPTION_ALL = "all";

    /** The "without" option. */
    private static final String OPTION_WITHOUT = "without";

    /** The "only" option. */
    private static final String OPTION_ONLY = "only";

    /** Option that selects all resources including the expired/unreleased resources. */
    public static CmsPair<String, String> m_optionAll;

    /** Option that selects all resources without the expired/unreleased resources. */
    public static CmsPair<String, String> m_optionWithout;

    /** Option that selects only the expired/unreleased resources. */
    public static CmsPair<String, String> m_optionOnly;

    /** Option list. */
    private static List<CmsPair<String, String>> m_options;

    /**
     * Creates a new expired/unreleased resources selection component.
     */
    public CmsAvailabilitySelector() {

        setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_EXPIRED_SELECTOR_0));
        setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_EXPIRED_SELECTOR_HELP_0));
        m_optionAll = CmsPair.create(OPTION_ALL, CmsVaadinUtils.getMessageText(Messages.GUI_EXPIRED_SELECTOR_ALL_0));
        m_optionWithout = CmsPair.create(
            OPTION_WITHOUT,
            CmsVaadinUtils.getMessageText(Messages.GUI_EXPIRED_SELECTOR_WITHOUT_0));
        m_optionOnly = CmsPair.create(OPTION_ONLY, CmsVaadinUtils.getMessageText(Messages.GUI_EXPIRED_SELECTOR_ONLY_0));
        m_options = new ArrayList<CmsPair<String, String>>();
        m_options.add(m_optionAll);
        m_options.add(m_optionWithout);
        m_options.add(m_optionOnly);
        setItems(m_options);
        setItemCaptionGenerator(CmsPair::getSecond);
        setEmptySelectionAllowed(false);
        setValue(m_optionAll);
        setWidthFull();
    }

    /**
     * Returns an option for a given option string.
     * @param option the option string
     * @return the option
     */
    public CmsPair<String, String> getOption(final String option) {

        if (option == null) {
            return m_optionAll;
        }
        switch (option) {
            case OPTION_WITHOUT:
                return m_optionWithout;
            case OPTION_ONLY:
                return m_optionOnly;
            case OPTION_ALL:
            default:
                return m_optionAll;
        }
    }

    /**
     * Returns whether the "all" option is currently selected.
     * @return whether the "all" option is currently selected
     */
    public boolean isOptionAll() {

        return getValue() == m_optionAll;
    }

    /**
     * Returns the "without" option.
     * @return the "without" option
     */
    public boolean isOptionOnly() {

        return getValue() == m_optionOnly;
    }

    /**
     * Returns the "only" option.
     * @return the "only" option
     */
    public boolean isOptionWithout() {

        return getValue() == m_optionWithout;
    }

    /**
     * Resets this availability selector.
     */
    public void reset() {

        setValue(m_optionAll);
    }
}
