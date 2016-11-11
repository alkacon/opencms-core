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

package org.opencms.ui.apps.projects;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.CmsProjectManagerConfiguration;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.contextmenu.CmsResourceContextMenuBuilder;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * The project files table.<p>
 */
public class CmsProjectFiles extends CmsFileTable implements I_CmsContextProvider {

    /** The serial version id. */
    private static final long serialVersionUID = -8713755588920379969L;

    /**
     * Constructor.<p>
     *
     * @param projectId the project id
     */
    public CmsProjectFiles(CmsUUID projectId) {
        super(null);
        setContextProvider(this);
        setMenuBuilder(new CmsResourceContextMenuBuilder());
        CmsObject cms = A_CmsUI.getCmsObject();
        List<CmsResource> childResources;
        try {
            childResources = cms.readProjectView(projectId, CmsResource.STATE_KEEP);
            fillTable(cms, childResources);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(
                CmsVaadinUtils.getMessageText(Messages.ERR_PROJECTS_CAN_NOT_DISPLAY_FILES_0),
                e);
        }

    }

    /**
     * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
     */
    public I_CmsDialogContext getDialogContext() {

        CmsFileTableDialogContext context = new CmsFileTableDialogContext(
            CmsProjectManagerConfiguration.APP_ID,
            ContextType.fileTable,
            this,
            m_currentResources);
        context.setEditableProperties(CmsFileExplorer.INLINE_EDIT_PROPERTIES);
        return context;
    }
}
