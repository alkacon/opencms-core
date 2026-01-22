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

import java.io.File;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    @JsonCreator
    public CmsAiProviderConfig(
        @JsonProperty(value = "apiKey", required = true) String apiKey,
        @JsonProperty(value = "providerUrl", required = true) String providerUrl,
        @JsonProperty(value = "modelName", required = true) String modelName) {

        m_apiKey = apiKey;
        m_providerUrl = providerUrl;
        m_modelName = modelName;
    }

    /**
     * Loads the provider configuration from a JSON file in the RFS.
     *
     * @param path the path of the config file
     * @return the provider configuration
     * @throws Exception if loading the file fails
     */
    public static CmsAiProviderConfig loadFromJsonFile(String path) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), CmsAiProviderConfig.class);
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
