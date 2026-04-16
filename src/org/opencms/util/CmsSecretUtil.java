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

package org.opencms.util;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

/**
 * Utility methods for fetching secrets from the secret provider.
 */
public final class CmsSecretUtil {

    /**
     * Gets the secret for the exact given key.
     *
     *  <p>If no secret is found, null will be returned.
     *
     * @param key the key for a secret
     * @return the secret
     */
    public static String getSecret(String key) {

        return OpenCms.getSecretStore().getSecret(key);
    }

    /**
     * Looks up a path dependent secret in the secret store for the current URI.
     *
     *  <p>This is done as follows:
     *  <p>For the root path of each subsitemap we are in, in ascending order, the key (prefix + &quot;.&quot; + path) is looked up.
     *  The first match will be returned. As a special case, the key for the site root is always looked up, even if it's not configured as
     *  a sitemap.
     *
     * @param prefix the key prefix
     * @param cms the OpenCms user context to use when resolving the secret
     * @return the secret (or null, if nothing was found)
     */
    public static String getSecretForUri(String prefix, CmsObject cms) {

        return getSecretForUri(prefix, cms, cms.getRequestContext().getRootUri());
    }

    /**
     * Looks up a path dependent secret in the secret store for the given root path.
     *
     *  <p>This is done as follows:
     *  <p>For the root path of each subsitemap we are in, in ascending order, the key (prefix + &quot;.&quot; + path) is looked up.
     *  The first match will be returned. As a special case, the key for the site root is always looked up, even if it's not configured as
     *  a sitemap.
     *
     * @param prefix the key prefix
     * @param cms the OpenCms user context to use when resolving the secret
     * @param rootPath the root path for which the secret should be looked up
     * @return the secret (or null, if nothing was found)
     */
    public static String getSecretForUri(String prefix, CmsObject cms, String rootPath) {

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(cms, rootPath);
        if (config != null) {
            return config.getSecretForUri(prefix);
        }
        return null;
    }

}
