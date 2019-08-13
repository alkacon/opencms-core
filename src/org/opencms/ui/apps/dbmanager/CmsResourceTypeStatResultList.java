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

package org.opencms.ui.apps.dbmanager;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.search.CmsSearchReplaceSettings;
import org.opencms.ui.apps.search.CmsSourceSearchApp;
import org.opencms.ui.apps.search.CmsSourceSearchAppConfiguration;
import org.opencms.ui.apps.search.CmsSourceSearchForm.SearchType;
import org.opencms.util.CmsDateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the database-statistic result list.<p>
 */
public class CmsResourceTypeStatResultList {

    /**Time after an entry gets removed.*/
    private static final long MAX_TIME = 24 * 60 * 60 * 1000; //24h

    /**List of results.*/
    private List<CmsResourceTypeStatResult> m_results = new ArrayList<CmsResourceTypeStatResult>();

    /**Was an entry updated? -> remove old entry and add new one to the top of the list.*/
    private boolean m_updated;

    /**
     * Method to initialize the list.
     *
     * @param resList a given instance or null
     * @return an instance
     * */
    public static CmsResourceTypeStatResultList init(CmsResourceTypeStatResultList resList) {

        if (resList == null) {
            return new CmsResourceTypeStatResultList();
        }

        resList.deleteOld();
        return resList;
    }

    /**
     * Adds a result to the list.<p>
     *
     * @param result to be added
     */
    public void addResult(CmsResourceTypeStatResult result) {

        if (!m_results.contains(result)) {
            m_results.add(result);
            m_updated = false;
        } else {
            m_results.remove(result);
            m_results.add(result);
            m_updated = true;
        }
    }

    /**
     * Deletes entries which are older than MAX_TIME.<p>
     */
    public void deleteOld() {

        Iterator<CmsResourceTypeStatResult> iterator = m_results.iterator();
        while (iterator.hasNext()) {
            CmsResourceTypeStatResult res = iterator.next();
            if (isToOld(res)) {
                iterator.remove();
            }
        }
    }

    /**
     * Checks if result list is empty.<p>
     *
     * @return true if result list is empty
     */
    public boolean isEmpty() {

        return m_results.size() == 0;
    }

    /**
     * Sets the layout.<p>
     *
     * @param layout to display the result in
     * @param addAll indicates if the whole list should be added or just the last item
     */
    public void setVerticalLayout(VerticalLayout layout, boolean addAll) {

        if (m_results.size() > 0) {
            if (addAll) {
                for (CmsResourceTypeStatResult result : m_results) {
                    layout.addComponent(getLayoutFromResult(result), 0);
                }
            } else {
                CmsResourceTypeStatResult statResult = m_results.get(m_results.size() - 1);

                if (m_updated) {
                    removeRow(layout, statResult);
                }
                layout.addComponent(getLayoutFromResult(statResult), 0);
            }
        }
    }

    /**
     * Creates a result row.<p>
     *
     * @param statResult result to be displayed
     * @return a row with information about result
     */
    private HorizontalLayout getLayoutFromResult(final CmsResourceTypeStatResult statResult) {

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setWidth("100%");
        hLayout.setHeight("60px");
        hLayout.addStyleName("o-report");

        Label result = new Label();
        result.setContentMode(ContentMode.HTML);
        result.addStyleName("v-scrollable");
        //result.addStyleName("o-report");
        result.setValue(statResult.getResult());

        Label type = new Label();
        type.setWidth("200px");
        type.setContentMode(ContentMode.HTML);
        type.addStyleName("v-scrollable");
        //type.addStyleName("o-report");
        type.setValue(statResult.getTypeTitle());

        Label time = new Label();
        time.setWidth("180px");
        time.setContentMode(ContentMode.HTML);
        time.addStyleName("v-scrollable");
        //time.addStyleName("o-report");
        time.setValue(
            CmsDateUtil.getDateTime(
                new Date(statResult.getTimestamp()),
                java.text.DateFormat.DATE_FIELD,
                A_CmsUI.get().getLocale()));

        Button showList = new Button(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_STATS_LIST_0));
        showList.setWidth("100px");

        showList.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 2665235403970750534L;

            public void buttonClick(ClickEvent event) {

                CmsSearchReplaceSettings settings = new CmsSearchReplaceSettings();
                settings.setPaths(Collections.singletonList("/"));
                settings.setSiteRoot(statResult.getSiteRoot());
                settings.setSearchpattern(".*");
                if (statResult.getType() != null) {
                    settings.setTypes(statResult.getType().getTypeName());
                }
                settings.setType(SearchType.fullText);
                CmsAppWorkplaceUi.get().showApp(
                    CmsSourceSearchAppConfiguration.APP_ID,
                    CmsSourceSearchApp.generateState(settings));
            }
        });

        hLayout.addComponent(type);
        hLayout.addComponent(result);
        hLayout.addComponent(time);
        hLayout.addComponent(showList);
        hLayout.setExpandRatio(result, 1);
        hLayout.setData(statResult);
        hLayout.setSpacing(true);
        hLayout.setComponentAlignment(showList, Alignment.MIDDLE_CENTER);
        hLayout.setComponentAlignment(time, Alignment.MIDDLE_CENTER);
        hLayout.setComponentAlignment(result, Alignment.MIDDLE_CENTER);
        hLayout.setComponentAlignment(type, Alignment.MIDDLE_CENTER);
        return hLayout;
    }

    /**
     * Checks if given results is to old.<p>
     *
     * @param res to be checked
     * @return true if result should be removed
     */
    private boolean isToOld(CmsResourceTypeStatResult res) {

        return ((System.currentTimeMillis() - res.getTimestamp()) > MAX_TIME);
    }

    /**
     * Removes result row representing given results.<p>
     *
     * @param layout with results
     * @param result to be removed
     */
    private void removeRow(VerticalLayout layout, CmsResourceTypeStatResult result) {

        Component componentToRemove = null;
        Iterator<Component> iterator = layout.iterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            if (component instanceof HorizontalLayout) {
                if (result.equals(((HorizontalLayout)component).getData())) {
                    componentToRemove = component;
                }
            }
        }
        if (componentToRemove != null) {
            layout.removeComponent(componentToRemove);
        }
    }
}
