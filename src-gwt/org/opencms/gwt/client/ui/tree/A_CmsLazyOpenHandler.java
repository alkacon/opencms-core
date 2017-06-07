/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.tree;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.user.client.Timer;

/**
 * Lazy list tree open handler abstract implementation.<p>
 *
 * @param <I> the specific lazy tree item implementation
 *
 * @since 8.0.0
 *
 * @see org.opencms.gwt.client.ui.tree.CmsLazyTree
 * @see org.opencms.gwt.client.ui.tree.CmsLazyTreeItem
 * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler
 */
public abstract class A_CmsLazyOpenHandler<I extends CmsLazyTreeItem> implements I_CmsLazyOpenHandler<I> {

    /**
     * Timer for showing the loading message.<p>
     */
    private final class OpenTimer extends Timer {

        /** The tree item to open. */
        private I m_target;

        /**
         * Default constructor.<p>
         *
         * @param target the tree item to open
         */
        public OpenTimer(I target) {

            m_target = target;
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            m_target.setOpen(true, false);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#onOpen(com.google.gwt.event.logical.shared.OpenEvent)
     */
    public void onOpen(OpenEvent<I> event) {

        I target = event.getTarget();
        if (target.getLoadState() != CmsLazyTreeItem.LoadState.UNLOADED) {
            return;
        }
        new OpenTimer(target).schedule(500);
        target.onStartLoading();
        target.setOpen(false, false);
        load(target);
    }
}
