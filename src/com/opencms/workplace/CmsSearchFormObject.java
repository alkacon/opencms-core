/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsSearchFormObject.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.workplace;

import java.io.Serializable;

/**
 * Describes a file in the Cms.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.3 $ $Date: 2003/04/01 15:20:18 $
 */
public class CmsSearchFormObject implements Cloneable,Serializable {

    /**
     * The filter of search.
     */
    private String m_filter;

    /**
     * The value of search.
     */
    private String m_value01;

    /**
     * The value of search.
     */
    private String m_value02;

    /**
     * The value of search.
     */
    private String m_value03;

     /**
      * Constructor, creates a new CmsSearchFormObject object.
      *
      * @param filter The filtername.
      * @param value01 The first value of search filter.
      * @param value02 The second value of search filter.
      * @param value03 The third value of search filter.
      */
     public CmsSearchFormObject(String filter, String value01, String value02, String value03){
        // set content and size.
        m_filter=filter;
        m_value01=value01;
        m_value02=value02;
        m_value03=value03;
   }

    /**
     * Gets the filter for search.
     *
     * @return the filtername.
     */
    public String getFilter() {
      return m_filter;
    }

    /**
     * Gets the first value for the filter.
     *
     * @return the value for filter.
     */
    public String getValue01() {
      return m_value01;
    }

    /**
     * Gets the second value for the filter.
     *
     * @return the value for filter.
     */
    public String getValue02() {
      return m_value02;
    }

    /**
     * Gets the third value for the filter.
     *
     * @return the value for filter.
     */
    public String getValue03() {
      return m_value03;
    }
}
