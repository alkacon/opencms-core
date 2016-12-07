/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsLabelSelectCell;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides the widget for the full text search tab.<p>
 *
 * @since 8.0.
 */
public class CmsSearchTab extends A_CmsTab {

    /** The parameter types of this tab. */
    public enum ParamType {
        /** The creation range type. */
        creation,
        /** The expired resources type. */
        expired,
        /** The language type. */
        language,
        /** The modification range type. */
        modification,
        /** The search scope type. */
        scope,
        /** Text query type. */
        text
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

    /** The search button. */
    @UiField
    protected CmsPushButton m_searchButton;

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

    /** The default search scope. */
    private CmsGallerySearchScope m_defaultScope;

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
     * @param defaultScope the default search scope
     * @param defaultIncludeExpired true if 'show expired' should be enabled by default
     */
    @SuppressWarnings("deprecation")
    public CmsSearchTab(
        CmsSearchTabHandler tabHandler,
        I_CmsAutoHider autoHideParent,
        String currentLocale,
        Map<String, String> availableLocales,
        CmsGallerySearchScope scope,
        CmsGallerySearchScope defaultScope,
        boolean defaultIncludeExpired) {

        // initialize the tab
        super(GalleryTabId.cms_tab_search.name());
        m_tab = uiBinder.createAndBindUi(this);
        initWidget(m_tab);
        addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().highTextBoxes());
        m_tabHandler = tabHandler;
        m_autoHideParent = autoHideParent;
        m_currentLocale = currentLocale;
        m_availableLocales = availableLocales;
        m_defaultScope = defaultScope;

        //add search roots selection
        String scopeLabelText = Messages.get().key(Messages.GUI_SEARCH_SCOPE_0);
        m_scopeLabel.setText(scopeLabelText);
        for (CmsGallerySearchScope choice : CmsGallerySearchScope.values()) {
            String name = Messages.get().key(choice.getKey());
            m_scopeSelection.addOption(choice.name(), name);
        }
        m_scopeSelection.selectValue(scope.name());

