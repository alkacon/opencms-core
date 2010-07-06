/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsSearchTab.java,v $
 * Date   : $Date: 2010/07/06 12:08:04 $
 * Version: $Revision: 1.7 $
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
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides the widget for the full text search tab.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.
 */
public class CmsSearchTab extends A_CmsTab {

    /**
     * The listener for the search tab.<p>
     * 
     * Delegates the methods to the search tab handler.<p>
     */
    protected class SearchTabListener implements ValueChangeHandler<Date>, ClickHandler {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<Date> event) {

            // if the since created date was changed, set it in the tab handler
            if (event.getSource() == m_dateCreatedStartDateBox) {
                if (m_dateCreatedStartDateBox.getValue() != null) {
                    m_tabHandler.setDateCreatedStart(m_dateCreatedStartDateBox.getValue().getTime());
                } else {
                    // if the field is empty take the min value
                    m_tabHandler.setDateCreatedStart(Long.MIN_VALUE);
                }
            }

            // if the until created date was changed, set it in the tab handler
            if (event.getSource() == m_dateCreatedEndDateBox) {
                if (m_dateCreatedEndDateBox.getValue() != null) {
                    m_tabHandler.setDateCreatedEnd(m_dateCreatedEndDateBox.getValue().getTime());
                } else {
                    // if the field is empty take the max value
                    m_tabHandler.setDateCreatedEnd(Long.MAX_VALUE);
                }
            }

            // if the since modified date was changed, set it in the tab handler
            if (event.getSource() == m_dateModifiedStartDateBox) {
                if (m_dateModifiedStartDateBox.getValue() != null) {
                    m_tabHandler.setDateModifiedStart(m_dateModifiedStartDateBox.getValue().getTime());
                } else {
                    // if the field is empty take the min value
                    m_tabHandler.setDateModifiedStart(Long.MIN_VALUE);
                }
            }

            // if the until modified date was changed, set it in the tab handler
            if (event.getSource() == m_dateModifiedEndDateBox) {
                if (m_dateModifiedEndDateBox.getValue() != null) {
                    m_tabHandler.setDateModifiedEnd(m_dateModifiedEndDateBox.getValue().getTime());
                } else {
                    // if the field is empty take the max value
                    m_tabHandler.setDateModifiedEnd(Long.MAX_VALUE);
                }
            }
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            m_searchInput.setText("");
            m_dateCreatedStartDateBox.getBox().setText("");
            m_dateCreatedEndDateBox.getBox().setText("");
            m_dateModifiedStartDateBox.getBox().setText("");
            m_dateModifiedEndDateBox.getBox().setText("");
            m_tabHandler.clearInput();
        }
    }

    /** The ui-binder interface. */
    interface I_CmsSearchTabUiBinder extends UiBinder<HTMLPanel, CmsSearchTab> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance. */
    private static I_CmsSearchTabUiBinder uiBinder = GWT.create(I_CmsSearchTabUiBinder.class);

    /** The button to clear the tab input. */
    @UiField
    protected CmsPushButton m_clearButton;

    /** The date box for the created until date. */
    @UiField
    protected CmsDateBox m_dateCreatedEndDateBox;

    /** The label for the created until date. */
    @UiField
    protected Label m_dateCreatedEndLabel;

    /** The date box for the created since date. */
    @UiField
    protected CmsDateBox m_dateCreatedStartDateBox;

    /** The label for the created since date. */
    @UiField
    protected Label m_dateCreatedStartLabel;

    /** The date box for the modified until date. */
    @UiField
    protected CmsDateBox m_dateModifiedEndDateBox;

    /** The label for the modified until date. */
    @UiField
    protected Label m_dateModifiedEndLabel;

    /** The date box for the modified since date. */
    @UiField
    protected CmsDateBox m_dateModifiedStartDateBox;

    /** The label for the modified since date. */
    @UiField
    protected Label m_dateModifiedStartLabel;

    /** The descrition label for this tab. */
    @UiField
    protected Label m_descriptionLabel;

    /** The input field for the search query. */
    @UiField
    protected CmsTextBox m_searchInput;

    /** The label for the search query. */
    @UiField
    protected Label m_searchLabel;

    /** The tab handler. */
    CmsSearchTabHandler m_tabHandler;

    /** The tab panel. */
    private HTMLPanel m_tab;

    /**
     * Constructor for the search tab.<p>
     * 
     * @param tabHandler the tab handler 
     */
    public CmsSearchTab(CmsSearchTabHandler tabHandler) {

        // initialize the tab
        super(GalleryTabId.cms_tab_search);
        m_tab = uiBinder.createAndBindUi(this);
        initWidget(m_tab);
        m_tabHandler = tabHandler;

        // add the texts to the labels and to the button
        // TODO: add localization
        m_descriptionLabel.setText("Search for resources");
        m_searchLabel.setText("Search:");
        m_dateCreatedStartLabel.setText("created since:");
        m_dateCreatedEndLabel.setText("created until:");
        m_dateModifiedStartLabel.setText("modified since:");
        m_dateModifiedEndLabel.setText("modified until:");
        m_clearButton.setText("clear");

        // add the handler to the according components
        SearchTabListener handler = new SearchTabListener();
        m_dateCreatedStartDateBox.addValueChangeHandler(handler);
        m_dateCreatedEndDateBox.addValueChangeHandler(handler);
        m_dateModifiedStartDateBox.addValueChangeHandler(handler);
        m_dateModifiedEndDateBox.addValueChangeHandler(handler);
        m_clearButton.addClickHandler(handler);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    public CmsSearchTabHandler getTabHandler() {

        return m_tabHandler;
    }

}