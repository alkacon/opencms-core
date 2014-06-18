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

package org.opencms.ade.editprovider.client;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.A_CmsDirectEditButtons;
import org.opencms.gwt.client.ui.CmsDeleteWarningDialog;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Method;
import org.opencms.gwt.client.util.CmsDomUtil.Target;
import org.opencms.gwt.client.util.CmsPositionBean;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;

/**
 * Direct edit buttons for the Toolbar direct edit provider.<p>
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
    public void setPosition(CmsPositionBean position, CmsPositionBean buttonsPosition, Element containerElement) {

        m_position = position;
        Element parent = CmsDomUtil.getPositioningParent(getElement());
        Style style = getElement().getStyle();
        style.setRight(
            parent.getOffsetWidth()
                - ((buttonsPosition.getLeft() + buttonsPosition.getWidth()) - parent.getAbsoluteLeft()),
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

        // create a form to submit a post request to the editor JSP
        Map<String, String> formValues = new HashMap<String, String>();
        if (m_editableData.getSitePath() != null) {
            formValues.put("resource", m_editableData.getSitePath());
        }
        if (m_editableData.getElementLanguage() != null) {
            formValues.put("elementlanguage", m_editableData.getElementLanguage());
        }
        if (m_editableData.getElementName() != null) {
            formValues.put("elementname", m_editableData.getElementName());
        }
        String backlink = CmsCoreProvider.get().getUri();
        if (Window.Location.getPath().endsWith(backlink)) {
            // CmsCoreProvider.get().getUri() is the request context uri from the time the direct edit provider
            // includes are generated. In case the template has changed the request context uri before that point, 
            // we don't append the request parameters, as they may be inappropriate for the new URI. 
            backlink += Window.Location.getQueryString();
        }
        formValues.put("backlink", backlink);
        formValues.put("redirect", "true");
        formValues.put("directedit", "true");
        formValues.put("editcontext", CmsCoreProvider.get().getUri());
        if (isNew) {
            formValues.put("newlink", m_editableData.getNewLink());
            formValues.put("editortitle", m_editableData.getNewTitle());
        }
        FormElement formElement = CmsDomUtil.generateHiddenForm(
            CmsCoreProvider.get().link(CmsCoreProvider.get().getContentEditorUrl()),
            Method.post,
            Target.TOP,
            formValues);
        getMarkerTag().appendChild(formElement);
        formElement.submit();
    }
}
