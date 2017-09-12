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

package org.opencms.acacia.client.widgets.serialdate;

import org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle;
import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBoxEvent;
import org.opencms.util.CmsPair;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/** The serial date widgets UI. */
public class CmsSerialDateView extends Composite
implements I_CmsSerialDateValueChangeObserver, CloseHandler<CmsFieldSet> {

    /** The UI binder interface. */
    interface I_CmsSerialDateUiBinder extends UiBinder<Widget, CmsSerialDateView> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsSerialDateUiBinder uiBinder = GWT.create(I_CmsSerialDateUiBinder.class);

    /* The status field */

    /** Panel containing the field set for the exceptions. */
    @UiField
    protected FlowPanel m_exceptionsPanelContainer;

    /** Controller. */
    CmsSerialDateController m_controller;

    /* 1. The dates panel. */

    /** The check box, indicating if the event should be displayed as "current" till it ends (checked state), or only till it starts (unchecked). */
    @UiField
    CmsCheckBox m_currentTillEndCheckBox;

    /** The daily pattern. */
    CmsPatternPanelDailyView m_dailyPattern;

    /** The panel with the basic dates. */
    @UiField
    FlowPanel m_datesPanel;

    /** Panel only shown when the widget is deactivated. */
    @UiField
    FlowPanel m_deactivationPanel;

    /** Information on the widget in deactivated state. */
    @UiField
    Label m_deactivationText;

    /** The panel with the serial date duration options. */
    @UiField
    FlowPanel m_durationPanel;

    /** The end time label. */
    @UiField
    Label m_endLabel;

    /* 2. The serial options panel */

    /** The ends after radio button. */
    @UiField(provided = true)
    CmsRadioButton m_endsAfterRadioButton;

    /* 2.1 The pattern options */

    /** The ends at radio button. */
    @UiField(provided = true)
    CmsRadioButton m_endsAtRadioButton;

    /** The end date box. */
    @UiField
    CmsDateBox m_endTime;

    /** The UI element for the list with exceptions. */
    @UiField(provided = true)
    CmsCheckableDatePanel m_exceptionsList;

    /** The panel with the serial date exceptions. */
    @UiField
    CmsFieldSet m_exceptionsPanel;

    /** The individual pattern. */
    CmsPatternPanelIndividualView m_individualPattern;

    /* 3. The manage exceptions and preview button that triggers the preview pop-up. */
    /** The button to manage exceptions. */
    @UiField
    CmsPushButton m_manageExceptionsButton;

    /** Model. */
    I_CmsObservableSerialDateValue m_model;

    /* 2.2. The duration panel */

    /** The monthly pattern. */
    CmsPatternPanelMonthlyView m_monthlyPattern;

    /** The times text box. */
    @UiField
    CmsFocusAwareTextBox m_occurrences;

    /** The label that displays the dependency to another series. */
    @UiField
    CmsLabel m_origSeriesLabel;

    /** The list shown in the pop-up panel to manage exceptions. */
    CmsCheckableDatePanel m_overviewList;

    /** The pop-up where the preview list is shown in. */
    CmsPopup m_overviewPopup;

    /** The label at the start of the duration line (text is "Ends"). */
    @UiField
    Label m_durationPrefixLabel;

    /** The label after the occurrences text box (text is "times"). */
    @UiField
    Label m_durationAfterPostfixLabel;

    /* 2.3. The exceptions panel */

    /** The pattern options panel, where the pattern specific options are displayed. */
    @UiField
    SimplePanel m_patternOptions;

    /** The panel with the serial date pattern options. */
    @UiField
    FlowPanel m_patternPanel;

    /** The panel to place the radio buttons for pattern selection. */
    @UiField
    VerticalPanel m_patternRadioButtonsPanel;

    /** The panel with all serial date options. */
    @UiField
    CmsFieldSet m_serialOptionsPanel;

    /* 4. The preview & exceptions selection pop-up */

    /** The preview list (with checkboxes to manage exceptions. */

    /** The check box, indicating if the date is a serial date. */
    @UiField
    CmsCheckBox m_seriesCheckBox;

    /* 5. Panel for de-active state */

    /** The end datebox. */
    @UiField
    CmsDateBox m_seriesEndDate;

    /** The start time label. */
    @UiField
    Label m_startLabel;

    /* Member variables for managing the internal state. */

    /** The start date box. */
    @UiField
    CmsDateBox m_startTime;

    /** The label that displays the current status. */
    @UiField
    CmsLabel m_statusLabel;

    /** The weekly pattern. */
    CmsPatternPanelWeeklyView m_weeklyPattern;

    /** The check box, indicating if the event should last the whole day. */
    @UiField
    CmsCheckBox m_wholeDayCheckBox;

    /** The yearly pattern. */
    CmsPatternPanelYearlyView m_yearlyPattern;

    /** Format with date only. */
    private String m_dateFormat = Messages.get().keyDefault(Messages.GUI_SERIALDATE_DATE_FORMAT_0, null);

    /* Date and time formats */

    /** Value of the radio group duration. */
    private CmsRadioButtonGroup m_groupDuration;

    /* Controller and model */

    /** Value of the radio group pattern. */
    private CmsRadioButtonGroup m_groupPattern;

    /** Map from the various patterns to the radio buttons for chosing the patterns. */
    private Map<PatternType, CmsRadioButton> m_patternButtons;

    /** Flag, indicating if change actions should not be triggered. */
    private boolean m_triggerChangeActions = true;

    /** The pop-up panel's "update exceptions" button. */
    private CmsPushButton m_updateExceptionsButton;

    /**
     * Category field widgets for ADE forms.<p>
     * @param controller the controller to communicate with
     * @param model the model to get values from
     */
    public CmsSerialDateView(CmsSerialDateController controller, I_CmsObservableSerialDateValue model) {

        m_controller = controller;
        m_model = model;
        m_model.registerValueChangeObserver(this);
        initDurationButtonGroup();
        m_exceptionsList = new CmsCheckableDatePanel(m_dateFormat, CmsCheckableDatePanel.Style.THREE_COLUMNS, true);

        // bind the ui
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().highTextBoxes());

        m_origSeriesLabel.setVisible(m_model.isFromOtherSeries());
        m_origSeriesLabel.setText(Messages.get().key(Messages.GUI_SERIALDATE_FROM_SERIES_INFORMATION_0));

        initPatternButtonGroup();
        initDeactivationPanel();
        initDatesPanel();
        initSerialOptionsPanel();
        initOverviewPopup();
    }

    /**
     * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
     */
    public void onClose(CloseEvent<CmsFieldSet> event) {

        m_controller.sizeChanged();

    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver#onValueChange()
     */
    @Override
    public void onValueChange() {

        m_triggerChangeActions = false;
        m_origSeriesLabel.setVisible(m_model.isFromOtherSeries());
        m_startTime.setValue(m_model.getStart());
        m_endTime.setValue(m_model.getEnd());
        m_wholeDayCheckBox.setChecked(m_model.isWholeDay());
        m_currentTillEndCheckBox.setChecked(m_model.isCurrentTillEnd());

        onPatternChange();
        m_triggerChangeActions = true;
    }

    /**
     * Enable/disable the management button.
     * @param enabled flag, indicating if the management button should be enabled.
     */
    public void setManagementButtonEnabled(boolean enabled) {

        m_manageExceptionsButton.setEnabled(enabled);

    }

    /**
     * Sets the current status.
     * @param status the status to set.
     */
    public void setStatus(String status) {

        m_statusLabel.setText(status);
    }

    /**
     * Shows the provided list of dates as current dates.
     * @param dates the current dates to show, accompanied with the information if they are exceptions or not.
     */
    public void showCurrentDates(Collection<CmsPair<Date, Boolean>> dates) {

        m_overviewList.setDatesWithCheckState(dates);
        m_overviewPopup.center();

    }

    /**
     * Updates the exceptions panel.
     */
    public void updateExceptions() {

        m_exceptionsList.setDates(m_model.getExceptions());
        if (m_model.getExceptions().size() > 0) {
            m_exceptionsPanel.setVisible(true);
        } else {
            m_exceptionsPanel.setVisible(false);
        }
    }

    /**
     * Returns a flag, indicating if change actions should be triggered.
     * @return a flag, indicating if change actions should be triggered.
     */
    boolean handleChange() {

        return m_triggerChangeActions;
    }

    /**
     * Handle a "current till end" change event.
     * @param event the change event.
     */
    @UiHandler("m_currentTillEndCheckBox")
    void onCurrentTillEndChange(ValueChangeEvent<Boolean> event) {

        if (handleChange()) {
            m_controller.setCurrentTillEnd(event.getValue());
        }
    }

    /**
     * Handle an end time change.
     * @param event the change event.
     */
    @UiHandler("m_endTime")
    void onEndTimeChange(CmsDateBoxEvent event) {

        if (handleChange() && !event.isUserTyping()) {
            m_controller.setEndTime(event.getDate());
        }
    }

    /**
     * Called when the end type is changed.
     */
    void onEndTypeChange() {

        EndType endType = m_model.getEndType();
        m_groupDuration.selectButton(getDurationButtonForType(endType));
        switch (endType) {
            case DATE:
            case TIMES:
                m_durationPanel.setVisible(true);
                m_seriesEndDate.setValue(m_model.getSeriesEndDate());
                int occurrences = m_model.getOccurrences();
                if (!m_occurrences.isFocused()) {
                    m_occurrences.setFormValueAsString(occurrences > 0 ? "" + occurrences : "");
                }
                break;
            default:
                m_durationPanel.setVisible(false);
                break;
        }
        updateExceptions();
    }

    /**
     * Handler for click events on the manage exceptions button.
     * @param e the click event.
     */
    @UiHandler("m_manageExceptionsButton")
    void onManageExceptionClicked(ClickEvent e) {

        if (handleChange()) {
            m_controller.executeShowDatesAction();
        }
    }

    /**
     * Handles the ends after change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_occurrences")
    void onOccurrencesChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setOccurrences(m_occurrences.getFormValueAsString());
        }
    }

    /**
     * Handles the ends after focus event.<p>
     *
     * @param event the focus event
     */
    @UiHandler("m_occurrences")
    void onOccurrencesFocus(FocusEvent event) {

        if (handleChange()) {
            m_groupDuration.selectButton(m_endsAfterRadioButton);
        }
    }

    /**
     * Called when the pattern has changed.
     */
    void onPatternChange() {

        PatternType patternType = m_model.getPatternType();
        boolean isSeries = !patternType.equals(PatternType.NONE);
        setSerialOptionsVisible(isSeries);
        m_seriesCheckBox.setChecked(isSeries);
        if (isSeries) {
            m_groupPattern.selectButton(m_patternButtons.get(patternType));
            m_controller.getPatternView().onValueChange();
            m_patternOptions.setWidget(m_controller.getPatternView());
            onEndTypeChange();
        }
        m_controller.sizeChanged();
    }

    /**
     * Handle changes of the series check box.
     * @param event the change event.
     */
    @UiHandler("m_seriesCheckBox")
    void onSeriesChange(ValueChangeEvent<Boolean> event) {

        if (handleChange()) {
            m_controller.setIsSeries(event.getValue());
        }
    }

    /**
     * Handles the end date change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_seriesEndDate")
    void onSeriesEndDateChange(CmsDateBoxEvent event) {

        if (handleChange() && !event.isUserTyping()) {
            m_controller.setSeriesEndDate(event.getDate());
        }
    }

    /**
     * Handles the focus event on the series end date date box.
     * @param event the focus event
     */
    void onSeriesEndDateFocus(FocusEvent event) {

        m_groupDuration.selectButton(m_endsAtRadioButton);

    }

    /**
     * Handle a start time change.
     *
     * @param event the change event
     */
    @UiHandler("m_startTime")
    void onStartTimeChange(CmsDateBoxEvent event) {

        if (handleChange() && !event.isUserTyping()) {
            m_controller.setStartTime(event.getDate());
        }
    }

    /**
     * Handle a whole day change event.
     * @param event the change event.
     */
    @UiHandler("m_wholeDayCheckBox")
    void onWholeDayChange(ValueChangeEvent<Boolean> event) {

        //TODO: Improve - adjust time selections?
        if (handleChange()) {
            m_controller.setWholeDay(event.getValue());
        }
    }

    /**
     * Sets the radio buttons active or inactive.<p>
     * @param active true or false to activate or deactivate
     * */
    void setActive(boolean active) {

        m_deactivationPanel.setVisible(!active);
        m_datesPanel.setVisible(active);
        if (m_seriesCheckBox.getFormValue().booleanValue()) {
            setSerialOptionsVisible(active);
        }
        m_controller.sizeChanged();
    }

    /**
     * Creates a pattern choice radio button and adds it where necessary.
     * @param pattern the pattern that should be chosen by the button.
     * @param messageKey the message key for the button's label.
     */
    private void createAndAddButton(PatternType pattern, String messageKey) {

        CmsRadioButton btn = new CmsRadioButton(pattern.toString(), Messages.get().key(messageKey));
        btn.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().radioButtonlabel());
        btn.setGroup(m_groupPattern);
        m_patternButtons.put(pattern, btn);
        m_patternRadioButtonsPanel.add(btn);

    }

    /**
     * Returns the duration button for type.<p>
     *
     * @param type the type
     *
     * @return the button
     */
    private CmsRadioButton getDurationButtonForType(EndType type) {

        switch (type) {
            case DATE:
                return m_endsAtRadioButton;
            default:
                return m_endsAfterRadioButton;
        }
    }

    /** Initialize dates panel elements. */
    private void initDatesPanel() {

        m_startLabel.setText(Messages.get().key(Messages.GUI_SERIALDATE_TIME_STARTTIME_0));
        m_startTime.setAllowInvalidValue(true);
        m_startTime.setValue(m_model.getStart());
        m_endLabel.setText(Messages.get().key(Messages.GUI_SERIALDATE_TIME_ENDTIME_0));
        m_endTime.setAllowInvalidValue(true);
        m_endTime.setValue(m_model.getEnd());
        m_seriesCheckBox.setText(Messages.get().key(Messages.GUI_SERIALDATE_SERIES_CHECKBOX_0));
        m_wholeDayCheckBox.setText(Messages.get().key(Messages.GUI_SERIALDATE_WHOLE_DAY_CHECKBOX_0));
        m_currentTillEndCheckBox.setText(Messages.get().key(Messages.GUI_SERIALDATE_CURRENT_TILL_END_CHECKBOX_0));
        m_currentTillEndCheckBox.getButton().setTitle(
            Messages.get().key(Messages.GUI_SERIALDATE_CURRENT_TILL_END_CHECKBOX_HELP_0));
    }

    /**
     * Initialize elements of the panel displayed for the deactivated widget.
     */
    private void initDeactivationPanel() {

        m_deactivationPanel.setVisible(false);
        m_deactivationText.setText(Messages.get().key(Messages.GUI_SERIALDATE_DEACTIVE_TEXT_0));

    }

    /**
     * Configure all UI elements in the "ending"-options panel.
     */
    private void initDurationButtonGroup() {

        m_groupDuration = new CmsRadioButtonGroup();

        m_endsAfterRadioButton = new CmsRadioButton(
            EndType.TIMES.toString(),
            Messages.get().key(Messages.GUI_SERIALDATE_DURATION_ENDTYPE_OCC_0));
        m_endsAfterRadioButton.setGroup(m_groupDuration);
        m_endsAtRadioButton = new CmsRadioButton(
            EndType.DATE.toString(),
            Messages.get().key(Messages.GUI_SERIALDATE_DURATION_ENDTYPE_DATE_0));
        m_endsAtRadioButton.setGroup(m_groupDuration);
        m_groupDuration.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                if (handleChange()) {
                    String value = event.getValue();
                    if (null != value) {
                        m_controller.setEndType(value);
                    }
                }
            }
        });

    }

    /** Initialize elements from the duration panel. */
    private void initDurationPanel() {

        m_durationPrefixLabel.setText(Messages.get().key(Messages.GUI_SERIALDATE_DURATION_PREFIX_0));
        m_durationAfterPostfixLabel.setText(Messages.get().key(Messages.GUI_SERIALDATE_DURATION_ENDTYPE_OCC_POSTFIX_0));
        m_seriesEndDate.setDateOnly(true);
        m_seriesEndDate.setAllowInvalidValue(true);
        m_seriesEndDate.setValue(m_model.getSeriesEndDate());
        m_seriesEndDate.getTextField().addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                if (handleChange()) {
                    onSeriesEndDateFocus(event);
                }

            }
        });
    }

    /**
     * Configure all UI elements in the exceptions panel.
     */
    private void initExceptionsPanel() {

        m_exceptionsPanel.setLegend(Messages.get().key(Messages.GUI_SERIALDATE_PANEL_EXCEPTIONS_0));
        m_exceptionsPanel.addCloseHandler(this);
        m_exceptionsPanel.setVisible(false);
    }

    /**
     * Initialize the ui elements for the management part.
     */
    private void initManagementPart() {

        m_manageExceptionsButton.setText(Messages.get().key(Messages.GUI_SERIALDATE_BUTTON_MANAGE_EXCEPTIONS_0));
        m_manageExceptionsButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
    }

    /**
     * Initialize the overview/handle exceptions popup.
     */
    private void initOverviewPopup() {

        m_updateExceptionsButton = new CmsPushButton();
        m_updateExceptionsButton.setText(Messages.get().key(Messages.GUI_LOCALE_DIALOG_OK_0));
        m_updateExceptionsButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                if (handleChange()) {
                    m_controller.updateExceptions(m_overviewList.getUncheckedDates());
                    m_overviewPopup.hide();
                }

            }
        });
        m_overviewPopup = new CmsPopup(Messages.get().key(Messages.GUI_SERIALDATE_OVERVIEW_POPUP_TITLE_0), 800);
        m_overviewPopup.center();
        m_overviewPopup.setAutoHideEnabled(true);
        m_overviewPopup.addDialogClose(null);
        m_overviewPopup.addButton(m_updateExceptionsButton);
        CmsScrollPanel panel = new CmsScrollPanel();
        m_overviewList = new CmsCheckableDatePanel(m_dateFormat, CmsCheckableDatePanel.Style.TWO_COLUMNS);
        m_overviewList.addDate(new Date());
        panel.getElement().getStyle().setPaddingLeft(10, Unit.PX);
        panel.getElement().getStyle().setProperty("maxHeight", m_overviewPopup.getAvailableHeight(0), Unit.PX);
        panel.add(m_overviewList);
        m_overviewPopup.add(panel);
        m_overviewPopup.hide();

    }

    /**
     * Initialize the pattern choice button group.
     */
    private void initPatternButtonGroup() {

        m_groupPattern = new CmsRadioButtonGroup();
        m_patternButtons = new HashMap<>();

        createAndAddButton(PatternType.DAILY, Messages.GUI_SERIALDATE_TYPE_DAILY_0);
        m_patternButtons.put(PatternType.NONE, m_patternButtons.get(PatternType.DAILY));
        createAndAddButton(PatternType.WEEKLY, Messages.GUI_SERIALDATE_TYPE_WEEKLY_0);
        createAndAddButton(PatternType.MONTHLY, Messages.GUI_SERIALDATE_TYPE_MONTHLY_0);
        createAndAddButton(PatternType.YEARLY, Messages.GUI_SERIALDATE_TYPE_YEARLY_0);
        // createAndAddButton(PatternType.INDIVIDUAL, Messages.GUI_SERIALDATE_TYPE_INDIVIDUAL_0);

        m_groupPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                if (handleChange()) {
                    String value = event.getValue();
                    if (value != null) {
                        m_controller.setPattern(value);
                    }
                }
            }
        });

    }

    /** Initialize elements from the serial options panel (and all sub-panels). */
    private void initSerialOptionsPanel() {

        m_serialOptionsPanel.setLegend(Messages.get().key(Messages.GUI_SERIALDATE_PANEL_SERIAL_OPTIONS_0));
        m_serialOptionsPanel.addCloseHandler(this);
        setSerialOptionsVisible(false);
        initDurationPanel();
        initExceptionsPanel();
        initManagementPart();
    }

    /**
     * Shows / hides serial options and exceptions.<p>
     *
     * @param visible  true if the widgets should be shown, false if they should be hidden
     */
    private void setSerialOptionsVisible(boolean visible) {

        m_serialOptionsPanel.setVisible(visible);
        m_exceptionsPanelContainer.setVisible(visible);
    }

}
