/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.repository;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsResourceTranslator;

/**
 * Represents a repository.<p>
 *
 * Since different types of repositories have very different methods, this interface only
 * provides the bare minimum of methods which are necessary for configuration.<p>
 *
 * @since 6.2.4
 */
public interface I_CmsRepository extends I_CmsConfigurationParameterHandler {

    /**
     * Gets the repository filter.<p>
     *
     * @return the repository filter
     */
    CmsRepositoryFilter getFilter();

    /**
     * Returns the name of the repository.<p>
     *
     * @return the name of the repository
     */
    String getName();

    /**
     * Gets the repository-specific file translations.
     *
     * @return the repository-specific file translations
     */
    CmsResourceTranslator getTranslation();

    /**
     * Initializes this repository with an admin CMS object.<p>
     *
     * @param cms an admin CMS object
     * @throws CmsException if something goes wrong
     */
    void initializeCms(CmsObject cms) throws CmsException;

    /**
     * Returns true if the repository specific file translations are enabled (they may also be set without being enabled).
     *
     * @return true if the file translations are enabled
     */
    boolean isTranslationEnabled();

    /**
     * Sets the repository filter.<p>
     *
     * @param filter the repository filter
     */
    void setFilter(CmsRepositoryFilter filter);

    /**
     * Sets the name for this repository.<p>
     *
     * @param name the name to use for the repository
     */
    void setName(String name);

    /**
     * Sets the repository-specific file translations
     *
     * @param translator the file translations
     * @param enabled true if the translations should be enabled
     */
    void setTranslation(CmsResourceTranslator translator, boolean enabled);
}
