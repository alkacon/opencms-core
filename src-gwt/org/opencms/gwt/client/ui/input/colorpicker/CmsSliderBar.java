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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

/**
 * Implements the SliderBar control.
 */
public final class CmsSliderBar extends HTML {

    /** Color mode for BLUE. */
    public static final int BLUE = 6;
    /** Color mode for BRIGHTNESS. */
    public static final int BRIGHTNESS = 2;
    /** Layer to affect. */
    public static final int COLORBAR_A = 1;
    /** Layer to affect. */
    public static final int COLORBAR_B = 2;
    /** Layer to affect. */
    public static final int COLORBAR_C = 3;
    /** Layer to affect. */
    public static final int COLORBAR_D = 4;
    /** Color mode for GREEN. */
    public static final int GREEN = 5;
    /** Color mode for HUE. */
    public static final int HUE = 3;
    /** Color mode for RED. */
    public static final int RED = 4;
    /** Color mode for SATURATIN. */
    public static final int SATURATIN = 1;

    /** Value if the mouse should be captured or not. */
    private boolean m_capturedMouse;
    /** Image holder. */
    private Image m_colorA;
    /** Image holder. */
    private Image m_colorB;
    /** Image holder. */
    private Image m_colorC;
    /** Image holder. */
    private Image m_colorD;

    /** Image resourcen. */
    private I_CmsColorPickerImageResource m_cpImageBundle;
    /** The parent CmsColorPicker. */
    private CmsColorSelector m_parent;
    /** Image holder. */
    private Image m_slider;

