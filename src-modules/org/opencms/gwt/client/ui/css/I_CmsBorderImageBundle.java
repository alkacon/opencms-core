/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsBorderImageBundle.java,v $
 * Date   : $Date: 2011/01/14 13:46:56 $
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

package org.opencms.gwt.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Resource bundle to access border image resources.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
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
    @Source("images/borders/highlightBottomNew.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource highlightBottomNew();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/highlightBottomChanged.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource highlightBottomChanged();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/highlightLeftNew.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource highlightLeftNew();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/highlightLeftChanged.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource highlightLeftChanged();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/highlightRightNew.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource highlightRightNew();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/highlightRightChanged.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource highlightRightChanged();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/highlightTopNew.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource highlightTopNew();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/borders/highlightTopChanged.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource highlightTopChanged();
}
