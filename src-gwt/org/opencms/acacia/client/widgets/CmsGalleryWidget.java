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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.CmsGalleryConfigurationJSO;
import org.opencms.ade.galleries.client.ui.CmsGalleryField;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 *
 * */
public class CmsGalleryWidget extends Composite implements I_CmsEditWidget, HasResizeHandlers {

    /** Value of the activation. */
    private boolean m_active = true;

    /** The allow uploads flag. */
    private boolean m_allowUploads;

    /** The link selector. */
    private CmsGalleryField m_linkSelect;

    /**
     * Constructs an gallery widget with the in XSD schema declared configuration.<p>
     *
     * @param openerTitle the gallery opener title
     * @param config the widget configuration string
     */
    public CmsGalleryWidget(String openerTitle, String config) {

        this(openerTitle, config, false);
    }

    /**
     * Constructs an gallery widget with the in XSD schema declared configuration.<p>
     *
     * @param openerTitle the gallery opener title
     * @param config the widget configuration string
     * @param hasImage <code>true</code> if the widget should show an image preview
     */
    public CmsGalleryWidget(String openerTitle, String config, boolean hasImage) {

        this(openerTitle, config, hasImage, false);
    }

    /**
     * Constructs an gallery widget with the in XSD schema declared configuration.<p>
     *
     * @param openerTitle the gallery opener title
     * @param config the widget configuration string
     * @param hasImage <code>true</code> if the widget should show an image preview
     * @param allowUploads states if the upload button should be enabled for this widget
     */
    public CmsGalleryWidget(String openerTitle, String config, boolean hasImage, boolean allowUploads) {

        m_linkSelect = new CmsGalleryField(CmsGalleryConfigurationJSO.parseConfiguration(config), allowUploads);
        m_linkSelect.setGalleryOpenerTitle(openerTitle);
        m_linkSelect.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }
        });
        // All composites must call initWidget() in their constructors.
        initWidget(m_linkSelect);
        m_linkSelect.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsDomUtil.fireFocusEvent(CmsGalleryWidget.this);
            }
        });
        m_linkSelect.setHasImage(hasImage);
        m_allowUploads = allowUploads;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return m_linkSelect.addResizeHandler(handler);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     *
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, m_linkSelect.getFormValueAsString());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_linkSelect.getFormValueAsString();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        onAttach();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        return (m_linkSelect != null)
            && (m_linkSelect.getPopup() != null)
            && m_linkSelect.getPopup().isShowing()
            && m_linkSelect.getPopup().getContainer().getElement().isOrHasChild(element);

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (m_active == active) {
            return;
        }
        m_active = active;
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        m_linkSelect.setName(name);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        m_linkSelect.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        if (m_allowUploads) {
            // use the parent element with CSS class .widgetHolder as the upload drop zone to allow proper highlighting
            Element dropZone = CmsDomUtil.getAncestor(getElement(), I_CmsLayoutBundle.INSTANCE.form().widgetHolder());
            if (dropZone != null) {
                m_linkSelect.setDropZoneElement(dropZone);
            }
        }
    }
}
