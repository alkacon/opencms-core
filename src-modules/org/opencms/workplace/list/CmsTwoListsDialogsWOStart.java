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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

/**
 * A <code>{@link org.opencms.workplace.list.CmsTwoListsDialog}</code> with no starting html for previous widget dialog display.<p>
 *
 * @since 6.0.0
 */
public class CmsTwoListsDialogsWOStart extends CmsTwoListsDialog {

    /**
     * Default constructor.<p>
     *
     * @param wp1 the workplace instance for the first list
     * @param wp2 the workplace instance for the second list
     */
    public CmsTwoListsDialogsWOStart(A_CmsListDialog wp1, A_CmsListDialog wp2) {

        super(wp1, wp2);
    }

    /**
     * @see org.opencms.workplace.list.CmsTwoListsDialog#defaultActionHtmlStart()
     */
    @Override
    protected String defaultActionHtmlStart() {

        return getActiveWp().getList().listJs() + getActiveWp().dialogContentStart(getActiveWp().getParamTitle());
    }
}
