/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultListItem.java,v $
 * Date   : $Date: 2011/03/11 09:12:05 $
 * Version: $Revision: 1.13 $
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
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.shared.CmsIconUtil;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the specific list item for the results list.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.13 $
 * 
 * @since 8.0.
 */
public class CmsResultListItem extends CmsListItem {

    /** The delete button. */
    private CmsPushButton m_deleteButton;

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

        CmsResultItemWidget resultItemWidget = new CmsResultItemWidget(
            resultItem,
            resultItem.getType(),
            resultItem.getPath());
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
            if (resultItemWidget.hasTileView()) {
                addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingItem());
            }
        }
        // add  preview button
        m_previewButton = new CmsPushButton();
        m_previewButton.setImageClass(I_CmsImageBundle.INSTANCE.style().magnifierIcon());
        m_previewButton.setShowBorder(false);
        m_previewButton.setTitle(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SHOW_0));
        m_previewButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        resultItemWidget.addButton(m_previewButton);
        m_selectButton = new CmsPushButton();
        // TODO: use different icon
        m_selectButton.setImageClass(I_CmsImageBundle.INSTANCE.style().newIcon());
        m_selectButton.setShowBorder(false);
        m_selectButton.setTitle(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SELECT_0));
        m_selectButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        m_selectButton.setVisible(false);
        resultItemWidget.addButton(m_selectButton);

        // add delete button
        m_deleteButton = new CmsPushButton();
        m_deleteButton.setImageClass(I_CmsImageBundle.INSTANCE.style().deleteIcon());
        m_deleteButton.setShowBorder(false);
        m_deleteButton.setTitle(Messages.get().key(Messages.GUI_RESULT_BUTTON_DELETE_0));
        m_deleteButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        if (!resultItem.isEditable()) {
            m_deleteButton.disable(resultItem.getNoEditReson());
        }
        resultItemWidget.addButton(m_deleteButton);
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
     * Adds the delete button click handler.<p>
     * 
     * @param handler the click handler
     */
    public void addDeleteClickHandler(ClickHandler handler) {

        m_deleteButton.addClickHandler(handler);
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
     * @see org.opencms.gwt.client.ui.CmsListItem#initMoveHandle(org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    @Override
    public boolean initMoveHandle(CmsDNDHandler dndHandler) {

        if (super.initMoveHandle(dndHandler)) {
            // move handle should always be visible
            getMoveHandle().getElement().addClassName(
                org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
            return true;
        }
        return false;
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