/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/Attic/CmsTemplateContentListItem.java,v $
 * Date   : $Date: 2005/07/07 16:25:27 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

/**
 * Holds information of a single XMLContent list item, either for the page side elements or the center area.<p>
 * 
 * Use the newInstance() method with correct arguments to create a fully configured item object for list generation.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsTemplateContentListItem {

    /** The display area of the list: center. */
    public static final String DISPLAYAREA_CENTER = "center";
    
    /** The display area of the list: left. */
    public static final String DISPLAYAREA_LEFT = "left";
    
    /** The display area of the list: right. */
    public static final String DISPLAYAREA_RIGHT = "right";

    /** The possible display areas for the template. */
    public static final String[] DISPLAYAREAS = {DISPLAYAREA_CENTER, DISPLAYAREA_LEFT, DISPLAYAREA_RIGHT};

    /** The name of the list variation: long. */
    public static final String LISTVARIATION_LONG = "long";
    
    /** The name of the list variation: short. */
    public static final String LISTVARIATION_SHORT = "short";

    /** The possible list variations for a content type. */
    public static final String[] LISTVARIATIONS = {"", '.' + LISTVARIATION_LONG, '.' + LISTVARIATION_SHORT};

    /** Macro used in property definition names to resolve the list index. */
    public static final String MACRO_LISTINDEX = "index";
    
    /** Macro used in folder String to represent the microsite folder. */
    public static final String MACRO_MICROSITEFOLDER = "microsite.folder";

    /** Request parameter name for the collector. */
    public static final String PARAM_COLLECTOR = "collector";
    
    /** Request parameter name for the list count. */
    public static final String PARAM_COUNT = "count";
    
    /** Request parameter name maximum number of elements to show. */
    public static final String PARAM_ELEMENTCOUNT = "elementcount";
    
    /** Request parameter name for the xmlcontent folder. */
    public static final String PARAM_FOLDER = "folder";
    
    /** Request parameter name for the xmlcontent listelement. */
    public static final String PARAM_LISTELEMENT = "listelement";

    /** Name of the property key to set the element collector. */
    public static final String PROPERTY_LAYOUT_COLLECTOR = "layout.${" + MACRO_LISTINDEX + "}." + PARAM_COLLECTOR;
    
    /** Name of the property key to set the element count. */
    public static final String PROPERTY_LAYOUT_COUNT = "layout.${" + MACRO_LISTINDEX + "}." + PARAM_COUNT;
    
    /** Name of the property key to set the folder holding the contents. */
    public static final String PROPERTY_LAYOUT_FOLDER = "layout.${" + MACRO_LISTINDEX + "}." + PARAM_FOLDER;
    
    /** Name of the property key to set the element type. */
    public static final String PROPERTY_LAYOUT_TYPE = "layout.${" + MACRO_LISTINDEX + "}.type";
    
    /** Name of the property key to set the list variation. */
    public static final String PROPERTY_LAYOUT_VARIATION = "layout.${" + MACRO_LISTINDEX + "}.variation";

    /** The property value for displaying no list. */
    public static final String PROPERTY_VALUE_NONE = "none";

    private String m_collector;
    private int m_count;
    private String m_displayArea;
    private String m_folder;
    private String m_listElement;
    private String m_type;
    private String m_variation;

    /**
     * Creates a Map holding the default values for the list creation from given workplace messages.<p>
     * 
     * @param messages the localized workplace messages
     * 
     * @return the default values for the list creation from given workplace messages
     */
    public static Map getDefaultValuesFromMessages(CmsMessages messages) {

        HashMap result = new HashMap();
        List resTypes = OpenCms.getResourceManager().getResourceTypes();

        for (int i = 0; i < resTypes.size(); i++) {
            I_CmsResourceType type = (I_CmsResourceType)resTypes.get(i);
            if (type.isAdditionalModuleResourceType()) {
                // only look for keys if the resource type is an additional one
                for (int k = 0; k < DISPLAYAREAS.length; k++) {
                    // loop over the possible display areas
                    String area = DISPLAYAREAS[k];
                    String typeName = type.getTypeName();
                    StringBuffer keyPrefix = new StringBuffer(8);
                    keyPrefix.append("layout.").append(typeName).append('.').append(area).append('.');
                    // get collector default
                    String key = keyPrefix + PARAM_COLLECTOR;
                    String value = messages.key(key);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)
                        || value.startsWith(CmsMessages.UNKNOWN_KEY_EXTENSION)) {
                        // no default collector found, this indicates no list specifications for this type are present
                        continue;
                    }
                    result.put(key, value);
                    // get the default count
                    key = keyPrefix + PARAM_COUNT;
                    result.put(key, messages.key(key));
                    // get the default folder
                    key = keyPrefix + PARAM_FOLDER;
                    result.put(key, messages.key(key));
                    // get the possible list element JSPs
                    for (int m = 0; m < LISTVARIATIONS.length; m++) {
                        key = keyPrefix + PARAM_LISTELEMENT + LISTVARIATIONS[m];
                        value = messages.key(key);
                        if (!value.startsWith(CmsMessages.UNKNOWN_KEY_EXTENSION)) {
                            result.put(key, value);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Factory method to create a new {@link CmsTemplateContentListItem} instance.<p>
     * 
     * If no type for the specified index can be found, null is returned.<p>
     * 
     * @param defaultValues holds the default values for collector, count, list folder, JSP element
     * @param properties the properties of the layout file to use to get the list information
     * @param microSiteFolder the folder URI of the current microsite
     * @param displayArea the area where to build the lists (left, center or right)
     * @param index the index of the content list item to create
     * 
     * @return a new instance of a {@link CmsTemplateContentListItem}
     */
    public static CmsTemplateContentListItem newInstance(
        Map defaultValues,
        Map properties,
        String microSiteFolder,
        String displayArea,
        int index) {

        // initialize a macro resolver to resolve the current index and the microsite folder
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro(MACRO_LISTINDEX, Integer.toString(index));
        resolver.addMacro(MACRO_MICROSITEFOLDER, microSiteFolder);

        // try to get the list type from the properties
        String type = (String)properties.get(resolver.resolveMacros(PROPERTY_LAYOUT_TYPE));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(type) && !PROPERTY_VALUE_NONE.equals(type)) {
            // type is specified, create new instance and fill members
            CmsTemplateContentListItem listItem = new CmsTemplateContentListItem();
            listItem.setType(type);
            // create workplace messages key prefix
            StringBuffer keyPrefix = new StringBuffer(8);
            keyPrefix.append("layout.").append(type).append('.').append(displayArea).append('.');
            // determine the collector name to use
            String collector = (String)properties.get(resolver.resolveMacros(PROPERTY_LAYOUT_COLLECTOR));
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(collector)) {
                collector = (String)defaultValues.get(keyPrefix + PARAM_COLLECTOR);
            }
            listItem.setCollector(collector);
            // determine the count of contents to display
            String count = (String)properties.get(resolver.resolveMacros(PROPERTY_LAYOUT_COUNT));
            String defaultCount = (String)defaultValues.get(keyPrefix + PARAM_COUNT);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(count)) {
                count = defaultCount;
            }
            try {
                listItem.setCount(Integer.parseInt(count));
            } catch (NumberFormatException e) {
                // no valid number specified, use default value
                listItem.setCount(Integer.parseInt(defaultCount));
            }
            // determine the folder which holds the contents
            String folder = (String)properties.get(resolver.resolveMacros(PROPERTY_LAYOUT_FOLDER));
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(folder)) {
                folder = (String)defaultValues.get(keyPrefix + PARAM_FOLDER);
            }
            listItem.setFolder(resolver.resolveMacros(folder));
            // determine the list variation to use
            String variation = (String)properties.get(resolver.resolveMacros(PROPERTY_LAYOUT_VARIATION));
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(variation)) {
                variation = "";
            } else {
                variation = '.' + variation;
            }
            // set the JSP element uri creating the list considering the list variation
            String listElement = (String)defaultValues.get(keyPrefix + PARAM_LISTELEMENT + variation);
            listItem.setListElement(listElement);
            // set the display area of the list
            listItem.setDisplayArea(displayArea);
            return listItem;
        } else {
            // no type specified, return null
            return null;
        }
    }

    /**
     * Returns the collector to use for this list.<p>
     *
     * @return the collector to use for this list
     */
    public String getCollector() {

        return m_collector;
    }

    /**
     * Returns the maximum count of entries for elements.<p>
     *
     * @return the maximum count of entries for elements
     */
    public int getCount() {

        return m_count;
    }

    /**
     * Returns the display area for this XMLContent list.<p>
     * 
     * @return the display area for this XMLContent list
     */
    public String getDisplayArea() {

        return m_displayArea;
    }

    /**
     * Returns the folder which holds the XMLContent.<p>
     *
     * @return the folder which holds the XMLContent
     */
    public String getFolder() {

        return m_folder;
    }

    /**
     * Returns the list element URI to use to display the list.<p>
     * 
     * @return the list element URI to use to display the list
     */
    public String getListElement() {

        return m_listElement;
    }

    /**
     * Returns the XMLContent type.<p>
     *
     * @return the XMLContent type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the list variation.<p>
     *
     * @return the list variation
     */
    public String getVariation() {

        return m_variation;
    }

    /**
     * Includes the list generation JSP and creates the necessary request parameters for the included JSP.<p>
     * 
     * @param jsp the JSP action element
     * @param showPageLinks flag to determine if page links should be shown
     * 
     * @throws JspException if inclusion of the element fails
     */
    public void includeListItem(CmsJspActionElement jsp, boolean showPageLinks) throws JspException {

        Map properties = new HashMap(4);
        properties.put(PARAM_COLLECTOR, getCollector());
        properties.put(PARAM_COUNT, Integer.toString(getCount()));
        if (showPageLinks) {
            properties.put(PARAM_ELEMENTCOUNT, Integer.toString(Integer.MAX_VALUE));
        } else {
            properties.put(PARAM_ELEMENTCOUNT, Integer.toString(getCount()));
        }
        properties.put(PARAM_FOLDER, getFolder());
        jsp.include(getListElement(), null, properties);
    }

    /**
     * Sets the collector to use for this list.<p>
     *
     * @param collector the collector to use for this list
     */
    protected void setCollector(String collector) {

        m_collector = collector;
    }

    /**
     * Sets the maximum count of entries for elements.<p>
     *
     * @param count the maximum count of entries for elements
     */
    protected void setCount(int count) {

        m_count = count;
    }

    /**
     * Sets the display area for this XMLContent list.<p>
     * 
     * @param displayArea the display area for this XMLContent list
     */
    protected void setDisplayArea(String displayArea) {

        m_displayArea = displayArea;
    }

    /**
     * Sets the folder which holds the XMLContent.<p>
     *
     * @param contentFolder the folder which holds the XMLContent
     */
    protected void setFolder(String contentFolder) {

        m_folder = contentFolder;
    }

    /**
     * Sets the list element URI to use to display the list.<p>
     * 
     * @param listElement the list element URI to use to display the list
     */
    protected void setListElement(String listElement) {

        m_listElement = listElement;
    }

    /**
     * Sets the XMLContent type.<p>
     *
     * @param type the XMLContent type to set
     */
    protected void setType(String type) {

        m_type = type;
    }

    /**
     * Sets the list variation.<p>
     *
     * @param variation the list variation to set
     */
    protected void setVariation(String variation) {

        m_variation = variation;
    }
}