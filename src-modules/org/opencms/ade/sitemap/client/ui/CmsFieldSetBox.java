/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsFieldSetBox.java,v $
 * Date   : $Date: 2011/03/02 08:25:55 $
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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsLazyWidgetWrapper;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Custom widget for the {@link org.opencms.ade.sitemap.client.edit.CmsVfsModeSitemapEntryEditor} to display two field sets of 
 * property fields.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 *  
 */
public class CmsFieldSetBox extends Composite {

    /** The first field set. */
    private CmsFieldSet m_fieldSet1 = new CmsFieldSet();

    /** The second field set. */
    private CmsFieldSet m_fieldSet2 = new CmsFieldSet();

    /** The info label. */
    private Label m_label = new Label();

    /** The root panel of this widget. */
    private FlowPanel m_panel = new FlowPanel();

    /** The lazy wrapper for the first fieldset. */
    private CmsLazyWidgetWrapper<CmsFieldSet> m_wrapper1 = new CmsLazyWidgetWrapper<CmsFieldSet>(m_fieldSet1);

    /** The lazy wrapper for the second fieldset. */
    private CmsLazyWidgetWrapper<CmsFieldSet> m_wrapper2 = new CmsLazyWidgetWrapper<CmsFieldSet>(m_fieldSet2);

    /** 
     * Creates a new instance.<p>
     * 
     * @param title1 the title of the first field set 
     * @param title2 the title of the second field set 
     */
    public CmsFieldSetBox(String title1, String title2) {

        initWidget(m_panel);
        m_label.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formInfo());
        m_panel.add(m_label);
        m_fieldSet1.setLegend(title1);
        m_fieldSet1.setAnimationDuration(50);
        m_panel.add(m_wrapper1);
        m_fieldSet2.setLegend(title2);
        m_fieldSet2.setAnimationDuration(50);
        m_panel.add(m_wrapper2);
        setStyle(m_fieldSet1);
        setStyle(m_fieldSet2);
    }

    /**
     * Adds a widget to a fieldset specified by an index (0 or 1).<p>
     *   
     * @param index the index of the field set (0 or 1)
     * @param widget the widget to add
     */
    public void addToFieldSet(int index, Widget widget) {

        assert (index == 0) || (index == 1);
        CmsLazyWidgetWrapper<CmsFieldSet> wrapper = (index == 0) ? m_wrapper1 : m_wrapper2;
        CmsFieldSet fieldSet = wrapper.widget();
        fieldSet.addContent(widget);
    }

    /**
     * Sets the text of the info label.<p>
     * 
     * @param label the text of the info label 
     */
    public void setLabel(String label) {

        m_label.setText(label);
    }

    /**
     * Sets the style of a fieldset.<p>
     * 
     * TODO: use CSS instead
     *  
     * @param fieldset the fieldset for which to set the style 
     */
    private void setStyle(CmsFieldSet fieldset) {

        //TODO: put this into CSS 
        Style style = fieldset.getElement().getStyle();
        style.setMarginTop(10, Unit.PX);
        style.setMarginLeft(5, Unit.PX);
        style.setMarginRight(5, Unit.PX);
    }

}
