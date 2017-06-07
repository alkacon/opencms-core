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
import org.opencms.gwt.shared.CmsHistoryVersion;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to display a file version in the history dialog.<p>
 */
public final class CmsVersionCell extends AbstractCell<CmsHistoryResourceBean> {

    /**
     * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
     */
    @Override
    public void render(
        com.google.gwt.cell.client.Cell.Context context,
        CmsHistoryResourceBean value,
        SafeHtmlBuilder sb) {

        CmsHistoryVersion version = value.getVersion();
        Integer versionNumber = version.getVersionNumber();

        String versionText = "";
        String versionTitle = "";

        if (versionNumber != null) {
            versionText = "" + versionNumber + (version.isOnline() ? "*" : "");
        } else if (version.isOffline()) {
            versionText = "--";
        } else if (version.isOnline()) {
            versionText = "*";
        }

        if (version.isOffline()) {
            versionTitle = CmsHistoryMessages.offline();
        } else if (version.isOnline()) {
            versionTitle = CmsHistoryMessages.online();
        }
        sb.append(CmsResourceHistoryTable.templates.textSpanWithTitle(versionText, versionTitle));
    }
}