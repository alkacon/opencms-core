/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsWorkplaceEditorConfiguration.java,v $
 * Date   : $Date: 2004/02/21 17:11:43 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.editor;

import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * Single editor configuration object.<p>
 * 
 * Holds all necessary information about an OpenCms editor which is stored in the
 * "editor_configuration.xml" file in each editor folder.<p>
 * 
 * Provides methods to get the editor information for the editor manager.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.3.1
 */
public class CmsWorkplaceEditorConfiguration {
    
    /** Name of the root document node */
    public static final String C_DOCUMENT_NODE = "editor";
    
    /** Name of the editor label node */
    protected static final String C_NODE_EDITORLABEL = "label";
    
    /** Name of the resourcetypes node */
    protected static final String C_NODE_RESOURCETYPES = "resourcetypes";   
    
    /** Name of the resource type node */
    protected static final String C_NODE_TYPE = "type";
    
    /** Name of the resource type subnode name */
    protected static final String C_NODE_NAME = "name";
    
    /** Name of the resource type subnode ranking */
    protected static final String C_NODE_RANKING = "ranking";
    
    /** Name of the resource type subnode mapto */
    protected static final String C_NODE_MAPTO = "mapto";
    
    /** Name of the useragents node */
    protected static final String C_NODE_USERAGENTS = "useragents";
    
    /** Name of the single user agent node */
    protected static final String C_NODE_AGENT = "agent";
    
    /** Debug flag */
    public static final boolean C_DEBUG = false;
    
    private List m_browserPattern;
    private String m_editorLabel;
    private String m_editorUri;
    private Map m_resTypes;
    private List m_userAgentsRegEx;
    private boolean m_validConfiguration;
    
    /**
     * Constructor with xml data String.<p>
     * 
     * @param xmlData the XML data String containing the information about the editor
     * @param editorUri the editor workplace URI
     */
    public CmsWorkplaceEditorConfiguration(String xmlData, String editorUri) {
        setValidConfiguration(true);
        try {
            Document document = DocumentHelper.parseText(xmlData);
            initialize(document, editorUri);
        } catch (DocumentException e) {
            // xml String could not be parsed
            logConfigurationError("Could not parse xml.", e);
        }
    }
    
    /**
     * Initializes all member variables.<p>
     * 
     * @param document the XML configuration document
     * @param editorUri the editor workplace URI
     */
    private void initialize(Document document, String editorUri) {
        // set the label of the editor
        setEditorLabel(document.getRootElement().elementText(C_NODE_EDITORLABEL));
        
        // set the URI of the editor
        setEditorUri(editorUri);
        
        // create the map of valid resource types
        Iterator i = document.getRootElement().element(C_NODE_RESOURCETYPES).elementIterator(C_NODE_TYPE);
        Map resTypes = new HashMap();
        while (i.hasNext()) {
            Element currentType = (Element)i.next();
            float ranking;
            String name = currentType.elementText(C_NODE_NAME);
            if (name == null || "".equals(name.trim())) {
                logConfigurationError("Invalid resource type name.", null);
                continue;
            }
            try {
                ranking = Float.parseFloat(currentType.elementText(C_NODE_RANKING));
            } catch (Throwable t) {
                logConfigurationError("Invalid ranking for resource type " + name + ".", t);
                continue;
            }
            String mapTo = currentType.elementText(C_NODE_MAPTO);
            if ("".equals(mapTo)) {
                mapTo = null;
            }
            resTypes.put(name, new String[] {"" + ranking, mapTo});          
        }
        setResourceTypes(resTypes);
        
        // create the list of user agents & compiled patterns for editor
        i = document.getRootElement().element(C_NODE_USERAGENTS).elementIterator(C_NODE_AGENT);
        List pattern = new ArrayList();
        List userAgents = new ArrayList();
        while (i.hasNext()) {
            Element currentAgent = (Element)i.next();
            String agentName = currentAgent.getText();
            if (agentName != null && !"".equals(agentName.trim())) {
                userAgents.add(agentName);
                try {
                    pattern.add(Pattern.compile(agentName));
                } catch (PatternSyntaxException e) {
                    logConfigurationError("Error in regular expression " + agentName + ".", e);
                }
            } else {
                logConfigurationError("Invalid user agent definition.", null);
            }
        }
        setBrowserPattern(pattern);
        setUserAgentsRegEx(userAgents);
    }
    
    /**
     * Returns the list of compiled browser patterns.<p>
     * 
     * @return the list of compiled browser patterns
     */
    public List getBrowserPattern() {
        return m_browserPattern;
    }
    
    /**
     * Returns the editor label key used for the localized nice name.<p>
     * 
     * @return the editor label key used for the localized nice name
     */
    public String getEditorLabel() {
        return m_editorLabel;
    }
    
    /**
     * Returns the editor workplace URI.<p>
     * 
     * @return the editor workplace URI
     */
    public String getEditorUri() {
        return m_editorUri;
    }
    
