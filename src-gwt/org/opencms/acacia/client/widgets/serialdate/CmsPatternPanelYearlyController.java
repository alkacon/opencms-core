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

import org.opencms.acacia.shared.I_CmsSerialDateValue.Month;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;

import com.google.gwt.user.client.Command;

/** Controller for the yearly pattern panel. */
public class CmsPatternPanelYearlyController extends A_CmsPatternPanelController {

    /** The controlled view. */
    private final CmsPatternPanelYearly m_view;

    /**
     * Constructor for the yearly pattern panel controller
     * @param model the model to read data from.
     * @param validationHandler the validation handler used for validation.
     */
    CmsPatternPanelYearlyController(CmsSerialDateValueWrapper model, I_ChangeHandler validationHandler) {
        super(model, validationHandler);
        m_view = new CmsPatternPanelYearly(this, m_model);
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.A_CmsPatternPanelController#getView()
     */
    @Override
    public I_CmsPatternView getView() {

        return m_view;
    }

    /**
     * Set the month.
     * @param monthStr the month to set.
     */
    public void setMonth(String monthStr) {

        final Month month = Month.valueOf(monthStr);
        if ((m_model.getMonth() == null) || !m_model.getMonth().equals(monthStr)) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setMonth(month);
                    onValueChange();

                }
            });
        }
    }

    /**
     * Set the pattern scheme.
     * @param isWeekDayBased flag, indicating if the week day based scheme should be set.
     */
    public void setPatternScheme(final boolean isWeekDayBased) {

        if (isWeekDayBased ^ (null != m_model.getWeekDay())) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    if (isWeekDayBased) {
                        m_model.setInterval(1);
                        m_model.setWeekDay(WeekDay.SUNDAY);
                        m_model.setMonth(Month.JANUARY);
                        m_model.setWeekOfMonth(WeekOfMonth.FIRST);
                        m_model.setDayOfMonth(1);
                        onValueChange();
                    } else {
                        m_model.setWeekDay(null);
                        m_model.setWeekOfMonth(null);
                        m_model.setDayOfMonth(1);
                        m_model.setInterval(1);
                        onValueChange();
                    }
                }
            });
        }
    }

    /**
     * Set the week day.
     * @param weekDayStr the week day to set.
     */
    public void setWeekDay(String weekDayStr) {

        final WeekDay weekDay = WeekDay.valueOf(weekDayStr);
        if ((m_model.getWeekDay() != null) || !m_model.getWeekDay().equals(weekDay)) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setWeekDay(weekDay);
                    onValueChange();
                }
            });

        }

    }

    /**
     * Set the week of month.
     * @param weekOfMonthStr the week of month to set.
     */
    public void setWeekOfMonth(String weekOfMonthStr) {

        final WeekOfMonth weekOfMonth = WeekOfMonth.valueOf(weekOfMonthStr);
        if ((null == m_model.getWeekOfMonth()) || !m_model.getWeekOfMonth().equals(weekOfMonth)) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setWeekOfMonth(weekOfMonth);
                    onValueChange();
                }
            });
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDatePatternController#validate()
     */
    public String validate() {

        if (m_model.getWeekDay() == null) {
            return validateDayOfMonth();
        }
        return null;
    }

}
