/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentFactory.java,v $
 * Date   : $Date: 2005/02/17 12:45:12 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.xml.sax.EntityResolver;

/**
 * Provides factory methods to unmarshal (read) an XML content object.<p> 
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
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
     * Create a new instance of an XML content based on the given content definiton,
     * that will have one language node for the given locale all initialized with default values.<p> 
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param encoding the encoding to use when marshalling the XML content later
     * @param contentDefinition the content definiton to create the content for
     * 
     * @return the created XML content
     */
    public static CmsXmlContent createDocument(
        CmsObject cms,
        Locale locale,
        String encoding,
        CmsXmlContentDefinition contentDefinition) {

        // create the XML content
        return new CmsXmlContent(cms, locale, encoding, contentDefinition);
    }

    /**
     * Factory method to unmarshal (generate) a XML content instance from a byte array
     * that contains XML data.<p>
     * 
     * When unmarshalling, the encoding is read directly from the XML header of the byte array. 
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

        return unmarshal(CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding, resolver);
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

        byte[] contentBytes = file.getContents();
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

        CmsXmlContent content;
        if (contentBytes.length > 0) {
            // content is initialized
            if (keepEncoding) {
                // use the encoding from the content
                content = unmarshal(contentBytes, encoding, new CmsXmlEntityResolver(cms));
            } else {
                // use the encoding from the file property
                // this usually only triggered by a save operation                
                try {
                    String contentStr = new String(contentBytes, encoding);
                    content = unmarshal(contentStr, encoding, new CmsXmlEntityResolver(cms));
                } catch (UnsupportedEncodingException e) {
                    // this will not happen since the encodig has already been validated
                    throw new CmsXmlException("Invalid content-encoding property set for xml content '"
                        + filename
                        + "'");
                }
            }
        } else {
            // content is empty
            content = new CmsXmlContent(DocumentHelper.createDocument(), encoding, new CmsXmlEntityResolver(cms));
        }

        // set the file
        content.setFile(file);
        // return the result
        return content;
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

        // return the result
        return content;
    }

    /**
     * Factory method to unmarshal (generate) a XML content instance from a XML document.<p>
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param document the XML document to generate the XML content from
     * @param encoding the encoding to use when marshalling the XML content later
     * @param resolver the XML entitiy resolver to use
     * @return a XML content instance unmarshalled from the String
     */
    public static CmsXmlContent unmarshal(Document document, String encoding, EntityResolver resolver) {

        return new CmsXmlContent(document, encoding, resolver);
    }

    /**
     * Factory method to unmarshal (generate) a XML content instance from a String
     * that contains XML data.<p>
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param xmlData the XML data in a String
     * @param encoding the encoding to use when marshalling the XML content later
     * @param resolver the XML entitiy resolver to use
     * @return a XML content instance unmarshalled from the String
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContent unmarshal(String xmlData, String encoding, EntityResolver resolver)
    throws CmsXmlException {

        // create the XML content object from the provided String
        CmsXmlContent content = unmarshal(CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding, resolver);
        // return the result with the user context set
        return content;

    }
}