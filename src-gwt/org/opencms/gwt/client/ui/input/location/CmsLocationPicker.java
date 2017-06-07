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

package org.opencms.gwt.client.ui.input.location;

import org.opencms.gwt.client.I_CmsHasResizeOnShow;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSimpleTextBox;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * A google maps based location picker widget.<p>
 */
public class CmsLocationPicker extends Composite implements HasValueChangeHandlers<String>, I_CmsHasResizeOnShow {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsLocationPickerUiBinder extends UiBinder<HTMLPanel, CmsLocationPicker> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsLocationPickerUiBinder uiBinder = GWT.create(I_CmsLocationPickerUiBinder.class);

    /** The location picker controller. */
    CmsLocationController m_controller;

    /** The location info panel. */
    @UiField
    Element m_locationInfoPanel;

    /** The map preview element. */
    @UiField
    Element m_mapPreview;

    /** The popup opener button. */
    @UiField(provided = true)
    CmsPushButton m_openerButton;

    /** The value display. */
    @UiField
    CmsSimpleTextBox m_textbox;

    /**
     * Constructor.<p>
     *
     * @param configuration the widget configuration
     **/
    public CmsLocationPicker(String configuration) {

        I_CmsLayoutBundle.INSTANCE.locationPickerCss().ensureInjected();
        m_openerButton = new CmsPushButton();
        m_openerButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_openerButton.setImageClass(I_CmsButton.GALLERY);
        m_openerButton.setSize(Size.small);
        m_openerButton.setTitle(Messages.get().key(Messages.GUI_LOCATION_DIALOG_TITLE_0));
        initWidget(uiBinder.createAndBindUi(this));
        // disable input, the picker popup is used for editing the value
        m_textbox.setEnabled(false);
        m_locationInfoPanel.getStyle().setDisplay(Display.NONE);
        m_controller = new CmsLocationController(this, configuration);
        m_mapPreview.setId(HTMLPanel.createUniqueId());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Returns the location value.<p>
     *
     * @return the location value
     */
    public CmsLocationValue getLocationValue() {

        return m_controller.getLocationValue();
    }

    /**
     * Returns the JSON string representation of the value.<p>
     *
     * @return the JSON string representation
     */
    public String getStringValue() {

        return m_controller.getStringValue();
    }

    /**
     * @see org.opencms.gwt.client.I_CmsHasResizeOnShow#resizeOnShow()
     */
    public void resizeOnShow() {

        m_controller.onPreviewResize();
    }

    /**
     * Sets the picker enabled.<p>
     *
     * @param enabled <code>true</code> to enable the picker
     */
    public void setEnabled(boolean enabled) {

        m_openerButton.setEnabled(enabled);
    }

    /**
     * Sets the widget value.<p>
     *
     * @param value the value
     */
    public void setValue(String value) {

        m_controller.setStringValue(value);
    }

    /**
     * Displays the given value.<p>
     *
     * @param value the value to display
     */
    protected void displayValue(String value) {

        m_textbox.setText(value);
    }

    /**
     * Returns the map preview element.<p>
     *
     * @return the map preview element
     */
    protected Element getMapPreview() {

        return m_mapPreview;
    }

    /**
     * Sets the location info to the info panel.<p>
     *
     * @param infos the location info items
     */
    protected void setLocationInfo(Map<String, String> infos) {

        if (infos.isEmpty()) {
            m_locationInfoPanel.getStyle().setDisplay(Display.NONE);
        } else {
            StringBuffer infoHtml = new StringBuffer();
            for (Entry<String, String> info : infos.entrySet()) {
                infoHtml.append("<p><span>").append(info.getKey()).append(":</span>").append(info.getValue()).append(
                    "</p>");
            }
            m_locationInfoPanel.setInnerHTML(infoHtml.toString());
            m_locationInfoPanel.getStyle().clearDisplay();
        }
    }

    /**
     * Sets the preview visible.<p>
     *
     * @param visible <code>true</code> to set the preview visible
     */
    protected void setPreviewVisible(boolean visible) {

        if (visible) {
            addStyleName(I_CmsLayoutBundle.INSTANCE.locationPickerCss().hasPreview());
        } else {
            removeStyleName(I_CmsLayoutBundle.INSTANCE.locationPickerCss().hasPreview());
        }
    }

    /**
     * Opens the location popup.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_openerButton")
    void onOpenerClick(ClickEvent event) {

        m_openerButton.clearHoverState();
        m_controller.openPopup();
    }
}
