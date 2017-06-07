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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Provides a simple progress bar.<p>
 *
 * @since 8.0.0
 */
public class CmsProgressBar extends FlowPanel {

    /** The widget for the completed part of the process. */
    private FlowPanel m_complete = new FlowPanel();

    /** The div element for the percentage text. */
    private HTML m_text = new HTML();

    /**
     * Creates a progress bar.<p>
     *
     * Initializes the progress bar with 0 percent.<p>
     */
    public CmsProgressBar() {

        m_text.setStyleName(I_CmsLayoutBundle.INSTANCE.progressBarCss().meterText());

        m_complete.setStyleName(I_CmsLayoutBundle.INSTANCE.progressBarCss().meterValue());
        m_complete.addStyleName(I_CmsLayoutBundle.INSTANCE.progressBarCss().colorComplete());
        m_complete.add(m_text);

        add(m_complete);
        setStyleName(I_CmsLayoutBundle.INSTANCE.progressBarCss().meterWrap());
        addStyleName(I_CmsLayoutBundle.INSTANCE.progressBarCss().colorIncomplete());

        setValue(0);
    }

    /**
     * Sets the progress.<p>
     *
     * @param percent the percent to set
     */
    public void setValue(int percent) {

        if (percent <= 100) {
            m_text.setText(percent + "%");
            m_complete.setWidth(percent + "%");
        }
    }
}
