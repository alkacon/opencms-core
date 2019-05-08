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
import java.util.ArrayList;
import java.util.Arrays;
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

    /** The "bom" bytes as String that need to be placed at the very beginning of the produced csv. */
    private static final String BOM = "\ufeff";

    /**vaadin serial id. */
    private static final long serialVersionUID = -3990661225158677324L;

    /**Non technical fields to be exported in reduced export. */
    private static final List<String> NON_TECHNICAL_VALUES = Arrays.asList(
        "description",
        "lastname",
        "firstname",
        "email",
        "address",
        "zipcode",
        "city",
        "country");

    /**File downloader. */
    private FileDownloader m_fileDownloader;

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
        initDownloadButton();

        getCloseButton().addClickListener(event -> window.close());
    }

    /**
     * Initializes the download button.<p>
     */
    protected void initDownloadButton() {

        if (m_fileDownloader != null) {
            m_fileDownloader.remove();
        }
        m_fileDownloader = new FileDownloader(getDownloadResource());
        m_fileDownloader.extend(getDownloadButton());
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

        //TODO use same CSV Writer like currently used in Apollo

        Map<CmsUUID, CmsUser> exportUsers = getUserToExport();

        CmsCsvWriter csvWriter = new CmsCsvWriter();

        StringBuffer buffer = new StringBuffer(BOM);

        CmsUserExportSettings settings = OpenCms.getImportExportManager().getUserExportSettings();

        List<String> values = settings.getColumns();

        buffer.append("name");
        Iterator<String> itValues = values.iterator();
        List<String> columns = new ArrayList<String>();
        columns.add("name");
        while (itValues.hasNext()) {
            String colName = itValues.next();
            if (isColumnExportable(colName)) {
                columns.add(colName);
            }
        }
        String[] cols = new String[columns.size()];
        csvWriter.addLine(columns.toArray(cols));

        Object[] users = exportUsers.values().toArray();

        for (int i = 0; i < users.length; i++) {
            CmsUser exportUser = (CmsUser)users[i];
            String[] colValues = new String[columns.size()];
            if (!isExportable(exportUser)) {
                continue;
            }
            colValues[0] = exportUser.getSimpleName();
            int colNumber = 1;
            itValues = values.iterator();
            while (itValues.hasNext()) {

                String curValue = itValues.next();
                if (isColumnExportable(curValue)) {
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
                            colValues[colNumber] = curOutput;
                        }
                    } catch (NoSuchMethodException e) {
                        Object obj = exportUser.getAdditionalInfo(curValue);
                        if (obj != null) {
                            String curOutput = String.valueOf(obj);
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(curOutput)) {
                                colValues[colNumber] = curOutput;
                            }
                        } else {
                            colValues[colNumber] = "";
                        }
                    } catch (IllegalAccessException e) {
                        colValues[colNumber] = "";
                    } catch (InvocationTargetException e) {
                        colValues[colNumber] = "";
                    }
                    if (colValues[colNumber] == null) {
                        colValues[colNumber] = "";
                    }
                    colNumber++;
                }
            }
            csvWriter.addLine(colValues);

        }
        return new ByteArrayInputStream(csvWriter.toString().getBytes());
    }

    /**
     * Gets the user to be exported.<p>
     *
     * @return Map of user
     */
    abstract Map<CmsUUID, CmsUser> getUserToExport();

    /**
     * Export including technical fields.<p>
     *
     * @return boolean
     */
    abstract boolean isExportWithTechnicalFields();

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

    /**
     * Checks if given column is to be exported.<p>
     *
     * @param colName to be checked
     * @return boolean
     */
    private boolean isColumnExportable(String colName) {

        if (isExportWithTechnicalFields()) {
            return true;
        }
        return NON_TECHNICAL_VALUES.contains(colName.toLowerCase());
    }
}
