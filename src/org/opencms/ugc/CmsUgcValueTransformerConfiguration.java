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

package org.opencms.ugc;

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.ugc.CmsUgcValueTransformerConfiguration.CmsUgcSingleValueTransformer.TransformType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for a value transformer configuration that is provided in content's schema file
 * in the parameters section as follows:
 *
 * <pre>
 * &lt;parameters&gt;
 *    &lt;param name="ugc.values.transformer"&gt;
 *    {
 *        "Paragraph/Text": {
 *            "transformer": "antisamy"
 *        },
 *        "Title": {
 *            "transformer": "antisamy",
 *            "config": {
 *                "policy": "/system/modules/my.module/resources/ugc/antisamy-policy.xml"
 *            }
 *        },
 *        "Author": {
 *            "transformer": "none"
 *        },
 *        "Email": {
 *            "transformer": "escape"
 *        }
 *    }
 *    &lt;/param&gt;
 * &lt;/parameter&gt;
 * </pre>
 *
 * The configuration defines how values that are added via UGC at a certain path have to be transformed, primarily to not cause XSS or other issues.
 */
public class CmsUgcValueTransformerConfiguration {

    /**
     * The transformer for a single value.
     */
    public static class CmsUgcSingleValueTransformer {

        /**
         * The transformer type.
         */
        public static enum TransformType {
            /** XML escape the value */
            escape,
            /** Clean the value via AntiSamy */
            antisamy,
            /** Leave the value unchanged. */
            none
        }

        /** The default single value transformer. */
        public static CmsUgcSingleValueTransformer DEFAULT = new CmsUgcSingleValueTransformer(TransformType.escape);
        /** The mapping type. */
        private TransformType m_type;
        /** The (optional) additional configuration for the mapping type. */
        private JSONObject m_config;

        /**
         * Creates a new mapping security value of the provided type.
         * @param type the mapping type.
         */
        public CmsUgcSingleValueTransformer(TransformType type) {

            m_type = type == null ? TransformType.escape : type;
        }

        /**
         * Creates a new mapping security value of the provided type.
         * @param type the mapping type.
         * @param config the (optional) additional configuration for the mapping.
         */
        public CmsUgcSingleValueTransformer(TransformType type, JSONObject config) {

            m_type = type;
            m_config = TransformType.antisamy.equals(m_type) ? config : null;
        }

        /**
         * @return the optional configuration for the mapping security.
         */
        public JSONObject getConfig() {

            return m_config;
        }

        /**
         * @return the type of the mapping security
         */
        public TransformType getType() {

            return m_type;
        }
    }

    /** JSON key in the transformer configuration. */
    private static final String JSON_KEY_TRANSFORMER = "transformer";

    /** JSON key in the transformer configuration. */
    private static final String JSON_KEY_CONFIG = "config";

    /** The default tranformer configuration. */
    public static final CmsUgcValueTransformerConfiguration DEFAULT = new CmsUgcValueTransformerConfiguration();

    /** The resource's transformer map with the explicitly specified transformers. */
    private Map<String, CmsUgcSingleValueTransformer> m_transformerMap;

    /**
     * Wraps the provided transformer configuration
     * @param config the configuration to wrap
     * @throws JSONException thrown if the configuration is not a valid JSON
     * @throws IllegalArgumentException thrown if the configuration values are not as expected
     */
    public CmsUgcValueTransformerConfiguration(String config)
    throws JSONException, IllegalArgumentException {

        if (config != null) {
            JSONObject security = new JSONObject(config);
            Set<String> paths = security.keySet();
            Map<String, CmsUgcSingleValueTransformer> mappingSecurity = new HashMap<>(paths.size());
            for (String path : paths) {
                JSONObject pathConfig = security.getJSONObject(path);
                String adjustment = pathConfig.getString(JSON_KEY_TRANSFORMER);
                JSONObject mappingConfig = pathConfig.optJSONObject(JSON_KEY_CONFIG);
                mappingSecurity.put(
                    path,
                    new CmsUgcSingleValueTransformer(TransformType.valueOf(adjustment), mappingConfig));
            }
            m_transformerMap = mappingSecurity;
        } else {
            m_transformerMap = Collections.emptyMap();
        }
    }

    /**
     * Constructor for the default configuration.
     */
    private CmsUgcValueTransformerConfiguration() {

        m_transformerMap = Collections.emptyMap();
    }

    /**
     * Returns the transformer for the provided XML path.
     * @param path the XML path to get the value transformer for.
     * @return the transformer for the provided XML path.
     */
    public CmsUgcSingleValueTransformer getTransformer(String path) {

        return m_transformerMap.getOrDefault(path, CmsUgcSingleValueTransformer.DEFAULT);
    }
}
