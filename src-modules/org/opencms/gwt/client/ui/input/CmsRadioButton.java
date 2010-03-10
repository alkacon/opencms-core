/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsRadioButton.java,v $
 * Date   : $Date: 2010/03/10 12:51:58 $
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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.css.I_CmsInputImageBundle;

import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.Image;

/**
 * Class representing a single radio button.<p>
 * 
 * This class is a helper class for the CmsRadioButtonGroup class, and is not very useful by itself.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsRadioButton extends CustomButton {

    /** The image bundle used by this widget. */
    private static final I_CmsInputImageBundle IMAGES = I_CmsInputImageBundle.INSTANCE;

    /** The value associated with this radio button. */
    private String m_name;

    /**
     * Creates a new radio button.<p>
     * 
     * @param name the value associated with this radio button
     */
    public CmsRadioButton(String name) {

        m_name = name;
        getUpFace().setImage(new Image(IMAGES.radioUnchecked()));
        getDownFace().setImage(new Image(IMAGES.radioChecked()));
        getUpDisabledFace().setImage(new Image(IMAGES.radioUncheckedDisabled()));
        getDownDisabledFace().setImage(new Image(IMAGES.radioCheckedDisabled()));
    }

    /**
     * Returns the value associated with this radio button.<p>
     * 
     * @return the value associated with this radio button
     */
    public String getName() {

        return m_name;
    }

    /**
     * Raise visibility of setDown to public.<p>
     * 
     * @see com.google.gwt.user.client.ui.CustomButton#setDown(boolean)
     */
    public void setDown(boolean down) {

        super.setDown(down);
    }

    /**
     * When the user clicks on the radio button, it always has to be checked afterwards,
     * so we override the onClick method.
     * 
     * @see com.google.gwt.user.client.ui.CustomButton#onClick()
     */
    @Override
    protected void onClick() {

        super.onClick();
        setDown(true);
    }

}
