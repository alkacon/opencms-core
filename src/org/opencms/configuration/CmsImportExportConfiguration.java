/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsImportExportConfiguration.java,v $
 * Date   : $Date: 2004/03/06 18:47:36 $
 * Version: $Revision: 1.1 $
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

import org.opencms.importexport.CmsImportExportManager;
import org.opencms.importexport.I_CmsImportExportHandler;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * Import/Export master configuration class.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsImportExportConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
    
    /** Identifier for user principals */
    public static final String C_PRINCIPAL_USER = "USER";

    /** Identifier for group principals */
    public static final String C_PRINCIPAL_GROUP = "GROUP";

    /** The node name of an individual import/export handler */
    protected static final String N_IMPORTEXPORTHANDLER = "importexporthandler";
    
    /** Master node for import/export handlers */
    protected static final String N_IMPORTEXPORTHANDLERS = "importexporthandlers";

    /** Master node for import version class names */
    protected static final String N_IMPORTVERSIONS = "importversions";    

    /** The node name of the import subconfiguration */
    protected static final String N_IMPORT = "import";      
    
    /** The node name of an individual import version class */
    protected static final String N_IMPORTVERSION = "importversion";   
    
    /** The main configuration node name */
    protected static final String N_IMPORTEXPORT = "importexport";
    
    /** The import overwrite node name */    
    protected static final String N_OVERWRITE = "overwrite";
    
    /** The import immutable resource node */
    protected static final String N_IMMUTABLES = "immutables";
    
    /** An individual immutable resource node */
    protected static final String N_RESOURCE = "resource";

    /** The principal translation node */
    protected static final String N_PRINCIPALTRANSLATIONS = "princialtranslations";
    
    /** An individual principal translation node */
    protected static final String N_PRINCIPALTRANSLATION = "princialtranslation";
    
    /** Node that indicates page conversion */
    protected static final String N_CONVERT = "convert";
    
    /** Node the contains optional URL of old web application */
    protected static final String N_OLDWEBAPPURL = "oldwebappurl";
    
    /** Node that contains a list of properties ignored during import */
    protected static final String N_IGNOREDPROPERTIES = "ignoredproperties";
    
    /** An indovidual property node */
    protected static final String N_PROPERTY = "property";
    
    /** The configured import/export manager */
    private CmsImportExportManager m_importExportManager;
    
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsImportExportConfiguration() {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Empty constructor called on " + this);
        }     
    } 

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {                        
        // add factory create method for "real" instance creation
        digester.addFactoryCreate("*/" + N_IMPORTEXPORT, CmsImportExportConfiguration.class);                            
        // call this method at the end of the import/export configuration
        digester.addCallMethod("*/" + N_IMPORTEXPORT, "initializeFinished");          
        // add this configuration object to the calling configuration after is has been processed
        digester.addSetNext("*/" + N_IMPORTEXPORT, "addConfiguration");        
        
        // creation of the import/export manager        
        digester.addObjectCreate("*/" + N_IMPORTEXPORT, CmsImportExportManager.class);                         
        // import/export manager finished
        digester.addSetNext("*/" + N_IMPORTEXPORT, "setImportExportManager");
                
        // add rules for import/export handlers
        digester.addObjectCreate("*/" + N_IMPORTEXPORT + "/" + N_IMPORTEXPORTHANDLERS + "/" + N_IMPORTEXPORTHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_IMPORTEXPORT + "/" + N_IMPORTEXPORTHANDLERS + "/" + N_IMPORTEXPORTHANDLER, "addImportExportHandler");
                        
        // overwrite rule
        digester.addCallMethod("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_OVERWRITE, "setOverwriteCollidingResources", 0); 
       
        // convert rule
        digester.addCallMethod("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_CONVERT, "setConvertToXmlPage", 0); 

        // old webapp rule
        digester.addCallMethod("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_OLDWEBAPPURL, "setOldWebAppUrl", 0);         
        
        // add rules for the import versions
        digester.addObjectCreate("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IMPORTVERSIONS + "/" + N_IMPORTVERSION, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IMPORTVERSIONS + "/" + N_IMPORTVERSION, "addImportVersionClass");

        // add rules for the import immutables
        digester.addCallMethod("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IMMUTABLES + "/" + N_RESOURCE, "addImmutableResource", 1); 
        digester.addCallParam("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IMMUTABLES + "/" + N_RESOURCE, 0, A_URI);

        // add rules for the import princial translations
        digester.addCallMethod("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_PRINCIPALTRANSLATIONS + "/" + N_PRINCIPALTRANSLATION , "addImportPrincipalTranslation", 3); 
        digester.addCallParam("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_PRINCIPALTRANSLATIONS + "/" + N_PRINCIPALTRANSLATION, 0, A_TYPE);
        digester.addCallParam("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_PRINCIPALTRANSLATIONS + "/" + N_PRINCIPALTRANSLATION, 1, A_TO);
        digester.addCallParam("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_PRINCIPALTRANSLATIONS + "/" + N_PRINCIPALTRANSLATION, 2, A_FROM);
                
        // add rules for the ignored properties
        digester.addCallMethod("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IGNOREDPROPERTIES + "/" + N_PROPERTY, "addIgnoredProperty", 1); 
        digester.addCallParam("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IGNOREDPROPERTIES + "/" + N_PROPERTY, 0, A_NAME);                      
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        // generate vfs node and subnodes
        Element importexportElement = parent.addElement(N_IMPORTEXPORT);
        
        Element resourceloadersElement = importexportElement.addElement(N_IMPORTEXPORTHANDLERS);
        List handlers = m_importExportManager.getImportExportHandlers();
        Iterator i = handlers.iterator();
        while (i.hasNext()) {          
            I_CmsImportExportHandler handler = (I_CmsImportExportHandler)i.next();
            // add the handler node
            Element loaderNode = resourceloadersElement.addElement(N_IMPORTEXPORTHANDLER);
            loaderNode.addAttribute(A_CLASS, handler.getClass().getName());
        }
        
        Element importElement = importexportElement.addElement(N_IMPORT);
        
        // <overwrite> node
        importElement.addElement(N_OVERWRITE).setText((new Boolean(m_importExportManager.overwriteCollidingResources())).toString());

        // <convert> node
        importElement.addElement(N_CONVERT).setText((new Boolean(m_importExportManager.convertToXmlPage())).toString());
        
        // <oldwebappurl> node
        if (m_importExportManager.getOldWebAppUrl() != null) {
            importElement.addElement(N_OLDWEBAPPURL).setText(m_importExportManager.getOldWebAppUrl());
        }
        
        // <importversions> node
        Element resourcetypesElement = importElement.addElement(N_IMPORTVERSIONS);
        i = m_importExportManager.getImportVersionClasses().iterator();
        while (i.hasNext()) {
            resourcetypesElement.addElement(N_IMPORTVERSION).addAttribute(A_CLASS, i.next().getClass().getName());
        }
        
        // <immutables> node
        Element immutablesElement = importElement.addElement(N_IMMUTABLES);
        i = m_importExportManager.getImmutableResources().iterator();
        while (i.hasNext()) {
            String uri = (String)i.next();
            immutablesElement.addElement(N_RESOURCE).addAttribute(A_URI, uri);
        }        

        // <princialtranslations> node
        Element principalsElement = importElement.addElement(N_PRINCIPALTRANSLATIONS);
        i = m_importExportManager.getImportGroupTranslations().keySet().iterator();
        while (i.hasNext()) {
            String from = (String)i.next();
            principalsElement.addElement(N_PRINCIPALTRANSLATION)
                .addAttribute(A_TYPE, C_PRINCIPAL_GROUP)
                .addAttribute(A_FROM, from)
                .addAttribute(A_TO, (String)m_importExportManager.getImportGroupTranslations().get(from));                
        }        
        i = m_importExportManager.getImportUserTranslations().keySet().iterator();
        while (i.hasNext()) {
            String from = (String)i.next();
            principalsElement.addElement(N_PRINCIPALTRANSLATION)
                .addAttribute(A_TYPE, C_PRINCIPAL_USER)
                .addAttribute(A_FROM, from)
                .addAttribute(A_TO, (String)m_importExportManager.getImportUserTranslations().get(from));                
        }  
        
        // <ignoredproperties> node
        Element propertiesElement = importElement.addElement(N_IGNOREDPROPERTIES);
        i = m_importExportManager.getIgnoredProperties().iterator();
        while (i.hasNext()) {
            String property = (String)i.next();
            propertiesElement.addElement(N_PROPERTY).addAttribute(A_NAME, property);
        }            
        
        // return the configured node
        return importexportElement;
    }
    
    /**
     * Returns the initialized import/export manager.<p>
     * 
     * @return the initialized import/export manager
     */
    public CmsImportExportManager getImportExportManager() {
        return m_importExportManager;
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#initialize()
     */
    public void initialize() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Import configuration : starting");
        }           
    }
    
    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Import configuration : finished");
        }            
    }   
    
    /**
     * Sets the generated import/export manager.<p>
     * 
     * @param manager the import/export manager to set
     */
    public void setImportExportManager(CmsImportExportManager manager) {
        m_importExportManager = manager;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Import manager init  : finished");
        }
    }
}
