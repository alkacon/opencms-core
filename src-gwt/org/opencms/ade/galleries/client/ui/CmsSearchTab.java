/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsLabelSelectCell;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
import org.opencms.util.CmsStringUtil;

import java.util.Date;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Provides the widget for the full text search tab.<p>
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
     * The date box change handler.<p>
     * 
     * Used for all date boxes inside the search tab.<p>
     * 
     * Delegates the methods to the search tab handler.<p>
     */
    protected class DateBoxChangeHandler implements ValueChangeHandler<Date>, KeyPressHandler {

        /**
         * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
         */
        public void onKeyPress(KeyPressEvent event) {

            UIObject source = (UIObject)event.getSource();
            Element el = source.getElement();
            if (m_dateCreatedStartDateBox.getElement().isOrHasChild(el)) {
                Scheduler.get().scheduleDeferred(new DateChangeCommand(m_dateCreatedStartDateBox));
            } else if (m_dateCreatedEndDateBox.getElement().isOrHasChild(el)) {
                Scheduler.get().scheduleDeferred(new DateChangeCommand(m_dateCreatedEndDateBox));
            } else if (m_dateModifiedStartDateBox.getElement().isOrHasChild(el)) {
                Scheduler.get().scheduleDeferred(new DateChangeCommand(m_dateModifiedStartDateBox));
            } else if (m_dateModifiedEndDateBox.getElement().isOrHasChild(el)) {
                Scheduler.get().scheduleDeferred(new DateChangeCommand(m_dateModifiedEndDateBox));
            }
        }

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
     * Scheduled command implementation for the date boxes that fires a value change event for the given date box.<p>
     */
    protected class DateChangeCommand implements ScheduledCommand {

        /** The date box to use as source. */
        private CmsDateBox m_dateBox;

        /**
         * The constructor.<p>
         * 
         * @param dateBox the date box to use as source for the value change event
         */
        public DateChangeCommand(CmsDateBox dateBox) {

            m_dateBox = dateBox;
        }

        /**
         * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
         */
        public void execute() {

            if (m_dateBox.isValideDateBox()) {
                ValueChangeEvent.fire(m_dateBox, m_dateBox.getValue());
            } else {
                ValueChangeEvent.fire(m_dateBox, null);
            }
        }
    }

    /**
     * Internal handler for the include expired check-box.<p>
     */
    protected class IncludeExpiredChangeHandler implements ValueChangeHandler<Boolean> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<Boolean> event) {

            Boolean value = event.getValue();
            m_tabHandler.setIncludeExpired(value.booleanValue());
        }
    }

    /**
     * The language selection handler.<p>
     * 
     * Delegates the methods to the search tab handler.<p>
     */
    protected class LanguageChangeHandler implements ValueChangeHandler<String> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            String value = event.getValue();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(value) || value.equals(NOT_SET_OPTION_VALUE)) {
                value = m_currentLocale;
            }
            m_tabHandler.setLocale(value);
        }
    }

    /**
     * Implements the ValueChangeHandler for the query input field.<p>
     */
    protected class QueryChangedHandler implements ValueChangeHandler<String>, KeyPressHandler {

        /**
         * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
         */
        public void onKeyPress(KeyPressEvent event) {

            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    public void execute() {

                        m_tabHandler.selectResultTab();
                    }
                });
            } else {

                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    /**
                     * @see com.google.gwt.user.client.Command#execute()
                     */
                    public void execute() {

                        ValueChangeEvent.fire(m_searchInput, m_searchInput.getText());
                    }
                });
            }
        }

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(event.getValue()) && (event.getValue().length() >= 3)) {
                m_tabHandler.setSearchQuery(event.getValue());
            } else {
                m_tabHandler.setSearchQuery(null);
            }
        }
    }

    /**
     * Internal handler for search scope changes.<p>
     */
    protected class ScopeChangeHandler implements ValueChangeHandler<String> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            String value = event.getValue();
            m_tabHandler.setScope(CmsGallerySearchScope.valueOf(value));

        }
    }

    /** The ui-binder interface. */
    interface I_CmsSearchTabUiBinder extends UiBinder<HTMLPanel, CmsSearchTab> {
        // GWT interface, nothing to do here
    }

    /** A constant for the "not set" valueof the language selection. */
    private static final String NOT_SET_OPTION_VALUE = "notSet";

    /** The ui-binder instance. */
    private static I_CmsSearchTabUiBinder uiBinder = GWT.create(I_CmsSearchTabUiBinder.class);

    /** The button to clear the tab input. */
    @UiField
    protected CmsPushButton m_clearButton;

    /** The current locale. */
    protected String m_currentLocale;

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

    /** The include expired resources check-box. */
    @UiField
    protected CmsCheckBox m_includeExpiredCheckBox;

    /** The include expired resources form row. */
    @UiField
    protected DivElement m_includeExpiredRow;

    /** The label for the language selection. */
    @UiField
    protected Label m_localeLabel;

    /** The row for the language selection. */
    @UiField
    protected HTMLPanel m_localeRow;

    /** The select box for the language selection. */
    @UiField
    protected CmsSelectBox m_localeSelection;

    /** The label for the search scope selection. */
    @UiField
    protected Label m_scopeLabel;

    /** The row for the search scope selection. */
    @UiField
    protected HTMLPanel m_scopeRow;

    /** The select box for the search scope selection. */
    @UiField
    protected CmsSelectBox m_scopeSelection;

    /** The input field for the search query. */
    @UiField
    protected CmsTextBox m_searchInput;

    /** The label for the search query. */
    @UiField
    protected Label m_searchLabel;

    /** The tab handler. */
    CmsSearchTabHandler m_tabHandler;

    /** The parent popup to this dialog if present. */
    private I_CmsAutoHider m_autoHideParent;

    /** The map of available locales. */
    private Map<String, String> m_availableLocales;

    /** The search parameter panel for this tab. */
    private CmsSearchParamPanel m_paramPanel;

    /** The search scope. */
    private CmsGallerySearchScope m_scope;

    /** The tab panel. */
    private HTMLPanel m_tab;

    /**
     * Constructor for the search tab.<p>
     * 
     * @param tabHandler the tab handler 
     * @param autoHideParent the auto-hide parent to this dialog if present
     * @param currentLocale the current content locale
     * @param availableLocales the available locales
     * @param scope the search scope 
     */
    @SuppressWarnings("deprecation")
    public CmsSearchTab(
        CmsSearchTabHandler tabHandler,
        I_CmsAutoHider autoHideParent,
        String currentLocale,
        Map<String, String> availableLocales,
        CmsGallerySearchScope scope) {

        // initialize the tab
        super(GalleryTabId.cms_tab_search.name());
        m_tab = uiBinder.createAndBindUi(this);
        initWidget(m_tab);
        addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().highTextBoxes());
        m_tabHandler = tabHandler;
        m_autoHideParent = autoHideParent;
        m_currentLocale = currentLocale;
        m_availableLocales = availableLocales;
        m_scope = scope;

        //add search roots selection
        String scopeLabelText = Messages.get().key(Messages.GUI_SEARCH_SCOPE_0);
        m_scopeLabel.setText(scopeLabelText);
        for (CmsGallerySearchScope choice : CmsGallerySearchScope.values()) {
            String name = Messages.get().key(choice.getKey());
            m_scopeSelection.addOption(choice.name(), name);
        }
        m_scopeSelection.setFormValueAsString(m_scope.name());

        m_scopeSelection.addValueChangeHandler(new ScopeChangeHandler());

        // add the language selection
        m_localeLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LANGUAGE_LABEL_TEXT_0));
        CmsLabelSelectCell notSelectedCell = new CmsLabelSelectCell(NOT_SET_OPTION_VALUE, Messages.get().key(
            Messages.GUI_TAB_SEARCH_LANGUAGE_NOT_SEL_0));
        notSelectedCell.setVisible(false);
        m_localeSelection.addOption(notSelectedCell);
        for (Map.Entry<String, String> entry : availableLocales.entrySet()) {
            m_localeSelection.addOption(entry.getKey(), entry.getValue());
        }
        m_localeSelection.addValueChangeHandler(new LanguageChangeHandler());

        // hide language selection if only one locale is available 
        if (availableLocales.size() <= 1) {
            m_localeRow.getElement().getStyle().setDisplay(Display.NONE);
        }

        // add the query
        m_searchLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_TEXT_0));
        QueryChangedHandler queryHandler = new QueryChangedHandler();
        m_searchInput.addValueChangeHandler(queryHandler);
        m_searchInput.addKeyPressHandler(queryHandler);
        m_includeExpiredCheckBox.setChecked(false);
        m_includeExpiredCheckBox.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_INCLUDE_EXPIRED_0));
        m_includeExpiredCheckBox.addValueChangeHandler(new IncludeExpiredChangeHandler());
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
        m_dateCreatedStartDateBox.addKeyPressHandler(handler);
        m_dateCreatedEndDateBox.addValueChangeHandler(handler);
        m_dateCreatedEndDateBox.addKeyPressHandler(handler);
        m_dateModifiedStartDateBox.addValueChangeHandler(handler);
        m_dateModifiedStartDateBox.addKeyPressHandler(handler);
        Date initialStartDate = new Date();
        initialStartDate.setHours(0);
        initialStartDate.setMinutes(0);
        m_dateModifiedStartDateBox.setInitialDate(initialStartDate);
        m_dateModifiedEndDateBox.addValueChangeHandler(handler);
        m_dateModifiedEndDateBox.addKeyPressHandler(handler);
        Date initialEndDate = new Date();
        initialEndDate.setHours(23);
        initialEndDate.setMinutes(59);
        m_dateModifiedEndDateBox.setInitialDate(initialEndDate);
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

        m_searchInput.setFormValueAsString("");
        ValueChangeEvent.fire(m_searchInput, "");
        m_dateCreatedStartDateBox.setValue(null, true);
        m_dateCreatedEndDateBox.setValue(null, true);
        m_dateModifiedStartDateBox.setValue(null, true);
        m_dateModifiedEndDateBox.setValue(null, true);
        m_includeExpiredCheckBox.setChecked(false);
        m_localeSelection.reset();
    }

    /**
     * Enables the include expired resources form input.<p>
     * 
     * @param enable <code>true</code> to enable the include expired resources form input
     */
    public void enableExpiredResourcesSearch(boolean enable) {

        m_includeExpiredRow.getStyle().setDisplay(enable ? Display.BLOCK : Display.NONE);
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

        // append the language to the resulting string
        String locale = m_localeSelection.getFormValueAsString();
        String language = m_availableLocales.get(locale);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(locale)
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(language)
            && !locale.equals(NOT_SET_OPTION_VALUE)) {
            result.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LANGUAGE_LABEL_TEXT_0)).append(" ").append(
                language);
        }

        // append the search query to the resulting string
        StringBuffer queryResult = new StringBuffer();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(query)) {
            queryResult.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_TEXT_0)).append(" ").append(query);
        }
        if ((result.length() > 0) && (queryResult.length() > 0)) {
            result.append(", ");
        }
        result.append(queryResult);

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

        if (m_includeExpiredCheckBox.getFormValue().booleanValue()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(Messages.get().key(Messages.GUI_PARAMS_LABEL_INCLUDING_EXPIRED_0));
        }
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