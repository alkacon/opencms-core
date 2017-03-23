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

package org.opencms.ui.apps.publishqueue;

import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobFinished;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 *Class for the Publish queue app.<p>
 */
public class CmsPublishQueue extends A_CmsWorkplaceApp {

    /**The icon for history. */
    public static final String ICON_HISTORY = "apps/publishqueue/publish_queue_history.png";

    /**The icon for history. */
    public static final String ICON = "apps/publishqueue/publish_queue.png";

    /**The icon for history. */
    public static final String TABLE_ICON = "apps/publish_queue.png";

    /**job id. */
    public static final String JOB_ID = "jobId";

    /**Path for history table.*/
    public static final String PATH_HISTORY = "history";

    /**Path for report. */
    public static final String PATH_REPORT = "report";

    /**Path for showing published resources.*/
    public static final String PATH_RESOURCE = "resource";

    /** The file table filter input. */
    private TextField m_siteTableFilter;

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        // Check if state is empty -> start
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_TITLE_0));
            return crumbs;
        }

        //Deeper path
        crumbs.put(CmsPublishQueueConfiguration.APP_ID, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_TITLE_0));

        //Report
        if (state.startsWith(PATH_REPORT)) {
            //Over History table?
            if (OpenCms.getPublishManager().getJobByPublishHistoryId(
                new CmsUUID(getJobIdFromState(state))) instanceof CmsPublishJobFinished) {
                crumbs.put(
                    CmsPublishQueueConfiguration.APP_ID + "/" + PATH_HISTORY,
                    CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_HISTORY_QUEUE_0));
            }
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_REPORT_0));
        }

        //History
        if (state.startsWith(PATH_HISTORY)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_HISTORY_QUEUE_0));
        }

        //Resources
        if (state.startsWith(PATH_RESOURCE)) {
            //Over History table?
            if (OpenCms.getPublishManager().getJobByPublishHistoryId(
                new CmsUUID(getJobIdFromState(state))) instanceof CmsPublishJobFinished) {
                crumbs.put(
                    CmsPublishQueueConfiguration.APP_ID + "/" + PATH_HISTORY,
                    CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_HISTORY_QUEUE_0));
            }
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_RESOURCES_0));
        }

        if (crumbs.size() > 1) {
            return crumbs;
        } else {
            return new LinkedHashMap<String, String>(); //size==1 & state was not empty -> state doesn't match to known path
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        //remove filter field
        if (m_siteTableFilter != null) {
            m_infoLayout.removeComponent(m_siteTableFilter);
            m_siteTableFilter = null;
        }

        //default ->
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            m_rootLayout.setMainHeightFull(true);
            return new CmsQueuedTable(this);
        }

        if (state.startsWith(PATH_RESOURCE)) {
            m_rootLayout.setMainHeightFull(false);
            return new CmsPublishResources(this, getJobIdFromState(state));
        }
        if (state.startsWith(PATH_REPORT)) {
            m_rootLayout.setMainHeightFull(false);
            return new CmsPublishReport(this, getJobIdFromState(state));
        }

        m_rootLayout.setMainHeightFull(true);

        if (state.startsWith(PATH_HISTORY)) {
            final CmsHistoryQueuedTable table = new CmsHistoryQueuedTable(this);
            m_siteTableFilter = new TextField();
            m_siteTableFilter.setIcon(FontOpenCms.FILTER);
            m_siteTableFilter.setInputPrompt(
                Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
            m_siteTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
            m_siteTableFilter.setWidth("200px");
            m_siteTableFilter.addTextChangeListener(new TextChangeListener() {

                private static final long serialVersionUID = 1L;

                public void textChange(TextChangeEvent event) {

                    table.filterTable(event.getText());

                }
            });
            m_infoLayout.addComponent(m_siteTableFilter);
            return table;
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        List<NavEntry> subNav = new ArrayList<NavEntry>();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state) | state.startsWith(PATH_HISTORY)) {

            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_HISTORY_QUEUE_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_HISTORY_QUEUE_DESCRIPTION_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_HISTORY)),
                    PATH_HISTORY));

            return subNav;
        }
        //Dialogs for Ressources or Report are shown or path is not valid -> no nav
        return null;

    }

    /**
     * Returns the job id from the given state.<p>
     *
     * @param state the state
     * @return the site root
     */
    private String getJobIdFromState(String state) {

        return A_CmsWorkplaceApp.getParamFromState(state, JOB_ID);
    }

    /**
     * Reads project from given state.<p>
     *
     * @param state state to be read
     * @return project name
     */
    private String getProjectFromState(String state) {

        String job_id = getJobIdFromState(state);
        return OpenCms.getPublishManager().getJobByPublishHistoryId(new CmsUUID(job_id)).getProjectName();
    }

    /**
     * Reads user from given state.<p>
     *
     * @param state state to be read
     * @return user name
     */
    private String getUserFromState(String state) {

        String job_id = getJobIdFromState(state);
        return OpenCms.getPublishManager().getJobByPublishHistoryId(new CmsUUID(job_id)).getUserName(
            A_CmsUI.getCmsObject());
    }

}
