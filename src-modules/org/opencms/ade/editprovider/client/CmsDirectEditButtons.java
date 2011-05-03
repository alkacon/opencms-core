/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/editprovider/client/Attic/CmsDirectEditButtons.java,v $
 * Date   : $Date: 2011/05/03 10:48:48 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.editprovider.client;

import org.opencms.gwt.client.ui.A_CmsDirectEditButtons;
import org.opencms.gwt.client.ui.CmsDeleteWarningDialog;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

/**
 * Direct edit buttons for the Toolbar direct edit provider.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsDirectEditButtons extends A_CmsDirectEditButtons implements I_CmsContentEditorHandler {

    /** 
     * Creates a new instance.<p>
     * 
     * @param editable the editable element 
     * @param parentId the parent id 
     */
    public CmsDirectEditButtons(Element editable, String parentId) {

        super(editable, parentId);

    }

    /**
     * @see org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler#onClose(java.lang.String, boolean)
     */
    public void onClose(String sitePath, boolean isNew) {

        Window.Location.reload();
    }

    /**
     * Sets the position. Make sure the widget is attached to the DOM.<p>
     * 
     * @param position the absolute position
     * @param buttonsPosition the corrected position for the buttons 
     * 
     * @param containerElement the parent container element
     */
    public void setPosition(
        CmsPositionBean position,
        CmsPositionBean buttonsPosition,
        com.google.gwt.user.client.Element containerElement) {

        m_position = position;
        Element parent = CmsDomUtil.getPositioningParent(getElement());
        Style style = getElement().getStyle();
        style.setRight(
            parent.getOffsetWidth()
                - (buttonsPosition.getLeft() + buttonsPosition.getWidth() - parent.getAbsoluteLeft()),
            Unit.PX);
        int top = buttonsPosition.getTop() - parent.getAbsoluteTop();
        if (top < 0) {
            top = 0;
        }
        style.setTop(top, Unit.PX);
    }

    /**
     * Delete the editable element from page and VFS.<p>
     */
    protected void deleteElement() {

        //CmsContainerpageController.get().deleteElement(m_editableData.getStructureId(), m_parentResourceId);
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickDelete()
     */
    @Override
    protected void onClickDelete() {

        CmsDeleteWarningDialog deleteDialog = new CmsDeleteWarningDialog(m_editableData.getSitePath()) {

            @Override
            protected void onAfterDeletion() {

                Window.Location.reload();
            }
        };
        deleteDialog.center();
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickEdit()
     */
    @Override
    protected void onClickEdit() {

        openEditDialog(false);
        removeHighlighting();
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickNew()
     */
    @Override
    protected void onClickNew() {

        openEditDialog(true);
        removeHighlighting();
    }

    /**
     * Opens the content editor.<p>
     * 
     * @param isNew <code>true</code> to create and edit a new resource
     */
    protected void openEditDialog(boolean isNew) {

        String sitePath = m_editableData.getSitePath();
        if (isNew) {
            sitePath = sitePath + "&amp;newlink=" + URL.encodeQueryString(m_editableData.getNewLink());
        }
        CmsContentEditorDialog.get().openEditDialog(new CmsUUID(m_editableData.getStructureId()), sitePath, isNew, this);
    }

}
