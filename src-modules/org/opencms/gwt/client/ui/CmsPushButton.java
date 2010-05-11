/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsPushButton.java,v $
 * Date   : $Date: 2010/05/11 10:43:31 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PushButton;

/**
 * Push button class.<p>
 * 
 * Uses CSS classes cmsState and dependent from 'button.css', make sure it is injected.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsPushButton extends PushButton implements HasHorizontalAlignment {

    /** The current horizontal alignment. */
    private HorizontalAlignmentConstant m_align;

    /** The down face image class. */
    private String m_downImageClass;

    /** The image class. */
    private String m_imageClass;

    /** Show border flag. */
    private boolean m_showBorder;

    /** The button size. */
    private I_CmsButton.Size m_size;

    /** The button text. */
    private String m_text;

    /** The title. */
    private String m_title;

    /** Use minimum width flag. */
    private boolean m_useMinWidth;

    /**
     * The constructor.<p>
     */
    public CmsPushButton() {

        m_align = HasHorizontalAlignment.ALIGN_RIGHT;
        setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsState());
        setShowBorder(true);
        setSize(I_CmsButton.Size.medium);
    }

    /**
     * The constructor. Setting the button icon.<p>
     * 
     * @param icon the icon
     */
    public CmsPushButton(I_CmsButton.UiIcon icon) {

        this();
        setUiIcon(icon);
    }

    /**
     * The constructor. Setting different icons for the up and down face of the button.<p>
     * 
     * @param upIcon the up face icon
     * @param downIcon the down face icon
     */
    public CmsPushButton(I_CmsButton.UiIcon upIcon, I_CmsButton.UiIcon downIcon) {

        this(upIcon);
        setDownUiIcon(downIcon);
    }

    /**
     * Disables the button and changes the button title attribute to the disabled reason.<p>
     *   
     * @param disabledReason the disabled reason
     */
    public void disable(String disabledReason) {

        setEnabled(false);
        super.setTitle(disabledReason);
    }

    /**
     * Enables the button, switching the button title attribute from the disabled reason to the original title.<p>
     */
    public void enable() {

        setEnabled(true);
        super.setTitle(m_title);
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
     * Checks if the button has borders.<p>
     *
     * @return <code>true</code> if the button has borders
     */
    public boolean isShowBorder() {

        return m_showBorder;
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
     * Sets the down face icon.<p>
     * 
     * @param icon the icon
     */
    public void setDownUiIcon(I_CmsButton.UiIcon icon) {

        setDownImageClass(I_CmsLayoutBundle.INSTANCE.iconsCss().uiIcon() + " " + icon.name());
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
     * Tells the button to use or not borders.<p>
     *
     * @param showBorder <code>true</code> to use borders
     */
    public void setShowBorder(boolean showBorder) {

        if (showBorder != m_showBorder) {
            if (showBorder) {
                // removing old style
                removeStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsImageButtonTransparent());
                //setting new style
                addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsTextButton());
                addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
            } else {
                // removing old style
                removeStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsTextButton());
                removeStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
                //setting new style
                addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsImageButtonTransparent());
            }
            m_showBorder = showBorder;
        }
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
        addStyleName(size.getCssClassName());
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
     * Sets the image class of this button using the provided icon.<p>
     * 
     * @param icon the icon
     */
    public void setUiIcon(I_CmsButton.UiIcon icon) {

        setImageClass(I_CmsLayoutBundle.INSTANCE.iconsCss().uiIcon() + " " + icon.name());
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
