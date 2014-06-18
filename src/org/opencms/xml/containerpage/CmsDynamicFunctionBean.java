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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A class which contains the data parsed from a dynamic function XML content.<p>
 */
public class CmsDynamicFunctionBean {

    /**
     * A bean which contains a single format for a dynamic function, which contains of the
     * function JSP, the container settings and the parameters.
     */
    public static class Format {

        /** The structure id of the function jsp. */
        private CmsUUID m_jspStructureId;

        /** The max width string of the container settings. */
        private String m_maxWidth;

        /** The min width string of the container settings. */
        private String m_minWidth;

        /** Flag to indicate whether there are no container settings. */
        private boolean m_noContainerSettings;

        /** The parameters for the JSP. */
        private Map<String, String> m_parameters = new HashMap<String, String>();

        /** Container type of the container settings. */
        private String m_type;

        /**
         * Creates a new format instance.<p>
         * 
         * @param structureId the structure id of the JSP 
         * @param type the container type 
         * @param minWidth the minimum width 
         * @param maxWidth the maximum width 
         * 
         * @param parameters the JSP parameters 
         */
        public Format(CmsUUID structureId, String type, String minWidth, String maxWidth, Map<String, String> parameters) {

            m_jspStructureId = structureId;
            m_type = type;
            m_minWidth = minWidth;
            m_maxWidth = maxWidth;
            m_parameters = parameters;
        }

        /**
         * Returns the structure id of the JSP.<p>
         * 
         * @return the structure id of the JSP 
         */
        public CmsUUID getJspStructureId() {

            return m_jspStructureId;
        }

        /**
         * Returns the maximum width.<p>
         * 
         * @return the maximum width 
         */
        public String getMaxWidth() {

            return m_maxWidth;
        }

        /**
         * Returns the minimum width.<p>
         * 
         * @return the minimum width 
         */
        public String getMinWidth() {

            return m_minWidth;
        }

        /**
         * Returns the map of parameters for the JSP.<p>
         * 
         * @return the map of parameters for the JSP 
         **/
        public Map<String, String> getParameters() {

            return m_parameters;
        }

        /**
         * Gets the container type.<p>
         * 
         * @return the container type 
         */
        public String getType() {

            return m_type;
        }

        /**
         * Returns true if this format has no container settings.<p>
         *  
         * @return true if this format has no container settings 
         */
        public boolean hasNoContainerSettings() {

            return m_noContainerSettings;
        }

        /**
         * Sets the parameters which should be used if the format has no parameters of its own.<p>
         * 
         * @param parameters the default parameters 
         */
        protected void setDefaultParameters(Map<String, String> parameters) {

            if (m_parameters.isEmpty()) {
                m_parameters = parameters;
            }
        }

        /**
         * Sets the flag to indicate that this format has no  container settings.<p>
         * 
         * @param noContainerSettings the new value for the noContainerSettings flag 
         */
        protected void setNoContainerSettings(boolean noContainerSettings) {

            m_noContainerSettings = true;
        }
    }

    /** The path of the formatter which calls the JSP. */
    public static final String FORMATTER_PATH = "/system/modules/org.opencms.ade.containerpage/formatters/function.jsp";

    /** The function formatter resource. */
    private CmsResource m_functionFormatter;

    /** The primary format of the dynamic function. */
    private Format m_mainFormat;

    /** The other formats of the dynamic function.*/
    private List<Format> m_otherFormats;

    /** The resource from which the dynamic function bean has been read. */
    private CmsResource m_resource;

    /** The setting configuration for the dynamic function. */
    private Map<String, CmsXmlContentProperty> m_settingConfig;

