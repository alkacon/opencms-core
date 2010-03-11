/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/lazytree/Attic/CmsLazyTree.java,v $
 * Date   : $Date: 2010/03/11 11:26:08 $
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

package org.opencms.gwt.client.ui.lazytree;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.user.client.ui.Tree;

/**
 * Lazy tree implementation.<p>
 * 
 * @param <I> the specific lazy tree item implementation 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.client.ui.lazytree.CmsLazyTreeItem
 */
public class CmsLazyTree<I extends CmsLazyTreeItem> extends Tree {

    /**
     * Constructs an empty tree.<p>
     * 
     * @param handler the open handler to use 
     */
    public CmsLazyTree(I_CmsLazyOpenHandler<I> handler) {

        super();
        addHandler(handler, OpenEvent.getType());
    }

    /**
     * Constructs a tree that uses the specified ClientBundle for images. If this
     * tree does not use leaf images, the width of the Resources's leaf image will
     * control the leaf indent.<p>
     * 
     * @param resources a bundle that provides tree specific images
     * @param useLeafImages use leaf images from bundle
     * @param handler the open handler to use 
     */
    public CmsLazyTree(Resources resources, boolean useLeafImages, I_CmsLazyOpenHandler<I> handler) {

        super(resources, useLeafImages);
        addHandler(handler, OpenEvent.getType());
    }

    /**
     * Constructs a tree that uses the specified ClientBundle for images.<p>
     * 
     * @param resources a bundle that provides tree specific images
     * @param handler the open handler to use 
     */
    public CmsLazyTree(Resources resources, I_CmsLazyOpenHandler<I> handler) {

        super(resources);
        addHandler(handler, OpenEvent.getType());
    }
}
