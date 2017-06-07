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

package org.opencms.gwt.client.ui.input.colorpicker;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

/**
 * Implements the SliderMap control.
 */
public final class CmsSliderMap extends HTML {

    /** Value to show if the mousebotton is pressed. */
    private boolean m_capturedMouse;
    /** Image for the overlaypanel. */
    private Image m_colorOverlay;
    /** Image for the underlaypanel. */
    private Image m_colorUnderlay;
    /** Class to load the different images. */
    private I_CmsColorPickerImageResource m_cpImageBundle;
    /** The parent ColorSelector. */
    private CmsColorSelector m_parent;
    /** Image for the slider. */
    private Image m_slider;

    /***
     * Initialize the SliderMap -- default mode is Saturation.
     * @param parent
     */
    public CmsSliderMap(CmsColorSelector parent) {

        super();

        m_parent = parent;
        m_cpImageBundle = (I_CmsColorPickerImageResource)GWT.create(I_CmsColorPickerImageResource.class);

        m_colorUnderlay = new Image(m_cpImageBundle.map_saturation());
        m_colorOverlay = new Image(m_cpImageBundle.map_saturation_overlay());
        m_slider = new Image(m_cpImageBundle.mappoint());

        getElement().appendChild(m_colorUnderlay.getElement());
        getElement().appendChild(m_colorOverlay.getElement());
        getElement().appendChild(m_slider.getElement());

        addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().sliderMap());
        getElement().getStyle().setBorderColor("black");
        getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        getElement().getStyle().setBorderWidth(1, Unit.PX);
        setHeight("256px");
        setWidth("256px");
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onAttach()
     */
    @Override
    public void onAttach() {

        super.onAttach();
        m_colorUnderlay.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().sliderMapUnderlay());
        m_colorUnderlay.getElement().getStyle().setPosition(Position.RELATIVE);
        m_colorOverlay.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().sliderMapOverlay());
        m_colorOverlay.getElement().getStyle().setPosition(Position.RELATIVE);
        m_colorOverlay.getElement().getStyle().setTop(-258, Unit.PX);
        m_slider.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().sliderMapSlider());
        m_slider.getElement().getStyle().setPosition(Position.ABSOLUTE);
        setOverlayOpacity(100);
    }

    /**
     * Fired whenever a browser event is received.
     * @param event Event to process
     */
    @Override
    public void onBrowserEvent(Event event) {

        super.onBrowserEvent(event);

        switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEUP:
                Event.releaseCapture(m_slider.getElement());
                m_capturedMouse = false;
                break;
            case Event.ONMOUSEDOWN:
                Event.setCapture(m_slider.getElement());
                m_capturedMouse = true;
                //$FALL-THROUGH$
            case Event.ONMOUSEMOVE:
                if (m_capturedMouse) {
                    event.preventDefault();
                    float x = ((event.getClientX() - (m_colorUnderlay.getAbsoluteLeft())) + Window.getScrollLeft());
                    float y = ((event.getClientY() - (m_colorUnderlay.getAbsoluteTop())) + Window.getScrollTop());

                    if (m_parent != null) {
                        m_parent.onMapSelected(x, y);
                    }

                    setSliderPosition(x, y);
                }
                //$FALL-THROUGH$
            default:

        }
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    public void onLoad() {

        sinkEvents(Event.MOUSEEVENTS);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onUnload()
     */
    @Override
    public void onUnload() {

        unsinkEvents(Event.MOUSEEVENTS);
    }

    /**
     * Sets the color selection mode.<p>
     * @param mode Saturation
     */
    public void setColorSelectMode(int mode) {

        if (!isAttached()) {
            return;
        }

        switch (mode) {
            case CmsSliderBar.SATURATIN:
                m_colorUnderlay.setResource(m_cpImageBundle.map_saturation());
                m_colorOverlay.setResource(m_cpImageBundle.map_saturation_overlay());
                break;

            case CmsSliderBar.BRIGHTNESS:
                m_colorUnderlay.setResource(m_cpImageBundle.map_white());
                m_colorOverlay.setResource(m_cpImageBundle.map_brightness());
                break;

            case CmsSliderBar.HUE:
                m_colorUnderlay.setResource(m_cpImageBundle.map_white());
                m_colorOverlay.setResource(m_cpImageBundle.map_hue());
                setOverlayOpacity(100);
                break;

            case CmsSliderBar.RED:
                m_colorOverlay.setResource(m_cpImageBundle.map_red_max());
                m_colorUnderlay.setResource(m_cpImageBundle.map_red_min());
                break;

            case CmsSliderBar.GREEN:
                m_colorOverlay.setResource(m_cpImageBundle.map_green_max());
                m_colorUnderlay.setResource(m_cpImageBundle.map_green_min());
                break;

            case CmsSliderBar.BLUE:
                m_colorOverlay.setResource(m_cpImageBundle.map_blue_max());
                m_colorUnderlay.setResource(m_cpImageBundle.map_blue_min());
                break;
            default:
                break;
        }
    }

    /**
     * Sets the overlay layer's color.<p>
     * @param color Hexadecimal representation of RGB.
     */
    public void setOverlayColor(String color) {

        m_colorOverlay.getElement().getStyle().setBackgroundColor(color);
    }

    /**
     * Set overlay layer's opacity.<p>
     * @param alpha An opacity percentage, between 100 (fully opaque) and 0 (invisible).
     */
    public void setOverlayOpacity(int alpha) {

        if ((alpha >= 0) && (alpha <= 100) && isAttached()) {
            CmsTransparencyImpl.setTransparency(m_colorOverlay.getElement(), alpha);
        }
    }

    /**
     * Sets the slider's position along the x-axis and y-axis.<p>
     * @param x position along the x-axis [0-256]
     * @param y position along the y-axis [0-256]
     */
    public void setSliderPosition(float x, float y) {

        if (x > 255) {
            x = 255;
        }
        if (x < 0) {
            x = 0;
        }
        if (y > 255) {
            y = 255;
        }
        if (y < 0) {
            y = 0;
        }
        x -= 7;
        y -= 7;
        m_slider.getElement().getStyle().setLeft(x, Unit.PX);
        m_slider.getElement().getStyle().setTop(y, Unit.PX);
    }

    /**
     * Sets the underlay's layer color.<p>
     * @param color Hexadecimal representation of RGB.
     */
    public void setUnderlayColor(String color) {

        m_colorUnderlay.getElement().getStyle().setBackgroundColor(color);
    }
}