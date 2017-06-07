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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.input.I_CmsFormField;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A simple form field container widget.<p>
 *
 * @since 8.0.0
 */
public class CmsSimpleFormFieldPanel extends A_CmsFormFieldPanel {

    /** The list of form fields. */
    protected List<I_CmsFormField> m_fields;

    /** The main panel .*/
    private FlowPanel m_panel;

    /**
     * Creates a new instance.<p>
     */
    public CmsSimpleFormFieldPanel() {

        m_panel = new FlowPanel();
        initWidget(m_panel);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#getDefaultGroup()
     */
    @Override
    public String getDefaultGroup() {

        return "";
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#renderFields(java.util.Collection)
     */
    @Override
    public void renderFields(Collection<I_CmsFormField> fields) {

        m_panel.clear();
        for (I_CmsFormField field : fields) {
            CmsFormRow row = createRow(field);
            m_panel.add(row);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        clientWidth -= 12;
        storeTruncation(textMetricsKey, clientWidth);
        truncatePanel(m_panel, textMetricsKey, clientWidth);
    }

}
