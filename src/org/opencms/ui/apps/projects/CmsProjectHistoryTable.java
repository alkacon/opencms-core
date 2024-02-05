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

package org.opencms.ui.apps.projects;

import org.opencms.file.CmsObject;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.projects.CmsProjectsTable.ProjectResources;
import org.opencms.util.CmsUUID;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.ui.Table;

/**
 * The project history table.<p>
 */
public class CmsProjectHistoryTable extends Table {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProjectHistoryTable.class);

    /** The serial version id. */
    private static final long serialVersionUID = 7343623156086839992L;

    /** Publish date property. */
    public static final String PROP_PUBLISH_DATE = "publishDate";

    /** Publish user property. */
    public static final String PROP_PUBLISH_USER = "publishUser";

    /** The data container. */
    IndexedContainer m_container;

    /** The project manager instance. */
    CmsProjectManager m_manager;

    /**
     * Constructor.<p>
     */
    public CmsProjectHistoryTable() {

        setSizeFull();
        m_container = new IndexedContainer();
        m_container.addContainerProperty(CmsProjectsTable.PROP_ID, CmsUUID.class, null);
        m_container.addContainerProperty(CmsProjectsTable.PROP_NAME, String.class, "");
        m_container.addContainerProperty(CmsProjectsTable.PROP_DESCRIPTION, String.class, "");
        m_container.addContainerProperty(PROP_PUBLISH_DATE, Date.class, "");
        m_container.addContainerProperty(PROP_PUBLISH_USER, String.class, "");
        m_container.addContainerProperty(CmsProjectsTable.PROP_ORG_UNIT, String.class, "");
        m_container.addContainerProperty(CmsProjectsTable.PROP_OWNER, String.class, "");
        m_container.addContainerProperty(CmsProjectsTable.PROP_MANAGER, String.class, "");
        m_container.addContainerProperty(CmsProjectsTable.PROP_USER, String.class, "");
        m_container.addContainerProperty(CmsProjectsTable.PROP_DATE_CREATED, Date.class, "");
        m_container.addContainerProperty(CmsProjectsTable.PROP_RESOURCES, CmsProjectsTable.ProjectResources.class, "");

        setContainerDataSource(m_container);
        setColumnHeader(CmsProjectsTable.PROP_NAME, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_NAME_0));
        setColumnHeader(
            CmsProjectsTable.PROP_DESCRIPTION,
            CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_DESCRIPTION_0));
        setColumnHeader(PROP_PUBLISH_DATE, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_PUBLISH_DATE_0));
        setColumnHeader(PROP_PUBLISH_USER, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_PUBLISHED_BY_0));
        setColumnHeader(
            CmsProjectsTable.PROP_ORG_UNIT,
            CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_ORG_UNIT_0));
        setColumnHeader(CmsProjectsTable.PROP_OWNER, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_OWNER_0));
        setColumnHeader(
            CmsProjectsTable.PROP_MANAGER,
            CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_MANAGER_GROUP_0));
        setColumnHeader(CmsProjectsTable.PROP_USER, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_USER_GROUP_0));
        setColumnHeader(
            CmsProjectsTable.PROP_DATE_CREATED,
            CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_DATE_CREATED_0));
        setColumnHeader(
            CmsProjectsTable.PROP_RESOURCES,
            CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_RESOURCES_0));

        setSelectable(true);
        setMultiSelect(true);
        addItemClickListener(event -> handleItemClick(event));
        loadProjects();
    }

    /**
     * Loads the projects table.<p>
     */
    public void loadProjects() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale locale = UI.getCurrent().getLocale();
        m_container.removeAllItems();
        boolean isMultiOU = false;
        // hide ou column if only one ou exists
        try {
            isMultiOU = !OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, "", true).isEmpty();
        } catch (CmsException e) {
            // noop
        }
        if (isMultiOU) {
            setVisibleColumns(
                CmsProjectsTable.PROP_NAME,
                CmsProjectsTable.PROP_DESCRIPTION,
                PROP_PUBLISH_DATE,
                PROP_PUBLISH_USER,
                CmsProjectsTable.PROP_ORG_UNIT,
                CmsProjectsTable.PROP_OWNER,
                CmsProjectsTable.PROP_MANAGER,
                CmsProjectsTable.PROP_USER,
                CmsProjectsTable.PROP_DATE_CREATED,
                CmsProjectsTable.PROP_RESOURCES);
        } else {
            setVisibleColumns(
                CmsProjectsTable.PROP_NAME,
                CmsProjectsTable.PROP_DESCRIPTION,
                PROP_PUBLISH_DATE,
                PROP_PUBLISH_USER,
                CmsProjectsTable.PROP_OWNER,
                CmsProjectsTable.PROP_MANAGER,
                CmsProjectsTable.PROP_USER,
                CmsProjectsTable.PROP_DATE_CREATED,
                CmsProjectsTable.PROP_RESOURCES);
        }

        // get content
        try {
            List<CmsHistoryProject> projects = cms.getAllHistoricalProjects();
            for (CmsHistoryProject project : projects) {
                Item item = m_container.addItem(Integer.valueOf(project.getPublishTag()));
                if (item != null) {
                    item.getItemProperty(CmsProjectsTable.PROP_ID).setValue(project.getUuid());
                    item.getItemProperty(CmsProjectsTable.PROP_NAME).setValue(project.getSimpleName());
                    item.getItemProperty(CmsProjectsTable.PROP_DESCRIPTION).setValue(project.getDescription());
                    item.getItemProperty(PROP_PUBLISH_DATE).setValue(new Date(project.getPublishingDate()));
                    item.getItemProperty(PROP_PUBLISH_USER).setValue(project.getPublishedByName(cms));
                    try {
                        item.getItemProperty(CmsProjectsTable.PROP_ORG_UNIT).setValue(
                            OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, project.getOuFqn()).getDisplayName(
                                locale));
                        item.getItemProperty(CmsProjectsTable.PROP_OWNER).setValue(
                            cms.readUser(project.getOwnerId()).getName());
                        item.getItemProperty(CmsProjectsTable.PROP_MANAGER).setValue(
                            cms.readGroup(project.getManagerGroupId()).getSimpleName());
                        item.getItemProperty(CmsProjectsTable.PROP_USER).setValue(
                            cms.readGroup(project.getGroupId()).getSimpleName());
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                    item.getItemProperty(CmsProjectsTable.PROP_DATE_CREATED).setValue(
                        new Date(project.getDateCreated()));

                    StringBuffer html = new StringBuffer(512);
                    ProjectResources resourceList = new ProjectResources(cms.readProjectResources(project));
                    item.getItemProperty(CmsProjectsTable.PROP_RESOURCES).setValue(resourceList);
                }
            }
            m_container.sort(new Object[] {PROP_PUBLISH_DATE}, new boolean[] {false});
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Handles item clicks.
     *
     * @param event the click event
     */
    private void handleItemClick(ItemClickEvent event) {

        if (event.getButton().equals(MouseButton.LEFT)
            && CmsProjectsTable.PROP_RESOURCES.equals(event.getPropertyId())) {
            CmsProjectsTable.showProjectResources(event.getItem());

        }
    }
}
