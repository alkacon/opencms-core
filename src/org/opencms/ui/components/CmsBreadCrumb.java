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

package org.opencms.ui.components;

import org.opencms.ui.shared.components.CmsBreadCrumbState;

import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.shared.communication.SharedState;
import com.vaadin.ui.AbstractComponent;

/**
 * The bread crumb component.<p>
 */
public class CmsBreadCrumb extends AbstractComponent {

    /** The serial version id. */
    private static final long serialVersionUID = 5348912019194395575L;

    /**
     * @see com.vaadin.server.AbstractClientConnector#getStateType()
     */
    @Override
    public Class<? extends SharedState> getStateType() {

        return CmsBreadCrumbState.class;
    }

    /**
     * Sets the bread crumb entries.<p>
     *
     * @param entries the bread crumb entries
     */
    public void setEntries(Map<String, String> entries) {

        String[][] entriesArray = null;
        if ((entries != null) && !entries.isEmpty()) {
            entriesArray = new String[entries.size()][];
            int i = 0;
            for (Entry<String, String> entry : entries.entrySet()) {
                entriesArray[i] = new String[] {entry.getKey(), entry.getValue()};
                i++;
            }
        }

        getState().setEntries(entriesArray);
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getState()
     */
    @Override
    protected CmsBreadCrumbState getState() {

        return (CmsBreadCrumbState)super.getState();
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getState(boolean)
     */
    @Override
    protected CmsBreadCrumbState getState(boolean markAsDirty) {

        return (CmsBreadCrumbState)super.getState(markAsDirty);
    }

}
