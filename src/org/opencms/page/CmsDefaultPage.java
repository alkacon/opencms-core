/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/page/Attic/CmsDefaultPage.java,v $
 * Date   : $Date: 2003/11/28 17:00:18 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.page;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Simple implementation of CmsDefaultPage.<p>
 * 
 * @version $Revision: 1.5 $ $Date: 2003/11/28 17:00:18 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsDefaultPage extends CmsXmlPage implements Serializable  {

    /** The document object of the page */
    private Document m_document = null;
    
    /** Reference for named elements */
    private Map m_elements = null;
    
    /**
     * Creates a new instance of a CmsDefaultPage.<p>
     * The new instance must be unmarshalled afterwards, or use CmsXmlPage.newInstance instead.
     * 
     * @param file the file content
     */
    public CmsDefaultPage(CmsFile file) {
        super(file);
    }
    
    /**
     * Adds a new empty element with the given name and language.<p>
     *  
     * @param name name of the element, must be unique
     * @param language language of the element
     */
    public void addElement(String name, String language) {

        Element elements = m_document.getRootElement().element("elements");
        
        Element element = elements.addElement("element")
              .addAttribute("name", name)
              .addAttribute("language", language);
       
        element.addElement("editdata");
        element.addElement("displaydata");

        m_elements.put(language+"_"+name, element);
    }
    
    /**
     * Removes an existing element with the given name and language.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     */
    public void removeElement(String name, String language) {

        
        Element elements = m_document.getRootElement().element("elements");
        
        Element element = (Element)m_elements.remove(language+"_"+name);
        elements.remove(element);
    }
    
    /**
     * Sets the data of an already existing element.<p>
     * The data will be enclosed as CDATA within the xml page structure.
     * When setting the element data, the content of this element will be
     * re processed automatically.
     * 
     * @param name name of the element
     * @param language language of the element
     * @param data character data (CDATA) of the element
     */
    public void setElementData(String name, String language, byte[] data) {
        
        Element element = (Element)m_elements.get(language+"_"+name);
        Element editdata = element.element("editdata");
        Element displaydata = element.element("displaydata");
        String content = new String(data);
        String cdata = CmsStringSubstitution.substituteContextPath(content, OpenCms.getOpenCmsContext()  + "/");
        
        editdata.setContent(null);
        editdata.addCDATA(cdata);

        // TODO: convert editdata to displaydata
        displaydata.setContent(null);
        displaydata.addCDATA(content);
    }
    
    /**
     * Returns the data of an element.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     * @return the character data of the element
     */
    public byte[] getElementData(String name, String language) {

        Element element = (Element)m_elements.get(language+"_"+name);
        
        if (element != null) {
            
            Element editdata = element.element("editdata");
            
            // set the context & servlet path in editor content
            String content = CmsStringSubstitution.substitute(editdata.getText(), I_CmsWpConstants.C_MACRO_OPENCMS_CONTEXT + "/", OpenCms.getOpenCmsContext() + "/");
 
            return content.getBytes();
        } else {
            return null;
        }
    }

    /**
     * Returns if the page object contains a name specified by name and language.<p>
     * 
     * @param name the name of the element
     * @param language the language of the element
     * @return true if this element exists
     */
    public boolean hasElement(String name, String language) {
    
        return m_elements.containsKey(language+"_"+name);
    }
    
    /**
     * @see org.opencms.page.CmsXmlPage#getNames(java.lang.String)
     */
    public List getNames(String language) {
        
        List names = new ArrayList();
        for (Iterator i = m_elements.keySet().iterator(); i.hasNext();) {
            String name = (String)i.next();
            if (name.startsWith(language+"_")) {
                names.add(name.substring(language.length()+1));
            }
        }
        return names;
    }

    /**
     * @see org.opencms.page.CmsXmlPage#getLanguages()
     */
    public Set getLanguages() {
    
        Set languages = new HashSet();
        for (Iterator i = m_elements.keySet().iterator(); i.hasNext();) {
            String name = (String)i.next();
            String language = name.substring(0, name.indexOf("_"));
            languages.add(language);
        }
        return languages;
    }
    
    /**
     * @see org.opencms.page.CmsXmlPage#getContent(java.lang.String, java.lang.String)
     */
    public byte[] getContent(String name, String language) {

        Element element = (Element)m_elements.get(language+"_"+name);
        Element displaydata = element.element("displaydata");
        
        // TODO: if displaydata contains link macros, replace them
        return displaydata.getText().getBytes();
    }
    
    /**
     * @see org.opencms.page.CmsXmlPage#unmarshal(com.opencms.file.CmsObject)
     */
    public CmsXmlPage unmarshal(CmsObject cms) 
        throws CmsPageException {

        byte[] content = getContents();
        m_elements = new HashMap();
        
        if (content.length > 0) { 
            InputStream in = new ByteArrayInputStream(content);
    
            try {
                SAXReader reader = new SAXReader();
                reader.setEntityResolver(new CmsEntityResolver(cms));
                m_document = reader.read(in);
    
                
                for (Iterator i = m_document.getRootElement().element("elements").elementIterator("element"); i.hasNext();) {
                   
                    Element elem = (Element)i.next();
                    String elementName = elem.attribute("name").getValue();
                    String elementLang = elem.attribute("language").getValue();
                    
                    m_elements.put(elementLang+"_"+elementName, elem);
                }
                
            } catch (Exception exc) {
                throw new CmsPageException("Unmarshalling xml page failed", exc);
            } finally {
                try {
                    in.close();
                } catch (Exception exc) {
                    // noop
                }
            }
        }         
        return this;
    }
    
    /**
     * @see org.opencms.page.CmsXmlPage#marshal()
     */
    public CmsFile marshal() 
        throws CmsPageException {
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
        
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(m_document);
            writer.close();
        
            setContents(out.toByteArray());
            
        } catch (Exception exc) {
            throw new CmsPageException("Marshalling xml page failed", exc);
        }
        
        return this;
    }
}
