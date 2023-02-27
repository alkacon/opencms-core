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
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.input.I_CmsFormField;

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Form field panel which puts its form fields into a field set, and also displays a resource info box.<p>
 */
public class CmsFieldsetFormFieldPanel extends A_CmsFormFieldPanel {

    /** Text metrics key for the info box. */
    public static String TM_INFOBOX = "infobox_";

    /** The default group id. */
    private static final String DEFAULT_GROUP = "";

    /** The list of form fields. */
    protected List<I_CmsFormField> m_fields;

    /** Group specific field sets. */
    private Map<String, CmsFieldSet> m_groupFieldSets;

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
        m_groupFieldSets = new HashMap<String, CmsFieldSet>();
        if (info != null) {
            m_infoWidget = new CmsListItemWidget(info);

            m_infoWidget.truncate(TM_INFOBOX, CmsFormDialog.STANDARD_DIALOG_WIDTH - 50);
            m_infoWidget.setStateIcon(StateIcon.standard);
            m_panel.add(m_infoWidget);
        }

        CmsFieldSet fieldSet = new CmsFieldSet();
        fieldSet.setLegend(legend);
        fieldSet.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        addGroupFieldSet(DEFAULT_GROUP, fieldSet);
        initWidget(m_panel);
    }

    /**
     * Adds a group specific field set.<p>
     *
     * @param group the group id
     * @param fieldSet the field set
     */
    public void addGroupFieldSet(String group, CmsFieldSet fieldSet) {

        // can't add a group field set twice
        if (!m_groupFieldSets.containsKey(group)) {
            m_groupFieldSets.put(group, fieldSet);
            m_panel.add(fieldSet);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#getDefaultGroup()
     */
    @Override
    public String getDefaultGroup() {

        return DEFAULT_GROUP;
    }

    /**
     * Returns the main field set.<p>
     *
     * @return the main field set
     */
    public CmsFieldSet getFieldSet() {

        return m_groupFieldSets.get(DEFAULT_GROUP);
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

        for (CmsFieldSet fieldSet : m_groupFieldSets.values()) {
            fieldSet.clear();
        }
        for (I_CmsFormField field : fields) {
            CmsFormRow row = createRow(field);
            String fieldGroup = field.getLayoutData().get("group");
            CmsFieldSet fieldSet;
            if (m_groupFieldSets.containsKey(fieldGroup)) {
                fieldSet = m_groupFieldSets.get(fieldGroup);
            } else {
                fieldSet = getFieldSet();
            }
            fieldSet.add(row);
        }
        for (CmsFieldSet fieldSet : m_groupFieldSets.values()) {
            fieldSet.setVisible(fieldSet.getWidgetCount() > 0);
        }
    }

}