    /**
     * Returns the mapping for the given resource type.<p>
     * 
     * @param resourceType the resource type name to check
     * @return the mapping or null, if no mapping is specified
     */
    public String getMappingForResourceType(String resourceType) {
        String[] resourceTypeParams = (String[])getResourceTypes().get(resourceType);
        if (resourceTypeParams == null) {
            return null;
        } else {
            return resourceTypeParams[1];
        }
    }
    
    /**
     * Returns the ranking value for the given resource type.<p>
     * 
     * @param resourceType the current resource type
     * @return the ranking (the higher the better)
     */
    public float getRankingForResourceType(String resourceType) {
        String[] resourceTypeParams = (String[])getResourceTypes().get(resourceType);
        if (resourceTypeParams == null) {
            return -1.0f;
        } else {
            return Float.parseFloat(resourceTypeParams[0]);
        }
    }
    
    /**
     * Returns the valid resource types of the editor.<p>
     * 
     * A single map item has the resource type name as key, 
     * the value is a String array with two entries:
     * <ul>
     * <li>Entry 0: the ranking for the resource type</li>
     * <li>Entry 1: the mapping to another resource type or null</li>
     * </ul><p>
     * 
     * @return the valid resource types of the editor
     */
    public Map getResourceTypes() {
        return m_resTypes;
    }
    
    /**
     * Returns the valid user agents regular expressions of the editor.<p>
     * 
     * @return the valid user agents regular expressions of the editor
     */
    public List getUserAgentsRegEx() {
        return m_userAgentsRegEx;
    }
    
    /**
     * Returns if the current configuration is valid.<p>
     * 
     * @return true if no configuration errors were found, otherwise false
     */
    public boolean isValidConfiguration() {
        return m_validConfiguration;
    }
    
    /**
     * Logs configuration errors and invalidates the current configuration.<p>
     * 
     * @param message the message specifying the configuration error
     * @param t the Throwable object or null
     */
    private void logConfigurationError(String message, Throwable t) {
        setValidConfiguration(false);
        if (OpenCms.getLog(this).isErrorEnabled()) {
            if (t == null) {
                OpenCms.getLog(this).error("Error in editor configuration: " + message);
            } else {
                OpenCms.getLog(this).error("Error in editor configuration: " + message, t);
            }
        }
    }
    
    /**
     * Returns if the configuration is suitable for the given resource type.<p>
     * 
     * @param resourceType the resource type to check
     * @return true if the configuration matches the resource type
     */
    public boolean matchesResourceType(String resourceType) {
        return m_resTypes.containsKey(resourceType);
    }
    
    /**
     * Tests if the current browser is matching the configuration.<p>
     * 
     * @param currentBrowser the users browser String to test
     * @return true if the browser matches the configuration, otherwise false
     */
    public boolean matchesBrowser(String currentBrowser) {      
        if (currentBrowser == null) {
            return false;
        }
        for (int i = 0; i < getBrowserPattern().size(); i++) {            
            boolean matches = ((Pattern)getBrowserPattern().get(i)).matcher(currentBrowser.trim()).matches();
            if (matches) {
                if (C_DEBUG) {
                    System.err.println("[CmsWorkplaceEditorConfiguration.isMatchingBrowser] - It matches! (" + currentBrowser + ")");
                }
                return true;
            }            
        }
        return false;
    }
    
    /**
     * Sets the list of compiled browser patterns.<p>
     * 
     * @param pattern the list of compiled browser patterns
     */
    private void setBrowserPattern(List pattern) {
        if (pattern == null || pattern.size() == 0) {
            logConfigurationError("Editor user agent pattern not set.", null);
        }
        m_browserPattern = pattern;
    }
    
    /**
     * Sets the editor label key used for the localized nice name.<p>
     * 
     * @param label the editor label key used for the localized nice name
     */
    private void setEditorLabel(String label) {
        if (label == null || "".equals(label.trim())) {
            logConfigurationError("Editor label not set.", null);
        }
        m_editorLabel = label;
    }
    
    /**
     * Sets the editor workplace URI.<p>
     * @param uri the editor workplace URI
     */
    private void setEditorUri(String uri) {
        if (uri == null || "".equals(uri.trim())) {
            logConfigurationError("Editor URI not set.", null);
        }
        m_editorUri = uri;
    }
    
    /**
     * Sets the valid resource types of the editor.<p>
     * 
     * @param types the valid resource types of the editor
     */
    private void setResourceTypes(Map types) {
        if (types == null || types.size() == 0) {
            logConfigurationError("No resource types specified for editor.", null);
        }
        m_resTypes = types;
    }
    
    /**
     * Sets the valid user agents regular expressions of the editor.<p>
     * 
     * @param agents the valid user agents regular expressions of the editor
     */
    private void setUserAgentsRegEx(List agents) {
        if (agents == null || agents.size() == 0) {
            logConfigurationError("No user agents specified for editor.", null);
        }
        m_userAgentsRegEx = agents;
    }
    
    /**
     * Sets if the current configuration is valid.<p>
     * 
     * @param isValid true if no configuration errors were found, otherwise false
     */
    private void setValidConfiguration(boolean isValid) {
        m_validConfiguration = isValid;
    }
    
}
