/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsExplorerContextMenu.java,v $
 * Date   : $Date: 2004/03/10 16:50:35 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Provides methods to build a context menu for an explorer resource type.<p>
 * 
 * This object stores all entries which are displayed in a context menu in a sorted list.
 * The sort order is specified in an attribute of the context menu subnodes
 * in the OpenCms configuration.<p> 
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.3.3
 */
public class CmsExplorerContextMenu {

    private List m_allEntries;
    private HashMap m_generatedScripts;
    
    /**
     * Default constructor.<p>
     */
    public CmsExplorerContextMenu() {
        m_allEntries = new ArrayList();
        m_generatedScripts = new HashMap();
    }
    
    /**
     * Adds a list of CmsContextMenuItem objects to the context menu list.<p>
     * 
     * @param entries a list of initialized context menu items
     */
    public void addEntries(List entries) {
        m_allEntries.addAll(entries);
        Collections.sort(m_allEntries);
    }
      
    /**
     * Returns all entries of the context menu.<p>
     * 
     * @return all entries of the context menu
     */
    public List getAllEntries() {
        return m_allEntries;
    }
    
    /**
     * Builds the Javascript to create the context menu.<p>
     * 
     * @param cms the CmsObject
     * @param settings the explorer type settings for which the context menu is created
     * @param resTypeId the id of the resource type which uses the context menu
     * @param messages the localized workplace messages 
     * @return the JavaScript output to create the context menu
     */
    public String getJSEntries(CmsObject cms, CmsExplorerTypeSettings settings, int resTypeId, CmsWorkplaceMessages messages) {
        // try to get the stored entries from the Map
        String entries = (String)m_generatedScripts.get(cms.getRequestContext().getLocale());
        if (entries == null) { 
            // entries not yet in Map, so generate them
            StringBuffer result = new StringBuffer(4096);
            String jspWorkplaceUri = OpenCms.getLinkManager().substituteLink(cms, CmsWorkplace.C_PATH_WORKPLACE);  
            String xmlWorkplaceUri = OpenCms.getLinkManager().substituteLink(cms, CmsWorkplaceAction.C_PATH_XML_WORKPLACE);
            
            // create the JS for the resource object
            result.append("\nvi.resource[" + resTypeId + "]=new res(\"" + settings.getName() + "\",");
            result.append("\"" + messages.key(settings.getKey()) + "\",vi.skinPath+\"filetypes/" + settings.getIcon() + "\",\"" + settings.getNewResourceUri() + "\",true);\n");
            
            Iterator i = getAllEntries().iterator();
            while (i.hasNext()) {
                // create the context menu items
                CmsExplorerContextMenuItem item = (CmsExplorerContextMenuItem)i.next();
                result.append("addMenuEntry(" + resTypeId + ", ");
                if (CmsExplorerContextMenuItem.C_TYPE_ENTRY.equals(item.getType())) {
                    // create a menu entry
                    result.append("\"" + messages.key(item.getKey()) + "\", ");
                    if (item.isXml()) {
                        // legacy XMLTemplate based entry
                        result.append("\"" + xmlWorkplaceUri + item.getUri());
                        String paramPrefix = "?";
                        if (item.getUri().indexOf("?") != -1) {
                            paramPrefix = "&";
                        }
                        result.append(paramPrefix + "initial=true");
                        result.append("\", ");
                    } else {
                        // JSP entry
                        result.append("\"" + jspWorkplaceUri + item.getUri() + "\", ");
                    }
                    // check the item target
                    String target = item.getTarget();
                    if (target == null) {
                        target = "";
                    }
                    result.append("\"'" + target + "'\", ");
                    // remove all blanks from the rule String
                    String rules = CmsStringSubstitution.substitute(item.getRules(), " ", "");
                    // parse the rules to create autlock column
                    rules = parseRules(rules, item.getKey());
                    result.append("\"" + rules + "\");\n");
                    // result: addMenuEntry([id], "[language_key]", "[dialogURI]", "'[target]'", "ddiiiiaaaiaaaiddddddddddddiiiidddd");
                } else {
                    // create a separator entry
                    result.append("\"-\", \" \", \"''\", \"\");\n");
                    // result: addMenuEntry([id], "-", " ", "''", "ddaaaaaaaaaaaaddddddddddddaaaadddd");
                }         
            }
            entries = result.toString();
            // store the generated entries
            m_generatedScripts.put(cms.getRequestContext().getLocale(), entries);
        }
        
        return entries;
    }
    
    /**
     * Parses the rules and adds a column for the autolock feature of resources.<p>
     * 
     * @param rules the current rules
     * @param key the key name of the current item
     * @return the rules with added autlock rules column
     */
    private String parseRules(String rules, String key) {
        StringBuffer newRules = new StringBuffer(rules.length() + 4);
        newRules.append(rules.substring(0, 6));
        if ("explorer.context.lock".equalsIgnoreCase(key) || "explorer.context.unlock".equalsIgnoreCase(key)) {
            // for "lock" and "unlock" item, use same rules as "unlocked" column
            newRules.append(rules.substring(2, 6));
        } else {
            // for all other items, use same rules as "locked exclusively by current user" column
            newRules.append(rules.substring(6, 10));
        }       
        newRules.append(rules.substring(6));
        return newRules.toString();
    }

    /**
     * Sets all entries of the context menu.<p>
     * 
     * @param entries all entries of the context menu
     */
    public void setAllEntries(List entries) {
        m_allEntries = entries;
        Collections.sort(m_allEntries);
    }
    
    /**
     * Sorts the list of entries according to the value of the "order" attribute in the configuration.<p>
     */
    public void sortEntries() {
        Collections.sort(m_allEntries);
    }

}
