/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/form/Attic/CmsSimpleFormFieldPanel.java,v $
 * Date   : $Date: 2011/03/02 08:25:55 $
 * Version: $Revision: 1.2 $
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

import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.I_CmsFormField;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * A simple form field container widget.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsSimpleFormFieldPanel extends A_CmsFormFieldPanel {

    /** The CSS bundle used for this form. **/
    private static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();
    /** The list of form fields. */
    protected List<I_CmsFormField> m_fields;

    /** The main panel .*/
    private FlowPanel m_panel;

    /**
     * Creates a new instance.<p>
     */
    public CmsSimpleFormFieldPanel() {

        m_panel = new FlowPanel();
        setBorder(m_panel);
        m_panel.addStyleName(CSS.form());
        m_panel.addStyleName(CSS.formTab());
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
        if (m_labelText != null) {
            Label label = new Label(m_labelText);
            label.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formInfo());
            m_panel.add(label);

        }
        for (I_CmsFormField field : fields) {
            CmsFormRow row = createRow(field);
            m_panel.add(row);
        }
    }

}
