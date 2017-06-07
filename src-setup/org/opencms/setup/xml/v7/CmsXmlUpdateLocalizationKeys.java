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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.xml.v7;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Updates localization keys.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlUpdateLocalizationKeys extends A_CmsXmlWorkplace {

    /** The new localization keys. */
    private List<String> m_newKeys;

    /** The old localization keys. */
    private List<String> m_oldKeys;

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Update localization keys";
    }

    /**
     * Returns the new Keys.<p>
     *
     * @return the new Keys
     */
    public List<String> getNewKeys() {

        if (m_newKeys == null) {
            m_newKeys = new ArrayList<String>();
            m_newKeys.add("GUI_EXPLORER_CONTEXT_LOCK_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_OVERRIDELOCK_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_UNLOCK_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_COPYTOPROJECT_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_PUBLISH_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_OPENGALLERY_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_RENAMEIMAGES_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_COMMENTIMAGES_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_PAGEEDIT_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_EDITSOURCE_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_EDITLINK_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_EDIT_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_COPY_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_MOVE_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_DELETE_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_REPLACE_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_TOUCH_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_AVAILABILITY_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_UNDOCHANGES_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_UNDELETE_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_SHOWSIBLINGS_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_ACCESS_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_SECURE_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_TYPE_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_CHNAV_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_EDITCONTROLFILE_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_HISTORY_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_PROPERTY_0");
            m_newKeys.add("GUI_EXPLORER_CONTEXT_MOVE_MULTI_0");
        }
        return m_newKeys;
    }

    /**
     * Returns the old Keys.<p>
     *
     * @return the old Keys
     */
    public List<String> getOldKeys() {

        if (m_oldKeys == null) {
            m_oldKeys = new ArrayList<String>();
            m_oldKeys.add("explorer.context.lock");
            m_oldKeys.add("explorer.context.overridelock");
            m_oldKeys.add("explorer.context.unlock");
            m_oldKeys.add("explorer.context.copytoproject");
            m_oldKeys.add("explorer.context.publish");
            m_oldKeys.add("explorer.context.opengallery");
            m_oldKeys.add("explorer.context.renameimages");
            m_oldKeys.add("explorer.context.commentimages");
            m_oldKeys.add("explorer.context.pageedit");
            m_oldKeys.add("explorer.context.editsource");
            m_oldKeys.add("explorer.context.editlink");
            m_oldKeys.add("explorer.context.edit");
            m_oldKeys.add("explorer.context.copy");
            m_oldKeys.add("explorer.context.move");
            m_oldKeys.add("explorer.context.delete");
            m_oldKeys.add("explorer.context.replace");
            m_oldKeys.add("explorer.context.touch");
            m_oldKeys.add("explorer.context.availability");
            m_oldKeys.add("explorer.context.undochanges");
            m_oldKeys.add("explorer.context.undelete");
            m_oldKeys.add("explorer.context.showsiblings");
            m_oldKeys.add("explorer.context.access");
            m_oldKeys.add("explorer.context.secure");
            m_oldKeys.add("explorer.context.type");
            m_oldKeys.add("explorer.context.chnav");
            m_oldKeys.add("explorer.context.editcontrolfile");
            m_oldKeys.add("explorer.context.history");
            m_oldKeys.add("explorer.context.property");
            m_oldKeys.add("explorer.context.move.multi");
        }
        return m_oldKeys;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node != null) {
            String key = xpath.substring(10, xpath.length() - 7);
            int pos = getOldKeys().indexOf(key);
            CmsSetupXmlHelper.setValue(document, xpath, getNewKeys().get(pos));
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/workplace/explorertypes
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsWorkplaceConfiguration.N_WORKPLACE).append("/").append(
                CmsWorkplaceConfiguration.N_EXPLORERTYPES).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // //*[@key='${key}']/@key
            StringBuffer xp = new StringBuffer(256);
            xp.append("//*[@");
            xp.append(I_CmsXmlConfiguration.A_KEY);
            xp.append("='${key}']/@");
            xp.append(I_CmsXmlConfiguration.A_KEY);

            m_xpaths = new ArrayList<String>();
            Iterator<String> it = getOldKeys().iterator();
            while (it.hasNext()) {
                m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${key}", it.next()));
            }
        }
        return m_xpaths;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#prepareDoc(org.dom4j.Document)
     */
    @Override
    protected Document prepareDoc(Document doc) {

        Document newDoc = super.prepareDoc(doc);
        String xpath = getCommonPath();
        Node node = doc.selectSingleNode(xpath);
        CmsSetupXmlHelper.setValue(newDoc, CmsXmlUtils.removeLastComplexXpathElement(xpath), "");
        node = (Node)node.clone();
        node.setParent(null);
        ((Branch)newDoc.selectSingleNode(CmsXmlUtils.removeLastComplexXpathElement(xpath))).add(node);
        return newDoc;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#updateDoc(org.dom4j.Document, org.dom4j.Document, java.lang.String)
     */
    @Override
    protected void updateDoc(Document document, Document newDoc, String xpath) {

        // do nothing
        return;
    }
}