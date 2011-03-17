/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsBigIconBundle.java,v $
 * Date   : $Date: 2011/03/17 16:11:01 $
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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Image bundle for big icons.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public interface I_CmsBigIconBundle extends ClientBundle {

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/addBigIconActive.png")
    ImageResource addBigIconActive(); // toolbarNew();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/addBigIconDeactivated.png")
    ImageResource addBigIconDeactivated(); // toolbarNewSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/brokenLinkBigIcon.png")
    ImageResource brokenLinkBigIcon();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/clipboardBigIconActive.png")
    ImageResource clipboardBigIconActive(); // toolbarClipboard();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/clipboardBigIconDeactivated.png")
    ImageResource clipboardBigIconDeactivated(); // toolbarClipboardSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/icons/big/contextMenuBigIconActive.png")
    ImageResource contextMenuBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/contextMenuBigIconDeactivated.png")
    ImageResource contextMenuBigIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/contextMenuDownBigIcon.png")
    ImageResource contextMenuDownBigIcon();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/deleteBigIconActive.png")
    ImageResource deleteBigIconActive(); // toolbarRemove();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/deleteBigIconDeactivated.png")
    ImageResource deleteBigIconDeactivated(); // toolbarRemoveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/editBigIconActive.png")
    ImageResource editBigIconActive(); // toolbarEdit();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/editBigIconDeactivated.png")
    ImageResource editBigIconDeactivated(); // toolbarEditSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/gotoBigIconDeactivated.png")
    ImageResource gotoBigIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/gotoPageBigIconActive.png")
    ImageResource gotoPageBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/icons/big/gotoParentBigIconActive.png")
    ImageResource gotoParentBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/icons/big/gotoParentBigIconDeactivated.png")
    ImageResource gotoParentBigIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/icons/big/gotoSubSitemapBigIconActive.png")
    ImageResource gotoSubSitemapBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/menuBigIconActive.png")
    ImageResource menuBigIconActive(); // toolbarAdd()

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/menuBigIconDeactivated.png")
    ImageResource menuBigIconDeactivated(); // toolbarAddSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/moveBigIconActive.png")
    ImageResource moveBigIconActive(); // toolbarMove();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/moveBigIconDeactivated.png")
    ImageResource moveBigIconDeactivated(); // toolbarMoveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/propertyBigIconActive.png")
    ImageResource propertyBigIconActive(); //  toolbarProperties();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/propertyBigIconDeactivated.png")
    ImageResource propertyBigIconDeactivated(); //  toolbarPropertiesSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/publishBigIconActive.png")
    ImageResource publishBigIconActive(); // toolbarPublish();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/publishBigIconDeactivated.png")
    ImageResource publishBigIconDeactivated(); //  toolbarPublishSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/removeBigIconActive.png")
    ImageResource removeBigIconActive(); // toolbarRemove();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/removeBigIconDeactivated.png")
    ImageResource removeBigIconDeactivated(); // toolbarRemoveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/resetBigIconActive.png")
    ImageResource resetBigIconActive(); // toolbarReset();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/resetBigIconDeactivated.png")
    ImageResource resetBigIconDeactivated(); //  toolbarResetSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/saveBigIconActive.png")
    ImageResource saveBigIconActive(); // toolbarSave();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/saveBigIconDeactivated.png")
    ImageResource saveBigIconDeactivated(); // toolbarSaveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/searchBigIconActive.png")
    ImageResource searchBigIconActive(); // toolbarAdd()

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/searchBigIconDeactivated.png")
    ImageResource searchBigIconDeactivated(); // toolbarAddSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/selectionBigIconActive.png")
    ImageResource selectionBigIconActive(); // toolbarSelection();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/selectionBigIconDeactivated.png")
    ImageResource selectionBigIconDeactivated(); // toolbarSelectionSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/sitemapBigIconActive.png")
    ImageResource sitemapBigIconActive(); // toolbarSitemap();;

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/sitemapBigIconDeactivated.png")
    ImageResource sitemapBigIconDeactivated(); // toolbarSitemapSW();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/icons/big/stateExportBigIcon.png")
    ImageResource stateExportBigIcon();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/icons/big/stateHiddenBigIcon.png")
    ImageResource stateHiddenBigIcon();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/icons/big/stateNormalBigIcon.png")
    ImageResource stateNormalBigIcon();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/icons/big/stateRedirectBigIcon.png")
    ImageResource stateRedirectBigIcon();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/icons/big/stateSecureBigIcon.png")
    ImageResource stateSecureBigIcon();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/undoBigIconActive.png")
    ImageResource undoBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/undoBigIconDeactivated.png")
    ImageResource undoBigIconDeactivated();
}