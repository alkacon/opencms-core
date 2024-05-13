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

package org.opencms.gwt.client.ui.input.upload;

import java.util.Comparator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.InputElement;

/**
 * A file object.<p>
 *
 * @since 8.0.0
 */
public class CmsFileInfo extends JavaScriptObject {

    /** The file info comparator. */
    public static final Comparator<CmsFileInfo> INFO_COMPARATOR = new Comparator<CmsFileInfo>() {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsFileInfo o1, CmsFileInfo o2) {

            return o1.getFileName().compareTo(o2.getFileName());
        }
    };

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
        if (typeof this.size === 'undefined') {
            return this.fileSize;
        } else {
            return this.size;
        }
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

    /**
     * Returns the associated input element if available.<p>
     *
     * @return the input element
     */
    public final native InputElement getInputElement() /*-{
                                                       return this.input ? this.input : null;
                                                       }-*/;

    /**
     * Returns the file name to override the original one if set, or the original file name.<p>
     *
     * @return the override file name
     */
    public final native String getOverrideFileName() /*-{
                                                     return this.overrideFileName ? this.overrideFileName
                                                     : this.name ? this.name : this.fileName;
                                                     }-*/;

    /**
     * Sets the file name to override the original one.<p>
     *
     * @param overrideFileName the override file name
     */
    public final native void setOverrideFileName(String overrideFileName) /*-{
                                                                          this.overrideFileName = overrideFileName;
                                                                          }-*/;
}
