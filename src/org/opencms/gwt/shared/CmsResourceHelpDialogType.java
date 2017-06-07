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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The Help dialog type
 */
public enum CmsResourceHelpDialogType implements IsSerializable {
    /**
     * Help Dialog type
     */
    RESOURCE(CmsResourceTypeHelpBean.HELP_DOCUMENTS_VFS_PATH), START(CmsResourceTypeHelpBean.HELP_DOCUMENTS_START_VFS_PATH), COMMON(CmsResourceTypeHelpBean.HELP_DOCUMENTS_COMMON_VFS_PATH), EDITOR(CmsResourceTypeHelpBean.HELP_DOCUMENTS_EDITOR_VFS_PATH);

    /** path part in vfs */
    private String path;

    /**
     * @param path the part path
     */
    private CmsResourceHelpDialogType(final String path) {
        this.path = path;
    }

    /**
     * @return path
     */
    public String getPath() {

        return path;
    }

}
