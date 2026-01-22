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

package org.opencms.ai;

/**
 * Simple configuration bean for AI provider access settings.<p>
 *
 * @since 21.0.0
 */
public class CmsAiProviderConfig {

    /** The API key used to access the provider. */
    private String m_apiKey;

    /** The base URL of the provider endpoint. */
    private String m_providerUrl;

    /** The model name to use for requests. */
    private String m_modelName;

    /**
     * Creates a new provider configuration.<p>
     *
     * @param apiKey the API key to use
     * @param providerUrl the provider base URL
     * @param modelName the model name
     */
    public CmsAiProviderConfig(String apiKey, String providerUrl, String modelName) {

        m_apiKey = apiKey;
        m_providerUrl = providerUrl;
        m_modelName = modelName;
    }

    /**
     * Returns the API key used to access the provider.<p>
     *
     * @return the API key
     */
    public String getApiKey() {

        return m_apiKey;
    }

    /**
     * Returns the model name used for requests.<p>
     *
     * @return the model name
     */
    public String getModelName() {

        return m_modelName;
    }

    /**
     * Returns the provider base URL.<p>
     *
     * @return the provider URL
     */
    public String getProviderUrl() {

        return m_providerUrl;
    }
}
