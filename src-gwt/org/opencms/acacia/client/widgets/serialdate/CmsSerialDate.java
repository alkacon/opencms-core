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
import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.Month;
import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.acacia.shared.rpc.I_CmsSerialDateService;
import org.opencms.acacia.shared.rpc.I_CmsSerialDateServiceAsync;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Composite;

/** Controller for the serial date widget, being the widget implementation itself. */
public class CmsSerialDate extends Composite implements I_CmsEditWidget, I_ChangeHandler {

    /** Confirmation dialog that should be shown when exceptions are removed when the value changes. */
    private class CmsExceptionsDeleteConfirmDialog {

        /** The confirmation dialog that is shown. */
        CmsConfirmDialog m_dialog;
        /** The command to execute on confirmation. */
        Command m_cmd;

        /** The value to reset the exceptions */
        final CmsSerialDateValueWrapper m_value;

        /**
         * Default constructor.
         * @param value the value where the exceptions should be reset.
         */
        CmsExceptionsDeleteConfirmDialog(final CmsSerialDateValueWrapper value) {
            m_value = value;
            m_dialog = new CmsConfirmDialog(
                Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_EXCEPTION_DIALOG_CAPTION_0),
                Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_EXCEPTION_DIALOG_MESSAGE_0));
            m_dialog.setOkText(Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_EXCEPTION_DIALOG_YES_BUTTON_0));
            m_dialog.setCloseText(Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_EXCEPTION_DIALOG_NO_BUTTON_0));
            m_dialog.setHandler(new I_CmsConfirmDialogHandler() {

                public void onClose() {

                    handleClose();

                }

                public void onOk() {

                    handleOk();

                }
            });
        }

        /**
         * Show the dialog.
         * @param okCmd the command to execute when "ok" is clicked.
         */
        public void show(final Command okCmd) {

            m_cmd = okCmd;
            m_dialog.center();
        }

        /**
         * Method called when the dialog is closed not performing the action.
         */
        void handleClose() {

            m_view.onValueChange();
            m_dialog.hide();
        }

        /**
         * Method called when the dialog is closed performing the action.
         */
        void handleOk() {

            m_value.clearExceptions();
            m_cmd.execute();
            m_dialog.hide();
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

    /** The confirmation dialog for deletion of exceptions. */
    private CmsExceptionsDeleteConfirmDialog m_exceptionConfirmDialog;

    /**
     * Category field widgets for ADE forms.<p>
     */
    public CmsSerialDate() {

        m_model = new CmsSerialDateValueWrapper();
        m_view = new CmsSerialDateView(this, m_model);
        initWidget(m_view);
        initPatternControllers();
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
        m_exceptionConfirmDialog = new CmsExceptionsDeleteConfirmDialog(m_model);
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
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#conditionallyRemoveExceptionsOnChange(com.google.gwt.user.client.Command, boolean)
     */
    @Override
    public void conditionallyRemoveExceptionsOnChange(Command action, boolean showDialog) {

        if (m_model.hasExceptions() && showDialog) {
            m_exceptionConfirmDialog.show(action);
        } else {
            action.execute();
        }
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
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#removeExceptionsOnChange(com.google.gwt.user.client.Command)
     */
    @Override
    public void removeExceptionsOnChange(Command cmd) {

        conditionallyRemoveExceptionsOnChange(cmd, true);

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
    public void setEndTime(final Date date) {

        if (!Objects.equals(m_model.getEnd(), date)) {
            m_model.setEnd(date);
            valueChanged();
        }

    }

    /**
     * Set the duration option.
     * @param value the duration option to set ({@link EndType} as string).
     */
    public void setEndType(final String value) {

        final EndType endType = EndType.valueOf(value);
        if (!endType.equals(m_model.getEndType())) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    switch (endType) {
                        case SINGLE:
                            m_model.setOccurrences(0);
                            m_model.setSeriesEndDate(null);
                            break;
                        case TIMES:
                            m_model.setOccurrences(1);
                            m_model.setSeriesEndDate(null);
                            break;
                        case DATE:
                            m_model.setOccurrences(0);
                            m_model.setSeriesEndDate(new Date());
                            break;
                        default:
                            break;
                    }
                    m_model.setEndType(endType);
                    valueChanged();
                }
            });
        }

    }

    /**
     * Toggle between single events and series.
     * @param isSeries flag, indicating if we want a series of events.
     */
    public void setIsSeries(Boolean isSeries) {

        if (null != isSeries) {
            final boolean series = isSeries.booleanValue();
            setPattern(series ? PatternType.DAILY.toString() : PatternType.NONE.toString());
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

        final PatternType type = PatternType.valueOf(patternType);
        if (type != m_model.getPatternType()) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    EndType oldEndType = m_model.getEndType();
                    m_model.setPatternType(type);
                    m_model.setIndividualDates(null);
                    m_model.setInterval(1);
                    m_model.setEveryWorkingDay(Boolean.FALSE);
                    m_model.clearWeekDays();
                    m_model.clearIndividualDates();
                    m_model.clearWeeksOfMonth();
                    m_model.clearExceptions();
                    if (type.equals(PatternType.NONE) || type.equals(PatternType.INDIVIDUAL)) {
                        m_model.setEndType(EndType.SINGLE);
                    } else if (oldEndType.equals(EndType.SINGLE)) {
                        m_model.setEndType(EndType.TIMES);
                        m_model.setOccurrences(1);
                        m_model.setSeriesEndDate(null);
                    }
                    m_model.setDayOfMonth(1);
                    m_model.setMonth(Month.JANUARY);
                    valueChanged();
                }
            });
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
    public void setStartTime(final Date date) {

        if (!Objects.equals(m_model.getStart(), date)) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setStart(date);
                    valueChanged();
                }
            });
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
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#valueChanged()
     */
    @Override
    public void valueChanged() {

        m_view.onValueChange();
        ValueChangeEvent.fire(this, m_model.toString());
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