/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearch.java,v $
 * Date   : $Date: 2010/01/11 13:26:40 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.galleries;

/**
 * Contains the functions for the ADE gallery search.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearch {

    /** Sort parameter constants. */
    public enum SortParam {

        /** Sort created by ascending. */
        CREATEDBY_ASC("createdby.asc"),

        /** Sort created by descending. */
        CREATEDBY_DESC("createdby.desc"),

        /** Sort date created ascending. */
        DATECREATED_ASC("datecreated.asc"),

        /** Sort date created descending. */
        DATECREATED_DESC("datecreated.desc"),

        /** Sort date expired ascending. */
        DATEEXPIRED_ASC("dateexpired.asc"),

        /** Sort date expired descending. */
        DATEEXPIRED_DESC("dateexpired.desc"),

        /** Sort date modified ascending. */
        DATEMODIFIED_ASC("datemodified.asc"),

        /** Sort date modified descending. */
        DATEMODIFIED_DESC("datemodified.desc"),

        /** Sort date released ascending. */
        DATERELEASED_ASC("datereleased.asc"),

        /** Sort date released descending. */
        DATERELEASED_DESC("datereleased.desc"),

        /** Sort modified by ascending. */
        MODIFIEDBY_ASC("modifiedby.asc"),

        /** Sort modified by descending. */
        MODIFIEDBY_DESC("modifiedby.desc"),

        /** Sort path ascending. */
        PATH_ASC("path.asc"),

        /** Sort path descending. */
        PATH_DESC("path.desc"),

        /** Sort score ascending. */
        SCORE_ASC("score.asc"),

        /** Sort score descending. */
        SCORE_DESC("score.desc"),

        /** Sort size ascending. */
        SIZE_ASC("size.asc"),

        /** Sort size descending. */
        SIZE_DESC("size.desc"),

        /** Sort state ascending. */
        STATE_ASC("state.asc"),

        /** Sort state descending. */
        STATE_DESC("state.desc"),

        /** Sort title ascending. */
        TITLE_ASC("title.asc"),

        /** Sort title ascending. */
        TITLE_DESC("title.desc"),

        /** Sort type ascending. */
        TYPE_ASC("type.asc"),

        /** Sort type descending. */
        TYPE_DESC("type.desc");

        /** The default sort parameter. */
        public static final SortParam DEFAULT = TITLE_DESC;

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private SortParam(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /**
     * Returns the search result list.<p>
     *
     * @return the search result list
     */
    public CmsGallerySearchResultList getResult() {

        /** TODO: implement this. */
        return null;
    }
}