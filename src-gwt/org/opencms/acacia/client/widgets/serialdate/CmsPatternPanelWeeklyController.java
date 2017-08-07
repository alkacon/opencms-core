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

import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.user.client.Command;

/** Controller for the weekly pattern panel. */
public class CmsPatternPanelWeeklyController extends A_CmsPatternPanelController {

    /** The controlled view. */
    private final CmsPatternPanelWeeklyView m_view;

    /**
     * Constructor for the weekly pattern panel controller
     * @param model the model to read data from.
     * @param changeHandler the value change handler.
     */
    CmsPatternPanelWeeklyController(final CmsSerialDateValue model, final I_ChangeHandler changeHandler) {
        super(model, changeHandler);
        m_view = new CmsPatternPanelWeeklyView(this, m_model);
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.A_CmsPatternPanelController#getView()
     */
    @Override
    public I_CmsSerialDatePatternView getView() {

        return m_view;
    }

    /**
     * Set the weekdays at which the event should take place.
     * @param weekDays the weekdays at which the event should take place.
     */
    public void setWeekDays(SortedSet<WeekDay> weekDays) {

        final SortedSet<WeekDay> newWeekDays = null == weekDays ? new TreeSet<WeekDay>() : weekDays;
        SortedSet<WeekDay> currentWeekDays = m_model.getWeekDays();
        if (!currentWeekDays.equals(newWeekDays)) {
            conditionallyRemoveExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setWeekDays(newWeekDays);
                    onValueChange();
                }
            }, !newWeekDays.containsAll(m_model.getWeekDays()));
        }
    }

}
