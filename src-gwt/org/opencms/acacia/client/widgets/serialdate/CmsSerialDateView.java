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

import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsErrorWidget;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/** The serial date widgets UI. */
public class CmsSerialDateView extends Composite
implements I_CmsSerialDateValueChangeObserver, CloseHandler<CmsFieldSet> {

    /** The UI binder interface. */
    interface I_CmsSerialDateUiBinder extends UiBinder<VerticalPanel, CmsSerialDateView> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsSerialDateUiBinder uiBinder = GWT.create(I_CmsSerialDateUiBinder.class);

    /* 1. The dates panel. */

    /** The panel with the basic dates. */
    @UiField
    CmsFieldSet m_datesPanel;

    /** The start time label. */
    @UiField
    Label m_startLabel;

    /** The start date box. */
    @UiField
    CmsDateBox m_startTime;

    /** The end time label. */
    @UiField
    Label m_endLabel;

    /** The end date box. */
    @UiField
    CmsDateBox m_endTime;

    /** The check box, indicating if the event should last the whole day. */
    @UiField
    CmsCheckBox m_wholeDayCheckBox;

    /* 2. The serial options panel */

    /** The check box, indicating if the date is a serial date. */
    @UiField
    CmsCheckBox m_seriesCheckBox;

    /* 2.1 The pattern options */

    /** The panel with all serial date options. */
    @UiField
    CmsFieldSet m_serialOptionsPanel;

    /** The panel with the serial date pattern options. */
    @UiField
    CmsFieldSet m_patternPanel;

    /** The pattern options panel, where the pattern specific options are displayed. */
    @UiField
    SimplePanel m_patternOptions;

    /** The daily pattern. */
    CmsPatternPanelDaily m_dailyPattern;

    /** The weekly pattern. */
    CmsPatternPanelWeekly m_weeklyPattern;

    /** The monthly pattern. */
    CmsPatternPanelMonthly m_monthlyPattern;

    /** The yearly pattern. */
    CmsPatternPanelYearly m_yearlyPattern;

    /* 2.2. The duration panel */

    /** The individual pattern. */
    CmsPatternPanelIndividual m_individualPattern;

    /** The panel with the serial date duration options. */
    @UiField
    CmsFieldSet m_durationPanel;

    /** The ends after radio button. */
    @UiField(provided = true)
    CmsRadioButton m_endsAfterRadioButton;

    /** The end datebox. */
    @UiField
    CmsDateBox m_seriesEndDate;

    /** The ends at radio button. */
    @UiField(provided = true)
    CmsRadioButton m_endsAtRadioButton;

    /* 2.3. The exceptions panel */

    /** The times text box. */
    @UiField
    TextBox m_occurrences;

    /** The panel with the serial date exceptions. */
    @UiField
    CmsFieldSet m_exceptionsPanel;

    /** The UI element for the list with exceptions */
    @UiField(provided = true)
    CmsCheckableDatePanel m_exceptionsList;

    /** Button to remove checked exceptions. */
    @UiField
    CmsPushButton m_removeExceptionsButton;

    /* 3. The manage exceptions and preview button that triggers the preview pop-up. */
    /** The button to manage exceptions. */
    @UiField
    CmsPushButton m_manageExceptionsButton;

    /* 4. The preview & exceptions selection pop-up */

    /** The preview list (with checkboxes to manage exceptions. */

    /** The widget to display "too many events" messages. */
    @UiField
    CmsErrorWidget m_errorWidget;

    /** The pop-up where the preview list is shown in. */
    CmsPopup m_overviewPopup;

    /* 5. Panel for de-active state */

    /** The list shown in the pop-up panel to manage exceptions. */
    CmsCheckableDatePanel m_overviewList;

    /** Panel only shown when the widget is deactivated. */
    @UiField
    FlowPanel m_deactivationPanel;

    /* Member variables for managing the internal state. */

    /** Information on the widget in deactivated state. */
    @UiField
    Label m_deactivationText;

    /** The panel to place the radio buttons for pattern selection. */
    @UiField
    VerticalPanel m_patternRadioButtonsPanel;

    /** Value of the radio group duration. */
    private CmsRadioButtonGroup m_groupDuration;

    /** Value of the radio group pattern. */
    private CmsRadioButtonGroup m_groupPattern;

    /** The pop-up panel's close button. */
    private CmsPushButton m_closeOverviewPopupButton;

    /** The pop-up panel's "update exceptions" button. */
    private CmsPushButton m_updateExceptionsButton;

    /* Date and time formats */

    /** Format with date only. */
    private String m_dateFormat = Messages.get().keyDefault(Messages.GUI_SERIALDATE_DATE_FORMAT_0, null);

    /* Controller and model */

    /** Controller */
    CmsSerialDate m_controller;

    /** Model */
    I_CmsObservableSerialDateValue m_model;

    /** Map from the various patterns to the radio buttons for chosing the patterns. */
    private Map<PatternType, CmsRadioButton> m_patternButtons;

    /**
     * Category field widgets for ADE forms.<p>
     * @param controller the controller to communicate with
     * @param model the model to get values from
     */
    public CmsSerialDateView(CmsSerialDate controller, I_CmsObservableSerialDateValue model) {

        m_controller = controller;
        m_model = model;
        m_model.registerValueChangeObserver(this);
        initDurationButtonGroup();
        m_exceptionsList = new CmsCheckableDatePanel(m_dateFormat);

        // bind the ui
        initWidget(uiBinder.createAndBindUi(this));

        initPatternButtonGroup();
        initDeactivationPanel();
        initDatesPanel();
        initSerialOptionsPanel();
        initOverviewPopup();
    }

    /**
     * Enable or disable the management button.
     * @param enable flag, indicating if the button should be enabled or disabled.
     */
    public void enableManagementButton(boolean enable) {

        m_manageExceptionsButton.setEnabled(enable);
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

        m_startTime.setValue(m_model.getStart());
        m_endTime.setValue(m_model.getEnd());

        onPatternChange();
    }

    /**
     * Shows the provided list of dates as current dates.
     * @param dates the current dates to show, accompanied with the information if they are exceptions or not.
     */
    public void showCurrentDates(Collection<CmsPair<Date, Boolean>> dates) {

        m_overviewList.setDatesWithCheckState(dates);
        m_overviewPopup.show();

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
     * Handle an end time change.
     * @param event the change event.
     */
    @UiHandler("m_endTime")
    void onEndTimeChange(ValueChangeEvent<Date> event) {

        m_controller.setEndTime(event.getValue());
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
                m_occurrences.setValue(occurrences > 0 ? "" + occurrences : "");
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

        m_controller.executeShowDatesAction();
    }

    /**
     * Handles the ends after change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_occurrences")
    void onOccurrencesChange(ValueChangeEvent<String> event) {

        m_controller.setOccurrences(m_occurrences.getValue());
    }

    /**
     * Handles the ends after focus event.<p>
     *
     * @param event the focus event
     */
    @UiHandler("m_occurrences")
    void onOccurrencesFocus(FocusEvent event) {

        m_groupDuration.selectButton(m_endsAfterRadioButton);
    }

    /**
     * Called when the pattern has changed.
     */
    void onPatternChange() {

        PatternType patternType = m_model.getPatternType();
        boolean isSeries = !patternType.equals(PatternType.NONE);
        m_serialOptionsPanel.setVisible(isSeries);
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

        m_controller.setIsSeries(event.getValue());
    }

    /**
     * Handles the end date change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_seriesEndDate")
    void onSeriesEndDateChange(ValueChangeEvent<Date> event) {

        m_controller.setSeriesEndDate(m_seriesEndDate.getValue());
    }

    /**
     * Handles the focus event on the series end date date box.
     * @param event the focus event
     */
    void onSeriesEndDateFocus(FocusEvent event) {

        m_groupDuration.selectButton(m_endsAtRadioButton);

    }

    /**
     * Handle a start time change
     * @param event the change event
     */
    @UiHandler("m_startTime")
    void onStartTimeChange(ValueChangeEvent<Date> event) {

        m_controller.setStartTime(event.getValue());
    }

    /**
     * Handle a whole day change event.
     * @param event the change event.
     */
    @UiHandler("m_wholeDayCheckBox")
    void onWholeDayChange(ValueChangeEvent<Boolean> event) {

        //TODO: Improve - adjust time selections?
        m_controller.setWholeDay(event.getValue());
    }

    /**
     * Removes the error message (if one is present).
     */
    void removeError() {

        m_errorWidget.setVisible(false);

    }

    /**
     * Sets the radio buttons active or inactive.<p>
     * @param active true or false to activate or deactivate
     * */
    void setActive(boolean active) {

        m_deactivationPanel.setVisible(!active);
        m_datesPanel.setVisible(active);
        if (m_seriesCheckBox.getFormValue().booleanValue()) {
            m_serialOptionsPanel.setVisible(active);
        }
        m_controller.sizeChanged();
    }

    /**
     * Displays the error widget with the provided error message.
     * @param errorMessage the error message to display.
     */
    void setError(String errorMessage) {

        m_errorWidget.setText(errorMessage);
        m_errorWidget.setVisible(true);
        m_controller.sizeChanged();

    }

    /**
     * Creates a pattern choice radio button and adds it where necessary.
     * @param pattern the pattern that should be chosen by the button.
     * @param messageKey the message key for the button's label.
     */
    private void createAndAddButton(PatternType pattern, String messageKey) {

        CmsRadioButton btn = new CmsRadioButton(pattern.toString(), Messages.get().key(messageKey));
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

        m_datesPanel.setLegend(Messages.get().key(Messages.GUI_SERIALDATE_PANEL_DATES_0));
        m_datesPanel.setOpenerVisible(false);

        Date now = new Date();

        m_startLabel.setText(Messages.get().key(Messages.GUI_SERIALDATE_TIME_STARTTIME_0));
        m_startTime.setValue(now);

        m_endLabel.setText(Messages.get().key(Messages.GUI_SERIALDATE_TIME_ENDTIME_0));
        m_endTime.setValue(now);

        m_seriesCheckBox.setText(Messages.get().key(Messages.GUI_SERIALDATE_SERIES_CHECKBOX_0));
        m_wholeDayCheckBox.setText(Messages.get().key(Messages.GUI_SERIALDATE_WHOLE_DAY_CHECKBOX_0));
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

                String value = event.getValue();
                if (null != value) {
                    m_controller.setEndType(value);
                }

            }
        });

    }

    /** Initialize elements from the duration panel. */
    private void initDurationPanel() {

        m_durationPanel.setLegend(Messages.get().key(Messages.GUI_SERIALDATE_PANEL_DURATION_0));
        m_durationPanel.setOpenerVisible(false);
        m_seriesEndDate.setDateOnly(true);
        m_seriesEndDate.getTextField().addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                onSeriesEndDateFocus(event);

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

        m_removeExceptionsButton.setText(Messages.get().key(Messages.GUI_SERIALDATE_BUTTON_REMOVE_EXCEPTIONS_0));
        m_removeExceptionsButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
        m_removeExceptionsButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_controller.updateExceptions(m_exceptionsList.getUncheckedDates());
            }
        });

    }

    /**
     * Initialize the ui elements for the management part.
     */
    private void initManagementPart() {

        m_errorWidget.getElement().getStyle().setMargin(5, Unit.PX);
        m_errorWidget.getElement().getStyle().setWidth(100, Unit.PCT);
        m_errorWidget.setVisible(false);
        m_manageExceptionsButton.setText(Messages.get().key(Messages.GUI_SERIALDATE_BUTTON_MANAGE_EXCEPTIONS_0));
        m_manageExceptionsButton.setEnabled(m_controller.isValid());
        m_manageExceptionsButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
    }

    /**
     * Initialize the overview/handle exceptions popup.
     */
    private void initOverviewPopup() {

        m_closeOverviewPopupButton = new CmsPushButton();
        m_closeOverviewPopupButton.setText("Close");
        m_updateExceptionsButton = new CmsPushButton();
        m_updateExceptionsButton.setText("Update Exceptions");
        m_updateExceptionsButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_controller.updateExceptions(m_overviewList.getUncheckedDates());
                m_overviewPopup.hide();

            }
        });
        m_overviewPopup = new CmsPopup("Dates", 800);
        m_overviewPopup.center();
        m_overviewPopup.setAutoHideEnabled(true);
        m_overviewPopup.addDialogClose(null);
        m_overviewPopup.addButton(m_closeOverviewPopupButton);
        m_closeOverviewPopupButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_overviewPopup.hide();

            }
        });
        m_overviewPopup.addButton(m_updateExceptionsButton);
        CmsScrollPanel panel = new CmsScrollPanel();
        m_overviewList = new CmsCheckableDatePanel(m_dateFormat);
        m_overviewList.addDate(new Date());
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
        createAndAddButton(PatternType.INDIVIDUAL, Messages.GUI_SERIALDATE_TYPE_INDIVIDUAL_0);

        m_groupPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                String value = event.getValue();
                if (value != null) {
                    m_controller.setPattern(value);
                }
            }
        });

    }

    /**
     * Create the pattern panels.
     */
    private void initPatternPanel() {

        m_patternPanel.setLegend(Messages.get().key(Messages.GUI_SERIALDATE_PANEL_PATTERN_0));
        m_patternPanel.setOpenerVisible(false);
    }

    /** Initialize elements from the serial options panel (and all sub-panels). */
    private void initSerialOptionsPanel() {

        m_serialOptionsPanel.setLegend(Messages.get().key(Messages.GUI_SERIALDATE_PANEL_SERIAL_OPTIONS_0));
        m_serialOptionsPanel.addCloseHandler(this);
        m_serialOptionsPanel.setVisible(false);
        initPatternPanel();
        initDurationPanel();
        initExceptionsPanel();
        initManagementPart();
    }

}
