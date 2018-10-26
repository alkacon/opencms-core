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

package org.opencms.ui.apps.searchindex;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the GUI to rebuild indexes.<p>
 */
public class CmsSearchindexRebuild extends VerticalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -3306537840428458751L;

    /**vaadin component.*/
    private Label m_confirm;

    /**List of indexes to rebuild.*/
    private List<String> m_indexList;

    /**vaadin component.*/
    private FormLayout m_layout;

    /**Instance of calling app.*/
    private CmsSearchindexApp m_manager;

    /**vaadin component.*/
    private Button m_ok;

    /**vaadin component.*/
    private Panel m_reportPanel;

    /**vaadin component.*/
    private Panel m_startPanel;

    /**
     * public constructor.<p>
     * @param app instance
     * @param data indexes to be updated
     */
    public CmsSearchindexRebuild(CmsSearchindexApp app, Set<String> data) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        setHeight("570px");
        m_manager = app;
        m_reportPanel.setVisible(false);
        m_confirm.setValue(
            CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_REBUILD_CONFIRM_1, getCommaSeperatedIndexes(data)));

        m_indexList = new ArrayList<String>(data);

        m_ok.addClickListener(new ClickListener() {

            /**vaadin serial id.*/
            private static final long serialVersionUID = 7361499756763447027L;

            public void buttonClick(ClickEvent event) {

                startThread();

            }
        });
    }

    /**
     * Starts the rebuild thread.<p>
     */
    protected void startThread() {

        m_reportPanel.setVisible(true);
        m_startPanel.setVisible(false);
        Component report = m_manager.getUpdateThreadComponent(m_indexList);
        report.setHeight("500px");
        report.setWidth("100%");
        m_layout.removeAllComponents();
        m_layout.addComponent(report);
    }

    /**
     * Creates a comma seperated string with all indexes represented by a string using the CmsSearchindexApp.SEPERATOR_INDEXNAMES.<p>
     *
     * @param data list of indexes seperated by CmsSearchindexApp.SEPERATOR_INDEXNAMES
     * @return string representation of indexes
     */
    private String getCommaSeperatedIndexes(Set<String> data) {

        Iterator<String> it = data.iterator();
        String res = it.next();
        if (data.size() == 1) {
            return res;
        }
        res += ", ";
        while (it.hasNext()) {
            res += it.next() + ", ";
        }
        res = res.substring(0, res.length() - 2);
        return res;
    }
}
