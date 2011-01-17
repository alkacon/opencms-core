/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsBigIconBundle.java,v $
 * Date   : $Date: 2011/01/17 16:16:08 $
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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Image bundle for big icons.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsBigIconBundle extends ClientBundle {

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/addBigIconActive.png")
    ImageResource addBigIconActive(); // toolbarAdd()

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/addBigIconDeactivated.png")
    ImageResource addBigIconDeactivated(); // toolbarAddSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/bigicons/brokenLinkBigIcon.png")
    ImageResource brokenLinkBigIcon();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/bigicons/contextMenuBigIconActive.png")
    ImageResource contextMenuBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/contextMenuBigIconDeactivated.png")
    ImageResource contextMenuBigIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/contextMenuDownBigIcon.png")
    ImageResource contextMenuDownBigIcon();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/deleteBigIconActive.png")
    ImageResource deleteBigIconActive(); // toolbarRemove();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/deleteBigIconDeactivated.png")
    ImageResource deleteBigIconDeactivated(); // toolbarRemoveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/editBigIconActive.png")
    ImageResource editBigIconActive(); // toolbarEdit();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/editBigIconDeactivated.png")
    ImageResource editBigIconDeactivated(); // toolbarEditSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/exitBigIconActive.png")
    ImageResource exitBigIconActive(); // toolbarExit();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/exitBigIconDeactivated.png")
    ImageResource exitBigIconDeactivated(); // toolbarExitSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/favoriteBigIconActive.png")
    ImageResource favoriteBigIconActive(); // toolbarClipboard();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/favoriteBigIconDeactivated.png")
    ImageResource favoriteBigIconDeactivated(); // toolbarClipboardSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/gotoBigIconDeactivated.png")
    ImageResource gotoBigIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/gotoPageBigIconActive.png")
    ImageResource gotoPageBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/bigicons/gotoParentBigIconActive.png")
    ImageResource gotoParentBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/bigicons/gotoParentBigIconDeactivated.png")
    ImageResource gotoParentBigIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */

    @Source("images/bigicons/gotoSubSitemapBigIconActive.png")
    ImageResource gotoSubSitemapBigIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/bigicons/magnifierBigIconActive.png")
    ImageResource magnifierBigIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/bigicons/magnifierBigIconDeactivated.png")
    ImageResource magnifierBigIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/moveBigIconActive.png")
    ImageResource moveBigIconActive(); // toolbarMove();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/moveBigIconDeactivated.png")
    ImageResource moveBigIconDeactivated(); // toolbarMoveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/newBigIconActive.png")
    ImageResource newBigIconActive(); // toolbarNew();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/newBigIconDeactivated.png")
    ImageResource newBigIconDeactivated(); // toolbarNewSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/propertyBigIconActive.png")
    ImageResource propertyBigIconActive(); //  toolbarProperties();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/propertyBigIconDeactivated.png")
    ImageResource propertyBigIconDeactivated(); //  toolbarPropertiesSW();

    /**
    @Source("images/bigicons/recentBigIconActive.png")
    ImageResource recentBigIconActive(); // toolbarRecent();

    @Source("images/bigicons/recentBigIconDeactivated.png")
    ImageResource recentBigIconDeactivated(); // toolbarRecentSW();
    
    **/

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/publishBigIconActive.png")
    ImageResource publishBigIconActive(); // toolbarPublish();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/publishBigIconDeactivated.png")
    ImageResource publishBigIconDeactivated(); //  toolbarPublishSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/redoBigIconActive.png")
    ImageResource redoBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/redoBigIconDeactivated.png")
    ImageResource redoBigIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/resetBigIconActive.png")
    ImageResource resetBigIconActive(); // toolbarReset();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/resetBigIconDeactivated.png")
    ImageResource resetBigIconDeactivated(); //  toolbarResetSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/saveBigIconActive.png")
    ImageResource saveBigIconActive(); // toolbarSave();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/saveBigIconDeactivated.png")
    ImageResource saveBigIconDeactivated(); // toolbarSaveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/selectionBigIconActive.png")
    ImageResource selectionBigIconActive(); // toolbarSelection();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/selectionBigIconDeactivated.png")
    ImageResource selectionBigIconDeactivated(); // toolbarSelectionSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/sitemapBigIconActive.png")
    ImageResource sitemapBigIconActive(); // toolbarSitemap();;

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/sitemapBigIconDeactivated.png")
    ImageResource sitemapBigIconDeactivated(); // toolbarSitemapSW();;

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/undoBigIconActive.png")
    ImageResource undoBigIconActive();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/bigicons/undoBigIconDeactivated.png")
    ImageResource undoBigIconDeactivated();

}
