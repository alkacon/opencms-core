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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.ui.input.CmsSelectBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Widget containing a select box for selecting formatters and a help icon.<p>
 */
public class CmsElementSettingsFormatterWidget extends Composite {

    /** The UiBinder interface for this class. */
    public interface I_UiBinder extends UiBinder<FlowPanel, CmsElementSettingsFormatterWidget> {
        // nothing here
    }

    /** The UiBinder instance for this class. */
    public static I_UiBinder uiBinder = GWT.create(I_UiBinder.class);

    /** The formatter select box. */
    @UiField
    protected CmsSelectBox m_formatterSelect;

    /** The help icon. */
    @UiField
    protected FlowPanel m_help;

    /**
     * Creates a new instance.<p>
     */
    public CmsElementSettingsFormatterWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Returns the formatterSelect.<p>
     *
     * @return the formatterSelect
     */
    public CmsSelectBox getFormatterSelect() {

        return m_formatterSelect;
    }

    /**
     * Returns the help.<p>
     *
     * @return the help
     */
    public FlowPanel getHelp() {

        return m_help;
    }

}
