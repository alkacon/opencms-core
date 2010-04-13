/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleriesVfs.java,v $
 * Date   : $Date: 2010/04/13 09:17:19 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.util.CmsGalleryProvider;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsFlowPanel;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStringUtil;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Gallery Dialog entry class to be open from the vfs tree.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 */
public class CmsGalleriesVfs extends A_CmsEntryPoint {

    /** The gallery service instance. */
    private I_CmsGalleryServiceAsync m_gallerySvc;

    /**
     * Ensures all style sheets are loaded.<p>
     */
    public static void initCss() {

        I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listTreeCss().ensureInjected();
        org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
    }

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        initCss();
        final CmsFlowPanel html = new CmsFlowPanel(CmsDomUtil.Tag.div.name());
        html.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().galleryDialogSize());
        RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().galleriesDialog());
        RootPanel.get().add(html);

        // set the search object
        // TODO: replace the dummy data with infos from openvfsgallery.jsp

        final CmsGallerySearchObject searchObj = new CmsGallerySearchObject();
        // if gallery is selected write the gallery path to the gallery list
        ArrayList<String> galleries = new ArrayList<String>();
        // TODO: replace string params through JSON string
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(CmsGalleryProvider.get().getGalleryPath())) {
            galleries.add(CmsGalleryProvider.get().getGalleryPath());

        }
        searchObj.setGalleries(galleries);
        searchObj.setSortOrder("");
        searchObj.setTabId(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_results.name());
        searchObj.setMachesPerPage(12);

        // set tabs config
        // TODO: why do this method throw ClassNotCast Exeption in host mode??
        // ANSWER: because it just did not work, but now it works ;)
        // String[] tabs = splitAsArray(CmsGalleryProvider.get().getTabs(), ",");
        String[] tabs = {
            I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name(),
            I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name(),
            I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name(),
            I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name()};
        final ArrayList<String> tabsConfig = new ArrayList<String>();
        for (int i = 0; tabs.length > i; i++) {
            tabsConfig.add(tabs[i]);
        }

        final String dialogMode = "view";

        CmsRpcAction<CmsGalleryInfoBean> getInitialAction = new CmsRpcAction<CmsGalleryInfoBean>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                getGalleryService().getInitialSettings(tabsConfig, searchObj, dialogMode, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsGalleryInfoBean infoBean) {

                CmsGalleryDialog galleryDialog = new CmsGalleryDialog(infoBean);
                html.add(galleryDialog);
            }
        };
        getInitialAction.execute();
    }

    /**
     * Returns the gallery service instance.<p>
     * 
     * @return the gallery service instance
     */
    protected I_CmsGalleryServiceAsync getGalleryService() {

        if (m_gallerySvc == null) {
            m_gallerySvc = GWT.create(I_CmsGalleryService.class);
        }
        return m_gallerySvc;
    }
}
