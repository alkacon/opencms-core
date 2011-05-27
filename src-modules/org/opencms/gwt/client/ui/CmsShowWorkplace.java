/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsShowWorkplace.java,v $
 * Date   : $Date: 2011/05/27 14:51:46 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.Window;

/**
 * Provides a method to open the workplace.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsShowWorkplace {

    /** The uri to open in the workplace. */
    protected CmsUUID m_structureId;

    /**
     * Public constructor.<p>
     * 
     * @param structureId the structure id of the resource for which the workplace should be opened 
     */
    public CmsShowWorkplace(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Opens the workplace.<p>
     */
    public void openWorkplace() {

        CmsRpcAction<String> callback = new CmsRpcAction<String>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                CmsCoreProvider.getService().getWorkplaceLink(m_structureId, this);
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
    protected final native void openWorkplace(String path, int winWidth, int winHeight, int winLeft, int winTop) /*-{

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
         deWindow.focus();
      }
    }-*/;
}
