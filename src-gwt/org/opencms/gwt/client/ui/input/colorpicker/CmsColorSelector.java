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
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Main class for the color picker.
 * */
public class CmsColorSelector extends Composite implements KeyPressHandler, ClickHandler, ChangeHandler {

    /** The blue value. */
    private int m_blue;
    /** The brightness value. */
    private int m_brightness;
    /** The color mode value. */
    private int m_colorMode;
    /** The color preview window. */
    private HTML m_colorpreview;
    /** The green value. */
    private int m_green;
    /** The hue value. */
    private int m_hue;
    /** The CmsRaidoButtonGroup to group all the radiobuttons. */
    private CmsRadioButtonGroup m_radioButtonGroup = new CmsRadioButtonGroup();
    /** The CmsradioButton to edit the blue value. */
    private CmsRadioButton m_rbBlue;
    /** The CmsRadioButton to edit the brightness value. */
    private CmsRadioButton m_rbBrightness;

    /** The CmsRadioButton to edit the green value. */
    private CmsRadioButton m_rbGreen;
    /** The CmsRadioButton to edit the hue value. */
    private CmsRadioButton m_rbHue;
    /** The CmsRadioButton to edit the red value. */
    private CmsRadioButton m_rbRed;
    /** The CmsRadioButton to edit the saturation value. */
    private CmsRadioButton m_rbSaturation;
    /** The red value. */
    private int m_red;
    /** The saturation value. */
    private int m_saturation;
    /** Colorpicker sliderbar. */
    private CmsSliderBar m_sliderbar;

    /** Colorpicker main slider. */
    private CmsSliderMap m_slidermap;
    /** The start color. */
    private String m_stcolor = "ff0000";

    /** The TextBox to show the blue value. */
    private TextBox m_tbBlue;
    /** The TextBox to show the brightness value. */
    private TextBox m_tbBrightness;
    /** The TextBox to show the green value. */
    private TextBox m_tbGreen;
    /** The TextBox to show the hex value. */
    private TextBox m_tbHexColor;
    /** The TextBox to show the hue value. */
    private TextBox m_tbHue;
    /** The TextBox to show the red value. */
    private TextBox m_tbRed;
    /** The TextBox to show the saturation value. */
    private TextBox m_tbSaturation;

    /**
     *  Constructor to create an CmsColorPicker. <p>
     */
    public CmsColorSelector() {

        m_hue = 0;
        m_saturation = 100;
        m_brightness = 100;
        m_red = 255;
        m_green = 0;
        m_blue = 0;

        HorizontalPanel panel = new HorizontalPanel();
        panel.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().colorSelectorWidget());
        FlexTable table = new FlexTable();

        // Add the slider map
        m_slidermap = new CmsSliderMap(this);
        panel.add(m_slidermap);
        panel.setCellWidth(m_slidermap, "258px");
        panel.setCellHeight(m_slidermap, "258px");

        // Add the slider bar
        m_sliderbar = new CmsSliderBar(this);
        panel.add(m_sliderbar);
        panel.setCellWidth(m_sliderbar, "40px");
        panel.setCellHeight(m_sliderbar, "258px");

        m_colorpreview = new HTML("");
        m_colorpreview.setWidth("auto");
        m_colorpreview.setHeight("50px");
        m_colorpreview.getElement().getStyle().setBorderColor("black");
        m_colorpreview.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        m_colorpreview.getElement().getStyle().setBorderWidth(1, Unit.PX);

        // Radio buttons

        m_rbHue = new CmsRadioButton("H:", "H:");
        m_rbHue.setGroup(m_radioButtonGroup);
        m_rbHue.addClickHandler(this);
        m_rbSaturation = new CmsRadioButton("S:", "S:");
        m_rbSaturation.setGroup(m_radioButtonGroup);
        m_rbSaturation.addClickHandler(this);
        m_rbBrightness = new CmsRadioButton("V:", "V:");
        m_rbBrightness.setGroup(m_radioButtonGroup);
        m_rbBrightness.addClickHandler(this);
        m_rbRed = new CmsRadioButton("R:", "R:");
        m_rbRed.setGroup(m_radioButtonGroup);
        m_rbRed.addClickHandler(this);
        m_rbGreen = new CmsRadioButton("G:", "G:");
        m_rbGreen.setGroup(m_radioButtonGroup);
        m_rbGreen.addClickHandler(this);
        m_rbBlue = new CmsRadioButton("B:", "B:");
        m_rbBlue.setGroup(m_radioButtonGroup);
        m_rbBlue.addClickHandler(this);

