/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/I_CmsButton.java,v $
 * Date   : $Date: 2011/03/28 09:57:06 $
 * Version: $Revision: 1.8 $
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

/**
 * Interface to hold button related enumerations. To be used with {@link org.opencms.gwt.client.ui.CmsPushButton}
 * and {@link org.opencms.gwt.client.ui.CmsToggleButton}.<p>
 */
public interface I_CmsButton {

    /** Available button icons. */
    public enum ButtonData {

        /** Toolbar button. */
        ADD(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarAdd(), Messages.get().key(
            Messages.GUI_TOOLBAR_ADD_0), true),

        /** Toolbar button. */
        ADD_TO_FAVORITES(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarClipboard(),
        Messages.get().key(Messages.GUI_TOOLBAR_ADD_TO_FAVORITES_0), false),

        /** Toolbar button. */
        CLIPBOARD(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarClipboard(), Messages.get().key(
            Messages.GUI_TOOLBAR_CLIPBOARD_0), true),

        /** Toolbar button. */
        CONTEXT(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarContext(), Messages.get().key(
            Messages.GUI_TOOLBAR_CONTEXT_0), true),

        /** Toolbar button. */
        DELETE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarDelete(), Messages.get().key(
            Messages.GUI_TOOLBAR_DELETE_0), false),

        /** Toolbar button. */
        EDIT(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarEdit(), Messages.get().key(
            Messages.GUI_TOOLBAR_EDIT_0), false),

        /** Toolbar button. */
        MOVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarMove(), Messages.get().key(
            Messages.GUI_TOOLBAR_MOVE_0), false),

        /** Toolbar button. */
        NEW(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarNew(), Messages.get().key(
            Messages.GUI_TOOLBAR_NEW_0), false),

        /** Toolbar button. */
        PROPERTIES(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarProperties(), Messages.get().key(
            Messages.GUI_TOOLBAR_PROPERTIES_0), false),

        /** Toolbar button. */
        PUBLISH(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarPublish(), Messages.get().key(
            Messages.GUI_TOOLBAR_PUBLISH_0), false),

        /** Toolbar button. */
        REMOVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarRemove(), Messages.get().key(
            Messages.GUI_TOOLBAR_REMOVE_0), false),

        /** Toolbar button. */
        RESET(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarReset(), Messages.get().key(
            Messages.GUI_TOOLBAR_RESET_0), false),

        /** Toolbar button. */
        SAVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSave(), Messages.get().key(
            Messages.GUI_TOOLBAR_SAVE_0), false),

        /** Toolbar button. */
        SELECTION(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSelection(), Messages.get().key(
            Messages.GUI_TOOLBAR_SELECTION_0), false),

        /** Toolbar button. */
        SITEMAP(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSitemap(), Messages.get().key(
            Messages.GUI_TOOLBAR_SITEMAP_0), false);

        /** The icon class name. */
        private String m_iconClass;

        /** The title. */
        private String m_title;

        private boolean m_menu;

        /**
         * Returns the menu.<p>
         *
         * @return the menu
         */
        public boolean isMenu() {

            return m_menu;
        }

        /**
         * Sets the menu.<p>
         *
         * @param menu the menu to set
         */
        public void setMenu(boolean menu) {

            m_menu = menu;
        }

        /**
         * Constructor.<p>
         * 
         * @param iconClass the icon class name
         * @param title the title
         */
        private ButtonData(String iconClass, String title, boolean menu) {

            m_iconClass = iconClass;
            m_title = title;
            m_menu = menu;
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

    /** Available button styles. */
    public enum ButtonStyle {

        /** Default button. */
        TEXT(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsTextButton(),
        I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()),

        /** Menu button. */
        MENU(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMenuButton(),
        I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()),

        /** Menu button. */
        IMAGE(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsImageButton(),
        I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()),

        /** Transparent button. */
        TRANSPARENT(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsTransparentButton());

        /** The list of additional style class names for this button style. */
        private String[] m_additionalClasses;

        /**
         * Constructor.<p>
         * 
         * @param additionalClasses the additional classes
         */
        private ButtonStyle(String... additionalClasses) {

            m_additionalClasses = additionalClasses;
        }

        /**
         * Returns the additional classes.<p>
         *
         * @return the additional classes
         */
        public String[] getAdditionalClasses() {

            return m_additionalClasses;
        }

        /**
         * Sets the additional classes.<p>
         *
         * @param additionalClasses the additional classes to set
         */
        public void setAdditionalClasses(String[] additionalClasses) {

            m_additionalClasses = additionalClasses;
        }

        /**
         * Returns the classes stored in the array as space separated list.<p>
         * 
         * @return the classes stored in the array as space separated list
         */
        public String getCssClassName() {

            StringBuffer sb = new StringBuffer();
            for (String addClass : m_additionalClasses) {
                sb.append(addClass + " ");
            }
            return sb.toString();
        }
    }

    /** CSS style variants. */
    public static enum Size {

        /** Big button style. */
        big(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonBig()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textBig()),

        /** Medium button style. */
        medium(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonMedium()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textMedium()),

        /** Small button style. */
        small(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonSmall()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textSmall());

        /** The CSS class name. */
        private String m_cssClassName;

        /**
         * Constructor.<p>
         * 
         * @param cssClassName the CSS class name
         */
        Size(String cssClassName) {

            m_cssClassName = cssClassName;
        }

        /**
         * Returns the CSS class name of this style.<p>
         * 
         * @return the CSS class name
         */
        public String getCssClassName() {

            return m_cssClassName;
        }
    }
}
