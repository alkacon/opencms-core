/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsTextButton.java,v $
 * Date   : $Date: 2010/04/15 13:53:28 $
 * Version: $Revision: 1.6 $
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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiConstructor;

/**
 * Provides a text button.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsTextButton extends CmsButton {

    /** CSS style variants. */
    public static enum ButtonStyle {

        /** Big button style. */
        cmsButtonBig(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonBig()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textBig()),

        /** Medium button style. */
        cmsButtonMedium(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonMedium()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textMedium()),

        /** Small button style. */
        cmsButtonSmall(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonSmall()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textSmall());

        /** The CSS class name. */
        private String m_cssClassName;

        /**
         * Constructor.<p>
         * 
         * @param cssClassName the CSS class name
         */
        ButtonStyle(String cssClassName) {

            m_cssClassName = cssClassName;
        }

        /**
         * Returns the CSS class name of this style.<p>
         * 
         * @return the CSS class name
         */
        String getCssClassName() {

            return m_cssClassName;
        }
    }

    /** The style of this button. */
    private static final ButtonStyle DEFAULT_BUTTON_STYLE = ButtonStyle.cmsButtonMedium;

    /**
     * The constructor.<p>
     */
    public CmsTextButton() {

        this(DEFAULT_BUTTON_STYLE);
    }

    /**
     * The constructor.<p>
     * 
     * @param style the style for this button
     */
    public CmsTextButton(ButtonStyle style) {

        super();
        addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsTextButton());
        addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        addStyleName(style.getCssClassName());
    }

    /**
     * The constructor.
     * 
     * @param buttonStyle the style name for this button
     */
    @UiConstructor
    public CmsTextButton(String buttonStyle) {

        this(ButtonStyle.valueOf(buttonStyle));
    }

    /**
     * Constructor for <code>CmsTextButton</code>. The supplied text is used to
     * construct the default face of the button.<p>
     * 
     * @param text the text for the default (up) face of the button, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     */
    public CmsTextButton(String text, String imageClass) {

        this(text, imageClass, DEFAULT_BUTTON_STYLE);
    }

    /**
     * Constructor for <code>CmsTextButton</code>. The supplied text is used to
     * construct the default face of the button.<p>
     * 
     * @param text the text for the default (up) face of the button, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     * @param handler the click handler
     */
    public CmsTextButton(String text, String imageClass, ClickHandler handler) {

        this(text, imageClass, handler, DEFAULT_BUTTON_STYLE);
    }

    /**
     * Constructor for <code>CmsTextButton</code>. The supplied text is used to
     * construct the default face of the button.<p>
     * 
     * @param text the text for the default (up) face of the button, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     * @param style the style for this button
     */
    public CmsTextButton(String text, String imageClass, ButtonStyle style) {

        this(style);
        setUpFace(text, imageClass);
    }

    /**
     * Constructor for <code>CmsTextButton</code>. The supplied text is used to
     * construct the default face of the button.
     * 
     * @param text the text for the default (up) face of the button, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     * @param handler the click handler
     * @param style the style for this button
     */
    public CmsTextButton(String text, String imageClass, ClickHandler handler, ButtonStyle style) {

        this(text, imageClass, style);
        addClickHandler(handler);
    }

    /**
     * Enables/disables minimum width on button. By default no minimum width is set.<p>
     * 
     * @param hasMinWidth if <code>true</code> the minimum width is set to 6em
     */
    public void useMinWidth(boolean hasMinWidth) {

        if (hasMinWidth) {
            addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMinWidth());
        } else {
            removeStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMinWidth());
        }
    }
}
