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
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerPageGalleryData;
import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.CmsGalleryControllerHandler;
import org.opencms.ade.galleries.client.I_CmsGalleryHandler;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.A_CmsToolbarMenu;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsToolbarPopup;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The gallery tool-bar menu.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarGalleryMenu extends A_CmsToolbarMenu<CmsContainerpageHandler> {

    /**
     * The gallery handler for the container page editor.<p>
     */
    class GalleryHandler implements I_CmsGalleryHandler {

        /** The drag and drop filter. */
        private Predicate<CmsResultItemBean> m_dndFilter;

        /**
         * Constructor.<p>
         *
         * @param dndFilter the drag and drop filter
         */
        GalleryHandler(Predicate<CmsResultItemBean> dndFilter) {

            m_dndFilter = dndFilter;
        }

        /**
         * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#filterDnd(org.opencms.ade.galleries.shared.CmsResultItemBean)
         */
        public boolean filterDnd(CmsResultItemBean resultBean) {

            if (m_dndFilter != null) {
                return m_dndFilter.apply(resultBean);
            } else {
                return true;
            }
        }

        /**
         * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#getAdditionalTypeTabControl()
         */
        public Widget getAdditionalTypeTabControl() {

            return CmsContainerpageController.get().getHandler().createViewSelectorForGalleryDialog();
        }

        /**
         * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#getAutoHideParent()
         */
        public I_CmsAutoHider getAutoHideParent() {

            return getPopup();
        }

        /**
         * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#getDndHandler()
         */
        public CmsDNDHandler getDndHandler() {

            return getDragHandler();
        }

        /**
         * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#processResultItem(org.opencms.ade.galleries.client.ui.CmsResultListItem)
         */
        public void processResultItem(CmsResultListItem item) {

            if (item.getResult().isCopyModel()) {
                item.getListItemWidget().setBackground(Background.YELLOW);
                CmsLabel titleWidget = item.getListItemWidget().getTitleWidget();
                titleWidget.getElement().getStyle().setFontStyle(FontStyle.OBLIQUE);
                String newText = Messages.get().key(Messages.GUI_COPY_MODEL_TITLE_WRAPPER_1, titleWidget.getText());
                titleWidget.setText(newText);
                item.getListItemWidget().setStateIcon(StateIcon.copy);
            }
        }

    }

    /** The gallery dialog instance. */
    private CmsGalleryDialog m_dialog;

    /** The drag and drop handler for the gallery menu. */
    private CmsDNDHandler m_dragHandler;

    /** The gallery data. */
    private CmsGalleryDataBean m_galleryData;

    /** The gallery search bean. */
    private CmsGallerySearchBean m_search;

    /** The gallery dialog container. */
    private SimplePanel m_tabsContainer;

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     * @param dragHandler the container-page drag handler
     */
    public CmsToolbarGalleryMenu(CmsContainerpageHandler handler, CmsDNDHandler dragHandler) {

        super(I_CmsButton.ButtonData.WAND_BUTTON, handler);
        m_dragHandler = dragHandler;
        FlowPanel contentPanel = new FlowPanel();
        m_tabsContainer = new SimplePanel();
        m_tabsContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuTabContainer());
        contentPanel.add(m_tabsContainer);
        setMenuWidget(contentPanel);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    @SuppressWarnings("unused")
    public void onToolbarActivate() {

        Document.get().getBody().addClassName(I_CmsButton.ButtonData.WAND_BUTTON.getIconClass());
        if (m_dialog == null) {
            int dialogHeight = CmsToolbarPopup.getAvailableHeight();
            int dialogWidth = CmsToolbarPopup.getAvailableWidth();
            Predicate<CmsResultItemBean> resultDndFilter = Predicates.alwaysTrue();
            if (CmsContainerpageController.get().getData().getTemplateContextInfo().getCurrentContext() != null) {
                resultDndFilter = new CmsTemplateContextResultDndFilter();
            }
            final Predicate<CmsResultItemBean> finalDndFilter = resultDndFilter;
            CmsGalleryDialog galleryDialog = new CmsGalleryDialog(new GalleryHandler(finalDndFilter));
            new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog), m_galleryData, m_search);
            m_dialog = galleryDialog;
            m_dialog.setDialogSize(dialogWidth, dialogHeight);
            getPopup().setWidth(dialogWidth);
            m_tabsContainer.add(m_dialog);
        } else {
            int dialogWidth = CmsToolbarPopup.getAvailableWidth();
            getPopup().setWidth(dialogWidth);
            m_dialog.truncate("GALLERY_DIALOG_TM", dialogWidth);
            m_dialog.updateSizes();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        Document.get().getBody().removeClassName(I_CmsButton.ButtonData.WAND_BUTTON.getIconClass());
    }

    /**
     * Updates the gallery data.<p>
     *
     * @param galleryData the gallery data
     * @param viewChanged <code>true</code> in case the element view changed
     */
    public void updateGalleryData(CmsContainerPageGalleryData galleryData, boolean viewChanged) {

        if (m_dialog != null) {
            if (viewChanged) {
                m_dialog.removeFromParent();
                m_dialog = null;
            } else {
                m_dialog.updateGalleryData(galleryData.getGalleryData());
            }
        }
        m_galleryData = galleryData.getGalleryData();
        m_search = galleryData.getGallerySearch();
    }

    /**
     * Gets the drag handler.<p>
     *
     * @return the drag handler
     */
    protected CmsDNDHandler getDragHandler() {

        return m_dragHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#getPopup()
     */
    @Override
    protected CmsPopup getPopup() {

        return super.getPopup();
    }

}