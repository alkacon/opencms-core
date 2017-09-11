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

import org.opencms.acacia.client.widgets.serialdate.CmsSerialDateController.PatternDefaultValues;
import org.opencms.acacia.shared.CmsSerialDateUtil;

import com.google.gwt.user.client.Command;

/** Abstract base class for pattern panel controllers. */
public abstract class A_CmsPatternPanelController implements I_CmsSerialDatePatternController {

    /** The change handler called on {@link #onValueChange()}. */
    private final I_ChangeHandler m_changeHandler;
    /** The model to read data from. */
    protected final CmsSerialDateValue m_model;

    /**
     * Constructor for the abstract pattern panel controller
     * @param model the model to read data from.
     * @param changeHandler the handler for value changes.
     */
    public A_CmsPatternPanelController(final CmsSerialDateValue model, final I_ChangeHandler changeHandler) {
        m_model = model;
        m_changeHandler = changeHandler;

    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDatePatternController#getView()
     */
    abstract public I_CmsSerialDatePatternView getView();

    /**
     * @param cmd see change handler
     * @param showDialog see change handler
     * @see I_ChangeHandler#conditionallyRemoveExceptionsOnChange(Command, boolean)
     */
    protected void conditionallyRemoveExceptionsOnChange(Command cmd, boolean showDialog) {

        m_changeHandler.conditionallyRemoveExceptionsOnChange(cmd, showDialog);
    }

    /**
     * Call when the value has changed.
     */
    protected void onValueChange() {

        m_changeHandler.valueChanged();
    }

    /**
     * @param cmd see change handler
     * @see I_ChangeHandler#removeExceptionsOnChange(Command)
     */
    protected void removeExceptionsOnChange(Command cmd) {

        m_changeHandler.removeExceptionsOnChange(cmd);
    }

    /**
     * Returns the default values for patterns.
     * @return the default values for patterns.
     */
    PatternDefaultValues getPatternDefaultValues() {

        return m_changeHandler.getPatternDefaultValues();
    }

    /**
     * Sets the day of the month.
     * @param day the day to set.
     */
    void setDayOfMonth(String day) {

        final int i = CmsSerialDateUtil.toIntWithDefault(day, -1);
        if (m_model.getDayOfMonth() != i) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setDayOfMonth(i);
                    onValueChange();
                }
            });
        }
    }

    /**
     * Sets the interval.
     * @param interval the interval to set.
     */
    void setInterval(String interval) {

        final int i = CmsSerialDateUtil.toIntWithDefault(interval, -1);
        if (m_model.getInterval() != i) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setInterval(i);
                    onValueChange();
                }
            });
        }

    }
}
