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

package org.opencms.xml.containerpage;

import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Interface representing a configured formatter.<p>
 */
public interface I_CmsFormatterBean {

    /**
     * Returns the formatter container type.<p>
     *
     * If this is "*", then the formatter is a width based formatter.<p>
     *
     * @return the formatter container type
     */
    Set<String> getContainerTypes();

    /**
     * Gets the CSS head includes.<p>
     *
     * @return the CSS head includes
     */
    Set<String> getCssHeadIncludes();

    /**
     * Returns the id of this formatter.<p>
     *
     * This method may return null because the id is not always defined for formatters, e.g. for those formatters declared in a schema.<p>
     *
     * @return the formatter id
     */
    String getId();

    /**
     * Gets the inline CSS snippets.<p>
     *
     * @return the inline CSS snippets
     */
    String getInlineCss();

    /**
     * Gets the inline JS snippets.<p>
     *
     * @return the inline JS snippets
     */
    String getInlineJavascript();

    /**
     * Gets the Javascript head includes.<p>
     *
     * @return the head includes
     */
    List<String> getJavascriptHeadIncludes();

    /**
     * Returns the root path of the formatter JSP in the OpenCms VFS.<p>
     *
     * @return the root path of the formatter JSP in the OpenCms VFS.<p>
     */
    String getJspRootPath();

    /**
     * Returns the structure id of the JSP resource for this formatter.<p>
     *
     * @return the structure id of the JSP resource for this formatter
     */
    CmsUUID getJspStructureId();

    /**
     * Returns the location this formatter was defined in.<p>
     *
     * This will be an OpenCms VFS root path, either to the XML schema XSD, or the
     * configuration file this formatter was defined in, or to the JSP that
     * makes up this formatter.<p>
     *
     * @return the location this formatter was defined in
     */
    String getLocation();

    /**
     * Returns the maximum formatter width.<p>
     *
     * If this is not set, then {@link Integer#MAX_VALUE} is returned.<p>
     *
     * @return the maximum formatter width
     */
    int getMaxWidth();

    /**
     * Returns the minimum formatter width.<p>
     *
     * If this is not set, then <code>-1</code> is returned.<p>
     *
     * @return the minimum formatter width
     */
    int getMinWidth();

    /**
     * Gets the nice name for this formatter.<p>
     *
     * @param locale the locale
     *
     * @return the nice name for this formatter
     */
    String getNiceName(Locale locale);

    /**
     * Gets the rank.<p>
     *
     * @return the rank
     */
    int getRank();

    /**
     * Gets the resource type names.<p>
     *
     * @return the resource type names
     */
    Collection<String> getResourceTypeNames();

    /**
     * Gets the defined settings.<p>
     *
     * @return the defined settings
     */
    Map<String, CmsXmlContentProperty> getSettings();

    /**
     * Returns if this formatter has nested containers.<p>
     *
     * @return <code>true</code> if this formatter has nested containers
     */
    boolean hasNestedContainers();

    /**
     * Returns true if the formatter is automatically enabled.<p>
     *
     * @return true if the formatter is automatically enabled
     */
    boolean isAutoEnabled();

    /**
     * Returns true if the formatter can be used for detail views.<p>
     *
     * @return true if the formatter can be used for detail views
     */
    boolean isDetailFormatter();

    /**
     * Returns whether this formatter should be used by the 'display' tag.<p>
     *
     * @return <code>true</code> if this formatter should be used by the 'display' tag
     */
    boolean isDisplayFormatter();

    /**
     * Returns true if the formatter is from a formatter configuration file.<p>
     *
     * @return formatter f
     */
    boolean isFromFormatterConfigFile();

    /**
     * Returns true if this formatter should match all type/width combinations.<p>
     *
     * @return true if this formatter should match all type/width combinations
     */
    boolean isMatchAll();

    /**
     * Indicates if this formatter is to be used as preview in the ADE gallery GUI.
     *
     * @return <code>true</code> if this formatter is to be used as preview in the ADE gallery GUI
     */
    boolean isPreviewFormatter();

    /**
     * Returns <code>true</code> in case an XML content formatted with this formatter should be included in the
     * online full text search.<p>
     *
     * @return <code>true</code> in case an XML content formatted with this formatter should be included in the
     * online full text search
     */
    boolean isSearchContent();

    /**
     * Returns <code>true</code> in case this formatter is based on type information.<p>
     *
     * @return <code>true</code> in case this formatter is based on type information
     */
    boolean isTypeFormatter();

    /**
     * Sets the JSP structure id.<p>
     *
     * @param structureId the jsp structure id
     */
    void setJspStructureId(CmsUUID structureId);

}