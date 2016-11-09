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

package org.opencms.ui.components.fileselect;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Window;

/**
 * Sitemap folder select field.<p>
 */
public class CmsSitemapSelectField extends CmsResourceSelectField {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapSelectField.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The resource to initially show in the target selection tree. */
    private CmsResource m_startResource;

    /** Selector popup window. */
    private Window m_window;

    /**
     * Creates a new instance.<p>
     *
     * @param cmsResource the start resource
     */
    public CmsSitemapSelectField(CmsResource cmsResource) {

        super();
        m_startResource = cmsResource;
    }

    /**
     * @see org.opencms.ui.components.fileselect.A_CmsFileSelectField#openFileSelector()
     */
    @Override
    protected void openFileSelector() {

        if (m_window == null) {
            m_window = CmsBasicDialog.prepareWindow();
        }
        m_window.close();
        try {
            m_window.setCaption(
                m_fileSelectCaption != null
                ? m_fileSelectCaption
                : CmsVaadinUtils.getMessageText(org.opencms.ui.components.Messages.GUI_FILE_SELECT_CAPTION_0));
            A_CmsUI.get().addWindow(m_window);
            CmsResourceSelectDialog fileSelect = new CmsResourceSelectDialog(
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder());
            CmsResource startResource = getValue() != null ? getValue() : m_startResource;
            if (startResource != null) {
                fileSelect.showStartResource(startResource);
            }
            fileSelect.showSitemapView(m_startWithSitemapView);
            m_window.setContent(fileSelect);
            fileSelect.addSelectionHandler(new I_CmsSelectionHandler<CmsResource>() {

                @SuppressWarnings("synthetic-access")
                public void onSelection(CmsResource selected) {

                    setResourceValue(selected);
                    m_window.close();
                }
            });
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }

    }

}
