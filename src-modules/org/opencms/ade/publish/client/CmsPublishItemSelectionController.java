/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/CmsPublishItemSelectionController.java,v $
 * Date   : $Date: 2010/03/29 08:47:34 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.publish.client;

import org.opencms.gwt.client.ui.input.CmsCheckBox;

import java.util.List;

/**
 * A helper class for managing the selection status of a resource item in the publish dialog.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
class CmsPublishItemSelectionController {

    /** True if there are problems with the given resource. */
    private final boolean m_hasProblems;

    /** The id of the given resource. */
    private final String m_id;

    /** The checkbox for selecting the resource for removal. */
    private final CmsCheckBox m_removeCheckBox;

    /** The checkbox for selecting the resource for publishing. */
    private final CmsCheckBox m_selectedCheckBox;

    /**
     * Constructs a new instance.<p>
     * 
     * @param id the id of the resource
     * @param selectedCheckBox the checkbox representing the selection status of the resource
     * @param removeCheckBox the checkbox representing the remove status of the resource 
     * @param hasProblems a flag indicating whether there are problems with the resource
     */
    public CmsPublishItemSelectionController(
        String id,
        CmsCheckBox selectedCheckBox,
        CmsCheckBox removeCheckBox,
        boolean hasProblems) {

        m_id = id;
        m_hasProblems = hasProblems;
        m_selectedCheckBox = selectedCheckBox;
        m_removeCheckBox = removeCheckBox;
        if (m_hasProblems) {
            m_selectedCheckBox.setChecked(false);
            m_selectedCheckBox.setEnabled(false);
        }
    }

    /**
     * Adds the resource's id to a given list if the resource is selected for publishing.<p>
     * 
     * @param ids the list of ids
     */
    public void addIdToPublish(List<String> ids) {

        if (isSelected()) {
            ids.add(m_id);
        }
    }

    /**
     * Adds the resource's id to a given list if the resource is selected for removal.<p>
     * 
     * @param ids the list of ids
     */
    public void addIdToRemove(List<String> ids) {

        if (shouldBeRemoved()) {
            ids.add(m_id);
        }
    }

    /**
     * Returns the UUID of the resource.<p>
     * 
     * @return a UUID string
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns true if the resource has been selected for publishing.<p>
     * 
     * @return true if the resource has been selected
     */
    public boolean isSelected() {

        return m_selectedCheckBox.isChecked();
    }

    /**
     * The method which should be called when the "remove" checkbox is clicked.<p>
     * 
     * @param remove if true, the "remove" checkbox has been checked 
     */
    public void onClickRemove(boolean remove) {

        if (!m_hasProblems) {
            m_selectedCheckBox.setChecked(!remove);
            m_selectedCheckBox.setEnabled(!remove);
        }
    }

    /**
     * Selects this controller's publish checkbox state.<p>
     * 
     * Setting the state to "checked" will only work if the resource doesn't have a  problem and hasn't already
     * been selected for removal.
     * 
     * @param select if true, try to check the checkbox, else uncheck it
     */
    public void selectIfPossible(boolean select) {

        m_selectedCheckBox.setChecked(select && !m_hasProblems && !shouldBeRemoved());
    }

    /**
     * Returns true if the resource has been selected for removal.<p>
     * 
     * @return true if the resource has been selected for removal
     */
    public boolean shouldBeRemoved() {

        return m_removeCheckBox.isChecked();
    }

}