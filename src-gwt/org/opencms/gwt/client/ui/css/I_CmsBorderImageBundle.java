/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * Resource bundle to access border image resources.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsBorderImageBundle extends ClientBundle {

    /** The bundle instance. */
    I_CmsBorderImageBundle INSTANCE = GWT.create(I_CmsBorderImageBundle.class);

    /** 
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/bottomNewBorderHighlight.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource bottomNewBorderHighlight();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/bottomChangedBorderHighlight.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource bottomChangedBorderHighlight();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/leftNewBorderHighlight.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource leftNewBorderHighlight();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/leftChangedBorderHighlight.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource leftChangedBorderHighlight();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/rightNewBorderHighlight.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource rightNewBorderHighlight();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/rightChangedBorderHighlight.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource rightChangedBorderHighlight();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/topNewBorderHighlight.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource topNewBorderHighlight();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/topChangedBorderHighlight.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource topChangedBorderHighlight();
}
