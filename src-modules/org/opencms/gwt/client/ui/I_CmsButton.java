/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/I_CmsButton.java,v $
 * Date   : $Date: 2011/03/02 14:24:09 $
 * Version: $Revision: 1.4 $
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

    /** Available button icons. */
    public enum ButtonData {

        /** Toolbar button. */
        ADD(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarAdd(), Messages.get().key(
            Messages.GUI_TOOLBAR_ADD_0)),

        /** Toolbar button. */
        ADD_TO_FAVORITES(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarClipboard(),
        Messages.get().key(Messages.GUI_TOOLBAR_ADD_TO_FAVORITES_0)),

        /** Toolbar button. */
        CONTEXT(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarContext(), Messages.get().key(
            Messages.GUI_TOOLBAR_CONTEXT_0)),

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

    /**
     *  Enumeration of available icons.<p>
     */
    public static enum UiIcon {
        /**  Icon name. */
        alert,
        /**  Icon name. */
        arrow_1_e,
        /**  Icon name. */
        arrow_1_n,
        /**  Icon name. */
        arrow_1_ne,
        /**  Icon name. */
        arrow_1_nw,
        /**  Icon name. */
        arrow_1_s,
        /**  Icon name. */
        arrow_1_se,
        /**  Icon name. */
        arrow_1_sw,
        /**  Icon name. */
        arrow_1_w,
        /**  Icon name. */
        arrow_2_e_w,
        /**  Icon name. */
        arrow_2_n_s,
        /**  Icon name. */
        arrow_2_ne_sw,
        /**  Icon name. */
        arrow_2_se_nw,
        /**  Icon name. */
        arrow_4,
        /**  Icon name. */
        arrow_4_diag,
        /**  Icon name. */
        arrowrefresh_1_e,
        /**  Icon name. */
        arrowrefresh_1_n,
        /**  Icon name. */
        arrowrefresh_1_s,
        /**  Icon name. */
        arrowrefresh_1_w,
        /**  Icon name. */
        arrowreturn_1_e,
        /**  Icon name. */
        arrowreturn_1_n,
        /**  Icon name. */
        arrowreturn_1_s,
        /**  Icon name. */
        arrowreturn_1_w,
        /**  Icon name. */
        arrowreturnthick_1_e,
        /**  Icon name. */
        arrowreturnthick_1_n,
        /**  Icon name. */
        arrowreturnthick_1_s,
        /**  Icon name. */
        arrowreturnthick_1_w,
        /**  Icon name. */
        arrowstop_1_e,
        /**  Icon name. */
        arrowstop_1_n,
        /**  Icon name. */
        arrowstop_1_s,
        /**  Icon name. */
        arrowstop_1_w,
        /**  Icon name. */
        arrowthick_1_e,
        /**  Icon name. */
        arrowthick_1_n,
        /**  Icon name. */
        arrowthick_1_ne,
        /**  Icon name. */
        arrowthick_1_nw,
        /**  Icon name. */
        arrowthick_1_s,
        /**  Icon name. */
        arrowthick_1_se,
        /**  Icon name. */
        arrowthick_1_sw,
        /**  Icon name. */
        arrowthick_1_w,
        /**  Icon name. */
        arrowthick_2_e_w,
        /**  Icon name. */
        arrowthick_2_n_s,
        /**  Icon name. */
        arrowthick_2_ne_sw,
        /**  Icon name. */
        arrowthick_2_se_nw,
        /**  Icon name. */
        arrowthickstop_1_e,
        /**  Icon name. */
        arrowthickstop_1_n,
        /**  Icon name. */
        arrowthickstop_1_s,
        /**  Icon name. */
        arrowthickstop_1_w,
        /**  Icon name. */
        battery_0,
        /**  Icon name. */
        battery_1,
        /**  Icon name. */
        battery_2,
        /**  Icon name. */
        battery_3,
        /**  Icon name. */
        bookmark,
        /**  Icon name. */
        bullet,
        /**  Icon name. */
        calculator,
        /**  Icon name. */
        calendar,
        /**  Icon name. */
        cancel,
        /**  Icon name. */
        carat_1_e,
        /**  Icon name. */
        carat_1_n,
        /**  Icon name. */
        carat_1_ne,
        /**  Icon name. */
        carat_1_nw,
        /**  Icon name. */
        carat_1_s,
        /**  Icon name. */
        carat_1_se,
        /**  Icon name. */
        carat_1_sw,
        /**  Icon name. */
        carat_1_w,
        /**  Icon name. */
        carat_2_e_w,
        /**  Icon name. */
        carat_2_n_s,
        /**  Icon name. */
        cart,
        /**  Icon name. */
        check,
        /**  Icon name. */
        circle_arrow_e,
        /**  Icon name. */
        circle_arrow_n,
        /**  Icon name. */
        circle_arrow_s,
        /**  Icon name. */
        circle_arrow_w,
        /**  Icon name. */
        circle_check,
        /**  Icon name. */
        circle_close,
        /**  Icon name. */
        circle_minus,
        /**  Icon name. */
        circle_plus,
        /**  Icon name. */
        circle_triangle_e,
        /**  Icon name. */
        circle_triangle_n,
        /**  Icon name. */
        circle_triangle_s,
        /**  Icon name. */
        circle_triangle_w,
        /**  Icon name. */
        circle_zoomin,
        /**  Icon name. */
        circle_zoomout,
        /**  Icon name. */
        circlesmall_close,
        /**  Icon name. */
        circlesmall_minus,
        /**  Icon name. */
        circlesmall_plus,
        /**  Icon name. */
        clipboard,
        /**  Icon name. */
        clock,
        /**  Icon name. */
        close,
        /**  Icon name. */
        closethick,
        /**  Icon name. */
        comment,
        /**  Icon name. */
        contact,
        /**  Icon name. */
        copy,
        /**  Icon name. */
        disk,
        /**  Icon name. */
        document,
        /**  Icon name. */
        document_b,
        /**  Icon name. */
        eject,
        /**  Icon name. */
        extlink,
        /**  Icon name. */
        flag,
        /**  Icon name. */
        folder_collapsed,
        /**  Icon name. */
        folder_open,
        /**  Icon name. */
        gear,
        /**  Icon name. */
        grip_diagonal_se,
        /**  Icon name. */
        grip_dotted_horizontal,
        /**  Icon name. */
        grip_dotted_vertical,
        /**  Icon name. */
        grip_solid_horizontal,
        /**  Icon name. */
        grip_solid_vertical,
        /**  Icon name. */
        gripsmall_diagonal_se,
        /**  Icon name. */
        heart,
        /**  Icon name. */
        help,
        /**  Icon name. */
        home,
        /**  Icon name. */
        image,
        /**  Icon name. */
        info,
        /**  Icon name. */
        key,
        /**  Icon name. */
        lightbulb,
        /**  Icon name. */
        link,
        /**  Icon name. */
        locked,
        /**  Icon name. */
        mail_closed,
        /**  Icon name. */
        mail_open,
        /**  Icon name. */
        minus,
        /**  Icon name. */
        minusthick,
        /**  Icon name. */
        newwin,
        /**  Icon name. */
        note,
        /**  Icon name. */
        notice,
        /**  Icon name. */
        pause,
        /**  Icon name. */
        pencil,
        /**  Icon name. */
        person,
        /**  Icon name. */
        pin_s,
        /**  Icon name. */
        pin_w,
        /**  Icon name. */
        play,
        /**  Icon name. */
        plus,
        /**  Icon name. */
        plusthick,
        /**  Icon name. */
        power,
        /**  Icon name. */
        print,
        /**  Icon name. */
        radio_off,
        /**  Icon name. */
        radio_on,
        /**  Icon name. */
        refresh,
        /**  Icon name. */
        scissors,
        /**  Icon name. */
        script,
        /**  Icon name. */
        search,
        /**  Icon name. */
        seek_end,
        /**  Icon name. */
        seek_first,
        /**  Icon name. */
        seek_next,
        /**  Icon name. */
        seek_prev,
        /**  Icon name. */
        shuffle,
        /**  Icon name. */
        signal,
        /**  Icon name. */
        signal_diag,
        /**  Icon name. */
        squaresmall_close,
        /**  Icon name. */
        squaresmall_minus,
        /**  Icon name. */
        squaresmall_plus,
        /**  Icon name. */
        star,
        /**  Icon name. */
        stop,
        /**  Icon name. */
        suitcase,
        /**  Icon name. */
        tag,
        /**  Icon name. */
        transfer_e_w,
        /**  Icon name. */
        transferthick_e_w,
        /**  Icon name. */
        trash,
        /**  Icon name. */
        triangle_1_e,
        /**  Icon name. */
        triangle_1_n,
        /**  Icon name. */
        triangle_1_ne,
        /**  Icon name. */
        triangle_1_nw,
        /**  Icon name. */
        triangle_1_s,
        /**  Icon name. */
        triangle_1_se,
        /**  Icon name. */
        triangle_1_sw,
        /**  Icon name. */
        triangle_1_w,
        /**  Icon name. */
        triangle_2_e_w,
        /**  Icon name. */
        triangle_2_n_s,
        /**  Icon name. */
        unlocked,
        /**  Icon name. */
        video,
        /**  Icon name. */
        volume_off,
        /**  Icon name. */
        volume_on,
        /**  Icon name. */
        wrench,
        /**  Icon name. */
        zoomin,
        /**  Icon name. */
        zoomout;
    }

}
