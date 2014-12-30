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

/**
 * Image bundle for smaller icons.<p>
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
    @Source("images/icons/addIconActive.png")
    ImageResource addIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/addIconDeactivated.png")
    ImageResource addIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/arrowDownIconActive.png")
    ImageResource arrowDownIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/arrowDownIconDeactivated.png")
    ImageResource arrowDownIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/arrowUpIconActive.png")
    ImageResource arrowUpIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/arrowUpIconDeactivated.png")
    ImageResource arrowUpIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/availabilityIcon.png")
    ImageResource availabilityIcon();

    /**
     * Access method.<p>
     *
     * @return the button CSS
     */
    @Source("images/icons/bumpIcon.png")
    ImageResource bumpIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/checkIconActive.png")
    ImageResource checkIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/checkIconDeactivated.png")
    ImageResource checkIconDeactivated();

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
    @Source("images/icons/editIconActive.png")
    ImageResource editIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/editIconDeactivated.png")
    ImageResource editIconDeactivated();

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
    @Source("images/icons/gotoPageIcon.png")
    ImageResource gotoPageIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/gotoParentIcon.png")
    ImageResource gotoParentIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/gotoSubSitemapIcon.png")
    ImageResource gotoSubSitemapIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/hideShowInNavigationIcon.png")
    ImageResource hideShowInNavigationIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/infoIconActive.png")
    ImageResource infoIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/infoIconDeactivated.png")
    ImageResource infoIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/inheritedIcon.png")
    ImageResource inheritedIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/lockIconActive.png")
    ImageResource lockIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/lockIconDeactivated.png")
    ImageResource lockIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/logoutIconActive.png")
    ImageResource logoutIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/menuIconActive.png")
    ImageResource menuIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/menuIconDeactivated.png")
    ImageResource menuIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/mergeSitemapIcon.png")
    ImageResource mergeSitemapIcon();

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
    @Source("images/icons/popupIconActive.png")
    ImageResource popupIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/popupIconDeactivated.png")
    ImageResource popupIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/previewIconActive.png")
    ImageResource previewIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/previewIconDeactivated.png")
    ImageResource previewIconDeactivated();

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
    @Source("images/icons/refreshIcon.png")
    ImageResource refreshIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/removeIconActive.png")
    ImageResource removeIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/removeIconDeactivated.png")
    ImageResource removeIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/renameIcon.png")
    ImageResource renameIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/replaceIcon.png")
    ImageResource replaceIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/replaceIconDeactivated.png")
    ImageResource replaceIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/searchIconActive.png")
    ImageResource searchIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/searchIconDeactivated.png")
    ImageResource searchIconDeactivated();

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
    @Source("images/icons/seoIcon.png")
    ImageResource seoIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/showPageIcon.png")
    ImageResource showPageIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/stateExportIcon.png")
    ImageResource stateExportIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/stateSecureIcon.png")
    ImageResource stateSecureIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/subSitemapIcon.png")
    ImageResource subSitemapIcon();

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
    @Source("images/icons/uploadSmallIconActive.png")
    ImageResource uploadSmallIconActive();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/uploadSmallIconDeactivated.png")
    ImageResource uploadSmallIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/workplaceIcon.png")
    ImageResource workplaceIcon();
}
