/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/I_CmsExtendedContentDefinition.java,v $
* Date   : $Date: 2001/10/22 11:36:01 $
* Version: $Revision: 1.2 $
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
package com.opencms.defaults;

import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;
import java.lang.reflect.*;
import com.opencms.core.*;
import com.opencms.core.exceptions.*;

/**
 * Content Definitions that uses the projectmanagement,
 * that means the cd can be published and the history
 * can be enabled, should implement this interface
 */
public interface I_CmsExtendedContentDefinition {

    /**
     * Returns the projectId of the content definition.
     * If the cd belongs to the current project the value
     * is the id of the current project otherwise its
     * the id of the online project
     *
     * @return int The project id
     */
    public int getProjectId();

    /**
     * Returns the state of the content definition:
     * unchanged, new, changed or deleted
     *
     * @return int The state of the cd
     */
    public int getState();

    /**
     * Returns the projectId of the content definition
     * that is stored in the cd table after the cd
     * was locked
     *
     * @return int The id of the cd
     */
    public int getLockedInProject();

    /**
     * Returns the url for the history of an entry
     *
     * @param cms The cms object
     * @param tagcontent
     * @param doc
     * @param userObject
     *
     * @return String The url for the history
     * @exception Exception Throws an exception if no url could be returned
     */
    public String getHistoryUrl(CmsObject cms, String tagcontent,
                                 A_CmsXmlContent doc, Object userObject) throws Exception;

    /**
     * Returns the url for the direct publish of an entry
     *
     * @param cms The cms object
     * @param tagcontent
     * @param doc
     * @param userObject
     *
     * @return String The url for the direct publish
     * @exception Exception Throws an exception if no url could be returned
     */
    public String getPublishUrl(CmsObject cms, String tagcontent,
                                 A_CmsXmlContent doc, Object userObject) throws Exception;

    /**
     * Returns the url for undeleting a deleted entry
     *
     * @param cms The cms object
     * @param tagcontent
     * @param doc
     * @param userObject
     *
     * @return String The url for undeleting
     * @exception Exception Throws an exception if no url could be returned
     */
    public String getUndeleteUrl(CmsObject cms, String tagcontent,
                                 A_CmsXmlContent doc, Object userObject) throws Exception;

}