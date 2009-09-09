/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/Attic/I_CmsResource.java,v $
 * Date   : $Date: 2009/09/09 14:26:35 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2008 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.file.history.CmsHistoryFolder;

import java.util.Comparator;

/**
 * Common ancestor interface for {@link CmsFile} and {@link CmsFolder} 
 * as well as for {@Link CmsHistoryFile} and {@link CmsHistoryFolder}.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 8.0.0 
 */
public interface I_CmsResource {

    /**
     * A comparator for the release date of two resources.<p>
     * 
     * If the release date of a resource is not set, the
     * creation date is used instead.<p>
     */
    public static final Comparator<I_CmsResource> COMPARE_DATE_RELEASED = new Comparator<I_CmsResource>() {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsResource r1, I_CmsResource r2) {

            if (r1 == r2) {
                return 0;
            }

            long date1 = r1.getDateReleased();
            if (date1 == CmsResource.DATE_RELEASED_DEFAULT) {
                // use last modification date if release date is not set
                date1 = r1.getDateLastModified();
            }

            long date2 = r2.getDateReleased();
            if (date2 == CmsResource.DATE_RELEASED_DEFAULT) {
                // use last modification date if release date is not set
                date2 = r2.getDateLastModified();
            }

            return (date1 > date2) ? -1 : (date1 < date2) ? 1 : 0;
        }
    };

    /**
     * A comparator for the root path of two resources.<p>
     */
    public static final Comparator<I_CmsResource> COMPARE_ROOT_PATH = new Comparator<I_CmsResource>() {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsResource r1, I_CmsResource r2) {

            if (r1 == r2) {
                return 0;
            }
            return r1.getRootPath().compareTo(r2.getRootPath());
        }
    };

    /**
     * A comparator for the root path of two resources ignoring case differences.<p>
     */
    public static final Comparator<I_CmsResource> COMPARE_ROOT_PATH_IGNORE_CASE = new Comparator<I_CmsResource>() {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsResource r1, I_CmsResource r2) {

            if (r1 == r2) {
                return 0;
            }
            return r1.getRootPath().compareToIgnoreCase(r2.getRootPath());
        }
    };

    /**
     * A comparator for the root path of two resources ignoring case differences, putting folders before files.<p>
     */
    public static final Comparator<I_CmsResource> COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST = new Comparator<I_CmsResource>() {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsResource r1, I_CmsResource r2) {

            if (r1 == r2) {
                return 0;
            }

            if (r1.isFolder() && !r2.isFolder()) {
                return -1;
            } else if (r2.isFolder() && !r1.isFolder()) {
                return 1;
            }
            // if same type, compare the name of the resource
            return r1.getRootPath().compareToIgnoreCase(r2.getRootPath());
        }
    };

    /**
     * Returns the date of the last modification of the content of this resource.<p>
     *
     * @return the date of the last modification of the content of this resource
     */
    long getDateContent();

    /**
     * Returns the date of the creation of this resource.<p>
     *
     * @return the date of the creation of this resource
     */
    long getDateCreated();

    /**
     * Returns the expiration date this resource.<p>
     *
     * @return the expiration date of this resource
     */
    long getDateExpired();

    /**
     * Returns the date of the last modification of this resource.<p>
     *
     * @return the date of the last modification of this resource
     */
    long getDateLastModified();

    /**
     * Returns the release date this resource.<p>
     *
     * @return the release date of this resource
     */
    long getDateReleased();

    /**
     * Returns the name of a resource with it's full path from the root folder 
     * including the current site root, 
     * for example <code>/sites/default/myfolder/index.html</code>.<p>
     *
     * @return the name of a resource with it's full path from the root folder 
     *      including the current site root
     */
    String getRootPath();

    /**
     * Returns <code>true</code> if the resource is a file, i.e. can have no sub-resources.<p>
     *
     * @return true if this resource is a file, false otherwise
     */
    boolean isFile();

    /**
     * Returns <code>true</code> if the resource is a folder, i.e. can have sub-resources.<p>
     *
     * @return true if this resource is a folder, false otherwise
     */
    boolean isFolder();
}