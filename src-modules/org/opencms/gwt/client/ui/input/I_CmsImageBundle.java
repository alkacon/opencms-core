/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/I_CmsImageBundle.java,v $
 * Date   : $Date: 2010/03/09 09:03:53 $
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

package org.opencms.gwt.client.ui.input;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Image bundle for this package.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public interface I_CmsImageBundle extends ClientBundle {

    /** The bundle instance. */
    I_CmsImageBundle INSTANCE = GWT.create(I_CmsImageBundle.class);

    /** 
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("../css/images/ui-bg_glass_75_cccccc_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource backgroundDark();

    /** 
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("../css/images/ui-bg_glass_75_e6e6e6_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource backgroundLight();

    /**
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/checked.png")
    ImageResource checkboxChecked();

    /**
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/checked.png")
    ImageResource checkboxCheckedBig();

    /**
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/checked-disabled.png")
    ImageResource checkboxCheckedDisabled();

    /**
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/checked-disabled.png")
    ImageResource checkboxCheckedDisabledBig();

    /**
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/unchecked.png")
    ImageResource checkboxUnchecked();

    /**
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/unchecked.png")
    ImageResource checkboxUncheckedBig();

    /**
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/unchecked-disabled.png")
    ImageResource checkboxUncheckedDisabled();

    /**
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/unchecked-disabled.png")
    ImageResource checkboxUncheckedDisabledBig();

    /** 
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/radioChecked.png")
    ImageResource radioChecked();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/radioCheckedDisabled.png")
    ImageResource radioCheckedDisabled();

    /** 
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/radioUnchecked.png")
    ImageResource radioUnchecked();

    /** 
     * Image resource accessor.<p>
     * @return an image resource
     */
    @Source("images/radioUncheckedDisabled.png")
    ImageResource radioUncheckedDisabled();

    /** 
     * Image resource accessor.<p>
     * @return an image resource
     */

    @Source("images/triangleDown.png")
    ImageResource triangleDown();

    /** 
     * Image resource accessor.<p>
     * @return an image resource
     */

    @Source("images/triangleRight.png")
    ImageResource triangleRight();

}
