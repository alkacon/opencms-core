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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ugc.CmsUgcValueTransformerConfiguration.CmsUgcSingleValueTransformer;
import org.opencms.util.CmsParameterEscaper;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import org.apache.commons.logging.Log;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

/**
 * The class transforms the values that should be written to an XML content via UGC according to a
 * content type specific value transformation configuration.
 *
 * By default all values are xml escaped to void XSS issues when content that is created via UGC is
 * rendered on a webpage without any extra caution.
 */
public class CmsUgcValueTranformHandler {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUgcValueTranformHandler.class);

    /** The default value transformer. */
    public static final CmsUgcValueTranformHandler DEFAULT = new CmsUgcValueTranformHandler();

    /** XSD parameter where the value transformer configuration is provided. */
    private static String PARAM_UGC_VALUES_TRANSFORMER = "ugc.values.transformer";

    /** Config parameter policy for the antisamy value transformer. */
    private static String JSON_KEY_POLICY = "policy";

    /** The context. */
    private CmsObject m_cms;
    /** The value transformer configuration. */
    private CmsUgcValueTransformerConfiguration m_config;
    /** The (lazily initialized) parameter escaper used for antisamy transformations. */
    private CmsParameterEscaper m_escaper;

    /**
     * Creates the value transformer instance for the provided resource.
     * @param cms the context
     * @param resource the resource of the XML content to get the mapping security for
     * @throws CmsXmlException thrown if the content can't be unmarshalled
     * @throws CmsException thrown if the content can't be read
     * @throws IllegalArgumentException thrown if the value transformer configuration contains invalid transformers.
     * @throws JSONException thrown if the value transformer configuration is no valid JSON.
     */
    public CmsUgcValueTranformHandler(CmsObject cms, CmsResource resource)
    throws CmsXmlException, CmsException, IllegalArgumentException, JSONException {

        m_cms = cms;
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(resource));
        String valuesTransformerConfig = content.getHandler().getParameter(PARAM_UGC_VALUES_TRANSFORMER);
        m_config = new CmsUgcValueTransformerConfiguration(valuesTransformerConfig);
    }

    /**
     * Constructor to get the default value transformer configuration.
     */
    private CmsUgcValueTranformHandler() {

        m_config = CmsUgcValueTransformerConfiguration.DEFAULT;
    }

    /**
     * The method applies the configured value transformer to the given value.
     * @param path the XML path to store the value
     * @param value the value to transform
     * @return the transformed value
     */
    public String transformValue(String path, String value) {

        CmsUgcSingleValueTransformer secVal = m_config.getTransformer(CmsXmlUtils.removeAllXpathIndices(path));
        switch (secVal.getType()) {
            case none:
                return value;
            case antisamy:
                AntiSamy antiSamy = getParameterEscaper().createAntiSamy(
                    m_cms,
                    null == secVal.getConfig() ? null : secVal.getConfig().optString(JSON_KEY_POLICY, null));
                try {
                    CleanResults cres = antiSamy.scan(value);
                    return cres.getCleanHTML();
                } catch (ScanException | PolicyException e) {
                    LOG.error(
                        "Failed to clean HTML value \""
                            + value
                            + "\" in path \""
                            + path
                            + "\" via AntiSamy. Defaulting to 'escape'.",
                        e);
                }
                break;
            case escape:
                // do the default;
                break;
            default:
                LOG.error("Unsupported Security mapping type " + secVal.getType() + ". Defaulting to 'escape'.");
        }
        // Default: Escape
        return CmsEncoder.escapeXml(value);
    }

    /**
     * Returns the parameter escaper to use for antisamy.
     * @return the parameter escaper to use for antisamy.
     */
    private CmsParameterEscaper getParameterEscaper() {

        if (m_escaper == null) {
            m_escaper = new CmsParameterEscaper();
        }
        return m_escaper;
    }
}
