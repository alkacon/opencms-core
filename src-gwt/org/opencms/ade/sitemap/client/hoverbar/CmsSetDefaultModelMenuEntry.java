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
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsModelInfo;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.util.CmsUUID;

/**
 * Context menu entry for copying a model page.<p>
 */
public class CmsSetDefaultModelMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Creates a new model page menu entry.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsSetDefaultModelMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_SET_AS_DEFAULT_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        final CmsUUID id = entry.getId();
        CmsRpcAction<CmsModelInfo> action = new CmsRpcAction<CmsModelInfo>() {

            @Override
            public void execute() {

                start(0, true);
                getHoverbar().getController().getService().setDefaultModel(
                    getHoverbar().getController().getData().getRoot().getId(),
                    id,
                    this);
            }

            @Override
            protected void onResponse(CmsModelInfo result) {

                stop(false);
                CmsSitemapView.getInstance().displayModelPages(result);
                getHoverbar().getController().loadNewElementInfo(null);
            }

        };
        action.execute();
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        final CmsUUID id = entry.getId();
        boolean show = getHoverbar().getController().isEditable()
            && CmsSitemapView.getInstance().isModelPageMode()
            && (CmsSitemapView.getInstance().getModelPageEntry(id) != null)
            && !CmsSitemapView.getInstance().getModelPageEntry(id).isDefault();
        setVisible(show);
    }
}
