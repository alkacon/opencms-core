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

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsEditProperties;
import org.opencms.gwt.client.ui.contextmenu.CmsEditProperties.PropertyEditingContext;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.util.CmsUUID;

import java.util.Map;

/**
 * Menu entry for changing categories.<p>
 */
public class CmsChangeCategoryMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsChangeCategoryMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_CONTEXTMENU_EDIT_CATEGORY_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        CmsUUID id = getHoverbar().getId();
        PropertyEditingContext context = new PropertyEditingContext();
        I_CmsContextMenuHandler handler = new I_CmsContextMenuHandler() {

            // this context menu handler only implements the refreshResource method

            public void ensureLockOnResource(CmsUUID structureId, I_CmsSimpleCallback<Boolean> callback) {
                // do nothing
            }

            public Map<String, I_CmsContextMenuCommand> getContextMenuCommands() {

                // do nothing
                return null;
            }

            public String getContextType() {

                return null;
            }

            public I_CmsContentEditorHandler getEditorHandler() {

                // do nothing
                return null;
            }

            public void leavePage(String targetUri) {
                // do nothing
            }

            public void onSiteOrProjectChange(String sitePath, String serverLink) {
                // do nothing
            }

            public void refreshResource(CmsUUID structureId) {

                getHoverbar().getController().loadCategories(true);
            }

            public void unlockResource(CmsUUID structureId) {
                // do nothing
            }
        };
        CmsEditProperties.editProperties(id, handler, true, null, false, context);
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        boolean visible = getHoverbar().getController().isEditable()
            && (getHoverbar().getId() != null)
            && !getHoverbar().getId().isNullUUID();
        setVisible(visible);
    }

}
