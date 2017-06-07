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
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.gwt.client.ui.CmsModelSelectDialog;
import org.opencms.gwt.client.ui.I_CmsModelSelectHandler;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

/**
 * Sitemap context menu new entry.<p>
 *
 * @since 8.0.0
 */
public class CmsNewChoiceMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsNewChoiceMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_NEW_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    @Override
    public void execute() {

        final CmsSitemapController controller = getHoverbar().getController();
        final CmsClientSitemapEntry entry = getHoverbar().getEntry();
        List<CmsNewResourceInfo> infos = controller.getData().getNewElementInfos();
        List<CmsModelResourceInfo> models = createModelInfos(infos);
        I_CmsModelSelectHandler handler = new I_CmsModelSelectHandler() {

            public void onModelSelect(CmsUUID modelStructureId) {

                controller.createSubEntry(entry, modelStructureId);
                //Window.alert("creating sub-entry " + modelStructureId);
            }
        };
        String title = Messages.get().key(Messages.GUI_SELECT_MODEL_TITLE_0);
        String message = Messages.get().key(Messages.GUI_SELECT_MODEL_MESSAGE_0);

        CmsModelSelectDialog dialog = new CmsModelSelectDialog(handler, models, title, message);
        dialog.center();
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        CmsSitemapController controller = getHoverbar().getController();
        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        boolean show = getHoverbar().getController().isEditable()
            && !CmsSitemapView.getInstance().isSpecialMode()
            && (entry != null)
            && entry.isEditable()
            && (controller.getData().getDefaultNewElementInfo() != null)
            && CmsSitemapView.getInstance().isNavigationMode()
            && entry.isInNavigation()
            && entry.isFolderType()
            && !entry.hasForeignFolderLock();
        setVisible(show);
    }

    /**
     * Helper method to create model resource info beans from new resource info beans.<p>
     *
     * @param resourceInfos the resource info beans
     *
     * @return the list of model resource info beans
     */
    protected List<CmsModelResourceInfo> createModelInfos(List<CmsNewResourceInfo> resourceInfos) {

        List<CmsModelResourceInfo> result = new ArrayList<CmsModelResourceInfo>();
        for (CmsNewResourceInfo resInfo : resourceInfos) {
            CmsModelResourceInfo model = new CmsModelResourceInfo();
            model.setTitle(resInfo.getTitle());
            model.setSubTitle(resInfo.getSubTitle());
            model.setStructureId(resInfo.getCopyResourceId());
            model.setResourceType(CmsGwtConstants.TYPE_CONTAINERPAGE);
            result.add(model);
        }
        return result;
    }
}
