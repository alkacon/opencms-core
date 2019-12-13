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

package org.opencms.configuration.test;

import java.util.function.Consumer;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.digester3.NodeCreateRule;
import org.apache.commons.digester3.Rule;

import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.w3c.dom.Document;

/**
 * Helper class for using JAXB to convert between beans and OpenCms config file XML elements.
 *
 * <p>To use this, first create a JAXB-compatible bean type with the correct annotations, then
 * create an instance of this class as a member in the OpenCms configuration class (e.g.CmsSystemConfiguration..
 * Add the rule returned by getDigesterRule in the config object's addXmlDigesterRules, and call
 * the appendToXml rule in the config object's generateXml method.
 *
 * @param <T> the bean type to convert to/from XML
 */
public class CmsJaxbConfigHelper<T> {

    /** Document builder factory. */
    private static DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();

    /** The JAXB context. */
    private JAXBContext m_jaxb;

    /** Document builder used for serialization. */
    private DocumentBuilder m_docBuilder;

    /**
     * Creates a new instance.
     *
     * @param cls the bean class
     */
    public CmsJaxbConfigHelper(Class<T> cls) {

        try {
            m_jaxb = JAXBContext.newInstance(cls);
            m_docBuilder = DBF.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends the XML element for the bean to the given parent element.
     *
     * @param parent the parent element
     * @param bean the bean to convert to an XML element and append to the parent
     */
    public void appendToXml(Element parent, T bean) {

        if (bean != null) {
            try {
                Document doc = m_docBuilder.newDocument();
                m_jaxb.createMarshaller().marshal(bean, doc);
                DOMReader rd = new DOMReader();
                Element result = rd.read(doc).getRootElement();
                result.detach();
                parent.add(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Gets a rule which uses JAXB to unmarshal a bean from the XML.
     *
     * @param beanCallback the callback to call with the finished bean
     * @return the digester rule
     */
    public Rule getDigesterRule(Consumer<T> beanCallback) {

        try {
            return new NodeCreateRule() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void end(String namespace, String name) throws Exception {

                    org.w3c.dom.Node root = (org.w3c.dom.Node)(getDigester().pop());
                    T bean = (T)(m_jaxb.createUnmarshaller().unmarshal(root));
                    beanCallback.accept(bean);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
