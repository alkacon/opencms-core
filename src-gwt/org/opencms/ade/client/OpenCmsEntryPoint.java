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

package org.opencms.ade.client;

import org.opencms.ade.containerpage.client.CmsContainerpageEditor;
import org.opencms.ade.contenteditor.client.CmsContentEditorEntryPoint;
import org.opencms.ade.editprovider.client.CmsDirectEditEntryPoint;
import org.opencms.ade.galleries.client.CmsGallery;
import org.opencms.ade.postupload.client.CmsPostUploadDialogEntryPoint;
import org.opencms.ade.properties.client.CmsPropertiesEntryPoint;
import org.opencms.ade.publish.client.CmsPublishEntryPoint;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.upload.client.CmsUpload;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.ModuleKey;

import com.google.gwt.core.client.EntryPoint;

/**
 * Main OpenCms entry point, will delegate to the requested module.<p>
 */
public class OpenCmsEntryPoint extends A_CmsEntryPoint {

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @SuppressWarnings("incomplete-switch")
    @Override
    public void onModuleLoad() {

        EntryPoint entry = null;
        ModuleKey key = getModuleKey();
        if (key != null) {
            switch (key) {
                case containerpage:
                    entry = new CmsContainerpageEditor();
                    break;
                case contenteditor:
                    entry = new CmsContentEditorEntryPoint();
                    break;
                case galleries:
                    entry = new CmsGallery();
                    break;
                case postupload:
                    entry = new CmsPostUploadDialogEntryPoint();
                    break;
                case publish:
                    entry = new CmsPublishEntryPoint();
                    break;
                case sitemap:
                    entry = new CmsSitemapView();
                    break;
                case upload:
                    entry = new CmsUpload();
                    break;
                case editprovider:
                    entry = new CmsDirectEditEntryPoint();
                    break;
                case properties:
                    entry = new CmsPropertiesEntryPoint();
                    break;
            }
        }

        if (entry != null) {
            entry.onModuleLoad();
        }
    }

    /**
     * Returns the key to the requested module.<p>
     *
     * @return the module key
     */
    private ModuleKey getModuleKey() {

        String key = CmsCoreProvider.getMetaElementContent(CmsCoreData.META_PARAM_MODULE_KEY);
        ModuleKey result = ModuleKey.valueOf(key);
        return result;
    }
}
