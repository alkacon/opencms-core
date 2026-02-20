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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.security;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;

/**
 * Interface for a system-wide secret store, which can be used to access (string-valued) credentials like
 * passwords or API keys for external services.
 */
public interface I_CmsSecretStore extends I_CmsConfigurationParameterHandler {

    /**
     * Gets the secret for the given key.
     *
     *  <p>If no secret is found, null will be returned.
     *  <p>Note: this <em>may</em> be called before the initialize() method, and in that case, it should not throw any exceptions
     *  (whether it can actually return something meaningful depends on the implementation).
     *
     * @param key the key for a secret
     * @return the secret
     */
    String getSecret(String key);

    /**
     * The Admin CmsObject to use for Vfs operations, etc.
     *
     * @param cmsObject the admin CmsObject
     */
    void initialize(CmsObject cmsObject);

}
