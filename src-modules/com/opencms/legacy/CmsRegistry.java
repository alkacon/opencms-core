/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsRegistry.java,v $
 * Date   : $Date: 2005/06/23 14:01:14 $
 * Version: $Revision: 1.7 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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

package com.opencms.legacy;

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.template.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The OpenCms registry.<p>
 * 
 * This registry contains information about the installed OpenCms modules,
 * and also important other system information
 * e.g. the mail server settings for the task management,
 * the workplace views and other items.<p>
 *
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * 
 * @deprecated The registry has been replaced by the new XML configuration.
 */
public class CmsRegistry extends A_CmsXmlContent {

    /** Id to identify that the version is not important .*/
    public static final int C_ANY_VERSION = -1;   
    
    /** The filename for this registry. */
    private String m_regFileName;

    /** The xml-document representing this registry. */
    private Document m_xmlReg;
    
    private static CmsRegistry m_registry;

    /**
     * Returns the registry to read values from it.<p>
     *
     * @return the registry
     */
    public static CmsRegistry getInstance() {
        if (m_registry == null) {
            // initialize the (deprecated) XML registry
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(". Initializing registry: starting");
            }    
            String path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("config/registry.xml");
            try {
                m_registry = new CmsRegistry(path);
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(". Initializing registry: finished");
                }                
            } catch (CmsException e) {
                CmsLog.getLog(CmsRegistry.class).error("Unable to read registry.xml from path: '" + path + "'", e);
            }
        }
        return m_registry;
    }    

    /**
     * Creates a new CmsRegistry that is initialized with the contents from the given filename.<p> 
     *
     * @param regFileName the path to the registry file
     * @throws CmsException in case somthing goes wrong
     */
    public CmsRegistry(String regFileName) throws CmsException {
        super();
        try {
            // store the filename
            m_regFileName = regFileName;

            // get the file
            File xmlFile = new File(m_regFileName);

            // parse the registry-xmlfile and store it.
            InputStream content = new FileInputStream(xmlFile);
            m_xmlReg = parse(content);
        } catch (Exception exc) {
            throw new CmsLegacyException("couldn't init registry", CmsLegacyException.C_REGISTRY_ERROR, exc);
        }
    }    

    /**
     * @see com.opencms.template.A_CmsXmlContent#getContentDescription()
     */
    public String getContentDescription() {
        return "Registry";
    }

    /**
     * Return the XML "system" node Element from the registry for further
     * processing in another class.
     * @return the system node.
     */
    public Element getSystemElement() {
        return (Element)m_xmlReg.getElementsByTagName("system").item(0);
    }

    /**
     * Returns a value for a system key.<p>
     * 
     * E.g. <code>&lt;system&gt;&lt;mailserver&gt;mail.server.com&lt;/mailserver&gt;&lt;/system&gt;</code>
     * can be requested via <code>getSystemValue("mailserver");</code> and returns "mail.server.com".<p>
     *
     * @param key the key of the system value
     * @return the system value for that key
     */
    public String getSystemValue(String key) {
        String retValue = null;
        try {
            Element systemElement = (Element)m_xmlReg.getElementsByTagName("system").item(0);
            retValue = systemElement.getElementsByTagName(key).item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - registry is not wellformed
        }
        return retValue;
    }

    /**
     * Returns a vector of values for a system key.<p>
     *
     * @param key the key of the system value
     * @return the values for that system key
     */
    public Hashtable getSystemValues(String key) {
        Hashtable retValue = new Hashtable();
        try {
                
            Element systemElement = (Element)m_xmlReg.getElementsByTagName("system").item(0);
            NodeList list = systemElement.getElementsByTagName(key).item(0).getChildNodes();
            for (int i = 0; i < list.getLength(); i++) { 
                String regKey = list.item(i).getNodeName();
                String regValue=null;
                if (list.item(i).hasChildNodes()) {
                    regValue = list.item(i).getFirstChild().getNodeValue();
                }                                 
                if (regValue!=null) {
                    retValue.put(regKey, regValue);
                }
            }
        } catch (Exception exc) {      
            // ignore the exception - registry is not wellformed
        }
        return retValue;
    }

    /**
     * Gets the expected tagname for the XML documents of this content type.<p>
     * 
     * @return Expected XML tagname
     */
    public String getXmlDocumentTagName() {
        return "registry";
    }
}

