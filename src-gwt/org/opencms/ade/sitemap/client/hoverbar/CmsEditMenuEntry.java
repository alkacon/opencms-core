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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.edit.CmsEditEntryHandler;
import org.opencms.ade.sitemap.client.edit.CmsNavModePropertyEditor;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.A_CmsPropertyEditor;
import org.opencms.gwt.client.property.CmsVfsModePropertyEditor;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;

/**
 * Sitemap context menu edit entry.<p>
 * 
 * @since 8.0.0
 */
public class CmsEditMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsEditMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setImageClass(I_CmsImageBundle.INSTANCE.contextMenuIcons().properties());
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_EDIT_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        final CmsSitemapController controller = getHoverbar().getController();
        final CmsClientSitemapEntry entry = getHoverbar().getEntry();

        final CmsUUID infoId;

        if ((entry.getDefaultFileId() != null) && CmsSitemapView.getInstance().isNavigationMode()) {
            infoId = entry.getDefaultFileId();
        } else {
            infoId = entry.getId();
        }
        CmsRpcAction<CmsListInfoBean> action = new CmsRpcAction<CmsListInfoBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getPageInfo(infoId, this);
            }

            @Override
            protected void onResponse(CmsListInfoBean result) {

                stop(false);
                CmsEditEntryHandler handler = new CmsEditEntryHandler(
                    controller,
                    entry,
                    CmsSitemapView.getInstance().isNavigationMode());
                handler.setPageInfo(result);
                A_CmsPropertyEditor editor = createEntryEditor(handler);
                editor.setPropertyNames(CmsSitemapView.getInstance().getController().getData().getAllPropertyNames());
                editor.start();
                String noEditReason = controller.getNoEditReason(entry);
                if (noEditReason != null) {
                    editor.disableInput(noEditReason);
                }
            }

        };
        action.execute();
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
     */
    @Override
    public void onShow(CmsHoverbarShowEvent event) {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        boolean show = (entry != null);
        setVisible(show);
    }

    /**
     * Creates the right sitemap entry editor for the current mode.<p>
     * 
     * @param handler the entry editor handler 
     * 
     * @return a sitemap entry editor instance 
     */
    protected A_CmsPropertyEditor createEntryEditor(I_CmsPropertyEditorHandler handler) {

        Map<String, CmsXmlContentProperty> propConfig = CmsSitemapView.getInstance().getController().getData().getProperties();

        if (CmsSitemapView.getInstance().isNavigationMode()) {
            return new CmsNavModePropertyEditor(propConfig, handler);
        } else {
            boolean isFolder = handler.isFolder();
            CmsVfsModePropertyEditor result = new CmsVfsModePropertyEditor(propConfig, handler);
            result.setShowResourceProperties(!isFolder);
            return result;
        }
    }
}
