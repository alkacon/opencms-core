/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsSimpleSitemapFormFieldPanel.java,v $
 * Date   : $Date: 2011/05/27 07:30:09 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsFormRow;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Form field panel for the sitemap entry editor in the navigation mode.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 * 
 * @version $
 */
public class CmsSimpleSitemapFormFieldPanel extends A_CmsFormFieldPanel {

    /** The list of form fields. */
    protected List<I_CmsFormField> m_fields;

    /** The main panel .*/
    private FlowPanel m_panel;

    /** The inner panel containing the form fields. */
    private FlowPanel m_innerPanel;

    /**
     * Creates a new instance.<p>
     * 
     * @param info the bean used to display the info item
     */
    public CmsSimpleSitemapFormFieldPanel(CmsListInfoBean info) {

        m_panel = new FlowPanel();
        m_innerPanel = new FlowPanel();
        CmsListItemWidget liWidget = new CmsListItemWidget(info);
        liWidget.setStateIcon(StateIcon.standard);
        if (CmsSitemapView.getInstance().isNavigationMode()) {
            liWidget.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().navMode());
        } else {
            liWidget.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().vfsMode());
        }
        m_panel.add(liWidget);
        m_panel.add(m_innerPanel);
        m_innerPanel.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_innerPanel.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.propertiesCss().navModePropertiesBox());
        //setBorder(m_panel);
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

        m_innerPanel.clear();
        for (I_CmsFormField field : fields) {
            CmsFormRow row = createRow(field);
            m_innerPanel.add(row);
        }
    }

}
