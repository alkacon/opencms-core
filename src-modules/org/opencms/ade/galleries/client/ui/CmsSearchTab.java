/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsSearchTab.java,v $
 * Date   : $Date: 2010/06/10 08:45:04 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsSearchTabHandler;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsTextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides the widget for the full text search tab.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.
 */
public class CmsSearchTab extends A_CmsTab {

    /** The ui-binder interface. */
    interface I_CmsSearchTabUiBinder extends UiBinder<HTMLPanel, CmsSearchTab> {
        // GWT interface, nothing to do here
    }

    @UiField
    protected Label m_descriptionLabel;

    @UiField
    protected Label m_searchLabel;

    @UiField
    protected Label m_dateLabel;

    @UiField
    protected Label m_dateField;

    @UiField
    protected CmsTextBox m_searchInput;

    //    @UiField
    //    protected DateBox m_datePicker;

    /** The ui-binder instance. */
    private static I_CmsSearchTabUiBinder uiBinder = GWT.create(I_CmsSearchTabUiBinder.class);

    /** The tab panel. */
    private HTMLPanel m_tab;

    /** The tab handler. */
    private CmsSearchTabHandler m_tabHandler;

    /**
     * Constructor.<p>
     * 
     * @param tabHandler the tab handler 
     */
    public CmsSearchTab(CmsSearchTabHandler tabHandler) {

        super(GalleryTabId.cms_tab_search);
        m_tab = uiBinder.createAndBindUi(this);
        initWidget(m_tab);
        m_tabHandler = tabHandler;
        // TODO: add localization
        m_descriptionLabel.setText("Search for resources");
        m_searchLabel.setText("Seach:");
        m_dateLabel.setText("Date:");
        Element ins = DOM.createDiv();
        ins.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()
            + " "
            + I_CmsLayoutBundle.INSTANCE.dialogCss().popupShadow());
        //        m_datePicker.getDatePicker().getParent().getElement().insertFirst(ins);
        //        m_datePicker.getDatePicker().addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        //        CmsDebugLog.getInstance().printLine(
        //            "datepicker class:" + m_datePicker.getDatePicker().getElement().getClassName());
        //        CmsDebugLog.getInstance().printLine(
        //            "datepicker parent class:" + m_datePicker.getDatePicker().getElement().getParentElement().getClassName());
        //        m_datePicker.getDatePicker().getElement().getParentElement().addClassName(
        //            I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        //        m_datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
        //
        //            public void onValueChange(ValueChangeEvent<Date> event) {
        //
        //                Date date = event.getValue();
        //                String dateString = DateTimeFormat.getMediumDateFormat().format(date);
        //                m_dateField.setText(dateString);
        //            }
        //        });
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    public CmsSearchTabHandler getTabHandler() {

        return m_tabHandler;
    }

}