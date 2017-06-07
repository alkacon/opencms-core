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

package org.opencms.gwt.client;

import org.opencms.util.CmsUUID;

/**
 * Interface for a bean holding data needed to open the xml content editor.<p>
 *
 * @since 8.0.1
 */
public interface I_CmsEditableData {

    /**
     * Gets the collector context id.<p>
     *
     * @return the collector context id
     */
    String getContextId();

    /**
     * Returns the edit id.<p>
     *
     * @return the edit id
     */
    String getEditId();

    /**
     * Returns the element language.<p>
     *
     * @return the element language
     */
    String getElementLanguage();

    /**
     * Returns the element name.<p>
     *
     * @return the element name
     */
    String getElementName();

    /**
     * Returns the new link url.<p>
     *
     * @return the new link url
     */
    String getNewLink();

    /**
     * Returns the new link url.<p>
     *
     * @return the new link url
     */
    String getNewTitle();

    /**
     * Returns the no edit reason.<p>
     *
     * @return the no edit reason
     */
    String getNoEditReason();

    /**
     * Gets the (fully qualified) name of the post-create handler class to use.<p>
     *
     * @return the post-create handler class name
     */
    String getPostCreateHandler();

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    String getSitePath();

    /**
     * Returns the structure id.<p>
     *
     * @return the structure id
     */
    CmsUUID getStructureId();

    /**
     * Returns if the given resource is unreleased or expired.<p>
     *
     * @return <code>true</code> if the given resource is unreleased or expired
     */
    boolean isUnreleasedOrExpired();

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path
     */
    void setSitePath(String sitePath);
}
