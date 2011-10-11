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

package org.opencms.gwt.client.ui.input.upload;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A file object.<p>
 * 
 * @since 8.0.0
 */
public class CmsFileInfo extends JavaScriptObject {

    /**
     * Creates a simple JS file object.<p>
     */
    protected CmsFileInfo() {

        // noop
    }

    /**
     * Returns the file name.<p>
     * 
     * @return the file name
     */
    public final native String getFileName() /*-{

        return this.name ? this.name : this.fileName;

    }-*/;

    /**
     * Returns the file size.<p>
     * 
     * @return the file size
     */
    public final native int getFileSize() /*-{

        return this.size ? this.size : this.fileSize;

    }-*/;

    /**
     * Returns the suffix of the file name with the dot at the beginning e.g. <code>".zip"</code>.<p>
     * 
     * @return the suffix of the file name
     */
    public final native String getFileSuffix() /*-{

        var filename = this.name ? this.name : this.fileName;
        var dot = filename.lastIndexOf(".");
        if (dot >= 0) {
            return filename.substr(dot, filename.length);
        }
        return "";

    }-*/;
}
