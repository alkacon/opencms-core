/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/modules/Attic/CmsTemplateContentListItem.java,v $
 * Date   : $Date: 2005/04/06 11:36:25 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.frontend.templateone.modules;

import org.opencms.frontend.templateone.CmsTemplateBean;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

/**
 * Holds information of a single XMLContent list item, either for the page right side element or the center area.<p>
 * 
 * Use the newInstance() method with correct arguments to create a fully configured item object for list generation.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsTemplateContentListItem {
    
    /** The display area of the list: center. */
    public static final String C_DISPLAYAREA_CENTER = "center";
    /** The display area of the list: left. */
    public static final String C_DISPLAYAREA_LEFT = "right";
    /** The display area of the list: right. */
    public static final String C_DISPLAYAREA_RIGHT = "right";
    
    /** Macro used in property definition names to resolve the list index. */
    public static final String C_MACRO_LISTINDEX = "index";
    /** Macro used in folder String to represent the microsite folder. */
    public static final String C_MACRO_MICROSITEFOLDER = "microsite.folder";
    
    /** Name of the property key to set the element collector. */
    public static final String C_PROPERTY_LAYOUT_COLLECTOR = "layout.${" + C_MACRO_LISTINDEX + "}.collector";
    /** Name of the property key to set the element count. */
    public static final String C_PROPERTY_LAYOUT_COUNT = "layout.${" + C_MACRO_LISTINDEX + "}.count";
    /** Name of the property key to set the folder holding the contents. */
    public static final String C_PROPERTY_LAYOUT_FOLDER = "layout.${" + C_MACRO_LISTINDEX + "}.folder";
    /** Name of the property key to set the element type. */
    public static final String C_PROPERTY_LAYOUT_TYPE = "layout.${" + C_MACRO_LISTINDEX + "}.type";
    
    private String m_collector;
    private int m_count;
    private String m_displayArea;
    private String m_folder;
    private String m_listElement;
    private String m_type;
    
    /**
     * Factory method to create a new {@link CmsTemplateContentListItem} instance.<p>
     * 
     * If no type for the specified index can be found, null is returned.<p>
     * 
     * @param messages the workplace messages holding the default values for collector and count
     * @param properties the properties to use to get the information
     * @param microSiteFolder the folder URI of the current microsite
     * @param displayArea the area where to build the lists (left, center or right)
     * @param index the index of the content list item to create
     * 
     * @return a new instance of a {@link CmsTemplateContentListItem}
     */
    public static CmsTemplateContentListItem newInstance(CmsWorkplaceMessages messages, Map properties, String microSiteFolder, String displayArea, int index) {

        // initialize a macro resolver to resolve the current index and the microsite folder
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro(C_MACRO_LISTINDEX, Integer.toString(index));
        resolver.addMacro(C_MACRO_MICROSITEFOLDER, microSiteFolder);
        
        // try to get the list type from the properties
        String type = (String)properties.get(resolver.resolveMacros(C_PROPERTY_LAYOUT_TYPE));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(type) && !"none".equals(type)) {
            // type is specified, create new instance and fill members
            CmsTemplateContentListItem listItem = new CmsTemplateContentListItem();
            listItem.setType(type);
            // create workplace messages key prefix
            StringBuffer keyPrefix = new StringBuffer(8);
            keyPrefix.append("layout.").append(type).append(".").append(displayArea);
            // determine the collector name to use
            String collector = (String)properties.get(resolver.resolveMacros(C_PROPERTY_LAYOUT_COLLECTOR));
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(collector)) {
                collector = messages.key(keyPrefix + ".collector");
            }
            listItem.setCollector(collector);
            // determine the count of contents to display
            String count = (String)properties.get(resolver.resolveMacros(C_PROPERTY_LAYOUT_COUNT));
            String defaultCount = messages.key(keyPrefix + ".count");
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
            String folder = (String)properties.get(resolver.resolveMacros(C_PROPERTY_LAYOUT_FOLDER));
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(folder)) {
                folder = messages.key(keyPrefix + ".folder");
            }
            listItem.setFolder(resolver.resolveMacros(folder));
            // set the JSP element uri creating the list
            String listElement = messages.key(keyPrefix + ".listelement");
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
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getType() {

        return m_type;
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

        Map properties = new HashMap(5);
        properties.put(CmsTemplateModules.C_PARAM_COLLECTOR, getCollector());
        properties.put(CmsTemplateModules.C_PARAM_COUNT, Integer.toString(getCount()));
        if (showPageLinks) {
            properties.put(CmsTemplateModules.C_PARAM_ELEMENTCOUNT, Integer.toString(Integer.MAX_VALUE));
        } else {
            properties.put(CmsTemplateModules.C_PARAM_ELEMENTCOUNT, Integer.toString(getCount()));
        }
        properties.put(CmsTemplateModules.C_PARAM_FOLDER, getFolder());
        properties.put(CmsTemplateBean.C_PARAM_SITE, jsp.getRequestContext().getSiteRoot());
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
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    protected void setType(String type) {

        m_type = type;
    }
}