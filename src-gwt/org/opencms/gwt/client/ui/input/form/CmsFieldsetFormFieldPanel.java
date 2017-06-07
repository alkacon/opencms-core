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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;

import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Form field panel which puts its form fields into a field set, and also displays a resource info box.<p>
 */
public class CmsFieldsetFormFieldPanel extends A_CmsFormFieldPanel {

    /** Text metrics key for the info box. */
    public static String TM_INFOBOX = "infobox_";

    /** The list of form fields. */
    protected List<I_CmsFormField> m_fields;

    /** The main field set. */
    private CmsFieldSet m_fieldSet;

    /** The main panel .*/
    private FlowPanel m_panel;

    /**
     * Creates a new instance.<p>
     *
     * @param info the bean used to display the info item
     * @param legend the legend for the field set
     */
    public CmsFieldsetFormFieldPanel(CmsListInfoBean info, String legend) {

        m_panel = new FlowPanel();
        if (info != null) {
            m_infoWidget = new CmsListItemWidget(info);
            m_infoWidget.truncate(TM_INFOBOX, CmsFormDialog.STANDARD_DIALOG_WIDTH - 50);
            m_infoWidget.setStateIcon(StateIcon.standard);
            m_panel.add(m_infoWidget);
        }
        m_fieldSet = new CmsFieldSet();
        m_fieldSet.setLegend(legend);
        m_fieldSet.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        m_panel.add(m_fieldSet);
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
     * Returns the main field set.<p>
     *
     * @return the main field set
     */
    public CmsFieldSet getFieldSet() {

        return m_fieldSet;
    }

    /**
     * Gets the main panel.<p>
     *
     * @return the main panel
     */
    public FlowPanel getMainPanel() {

        return m_panel;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#renderFields(java.util.Collection)
     */
    @Override
    public void renderFields(Collection<I_CmsFormField> fields) {

        m_fieldSet.clear();
        for (I_CmsFormField field : fields) {
            CmsFormRow row = createRow(field);
            m_fieldSet.add(row);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        storeTruncation(textMetricsKey, clientWidth);
        truncatePanel(m_panel, textMetricsKey, clientWidth);
    }
}