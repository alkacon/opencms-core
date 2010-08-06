/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsFieldSetFactory.java,v $
 * Date   : $Date: 2010/08/06 14:08:14 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.gwt.client.ui.input.CmsLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.Widget;

/**
 * A concrete widget factory for field sets.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since version 8.0.0
 */
public class CmsFieldSetFactory {

    /** The count of rows in that field set. */
    int m_count;

    /** The data set to create the field set from. */
    Map<Integer, List<Widget>> m_data;

    /** The table data for the label column. */
    TableData m_labelColumn;

    /** The name of the heading of the field set. */
    String m_name;

    /** The table data for a single row column. */
    TableData m_oneColumn;

    /** The table data for the widget column. */
    TableData m_widgetColumn;

    /**
     * The constructor.<p>
     * 
     * @param name the name of the heading of the field set
     */
    public CmsFieldSetFactory(String name) {

        m_name = name;

        m_labelColumn = new TableData();
        m_labelColumn.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cellpadding());

        m_widgetColumn = new TableData();
        m_widgetColumn.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cellpadding());

        m_oneColumn = new TableData();
        m_oneColumn.setColspan(2);
        m_oneColumn.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cellpadding());

        m_data = new HashMap<Integer, List<Widget>>();

        m_count = 0;
    }

    /**
     * Adds a row to the field set with a label and a widget.<p>
     * 
     * @param label the label
     * @param widget the widget
     */
    public void addFieldSetRow(CmsLabel label, Widget widget) {

        List<Widget> widgets = new ArrayList<Widget>();
        widgets.add(label);
        widgets.add(widget);
        m_data.put(Integer.valueOf(m_count), widgets);
        m_count++;
    }

    /**
     * Adds a row of text to the field set.<p>
     * 
     * @param text the text to add
     */
    public void addTextRow(String text) {

        List<Widget> widgets = new ArrayList<Widget>();
        Widget widget = new CmsLabel(text);
        widgets.add(widget);
        m_data.put(Integer.valueOf(m_count), widgets);
        m_count++;
    }

    /**
     * Adds a given widget to the field set which has the full width of the field set.<p>
     * 
     * @param widget the widget to add
     */
    public void addWidgetRow(Widget widget) {

        List<Widget> widgets = new ArrayList<Widget>();
        widgets.add(widget);
        m_data.put(Integer.valueOf(m_count), widgets);
        m_count++;
    }

    /**
     * Creates the field set on base of the added data.<p>
     * 
     * @return the created field set
     */
    public FieldSet createFieldSet() {

        if ((m_data != null) && !m_data.isEmpty()) {
            FieldSet fieldSet = new FieldSet();
            if ((m_name != null) && (m_name.trim().length() != 0)) {
                fieldSet.setHeading(m_name);
            }

            fieldSet.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());

            fieldSet.setCollapsible(true);
            TableLayout publishSetTl = new TableLayout(2);
            publishSetTl.setWidth("100%");
            fieldSet.setLayout(publishSetTl);

            for (Integer i : m_data.keySet()) {
                List<Widget> list = m_data.get(i);
                if (list.size() == 1) {
                    fieldSet.add(list.get(0), m_oneColumn);
                } else if (list.size() == 2) {
                    fieldSet.add(list.get(0), m_labelColumn);
                    fieldSet.add(list.get(1), m_widgetColumn);
                } else {
                    throw new IllegalArgumentException();
                }
            }
            m_data.clear();
            m_count = 0;
            return fieldSet;
        }
        return null;
    }

    /**
     * Sets the width of the label column.<p>
     * 
     * @param lableComlumnWidth the width in CSS units (e.g. 200px, 100%)
     */
    public void setLabelWidth(String lableComlumnWidth) {

        m_labelColumn.setWidth(lableComlumnWidth);
    }

    /**
     * Sets the name of the heading of the field set.<p>
     * 
     * @param name the heading
     */
    public void setName(String name) {

        m_name = name;
    }
}
