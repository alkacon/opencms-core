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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.client.ui.CmsGalleryPopup;
import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabbedPanelStyle;
import org.opencms.gwt.client.ui.I_CmsAutoHider;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.ui.Widget;

/**
 * Factory class to create gallery dialog with or without parameter.<p>
 *
 * @since 8.0.
 */
public final class CmsGalleryFactory {

    /**
     * Prevent instantiation.<p>
     */
    private CmsGalleryFactory() {

        // empty
    }

    /**
     * Returns a gallery dialog object.<p>
     *
     * @param popup the parent popup widget
     *
     * @return gallery dialog
     */
    @SuppressWarnings("unused")
    public static CmsGalleryDialog createDialog(final CmsPopup popup) {

        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(new I_CmsGalleryHandler() {

            public boolean filterDnd(CmsResultItemBean resultBean) {

                // TODO: Auto-generated method stub
                return true;
            }

            public Widget getAdditionalTypeTabControl() {

                return null;
            }

            public I_CmsAutoHider getAutoHideParent() {

                return popup;
            }

            public CmsDNDHandler getDndHandler() {

                return null;
            }

            public void processResultItem(CmsResultListItem item) {

                // do nothing
            }

        }, CmsTabbedPanelStyle.buttonTabs);
        try {
            CmsGalleryDataBean data = getGalleryDataFromDict();
            CmsGallerySearchBean search = getSearchBeanFromDict();
            new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog), data, search);
        } catch (SerializationException e) {
            CmsErrorDialog.handleException(
                new Exception(
                    "Deserialization of gallery data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                    e));
        }

        return galleryDialog;
    }

    /**
     * Creates a new gallery dialog.<p>
     *
     * @param galleryHandler the gallery handler
     * @param data the gallery data
     *
     * @return the gallery dialog instance
     */
    @SuppressWarnings("unused")
    public static CmsGalleryDialog createDialog(I_CmsGalleryHandler galleryHandler, CmsGalleryDataBean data) {

        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(galleryHandler);
        new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog), data, null);
        return galleryDialog;
    }

    /**
     * Creates a gallery widget pop-up.<p>
     *
     * @param handler the widget handler, used to set the widgets value
     * @param conf the gallery configuration
     *
     * @return the generated pop-up
     */
    public static CmsGalleryPopup createGalleryPopup(
        I_CmsGalleryWidgetHandler handler,
        I_CmsGalleryConfiguration conf) {

        return new CmsGalleryPopup(handler, conf);
    }

    /**
     * Deserializes the prefetched gallery data.<p>
     *
     * @return the gallery data
     *
     * @throws SerializationException in case deserialization fails
     */
    private static CmsGalleryDataBean getGalleryDataFromDict() throws SerializationException {

        return (CmsGalleryDataBean)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
            CmsGalleryController.getGalleryService(),
            CmsGalleryDataBean.DICT_NAME);
    }

    /**
     * Deserializes the prefetched gallery search.<p>
     *
     * @return the gallery data
     *
     * @throws SerializationException in case deserialization fails
     */
    private static CmsGallerySearchBean getSearchBeanFromDict() throws SerializationException {

        return (CmsGallerySearchBean)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
            CmsGalleryController.getGalleryService(),
            CmsGallerySearchBean.DICT_NAME);
    }
}