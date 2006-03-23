/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/xml/Attic/A_CmsSetupXmlUpdate.java,v $
 * Date   : $Date: 2006/03/23 17:47:21 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.xml;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.i18n.CmsEncoder;
import org.opencms.setup.CmsSetupBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Node;

/**
 * Skeleton for xml update plugins.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.8 
 */
public abstract class A_CmsSetupXmlUpdate implements I_CmsSetupXmlUpdate {

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#execute(org.opencms.setup.CmsSetupBean)
     */
    public void execute(CmsSetupBean setupBean) throws Exception {

        Document doc = setupBean.getXmlHelper().getDocument(getXmlFilename());
        Iterator itRemove = getXPathsToRemove().iterator();
        while (itRemove.hasNext()) {
            String xpath = (String)itRemove.next();
            CmsSetupXmlHelper.setValue(doc, xpath, null);
        }
        Iterator itUpdate = getXPathsToUpdate().iterator();
        while (itUpdate.hasNext()) {
            String xpath = (String)itUpdate.next();
            executeUpdate(doc, xpath);
        }
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getCodeToChange(org.opencms.setup.CmsSetupBean)
     */
    public String getCodeToChange(CmsSetupBean setupBean) throws Exception {

        String ret = "";
        Document doc = setupBean.getXmlHelper().getDocument(getXmlFilename());

        // get the nodes to be deleted
        Iterator itRemove = getXPathsToRemove().iterator();
        while (itRemove.hasNext()) {
            String xpath = (String)itRemove.next();
            Node node = doc.selectSingleNode(xpath);
            if (node != null) {
                ret += CmsXmlUtils.marshal(node, CmsEncoder.ENCODING_UTF_8);
            }
        }

        // create new temp doc to modify
        String parentPath = null;
        // could be better done...
        Document newDoc = prepareDoc(doc);

        boolean modified = false;
        // update the temp doc
        Iterator itUpdate = getXPathsToUpdate().iterator();
        while (itUpdate.hasNext()) {
            String xpath = (String)itUpdate.next();
            Node node = doc.selectSingleNode(xpath);
            if (parentPath == null && getNodeRelation() > 0) {
                parentPath = (!xpath.startsWith("//*") || node == null ? xpath : node.getUniquePath());
                for (int i = getNodeRelation(); i > 0; i--) {
                    parentPath = CmsXmlUtils.removeLastXpathElement(parentPath);
                }
            }
            if (node != null) {
                // could be better done...
                if (!xpath.startsWith("//*")) {
                    CmsSetupXmlHelper.setValue(newDoc, CmsXmlUtils.removeLastXpathElement(xpath), " ");
                    node = (Node)node.clone();
                    node.setParent(null);
                    ((Branch)newDoc.selectSingleNode(CmsXmlUtils.removeLastXpathElement(xpath))).add(node);
                }
            }
            boolean exe = executeUpdate(newDoc, xpath);
            modified = modified || exe;
            if (getNodeRelation() == 0 && exe) {
                Node node2 = newDoc.selectSingleNode(xpath);
                if (node2 != null) {
                    ret += CmsXmlUtils.marshal(node2, CmsEncoder.ENCODING_UTF_8);
                }
            }
        }
        if (parentPath != null && modified) {
            Node node = newDoc.selectSingleNode(parentPath);
            if (node != null) {
                ret += CmsXmlUtils.marshal(node, CmsEncoder.ENCODING_UTF_8);
            }
        }
        return ret.trim();
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#validate(org.opencms.setup.CmsSetupBean)
     */
    public boolean validate(CmsSetupBean setupBean) throws Exception {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(getCodeToChange(setupBean));
    }

    /**
     * Executes the adding/updating changes on the given document.<p>
     * 
     * Only needs to be overriden if {@link #getXPathsToUpdate()} is not empty.<p>
     * 
     * @param document the document to apply the changes to
     * @param xpath the xpath to execute the changes for
     * 
     * @return if something was modified
     */
    protected boolean executeUpdate(Document document, String xpath) {

        // do something to avoid warning
        return ((Object)document == (Object)xpath);
    }

    /**
     * Returns the degree of node relation, zero is no relation, one is parent relation and so on.<p>
     * 
     * @return the degree of node relation
     */
    protected int getNodeRelation() {

        return 1;
    }

    /**
     * Returns a list of xpaths for the nodes to remove.<p>
     * 
     * @return a list of strings
     */
    protected List getXPathsToRemove() {

        return Collections.emptyList();
    }

    /**
     * Returns a list of xpaths for the nodes to add/update.<p>
     * 
     * @return a list of strings
     */
    protected List getXPathsToUpdate() {

        return Collections.emptyList();
    }

    /**
     * Prepares a new document.<p>
     * @param doc the original document
     * 
     * @return a new document 
     */
    protected Document prepareDoc(Document doc) {

        Document newDoc = new DocumentFactory().createDocument();
        newDoc.addElement(CmsConfigurationManager.N_ROOT);
        newDoc.setName(doc.getName());
        return newDoc;
    }
}