/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsXmlPageConverter.java,v $
 * Date   : $Date: 2004/02/17 11:40:29 $
 * Version: $Revision: 1.8 $
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
import org.opencms.page.CmsXmlPage;
import org.opencms.util.CmsStringSubstitution;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.Iterator;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * @version $Revision: 1.8 $ $Date: 2004/02/17 11:40:29 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public final class CmsXmlPageConverter {

    /**
     * Constructor made private to avoid class instanciation
     */
    private CmsXmlPageConverter() {
        // noop
    }
    
    /**
     * Converts the contents of a page into an xml page.<p>
     * 
     * @param cms the cms object
     * @param content the content used with xml templates
     * @param body the name of the body
     * @param locale the locale of the body
     * @return the xml page content or null if conversion failed
     * @throws CmsException if something goes wrong
     */
    public static CmsXmlPage convertToXmlPage(CmsObject cms, String content, String body, Locale locale) throws CmsException {

        CmsXmlPage xmlPage = null;
        
        try {
            xmlPage = new CmsXmlPage();
            Document page = CmsImport.getXmlDocument(content);
            
            Element xmltemplate = page.getRootElement();
            if (xmltemplate == null || !"XMLTEMPLATE".equals(xmltemplate.getName())) {
                throw new Exception("Element XMLTEMPLATE not found");
            }
            
            Element edittemplate = xmltemplate.element("edittemplate");
            String bodyContent = null;
            
            if (edittemplate != null) {
                bodyContent = edittemplate.getText();
            } else {
                Element template = xmltemplate.element("TEMPLATE");
                if (template != null) {
                    StringBuffer contentBuffer = new StringBuffer();
                    for (Iterator i = template.nodeIterator(); i.hasNext();) {
                        Node n = (Node)i.next();
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
                        
                        // ignore other node types
                        // contentBuffer.append("<!-- ignored: \n");
                        // contentBuffer.append(n.toString());
                        // contentBuffer.append("\n//-->");
                    }
                    bodyContent = contentBuffer.toString();
                }
            }
        
            if (bodyContent == null) {
                throw new Exception("Body content not found");
            }
            
            bodyContent = CmsStringSubstitution.substitute(bodyContent, I_CmsWpConstants.C_MACRO_OPENCMS_CONTEXT, OpenCms.getSystemInfo().getOpenCmsContext());
            
            xmlPage.addElement(body, locale);
            xmlPage.setContent(cms, body, locale, bodyContent);
            
            return xmlPage;
        } catch (Exception exc) {
            throw new CmsException(exc.toString());
        }
    }
}
