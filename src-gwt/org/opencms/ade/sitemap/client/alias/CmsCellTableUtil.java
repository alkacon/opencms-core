/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.alias;

import org.opencms.gwt.client.ui.css.I_CmsCellTableResources;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.Timer;

/**
 * Various static utility methods for dealing with cell tables.<p>
 */
public final class CmsCellTableUtil {

    /** CSS class for the error text. */
    public static final String STATUS_ERROR = I_CmsCellTableResources.INSTANCE.cellTableStyle().statusError();

    /** CSS class for the 'Status OK' text. */
    public static final String STATUS_OK = I_CmsCellTableResources.INSTANCE.cellTableStyle().statusOk();

    /**
     * Hidden constructor.<p>
     */
    private CmsCellTableUtil() {

        // nothing
    }

    /**
     * Ensures that surrounding scroll panels are notified when a table changes size.<p>
     *
     * @param table the table for which the parent scroll panels should be notified
     */
    public static void ensureCellTableParentResize(final CellTable<?> table) {

        // we need to update the scroll panel when the table is redrawn, but the redraw() method of the table is asynchronous,
        // i.e. it only schedules an actual redraw. However, the method which is responsible for the actual redrawing triggers a
        // loading state event before it does the redrawing. Using a timer at this point, we can execute code after the redrawing
        // has happend.
        table.addLoadingStateChangeHandler(new LoadingStateChangeEvent.Handler() {

            public void onLoadingStateChanged(LoadingStateChangeEvent event) {

                Timer resizeTimer = new Timer() {

                    @Override
                    public void run() {

                        CmsDomUtil.resizeAncestor(table);
                    }
                };
                resizeTimer.schedule(10);
            }
        });
    }

    /**
     * Formats the HTML for the error column of a cell table given an error message.<p>
     *
     * @param error the error message (null for no error)
     *
     * @return the SafeHtml representing the contents of the error cell
     */
    public static SafeHtml formatErrorHtml(String error) {

        String text;
        String cssClass;
        String title = "";

        if (error == null) {
            text = CmsAliasMessages.messageStatusOk();
            cssClass = STATUS_OK;
        } else {
            text = CmsAliasMessages.messageStatusError();
            title = SafeHtmlUtils.htmlEscape(error);
            cssClass = STATUS_ERROR;
        }
        String html = "<div class='" + cssClass + "' title='" + title + "'>" + text + "</div>";
        return SafeHtmlUtils.fromSafeConstant(html);
    }

}
