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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.CmsGalleryControllerHandler;
import org.opencms.ade.galleries.client.I_CmsGalleryHandler;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.ade.galleries.shared.CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.A_CmsToolbarMenu;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsToolbarPopup;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.shared.CmsGwtConstants;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The gallery tool-bar menu.<p>
 *
 * This is used to access all galleries in the system (including download and image galleries), but can not
 * be used to add elements to the container page.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarAllGalleriesMenu extends A_CmsToolbarMenu<CmsContainerpageHandler> {

    /** Marker string to distinguish search results from the 'all galleries' dialog from results from other instances of the gallery dialog. */
    public static final String DND_MARKER = "ALL_GALLERIES_DND_MARKER";

    /** The gallery service instance. */
    I_CmsGalleryServiceAsync m_galleryService = CmsGalleryController.createGalleryService();

    /** The main content widget. */
    private FlowPanel m_contentPanel;

    /** The gallery dialog instance. */
    private CmsGalleryDialog m_dialog;

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     * @param dragHandler the container-page drag handler
     */
    public CmsToolbarAllGalleriesMenu(CmsContainerpageHandler handler, CmsDNDHandler dragHandler) {

        super(I_CmsButton.ButtonData.GALLERY_BUTTON, handler);
        m_contentPanel = new FlowPanel();
        setMenuWidget(m_contentPanel);
    }

    /**
     * Creates a new gallery dialog and instantiates the controller for it.<p>
     *
     * @param configuration the gallery configuration
     * @param galleryHandler the gallery handler
     *
     * @return the gallery dialog instance
     */
    @SuppressWarnings("unused")
    protected static CmsGalleryDialog createDialog(
        I_CmsGalleryConfiguration configuration,
        I_CmsGalleryHandler galleryHandler) {

        CmsGalleryDialog result = new CmsGalleryDialog(galleryHandler);
        new CmsGalleryController(new CmsGalleryControllerHandler(result), configuration);
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        loadConfiguration();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        Document.get().getBody().removeClassName(I_CmsButton.ButtonData.WAND_BUTTON.getIconClass());
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#getPopup()
     */
    @Override
    protected CmsPopup getPopup() {

        return super.getPopup();
    }

    /**
     * Opens the dialog given the loaded gallery configuration.<p>
     *
     * @param configuration the gallery configuration
     */
    protected void openDialog(I_CmsGalleryConfiguration configuration) {

        Document.get().getBody().addClassName(I_CmsButton.ButtonData.WAND_BUTTON.getIconClass());
        if (m_dialog == null) {
            SimplePanel tabsContainer = new SimplePanel();
            tabsContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuTabContainer());
            int dialogHeight = CmsToolbarPopup.getAvailableHeight();
            int dialogWidth = CmsToolbarPopup.getAvailableWidth();
            m_dialog = createDialog(configuration, new I_CmsGalleryHandler() {

                public boolean filterDnd(CmsResultItemBean resultBean) {

                    return CmsGwtConstants.TYPE_IMAGE.equals(resultBean.getType());
                }

                public Widget getAdditionalTypeTabControl() {

                    return null;
                }

                public I_CmsAutoHider getAutoHideParent() {

                    return getPopup();
                }

                public CmsDNDHandler getDndHandler() {

                    return CmsContainerpageController.get().getDndHandler();
                }

                public void processResultItem(CmsResultListItem item) {

                    item.setData(DND_MARKER);
                    item.setDndHelperClass("imagedrag");
                    item.setDndParentClass(
                        "imageparent "
                            + org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingList());
                    //item.setDragHelperTransformer(DRAG_HELPER_TRANSFORMER);
                }

            });
            m_dialog.setDialogSize(dialogWidth, dialogHeight);
            getPopup().setWidth(dialogWidth);
            tabsContainer.add(m_dialog);
            m_contentPanel.add(tabsContainer);
        } else {
            int dialogWidth = CmsToolbarPopup.getAvailableWidth();
            getPopup().setWidth(dialogWidth);
            m_dialog.truncate("GALLERY_DIALOG_TM", dialogWidth);
            m_dialog.updateSizes();
        }
    }

    /**
     * Loads the configuration from the server.<p>
     */
    private void loadConfiguration() {

        CmsRpcAction<CmsGalleryConfiguration> action = new CmsRpcAction<CmsGalleryConfiguration>() {

            @Override
            public void execute() {

                start(200, false);
                m_galleryService.getAdeViewModeConfiguration(this);
            }

            @Override
            protected void onResponse(CmsGalleryConfiguration result) {

                stop(false);
                if (result.getReferencePath() == null) {
                    result.setReferencePath(CmsCoreProvider.get().getUri());
                }
                openDialog(result);

            }
        };
        if (m_dialog != null) {
            openDialog(null);
        } else {
            action.execute();
        }

    }

}