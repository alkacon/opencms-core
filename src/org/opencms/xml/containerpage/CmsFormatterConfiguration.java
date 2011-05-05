/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/CmsFormatterConfiguration.java,v $
 * Date   : $Date: 2011/05/05 08:16:50 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Represents a formatter configuration.<p>
 * 
 * A formatter configuration can be either defined in the XML schema XSD of a XML content, 
 * or in a special sitemap configuration file.<p>
 * 
 * @author Georg Westenberger
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsFormatterConfiguration {

    /** The log instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsFormatterConfiguration.class);

    /** All formatters that have been added to this configuration. */
    private List<CmsFormatterBean> m_allFormatters;

    /** Indicates if this configuration has been frozen. */
    private boolean m_frozen;

    /** The formatter that is to be used for the preview in the ADE gallery GUI. */
    private CmsFormatterBean m_previewFormatter;

    /** The formatters for different container types. */
    private Map<String, CmsFormatterBean> m_typeFormatters;

    /** The formatters for different widths. */
    private List<CmsFormatterBean> m_widthFormatters;

    /** 
     * Create a new formatter configuration.<p>
     */
    public CmsFormatterConfiguration() {

        m_typeFormatters = new HashMap<String, CmsFormatterBean>(4);
        m_widthFormatters = new ArrayList<CmsFormatterBean>(4);
    }

    /**
     * Adds a formatter to this configuration.<p>
     * 
     * @param formatter the formatter to add
     * 
     * @throws CmsConfigurationException in case this formatter configuration is already frozen
     */
    public void addFormatter(CmsFormatterBean formatter) throws CmsConfigurationException {

        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_FORMATTER_CONFIG_FROZEN_0));
        }
        m_allFormatters.add(formatter);
    }

    /**
     * Freezes this configuration.<p>
     * 
     * @param cms an OpenCms user context that is used to validate the JSP root paths entries
     */
    public void freeze(CmsObject cms) {

        if (!m_frozen) {
            m_frozen = true;
            initFormatters(cms);
        }
    }

    /**
     * Selects the matching formatter for the provided type and width from this configuration.<p>
     * 
     * This method first tries to find the formatter for the provided container type. 
     * If this fails, it returns the width based formatter that matched the container width.<p>
     * 
     * @param containerType the container type 
     * @param containerWidth the container width
     *  
     * @return the matching formatter, or <code>null</code> if none was found 
     */
    public CmsFormatterBean getFormatter(String containerType, int containerWidth) {

        if (CmsFormatterBean.isPreviewType(containerType)) {
            // the preview formatter was requested
            return getPreviewFormatter();
        }
        CmsFormatterBean result = m_typeFormatters.get(containerType);
        if ((result == null) && (containerWidth > 0)) {
            // in case we don't have found a type and width info is set, check for width formatters
            for (CmsFormatterBean f : m_widthFormatters) {
                // iterate all width containers and see if we have a fit
                if ((f.getMinWidth() <= containerWidth) && (containerWidth <= f.getMaxWidth())) {
                    // found a match
                    if ((result == null) || (result.getMinWidth() < f.getMinWidth())) {
                        result = f;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the formatter from this configuration that is to be used for the preview in the ADE gallery GUI.<p>
     * 
     * @return the formatter from this configuration that is to be used for the preview in the ADE gallery GUI
     */
    public CmsFormatterBean getPreviewFormatter() {

        if (m_previewFormatter == null) {
            // preview formatter has not been calculated yet
            CmsFormatterBean result = null;
            // check the width formatter if we have a matching width for the preview window
            result = getFormatter(CmsFormatterBean.WILDCARD_TYPE, CmsFormatterBean.PREVIEW_WIDTH);
            if ((result == null) && !m_widthFormatters.isEmpty()) {
                // no width is matching, see if we have one with a BIGGER width (preview will have scrollbars)
                for (CmsFormatterBean f : m_widthFormatters) {
                    // iterate all width containers and see if we have a fit
                    if ((f.getMinWidth() >= CmsFormatterBean.PREVIEW_WIDTH)
                        && (CmsFormatterBean.PREVIEW_WIDTH <= f.getMaxWidth())) {
                        // found a match
                        if ((result == null) || (result.getMinWidth() < f.getMinWidth())) {
                            result = f;
                        }
                    }
                }
            }
            if ((result == null) && !m_typeFormatters.isEmpty()) {
                // no luck with any width based formatter, let's just get the first type based formatter
                result = m_typeFormatters.values().iterator().next();
            }
            if (result == null) {
                // there are no usable previews configured at all, so use the stupid default preview formatter
                m_previewFormatter = CmsFormatterBean.getDefaultPreviewFormatter();
            }
            m_previewFormatter = result;
        }
        return m_previewFormatter;
    }

    /**
     * Returns the provided <code>true</code> in case this configuration has a formatter 
     * for the given type / width parameters.<p>
     * 
     * @param containerType the container type 
     * @param containerWidth the container width
     *  
     * @return the provided <code>true</code> in case this configuration has a formatter 
     *      for the given type / width parameters.
     */
    public boolean hasFormatter(String containerType, int containerWidth) {

        return getFormatter(containerType, containerWidth) != null;
    }

    /**
     * Returns <code>true</code> in case there is at least one formatter configured in this configuration.<p>
     * 
     * @return <code>true</code> in case there is at least one formatter configured in this configuration
     */
    public boolean hasFormatters() {

        return (m_typeFormatters.size() > 0) || (m_widthFormatters.size() > 0);
    }

    /**
     * Indicates if this configuration has been frozen.<p>
     * 
     * @return <code>true</code> if this configuration has been frozen
     */
    public boolean isFrozen() {

        return m_frozen;
    }

    /**
     * Initializes all formatters of this configuration.<p>
     * 
     * It is also checked if the configured JSP root path exists, if not the formatter is removed 
     * as it is unusable.<p>
     * 
     * @param cms the OpenCms user context to use for validating the JSP resources
     */
    private void initFormatters(CmsObject cms) {

        for (CmsFormatterBean formatter : m_allFormatters) {

            // first we make sure that the JSP exists at all (and also we read the UUID that way)
            CmsResource res = null;
            try {
                // first get a cms copy so we can mess up the context without modifying the original
                CmsObject cmsCopy = OpenCms.initCmsObject(cms);
                // switch to the root site
                cmsCopy.getRequestContext().setSiteRoot("");
                // now read the JSP
                res = cms.readResource(formatter.getJspRootPath());
            } catch (CmsException e) {
                //if this happens the result is null and we write a LOG error
            }
            if (!CmsResourceTypeJsp.isJsp(res)) {
                // the formatter must be a JSP
                LOG.error(Messages.get().getBundle().key(
                    Messages.ERR_FORMATTER_JSP_DONT_EXIST_1,
                    formatter.getJspRootPath()));
            }

            if (res != null) {

                formatter.setJspStructureId(res.getStructureId());

                String oldUri = null;
                Object key;
                if (formatter.isTypeFormatter()) {

                    String type = formatter.getContainerType();
                    key = type;
                    CmsFormatterBean oldFormatter = m_typeFormatters.get(type);
                    if (oldFormatter != null) {
                        oldUri = oldFormatter.getJspRootPath();
                    }
                    m_typeFormatters.put(type, formatter);
                } else {

                    Integer minWidth = Integer.valueOf(formatter.getMinWidth());
                    key = minWidth;
                    int old = m_widthFormatters.lastIndexOf(formatter);
                    if (old >= 0) {
                        oldUri = m_widthFormatters.remove(old).getJspRootPath();
                    }
                    m_widthFormatters.add(formatter);
                }
                if (formatter.isPreviewFormatter()) {
                    // this is a preview formatter, last one overwrites earlier one
                    m_previewFormatter = formatter;
                }
                if (oldUri != null) {
                    LOG.warn(Messages.get().getBundle().key(
                        Messages.LOG_CONTENT_DEFINITION_DUPLICATE_FORMATTER_4,
                        new Object[] {key, oldUri, formatter.getJspRootPath(), formatter.getLocation()}));
                }
            }
        }
        m_allFormatters = Collections.unmodifiableList(m_allFormatters);
        m_typeFormatters = Collections.unmodifiableMap(m_typeFormatters);
        m_widthFormatters = Collections.unmodifiableList(m_widthFormatters);
    }
}