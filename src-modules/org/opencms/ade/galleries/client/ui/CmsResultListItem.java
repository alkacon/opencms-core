/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultListItem.java,v $
 * Date   : $Date: 2010/09/30 13:32:25 $
 * Version: $Revision: 1.9 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the specific list item for the results list.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 8.0.
 */
public class CmsResultListItem extends CmsListItem {

    /** The preview button. */
    private CmsPushButton m_previewButton;

    /** The resource type name of the resource. */
    private String m_resourceType;

    /** The vfs path. */
    private String m_vfsPath;

    /** The name. */
    private String m_name;

    /**
     * Returns the vfs path.<p>
     *
     * @return the vfs path
     */
    public String getVfsPath() {

        return m_vfsPath;
    }

    /** The select button. */
    private CmsPushButton m_selectButton;

    /**
     * Creates a new result list item with a main widget and a check box.<p>
     * 
     * @param checkbox the check box 
     * @param mainWidget the main widget 
     */
    public CmsResultListItem(CmsCheckBox checkbox, Widget mainWidget) {

        initContent(checkbox, mainWidget);
    }

    /**
     * Creates a new result list item with a main widget.<p>
     * 
     * @param resultItem the result item
     * @param dndHandler the drag and drop handler
     */
    public CmsResultListItem(CmsResultItemBean resultItem, CmsDNDHandler dndHandler) {

        CmsListItemWidget resultItemWidget;
        CmsListInfoBean infoBean = new CmsListInfoBean(resultItem.getTitle(), resultItem.getDescription(), null);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resultItem.getExcerpt())) {
            infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_RESULT_LABEL_EXCERPT_0), resultItem.getExcerpt());
        }
        m_vfsPath = resultItem.getPath();
        resultItemWidget = new CmsResultItemWidget(infoBean, resultItem.getType(), resultItem.getPath());
        initContent(resultItemWidget);
        if (dndHandler != null) {
            setId(resultItem.getClientId());
            if (resultItem.getTitle() != null) {
                setName(resultItem.getTitle().toLowerCase().replace("/", "-").replace(" ", "_"));
            } else {
                // TODO: check if another name makes more sense
                setName(resultItem.getClientId());
            }
            initMoveHandle(dndHandler);
        } else {
            if (((CmsResultItemWidget)resultItemWidget).hasTileView()) {
                addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingItem());
            }
        }
        // add  preview button
        m_previewButton = new CmsPushButton();
        m_previewButton.setImageClass(I_CmsImageBundle.INSTANCE.style().magnifierIcon());
        m_previewButton.setShowBorder(false);
        m_previewButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        resultItemWidget.addButton(m_previewButton);
        m_selectButton = new CmsPushButton();
        // TODO: use different icon
        m_selectButton.setImageClass(I_CmsImageBundle.INSTANCE.style().newIcon());
        m_selectButton.setShowBorder(false);
        m_selectButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        m_selectButton.setVisible(false);
        resultItemWidget.addButton(m_selectButton);

        // add file icon
        resultItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(resultItem.getType(), resultItem.getPath(), false));

    }

    /**
     * Adds the preview button click handler.<p>
     * 
     * @param handler the click handler
     */
    public void addPreviewClickHandler(ClickHandler handler) {

        m_previewButton.addClickHandler(handler);
    }

    /**
     * Adds the select button click handler.<p>
     * 
     * @param handler the click handler
     */
    public void addSelectClickHandler(ClickHandler handler) {

        m_selectButton.setVisible(true);
        m_selectButton.addClickHandler(handler);
    }

    /**
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Sets the resource type name.<p>
     *
     * @param resourceType the resource type name to set
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }
}