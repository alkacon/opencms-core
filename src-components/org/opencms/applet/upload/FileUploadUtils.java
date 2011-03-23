/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/FileUploadUtils.java,v $
 * Date   : $Date: 2011/03/23 14:56:55 $
 * Version: $Revision: 1.17 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.applet.upload;

import java.io.File;

/**
 * Util class for the FileUpload applet, collects various util methods.<p> 
 *
 * Based on the Java 1.4 example.
 *
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.17 $ 
 * 
 * @since 6.0.0 
 */
public final class FileUploadUtils {

    /**
     * Empty Constructor.<p>
     */
    private FileUploadUtils() {

        // noop
    }

    /**
     * Gets the extension of a file.<p>
     * 
     * @param f The file to get the extension from
     * @return The file extension
     */
    public static String getExtension(File f) {

        String ext = null;
        if (f != null) {
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if ((i > 0) && (i < s.length() - 1)) {
                ext = s.substring(i).toLowerCase();
            }
        }
        return ext;
    }

    /**
     * Returns <code>true</code> in case the given extension is one of the known image extensions.<p>
     * 
     * Known extensions are: <code>.gif, .tiff, .jpeg, .jpg, .bmp, .png, .pnm, .pgm, .ppm, .pbm</code>.<p>
     * 
     * @param extension the extension to check, must start with a dot '.'
     * @return <code>true</code> in case the given extension is one of the known image extensions
     */
    public static boolean isImageExtension(String extension) {

        if (extension != null) {
            return (extension.equals(".gif")
                || extension.equals(".tiff")
                || extension.equals(".tif")
                || extension.equals(".jpeg")
                || extension.equals(".jpg")
                || extension.equals(".bmp")
                || extension.equals(".pnm")
                || extension.equals(".pbm")
                || extension.equals(".pgm")
                || extension.equals(".ppm") || extension.equals(".png"));
        } else {
            return false;
        }
    }

    /**
     * Returns <code>true</code> in case the given extension is one of the known office file extensions.<p>
     * 
     * @param extension the extension to check, must start with a dot '.'
     * @return <code>true</code> in case the given extension is one of the known office file extensions
     */
    public static boolean isOfficeExtension(String extension) {

        // TODO: next modification move this to a static sorted map for faster lookup:
        if (extension != null) {
            return (extension.equals(".odt")
                || extension.equals(".ods")
                || extension.equals(".odp")
                || extension.equals(".odg")
                || extension.equals(".pdf")
                || extension.equals(".doc")
                || extension.equals(".xls")
                || extension.equals(".vsd")
                || extension.equals(".ppt")
                || extension.equals(".docx")
                || extension.equals(".docm")
                || extension.equals(".dotx")
                || extension.equals(".dotm")
                || extension.equals(".xlsx")
                || extension.equals(".xlsm")
                || extension.equals(".xlsb")
                || extension.equals(".xlam")
                || extension.equals(".pptx")
                || extension.equals(".pptm")
                || extension.equals(".ppsx")
                || extension.equals(".ppsm")
                || extension.equals(".potx")
                || extension.equals(".potm")
                || extension.equals(".ppam")
                || extension.equals(".sldx")
                || extension.equals(".sldm") || extension.equals(".thmx")

            );
        } else {
            return false;
        }
    }

    /**
     * Returns <code>true</code> in case the given extension is one of the known text file extensions.<p>
     * 
     * Known extensions are: <code>.txt, .ini, .bat, .cmd, .sh, .java, .log, .xml, .html, .sys</code>.<p>
     * 
     * @param extension the extension to check, must start with a dot '.'
     * @return <code>true</code> in case the given extension is one of the known text file extensions
     */
    public static boolean isTextExtension(String extension) {

        if (extension != null) {
            return (extension.equals(".txt")
                || extension.equals(".ini")
                || extension.equals(".bat")
                || extension.equals(".cmd")
                || extension.equals(".sh")
                || extension.equals(".java")
                || extension.equals(".log")
                || extension.equals(".xml")
                || extension.equals(".html") || extension.equals(".sys"));
        } else {
            return false;
        }
    }

    /**
     * Returns <code>true</code> in case the given extension is one of the known office file extensions.<p>
     * 
     * Known extensions are: <code>.html, .htm, .shtml, .xml, .xhtml, .js, .css and .txt</code>.<p>
     * 
     * @param extension the extension to check, must start with a dot '.'
     * 
     * @return <code>true</code> in case the given extension is one of the known office file extensions
     */
    public static boolean isWebExtension(String extension) {

        if (extension != null) {
            return (extension.equals(".html")
                || extension.equals(".htm")
                || extension.equals(".shtml")
                || extension.equals(".xml")
                || extension.equals(".xhtml")
                || extension.equals(".js")
                || extension.equals(".css") || extension.equals(".txt"));
        } else {
            return false;
        }
    }
}