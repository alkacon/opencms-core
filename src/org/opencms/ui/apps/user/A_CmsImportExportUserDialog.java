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

package org.opencms.ui.apps.user;

import org.opencms.db.CmsUserExportSettings;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;

/**
 * Class to export user.<p>
 */
public abstract class A_CmsImportExportUserDialog extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = -3990661225158677324L;

    /**Ou name to export from or import to. */
    protected String m_ou;

    /**Window. */
    protected Window m_window;

    /**
     * Init method.<p>
     *
     * @param ou OU
     * @param window window
     */
    protected void init(String ou, Window window) {

        try {
            displayResourceInfoDirectly(
                Collections.singletonList(
                    CmsAccountsApp.getOUInfo(
                        OpenCms.getOrgUnitManager().readOrganizationalUnit(A_CmsUI.getCmsObject(), ou))));
        } catch (CmsException e) {
            //
        }

        m_ou = ou;
        m_window = window;
        getDownloadButton().setEnabled(true);

        FileDownloader fileDownloader = new FileDownloader(getDownloadResource());
        fileDownloader.extend(getDownloadButton());

        getCloseButton().addClickListener(event -> window.close());
    }

    /**
     * Checks if the user can be exported.<p>
     *
     * @param exportUser the suer to check
     *
     * @return <code>true</code> if the user can be exported
     */
    protected boolean isExportable(CmsUser exportUser) {

        return exportUser.getFlags() < I_CmsPrincipal.FLAG_CORE_LIMIT;
    }

    /**
     * Gets the close button from layout.
     * @return Button
     *  */
    abstract Button getCloseButton();

    /**
     * Gets the download button from layout.
     * @return Button
     *  */
    abstract Button getDownloadButton();

    /**
     * Save export file.<p>
     * @return InputStream
     */
    ByteArrayInputStream getExportStream() {

        Map<CmsUUID, CmsUser> exportUsers = getUserToExport();

        StringBuffer buffer = new StringBuffer();
        CmsUserExportSettings settings = OpenCms.getImportExportManager().getUserExportSettings();

        String separator = CmsStringUtil.substitute(settings.getSeparator(), "\\t", "\t");
        List<String> values = settings.getColumns();

        buffer.append("name");
        Iterator<String> itValues = values.iterator();
        while (itValues.hasNext()) {
            buffer.append(separator);
            buffer.append(itValues.next());
        }
        buffer.append("\n");

        Object[] users = exportUsers.values().toArray();

        for (int i = 0; i < users.length; i++) {
            CmsUser exportUser = (CmsUser)users[i];
            if (!isExportable(exportUser)) {
                continue;
            }
            buffer.append(exportUser.getSimpleName());
            itValues = values.iterator();
            while (itValues.hasNext()) {
                buffer.append(separator);
                String curValue = itValues.next();
                try {
                    Method method = CmsUser.class.getMethod(
                        "get" + curValue.substring(0, 1).toUpperCase() + curValue.substring(1));
                    String curOutput = (String)method.invoke(exportUser);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(curOutput) || curOutput.equals("null")) {
                        curOutput = (String)exportUser.getAdditionalInfo(curValue);
                    }

                    if (curValue.equals("password")) {
                        curOutput = OpenCms.getPasswordHandler().getDigestType() + "_" + curOutput;
                    }

                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(curOutput) && !curOutput.equals("null")) {
                        buffer.append(curOutput);
                    }
                } catch (NoSuchMethodException e) {
                    Object obj = exportUser.getAdditionalInfo(curValue);
                    if (obj != null) {
                        String curOutput = String.valueOf(obj);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(curOutput)) {
                            buffer.append(curOutput);
                        }
                    }
                } catch (IllegalAccessException e) {
                    //
                } catch (InvocationTargetException e) {
                    //
                }
            }
            buffer.append("\n");

        }
        return new ByteArrayInputStream(buffer.toString().getBytes());
    }

    /**
     * Gets the user to be exported.<p>
     *
     * @return Map of user
     */
    abstract Map<CmsUUID, CmsUser> getUserToExport();

    /**
     * Get download resource for export.<p>
     *
     * @return Resource
     */
    private Resource getDownloadResource() {

        return new StreamResource(new StreamResource.StreamSource() {

            private static final long serialVersionUID = -8868657402793427460L;

            public InputStream getStream() {

                return getExportStream();
            }
        }, "User_Export.csv");
    }
}
