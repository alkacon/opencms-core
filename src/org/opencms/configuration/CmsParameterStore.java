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

package org.opencms.configuration;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.google.common.primitives.Doubles;

/**
 * Class for accessing global 'weighted' configuration parameters defined in parameter files in the VFS. Used as a singleton.
 *
 * <p>
 * Parameter files are XML configuration files that contain a list of named, string-valued configuration parameters, optionally with a numeric weight. The weight can be set
 * individually for each parameter, or globally for a whole parameter file, but individual weights override parameter file weights. The schema for these is defined in org/opencm/configuration/paramfile.dtd.
 * <p>
 * To register a parameter file in OpenCms, its path must be listed as a value  of the 'paramfile' module parameter for an installed module. The module parameter
 * can be set on multiple modules, and may also contain multiple paths separated by commas.
 * <p>
 * When retrieving a value that is defined in multiple parameter files, the one with the highest weight wins. If there are multiple instances with the same weight, which one of them wins is implementation dependent.
 */
public class CmsParameterStore {

    /**
     * An individual weighted parameter value, with a 'source' attribute for better debuggability.
     */
    public static class WeightedValue {

        /** The source where the value comes from. */
        private String m_source;

        /** The actual value. */
        private String m_value;

        /** The weight of the value. */
        private double m_weight;

        /**
         * Creates a new weighted value.
         *
         * @param value the actual value
         * @param weight the weight
         * @param source the source
         */
        public WeightedValue(String value, double weight, String source) {

            m_value = value;
            m_source = source;
            m_weight = weight;
        }

        /**
         * Gets the source of the value (for debugging).
         *
         * @return the source of the value
         */
        public String getSource() {

            return m_source;
        }

        /**
         * Gets the value.
         *
         * @return value
         */
        public String getValue() {

            return m_value;
        }

        /**
         * Gets the weight of the value.
         *
         * @return the weight of the value
         */
        public double getWeight() {

            return m_weight;
        }
    }

    /** XML attribute name. */
    public static final String A_NAME = "name";

    /** XML attribute name. */
    public static final String A_WEIGHT = "weight";

    /** Default weight, if not defined in parameter file. */
    public static final double DEFAULT_WEIGHT = 100.0;

    /** XML node name. */
    public static final String N_PARAM = "param";

    /** Module parameter for registering parameter files. */
    public static final String PARAM_PARAMFILE = "paramfile";

    /** The global parameter store instance. */
    private static final CmsParameterStore INSTANCE = new CmsParameterStore();

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsParameterStore.class);

    /**
     * Gets the global instance.
     *
     * @return the global instance
     */
    public static CmsParameterStore getInstance() {

        return INSTANCE;
    }

    /**
     * Helper method for parsing a parameter file from a byte array.
     *
     * @param data the binary data for the parameter file
     * @param source the source identifier
     * @return the of parameters
     * @throws CmsXmlException if something goes wrong
     */
    public static Map<String, WeightedValue> parse(byte[] data, String source) throws CmsXmlException {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        Document doc;
        // don't want missing DTD reference to cause a hard error, so we just warn on validation
        // errors and reparse without validation
        try {
            doc = CmsXmlUtils.unmarshalHelper(data, resolver, /*validate=*/true);
        } catch (CmsXmlException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            doc = CmsXmlUtils.unmarshalHelper(data, resolver, false);
        }
        return parse(doc.getRootElement(), source);
    }

    /**
     * Helper method for parsing a parameter file from a VFS resource.
     *
     * @param cms the CmsObject
     * @param path the path of the resource
     * @return the map of parameters
     *
     * @throws CmsException if something goes wrong
     */
    public static Map<String, WeightedValue> parse(CmsObject cms, String path) throws CmsException {

        CmsFile file = cms.readFile(path, CmsResourceFilter.IGNORE_EXPIRATION);
        return parse(file.getContents(), file.getRootPath());

    }

    /**
     * Parses a parameter file from an XML element.
     *
     * @param rootElem the root element of the XML
     * @param source the source identifier
     *
     * @return the parameter map
     */
    public static Map<String, WeightedValue> parse(Element rootElem, String source) {

        double defaultWeight = DEFAULT_WEIGHT;
        String defaultWeightStr = rootElem.attributeValue(A_WEIGHT);
        Map<String, WeightedValue> result = new HashMap<>();

        if (defaultWeightStr != null) {
            try {
                defaultWeight = Double.parseDouble(defaultWeightStr);
            } catch (NumberFormatException e) {
                LOG.error(source + ":" + e.getLocalizedMessage(), e);
            }
        }
        for (Node node : rootElem.selectNodes(N_PARAM)) {
            Element paramElem = (Element)node;
            String nameStr = paramElem.attributeValue(A_NAME);
            String weightStr = paramElem.attributeValue(A_WEIGHT);
            String content = paramElem.getText();
            if (nameStr == null) {
                LOG.error("Missing name attribute in " + source);
                continue;
            }
            double weight = defaultWeight;
            if (weightStr != null) {
                Double weightObj = Doubles.tryParse(weightStr);
                if (weightObj != null) {
                    weight = weightObj.doubleValue();
                }
            }
            WeightedValue val = new WeightedValue(content, weight, source);
            result.put(nameStr, val);
        }
        return result;
    }

    /**
     * Gets the string value with the maximal weight for the parameter with the given key.
     *
     * @param cms the CMS context
     * @param key the key
     *
     * @return the string value with maximal weight
     */
    public String getValue(CmsObject cms, String key) {

        WeightedValue val = getWeightedValue(cms, key);
        return val == null ? null : val.getValue();
    }

    /**
     * Finds the value with the maximal weight for the given key.
     *
     * @param cms the CMS context
     * @param key the parameter key
     *
     * @return the value with the maximal weight
     */
    public WeightedValue getWeightedValue(CmsObject cms, String key) {

        return getConfigurations(cms).stream().map(m -> m.get(key)).filter(val -> val != null).max(
            (v1, v2) -> Double.compare(v1.getWeight(), v2.getWeight())).orElse(null);
    }

    /**
     * Retrieves data for all registered parameter files.
     *
     * @param cms the CMS context
     * @return the list of parameter maps from all registered files
     */
    private List<Map<String, WeightedValue>> getConfigurations(CmsObject cms) {

        List<CmsModule> modules = OpenCms.getModuleManager().getAllInstalledModules();
        List<Map<String, WeightedValue>> result = new ArrayList<>();
        for (CmsModule module : modules) {
            String paramConfigsStr = module.getParameter(PARAM_PARAMFILE);
            if (paramConfigsStr != null) {
                String[] paths = paramConfigsStr.trim().split(" *, *");
                for (String path : paths) {
                    result.add(getConfigurationWithCache(cms, path));
                }
            }
        }
        return result;
    }

    /**
     * Gets the configuration for a VFS path, and uses a cache for the results.
     *
     * @param cms the CMS context
     * @param path the path
     * @return the parameter map
     */
    @SuppressWarnings("unchecked")
    private Map<String, WeightedValue> getConfigurationWithCache(CmsObject cms, String path) {

        String rootPath = cms.getRequestContext().addSiteRoot(path);
        Map<String, WeightedValue> result;
        result = (Map<String, WeightedValue>)CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().getCachedObject(
            cms,
            rootPath);
        if (result == null) {
            try {
                result = parse(cms, path);
            } catch (CmsException e) {
                LOG.info(path + ": " + e.getLocalizedMessage(), e);
                result = new HashMap<>();
            }
            CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().putCachedObject(cms, rootPath, result);
        }
        return result;

    }

}
