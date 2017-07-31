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

package org.opencms.acacia.client.widgets.serialdate;

import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.acacia.shared.CmsSerialDateUtil;
import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.acacia.shared.rpc.I_CmsSerialDateService;
import org.opencms.acacia.shared.rpc.I_CmsSerialDateServiceAsync;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsPair;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Composite;

/** Controller for the serial date widget, being the widget implementation itself. */
public class CmsSerialDate extends Composite implements I_CmsEditWidget, I_ChangeHandler, I_ValidationHandler {

    /**
     * The validation timer.<p>
     * Validation takes place at most every 500 milliseconds.
     * Whenever re-validation is triggered during the time waiting, the timer is reset,
     * i.e., it is waited for 500 milliseconds again.
     */
    protected static class ValidationTimer extends Timer {

        /** The single validation timer that is allowed. */
        private static ValidationTimer m_validationTimer;
        /** The handler to call for the validation. */
        private I_ValidationHandler m_handler;

        /**
         * Constructor.<p>
         * @param handler the handler to call for the validation.
         */
        private ValidationTimer(I_ValidationHandler handler) {
            m_handler = handler;
        }

        /**
         * Trigger the validation after a certain time out.
         * @param handler the validation handler, that actually performs the validation.
         */
        public static void validate(I_ValidationHandler handler) {

            if (null != m_validationTimer) {
                m_validationTimer.cancel();
            }
            m_validationTimer = new ValidationTimer(handler);
            m_validationTimer.schedule(500);
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            m_handler.validate();
            m_validationTimer = null;
        }

    }

    /** Default date format. */
    public static final String DEFAULT_DATE_FORMAT = "EEEE, MMMM dd, yyyy";

    /** The RPC service for the serial date widget. */
    private static I_CmsSerialDateServiceAsync SERVICE;

    /** Flag, indicating if the widget is currently active. */
    private boolean m_active;

    /** Flag, indicating if the widget value is valid. */
    private boolean m_valid;

    /** RPC call action to get dates. */
    private final CmsRpcAction<Collection<CmsPair<Date, Boolean>>> m_getDatesAction;

    /** RPC call action to get dates. */
    private final CmsRpcAction<CmsPair<Boolean, Date>> m_hasTooManyDatesAction;

    /** The pattern controllers. */
    private final Map<PatternType, I_CmsSerialDatePatternController> m_patternControllers = new HashMap<>();

    /* Date and time formats */

    /** Format with date only. */
    DateTimeFormat m_dateFormat = DateTimeFormat.getFormat(
        Messages.get().keyDefault(Messages.GUI_SERIALDATE_DATE_FORMAT_0, DEFAULT_DATE_FORMAT));

    /** The view */
    final CmsSerialDateView m_view;

    /** The model */
    final CmsSerialDateValueWrapper m_model;

