/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentFactory.java,v $
 * Date   : $Date: 2004/10/16 08:24:38 $
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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsXmlContentLoader;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletRequest;

import org.dom4j.Document;
import org.xml.sax.EntityResolver;

/**
 * Provides factory methods to unmarshal (read) an XML content object.<p> 
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.5.0
 */
public final class CmsXmlContentFactory {

    /**
     * No instances of this class should be created.<p> 
     */
    private CmsXmlContentFactory() {

        // noop
    }

    /**
     * Creates a valid XML content document for the given content definition, 
     * containing one empty element in the given locale.<p>
     * 
     * @param locale the locale to create the XML content for
     * @param contentDefinition the content definition to create the XML content for
     * 
     * @return a valid XML page document
     */
    public static Document createDocument(Locale locale, CmsXmlContentDefinition contentDefinition) {

        return contentDefinition.createDocument(locale);
    }

    /**
     * Creates a valid XML content String representation for the given content definition, 
     * containing one empty element in the given locale.<p>
     * 
     * @param locale the locale to create the XML content for
     * @param encoding the encoding to use when creating the String from the XML 
     * @param contentDefinition the content definition to create the XML content for
     * 
     * @return a valid XML page document as a String
     */
    public static String createDocument(Locale locale, String encoding, CmsXmlContentDefinition contentDefinition) {

        try {
            return CmsXmlUtils.marshal(createDocument(locale, contentDefinition), encoding);
        } catch (CmsXmlException e) {
            // this should never happen
            OpenCms.getLog(CmsXmlContentFactory.class).error("Could not create XML document", e);
            return null;
        }
    }

    /**
     * Factory method to unmarshal (read) a XML content instance from a byte array
     * that contains XML data.<p>
     * 
     * When unmarshalling, the encoding is read directly from the XML header. 
     * The given encoding is used only when marshalling the XML again later.<p>
     * 
     * @param xmlData the XML data in a byte array
     * @param encoding the encoding to use when marshalling the XML content later
     * @param resolver the XML entitiy resolver to use
     * @return a XML content instance unmarshalled from the byte array
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContent unmarshal(byte[] xmlData, String encoding, EntityResolver resolver)
    throws CmsXmlException {

        return new CmsXmlContent(CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding, resolver);
    }

    /**
     * Factory method to unmarshal (read) a XML content instance from a OpenCms VFS file
     * that contains XML data.<p>
     * 
     * @param cms the current cms object
     * @param file the file with the XML data to unmarshal
     * @return a XML page instance unmarshalled from the provided file
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContent unmarshal(CmsObject cms, CmsFile file) throws CmsXmlException {

        return unmarshal(cms, file, true);
    }

    /**
     * Factory method to unmarshal (read) a XML content instance from a OpenCms VFS file
     * that contains XML data, using wither the encoding set
     * in the XML file header, or the encoding set in the VFS file property.<p>
     * 
     * If you are not sure about the implications of the encoding issues, 
     * use {@link #unmarshal(CmsObject, CmsFile)} instead.<p>
     * 
     * @param cms the current cms object
     * @param file the file with the XML data to unmarshal
     * @param keepEncoding if true, the encoding spefified in the XML header is used, 
     *    otherwise the encoding from the VFS file property is used
     * @return a XML content instance unmarshalled from the provided file
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContent unmarshal(CmsObject cms, CmsFile file, boolean keepEncoding) throws CmsXmlException {

        byte[] content = file.getContents();
        String filename = cms.getSitePath(file);

        String encoding = null;
        try {
            encoding = cms.readPropertyObject(filename, I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true).getValue();
        } catch (CmsException e) {
            // encoding will be null 
        }
        if (encoding == null) {
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        } else {
            encoding = CmsEncoder.lookupEncoding(encoding, null);
            if (encoding == null) {
                throw new CmsXmlException("Invalid content-encoding property set for xml content '" + filename + "'");
            }
        }

        CmsXmlContent newContent;
        if (content.length > 0) {
            // content is initialized
            if (keepEncoding) {
                // use the encoding from the content
                newContent = unmarshal(content, encoding, new CmsXmlEntityResolver(cms));
            } else {
                // use the encoding from the file property
                // this usually only triggered by a save operation                
                try {
                    String contentStr = new String(content, encoding);
                    newContent = unmarshal(contentStr, encoding, new CmsXmlEntityResolver(cms));
                } catch (UnsupportedEncodingException e) {
                    // this will not happen since the encodig has already been validated
                    throw new CmsXmlException("Invalid content-encoding property set for xml content '"
                        + filename
                        + "'");
                }
            }
        } else {
            // content is empty
            newContent = new CmsXmlContent(encoding, new CmsXmlEntityResolver(cms));
        }

        // set the file
        newContent.setFile(file);
        return newContent;
    }

    /**
     * Factory method to unmarshal (read) a XML content instance from
     * a resource, using the request attributes as cache.<p>
     * 
     * @param cms the current OpenCms context object
     * @param resource the resource to unmarshal
     * @param req the current request
     * 
     * @return the unmarshaled xml content, or null if the given resource was not of type {@link org.opencms.file.types.CmsResourceTypeXmlContent}
     * 
     * @throws CmsException in something goes wrong
     */
    public static CmsXmlContent unmarshal(CmsObject cms, CmsResource resource, ServletRequest req) throws CmsException {

        String rootPath = resource.getRootPath();

        if (!(OpenCms.getResourceManager().getLoader(resource) instanceof CmsXmlContentLoader)) {
            // sanity check: resource must be of type XML content
            throw new CmsXmlException("Resource '"
                + cms.getSitePath(resource)
                + "' is not of required type XML content");
        }

        // try to get the requested content form the current request attributes 
        CmsXmlContent content = (CmsXmlContent)req.getAttribute(rootPath);

        if (content == null) {
            // unmarshal XML structure from the file content
            content = unmarshal(cms, CmsFile.upgrade(resource, cms));
            // store the content as request attribute for future read requests
            req.setAttribute(rootPath, content);
        }

        return content;
    }
    
    /**
     * Factory method to unmarshal (read) a XML document instance from
     * a filename in the VFS, using the request attributes as cache.<p>
     * 
     * @param cms the current OpenCms context object
     * @param filename the filename of the resource to unmarshal
     * @param req the current request
     * 
     * @return the unmarshaled xml document, or null if the given resource was not of type {@link A_CmsXmlDocument}
     * 
     * @throws CmsException in something goes wrong
     */
    public static A_CmsXmlDocument unmarshal(CmsObject cms, String filename, ServletRequest req) throws CmsException {
        
        // use code from XML page factory implementation
        return CmsXmlPageFactory.unmarshal(cms, filename, req);        
    }    

    /**
     * Factory method to unmarshal (read) a XML content instance from a String
     * that contains XML data.<p>
     * 
     * When unmarshalling, the encoding is read directly from the XML header. 
     * The given encoding is used only when marshalling the XML again later.<p>
     * 
     * @param xmlData the XML data in a String
     * @param encoding the encoding to use when marshalling the XML content later
     * @param resolver the XML entitiy resolver to use
     * @return a XML content instance unmarshalled from the String
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContent unmarshal(String xmlData, String encoding, EntityResolver resolver)
    throws CmsXmlException {

        return new CmsXmlContent(CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding, resolver);
    }
}