        // Textboxes
        m_tbHue = new TextBox();
        m_tbHue.setText(new Integer(m_hue).toString());
        m_tbHue.setMaxLength(3);
        m_tbHue.setVisibleLength(6);
        m_tbHue.addKeyPressHandler(this);
        m_tbHue.addChangeHandler(this);
        m_tbHue.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().tableField());

        m_tbSaturation = new TextBox();
        m_tbSaturation.setText(new Integer(m_saturation).toString());
        m_tbSaturation.setMaxLength(3);
        m_tbSaturation.setVisibleLength(6);
        m_tbSaturation.addKeyPressHandler(this);
        m_tbSaturation.addChangeHandler(this);
        m_tbSaturation.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().tableField());

        m_tbBrightness = new TextBox();
        m_tbBrightness.setText(new Integer(m_brightness).toString());
        m_tbBrightness.setMaxLength(3);
        m_tbBrightness.setVisibleLength(6);
        m_tbBrightness.addKeyPressHandler(this);
        m_tbBrightness.addChangeHandler(this);
        m_tbBrightness.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().tableField());

        m_tbRed = new TextBox();
        m_tbRed.setText(new Integer(m_red).toString());
        m_tbRed.setMaxLength(3);
        m_tbRed.setVisibleLength(6);
        m_tbRed.addKeyPressHandler(this);
        m_tbRed.addChangeHandler(this);
        m_tbRed.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().tableField());

        m_tbGreen = new TextBox();
        m_tbGreen.setText(new Integer(m_green).toString());
        m_tbGreen.setMaxLength(3);
        m_tbGreen.setVisibleLength(6);
        m_tbGreen.addKeyPressHandler(this);
        m_tbGreen.addChangeHandler(this);
        m_tbGreen.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().tableField());

        m_tbBlue = new TextBox();
        m_tbBlue.setText(new Integer(m_blue).toString());
        m_tbBlue.setMaxLength(3);
        m_tbBlue.setVisibleLength(6);
        m_tbBlue.addKeyPressHandler(this);
        m_tbBlue.addChangeHandler(this);
        m_tbBlue.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().tableField());

        m_tbHexColor = new TextBox();
        m_tbHexColor.setText(m_stcolor);
        m_tbHexColor.setMaxLength(6);
        m_tbHexColor.setVisibleLength(6);
        m_tbHexColor.addKeyPressHandler(this);
        m_tbHexColor.addChangeHandler(this);
        m_tbHexColor.addStyleName(I_CmsLayoutBundle.INSTANCE.colorSelectorCss().tableField());

        // Put together the FlexTable
        table.setWidget(0, 1, m_colorpreview);
        table.setWidget(1, 0, m_rbHue);
        table.setWidget(1, 1, m_tbHue);
        table.setWidget(1, 2, new HTML("&deg;"));
        table.setWidget(2, 0, m_rbSaturation);
        table.setWidget(2, 1, m_tbSaturation);
        table.setText(2, 2, "%");
        table.setWidget(3, 0, m_rbBrightness);
        table.setWidget(3, 1, m_tbBrightness);
        table.setText(3, 2, "%");
        table.setWidget(4, 0, m_rbRed);
        table.setWidget(4, 1, m_tbRed);
        table.setWidget(5, 0, m_rbGreen);
        table.setWidget(5, 1, m_tbGreen);
        table.setWidget(6, 0, m_rbBlue);
        table.setWidget(6, 1, m_tbBlue);
        table.setText(7, 0, "Web:");
        table.setWidget(7, 1, m_tbHexColor);

        table.setCellSpacing(3);
        // Final setup
        panel.add(table);
        m_radioButtonGroup.selectButton(m_rbRed);

        setPreview(m_stcolor);
        m_colorpreview.getElement().getStyle().setCursor(Cursor.DEFAULT);

        // First event
        onClick(m_radioButtonGroup.getSelectedButton());

        initWidget(panel);
    }

    /**
     * Gets the hexadecimal of current selected color.<p>
     * @return Hexadecimal in the range of 000000-FFFFFF
     */
    public String getHexColor() {

        return m_tbHexColor.getText();
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    public void onAttach() {

        super.onAttach();
        m_colorMode = -1;
        updateSliders();
    }

    /**
     * Fires whenever the user generates picking events along the color picker bar.<p>
     * @param y the distance along the y-axis
     */
    public void onBarSelected(int y) {

        switch (m_colorMode) {
            case CmsSliderBar.HUE:
                m_hue = 360 - percentOf(y, 360);
                m_tbHue.setText(Integer.toString(m_hue));
                onChange(m_tbHue);
                break;
            case CmsSliderBar.SATURATIN:
                m_saturation = 100 - percentOf(y, 100);
                m_tbSaturation.setText(Integer.toString(m_saturation));
                onChange(m_tbSaturation);
                break;
            case CmsSliderBar.BRIGHTNESS:
                m_brightness = 100 - percentOf(y, 100);
                m_tbBrightness.setText(Integer.toString(m_brightness));
                onChange(m_tbBrightness);
                break;
            case CmsSliderBar.RED:
                m_red = 255 - y;
                m_tbRed.setText(Integer.toString(m_red));
                onChange(m_tbRed);
                break;
            case CmsSliderBar.GREEN:
                m_green = 255 - y;
                m_tbGreen.setText(Integer.toString(m_green));
                onChange(m_tbGreen);
                break;
            case CmsSliderBar.BLUE:
                m_blue = 255 - y;
                m_tbBlue.setText(Integer.toString(m_blue));
                onChange(m_tbBlue);
                break;
            default:
                break;

        }
    }

    /**
     * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
     */
    public void onChange(ChangeEvent event) {

        onChange((Widget)event.getSource());
    }

    /**
     * Fired whenever something in this widget changes.<p>
     *
     * @param widget that has changed.
     */
    public void onChange(Widget widget) {

        if (widget == m_tbHexColor) {
            try {
                CmsColor color = new CmsColor();
                color.setHex(m_tbHexColor.getText());
                m_tbHue.setText(Integer.toString(color.getHue()));
                m_tbSaturation.setText(Integer.toString(color.getSaturation()));
                m_tbBrightness.setText(Integer.toString(color.getValue()));
                m_tbRed.setText(Integer.toString(color.getRed()));
                m_tbGreen.setText(Integer.toString(color.getGreen()));
                m_tbBlue.setText(Integer.toString(color.getBlue()));
                m_tbHexColor.setText(color.getHex());
                setPreview(color.getHex());
            } catch (Exception e) {
                // do something.
            }
        }

        if ((widget == m_tbRed) || (widget == m_tbGreen) || (widget == m_tbBlue)) {
            // Don't allow this value to overflow or underflow
            try {
                if (Integer.parseInt(((TextBox)widget).getText()) > 255) {
                    ((TextBox)widget).setText("255");
                }
                if (Integer.parseInt(((TextBox)widget).getText()) < 0) {
                    ((TextBox)widget).setText("0");
                }
            } catch (Exception e) {
                // do something.
            }

            m_red = Integer.parseInt(m_tbRed.getText());
            m_green = Integer.parseInt(m_tbGreen.getText());
            m_blue = Integer.parseInt(m_tbBlue.getText());
            m_hue = Integer.parseInt(m_tbHue.getText());
            m_saturation = Integer.parseInt(m_tbSaturation.getText());
            m_brightness = Integer.parseInt(m_tbBrightness.getText());

            try {
                CmsColor color = new CmsColor();
                color.setRGB(m_red, m_green, m_blue);
                m_tbHue.setText(Integer.toString(color.getHue()));
                m_tbSaturation.setText(Integer.toString(color.getSaturation()));
                m_tbBrightness.setText(Integer.toString(color.getValue()));
                m_tbHexColor.setText(color.getHex());
                setPreview(color.getHex());
            } catch (Exception e) {
                // do something.
            }
        } else if ((widget == m_tbHue) || (widget == m_tbSaturation) || (widget == m_tbBrightness)) {
            try {
                if (Integer.parseInt(m_tbHue.getText()) > 359) {
                    m_tbHue.setText("359");
                }

                if (Integer.parseInt(m_tbSaturation.getText()) > 100) {
                    m_tbSaturation.setText("100");
                }

                if (Integer.parseInt(m_tbBrightness.getText()) > 100) {
                    m_tbBrightness.setText("100");
                }
            } catch (Exception e) {
                // do something.
            }

            m_red = Integer.parseInt(m_tbRed.getText());
            m_green = Integer.parseInt(m_tbGreen.getText());
            m_blue = Integer.parseInt(m_tbBlue.getText());
            m_hue = Integer.parseInt(m_tbHue.getText());
            m_saturation = Integer.parseInt(m_tbSaturation.getText());
            m_brightness = Integer.parseInt(m_tbBrightness.getText());

            // Figure out colors
            try {
                CmsColor color = new CmsColor();
                color.setHSV(m_hue, m_saturation, m_brightness);
                m_tbRed.setText(Integer.toString(color.getRed()));
                m_tbGreen.setText(Integer.toString(color.getGreen()));
                m_tbBlue.setText(Integer.toString(color.getBlue()));
                m_tbHexColor.setText(color.getHex());
                setPreview(color.getHex());
            } catch (Exception e) {
                // do something.
            }
        }

        // Let the sliders know something's changed
        updateSliders();
    }

    /**
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    public void onClick(ClickEvent event) {

        onClick((Widget)event.getSource());
    }

    /**
     * Fired when the user clicks on a widget.<p>
     *
     * @param widget the widget sending the event.
     */
    public void onClick(Widget widget) {

        if (widget == m_rbHue) {
            if (m_colorMode != CmsSliderBar.HUE) {
                m_colorMode = CmsSliderBar.HUE;
                m_slidermap.setColorSelectMode(CmsSliderBar.HUE);
                m_sliderbar.setColorSelectMode(CmsSliderBar.HUE);
                m_slidermap.setOverlayOpacity(100);
                m_sliderbar.setLayerOpacity(100, CmsSliderBar.COLORBAR_D);
            }

            try {
                CmsColor color = new CmsColor();
                color.setHSV(m_hue, 100, 100);
                m_slidermap.setOverlayColor("#" + color.getHex());
            } catch (Exception e) {
                // do something.
            }

            m_sliderbar.setSliderPosition(256 - (int)((new Integer(m_hue).floatValue() / 360) * 256));
            m_slidermap.setSliderPosition(
                (int)((new Integer(m_saturation).floatValue() / 100) * 256),
                256 - (int)((new Integer(m_brightness).floatValue() / 100) * 256));
        } else if (widget == m_rbSaturation) {
            if (m_colorMode != CmsSliderBar.SATURATIN) {
                m_colorMode = CmsSliderBar.SATURATIN;
                m_slidermap.setColorSelectMode(CmsSliderBar.SATURATIN);
                m_sliderbar.setColorSelectMode(CmsSliderBar.SATURATIN);
                m_slidermap.setOverlayColor("transparent");
                m_sliderbar.setLayerOpacity(100, CmsSliderBar.COLORBAR_D);
            }

            try {
                CmsColor color = new CmsColor();
                color.setHSV(m_hue, 100, m_brightness);
                m_sliderbar.setLayerColor("#" + color.getHex(), CmsSliderBar.COLORBAR_D);
            } catch (Exception e) {
                // do something.
            }

            m_slidermap.setOverlayOpacity(100 - m_saturation);

            m_sliderbar.setSliderPosition(256 - (int)((new Integer(m_saturation).floatValue() / 100) * 256));
            m_slidermap.setSliderPosition(
                (int)((new Integer(m_hue).floatValue() / 360) * 256),
                256 - (int)((new Integer(m_brightness).floatValue() / 100) * 256));
        } else if (widget == m_rbBrightness) {
            if (m_colorMode != CmsSliderBar.BRIGHTNESS) {
                m_colorMode = CmsSliderBar.BRIGHTNESS;
                m_slidermap.setColorSelectMode(CmsSliderBar.BRIGHTNESS);
                m_sliderbar.setColorSelectMode(CmsSliderBar.BRIGHTNESS);
                m_slidermap.setUnderlayColor("#000000");
                m_slidermap.setOverlayColor("transparent");
                m_sliderbar.setLayerOpacity(100, CmsSliderBar.COLORBAR_D);
            }

            try {
                CmsColor color = new CmsColor();
                color.setHSV(m_hue, m_saturation, 100);
                m_sliderbar.setLayerColor("#" + color.getHex(), CmsSliderBar.COLORBAR_D);
            } catch (Exception e) {
                // do something.
            }

            m_slidermap.setOverlayOpacity(m_brightness);

            m_sliderbar.setSliderPosition(256 - (int)((new Integer(m_brightness).floatValue() / 100) * 256));
            m_slidermap.setSliderPosition(
                (int)((new Integer(m_hue).floatValue() / 360) * 256),
                256 - (int)((new Integer(m_saturation).floatValue() / 100) * 256));
        } else if (widget == m_rbRed) {
            if (m_colorMode != CmsSliderBar.RED) {
                m_colorMode = CmsSliderBar.RED;
                m_slidermap.setColorSelectMode(CmsSliderBar.RED);
                m_sliderbar.setColorSelectMode(CmsSliderBar.RED);
            }
            m_slidermap.setOverlayOpacity(percentOf(m_red, 100));

            m_sliderbar.setSliderPosition(256 - m_red);
            m_slidermap.setSliderPosition(m_blue, 256 - m_green);
        } else if (widget == m_rbGreen) {
            if (m_colorMode != CmsSliderBar.GREEN) {
                m_colorMode = CmsSliderBar.GREEN;
                m_slidermap.setColorSelectMode(CmsSliderBar.GREEN);
                m_sliderbar.setColorSelectMode(CmsSliderBar.GREEN);
            }

            m_slidermap.setOverlayOpacity(percentOf(m_green, 100));

            m_sliderbar.setSliderPosition(256 - m_green);
            m_slidermap.setSliderPosition(m_blue, 256 - m_red);
        } else if (widget == m_rbBlue) {
            if (m_colorMode != CmsSliderBar.BLUE) {
                m_colorMode = CmsSliderBar.BLUE;
                m_slidermap.setColorSelectMode(CmsSliderBar.BLUE);
                m_sliderbar.setColorSelectMode(CmsSliderBar.BLUE);
            }

            m_slidermap.setOverlayOpacity(percentOf(m_blue, 100));

            m_sliderbar.setSliderPosition(256 - m_blue);
            m_slidermap.setSliderPosition(m_red, 256 - m_green);
        }

        if ((m_colorMode == CmsSliderBar.RED)
            || (m_colorMode == CmsSliderBar.GREEN)
            || (m_colorMode == CmsSliderBar.BLUE)) {
            int x = 0;
            int y = 0;

            if (m_colorMode == CmsSliderBar.RED) {
                x = m_blue;
                y = m_green;
            }

            if (m_colorMode == CmsSliderBar.GREEN) {
                x = m_blue;
                y = m_red;
            }

            if (m_colorMode == CmsSliderBar.BLUE) {
                x = m_red;
                y = m_green;
            }

            int horzPer = (int)((new Float(x).floatValue() / 256) * 100);
            int vertPer = (int)((new Float(y).floatValue() / 256) * 100);
            int horzPerRev = (int)(((256 - new Float(x).floatValue()) / 256) * 100);
            int vertPerRev = (int)(((256 - new Float(y).floatValue()) / 256) * 100);

            if (vertPerRev > horzPerRev) {
                m_sliderbar.setLayerOpacity(horzPerRev, CmsSliderBar.COLORBAR_D);
            } else {
                m_sliderbar.setLayerOpacity(vertPerRev, CmsSliderBar.COLORBAR_D);
            }
            if (vertPerRev > horzPer) {
                m_sliderbar.setLayerOpacity(horzPer, CmsSliderBar.COLORBAR_C);
            } else {
                m_sliderbar.setLayerOpacity(vertPerRev, CmsSliderBar.COLORBAR_C);
            }
            if (vertPer > horzPer) {
                m_sliderbar.setLayerOpacity(horzPer, CmsSliderBar.COLORBAR_B);
            } else {
                m_sliderbar.setLayerOpacity(vertPer, CmsSliderBar.COLORBAR_B);
            }
            if (vertPer > horzPerRev) {
                m_sliderbar.setLayerOpacity(horzPerRev, CmsSliderBar.COLORBAR_A);
            } else {
                m_sliderbar.setLayerOpacity(vertPer, CmsSliderBar.COLORBAR_A);
            }
        }
    }

    /**
     * Fired when a keyboard action generates a character. This occurs after onKeyDown and onKeyUp are fired for the physical key that was pressed.<p>
     * It should be noted that many browsers do not generate keypress events for non-printing keyCode values.<p>
     * Such as KEY_ENTER or arrow keys.
     *
     * @param event the widget that was focused when the event occurred.
     */
    public void onKeyPress(KeyPressEvent event) {

        Widget widget = (Widget)event.getSource();
        int unicodeCharCode = event.getUnicodeCharCode();
        char keyCode = event.getCharCode();

        if (widget == m_tbHexColor) {
            // Disallow non-hex in hexadecimal boxes
            if ((!Character.isDigit(keyCode))
                && (unicodeCharCode != 'A')
                && (unicodeCharCode != 'a')
                && (unicodeCharCode != 'B')
                && (unicodeCharCode != 'b')
                && (unicodeCharCode != 'C')
                && (unicodeCharCode != 'c')
                && (unicodeCharCode != 'D')
                && (unicodeCharCode != 'd')
                && (unicodeCharCode != 'E')
                && (unicodeCharCode != 'e')
                && (unicodeCharCode != 'F')
                && (unicodeCharCode != 'f')
                && (unicodeCharCode != KeyCodes.KEY_TAB)
                && (unicodeCharCode != (char)KeyCodes.KEY_BACKSPACE)
                && (unicodeCharCode != (char)KeyCodes.KEY_DELETE)
                && (unicodeCharCode != (char)KeyCodes.KEY_ENTER)
                && (unicodeCharCode != (char)KeyCodes.KEY_HOME)
                && (unicodeCharCode != (char)KeyCodes.KEY_END)
                && (unicodeCharCode != (char)KeyCodes.KEY_LEFT)
                && (unicodeCharCode != (char)KeyCodes.KEY_UP)
                && (unicodeCharCode != (char)KeyCodes.KEY_RIGHT)
                && (unicodeCharCode != (char)KeyCodes.KEY_DOWN)) {
                ((TextBox)widget).cancelKey();
            }
        } else {
            // Disallow non-numerics in numeric boxes
            if ((!Character.isDigit(keyCode))
                && (keyCode != (char)KeyCodes.KEY_TAB)
                && (keyCode != (char)KeyCodes.KEY_BACKSPACE)
                && (keyCode != (char)KeyCodes.KEY_DELETE)
                && (keyCode != (char)KeyCodes.KEY_ENTER)
                && (keyCode != (char)KeyCodes.KEY_HOME)
                && (keyCode != (char)KeyCodes.KEY_END)
                && (keyCode != (char)KeyCodes.KEY_LEFT)
                && (keyCode != (char)KeyCodes.KEY_UP)
                && (keyCode != (char)KeyCodes.KEY_RIGHT)
                && (keyCode != (char)KeyCodes.KEY_DOWN)) {
                ((TextBox)widget).cancelKey();
            }
        }
    }

    /**
     * Fires whenever the user generates picking events on the color picker map.<p>
     *
     * @param x the distance along the x-axis, between 0 and 255
     * @param y the distance along the y-axis, between 0 and 255
     */
    public void onMapSelected(float x, float y) {

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
        switch (m_colorMode) {
            case CmsSliderBar.HUE:
                m_saturation = percentOf(x, 100);
                m_brightness = 100 - percentOf(y, 100);
                m_tbSaturation.setText(Integer.toString(m_saturation));
                m_tbBrightness.setText(Integer.toString(m_brightness));
                onChange(m_tbHue);
                break;
            case CmsSliderBar.SATURATIN:
                m_hue = percentOf(x, 360);
                m_brightness = 100 - percentOf(y, 100);
                m_tbHue.setText(Integer.toString(m_hue));
                m_tbBrightness.setText(Integer.toString(m_brightness));
                onChange(m_tbSaturation);
                break;
            case CmsSliderBar.BRIGHTNESS:
                m_hue = percentOf(x, 360);
                m_saturation = 100 - percentOf(y, 100);
                m_tbHue.setText(Integer.toString(m_hue));
                m_tbSaturation.setText(Integer.toString(m_saturation));
                onChange(m_tbBrightness);
                break;
            case CmsSliderBar.RED:
                m_blue = (int)x;
                m_green = 255 - (int)y;
                m_tbBlue.setText(Integer.toString(m_blue));
                m_tbGreen.setText(Integer.toString(m_green));
                onChange(m_tbRed);
                break;
            case CmsSliderBar.GREEN:
                m_blue = (int)x;
                m_red = 255 - (int)y;
                m_tbBlue.setText(Integer.toString(m_blue));
                m_tbRed.setText(Integer.toString(m_red));
                onChange(m_tbGreen);
                break;
            case CmsSliderBar.BLUE:
                m_red = (int)x;
                m_green = 255 - (int)y;
                m_tbRed.setText(Integer.toString(m_red));
                m_tbGreen.setText(Integer.toString(m_green));
                onChange(m_tbBlue);
                break;
            default:
                break;
        }
    }

    /**
     * Sets the hexadecimal notation for Red, Green, and Blue. <p>
     * @param hex Hexadecimal notation of Red, Green and Blue
     * @throws java.lang.Exception if something goes wrong
     */
    public void setHex(String hex) throws Exception {

        CmsColor color = new CmsColor();
        color.setHex(hex);

        m_red = color.getRed();
        m_green = color.getGreen();
        m_blue = color.getBlue();
        m_hue = color.getHue();
        m_saturation = color.getSaturation();
        m_brightness = color.getValue();

        m_tbRed.setText(Integer.toString(m_red));
        m_tbGreen.setText(Integer.toString(m_green));
        m_tbBlue.setText(Integer.toString(m_blue));
        m_tbHue.setText(Integer.toString(m_hue));
        m_tbSaturation.setText(Integer.toString(m_saturation));
        m_tbBrightness.setText(Integer.toString(m_brightness));
        m_tbHexColor.setText(color.getHex());
        setPreview(color.getHex());

        updateSliders();
    }

    /**
     * Set the Hue, Saturation and Brightness variables.<p>
     *
     * @param hue angle - valid range is 0-359
     * @param sat percent - valid range is 0-100
     * @param bri percent (Brightness) - valid range is 0-100
     * @throws java.lang.Exception if something goes wrong
     */
    public void setHSV(int hue, int sat, int bri) throws Exception {

        CmsColor color = new CmsColor();
        color.setHSV(hue, sat, bri);

        m_red = color.getRed();
        m_green = color.getGreen();
        m_blue = color.getBlue();
        m_hue = hue;
        m_saturation = sat;
        m_brightness = bri;

        m_tbRed.setText(Integer.toString(m_red));
        m_tbGreen.setText(Integer.toString(m_green));
        m_tbBlue.setText(Integer.toString(m_blue));
        m_tbHue.setText(Integer.toString(m_hue));
        m_tbSaturation.setText(Integer.toString(m_saturation));
        m_tbBrightness.setText(Integer.toString(m_brightness));
        m_tbHexColor.setText(color.getHex());
        setPreview(color.getHex());

        updateSliders();
    }

    /**
     * Sets the Red, Green, and Blue color variables. This will automatically populate the Hue, Saturation and Brightness and Hexadecimal fields, too.
     *
     * The RGB color model is an additive color model in which red, green, and blue light are added together in various ways to reproduce a broad array of colors. The name of the model comes from the initials of the three additive primary colors, red, green, and blue.
     * @param red strength - valid range is 0-255
     * @param green strength - valid range is 0-255
     * @param blue strength - valid range is 0-255
     * @throws java.lang.Exception Exception if the Red, Green or Blue variables are out of range.
     */
    public void setRGB(int red, int green, int blue) throws Exception {

        CmsColor color = new CmsColor();
        color.setRGB(red, green, blue);

        m_red = red;
        m_green = green;
        m_blue = blue;
        m_hue = color.getHue();
        m_saturation = color.getSaturation();
        m_brightness = color.getValue();

        m_tbRed.setText(Integer.toString(m_red));
        m_tbGreen.setText(Integer.toString(m_green));
        m_tbBlue.setText(Integer.toString(m_blue));
        m_tbHue.setText(Integer.toString(m_hue));
        m_tbSaturation.setText(Integer.toString(m_saturation));
        m_tbBrightness.setText(Integer.toString(m_brightness));
        m_tbHexColor.setText(color.getHex());
        setPreview(color.getHex());

        updateSliders();
    }

    /**
     * Divides the first value by 256 then multiplies it by the second value.<p>
     * @param first value.
     * @param second value.
     * @return result (first / 256) * second
     */
    private int percentOf(float first, float second) {

        return (int)((first / 256) * second);
    }

    /**
     * Called when the widget wants to update the preview color sample box in the top-right corner of the UI.
     * @param hex Hexadecimal notation of RGB
     */
    private void setPreview(String hex) {

        m_colorpreview.getElement().getStyle().setBackgroundColor("#" + hex);
    }

    /**
     * Called whenever the internal state has been changed and needs to synchronize the other components.
     */
    private void updateSliders() {

        onClick(m_radioButtonGroup.getSelectedButton());

    }
}