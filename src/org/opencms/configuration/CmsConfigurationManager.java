/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsConfigurationManager.java,v $
 * Date   : $Date: 2004/03/12 16:00:48 $
 * Version: $Revision: 1.5 $
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
package org.opencms.configuration;

import org.opencms.main.OpenCms;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.digester.Digester;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Configuration manager for digesting the OpenCms XML configuration.<p>
 * 
 * Reads the individual configuration class nodes first and creaes new 
 * instances of the "base" configuration classes.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsConfigurationManager implements I_CmsXmlConfiguration {
    
    /**
     * Adds resolve rules for the configured system internal DTD.<p> 
     */
    private class CmsConfigurationEntitiyResolver implements EntityResolver {
        
        /**
         * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
         */
        public InputSource resolveEntity(String publicId, String systemId) {
            Iterator i = m_dtdPrefixes.keySet().iterator();
            while (i.hasNext()) {
                // test all configured DTD prefixes
                String prefix = (String)i.next();
                if (systemId.startsWith(prefix)) {
                    String location = (String)m_dtdPrefixes.get(prefix);
                    String id = location + systemId.substring(prefix.length());
                    try {
                        return new InputSource(getClass().getClassLoader().getResourceAsStream(id));
                    } catch (Throwable t) {
                        if (OpenCms.getLog(this).isDebugEnabled()) {
                            OpenCms.getLog(this).error("Did not find " + systemId + " in " + location);
                        }
                    }                    
                }
            }
            // use the default behaviour (i.e. resolve through external URL)
            return null;
        }
    }
    
    /** The location of the opencms configuration DTD if the default prefix is the system ID */
    public static final String C_DEFAULT_DTD_LOCATION = "org/opencms/configuration/";
    
    /** The default prefix for the opencms configuration DTD */
    public static final String C_DEFAULT_DTD_PREFIX = "http://www.opencms.org/dtd/6.0/";
    
    /** The name of the default XML file for this configuration */
    private static final String C_DEFAULT_XML_FILE_NAME = "opencms.xml";    
    
    /** The name of the DTD file for this configuration */
    private static final String C_DTD_FILE_NAME = "opencms-configuration.dtd";
    
    /** The config node */
    protected static final String N_CONFIG = "config";
    
    /** The configurations node */
    protected static final String N_CONFIGURATION = "configuration";
    
    /** The "opencms" root node of the XML configuration */
    protected static final String N_ROOT = "opencms";
    
    /** The initialized configuration classes */
    private List m_configurations;

    /** The digester for reading the XML configuration */
    private Digester m_digester;
    
    /** A map of DTD prefix values for lookup */
    protected Map m_dtdPrefixes;
    
    /**
     * Creates a new OpenCms configuration manager.<p>
     */
    public CmsConfigurationManager() {
        m_dtdPrefixes = new HashMap();
        m_dtdPrefixes.put(C_DEFAULT_DTD_PREFIX, C_DEFAULT_DTD_LOCATION);
        m_configurations = new ArrayList();
        // m_configurations.add(this);
    }
    
    /**
     * Adds a configuration object to the configuration manager.<p>
     * 
     * @param configuration the configuration to add
     */
    public void addConfiguration(I_CmsXmlConfiguration configuration) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Adding configuration: " + configuration);
        }        
        m_configurations.add(configuration);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {
        // noop, this configuration has no additional parameters
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {
        // add rule for <configuration> node        
        digester.addObjectCreate("*/" + N_CONFIGURATION + "/" + N_CONFIG, I_CmsXmlConfiguration.A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_CONFIGURATION + "/" + N_CONFIG, "addConfiguration");            
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        // add the <configuration> node
        Element configurationElement = parent.addElement(N_CONFIGURATION);
        Iterator i = m_configurations.iterator();        
        while (i.hasNext()) {
            // append the individual configuration 
            I_CmsXmlConfiguration configuration = (I_CmsXmlConfiguration)i.next();
            configurationElement
                .addElement(N_CONFIG)
                .addAttribute(I_CmsXmlConfiguration.A_CLASS, configuration.getClass().getName());
        }
        return parent;
    }
    
    /**
     * Creates the XML document build from the provided configuration.<p>
     * 
     * @param configuration the configuration to build the XML for
     * @return the XML document build from the provided configuration
     */
    public Document generateXml(I_CmsXmlConfiguration configuration) {
        // create a new document
        Document result = DocumentHelper.createDocument();
        
        // set the document type        
        DOMDocumentType docType = new DOMDocumentType();
        docType.setElementName(N_ROOT);
        docType.setPublicID(configuration.getDtdUrlPrefix() + configuration.getDtdFilename());
        result.setDocType(docType); 
        
        Element root = result.addElement(N_ROOT);
        // start the XML generation
        configuration.generateXml(root);
        
        // return the resulting document
        return result;
    } 

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {
        // noop, this configuration has no additional parameters
        return null;
    }
    
    /**
     * Returns a specific configuration from the list of initialized configurations.<p>
     * 
     * @param clazz the configuration class that should be returned
     * @return the initialized configuration class instance, or <code>null</code> if this is not found
     */
    public I_CmsXmlConfiguration getConfiguration(Class clazz) {
        Iterator i = m_configurations.iterator();
        while (i.hasNext()) {
            I_CmsXmlConfiguration configuration = (I_CmsXmlConfiguration)i.next();
            if (clazz.equals(configuration.getClass())) {
                return configuration;
            }
        }        
        return null;
    }
    
    /**
     * Returns the list of initialized configurations.<p>
     * 
     * @return the list of initialized configurations
     */
    public List getConfigurations() {
        return m_configurations;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {
        return C_DTD_FILE_NAME;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdSystemLocation()
     */
    public String getDtdSystemLocation() {
        return C_DEFAULT_DTD_LOCATION;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdUrlPrefix()
     */
    public String getDtdUrlPrefix() {
        return C_DEFAULT_DTD_PREFIX;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getXmlFileName()
     */
    public String getXmlFileName() {
        return C_DEFAULT_XML_FILE_NAME;
    }
    
    /**
     * Loads the OpenCms configuration from the given XML file.<p>
     * 
     * @param baseUrl base URL of the XML configurations to load
     * @throws SAXException in case of XML parse errors
     * @throws IOException in case of file IO errors
     */    
    public void loadXmlConfiguration(URL baseUrl) throws SAXException, IOException {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).error("Base URL is " + baseUrl);
        }
        
        // first load the base configuration
        loadXmlConfiguration(baseUrl, this);
        
        // now iterate all sub-configrations
        Iterator i = m_configurations.iterator();
        while (i.hasNext()) {
            I_CmsXmlConfiguration config = (I_CmsXmlConfiguration)i.next();
            loadXmlConfiguration(baseUrl, config);
        }
    }
    
    /**
     * Loads the OpenCms configuration from the given XML URL.<p>
     * 
     * @param url the base URL of the XML configuration to load
     * @param configuration the configuration to load
     * @throws SAXException in case of XML parse errors
     * @throws IOException in case of file IO errors
     */
    public void loadXmlConfiguration(URL url, I_CmsXmlConfiguration configuration) throws SAXException, IOException {     
        
        // generate the file URL for the XML input
        URL fileUrl = new URL(url, configuration.getXmlFileName());        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("XML input file URL is " + fileUrl);
        }
        
        // instantiate Digester and enable XML validation
        m_digester = new Digester();
        m_digester.setValidating(true);
        m_digester.setEntityResolver(new CmsConfigurationEntitiyResolver());
        m_digester.setRuleNamespaceURI(null);       

        // add this class to the Digester
        m_digester.push(configuration);
  
        configuration.addXmlDigesterRules(m_digester);
        
        // start the parsing process        
        m_digester.parse(fileUrl.openStream());        
    }


    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#setXmlFileName(java.lang.String)
     */
    public void setXmlFileName(String fileName) {
        // noop, file name is fixed for this mater configuration to "opencms.xml"
    }
}
