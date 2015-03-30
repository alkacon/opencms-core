/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace;

import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.ui.A_CmsUI;
import org.opencms.workplace.ui.CmsFileBrowser;

import java.util.List;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;

@Theme("valo")
public class CmsWorkplaceUI extends A_CmsUI {

    private HorizontalLayout m_buttonBar;

    private Label m_statusBar;

    private AbsoluteLayout m_explorerView;

    private HorizontalLayout m_explorerAdressBar;

    private CmsFileBrowser m_fileBrowser;

    private Tree m_fileTree;

    private Table m_fileTable;

    private AbsoluteLayout m_mainLayout;

    private CmsWorkplaceSettings m_workplaceSettings;

    public static final String CLOSE_FUNCTION = "cmsRequestCloseWindow";

    public static CmsWorkplaceUI get() {

        return (CmsWorkplaceUI)(UI.getCurrent());
    }

    @Override
    protected void init(VaadinRequest request) {

        m_mainLayout = new AbsoluteLayout();
        m_mainLayout.setWidth(100, Unit.PERCENTAGE);
        m_mainLayout.setHeight(100, Unit.PERCENTAGE);
        setContent(m_mainLayout);

        m_buttonBar = new HorizontalLayout();
        Button customButton = new Button("custom");
        m_buttonBar.addComponent(customButton);
        customButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                JavaScript.getCurrent().execute("window.executeServerCommand('grmbl', '');");
            }
        });

        m_buttonBar.setHeight(30, Unit.PIXELS);
        m_mainLayout.addComponent(m_buttonBar, "top: 0px; left: 0px; right: 0px;");

        populateButtonBar();

        m_explorerView = new AbsoluteLayout();
        m_mainLayout.addComponent(m_explorerView, "top: 30px; left: 0px; right: 0px; bottom: 30px;");

        m_explorerAdressBar = new HorizontalLayout();
        m_explorerAdressBar.setHeight(30, Unit.PIXELS);
        m_explorerView.addComponent(m_explorerAdressBar, "top: 0px; left: 0px; right: 0px;");
        m_explorerAdressBar.addComponent(new Label("Explorer address bar"));

        m_fileBrowser = new CmsFileBrowser();
        m_explorerView.addComponent(m_fileBrowser, "top: 30px; left: 0px; right: 0px; bottom: 0px;");
        m_statusBar = new Label("User: " + getCmsObject().getRequestContext().getCurrentUser().getFullName());
        m_statusBar.setHeight(30, Unit.PIXELS);
        m_mainLayout.addComponent(m_statusBar, "left: 0px; right: 0px; bottom: 0px;");
    }

    /**
     * Replaces the site title, if necessary.<p>
     * 
     * @param title the site title
     *  
     * @return the new site title 
     */
    protected String substituteSiteTitle(String title) {

        if (title.equals(CmsSiteManagerImpl.SHARED_FOLDER_TITLE)) {
            return Messages.get().getBundle(getWorkplaceSettings().getUserSettings().getLocale()).key(
                Messages.GUI_SHARED_TITLE_0);
        }
        return title;
    }

    private CmsWorkplaceSettings getWorkplaceSettings() {

        if (m_workplaceSettings == null) {
            VaadinSession session = getCurrent().getSession();
            m_workplaceSettings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);

            if (m_workplaceSettings == null) {
                // create the settings object
                m_workplaceSettings = new CmsWorkplaceSettings();
                m_workplaceSettings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), m_workplaceSettings, false);

                session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, m_workplaceSettings);
            }
        }
        return m_workplaceSettings;
    }

    private void populateButtonBar() {

        ComboBox siteSelect = new ComboBox();
        siteSelect.addContainerProperty("Title", String.class, null);
        siteSelect.setItemCaptionPropertyId("Title");
        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(getCmsObject(), true);
        CmsSite current = null;
        for (CmsSite site : sites) {
            Item selectItem = siteSelect.addItem(site);
            selectItem.getItemProperty("Title").setValue(substituteSiteTitle(site.getTitle()));
            if (((site.getSiteRoot() != null) && site.getSiteRoot().equals(
                getCmsObject().getRequestContext().getSiteRoot()))
                || ((site.getSiteRoot() == null) && (getCmsObject().getRequestContext().getSiteRoot() == null))) {
                current = site;
            }
        }
        siteSelect.setValue(current);
        siteSelect.setImmediate(true);
        siteSelect.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                Notification.show("Selected site: " + ((CmsSite)event.getProperty().getValue()).getTitle());
                getCmsObject().getRequestContext().setSiteRoot(((CmsSite)event.getProperty().getValue()).getSiteRoot());
                m_fileBrowser.populateFolderTree();
                m_fileBrowser.populateFileTable("/");
            }
        });
        m_buttonBar.addComponent(siteSelect);
    }
}
