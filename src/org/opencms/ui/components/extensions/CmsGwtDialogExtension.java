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

package org.opencms.ui.components.extensions;

import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc;
import org.opencms.ui.shared.components.I_CmsGwtDialogServerRpc;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * Extension used to open existing GWT based dialogs (from ADE, etc.) from the server side, for use in context menu actions.<p>
 */
public class CmsGwtDialogExtension extends AbstractExtension implements I_CmsGwtDialogServerRpc {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context for which this extension instance was created. */
    private I_CmsDialogContext m_dialogContext;

    /**
     * Creates a new instance and binds it to a UI instance.<p>
     *
     * @param ui the UI to bind this extension to
     * @param dialogContext the dialog context
     */
    public CmsGwtDialogExtension(UI ui, I_CmsDialogContext dialogContext) {
        extend(ui);
        m_dialogContext = dialogContext;
        registerRpc(this, I_CmsGwtDialogServerRpc.class);
    }

    /**
     * Open property editor for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource
     */
    public void editProperties(CmsUUID structureId) {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).editProperties("" + structureId);
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogServerRpc#onClose(java.util.List)
     */
    public void onClose(List<String> changedStructureIds) {

        remove();
        List<CmsUUID> changed = Lists.newArrayList();
        for (String id : changedStructureIds) {
            changed.add(new CmsUUID(id));
        }
        m_dialogContext.finish(changed);
    }
}
