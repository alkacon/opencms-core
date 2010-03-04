/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsImageButton.java,v $
 * Date   : $Date: 2010/03/04 15:17:19 $
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

/**
 * Button holding only an image or icon.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsImageButton extends CmsButton {

    /**
     *  Enumeration of available icons.<p>
     */
    public static enum ICON {
        /**  Icon name. */
        carat_1_n,
        /**  Icon name. */
        carat_1_ne,
        /**  Icon name. */
        carat_1_e,
        /**  Icon name. */
        carat_1_se,
        /**  Icon name. */
        carat_1_s,
        /**  Icon name. */
        carat_1_sw,
        /**  Icon name. */
        carat_1_w,
        /**  Icon name. */
        carat_1_nw,
        /**  Icon name. */
        carat_2_n_s,
        /**  Icon name. */
        carat_2_e_w,
        /**  Icon name. */
        triangle_1_n,
        /**  Icon name. */
        triangle_1_ne,
        /**  Icon name. */
        triangle_1_e,
        /**  Icon name. */
        triangle_1_se,
        /**  Icon name. */
        triangle_1_s,
        /**  Icon name. */
        triangle_1_sw,
        /**  Icon name. */
        triangle_1_w,
        /**  Icon name. */
        triangle_1_nw,
        /**  Icon name. */
        triangle_2_n_s,
        /**  Icon name. */
        triangle_2_e_w,
        /**  Icon name. */
        arrow_1_n,
        /**  Icon name. */
        arrow_1_ne,
        /**  Icon name. */
        arrow_1_e,
        /**  Icon name. */
        arrow_1_se,
        /**  Icon name. */
        arrow_1_s,
        /**  Icon name. */
        arrow_1_sw,
        /**  Icon name. */
        arrow_1_w,
        /**  Icon name. */
        arrow_1_nw,
        /**  Icon name. */
        arrow_2_n_s,
        /**  Icon name. */
        arrow_2_ne_sw,
        /**  Icon name. */
        arrow_2_e_w,
        /**  Icon name. */
        arrow_2_se_nw,
        /**  Icon name. */
        arrowstop_1_n,
        /**  Icon name. */
        arrowstop_1_e,
        /**  Icon name. */
        arrowstop_1_s,
        /**  Icon name. */
        arrowstop_1_w,
        /**  Icon name. */
        arrowthick_1_n,
        /**  Icon name. */
        arrowthick_1_ne,
        /**  Icon name. */
        arrowthick_1_e,
        /**  Icon name. */
        arrowthick_1_se,
        /**  Icon name. */
        arrowthick_1_s,
        /**  Icon name. */
        arrowthick_1_sw,
        /**  Icon name. */
        arrowthick_1_w,
        /**  Icon name. */
        arrowthick_1_nw,
        /**  Icon name. */
        arrowthick_2_n_s,
        /**  Icon name. */
        arrowthick_2_ne_sw,
        /**  Icon name. */
        arrowthick_2_e_w,
        /**  Icon name. */
        arrowthick_2_se_nw,
        /**  Icon name. */
        arrowthickstop_1_n,
        /**  Icon name. */
        arrowthickstop_1_e,
        /**  Icon name. */
        arrowthickstop_1_s,
        /**  Icon name. */
        arrowthickstop_1_w,
        /**  Icon name. */
        arrowreturnthick_1_w,
        /**  Icon name. */
        arrowreturnthick_1_n,
        /**  Icon name. */
        arrowreturnthick_1_e,
        /**  Icon name. */
        arrowreturnthick_1_s,
        /**  Icon name. */
        arrowreturn_1_w,
        /**  Icon name. */
        arrowreturn_1_n,
        /**  Icon name. */
        arrowreturn_1_e,
        /**  Icon name. */
        arrowreturn_1_s,
        /**  Icon name. */
        arrowrefresh_1_w,
        /**  Icon name. */
        arrowrefresh_1_n,
        /**  Icon name. */
        arrowrefresh_1_e,
        /**  Icon name. */
        arrowrefresh_1_s,
        /**  Icon name. */
        arrow_4,
        /**  Icon name. */
        arrow_4_diag,
        /**  Icon name. */
        extlink,
        /**  Icon name. */
        newwin,
        /**  Icon name. */
        refresh,
        /**  Icon name. */
        shuffle,
        /**  Icon name. */
        transfer_e_w,
        /**  Icon name. */
        transferthick_e_w,
        /**  Icon name. */
        folder_collapsed,
        /**  Icon name. */
        folder_open,
        /**  Icon name. */
        document,
        /**  Icon name. */
        document_b,
        /**  Icon name. */
        note,
        /**  Icon name. */
        mail_closed,
        /**  Icon name. */
        mail_open,
        /**  Icon name. */
        suitcase,
        /**  Icon name. */
        comment,
        /**  Icon name. */
        person,
        /**  Icon name. */
        print,
        /**  Icon name. */
        trash,
        /**  Icon name. */
        locked,
        /**  Icon name. */
        unlocked,
        /**  Icon name. */
        bookmark,
        /**  Icon name. */
        tag,
        /**  Icon name. */
        home,
        /**  Icon name. */
        flag,
        /**  Icon name. */
        calendar,
        /**  Icon name. */
        cart,
        /**  Icon name. */
        pencil,
        /**  Icon name. */
        clock,
        /**  Icon name. */
        disk,
        /**  Icon name. */
        calculator,
        /**  Icon name. */
        zoomin,
        /**  Icon name. */
        zoomout,
        /**  Icon name. */
        search,
        /**  Icon name. */
        wrench,
        /**  Icon name. */
        gear,
        /**  Icon name. */
        heart,
        /**  Icon name. */
        star,
        /**  Icon name. */
        link,
        /**  Icon name. */
        cancel,
        /**  Icon name. */
        plus,
        /**  Icon name. */
        plusthick,
        /**  Icon name. */
        minus,
        /**  Icon name. */
        minusthick,
        /**  Icon name. */
        close,
        /**  Icon name. */
        closethick,
        /**  Icon name. */
        key,
        /**  Icon name. */
        lightbulb,
        /**  Icon name. */
        scissors,
        /**  Icon name. */
        clipboard,
        /**  Icon name. */
        copy,
        /**  Icon name. */
        contact,
        /**  Icon name. */
        image,
        /**  Icon name. */
        video,
        /**  Icon name. */
        script,
        /**  Icon name. */
        alert,
        /**  Icon name. */
        info,
        /**  Icon name. */
        notice,
        /**  Icon name. */
        help,
        /**  Icon name. */
        check,
        /**  Icon name. */
        bullet,
        /**  Icon name. */
        radio_off,
        /**  Icon name. */
        radio_on,
        /**  Icon name. */
        pin_w,
        /**  Icon name. */
        pin_s,
        /**  Icon name. */
        play,
        /**  Icon name. */
        pause,
        /**  Icon name. */
        seek_next,
        /**  Icon name. */
        seek_prev,
        /**  Icon name. */
        seek_end,
        /**  Icon name. */
        seek_first,
        /**  Icon name. */
        stop,
        /**  Icon name. */
        eject,
        /**  Icon name. */
        volume_off,
        /**  Icon name. */
        volume_on,
        /**  Icon name. */
        power,
        /**  Icon name. */
        signal_diag,
        /**  Icon name. */
        signal,
        /**  Icon name. */
        battery_0,
        /**  Icon name. */
        battery_1,
        /**  Icon name. */
        battery_2,
        /**  Icon name. */
        battery_3,
        /**  Icon name. */
        circle_plus,
        /**  Icon name. */
        circle_minus,
        /**  Icon name. */
        circle_close,
        /**  Icon name. */
        circle_triangle_e,
        /**  Icon name. */
        circle_triangle_s,
        /**  Icon name. */
        circle_triangle_w,
        /**  Icon name. */
        circle_triangle_n,
        /**  Icon name. */
        circle_arrow_e,
        /**  Icon name. */
        circle_arrow_s,
        /**  Icon name. */
        circle_arrow_w,
        /**  Icon name. */
        circle_arrow_n,
        /**  Icon name. */
        circle_zoomin,
        /**  Icon name. */
        circle_zoomout,
        /**  Icon name. */
        circle_check,
        /**  Icon name. */
        circlesmall_plus,
        /**  Icon name. */
        circlesmall_minus,
        /**  Icon name. */
        circlesmall_close,
        /**  Icon name. */
        squaresmall_plus,
        /**  Icon name. */
        squaresmall_minus,
        /**  Icon name. */
        squaresmall_close,
        /**  Icon name. */
        grip_dotted_vertical,
        /**  Icon name. */
        grip_dotted_horizontal,
        /**  Icon name. */
        grip_solid_vertical,
        /**  Icon name. */
        grip_solid_horizontal,
        /**  Icon name. */
        gripsmall_diagonal_se,
        /**  Icon name. */
        grip_diagonal_se;
    }

    /**
     * Constructor takes the CSS class of an image sprite and if to show border and background.<p> 
     * 
     * @param upImageClass up-face image sprite CSS class
     * @param hasBorder set <code>true</code> to show border and background
     */
    public CmsImageButton(String upImageClass, boolean hasBorder) {

        super();
        if (hasBorder) {
            this.addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsImageButton());
        } else {
            this.addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsImageButtonTransparent());
        }
        String upFaceHtml = "<div class='" + upImageClass + "'></div>";
        this.getUpFace().setHTML(upFaceHtml);
        I_CmsLayoutBundle.INSTANCE.buttonCss().ensureInjected();
    }

    /**
     * Constructor takes the CSS classes of image sprite and if to show border and background.<p> 
     * 
     * @param upImageClass up-face image sprite CSS class
     * @param downImageClass down-face image sprite CSS class
     * @param hasBorder set <code>true</code> to show border and background
     */
    public CmsImageButton(String upImageClass, String downImageClass, boolean hasBorder) {

        this(upImageClass, hasBorder);
        String downFaceHtml = "<div class='" + downImageClass + "'></div>";
        getDownFace().setHTML(downFaceHtml);
    }

    /**
     * Constructor takes the icon and if to show border and background.<p> 
     * 
     * @param upIcon up-face icon
     * @param hasBorder set <code>true</code> to show border and background
     */
    public CmsImageButton(ICON upIcon, boolean hasBorder) {

        this(I_CmsLayoutBundle.INSTANCE.iconsCss().uiIcon() + " " + upIcon.name(), hasBorder);
        I_CmsLayoutBundle.INSTANCE.iconsCss().ensureInjected();
    }

    /**
     * Constructor takes the icons and if to show border and background.<p> 
     * 
     * @param upIcon up-face icon
     * @param downIcon down-face icon
     * @param hasBorder set <code>true</code> to show border and background
     */
    public CmsImageButton(ICON upIcon, ICON downIcon, boolean hasBorder) {

        this(
            I_CmsLayoutBundle.INSTANCE.iconsCss().uiIcon() + " " + upIcon.name(),
            I_CmsLayoutBundle.INSTANCE.iconsCss().uiIcon() + " " + downIcon.name(),
            hasBorder);
        I_CmsLayoutBundle.INSTANCE.iconsCss().ensureInjected();
    }
}
