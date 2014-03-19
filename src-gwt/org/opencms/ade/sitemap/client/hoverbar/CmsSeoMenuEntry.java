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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.edit.CmsEditEntryHandler;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.seo.CmsSeoOptionsDialog;
import org.opencms.gwt.client.seo.Messages;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.alias.CmsAliasBean;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The context menu entry used for opening the "alias editor" dialog.<p>
 */
public class CmsSeoMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsSeoMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        //setImageClass(I_CmsImageBundle.INSTANCE.contextMenuIcons().gotoPage());
        setLabel(org.opencms.gwt.client.seo.Messages.get().key(Messages.GUI_SEO_OPTIONS_0));
        setActive(true);
        setVisible(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        CmsSitemapView view = CmsSitemapView.getInstance();
        final CmsSitemapController controller = view.getController();
        String sitePath = getHoverbar().getEntry().getSitePath();
        CmsClientSitemapEntry entry = controller.getEntry(sitePath);
        CmsUUID structureId = entry.getDefaultFileId();
        if (structureId == null) {
            structureId = entry.getId();
        }
        // use another final variable so that we can use it in the callback
        final CmsUUID constStructureId = structureId;
        CmsRpcAction<CmsListInfoBean> infoAction = new CmsRpcAction<CmsListInfoBean>() {

            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().getPageInfo(constStructureId, this);
            }

            @Override
            protected void onResponse(final CmsListInfoBean listInfoBean) {

                stop(false);
                CmsSeoOptionsDialog.loadAliases(constStructureId, new AsyncCallback<List<CmsAliasBean>>() {

                    public void onFailure(Throwable caught) {

                        // do nothing
                    }

                    public void onSuccess(List<CmsAliasBean> result) {

                        CmsEditEntryHandler handler = new CmsEditEntryHandler(
                            controller,
                            getHoverbar().getEntry(),
                            CmsSitemapView.getInstance().isNavigationMode());
                        handler.setPageInfo(listInfoBean);
                        Map<String, CmsXmlContentProperty> propConfig = CmsSitemapView.getInstance().getController().getData().getProperties();
                        CmsSeoOptionsDialog dialog = new CmsSeoOptionsDialog(
                            constStructureId,
                            listInfoBean,
                            result,
                            propConfig,
                            handler);
                        dialog.center();
                    }

                });

            }

        };
        infoAction.execute();

    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
     */
    @Override
    public void onShow(CmsHoverbarShowEvent event) {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        boolean show = !CmsSitemapView.getInstance().isGalleryMode() && (entry != null) && entry.isEditable();
        setVisible(show);
    }
}
