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

package org.opencms.ui.actions.prefillpage;

import org.opencms.ui.I_CmsDialogContext;

/**
 * Interface for prefill page handlers, callable via the context menu action {@link org.opencms.ui.actions.CmsPrefillPageAction}.
 * A handler is configured via the sitemap attribute "template.prefill.handler".
 */
public interface I_CmsPrefillPageHandler {

    /**
     * Execute the prefill action.
     * It is assumed that{@link I_CmsPrefillPageHandler#isExecutable(I_CmsDialogContext)} has yielded true for the provided context.
     *
     * @param context the context in which the action is performed.
     */
    void execute(I_CmsDialogContext context);

    /**
     * Checks if the action is executable in the provided context.
     * It already is checked, that we are in the page editor and have only one resource in the context (the containerpage).
     * @param context the context in which the action shall be performed.
     * @return <code>true</code> iff the action is executable in the provided context.
     */
    boolean isExecutable(I_CmsDialogContext context);
}
