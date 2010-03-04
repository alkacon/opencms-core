/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsButton.java,v $
 * Date   : $Date: 2010/03/04 15:17:18 $
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
 * @version $Revision: 1.1 $
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

    /**
     * @see com.google.gwt.user.client.ui.CustomButton#setDown(boolean)
     */
    @Override
    public void setDown(boolean down) {

        super.setDown(down);
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

}
