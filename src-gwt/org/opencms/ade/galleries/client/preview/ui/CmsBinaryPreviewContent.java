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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.preview.CmsBinaryPreviewHandler;
import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.ade.galleries.client.ui.CmsResultsTab;
import org.opencms.ade.galleries.client.ui.CmsResultsTab.DeleteHandler;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.util.CmsDateTimeUtil;
import org.opencms.gwt.client.util.CmsDateTimeUtil.Format;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Widget to display resource informations within the resource preview.<p>
 *
 * @since 8.0.0
 */
public class CmsBinaryPreviewContent extends Composite {

    /**
     * The ui-binder interface.<p>
     */
    public interface I_CmsPreviewContentUiBinder extends UiBinder<HTMLPanel, CmsBinaryPreviewContent> {
        // GWT interface, nothing to do
    }

    /** The ui binder instance for this widget class. */
    private static I_CmsPreviewContentUiBinder uiBinder = GWT.create(I_CmsPreviewContentUiBinder.class);

    /** The preview handler. */
    protected CmsBinaryPreviewHandler m_binaryPreviewHandler;

    /** The list which only contains the list item for the previewed resource (required for drag and drop). */
    @UiField
    protected CmsList<CmsListItem> m_list;

    /** The preview content HTML. */
    @UiField
    protected HTML m_previewContent;

    /**
     * Constructor.<p>
     *
     * @param info the resource info to display
     * @param previewHandler the preview handler
     */
    public CmsBinaryPreviewContent(CmsResourceInfoBean info, CmsBinaryPreviewHandler previewHandler) {

        m_binaryPreviewHandler = previewHandler;
        CmsListItem listItem = createListItem(info, m_binaryPreviewHandler.getGalleryDialog().getDndHandler());
        initWidget(uiBinder.createAndBindUi(this));
        CmsUUID structureId = info.getStructureId();

        if (structureId != null) {
            listItem.setId(structureId.toString());
        }
        m_list.addItem(listItem);

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(info.getPreviewContent())) {
            m_previewContent.setHTML(info.getPreviewContent());
        }
    }

    /**
     * Creates the list item for the resource information bean.<p>
     *
     * @param resourceInfo the resource information bean
     * @param dndHandler the drag and drop handler
     *
     * @return the list item widget
     */
    private CmsListItem createListItem(CmsResourceInfoBean resourceInfo, CmsDNDHandler dndHandler) {

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(
            CmsStringUtil.isNotEmptyOrWhitespaceOnly(resourceInfo.getProperties().get(CmsClientProperty.PROPERTY_TITLE))
            ? resourceInfo.getProperties().get(CmsClientProperty.PROPERTY_TITLE)
            : resourceInfo.getTitle());
        infoBean.setSubTitle(resourceInfo.getResourcePath());
        infoBean.setResourceType(resourceInfo.getResourceType());
        infoBean.setDetailResourceType(resourceInfo.getDetailResourceType());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_PREVIEW_LABEL_SIZE_0), resourceInfo.getSize());
        if (resourceInfo.getDescription() != null) {
            infoBean.addAdditionalInfo(
                Messages.get().key(Messages.GUI_PREVIEW_LABEL_DESCRIPTION_0),
                resourceInfo.getDescription());
        }
        if (resourceInfo.getLastModified() != null) {
            infoBean.addAdditionalInfo(
                Messages.get().key(Messages.GUI_PREVIEW_LABEL_DATEMODIFIED_0),
                CmsDateTimeUtil.getDate(resourceInfo.getLastModified(), Format.MEDIUM));
        }
        CmsListItemWidget itemWidget = new CmsListItemWidget(infoBean);
        itemWidget.addOpenHandler(new OpenHandler<CmsListItemWidget>() {

            public void onOpen(OpenEvent<CmsListItemWidget> event) {

                int widgetHeight = event.getTarget().getOffsetHeight();
                m_previewContent.getElement().getStyle().setTop(12 + widgetHeight, Unit.PX);
            }
        });
        itemWidget.addCloseHandler(new CloseHandler<CmsListItemWidget>() {

            public void onClose(CloseEvent<CmsListItemWidget> event) {

                m_previewContent.getElement().getStyle().clearTop();
            }
        });
        CmsListItem result = new CmsListItem(itemWidget);

        CmsPushButton button = CmsResultListItem.createDeleteButton();
        if (dndHandler != null) {
            result.initMoveHandle(dndHandler);
        }
        CmsResultsTab resultsTab = m_binaryPreviewHandler.getGalleryDialog().getResultsTab();
        final DeleteHandler deleteHandler = resultsTab.makeDeleteHandler(resourceInfo.getResourcePath());
        ClickHandler handler = new ClickHandler() {

            public void onClick(ClickEvent event) {

                deleteHandler.onClick(event);
                m_binaryPreviewHandler.closePreview();
            }
        };
        button.addClickHandler(handler);
        itemWidget.addButton(button);
        return result;
    }
}
