/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

/**
 * Interface for a single named configuration object that can either be merged with other configuration
 * objects or disable a configuration object with the same name.<p>
 *
 * @author Georg Westenberger
 *
 * @version $Revision: 1.0$
 *
 * @since 8.0.1
 *
 * @param <X> the configuration object type which can be merged
 */
public interface I_CmsConfigurationObject<X extends I_CmsConfigurationObject<X>> {

    /** Default order constant for module configurations. */
    int DEFAULT_ORDER = 10000;

    /**
     * The name of the configuration object.<p>
     *
     * This name should be unique for each single configuration
     *
     * @return the name
     */
    String getKey();

    /**
     * If true, this configuration object will disable an inherited configuration object of the same name.<p>
     *
     * @return true if this configuration object is marked as "disabled"
     */
    boolean isDisabled();

    /**
     * Merges this configuration object with a child configuration object.<p>
     *
     * @param child the child configuration object
     *
     * @return the merged configuration objects
     */
    X merge(X child);
}
