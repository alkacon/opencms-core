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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.plugins.CmsTemplatePlugin;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Interface representing a configured formatter.<p>
 */
public interface I_CmsFormatterBean {

    /**
     * Gets the set of alias keys for the formatter.
     *
     * @return the set of alias keys
     */
    Set<String> getAliasKeys();

    /**
     * Gets the complete set of keys for the formatter, i.e. its main key and all alias keys.
     *
     * @return the complete set of keys
     */
    Set<String> getAllKeys();

    /**
     * Gets the map of attributes.<p>
     *
     * @return the attribute map
     */
    Map<String, String> getAttributes();

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
     * Gets the formatter description.<p>
     *
     * If a locale is passed in, macros in the configured description will be resolved with a macro resolver set to that locale.
     * If null is passed in as a locale, the raw configured description will be returned.
     *
     * @param locale the locale (may be null)
     *
     * @return the formatter description
     */
    String getDescription(Locale locale);

    /**
     * The display type of this formatter or <code>null</code> in case this is not a display formatter.<p>
     *
     * @return the display type
     */
    String getDisplayType();

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
     * Gets the formatter key, or null if no formatter key is set.
     *
     * <p>A formatter key is used to allow dynamic switching between formatters with the same key by enabling/disabling the formatters
     * in the sitemap configuration. I.e. if a formatter referenced in a container page has been disabled in the sitemap configuration,
     * but a different formatter with the same key is enabled, the second formatter will be used instead when rendering the page .
     *
     * @return the formatter key, or null
     */
    String getKey();

    /**
     * Helper method for getting either the key, if it exists, or the ID (as a string) if it does not.
     *
     * @return the formatter key or id
     */
    default String getKeyOrId() {

        if (getKey() != null) {
            return getKey();
        }
        return getId();
    }

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
     * Returns the meta mappings.<p>
     *
     * @return the meta mappings
     */
    List<CmsMetaMapping> getMetaMappings();

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
     * @param sitemapConfig the sitemap configuration for which the settings should be retrieved
     *
     * @return the defined settings
     */
    Map<String, CmsXmlContentProperty> getSettings(CmsADEConfigData sitemapConfig);

    /**
     * Gets the template plugins.
     *
     * @return the template plugins
     */
    List<CmsTemplatePlugin> getTemplatePlugins();

    /**
     * Returns if nested formatter settings should be displayed.<p>
     *
     * @return <code>true</code> if nested formatter settings should be displayed
     */
    boolean hasNestedFormatterSettings();

    /**
     * Returns whether this formatter allows settings to be edited in the content editor.<p>
     *
     * @return <code>true</code> in case editing the settings is allowed in the content editor
     */
    boolean isAllowsSettingsInEditor();

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

    /**
     * Returns true if meta mappings should be evaluated for normal container elements using this formatter, not just detail elements.<p>
     *
     * @return true if meta mappings should be evaluated for normal container elements
     */
    boolean useMetaMappingsForNormalElements();

    /**
     * If possible, returns a formatter bean that is basically a copy of this one, but has the keys supplied
     * as a parameter.
     *
     * <p>Note that this only works for formatters which already have a key, and can not replace the main key. If the keys
     * already match, the current instance may be returned rather than a copy.
     *
     * <p>If the formatter bean implementation does not support key replacement, or an error occurs, an empty Optional is returned
     *
     * @param keys the keys to use (should include the current key of the formatter
     * @return the copy with the replaced keys
     */
    Optional<I_CmsFormatterBean> withKeys(Collection<String> keys);

}