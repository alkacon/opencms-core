/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsIconBundle.java,v $
 * Date   : $Date: 2011/02/24 15:23:01 $
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

/**
 * Image bundle for smaller icons.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public interface I_CmsIconBundle extends ClientBundle {

    /** Instance of this image bundle. */
    I_CmsIconBundle INSTANCE = GWT.create(I_CmsIconBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/availabilityIconActive.png")
    ImageResource availabilityIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/availabilityIconDeactivated.png")
    ImageResource availabilityIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/availabilityIconSmall.png")
    ImageResource availabilityIconSmall();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("images/icons/bumpIconActive.png")
    ImageResource bumpIconActive();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("images/icons/bumpIconDeactivated.png")
    ImageResource bumpIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/deleteIconActive.png")
    ImageResource deleteIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/deleteIconDeactivated.png")
    ImageResource deleteIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/deleteIconInactive.png")
    ImageResource deleteIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/editorIconActive.png")
    ImageResource editorIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/editorIconDeactivated.png")
    ImageResource editorIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/editorIconInactive.png")
    ImageResource editorIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/favoriteIconActive.png")
    ImageResource favoriteIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/favoriteIconDeactivated.png")
    ImageResource favoriteIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/favoriteIconInactive.png")
    ImageResource favoriteIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/gotoPageIconActive.png")
    ImageResource gotoPageIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/gotoPageIconDeactivated.png")
    ImageResource gotoPageIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/gotoParentIconActive.png")
    ImageResource gotoParentIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/gotoParentIconDeactivated.png")
    ImageResource gotoParentIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/gotoSubSitemapIconActive.png")
    ImageResource gotoSubSitemapIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/lockOther.gif")
    ImageResource lockOther();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/lockShared.gif")
    ImageResource lockShared();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/lockUser.gif")
    ImageResource lockUser();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/mergeSitemapIconActive.png")
    ImageResource mergeSitemapIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/mergeSitemapIconDeactivated.png")
    ImageResource mergeSitemapIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/moveIconActive.png")
    ImageResource moveIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/moveIconDeactivated.png")
    ImageResource moveIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/moveIconInactive.png")
    ImageResource moveIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/newIconActive.png")
    ImageResource newIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/newIconDeactivated.png")
    ImageResource newIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/newIconInactive.png")
    ImageResource newIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/propertyIconActive.png")
    ImageResource propertyIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/propertyIconDeactivated.png")
    ImageResource propertyIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/propertyIconInactive.png")
    ImageResource propertyIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/refreshIconActive.png")
    ImageResource refreshIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/refreshIconDeactivated.png")
    ImageResource refreshIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/refreshIconInactive.png")
    ImageResource refreshIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/selectionIconActive.png")
    ImageResource selectionIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/selectionIconDeactivated.png")
    ImageResource selectionIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/selectionIconInactive.png")
    ImageResource selectionIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/subSitemapIconActive.png")
    ImageResource subSitemapIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/subSitemapIconDeactivated.png")
    ImageResource subSitemapIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/uploadIconActive.png")
    ImageResource uploadIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/uploadIconDeactivated.png")
    ImageResource uploadIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/uploadIconInactive.png")
    ImageResource uploadIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/warningIcon.png")
    ImageResource warningIcon();

}
