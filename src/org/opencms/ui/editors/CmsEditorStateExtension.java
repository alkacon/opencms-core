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

package org.opencms.ui.editors;

import org.opencms.ui.shared.rpc.I_CmsEditorStateRPC;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractComponent;

/**
 * Extension for iFrame embedded editors storing the changed state of the edited content.<p>
 */
public class CmsEditorStateExtension extends AbstractExtension implements I_CmsEditorStateRPC {

    /** The serial version id. */
    private static final long serialVersionUID = -7159723321228453105L;

    /** The changed flag. */
    private boolean m_hasChanges;

    /**
     * Constructor.<p>
     *
     * @param component the component to extend
     */
    public CmsEditorStateExtension(AbstractComponent component) {
        extend(component);
        registerRpc(this);
    }

    /**
     * Returns whether the editor has content changes.<p>
     *
     * @return <code>true</code> in case the editor has content changes
     */
    public boolean hasChanges() {

        return m_hasChanges;
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEditorStateRPC#setHasChanges(boolean)
     */
    public void setHasChanges(boolean hasChanges) {

        m_hasChanges = hasChanges;
    }
}
