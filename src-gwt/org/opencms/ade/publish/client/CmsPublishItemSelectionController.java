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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.client.CmsPublishItemStatus.State;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListItemWidgetCss;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.util.CmsUUID;

/**
 * A helper class for managing the selection status of a resource item in the publish dialog.<p>
 *
 * @since 8.0.0
 */
class CmsPublishItemSelectionController {

    /** True if there are problems with the given resource. */
    private final boolean m_hasProblems;

    /** The id of the given resource. */
    private final CmsUUID m_id;

    /** The checkbox for selecting the resource for removal. */
    private final CmsCheckBox m_removeCheckBox;

    /** The checkbox for selecting the resource for publishing. */
    private final CmsCheckBox m_selectedCheckBox;

    /** The CSS bundle used for this widget. */
    private static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();

    /** A style variable which is changed depending on the "removed" state of the corresponding publish item. */
    private final CmsStyleVariable m_removeStyle;

    /**
     * Constructs a new instance.<p>
     *
     * @param id the id of the resource
     * @param selectedCheckBox the checkbox representing the selection status of the resource
     * @param removeCheckBox the checkbox representing the remove status of the resource
     * @param removeStyle the style variable to keep track of the "remove" status
     * @param hasProblems a flag indicating whether there are problems with the resource
     */
    public CmsPublishItemSelectionController(
        CmsUUID id,
        CmsCheckBox selectedCheckBox,
        CmsCheckBox removeCheckBox,
        CmsStyleVariable removeStyle,
        boolean hasProblems) {

        m_id = id;
        m_hasProblems = hasProblems;
        m_selectedCheckBox = selectedCheckBox;
        m_removeCheckBox = removeCheckBox;
        m_removeStyle = removeStyle;
        if (m_hasProblems) {
            m_selectedCheckBox.setChecked(false);
            m_selectedCheckBox.setEnabled(false);
        }
    }

    /**
     * Returns the UUID of the resource.<p>
     *
     * @return a UUID string
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Updates the list item and checkboxes with the current item status from the model.<p>
     *
     * @param status the status which should be used to update the widgets
     */
    public void update(CmsPublishItemStatus status) {

        if (status.isDisabled()) {
            m_selectedCheckBox.setEnabled(false);
            m_selectedCheckBox.setChecked(false);
            m_removeCheckBox.setChecked(status.getState() == State.remove);
            updateRemoval(status.getState() == State.remove);
        } else {
            switch (status.getState()) {
                case remove:
                    m_selectedCheckBox.setChecked(false);
                    m_selectedCheckBox.setEnabled(false);
                    m_removeCheckBox.setChecked(true);
                    updateRemoval(true);
                    break;
                case publish:
                    m_selectedCheckBox.setEnabled(true);
                    m_selectedCheckBox.setChecked(true);
                    m_removeCheckBox.setChecked(false);
                    updateRemoval(false);
                    break;
                case normal:
                default:
                    m_selectedCheckBox.setChecked(false);
                    m_selectedCheckBox.setEnabled(true);
                    m_removeCheckBox.setChecked(false);
                    updateRemoval(false);
                    break;

            }
        }
    }

    /**
     * Updates the style of the list item depending on whether it is marked to be removed or not.<p>
     *
     * @param remove true if the item is marked to be removed
     */
    public void updateRemoval(boolean remove) {

        I_CmsListItemWidgetCss itemWidgetCss = I_CmsLayoutBundle.INSTANCE.listItemWidgetCss();
        m_removeStyle.setValue(remove ? itemWidgetCss.disabledItem() : CSS.itemToKeep());
        m_removeCheckBox.setTitle(
            remove
            ? Messages.get().key(Messages.GUI_PUBLISH_UNREMOVE_BUTTON_0)
            : Messages.get().key(Messages.GUI_PUBLISH_REMOVE_BUTTON_0));
    }
}