/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/I_CmsSitemapEntryEditorModeHandler.java,v $
 * Date   : $Date: 2010/05/26 13:55:48 $
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

package org.opencms.ade.sitemap.client;

import java.util.Map;

/**
 * An interface for sitemap entry editor modes.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsSitemapEntryEditorModeHandler {

    /**
     * Creates the full path for a given url name.
     *  
     * @param urlName the url name for which the full path should be returned 
     * @return the full path 
     */
    String createPath(String urlName);

    /**
     * Returns the URL name with which the sitemap entry editor should be initialized.<p>
     * 
     * @return the initial URL name
     */
    String getName();

    /**
     * Returns the title with which the sitemap entry editor should be initialized.<p>
     * 
     * @return the initial title 
     */
    String getTitle();

    /**
     * Handles the submit action for the sitemap entry editor.<p>
     * 
     * @param newTitle the new title 
     * @param newUrlName the new url name 
     * @param vfsPath the new vfs path 
     * @param properties the new properties 
     */
    void handleSubmit(String newTitle, String newUrlName, String vfsPath, Map<String, String> properties);

    /**
     * Checks whether the new path of the edited sitemap entry is allowed.<p>
     * 
     * @param path the path which should be checked 
     * 
     * @return true if it is allowed to set the path of the edited entry to the argument 
     */
    boolean isPathAllowed(String path);
}
