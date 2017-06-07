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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;

import java.util.Collection;
import java.util.Locale;

/**
 * Handles changes during the xml content editor session.<p>
 * Allows to modify the content after certain content fields have been edited. The modifications will then be transfered to the editor.<p>
 * For example, if the user sets a value for the field /link the change handler may set the value of another field /linktitle to the title of the linked resource.<p>
 */
public interface I_CmsXmlContentEditorChangeHandler {

    /**
     * Returns the handler configuration.<p>
     *
     * @return the handler configuration
     */
    String getConfiguration();

    /**
     * Returns the handler scope.<p>
     *
     * @return the handler scope
     */
    String getScope();

    /**
     * Handles the content change.<p>
     *
     * @param cms the cms context
     * @param content the changed content
     * @param locale the edited locale
     * @param changedPaths the changed content value paths
     *
     * @return the changed content
     */
    CmsXmlContent handleChange(CmsObject cms, CmsXmlContent content, Locale locale, Collection<String> changedPaths);

    /**
     * Sets the configuration.<p>
     *
     * @param configuration the configuration
     */
    void setConfiguration(String configuration);

    /**
     * Sets the scope to observe for changes.<p>
     * With a scope set to '/link' all changes below the path /link will be observed.<p>
     *
     * @param scope the scope
     */
    void setScope(String scope);
}
