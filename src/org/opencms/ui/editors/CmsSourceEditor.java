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

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;

/**
 * The source and plain text editor.<p>
 */
public class CmsSourceEditor extends A_CmsFrameEditor {

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#getPriority()
     */
    public int getPriority() {

        return 10;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#matchesResource(org.opencms.file.CmsResource, boolean)
     */
    public boolean matchesResource(CmsResource resource, boolean plainText) {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);

        return !((type instanceof CmsResourceTypeBinary) || (type instanceof CmsResourceTypeImage));
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#newInstance()
     */
    public I_CmsEditor newInstance() {

        return new CmsSourceEditor();
    }

    /**
     * @see org.opencms.ui.editors.A_CmsFrameEditor#getEditorUri()
     */
    @Override
    protected String getEditorUri() {

        return "/system/workplace/editors/codemirror/editor.jsp";
    }

    /**
     * @see org.opencms.ui.editors.A_CmsFrameEditor#getFrameStyles()
     */
    @Override
    protected String getFrameStyles() {

        return "o-editor-frame";
    }

}