    /**
     * Category field widgets for ADE forms.<p>
     */
    public CmsSerialDate() {

        m_model = new CmsSerialDateValueWrapper();
        m_view = new CmsSerialDateView(this, m_model);
        initWidget(m_view);
        initPatternControllers();
        m_hasTooManyDatesAction = new CmsRpcAction<CmsPair<Boolean, Date>>() {

            @Override
            public void execute() {

                getService().hasTooManyDates(m_model.toString(), this);

            }

            @Override
            protected void onResponse(CmsPair<Boolean, Date> result) {

                if (result.getFirst().booleanValue()) {
                    onValidated(
                        Messages.get().key(
                            Messages.GUI_SERIALDATE_ERROR_SERIESEND_TOO_FAR_AWAY_1,
                            m_dateFormat.format(result.getSecond())));
                } else {
                    onValidated(null);
                }

            }

        };
        m_getDatesAction = new CmsRpcAction<Collection<CmsPair<Date, Boolean>>>() {

            @Override
            public void execute() {

                getService().getDates(m_model.toString(), this);

            }

            @Override
            protected void onResponse(Collection<CmsPair<Date, Boolean>> result) {

                CmsSerialDate.this.m_view.showCurrentDates(result);
            }
        };
        m_active = true;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Show the dates for managing exceptions.
     */
    public void executeShowDatesAction() {

        m_getDatesAction.execute();

    }

    /**
     * Returns the controller for the currently active pattern.
     * @return the controller for the currently active pattern.
     */
    public I_CmsSerialDatePatternController getPattern() {

        return m_patternControllers.get(m_model.getPatternType());
    }

    /**
     * Returns the widget to place in the pattern panel.
     * @return the widget to place in the pattern panel.
     */
    public I_CmsPatternView getPatternView() {

        return getPattern().getView();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_model.toString();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * Returns a flag, indicating if the widget value is valid.
     * @return a flag, indicating if the widget value is valid.
     */
    public boolean isValid() {

        return m_valid;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        return getElement().isOrHasChild(element);
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (active != m_active) {
            m_active = active;
            m_view.setActive(active);
        }

    }

    /**
     * Set the end time.
     * @param date the end time to set.
     */
    public void setEndTime(Date date) {

        if (!Objects.equals(m_model.getEnd(), date)) {
            m_model.setEnd(date);
            valueChanged();
        }

    }

    /**
     * Set the duration option.
     * @param value the duration option to set ({@link EndType} as string).
     */
    public void setEndType(String value) {

        EndType endType = EndType.valueOf(value);
        if (!endType.equals(m_model.getEndType())) {
            m_model.setEndType(endType);
            m_view.onEndTypeChange();
            valueChanged();
        }

    }

    /**
     * Set the flag, indicating that the event takes place every working day.
     * @param isEveryWorkingDay flag, indicating that the event takes place every working day.
     */
    public void setEveryWorkingDay(boolean isEveryWorkingDay) {

        if (m_model.isEveryWorkingDay() != isEveryWorkingDay) {
            m_model.setEveryWorkingDay(isEveryWorkingDay);
            valueChanged();
        }
    }

    /**
     * Toggle between single events and series.
     * @param isSeries flag, indicating if we want a series of events.
     */
    public void setIsSeries(Boolean isSeries) {

        if (null != isSeries) {
            boolean noSeries = !isSeries.booleanValue();
            if (noSeries ^ m_model.getPatternType().equals(PatternType.NONE)) {
                if (noSeries) {
                    m_model.setPatternType(PatternType.NONE);
                } else {
                    m_model.setPatternType(PatternType.DAILY);
                }
                m_view.onPatternChange();
                valueChanged();
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // not necessary

    }

    /**
     * Set the occurrences. If the String is invalid, the occurrences will be set to "-1" to cause server-side validation to fail.
     * @param occurrences the interval to set.
     */
    public void setOccurrences(String occurrences) {

        int o = CmsSerialDateUtil.toIntWithDefault(occurrences, -1);
        if (m_model.getOccurrences() != o) {
            m_model.setOccurrences(o);
            valueChanged();
        }
    }

    /**
     * Set the serial pattern type.
     * @param patternType the pattern type to set.
     */
    public void setPattern(String patternType) {

        PatternType type = PatternType.valueOf(patternType);
        if (type != m_model.getPatternType()) {
            m_model.setPatternType(type);
            m_view.onPatternChange();
            valueChanged();
        }

    }

    /**
     * Set the serial end date.
     * @param date the serial end date.
     */
    public void setSeriesEndDate(Date date) {

        if (!Objects.equals(m_model.getSeriesEndDate(), date)) {
            m_model.setSeriesEndDate(date);
            valueChanged();
        }

    }

    /**
     * Set the start time.
     * @param date the start time to set.
     */
    public void setStartTime(Date date) {

        if (!Objects.equals(m_model.getStart(), date)) {
            m_model.setStart(date);
            valueChanged();
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setValue(java.lang.String, boolean)
     */
    public void setValue(String value, boolean fireEvent) {

        if (!m_model.toString().equals(value)) {
            m_model.setValue(value);
            if (fireEvent) {
                valueChanged();
            } else {
                validate();
            }
        }
    }

    /**
     * Sets the whole day flag.
     * @param isWholeDay flag, indicating if the event lasts whole days.
     */
    public void setWholeDay(Boolean isWholeDay) {

        if (m_model.isWholeDay() ^ ((null != isWholeDay) && isWholeDay.booleanValue())) {
            m_model.setWholeDay(isWholeDay);
            valueChanged();
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#sizeChanged()
     */
    public void sizeChanged() {

        CmsDomUtil.resizeAncestor(m_view.getParent());
    }

    /**
     * Updates the exceptions.
     * @param exceptions the exceptions to set
     */
    public void updateExceptions(SortedSet<Date> exceptions) {

        SortedSet<Date> e = null == exceptions ? new TreeSet<Date>() : exceptions;
        if (!m_model.getExceptions().equals(e)) {
            m_model.setExceptions(e);
            m_view.updateExceptions();
            valueChanged();
            sizeChanged();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_ValidationHandler#validate()
     */
    @Override
    public void validate() {

        String errorMessage = checkIfTimesAreValid();
        if (null == errorMessage) {
            errorMessage = m_patternControllers.get(m_model.getPatternType()).validate();
            if (null == errorMessage) {
                errorMessage = m_model.getEndType().equals(EndType.DATE)
                ? checkIfSeriesEndTimeIsValid()
                : checkIfOccurrencesAreValid();
                if ((null == errorMessage) && m_model.getEndType().equals(EndType.DATE)) {
                    m_hasTooManyDatesAction.execute();
                    return;
                }
            }
        }
        onValidated(errorMessage);

    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#valueChanged()
     */
    @Override
    public void valueChanged() {

        ValueChangeEvent.fire(this, m_model.toString());

        ValidationTimer.validate(this);
    }

    /**
     * Returns the RPC service for serial dates.
     * @return the RPC service for serial dates.
     */
    I_CmsSerialDateServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsSerialDateService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.contenteditor.CmsSerialDateService.gwt");
            ((ServiceDefTarget)SERVICE).setServiceEntryPoint(serviceUrl);
        }
        return SERVICE;
    }

    /**
     * Call-back function when validation finishes.
     * @param errorMessage the validation error to display, <code>null</code> if validation succeeded.
     */
    void onValidated(String errorMessage) {

        if (null == errorMessage) {
            setValid(true);
            m_view.removeError();
        } else {
            setValid(false);
            m_view.setError(errorMessage);
        }
    }

    /**
     * Set a flag, indicating if the value in the widget is valid.
     * @param isValid flag, indicating if the value in the widget is valid.
     */
    void setValid(boolean isValid) {

        if (isValid != m_valid) {
            m_valid = isValid;
            m_view.enableManagementButton(m_valid);
        }
    }

    /**
     * Checks if the occurrences are valid.
     * @return <code>null</code> if occurrences are valid, as suitable error message otherwise.
     */
    private String checkIfOccurrencesAreValid() {

        return (!m_model.getEndType().equals(EndType.TIMES) || (m_model.getOccurrences() > 0))
            && (m_model.getOccurrences() <= CmsSerialDateUtil.getMaxEvents())
            ? null
            : Messages.get().key(
                Messages.GUI_SERIALDATE_ERROR_INVALID_OCCURRENCES_1,
                Integer.valueOf(CmsSerialDateUtil.getMaxEvents()));
    }

    /**
     * Returns <code>null</code> if the series end time is valid, a suitable error message otherwise.
     * @return <code>null</code> if the series end time is valid, a suitable error message otherwise.
     */
    private String checkIfSeriesEndTimeIsValid() {

        return m_model.getStart().getTime() < (m_model.getSeriesEndDate().getTime()
            + I_CmsSerialDateValue.DAY_IN_MILLIS)
            ? null
            : Messages.get().key(Messages.GUI_SERIALDATE_ERROR_SERIAL_END_BEFORE_START_0);
    }

    /**
     * Returns <code>null</code> if the start date is not after the end date, a suitable error message otherwise.
     * @return <code>null</code> if the start date is not after the end date, a suitable error message otherwise.
     */
    private String checkIfTimesAreValid() {

        return m_model.getStart().after(m_model.getEnd())
        ? Messages.get().key(Messages.GUI_SERIALDATE_ERROR_END_BEFORE_START_0)
        : null;
    }

    /**
     * Initialize the pattern controllers.
     */
    private void initPatternControllers() {

        m_patternControllers.put(PatternType.NONE, new CmsPatternPanelNoneController());
        m_patternControllers.put(PatternType.DAILY, new CmsPatternPanelDailyController(m_model, this));
        m_patternControllers.put(PatternType.WEEKLY, new CmsPatternPanelWeeklyController(m_model, this));
        m_patternControllers.put(PatternType.MONTHLY, new CmsPatternPanelMonthlyController(m_model, this));
        m_patternControllers.put(PatternType.YEARLY, new CmsPatternPanelYearlyController(m_model, this));
        m_patternControllers.put(PatternType.INDIVIDUAL, new CmsPatternPanelIndividualController(m_model, this));
    }
}