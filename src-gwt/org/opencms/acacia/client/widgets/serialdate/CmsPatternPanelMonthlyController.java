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

import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;

import com.google.gwt.user.client.Command;

/** Controller for the monthly pattern panel. */
public class CmsPatternPanelMonthlyController extends A_CmsPatternPanelController {

    /** The controlled view. */
    private final CmsPatternPanelMonthlyView m_view;

    /**
     * Constructor for the monthly pattern panel controller
     * @param model the model to read data from.
     * @param changeHandler the change handler.
     */
    CmsPatternPanelMonthlyController(final CmsSerialDateValue model, final I_ChangeHandler changeHandler) {
        super(model, changeHandler);
        m_view = new CmsPatternPanelMonthlyView(this, m_model);
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.A_CmsPatternPanelController#getView()
     */
    @Override
    public I_CmsSerialDatePatternView getView() {

        return m_view;
    }

    /**
     * Set the pattern scheme to either "by weekday" or "by day of month".
     * @param isByWeekDay flag, indicating if the pattern "by weekday" should be set.
     * @param fireChange flag, indicating if a value change event should be fired.
     */
    public void setPatternScheme(final boolean isByWeekDay, final boolean fireChange) {

        if (isByWeekDay ^ (null != m_model.getWeekDay())) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    if (isByWeekDay) {
                        m_model.setWeekOfMonth(getPatternDefaultValues().getWeekOfMonth());
                        m_model.setWeekDay(getPatternDefaultValues().getWeekDay());
                    } else {
                        m_model.clearWeekDays();
                        m_model.clearWeeksOfMonth();
                        m_model.setDayOfMonth(getPatternDefaultValues().getDayOfMonth());
                    }
                    m_model.setInterval(getPatternDefaultValues().getInterval());
                    if (fireChange) {
                        onValueChange();
                    }
                }
            });
        }

    }

    /**
     * Set the week day the event should take place.
     * @param dayString the day as string.
     */
    public void setWeekDay(String dayString) {

        final WeekDay day = WeekDay.valueOf(dayString);
        if (m_model.getWeekDay() != day) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setWeekDay(day);
                    onValueChange();
                }
            });
        }

    }

    /**
     * Handle a change in the weeks of month.
     * @param week the changed weeks checkbox's internal value.
     * @param value the new value of the changed checkbox.
     */
    public void weeksChange(String week, Boolean value) {

        final WeekOfMonth changedWeek = WeekOfMonth.valueOf(week);
        boolean newValue = (null != value) && value.booleanValue();
        boolean currentValue = m_model.getWeeksOfMonth().contains(changedWeek);
        if (newValue != currentValue) {
            if (newValue) {
                setPatternScheme(true, false);
                m_model.addWeekOfMonth(changedWeek);
                onValueChange();
            } else {
                removeExceptionsOnChange(new Command() {

                    public void execute() {

                        m_model.removeWeekOfMonth(changedWeek);
                        onValueChange();
                    }
                });
            }
        }
    }
}
