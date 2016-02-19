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

package org.opencms.ui.editors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.components.CmsBrowserFrame;

import com.vaadin.server.ExternalResource;

/**
 * Class to extended by frame based editors.<p>
 */
public abstract class A_CmsFrameEditor implements I_CmsEditor {

    /** The frame component. */
    private CmsBrowserFrame m_frame;

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#initUI(org.opencms.ui.apps.I_CmsAppUIContext, org.opencms.file.CmsResource, java.lang.String)
     */
    public void initUI(I_CmsAppUIContext context, CmsResource resource, String backLink) {

        CmsObject cms = A_CmsUI.getCmsObject();
        String link = OpenCms.getLinkManager().substituteLinkForRootPath(cms, getEditorUri());
        m_frame = new CmsBrowserFrame();
        m_frame.setDescription("Editor");
        m_frame.setName("edit");
        m_frame.setSource(
            new ExternalResource(link + "?resource=" + cms.getSitePath(resource) + "&backlink=" + backLink));
        m_frame.setSizeFull();
        context.showInfoArea(false);
        context.hideToolbar();
        m_frame.addStyleName("o-editor-frame");
        context.setAppContent(m_frame);
    }

    /**
     * Returns the editor URI.<p>
     *
     * @return the editor URI
     */
    protected abstract String getEditorUri();
}
