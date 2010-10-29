/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsSearchTab.java,v $
 * Date   : $Date: 2010/10/29 12:18:49 $
 * Version: $Revision: 1.15 $
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
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
import org.opencms.util.CmsStringUtil;

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
 * @version $Revision: 1.15 $
 * 
 * @since 8.0.
 */
public class CmsSearchTab extends A_CmsTab {

    /**
     * Implements the ClickHandler for the clear button.<p>
     */
    protected class ClearButtonClickHandler implements ClickHandler {

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            clearInput();
        }
    }

    /**
     * The listener for the search tab.<p>
     * 
     * Delegates the methods to the search tab handler.<p>
     */
    protected class DateBoxChangeHandler implements ValueChangeHandler<Date> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<Date> event) {

            // if the since created date was changed, set it in the tab handler
            if (event.getSource() == m_dateCreatedStartDateBox) {
                if (event.getValue() != null) {
                    m_tabHandler.setDateCreatedStart(event.getValue().getTime());
                } else {
                    // if the field is empty take the min value
                    m_tabHandler.setDateCreatedStart(-1L);
                }
            }

            // if the until created date was changed, set it in the tab handler
            if (event.getSource() == m_dateCreatedEndDateBox) {
                if (event.getValue() != null) {
                    m_tabHandler.setDateCreatedEnd(event.getValue().getTime());
                } else {
                    // if the field is empty take the max value
                    m_tabHandler.setDateCreatedEnd(-1L);
                }
            }

            // if the since modified date was changed, set it in the tab handler
            if (event.getSource() == m_dateModifiedStartDateBox) {
                if (event.getValue() != null) {
                    m_tabHandler.setDateModifiedStart(event.getValue().getTime());
                } else {
                    // if the field is empty take the min value
                    m_tabHandler.setDateModifiedStart(-1L);
                }
            }

            // if the until modified date was changed, set it in the tab handler
            if (event.getSource() == m_dateModifiedEndDateBox) {
                if (event.getValue() != null) {
                    m_tabHandler.setDateModifiedEnd(event.getValue().getTime());
                } else {
                    // if the field is empty take the max value
                    m_tabHandler.setDateModifiedEnd(-1L);
                }
            }
        }

    }

    /**
     * Implements the ValueChangeHandler for the query input field.<p>
     */
    protected class QueryChangedHandler implements ValueChangeHandler<String> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            m_tabHandler.setSearchQuery(event.getValue());

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

    /** The description label for this tab. */
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

    /** The search parameter panel for this tab. */
    private CmsSearchParamPanel m_paramPanel;

    /** The parent popup to this dialog if present. */
    private I_CmsAutoHider m_autoHideParent;

    /** The tab panel. */
    private HTMLPanel m_tab;

    /**
     * Constructor for the search tab.<p>
     * 
     * @param tabHandler the tab handler 
     * @param autoHideParent the auto-hide parent to this dialog if present
     */
    public CmsSearchTab(CmsSearchTabHandler tabHandler, I_CmsAutoHider autoHideParent) {

        // initialize the tab
        super(GalleryTabId.cms_tab_search);
        m_tab = uiBinder.createAndBindUi(this);
        initWidget(m_tab);
        m_tabHandler = tabHandler;
        m_autoHideParent = autoHideParent;
        // set the description for the search tab
        m_descriptionLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_DESCRIPTION_0));

        // add the query
        m_searchLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_TEXT_0));
        QueryChangedHandler queryHandler = new QueryChangedHandler();
        m_searchInput.addValueChangeHandler(queryHandler);

        // set the labels for the date box widgets
        m_dateCreatedStartLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_SINCE_0));
        m_dateCreatedEndLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_UNTIL_0));
        m_dateModifiedStartLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_MODIFIED_SINCE_0));
        m_dateModifiedEndLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_MODIFIED_UNTIL_0));

        if (m_autoHideParent != null) {
            m_dateCreatedEndDateBox.setAutoHideParent(m_autoHideParent);
            m_dateCreatedStartDateBox.setAutoHideParent(m_autoHideParent);
            m_dateModifiedEndDateBox.setAutoHideParent(m_autoHideParent);
            m_dateModifiedStartDateBox.setAutoHideParent(m_autoHideParent);
        }
        // add the handler to the according date box widgets
        DateBoxChangeHandler handler = new DateBoxChangeHandler();
        m_dateCreatedStartDateBox.addValueChangeHandler(handler);
        m_dateCreatedEndDateBox.addValueChangeHandler(handler);
        m_dateModifiedStartDateBox.addValueChangeHandler(handler);
        m_dateModifiedEndDateBox.addValueChangeHandler(handler);

        // add the clear button
        m_clearButton.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_BUTTON_CLEAR_0));
        m_clearButton.setUseMinWidth(true);
        ClearButtonClickHandler clearHandler = new ClearButtonClickHandler();
        m_clearButton.addClickHandler(clearHandler);
    }

    /**
     * Clears the search tab input.<p>
     */
    public void clearInput() {

        m_searchInput.setText("");
        m_dateCreatedStartDateBox.setValue(null);
        m_dateCreatedEndDateBox.setValue(null);
        m_dateModifiedStartDateBox.setValue(null);
        m_dateModifiedEndDateBox.setValue(null);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanel(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public CmsSearchParamPanel getParamPanel(CmsGallerySearchBean searchObj) {

        if (m_paramPanel == null) {
            m_paramPanel = new CmsSearchParamPanel(Messages.get().key(Messages.GUI_PARAMS_LABEL_SEARCH_0), this);
        }
        String content = getSearchParams();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_paramPanel.setContent(content);
            return m_paramPanel;
        }
        return null;
    }

    /**
     * Returns the content of the full text search parameter.<p>
     * 
     * @return the inputs from the search tab
     */
    public String getSearchParams() {

        StringBuffer result = new StringBuffer();

        // get the required data
        String query = m_searchInput.getText();
        String cStart = m_dateCreatedStartDateBox.getValueAsFormatedString();
        String cEnd = m_dateCreatedEndDateBox.getValueAsFormatedString();
        String mStart = m_dateModifiedStartDateBox.getValueAsFormatedString();
        String mEnd = m_dateModifiedEndDateBox.getValueAsFormatedString();

        // append the search query to the resulting string
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(query)) {
            result.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_TEXT_0)).append(" ").append(query);
        }

        // append the date created range to the resulting string
        StringBuffer createdResult = new StringBuffer();
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(cStart) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(cEnd))) {
            createdResult.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_RANGE_0)).append(" ").append(
                cStart).append(" - ").append(cEnd);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cStart)) {
            createdResult.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_SINCE_0)).append(" ").append(
                cStart);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cEnd)) {
            createdResult.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_UNTIL_0)).append(" ").append(
                cEnd);
        }
        if ((result.length() > 0) && (createdResult.length() > 0)) {
            result.append(", ");
        }
        result.append(createdResult);

        // append the date modified range to the resulting string
        StringBuffer modifiedResult = new StringBuffer();
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(mStart) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(mEnd))) {
            modifiedResult.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_MODIFIED_RANGE_0)).append(" ").append(
                mStart).append(" - ").append(mEnd);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mStart)) {
            modifiedResult.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_MODIFIED_SINCE_0)).append(" ").append(
                mStart);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mEnd)) {
            modifiedResult.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_MODIFIED_UNTIL_0)).append(" ").append(
                mEnd);
        }
        if ((result.length() > 0) && (modifiedResult.length() > 0)) {
            result.append(", ");
        }
        result.append(modifiedResult);

        return result.toString();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    public CmsSearchTabHandler getTabHandler() {

        return m_tabHandler;
    }
}