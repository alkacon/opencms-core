/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsButton.java,v $
 * Date   : $Date: 2010/03/29 06:39:40 $
 * Version: $Revision: 1.3 $
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

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.PushButton;

/**
 * Base button class.<p>
 * 
 * Uses CSS classes cmsState and dependent from 'button.css', make sure it is injected<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsButton extends PushButton {

    /**
     * The constructor.<p>
     */
    public CmsButton() {

        super();
        setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsState());
    }

    static {
        I_CmsLayoutBundle.INSTANCE.buttonCss().ensureInjected();
    }

    /**
     * Sets the button to an absolute position.<p>
     * 
     * @param bottom CSS bottom value
     * @param left CSS left value
     */
    public void setBottomLeft(double bottom, double left) {

        Style style = this.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setBottom(bottom, Unit.PX);
        style.setLeft(left, Unit.PX);
    }

    /**
     * Sets the button to an absolute position.<p>
     * 
     * @param bottom CSS bottom value
     * @param right CSS right value
     */
    public void setBottomRight(double bottom, double right) {

        Style style = this.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setBottom(bottom, Unit.PX);
        style.setRight(right, Unit.PX);
    }

    /**
     * @see com.google.gwt.user.client.ui.CustomButton#setDown(boolean)
     */
    @Override
    public void setDown(boolean down) {

        super.setDown(down);
    }

    /**
     * Sets the down face text and image. If there is no down face set, the up face will be used on button down<p>
     * 
     * @param text the up face text to set, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     */
    public void setDownFace(String text, String imageClass) {

        this.getDownFace().setHTML(getFaceHtml(text, imageClass));
    }

    /**
     * Sets the button to an absolute position.<p>
     * 
     * @param top CSS top value
     * @param left CSS left value
     */
    public void setTopLeft(double top, double left) {

        Style style = this.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setTop(top, Unit.PX);
        style.setLeft(left, Unit.PX);

    }

    /**
     * Sets the button to an absolute position.<p>
     * 
     * @param top CSS top value
     * @param right CSS right value
     */
    public void setTopRight(double top, double right) {

        Style style = this.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setTop(top, Unit.PX);
        style.setRight(right, Unit.PX);
    }

    /**
     * Sets the up face text and image.<p>
     * 
     * @param text the up face text to set, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     */
    public void setUpFace(String text, String imageClass) {

        this.getUpFace().setHTML(getFaceHtml(text, imageClass));
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

}
