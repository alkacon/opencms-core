/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsConfigurationManager.java,v $
 * Date   : $Date: 2004/03/07 19:22:02 $
 * Version: $Revision: 1.4 $
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class CmsConfigurationManager {
    
    /** The DTD system declaration used for the opencms configuration DTD */
    public static final String C_CONFIGURATION_DTD = "http://www.opencms.org/opencms-configuration.dtd";
    
    /** The root node of the XML configuration */
    protected static final String N_ROOT = "opencms";
    
    /** The configurations node */
    protected static final String N_CONFIGURATION = "configuration";
    
    /** The config node */
    protected static final String N_CONFIG = "config";
    
    /** The initialized configuration classes */
    private List m_configurations;

    /** The digester for reading the XML configuration */
    private Digester m_digester;
    
    /**
     * Creates a new OpenCms configuration manager.<p>
     */
    public CmsConfigurationManager() {
        m_configurations = new ArrayList();
    }
    
    /**
     * Adds a configuration object to the configuration manager.<p>
     * 
     * @param configuration the configuration to add
     */
    public void addConfiguration(I_CmsXmlConfiguration configuration) {
        if (OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info("Adding configuration: " + configuration);
        }        
        m_configurations.add(configuration);
    }
    
    /**
     * Adds a configuration class to this configuration manager.<p>
     * 
     * @param clazz the name of the configuration class to add
     */
    public void addConfiguration(String clazz) {
        I_CmsXmlConfiguration configuration;
        
        // create a new instance of the class 
        try {
            configuration = (I_CmsXmlConfiguration)(Class.forName(clazz)).newInstance();
        } catch (InstantiationException e) {
            configuration = null;
            OpenCms.getLog(this).error("Trouble when initializing class " + clazz, e);
        } catch (IllegalAccessException e) {
            configuration = null;
            OpenCms.getLog(this).error("Trouble when initializing class " + clazz, e);
        } catch (ClassNotFoundException e) {
            configuration = null;
            OpenCms.getLog(this).error("Trouble when initializing class " + clazz, e);
        }
        
        // add the digester rules from the new configuration
        configuration.addXmlDigesterRules(m_digester);
    }

    /**
     * Creates the XML document build from the current configuration.<p>
     * 
     * @return the XML document build from the current configuration
     */
    public Document generateXml() {
        // create a new document
        Document result = DocumentHelper.createDocument();
        
        // set the document type        
        DOMDocumentType docType = new DOMDocumentType();
        docType.setElementName(N_ROOT);
        docType.setPublicID(C_CONFIGURATION_DTD);
        result.setDocType(docType); 
        
        Element root = result.addElement(N_ROOT);
        // start the XML generation
        // add the <configuration> node
        Element configurationElement = root.addElement(N_CONFIGURATION);
        Iterator i = m_configurations.iterator();        
        while (i.hasNext()) {
            // append the current configuration 
            I_CmsXmlConfiguration configuration = (I_CmsXmlConfiguration)i.next();
            configurationElement
                .addElement(N_CONFIG)
                .addAttribute(I_CmsXmlConfiguration.A_CLASS, configuration.getClass().getName());
            configuration.generateXml(root);
        }
        // return the resulting document
        return result;
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
     * Loads the OpenCms configuration from the given XML URL.<p>
     * 
     * @param url the URL of the XML configuration to load
     * @throws SAXException in case of XML parse errors
     * @throws IOException in case of file IO errors
     */
    public void loadXmlConfiguration(URL url) throws SAXException, IOException {        
        // instantiate Digester and disable XML validation
        m_digester = new Digester();
        m_digester.setValidating(true);
        m_digester.setEntityResolver(new CmsConfigurationEntitiyResolver());
        m_digester.setRuleNamespaceURI(null);       

        // add this class to the Digester
        m_digester.push(this);
        
        // add rule for <configuration> node        
        m_digester.addCallMethod("*/" + N_CONFIGURATION + "/" + N_CONFIG, "addConfiguration", 1);
        m_digester.addCallParam("*/" + N_CONFIGURATION + "/" + N_CONFIG, 0, I_CmsXmlConfiguration.A_CLASS);    
        
        // generic <param> parameter rules
        m_digester.addCallMethod("*/" + I_CmsXmlConfiguration.N_PARAM, I_CmsConfigurationParameterHandler.C_ADD_PARAMETER_METHOD, 2);
        m_digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 0,  I_CmsXmlConfiguration.A_NAME);
        m_digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 1);
        
        // start the parsing process        
        m_digester.parse(url.openStream());        
    }
    
    /**
     * Loads the OpenCms configuration from the given XML file.<p>
     * 
     * @param filename filename of the XML configuration to load
     * @throws SAXException in case of XML parse errors
     * @throws IOException in case of file IO errors
     */    
    public void loadXmlConfiguration(String filename) throws SAXException, IOException {
        loadXmlConfiguration((new File(filename)).toURL());
    }
    
    /**
     * Allows resolving the DTD "http://www.opencms.org/system/shared/opencms-configuration.dtd";
     */
    private class CmsConfigurationEntitiyResolver implements EntityResolver {
        
        /**
         * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
         */
        public InputSource resolveEntity(String publicId, String systemId) {
            if (systemId.equals(C_CONFIGURATION_DTD)) {
                // return our special input source
                try {
                    return new InputSource(getClass().getClassLoader().getResourceAsStream("org/opencms/configuration/opencms-configuration.dtd"));
                } catch (Throwable t) {
                    OpenCms.getLog(this).error("Could not open opencms-configuration.dtd mapping", t);
                    return null;
                }
            } else {
                // use the default behaviour
                return null;
            }
        }
    }
}
