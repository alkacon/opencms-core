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

import com.google.gwt.user.client.Command;

/** Controller for the daily pattern. */
public class CmsPatternPanelDailyController extends A_CmsPatternPanelController {

    /** The controlled view. */
    private final CmsPatternPanelDailyView m_view;

    /**
     * Constructor for the daily pattern panel controller
     * @param model the model to read data from.
     * @param changeHandler the value change handler.
     */
    CmsPatternPanelDailyController(final CmsSerialDateValue model, final I_ChangeHandler changeHandler) {
        super(model, changeHandler);
        m_view = new CmsPatternPanelDailyView(this, m_model);
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDatePatternController#getView()
     */
    @Override
    public CmsPatternPanelDailyView getView() {

        return m_view;
    }

    /**
     * Set the "everyWorkingDay" flag.
     * @param isEveryWorkingDay flag, indicating if the event should take place every working day.
     */
    public void setEveryWorkingDay(final boolean isEveryWorkingDay) {

        if (m_model.isEveryWorkingDay() != isEveryWorkingDay) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setEveryWorkingDay(Boolean.valueOf(isEveryWorkingDay));
                    m_model.setInterval(getPatternDefaultValues().getInterval());
                    onValueChange();
                }
            });
        }
    }
}
