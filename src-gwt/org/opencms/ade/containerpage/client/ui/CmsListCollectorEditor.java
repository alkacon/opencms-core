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
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.A_CmsDirectEditButtons;
import org.opencms.gwt.client.ui.CmsCreateModeSelectionDialog;
import org.opencms.gwt.client.ui.CmsDeleteWarningDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Class to provide direct edit buttons within list collector elements.<p>
 *
 * @since 8.0.0
 */
public class CmsListCollectorEditor extends A_CmsDirectEditButtons {

    /** True if the parent element has offset height or width. */
    private boolean m_parentHasDimensions;

    /**
     * Creates a new instance.<p>
     *
     * @param editable the editable element
     * @param parentId the parent id
     */
    public CmsListCollectorEditor(Element editable, String parentId) {

        super(editable, parentId);
    }

    /**
     * Creates the button to add an element to the user's favorites.<p>
     *
     * @return the created button
     */
    public CmsPushButton createFavButton() {

        CmsPushButton favButton = new CmsPushButton();
        favButton.setImageClass(I_CmsButton.ButtonData.ADD_TO_FAVORITES.getSmallIconClass());
        favButton.setTitle(I_CmsButton.ButtonData.ADD_TO_FAVORITES.getTitle());
        favButton.setButtonStyle(I_CmsButton.ButtonStyle.FONT_ICON, null);
        add(favButton);
        favButton.addClickHandler(new ClickHandler() {

            @SuppressWarnings("synthetic-access")
            public void onClick(ClickEvent event) {

                CmsContainerpageController.get().getHandler().addToFavorites("" + m_editableData.getStructureId());
            }
        });
        return favButton;
    }

    /**
     * Returns true if the element view of the element is compatible with the currently set element view in the container page editor.<p>
     *
     * @return true if the element should be visible in the current mode
     */
    public boolean isVisibleInCurrentView() {

        return CmsContainerpageController.get().matchRootView(m_editableData.getElementView());
    }

    /**
     * Sets the 'parentHasDimensions' flag.<p>
     *
     * @param parentHasDimensions the new value of the flag
     */
    public void setParentHasDimensions(boolean parentHasDimensions) {

        m_parentHasDimensions = parentHasDimensions;
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#setPosition(org.opencms.gwt.client.util.CmsPositionBean, com.google.gwt.dom.client.Element)
     */
    @Override
    public void setPosition(CmsPositionBean position, Element containerElement) {

        m_position = position;
        Element parent = CmsDomUtil.getPositioningParent(getElement());
        if (!containerElement.isOrHasChild(parent)) {
            // the container element should have position relative,
            // so don't use any positioning parent that is not a child of the container-element
            parent = containerElement;
        }
        Style style = getElement().getStyle();
        int right = parent.getOffsetWidth()
            - ((m_position.getLeft() + m_position.getWidth()) - parent.getAbsoluteLeft());

        int top = m_position.getTop() - parent.getAbsoluteTop();
        if (m_position.getHeight() < 24) {
            // if the highlighted area has a lesser height than the buttons, center vertically
            top -= (24 - m_position.getHeight()) / 2;
        }
        if (top < 25) {
            // check if there is a parent option bar element present
            Element parentOptionBar = CmsDomUtil.getFirstChildWithClass(
                parent,
                I_CmsLayoutBundle.INSTANCE.containerpageCss().optionBar());
            if (parentOptionBar != null) {
                int optBarTop = CmsDomUtil.getCurrentStyleInt(parentOptionBar, CmsDomUtil.Style.top);
                int optBarRight = CmsDomUtil.getCurrentStyleInt(parentOptionBar, CmsDomUtil.Style.right);
                if ((Math.abs(optBarRight - right) < 25) && (Math.abs(optBarTop - top) < 25)) {
                    // in case the edit buttons overlap, move to the left
                    right = optBarRight + 25;
                }
            }
        }
        style.setRight(right, Unit.PX);
        style.setTop(top, Unit.PX);
        updateExpiredOverlayPosition(parent);
    }

    /**
     * Shows or hides the widget depending on the current view and whether the parent element has width or height.<p>
     */
    public void updateVisibility() {

        boolean visible = m_parentHasDimensions && isVisibleInCurrentView();
        setDisplayNone(!visible);

    }

    /**
     * Delete the editable element from page and VFS.<p>
     */
    protected void deleteElement() {

        CmsContainerpageController.get().deleteElement(m_editableData.getStructureId().toString(), m_parentResourceId);
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#getAdditionalButtons()
     */
    @Override
    protected Map<Integer, CmsPushButton> getAdditionalButtons() {

        Map<Integer, CmsPushButton> result = Maps.newHashMap();
        // only show add to favorites and info button, in case there actually is a resource and not in case of create new only
        if (m_editableData.hasDelete() || m_editableData.hasEdit()) {
            result.put(Integer.valueOf(130), createFavButton());
            result.put(Integer.valueOf(160), createInfoButton());
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickDelete()
     */
    @Override
    protected void onClickDelete() {

        removeHighlighting();
        CmsDomUtil.ensureMouseOut(getElement());
        openWarningDialog();
        m_delete.clearHoverState();
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickEdit()
     */
    @Override
    protected void onClickEdit() {

        openEditDialog(false, null);
        removeHighlighting();
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickNew(boolean)
     */
    @Override
    protected void onClickNew(boolean askCreateMode) {

        if (!askCreateMode) {
            openEditDialog(true, null);
            removeHighlighting();
        } else {

            CmsUUID referenceId = m_editableData.getStructureId();
            CmsCreateModeSelectionDialog.showDialog(referenceId, new AsyncCallback<String>() {

                public void onFailure(Throwable caught) {

                    // is never called

                }

                public void onSuccess(String result) {

                    openEditDialog(true, result);
                    removeHighlighting();
                }
            });
        }

    }

    /**
     * Opens the content editor.<p>
     *
     * @param isNew <code>true</code> to create and edit a new resource
     * @param mode the content creation mode
     */
    protected void openEditDialog(boolean isNew, String mode) {

        CmsContainerpageController.get().getContentEditorHandler().openDialog(
            m_editableData,
            isNew,
            m_parentResourceId,
            mode);
    }

    /**
     * Shows the delete warning dialog.<p>
     */
    protected void openWarningDialog() {

        CmsDeleteWarningDialog dialog = new CmsDeleteWarningDialog(m_editableData.getSitePath());
        Command callback = new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                deleteElement();
            }
        };
        dialog.loadAndShow(callback);
    }

    /**
     * Sets the display CSS property to none, or clears it, depending on the given parameter.<p>
     *
     * @param displayNone true if the widget should not be displayed
     */
    void setDisplayNone(boolean displayNone) {

        if (displayNone) {
            getElement().getStyle().setDisplay(Display.NONE);
        } else {
            getElement().getStyle().clearDisplay();
        }
    }

}
