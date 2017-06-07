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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EntryType;
import org.opencms.gwt.client.util.CmsScriptCallbackHelper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Menu entry for the "copy page" command.<p>
 */
public class CmsCopyPageMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Creates a new instance.<p>
     *
     * @param hoverbar the hoverbar for the current item
     */
    public CmsCopyPageMenuEntry(CmsSitemapHoverbar hoverbar) {
        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_COPYPAGE_MENU_ENTRY_0));
        setActive(true);

    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    @Override
    public void execute() {

        CmsUUID id = getHoverbar().getEntry().getId();
        CmsScriptCallbackHelper callback = new CmsScriptCallbackHelper() {

            @Override
            public void run() {

                JsArrayString args = m_arguments.cast();
                String ids = args.get(0);
                if (ids.length() > 0) {
                    List<String> idList = CmsStringUtil.splitAsList(ids, "|");
                    CmsSitemapController controller = CmsSitemapView.getInstance().getController();
                    for (String idFromList : idList) {

                        if (null != controller.getEntryById(new CmsUUID(idFromList))) {
                            controller.updateEntry(new CmsUUID(idFromList));
                        }
                    }
                }
            }
        };
        openPageCopyDialog("" + id, callback.createCallback());
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        boolean show = getHoverbar().getController().isEditable()
            && CmsSitemapView.getInstance().isNavigationMode()
            && (entry != null)
            && ((entry.getEntryType() == EntryType.folder) || (entry.getEntryType() == EntryType.leaf));
        setVisible(show);
    }

    /**
     * Opens the page copy dialog for the given structure id.<p>
     *
     * @param id the structure id of the selected sitemap entry
     * @param callback the callback to call when the dialog has finished
     */
    public native void openPageCopyDialog(String id, JavaScriptObject callback) /*-{
                                                                                $wnd.cmsOpenPageCopyDialog(id, callback);
                                                                                }-*/;

}
