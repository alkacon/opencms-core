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

package org.opencms.setup.ui;

import org.opencms.setup.comptest.CmsSetupTestResult;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * The widget used to show setup test results.
 */
public class CmsSetupTestResultWidget extends HorizontalLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The test result. */
    private CmsSetupTestResult m_result;

    /**
     * Creates a new instance.
     *
     * @param result the result
     */
    public CmsSetupTestResultWidget(CmsSetupTestResult result) {

        m_result = result;
        if (!result.isGreen()) {
            setDescription(result.getInfo());
        }
        Label nameLabel = new Label(result.getName());
        nameLabel.setWidth("250px");
        Label resultLabel = new Label(result.getResult());
        addComponent(nameLabel);
        addComponent(resultLabel);
        String style = null;
        if (result.isRed()) {
            style = "o-setuptest-red";
        } else if (result.isYellow()) {
            style = "o-setuptest-yellow";
        } else if (result.isGreen()) {
            style = "o-setuptest-green";
        }
        resultLabel.addStyleName(style);
        setComponentAlignment(resultLabel, Alignment.MIDDLE_RIGHT);
    }

}