    /***
     * Initialize the SliderMap.<p>
     * @param parent the parent of the slider bar
     */
    public CmsSliderBar(CmsColorSelector parent) {

        super();

        m_parent = parent;

        setWidth("40px");
        setHeight("256px");

        m_cpImageBundle = (I_CmsColorPickerImageResource)GWT.create(I_CmsColorPickerImageResource.class);

        m_colorA = new Image(m_cpImageBundle.bar_white());
        m_colorB = new Image(m_cpImageBundle.bar_white());
        m_colorC = new Image(m_cpImageBundle.bar_white());
        m_colorD = new Image(m_cpImageBundle.bar_saturation());
        m_slider = new Image(m_cpImageBundle.rangearrows());

        getElement().appendChild(m_colorA.getElement());
        getElement().appendChild(m_colorB.getElement());
        getElement().appendChild(m_colorC.getElement());
        getElement().appendChild(m_colorD.getElement());
        getElement().appendChild(m_slider.getElement());

        m_colorA.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        m_colorA.getElement().getStyle().setBorderWidth(1, Unit.PX);
        m_colorA.getElement().getStyle().setBorderColor("black");
        m_colorB.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        m_colorB.getElement().getStyle().setBorderWidth(1, Unit.PX);
        m_colorB.getElement().getStyle().setBorderColor("black");
        m_colorC.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        m_colorC.getElement().getStyle().setBorderWidth(1, Unit.PX);
        m_colorC.getElement().getStyle().setBorderColor("black");
        m_colorD.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        m_colorD.getElement().getStyle().setBorderWidth(1, Unit.PX);
        m_colorD.getElement().getStyle().setBorderColor("black");

    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onAttach()
     */
    @Override
    public void onAttach() {

        super.onAttach();

        m_colorA.getElement().getStyle().setPosition(Position.RELATIVE);
        m_colorA.getElement().getStyle().setLeft(10, Unit.PX);

        m_colorB.getElement().getStyle().setPosition(Position.RELATIVE);
        m_colorB.getElement().getStyle().setLeft(10, Unit.PX);
        m_colorB.getElement().getStyle().setTop(-260, Unit.PX);

        m_colorC.getElement().getStyle().setPosition(Position.RELATIVE);
        m_colorC.getElement().getStyle().setLeft(10, Unit.PX);
        m_colorC.getElement().getStyle().setTop(-520, Unit.PX);

        m_colorD.getElement().getStyle().setPosition(Position.RELATIVE);
        m_colorD.getElement().getStyle().setLeft(10, Unit.PX);
        m_colorD.getElement().getStyle().setTop(-780, Unit.PX);

        m_slider.getElement().getStyle().setPosition(Position.RELATIVE);
        m_slider.getElement().getStyle().setLeft(1, Unit.PX);
        m_slider.getElement().getStyle().setTop(-1047, Unit.PX);
    }

    /**
     * Fired whenever a browser event is received.<p>
     * @param event Event to process
     */
    @Override
    public void onBrowserEvent(Event event) {

        switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEUP:
                Event.releaseCapture(getElement());
                m_capturedMouse = false;
                break;
            case Event.ONMOUSEDOWN:
                Event.setCapture(getElement());
                m_capturedMouse = true;
                //$FALL-THROUGH$
            case Event.ONMOUSEMOVE:
                if (m_capturedMouse) {
                    event.preventDefault();
                    double abolut_top = getAbsoluteTop();
                    int y = ((event.getClientY() - (int)abolut_top) + Window.getScrollTop());
                    setSliderPosition(y);
                    if (m_parent != null) {
                        m_parent.onBarSelected(y);
                    }
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
     * Sets the color selection mode. <p>
     * @param mode Can be one of: ColorBar.Saturation, ColorBar.Hue, ColorBar.Brightness, ColorBar.Red, ColorBar.Green, ColorBar.Blue, ColorBar.Red.
     */
    public void setColorSelectMode(int mode) {

        if (!isAttached()) {
            return;
        }
        m_colorA.setResource(m_cpImageBundle.bar_white());
        m_colorB.setResource(m_cpImageBundle.bar_white());
        m_colorC.setResource(m_cpImageBundle.bar_white());
        switch (mode) {
            case SATURATIN:
                m_colorD.setResource(m_cpImageBundle.bar_saturation());
                break;

            case BRIGHTNESS:
                m_colorD.setResource(m_cpImageBundle.bar_brightness());
                break;

            case HUE:
                m_colorD.setResource(m_cpImageBundle.bar_hue());
                break;

            case RED:
                m_colorA.setResource(m_cpImageBundle.bar_red_tl());
                m_colorB.setResource(m_cpImageBundle.bar_red_tr());
                m_colorC.setResource(m_cpImageBundle.bar_red_br());
                m_colorD.setResource(m_cpImageBundle.bar_red_bl());
                break;

            case GREEN:
                m_colorA.setResource(m_cpImageBundle.bar_green_tl());
                m_colorB.setResource(m_cpImageBundle.bar_green_tr());
                m_colorC.setResource(m_cpImageBundle.bar_green_br());
                m_colorD.setResource(m_cpImageBundle.bar_green_bl());
                break;

            case BLUE:
                m_colorA.setResource(m_cpImageBundle.bar_blue_tl());
                m_colorB.setResource(m_cpImageBundle.bar_blue_tr());
                m_colorC.setResource(m_cpImageBundle.bar_blue_br());
                m_colorD.setResource(m_cpImageBundle.bar_blue_bl());
                break;
            default:
                break;
        }
    }

    /**
     * Sets the color of a particular layer.<p>
     * @param color Hexadecimal notation of RGB to change the layer's color
     * @param layer Which layer to affect
     */
    public void setLayerColor(String color, int layer) {

        switch (layer) {
            case COLORBAR_A:
                m_colorA.getElement().getStyle().setBackgroundColor(color);
                break;
            case COLORBAR_B:
                m_colorB.getElement().getStyle().setBackgroundColor(color);
                break;
            case COLORBAR_C:
                m_colorC.getElement().getStyle().setBackgroundColor(color);
                break;
            case COLORBAR_D:
                m_colorD.getElement().getStyle().setBackgroundColor(color);
                break;
            default:
                return;
        }
    }

    /**
     * Set overlay's opacity.<p>
     * @param alpha An opacity percentage, between 100 (fully opaque) and 0 (invisible).
     * @param layer which bar to change opacity for, 1-4
     */
    public void setLayerOpacity(int alpha, int layer) {

        if ((alpha >= 0) && (alpha <= 100) && isAttached()) {
            Element colorbar;

            switch (layer) {
                case COLORBAR_A:
                    colorbar = m_colorA.getElement();
                    break;
                case COLORBAR_B:
                    colorbar = m_colorB.getElement();
                    break;
                case COLORBAR_C:
                    colorbar = m_colorC.getElement();
                    break;
                case COLORBAR_D:
                    colorbar = m_colorD.getElement();
                    break;
                default:
                    return;
            }
            colorbar.getStyle().setOpacity((1.0 * alpha) / 100);
            CmsTransparencyImpl.setTransparency(colorbar, alpha);
        }
    }

    /**
     * Sets the slider's position on the y-axis.<p>
     * @param y Position along the y-axis to set the slider's position to.
     */
    public void setSliderPosition(int y) {

        if (y < 0) {
            y = 0;
        }
        if (y > 256) {
            y = 256;
        }
        m_slider.getElement().getStyle().setTop(y - 1047, Unit.PX);
    }
}