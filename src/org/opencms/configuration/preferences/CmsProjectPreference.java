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

package org.opencms.configuration.preferences;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Preference subclass for selecting the start project.<p>
 */
public class CmsProjectPreference extends CmsBuiltinPreference {

    /** The nice name. */
    private static final String NICE_NAME = "%(key."
        + org.opencms.workplace.commons.Messages.GUI_PREF_STARTUP_PROJECT_0
        + ")";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProjectPreference.class);

    /**
     * Creates a new instance.<p>
     *
     * @param name the name of the project
     */
    public CmsProjectPreference(String name) {

        super(name);
        m_basic = false;
    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition() {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            null, //widget
            null, //widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            NICE_NAME, //nicename
            null, //description
            null, //error
            null//preferfolder
        );
        return prop;
    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition(CmsObject cms) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            "select_notnull", //widget
            getProjectSelectOptions(cms, locale), //widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            NICE_NAME, //nicename
            null, //description
            null, //error
            null//preferfolder)
        );
        return prop;
    }

    /**
     * Gets the options for the project selector.<p>
     *
     * @param cms  the CMS context
     * @param  locale the locale
     *
     * @return the options for the project selector
     */
    private String getProjectSelectOptions(CmsObject cms, Locale locale) {

        List<CmsProject> allProjects;
        try {
            String ouFqn = "";
            CmsUserSettings settings = new CmsUserSettings(cms);
            if (!settings.getListAllProjects()) {
                ouFqn = cms.getRequestContext().getCurrentUser().getOuFqn();
            }
            allProjects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(
                cms,
                ouFqn,
                settings.getListAllProjects());
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            allProjects = Collections.emptyList();
        }

        boolean singleOu = true;
        String ouFqn = null;
        Iterator<CmsProject> itProjects = allProjects.iterator();
        while (itProjects.hasNext()) {
            CmsProject prj = itProjects.next();
            if (prj.isOnlineProject()) {
                // skip the online project
                continue;
            }
            if (ouFqn == null) {
                // set the first ou
                ouFqn = prj.getOuFqn();
            }
            if (!ouFqn.equals(prj.getOuFqn())) {
                // break if one different ou is found
                singleOu = false;
                break;
            }
        }

        int counter = 0;
        StringBuffer resultBuffer = new StringBuffer();
        for (int i = 0, n = allProjects.size(); i < n; i++) {
            CmsProject project = allProjects.get(i);
            String projectName = project.getSimpleName();
            if (!singleOu && !project.isOnlineProject()) {
                try {
                    projectName = projectName
                        + " - "
                        + OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, project.getOuFqn()).getDisplayName(
                            locale);
                } catch (CmsException e) {
                    projectName = projectName + " - " + project.getOuFqn();
                }
            }
            if (counter != 0) {
                resultBuffer.append("|");
            }
            counter++;
            resultBuffer.append(project.getName()).append(":").append(projectName);
        }
        return resultBuffer.toString();
    }

}
