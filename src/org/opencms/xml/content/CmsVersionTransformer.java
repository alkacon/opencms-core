/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.logging.Log;
import org.apache.xerces.parsers.SAXParser;

import org.dom4j.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Provides static methods for XML content version transformations.
 */
public class CmsVersionTransformer {

    /** Logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVersionTransformer.class);

    /** XSL parameter for passing a context object to version transformations. */
    public static final String XSL_PARAM_TRANSFORMATION_CONTEXT = "context";

    /**
     * Converts an XML content document to the current version using the version transformation XSLT file which is configured in the schema.
     *
     * @param cms the current CMS context
     * @param document the document to transform
     * @param contentDefinition the content definition for which we are doing the conversion
     *
     * @return the converted document
     */
    @SuppressWarnings("synthetic-access")
    public static Document transformDocumentToCurrentVersion(
        CmsObject cms,
        Document document,
        CmsXmlContentDefinition contentDefinition) {

        String transformation = contentDefinition.getContentHandler().getVersionTransformation();
        if (transformation == null) {
            LOG.warn(
                "Schema version detected, but no version transformation defined for "
                    + contentDefinition.getSchemaLocation());
            return document;
        }

        try {
            CmsResource xsltResource = cms.readResource(transformation, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsFile xsltFile = cms.readFile(xsltResource);
            // we explicitly want an Xalan transformer factory here, even if we add some other XSLT implementation later,
            // because we rely on specific Xalan features (the way extension functions work).
            TransformerFactory transformerFactory = new org.apache.xalan.processor.TransformerFactoryImpl();
            List<Exception> errors = new ArrayList<>();
            transformerFactory.setErrorListener(new ErrorListener() {

                public void error(TransformerException e) throws TransformerException {

                    errors.add(e);
                    throw e;

                }

                public void fatalError(TransformerException e) throws TransformerException {

                    errors.add(e);
                    throw e;
                }

                public void warning(TransformerException e) {

                    LOG.warn(e.getLocalizedMessage(), e);
                }
            });
            SAXSource transformationSource = new SAXSource(
                new InputSource(new ByteArrayInputStream(xsltFile.getContents())));
            SAXParser parser = new SAXParser();
            parser.setErrorHandler(new ErrorHandler() {

                public void error(SAXParseException e) throws SAXException {

                    errors.add(e);
                    throw e;

                }

                public void fatalError(SAXParseException e) throws SAXException {

                    errors.add(e);
                    throw e;

                }

                public void warning(SAXParseException e) {

                    LOG.warn(e.getLocalizedMessage(), e);

                }
            });
            transformationSource.setXMLReader(parser);
            Transformer transformer = transformerFactory.newTransformer(transformationSource);
            if (errors.size() > 0) {
                throw errors.get(0);
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            Source source = new DOMSource(CmsXmlUtils.convertDocumentFromDom4jToW3C(document));
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document targetDoc = builder.newDocument();
            DOMResult target = new DOMResult(targetDoc);
            transformer.setParameter(XSL_PARAM_TRANSFORMATION_CONTEXT, new CmsXsltContext(cms));
            transformer.transform(source, target);
            if (errors.size() > 0) {
                throw errors.get(0);
            }
            Document result = CmsXmlUtils.convertDocumentFromW3CToDom4j(targetDoc);
            result.getRootElement().addAttribute(CmsXmlContent.A_VERSION, "" + contentDefinition.getVersion());
            if (LOG.isDebugEnabled()) {
                try {
                    LOG.debug(
                        "Used XSL transformation "
                            + transformation
                            + "\n----------------------------"
                            + "\nOriginal XML:"
                            + "\n----------------------------\n"
                            + CmsXmlUtils.marshal(document, "UTF-8")
                            + "\n----------------------------\nTransformed XML:"
                            + "\n----------------------------\n"
                            + CmsXmlUtils.marshal(result, "UTF-8"));
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return result;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRuntimeException(
                Messages.get().container(Messages.ERR_XMLCONTENT_VERSION_TRANSFORMATION_ERROR_1, transformation),
                e);
        }
    }

}
