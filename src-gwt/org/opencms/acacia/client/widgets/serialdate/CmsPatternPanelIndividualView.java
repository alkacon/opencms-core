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

import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;

import java.util.Date;
import java.util.SortedSet;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * The daily pattern panel.<p>
 * */
public class CmsPatternPanelIndividualView extends Composite implements I_CmsSerialDatePatternView {

    /** The UI binder interface. */
    interface I_CmsPatternPanelIndividualUiBinder extends UiBinder<HTMLPanel, CmsPatternPanelIndividualView> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsPatternPanelIndividualUiBinder uiBinder = GWT.create(I_CmsPatternPanelIndividualUiBinder.class);

    /** Format with date only. */
    private String m_dateFormat = Messages.get().keyDefault(Messages.GUI_SERIALDATE_DATE_FORMAT_0, null);

    /** Input field for new dates. */
    @UiField
    CmsDateBox m_newDate;

    /** Button to add new dates. */
    @UiField
    CmsPushButton m_addButton;

    /** Button to remove selected dates. */
    @UiField
    CmsPushButton m_removeSelectedButton;

    /** The ui element for the list with the individual dates */
    @UiField(provided = true)
    CmsCheckableDatePanel m_dateList;

    /** The model to read the data from. */
    private final I_CmsObservableSerialDateValue m_model;

    /** The controller to handle changes. */
    final CmsPatternPanelIndividualController m_controller;

    /** Flag, indicating if change actions should not be triggered. */
    private boolean m_triggerChangeActions = true;

    /**
     * Default constructor to create the panel.<p>
     * @param controller the controller that handles value changes.
     * @param model the model that provides the values.
     */
    public CmsPatternPanelIndividualView(
        CmsPatternPanelIndividualController controller,
        I_CmsObservableSerialDateValue model) {
        m_controller = controller;
        m_model = model;
        m_model.registerValueChangeObserver(this);

        m_dateList = new CmsCheckableDatePanel(m_dateFormat);
        m_dateList.setWidth("100%");
        initWidget(uiBinder.createAndBindUi(this));
        m_addButton.setText(Messages.get().key(Messages.GUI_SERIALDATE_BUTTON_ADD_INDIVIDUAL_0));
        m_removeSelectedButton.setText(Messages.get().key(Messages.GUI_SERIALDATE_BUTTON_REMOVE_INDIVIDUAL_0));

    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver#onValueChange()
     */
    public void onValueChange() {

        m_dateList.setDates(m_model.getIndividualDates());

    }

    /**
     * Handle click on "Add" button.
     * @param e the click event.
     */
    @UiHandler("m_addButton")
    void addButtonClick(ClickEvent e) {

        if (null != m_newDate.getValue()) {
            m_dateList.addDate(m_newDate.getValue());
            m_newDate.setValue(null);
            if (handleChange()) {
                m_controller.setDates(m_dateList.getDates());
            }
        }
    }

    /**
     * Handle value change event on the individual dates list.
     * @param event the change event.
     */
    @UiHandler("m_dateList")
    void dateListValueChange(ValueChangeEvent<SortedSet<Date>> event) {

        if (handleChange()) {
            m_controller.setDates(event.getValue());
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
     * Handle click on "Remove selected" button.
     * @param e the click event.
     */
    @UiHandler("m_removeSelectedButton")
    void removeSelectedButtonClick(ClickEvent e) {

        m_dateList.setDates(m_dateList.getUncheckedDates());
    }

}
