/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsXmlPageConverter.java,v $
 * Date   : $Date: 2004/06/13 23:33:38 $
 * Version: $Revision: 1.12 $
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
package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Iterator;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * @version $Revision: 1.12 $ $Date: 2004/06/13 23:33:38 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public final class CmsXmlPageConverter {

    /**
     * Constructor made private to avoid class instanciation.<p>
     */
    private CmsXmlPageConverter() {
        // noop
    }
    
    /**
     * Converts the contents of a page into an xml page.<p>
     * 
     * @param cms the cms object
     * @param content the content used with xml templates
     * @param body the name of the default body element
     * @param locale the locale of the body element(s)
     * @param encoding the encoding to the xml page
     * @return the xml page content or null if conversion failed
     * @throws CmsException if something goes wrong
     */
    public static CmsXmlPage convertToXmlPage(CmsObject cms, String content, String body, Locale locale, String encoding) throws CmsException {
        CmsXmlPage xmlPage = null;
        
        try {
            xmlPage = new CmsXmlPage(encoding);
            Document page = CmsImport.getXmlDocument(content);
            
            Element xmltemplate = page.getRootElement();
            if (xmltemplate == null || !"XMLTEMPLATE".equals(xmltemplate.getName())) {
                throw new Exception("Element XMLTEMPLATE not found");
            }
            
            // get all edittemplate nodes
            Iterator i = xmltemplate.elementIterator("edittemplate");
            boolean useEditTemplates = true;
            if (!i.hasNext()) {
                // no edittemplate nodes found, get the template nodes
                i = xmltemplate.elementIterator("TEMPLATE");
                useEditTemplates = false;
            }
            
            while (i.hasNext()) {
                Element currentTemplate = (Element)i.next();
                String bodyName = currentTemplate.attributeValue("name");
                if (bodyName == null || "".equals(bodyName)) {
                    // no template name found, use the parameter body name
                    bodyName = body;
                }
                String bodyContent = null;
                
                if (useEditTemplates) {
                    // no content manipulation needed for edittemplates
                    bodyContent = currentTemplate.getText();
                } else {
                    // parse content for TEMPLATEs
                    if (currentTemplate != null) {
                        StringBuffer contentBuffer = new StringBuffer();
                        for (Iterator k = currentTemplate.nodeIterator(); k.hasNext();) {
                            Node n = (Node)k.next();
                            if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
                                contentBuffer.append(n.getText());
                                continue;
                            } else if (n.getNodeType() == Node.ELEMENT_NODE) {
                                if ("LINK".equals(n.getName())) {
                                    contentBuffer.append(OpenCms.getSystemInfo().getOpenCmsContext());
                                    contentBuffer.append(n.getText());
                                    continue;
                                } 
                            } 
                        }
                        bodyContent = contentBuffer.toString();
                    }
                }
            
                if (bodyContent == null) {
                    throw new Exception("Body content not found");
                }
                
                bodyContent = CmsStringSubstitution.substitute(bodyContent, I_CmsWpConstants.C_MACRO_OPENCMS_CONTEXT, OpenCms.getSystemInfo().getOpenCmsContext());
                
                if (!"".equals(bodyContent.trim())) {
                    xmlPage.addElement(bodyName, locale);
                    xmlPage.setContent(cms, bodyName, locale, bodyContent);
                }
            }
            
            return xmlPage;
        } catch (Exception exc) {
            throw new CmsException(exc.toString());
        }
    }
    
}
