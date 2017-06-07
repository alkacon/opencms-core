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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.shared.I_CmsBinaryPreviewProvider;
import org.opencms.gwt.client.I_CmsHasInit;

import java.util.HashMap;
import java.util.Map;

/**
 * The binary resource preview factory.<p>
 *
 * @since 8.0.3
 */
public final class CmsBinaryPreviewFactory implements I_CmsPreviewFactory, I_CmsHasInit {

    /** The preview registry. */
    private Map<String, CmsBinaryResourcePreview> m_previewRegistry;

    /**
     * Constructor.<p>
     */
    private CmsBinaryPreviewFactory() {

        m_previewRegistry = new HashMap<String, CmsBinaryResourcePreview>();
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        CmsBinaryPreviewFactory factory = new CmsBinaryPreviewFactory();
        CmsGalleryController.registerPreviewFactory(I_CmsBinaryPreviewProvider.PREVIEW_NAME, factory);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewFactory#getPreview(org.opencms.ade.galleries.client.ui.CmsGalleryDialog)
     */
    public I_CmsResourcePreview<?> getPreview(CmsGalleryDialog dialog) {

        if (!m_previewRegistry.containsKey(dialog.getDialogId())) {
            m_previewRegistry.put(dialog.getDialogId(), new CmsBinaryResourcePreview(dialog));
        }
        return m_previewRegistry.get(dialog.getDialogId());
    }

}
