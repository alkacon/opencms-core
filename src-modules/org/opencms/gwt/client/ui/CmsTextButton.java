/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsTextButton.java,v $
 * Date   : $Date: 2010/03/03 15:32:37 $
 * Version: $Revision: 1.1 $
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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.PushButton;

/**
 * Provides a text button.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsTextButton extends PushButton {

    /** CSS style variants. */
    public static enum BUTTON_STYLE {

        /** Big button style. */
        cmsButtonBig(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonBig()),

        /** Medium button style. */
        cmsButtonMedium(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonMedium()),

        /** Small button style. */
        cmsButtonSmall(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonSmall());

        /** The CSS class name. */
        private String m_cssClassName;

        /**
         * Constructor.<p>
         * 
         * @param cssClassName the CSS class name
         */
        BUTTON_STYLE(String cssClassName) {

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
    private BUTTON_STYLE m_buttonStyle;

    /**
     * The constructor.<p>
     */
    public CmsTextButton() {

        super();
        this.setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsState());
        this.addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsTextButton());
        m_buttonStyle = BUTTON_STYLE.cmsButtonMedium;

    }

    /**
     * Constructor for <code>CmsTextButton</code>. The supplied text is used to
     * construct the default face of the button.
     * 
     * @param upText the text for the default (up) face of the button.
     */
    public CmsTextButton(String upText) {

        this();
        this.setText(upText);
    }

    /**
     * Constructor for <code>CmsTextButton</code>. The supplied text is used to
     * construct the default face of the button.
     * 
     * @param upText the text for the default (up) face of the button
     * @param handler the click handler
     */
    public CmsTextButton(String upText, ClickHandler handler) {

        this(upText);
        this.addClickHandler(handler);
    }

    /**
     * Constructor for <code>CmsTextButton</code>. The supplied text is used to
     * construct the default face of the button.
     * 
     * @param upText the text for the default (up) face of the button
     * @param style the style for this button
     */
    public CmsTextButton(String upText, BUTTON_STYLE style) {

        this(upText);
        setButtonStyle(style);
    }

    /**
     * Constructor for <code>CmsTextButton</code>. The supplied text is used to
     * construct the default face of the button.
     * 
     * @param upText the text for the default (up) face of the button
     * @param handler the click handler
     * @param style the style for this button
     */
    public CmsTextButton(String upText, ClickHandler handler, BUTTON_STYLE style) {

        this(upText, handler);
        setButtonStyle(style);
    }

    /**
     * Sets the button style.<p>
     * 
     * @param style the style name
     */
    public void setButtonStyle(String style) {

        setButtonStyle(BUTTON_STYLE.valueOf(style));
    }

    /**
     * Sets the button style.<p>
     * 
     * @param style the style
     */
    public void setButtonStyle(BUTTON_STYLE style) {

        if (style != m_buttonStyle) {
            removeStyleName(m_buttonStyle.getCssClassName());
            m_buttonStyle = style;
            addStyleName(style.getCssClassName());
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.CustomButton#setDown(boolean)
     */
    @Override
    public void setDown(boolean down) {

        super.setDown(down);
    }

}