        // add the language selection
        m_localeLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LANGUAGE_LABEL_TEXT_0));
        CmsLabelSelectCell notSelectedCell = new CmsLabelSelectCell(
            NOT_SET_OPTION_VALUE,
            Messages.get().key(Messages.GUI_TAB_SEARCH_LANGUAGE_NOT_SEL_0));
        notSelectedCell.setVisible(false);
        m_localeSelection.addOption(notSelectedCell);
        for (Map.Entry<String, String> entry : availableLocales.entrySet()) {
            m_localeSelection.addOption(entry.getKey(), entry.getValue());
        }
        // hide language selection if only one locale is available
        if (availableLocales.size() <= 1) {
            m_localeRow.getElement().getStyle().setDisplay(Display.NONE);
        }
        m_searchButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_searchButton.setImageClass(I_CmsButton.SEARCH_SMALL);
        m_searchButton.setTitle(Messages.get().key(Messages.GUI_TAB_SEARCH_SEARCH_EXISTING_0));
        // add the query
        m_searchLabel.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_TEXT_0));
        m_searchInput.setGhostValue(Messages.get().key(Messages.GUI_QUICK_FINDER_SEARCH_0), true);
        m_searchInput.setGhostModeClear(true);
        m_includeExpiredCheckBox.setChecked(defaultIncludeExpired);
        m_tabHandler.setIncludeExpired(defaultIncludeExpired, false);
        m_includeExpiredCheckBox.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_INCLUDE_EXPIRED_0));
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
        Date initialStartDate = new Date();
        initialStartDate.setHours(0);
        initialStartDate.setMinutes(0);
        m_dateModifiedStartDateBox.setInitialDate(initialStartDate);
        m_dateCreatedStartDateBox.setInitialDate(initialStartDate);
        Date initialEndDate = new Date();
        initialEndDate.setHours(23);
        initialEndDate.setMinutes(59);
        m_dateModifiedEndDateBox.setInitialDate(initialEndDate);
        m_dateCreatedEndDateBox.setInitialDate(initialEndDate);
        // add the clear button
        m_clearButton.setText(Messages.get().key(Messages.GUI_TAB_SEARCH_BUTTON_CLEAR_0));
        m_clearButton.setUseMinWidth(true);
        // add change handler to display the query string changes that may have occurred within another tab
        getTabHandler().addSearchChangeHandler(new ValueChangeHandler<CmsGallerySearchBean>() {

            public void onValueChange(ValueChangeEvent<CmsGallerySearchBean> arg0) {

                // only set the query if the tab is not currently selected
                if (!isSelected()) {
                    m_searchInput.setFormValueAsString(arg0.getValue().getQuery());
                }
            }
        });
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
     * Sets the form fields to the values from the stored  gallery search.<p>
     *
     * @param search a previously stored gallery search
     */
    public void fillParams(CmsGallerySearchBean search) {

        m_localeSelection.setFormValue(search.getLocale(), false);
        m_searchInput.setFormValueAsString(search.getQuery());
        m_includeExpiredCheckBox.setChecked(search.isIncludeExpired());
        if (search.getDateCreatedStart() > 9) {
            m_dateCreatedStartDateBox.setValue(new Date(search.getDateCreatedStart()));
        }
        if (search.getDateCreatedEnd() > 0) {
            m_dateCreatedEndDateBox.setValue(new Date(search.getDateCreatedEnd()));
        }
        if (search.getDateModifiedStart() > 0) {
            m_dateModifiedStartDateBox.setValue(new Date(search.getDateModifiedStart()));
        }
        if (search.getDateModifiedEnd() > 0) {
            m_dateModifiedEndDateBox.setValue(new Date(search.getDateModifiedEnd()));
        }
        if (search.getScope() != null) {
            m_scopeSelection.setFormValue(search.getScope().name());
        }

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        List<CmsSearchParamPanel> result = new ArrayList<CmsSearchParamPanel>();
        // get the required data
        String query = m_searchInput.getFormValueAsString();
        String createdStart = m_dateCreatedStartDateBox.getValueAsFormatedString();
        String createdEnd = m_dateCreatedEndDateBox.getValueAsFormatedString();
        String modifiedStart = m_dateModifiedStartDateBox.getValueAsFormatedString();
        String modifiedEnd = m_dateModifiedEndDateBox.getValueAsFormatedString();

        CmsGallerySearchScope scope = CmsGallerySearchScope.valueOf(m_scopeSelection.getFormValueAsString());
        if ((scope != m_defaultScope)) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_PARAMS_LABEL_SCOPE_0),
                this);
            panel.setContent(Messages.get().key(scope.getKey()), ParamType.scope.name());
            result.add(panel);
        }
        // append the language
        String locale = m_localeSelection.getFormValueAsString();
        String language = m_availableLocales.get(locale);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(locale)
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(language)
            && !locale.equals(NOT_SET_OPTION_VALUE)) {

            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LANGUAGE_LABEL_TEXT_0),
                this);
            panel.setContent(language, ParamType.language.name());
            result.add(panel);
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(query)) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_TEXT_0),
                this);
            panel.setContent(query, ParamType.text.name());
            result.add(panel);
        }

        // append the date created range
        StringBuffer createdResult = new StringBuffer();
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(createdStart)
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(createdEnd))) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_RANGE_0),
                this);
            panel.setContent(createdStart + " - " + createdEnd, ParamType.creation.name());
            result.add(panel);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(createdStart)) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_SINCE_0),
                this);
            panel.setContent(createdStart, ParamType.creation.name());
            result.add(panel);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(createdEnd)) {
            createdResult.append(Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_UNTIL_0)).append(" ").append(
                createdEnd);

            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_CREATED_UNTIL_0),
                this);
            panel.setContent(createdEnd, ParamType.creation.name());
            result.add(panel);
        }

        // append the date modified range
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(modifiedStart)
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(modifiedEnd))) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_MODIFIED_RANGE_0),
                this);
            panel.setContent(modifiedStart + " - " + modifiedEnd, ParamType.modification.name());
            result.add(panel);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(modifiedStart)) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_MODIFIED_SINCE_0),
                this);
            panel.setContent(modifiedStart, ParamType.modification.name());
            result.add(panel);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(modifiedEnd)) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_MODIFIED_UNTIL_0),
                this);
            panel.setContent(modifiedEnd, ParamType.modification.name());
            result.add(panel);
        }

        if (m_includeExpiredCheckBox.getFormValue().booleanValue()) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_PARAMS_LABEL_INCLUDING_EXPIRED_0),
                this);
            panel.setContent("", ParamType.expired.name());
            result.add(panel);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getRequiredHeight()
     */
    @Override
    public int getRequiredHeight() {

        return 255;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    public CmsSearchTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * Removes the given parameter type.<p>
     *
     * @param type the parameter type
     */
    public void removeParameter(ParamType type) {

        switch (type) {
            case language:
                m_localeSelection.reset();
                break;
            case text:
                m_searchInput.setFormValueAsString("");
                break;
            case expired:
                m_includeExpiredCheckBox.setChecked(false);
                break;
            case creation:
                m_dateCreatedStartDateBox.setValue(null, true);
                m_dateCreatedEndDateBox.setValue(null, true);
                break;
            case modification:
                m_dateModifiedStartDateBox.setValue(null, true);
                m_dateModifiedEndDateBox.setValue(null, true);
                break;
            case scope:
                m_scopeSelection.setFormValueAsString(m_defaultScope.name());
                break;
            default:
        }
    }

    /**
     * Clears the search tab input.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_clearButton")
    protected void clearInput(ClickEvent event) {

        clearInput();
    }

    /**
     * Handles changes of date created range end box.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_dateCreatedEndDateBox")
    protected void onDateCreatedEndChange(ValueChangeEvent<Date> event) {

        if (event.getValue() != null) {
            m_tabHandler.setDateCreatedEnd(event.getValue().getTime());
        } else {
            // if the field is empty take the max value
            m_tabHandler.setDateCreatedEnd(-1L);
        }
    }

    /**
     * Handles changes of date created range start box.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_dateCreatedStartDateBox")
    protected void onDateCreatedStartChange(ValueChangeEvent<Date> event) {

        if (event.getValue() != null) {
            m_tabHandler.setDateCreatedStart(event.getValue().getTime());
        } else {
            // if the field is empty take the min value
            m_tabHandler.setDateCreatedStart(-1L);
        }
    }

    /**
     * Handles changes of date modified range end box.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_dateModifiedEndDateBox")
    protected void onDateModifiedEndChange(ValueChangeEvent<Date> event) {

        if (event.getValue() != null) {
            m_tabHandler.setDateModifiedEnd(event.getValue().getTime());
        } else {
            // if the field is empty take the max value
            m_tabHandler.setDateModifiedEnd(-1L);
        }
    }

    /**
     * Handles changes of date modified range start box.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_dateModifiedStartDateBox")
    protected void onDateModifiedStartChange(ValueChangeEvent<Date> event) {

        if (event.getValue() != null) {
            m_tabHandler.setDateModifiedStart(event.getValue().getTime());
        } else {
            // if the field is empty take the min value
            m_tabHandler.setDateModifiedStart(-1L);
        }
    }

    /**
     * Handles changes of the include expired check box.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_includeExpiredCheckBox")
    protected void onIncludeExpiredChange(ValueChangeEvent<Boolean> event) {

        Boolean value = event.getValue();
        m_tabHandler.setIncludeExpired(value.booleanValue(), true);
    }

    /**
     * Handles the change event of the locale select box.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_localeSelection")
    protected void onLocaleChange(ValueChangeEvent<String> event) {

        String value = event.getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value) || value.equals(NOT_SET_OPTION_VALUE)) {
            value = m_currentLocale;
        }
        m_tabHandler.setLocale(value);
    }

    /**
     * Handles the change event on the search scope select box.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_scopeSelection")
    protected void onScopeChange(ValueChangeEvent<String> event) {

        String value = event.getValue();
        m_tabHandler.setScope(CmsGallerySearchScope.valueOf(value));

    }

    /**
     * Handles search input change events.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_searchInput")
    protected void onSearchInputChange(ValueChangeEvent<String> event) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(event.getValue()) && (event.getValue().length() >= 3)) {
            m_tabHandler.setSearchQuery(event.getValue());
        } else {
            m_tabHandler.setSearchQuery(null);
        }
    }

    /**
     * Handles key press events of the search input field.<p>
     *
     * @param event the key press event
     */
    @UiHandler("m_searchInput")
    protected void onSearchInputKeyPress(KeyPressEvent event) {

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
     * Starts the search.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_searchButton")
    protected void startSearch(ClickEvent event) {

        getTabHandler().selectResultTab();
    }
}