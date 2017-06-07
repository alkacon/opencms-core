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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PushButton;

/**
 * Push button class.<p>
 *
 * Uses CSS classes cmsState and dependent from 'button.css', make sure it is injected.<p>
 *
 * @since 8.0.0
 */
public class CmsPushButton extends PushButton implements HasHorizontalAlignment {

    /** The current horizontal alignment. */
    private HorizontalAlignmentConstant m_align;

    /** Stores the button style. */
    private ButtonStyle m_buttonStyle;

    /** Stores the button color. */
    private I_CmsButton.ButtonColor m_color;

    /** The down face image class. */
    private String m_downImageClass;

    /** The image class. */
    private String m_imageClass;

    /** Flag to indicate the button was reenalbled. Set until the next mouse up, over or out event. */
    private boolean m_isReenabled;

    /** The button size. */
    private I_CmsButton.Size m_size;

    /** The button text. */
    private String m_text;

    /** The title. */
    private String m_title;

    /** Use minimum width flag. */
    private boolean m_useMinWidth;

    /** The disabled reason. */
    private String m_disabledReason;

    /**
     * The constructor.<p>
     */
    public CmsPushButton() {

        m_align = HasHorizontalAlignment.ALIGN_RIGHT;
        setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsState());
        setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        setSize(I_CmsButton.Size.medium);
    }

    /**
     * The constructor. Setting the button icon.<p>
     *
     * @param imageClass the class for the image
     */
    public CmsPushButton(String imageClass) {

        this();
        setImageClass(imageClass);
    }

    /**
     * The constructor. Setting different icons for the up and down face of the button.<p>
     *
     * @param imageClass the class for the up face image
     * @param downImageClass the class for the down face image
     */
    public CmsPushButton(String imageClass, String downImageClass) {

        this(imageClass);
        setDownImageClass(downImageClass);
    }

    /**
     * Clears the hover state of the button.<p>
     * Use when opening modal popups to ensure the button is not left in hovered state.<p>
     */
    public void clearHoverState() {

        setEnabled(!isEnabled());
        setEnabled(!isEnabled());
    }

    /**
     * Disables the button and changes the button title attribute to the disabled reason.<p>
     *
     * @param disabledReason the disabled reason
     */
    public void disable(String disabledReason) {

        setDown(false);
        setEnabled(false);
        m_disabledReason = disabledReason;
        super.setTitle(disabledReason);
    }

    /**
     * Enables the button, switching the button title attribute from the disabled reason to the original title.<p>
     */
    public void enable() {

        m_isReenabled = true;
        setEnabled(true);
        m_disabledReason = null;
        super.setTitle(m_title);
    }

    /**
     * Returns the disabled reason.<p>
     *
     * @return the disabled reason
     */
    public String getDisabledReason() {

        return m_disabledReason;
    }

    /**
     * Returns the image class of the down face.<p>
     *
     * @return the image class of the down face
     */
    public String getDownImageClass() {

        return m_downImageClass;
    }

    /**
     * This is the alignment of the text in reference to the image, possible values are left or right.<p>
     *
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#getHorizontalAlignment()
     */
    public HorizontalAlignmentConstant getHorizontalAlignment() {

        return m_align;
    }

    /**
     * Returns the master image class.<p>
     *
     * @return the master image class
     */
    public String getImageClass() {

        return m_imageClass;
    }

    /**
     * Returns the size.<p>
     *
     * @return the size
     */
    public I_CmsButton.Size getSize() {

        return m_size;
    }

    /**
     * Returns the text.<p>
     *
     * @return the text
     */
    @Override
    public String getText() {

        return m_text;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return m_title;
    }

    /**
     * @see com.google.gwt.user.client.ui.CustomButton#isDown()
     */
    @Override
    public boolean isDown() {

        return super.isDown();
    }

    /**
     * Checks if the button is constraint to a minimal width.<p>
     *
     * @return <code>true</code> if the button is constraint to a minimal width
     */
    public boolean isUseMinWidth() {

        return m_useMinWidth;
    }

    /**
     * @see com.google.gwt.user.client.ui.CustomButton#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {

        // if the button is enabled while the mouse-pointer is within the button element,
        // the mouse-over element will not get triggered again
        // this may prevent correct handling of the click event
        if (isEnabled() && m_isReenabled) {
            int type = DOM.eventGetType(event);
            switch (type) {
                case Event.ONMOUSEUP:
                    m_isReenabled = false;
                    CmsDomUtil.ensureMouseOver(getElement());
                    break;
                case Event.ONMOUSEOVER:
                case Event.ONMOUSEOUT:
                    m_isReenabled = false;
                    break;
                default:
            }
        }

        super.onBrowserEvent(event);
    }

    /**
     * Sets the button style.<p>
     *
     * @param style the style to set
     * @param color the color to set
     */
    public void setButtonStyle(I_CmsButton.ButtonStyle style, I_CmsButton.ButtonColor color) {

        if (m_buttonStyle != null) {
            for (String styleName : m_buttonStyle.getAdditionalClasses()) {
                removeStyleName(styleName);
            }
        }
        if (style == ButtonStyle.TRANSPARENT) {
            setSize(null);
        }
        addStyleName(style.getCssClassName());
        m_buttonStyle = style;

        if (m_color != null) {
            removeStyleName(m_color.getClassName());
        }
        if (color != null) {
            addStyleName(color.getClassName());
        }
        m_color = color;
    }

    /**
     * @see com.google.gwt.user.client.ui.CustomButton#setDown(boolean)
     */
    @Override
    public void setDown(boolean down) {

        super.setDown(down);
    }

    /**
     * Sets the down face text and image.<p>
     *
     * @param text the down face text to set, set to <code>null</code> to not show any
     * @param imageClass the down face image class to use, set to <code>null</code> to not show any
     */
    public void setDownFace(String text, String imageClass) {

        m_downImageClass = imageClass;
        getDownFace().setHTML(getFaceHtml(text, imageClass));
    }

    /**
     * Sets the image class for the down face.<p>
     *
     * @param downImageClass the image class to set
     */
    public void setDownImageClass(String downImageClass) {

        setDownFace(m_text, downImageClass);
    }

    /**
     * This is the alignment of the text in reference to the image, possible values are left or right.<p>
     *
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#setHorizontalAlignment(com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant)
     */
    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {

        if (align.equals(HasHorizontalAlignment.ALIGN_CENTER)) {
            // ignore center alignment
            return;
        }
        m_align = align;
    }

    /**
     * Sets the master image class.<p>
     *
     * @param imageClass the master image class to set
     */
    public void setImageClass(String imageClass) {

        setUpFace(m_text, imageClass);
    }

    /**
     * Sets the size.<p>
     *
     * @param size the size to set
     */
    public void setSize(I_CmsButton.Size size) {

        if (m_size != null) {
            removeStyleName(m_size.getCssClassName());
        }
        if (size != null) {
            addStyleName(size.getCssClassName());
        }
        m_size = size;
    }

    /**
     * Sets the text.<p>
     *
     * @param text the text to set
     */
    @Override
    public void setText(String text) {

        setUpFace(text, m_imageClass);
        setTitle(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {

        super.setTitle(title);
        m_title = title;
    }

    /**
     * Sets the up face text and image.<p>
     *
     * @param text the up face text to set, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     */
    public void setUpFace(String text, String imageClass) {

        m_text = text;
        m_imageClass = imageClass;
        getUpFace().setHTML(getFaceHtml(text, imageClass));
    }

    /**
     * Tells the button to use a minimal width.<p>
     *
     * @param useMinWidth <code>true</code> to use a minimal width
     */
    public void setUseMinWidth(boolean useMinWidth) {

        if (useMinWidth != m_useMinWidth) {
            if (useMinWidth) {
                addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMinWidth());
            } else {
                removeStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMinWidth());
            }
            m_useMinWidth = useMinWidth;
        }
    }

    /**
     * Convenience method to assemble the HTML to use for a button face.<p>
     *
     * @param text text the up face text to set, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     *
     * @return the HTML
     */
    protected String getFaceHtml(String text, String imageClass) {

        return CmsDomUtil.createFaceHtml(text, imageClass, m_align);
    }
}
