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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.Window;

/**
 * Provides a method to open the workplace.<p>
 * 
 * @since 8.0.0
 */
public final class CmsShowWorkplace implements I_CmsHasContextMenuCommand {

    /**
     * Hidden utility class constructor.<p>
     */
    private CmsShowWorkplace() {

        // nothing to do
    }

    /**
     * Returns the context menu command according to 
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     * 
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new I_CmsContextMenuCommand() {

            public void execute(CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

                openWorkplace(structureId);
            }

            public String getCommandIconClass() {

                return org.opencms.gwt.client.ui.css.I_CmsImageBundle.INSTANCE.contextMenuIcons().workplace();
            }
        };
    }

    /**
     * Opens the workplace.<p>
     * 
     * @param structureId the structure id of the resource for which the workplace should be opened 
     */
    protected static void openWorkplace(final CmsUUID structureId) {

        CmsRpcAction<String> callback = new CmsRpcAction<String>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                CmsCoreProvider.getService().getWorkplaceLink(structureId, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(String result) {

                int width = Window.getClientWidth();
                int height = Window.getClientHeight();
                int left = Window.getScrollLeft();
                int top = Window.getScrollTop();

                openWorkplace(result, width, height, left, top);
            }
        };
        callback.execute();
    }

    /**
     * Opens the workplace.<p>
     * 
     * @param path the workplace path to open
     * @param winWidth the width of the window
     * @param winHeight the height of the window
     * @param winLeft the left space of the window
     * @param winTop the top space of the window
     */
    protected static native void openWorkplace(String path, int winWidth, int winHeight, int winLeft, int winTop) /*-{

      if ($wnd.opener && $wnd.opener != self) {
         $wnd.opener.location.href = path;
         $wnd.opener.focus();
      } else {
         var openerStr = 'width='
               + winWidth
               + ',height='
               + winHeight
               + ',left='
               + winLeft
               + ',top='
               + winTop
               + ',scrollbars=no,location=no,toolbar=no,menubar=no,directories=no,status=yes,resizable=yes';
         var deWindow = $wnd.open(path, "DirectEditWorkplace", openerStr);
         if (deWindow) {
            deWindow.focus();
         }
      }
    }-*/;
}
