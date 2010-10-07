/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsInputImageBundle.java,v $
 * Date   : $Date: 2010/10/07 07:56:34 $
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

package org.opencms.gwt.client.ui.css;

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
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 */
public interface I_CmsInputImageBundle extends ClientBundle {

    /** The bundle instance. */
    I_CmsInputImageBundle INSTANCE = GWT.create(I_CmsInputImageBundle.class);

    /** 
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/ui-bg_glass_75_cccccc_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource backgroundDark();

    /** 
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/ui-bg_glass_75_e6e6e6_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource backgroundLight();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/checkboxChecked.png")
    ImageResource checkboxChecked();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/checkboxCheckedDisabled.png")
    ImageResource checkboxCheckedDisabled();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/checkboxCheckedHover.png")
    ImageResource checkboxCheckedHover();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/checkboxUnchecked.png")
    ImageResource checkboxUnchecked();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/checkboxUncheckedDisabled.png")
    ImageResource checkboxUncheckedDisabled();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/checkboxUncheckedHover.png")
    ImageResource checkboxUncheckedHover();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/minus.png")
    ImageResource minus();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/plus.png")
    ImageResource plus();

    /** 
     * Image resource accessor.<p>
     * 
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
     * 
     * @return an image resource
     */
    @Source("images/radioCheckedHover.png")
    ImageResource radioCheckedHover();

    /** 
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/radioUnchecked.png")
    ImageResource radioUnchecked();

    /** 
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/radioUncheckedDisabled.png")
    ImageResource radioUncheckedDisabled();

    /** 
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/radioUncheckedHover.png")
    ImageResource radioUncheckedHover();

}
