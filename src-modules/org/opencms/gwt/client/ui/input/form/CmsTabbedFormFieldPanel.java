/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/form/Attic/CmsTabbedFormFieldPanel.java,v $
 * Date   : $Date: 2011/02/14 10:02:24 $
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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A tabbed form field container widget.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsTabbedFormFieldPanel extends A_CmsFormFieldPanel {

    /** The CSS bundle used for this form. **/
    private static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();
    /** The list of form fields. */
    protected List<I_CmsFormField> m_fields = Lists.newArrayList();

    /** The tab panel . */
    private CmsTabbedPanel<FlowPanel> m_panel;

    /**
     * Creates a new instance.<p>
     */
    public CmsTabbedFormFieldPanel() {

        m_panel = new CmsTabbedPanel<FlowPanel>();

        initWidget(m_panel);

        m_panel.addSelectionHandler(new SelectionHandler<Integer>() {

            public void onSelection(SelectionEvent<Integer> event) {

                for (I_CmsFormField field : m_fields) {
                    I_CmsFormWidget w = field.getWidget();
                    if (w instanceof CmsTextBox) {
                        ((CmsTextBox)w).updateLayout();
                    }
                }
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#addField(org.opencms.gwt.client.ui.input.I_CmsFormField, java.lang.String)
     */
    @Override
    public void addField(I_CmsFormField field, String fieldGroup) {

        m_fields.add(field);
        Widget target = m_panel.getTabById(fieldGroup);
        ((Panel)target).add(createRow(field.getLabel(), field.getDescription(), (Widget)(field.getWidget())));
    }

    /**
     * Adds a tab.<p>
     * 
     * @param tabId the tab id 
     * @param tabLabel the tab label 
     */
    public void addTab(String tabId, String tabLabel) {

        FlowPanel tab = new FlowPanel();
        tab.getElement().getStyle().setHeight(500, Unit.PX);
        tab.addStyleName(CSS.formTab());
        m_panel.addNamed(new FlowPanel(), tabLabel, tabId);
    }

}
