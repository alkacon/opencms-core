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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * Wraps the GWT suggest box to enhance the layout.<p>
 */
public class CmsSuggestBox extends Composite
implements HasValueChangeHandlers<String>, HasSelectionHandlers<SuggestOracle.Suggestion> {

    /** The wrapped suggest box. */
    private SuggestBox m_suggestBox;

    /** The container for the text box. */
    private CmsPaddedPanel m_textBoxContainer;

    /**
     * Constructor.<p>
     *
     * @param oracle the suggestion oracle
     */
    public CmsSuggestBox(SuggestOracle oracle) {

        m_textBoxContainer = new CmsPaddedPanel(4);
        m_textBoxContainer.setStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().textBoxPanel());
        m_textBoxContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_textBoxContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());
        m_suggestBox = new SuggestBox(oracle);
        m_textBoxContainer.add(m_suggestBox);

        initWidget(m_textBoxContainer);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasSelectionHandlers#addSelectionHandler(com.google.gwt.event.logical.shared.SelectionHandler)
     */
    public HandlerRegistration addSelectionHandler(SelectionHandler<Suggestion> handler) {

        return m_suggestBox.addSelectionHandler(handler);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return m_suggestBox.addValueChangeHandler(handler);
    }

    /**
     * Sets the text value.<p>
     *
     * @param textValue he text value
     */
    public void setTextValue(String textValue) {

        m_suggestBox.setText(textValue);
    }
}
