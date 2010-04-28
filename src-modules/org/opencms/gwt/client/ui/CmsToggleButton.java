/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsToggleButton.java,v $
 * Date   : $Date: 2010/04/28 13:14:57 $
 * Version: $Revision: 1.2 $
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

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;

import com.google.gwt.user.client.ui.ToggleButton;

/**
 * Tool-bar button class.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsToggleButton extends ToggleButton {

    /** Available button icons. DEPRECATED: Use I_CmsButton.ButtonData instead.*/
    @Deprecated
    public enum ButtonData {

        /** Toolbar button. */
        ADD(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarAdd(), Messages.get().key(
            Messages.GUI_TOOLBAR_ADD_0)),

        /** Toolbar button. */
        CLIPBOARD(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarClipboard(), Messages.get().key(
            Messages.GUI_TOOLBAR_CLIPBOARD_0)),

        /** Toolbar button. */
        EDIT(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarEdit(), Messages.get().key(
            Messages.GUI_TOOLBAR_EDIT_0)),

        /** Toolbar button. */
        EXIT(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarExit(), Messages.get().key(
            Messages.GUI_TOOLBAR_EXIT_0)),

        /** Toolbar button. */
        MOVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarMove(), Messages.get().key(
            Messages.GUI_TOOLBAR_MOVE_0)),

        /** Toolbar button. */
        NEW(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarNew(), Messages.get().key(
            Messages.GUI_TOOLBAR_NEW_0)),

        /** Toolbar button. */
        PROPERTIES(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarProperties(), Messages.get().key(
            Messages.GUI_TOOLBAR_PROPERTIES_0)),

        /** Toolbar button. */
        PUBLISH(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarPublish(), Messages.get().key(
            Messages.GUI_TOOLBAR_PUBLISH_0)),

        /** Toolbar button. */
        REMOVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarRemove(), Messages.get().key(
            Messages.GUI_TOOLBAR_REMOVE_0)),

        /** Toolbar button. */
        RESET(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarReset(), Messages.get().key(
            Messages.GUI_TOOLBAR_RESET_0)),

        /** Toolbar button. */
        SAVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSave(), Messages.get().key(
            Messages.GUI_TOOLBAR_SAVE_0)),

        /** Toolbar button. */
        SELECTION(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSelection(), Messages.get().key(
            Messages.GUI_TOOLBAR_SELECTION_0)),

        /** Toolbar button. */
        SITEMAP(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSitemap(), Messages.get().key(
            Messages.GUI_TOOLBAR_SITEMAP_0));

        /** The icon class name. */
        private String m_iconClass;

        /** The title. */
        private String m_title;

        /**
         * Constructor.<p>
         * 
         * @param iconClass the icon class name
         * @param title the title
         */
        private ButtonData(String iconClass, String title) {

            m_iconClass = iconClass;
            m_title = title;
        }

        /**
         * Returns the CSS class name.<p>
         * 
         * @return the CSS class name
         */
        public String getIconClass() {

            return m_iconClass;
        }

        /**
         * Returns the title.<p>
         * 
         * @return the title
         */
        public String getTitle() {

            return m_title;
        }
    }

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
     * Constructor.<p>
     */
    public CmsToggleButton() {

        super();
        setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsState());
        addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsImageButton());
        addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        setShowBorder(true);
        setSize(I_CmsButton.Size.medium);
    }

    /**
     * Constructor.<p>
     * 
     * @param buttonData the button data 
     */
    public CmsToggleButton(I_CmsButton.ButtonData buttonData) {

        this();
        setImageClass(buttonData.getIconClass());
        setTitle(buttonData.getTitle());
    }

    /**
     * Constructor.<p>
     * 
     * DEPRECATED: Use setImageClass and setTitle instead.<p>
     * 
     * @param iconClass the icon to use for the button
     * @param title the button title
     */
    @Deprecated
    public CmsToggleButton(String iconClass, String title) {

        super();
        setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsState());
        addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsImageButton());
        addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        setTitle(title);
        String upFaceHtml = "<div class='" + iconClass + "'></div>";
        getUpFace().setHTML(upFaceHtml);
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
     * Returns the imageClass.<p>
     *
     * @return the imageClass
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
     * Returns the showBorder.<p>
     *
     * @return the showBorder
     */
    public boolean isShowBorder() {

        return m_showBorder;
    }

    /**
     * Returns the useMinWidth.<p>
     *
     * @return the useMinWidth
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
     * Sets the imageClass.<p>
     *
     * @param imageClass the imageClass to set
     */
    public void setImageClass(String imageClass) {

        setUpFace(m_text, imageClass);
        m_imageClass = imageClass;
    }

    /**
     * Sets the showBorder.<p>
     *
     * @param showBorder the showBorder to set
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
        m_text = text;
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
     * Sets the useMinWidth.<p>
     *
     * @param useMinWidth the useMinWidth to set
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

        String result = ((imageClass != null) && (imageClass.trim().length() > 0)) ? "<span class='"
            + imageClass
            + "'></span>" : "";
        if ((text != null) && (text.trim().length() > 0)) {
            result += (result.length() > 0) ? "&nbsp;" : "";
            result += text.trim();
        }
        return result;
    }

    /**
     * Sets the up face text and image.<p>
     * 
     * @param text the up face text to set, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     */
    protected void setUpFace(String text, String imageClass) {

        getUpFace().setHTML(getFaceHtml(text, imageClass));
    }
}
