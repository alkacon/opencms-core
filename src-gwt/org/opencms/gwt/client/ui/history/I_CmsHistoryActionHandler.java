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

package org.opencms.gwt.client.ui.history;

import org.opencms.gwt.shared.CmsHistoryResourceBean;

/**
 * Interface for classes which implement the actions that can be triggered from the history dialog.<p>
 *
 */
public interface I_CmsHistoryActionHandler {

    /**
     * Reverts a resource to a given historical state.<p>
     *
     * @param historyRes the historical resource to which the resource should be reverted
     */
    void revert(CmsHistoryResourceBean historyRes);

    /**
     * Sets the action which should be executed after reverting the resource.<p>
     *
     * @param action the action
     */
    void setPostRevertAction(Runnable action);

    /**
     * Show the preview for a given historical resource.<p>
     *
     * @param historyRes the history resource
     */
    void showPreview(CmsHistoryResourceBean historyRes);

}
