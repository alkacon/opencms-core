/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.acacia.client.widgets.serialdate;

import java.util.Date;
import java.util.SortedSet;

/** Controller for the individual pattern panel. */
public class CmsPatternPanelIndividualController extends A_CmsPatternPanelController {

    /** The controlled view. */
    private final CmsPatternPanelIndividualView m_view;

    /**
     * Constructor for the individual pattern panel controller
     * @param model the model to read data from.
     * @param changeHandler the value change handler.
     */
    CmsPatternPanelIndividualController(final CmsSerialDateValue model, final I_ChangeHandler changeHandler) {
        super(model, changeHandler);
        m_view = new CmsPatternPanelIndividualView(this, m_model);
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.A_CmsPatternPanelController#getView()
     */
    @Override
    public I_CmsSerialDatePatternView getView() {

        return m_view;
    }

    /**
     * Set the individual dates.
     * @param dates the dates to set.
     */
    public void setDates(SortedSet<Date> dates) {

        if (!m_model.getIndividualDates().equals(dates)) {
            m_model.setIndividualDates(dates);
            onValueChange();
        }

    }
}
