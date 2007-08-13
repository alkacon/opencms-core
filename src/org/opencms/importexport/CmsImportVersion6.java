/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportVersion6.java,v $
 * Date   : $Date: 2007/08/13 16:30:11 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.importexport;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDataTypeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;

import org.dom4j.Element;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for 
 * the import version 6.<p>
 * 
 * This import format is used in OpenCms since 6.5.6.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.5.6 
 * 
 * @see org.opencms.importexport.A_CmsImport
 */
public class CmsImportVersion6 extends CmsImportVersion5 {

    /** The version number of this import implementation.<p> */
    public static final int IMPORT_VERSION6 = 6;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportVersion6.class);

    /**
     * Creates a new CmsImportVerion7 object.<p>
     */
    public CmsImportVersion6() {

        m_convertToXmlPage = true;
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#getVersion()
     */
    public int getVersion() {

        return CmsImportVersion6.IMPORT_VERSION6;
    }

    /**
     * Imports the OpenCms users.<p>
     * 
     * @throws CmsImportExportException if something goes wrong
     */
    protected void importUsers() throws CmsImportExportException {

        try {
            // getAll user nodes
            List userNodes = m_docXml.selectNodes("//" + CmsImportExportManager.N_USERDATA);
            // walk threw all groups in manifest
            for (int i = 0; i < userNodes.size(); i++) {
                Element currentElement = (Element)userNodes.get(i);

                String name = CmsImport.getChildElementTextValue(currentElement, CmsImportExportManager.N_NAME);
                name = OpenCms.getImportExportManager().translateUser(name);

                // decode passwords using base 64 decoder
                String pwd = CmsImport.getChildElementTextValue(currentElement, CmsImportExportManager.N_PASSWORD);
                String password = new String(Base64.decodeBase64(pwd.trim().getBytes()));

                String flags = CmsImport.getChildElementTextValue(currentElement, CmsImportExportManager.N_FLAGS);
                String firstname = CmsImport.getChildElementTextValue(
                    currentElement,
                    CmsImportExportManager.N_FIRSTNAME);
                String lastname = CmsImport.getChildElementTextValue(currentElement, CmsImportExportManager.N_LASTNAME);
                String email = CmsImport.getChildElementTextValue(currentElement, CmsImportExportManager.N_EMAIL);
                long dateCreated = Long.parseLong(CmsImport.getChildElementTextValue(
                    currentElement,
                    CmsImportExportManager.N_DATECREATED));

                // get the userinfo and put it into the additional info map
                Map userInfo = new HashMap();
                Iterator itInfoNodes = currentElement.selectNodes(
                    "./" + CmsImportExportManager.N_USERINFO + "/" + CmsImportExportManager.N_USERINFO_ENTRY).iterator();
                while (itInfoNodes.hasNext()) {
                    Element infoEntryNode = (Element)itInfoNodes.next();
                    String key = infoEntryNode.attributeValue(CmsImportExportManager.A_NAME);
                    String type = infoEntryNode.attributeValue(CmsImportExportManager.A_TYPE);
                    String value = infoEntryNode.getTextTrim();
                    userInfo.put(key, CmsDataTypeUtil.dataImport(value, type));
                }

                // get the groups of the user and put them into the list
                List groupNodes = currentElement.selectNodes("*/" + CmsImportExportManager.N_GROUPNAME);
                List userGroups = new ArrayList();
                for (int j = 0; j < groupNodes.size(); j++) {
                    Element currentGroup = (Element)groupNodes.get(j);
                    String userInGroup = CmsImport.getChildElementTextValue(currentGroup, CmsImportExportManager.N_NAME);
                    userInGroup = OpenCms.getImportExportManager().translateGroup(userInGroup);
                    userGroups.add(userInGroup);
                }

                // import this user
                importUser(name, flags, password, firstname, lastname, email, dateCreated, userInfo, userGroups);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (Exception e) {
            m_report.println(e);
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_USERS_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
    }
}