    /**
     * Creates a new dynamic function bean.<p>
     * 
     * @param mainFormat the primary format 
     * @param otherFormats the list of other formats 
     * @param settingConfig the setting configuration 
     * @param resource the resource from which the dynamic function bean has been read 
     * @param functionFormatter the generic formatter for dynamic functions 
     */
    public CmsDynamicFunctionBean(
        Format mainFormat,
        List<Format> otherFormats,
        Map<String, CmsXmlContentProperty> settingConfig,
        CmsResource resource,
        CmsResource functionFormatter) {

        m_mainFormat = mainFormat;
        m_otherFormats = otherFormats;
        m_settingConfig = settingConfig;
        for (Format format : otherFormats) {
            format.setDefaultParameters(mainFormat.getParameters());
        }
        m_resource = resource;
        m_functionFormatter = functionFormatter;
    }

    /**
     * Finds the correct format for a given container type and width.<p>
     * 
     * @param cms the current CMS context 
     * @param type the container type 
     * @param width the container width 
     * 
     * @return the format for the given container type and width 
     */
    public Format getFormatForContainer(CmsObject cms, String type, int width) {

        IdentityHashMap<CmsFormatterBean, Format> formatsByFormatter = new IdentityHashMap<CmsFormatterBean, Format>();
        // relate formatters to formats so we can pick the corresponding format after a formatter has been selected  
        CmsFormatterBean mainFormatter = createFormatterBean(m_mainFormat, true);
        formatsByFormatter.put(mainFormatter, m_mainFormat);
        List<I_CmsFormatterBean> formatters = new ArrayList<I_CmsFormatterBean>();
        for (Format format : m_otherFormats) {
            CmsFormatterBean formatter = createFormatterBean(format, false);
            formatsByFormatter.put(formatter, format);
            formatters.add(formatter);
        }
        formatters.add(0, mainFormatter);
        CmsFormatterConfiguration formatterConfiguration = CmsFormatterConfiguration.create(cms, formatters);
        I_CmsFormatterBean matchingFormatter = formatterConfiguration.getDefaultFormatter(type, width);
        if (matchingFormatter == null) {
            return null;
        }
        return formatsByFormatter.get(matchingFormatter);
    }

    /**
     * Creates the formatter list for this dynamic function.<p>
     *  
     * @return the formatter list for this dynamic function 
     */
    public List<CmsFormatterBean> getFormatters() {

        CmsFormatterBean mainFormatter = createFormatterBean(m_mainFormat, true);
        List<CmsFormatterBean> formatters = new ArrayList<CmsFormatterBean>();
        formatters.add(mainFormatter);
        for (Format format : m_otherFormats) {
            formatters.add(createFormatterBean(format, false));
        }
        return formatters;
    }

    /**
     * Gets the generic function formatter resource.<p>
     * 
     * @return the generic function formatter resource 
     */
    public CmsResource getFunctionFormatter() {

        return m_functionFormatter;
    }

    /**
     * Gets the main format.<p>
     * 
     * @return the main format 
     */
    public Format getMainFormat() {

        return m_mainFormat;
    }

    /** 
     * Returns the setting configuration for this dynamic function.<p>
     * 
     * @return the setting configuration for this dynamic function 
     */
    public Map<String, CmsXmlContentProperty> getSettings() {

        return m_settingConfig;
    }

    /**
     * Helper method to create a formatter bean from a format.<p>
     * 
     * @param format the format bean 
     * @param isPreview if true, the formatter returned will be marked as a preview formatter 
     * 
     * @return the formatter corresponding to the format 
     */
    protected CmsFormatterBean createFormatterBean(Format format, boolean isPreview) {

        if (format.hasNoContainerSettings()) {
            return new CmsFormatterBean(
                FORMATTER_PATH,
                m_functionFormatter.getStructureId(),
                m_resource.getRootPath(),
                isPreview);
        } else {
            CmsFormatterBean result = new CmsFormatterBean(
                format.getType(),
                FORMATTER_PATH,
                format.getMinWidth(),
                format.getMaxWidth(),
                "" + isPreview,
                "false",
                m_resource.getRootPath());
            result.setJspStructureId(m_functionFormatter.getStructureId());
            return result;
        }
    }

}
