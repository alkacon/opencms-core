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

package org.opencms.ui.apps.sitemanager;

import org.opencms.main.OpenCms;
import org.opencms.site.CmsSSLMode;
import org.opencms.site.CmsSite;
import org.opencms.ui.CmsVaadinUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.ui.FormLayout;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.AbstractSelect.NewItemHandler;
import com.vaadin.v7.ui.ComboBox;

/**
 * Layout for workplace server configuration.<p>
 */
public class CmsWorkplaceServerWidget extends FormLayout {

    /**vaadin serial id. */
    private static final long serialVersionUID = -2972167045879745058L;

    /**vaadin component. */
    private ComboBox m_encryption;

    /**vaadin component. */
    private ComboBox m_server;

    /**ItemContainer. */
    BeanItemContainer<CmsSite> m_serverContainer;

    /**Flag to protect changes of ValueChangeListener. */
    protected boolean m_do_not_change = false;

    /**
     * Public constructor.<p>
     *
     * @param sites all sites
     * @param server current server
     */
    public CmsWorkplaceServerWidget(List<CmsSite> sites, String server) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsSSLMode sslMode = OpenCms.getSiteManager().getSSLModeForWorkplaceServer(server);
        m_encryption.setContainerDataSource(CmsEditSiteForm.getSSLModeContainer("caption", false, sslMode));
        m_encryption.setItemCaptionPropertyId("caption");
        m_encryption.setNullSelectionAllowed(false);
        m_encryption.setNewItemsAllowed(false);
        m_encryption.select(CmsSSLMode.NO);

        List<CmsSite> sitesWithUrl = sites.stream().filter(site -> site.getSiteMatcher() != null).collect(
            Collectors.toList());
        m_serverContainer = setUpWorkplaceComboBox(sitesWithUrl, m_server, false, server, sslMode);

        m_encryption.select(sslMode);
        m_encryption.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -2628646995757479728L;

            public void valueChange(ValueChangeEvent event) {

                if (m_do_not_change) {
                    return;
                }
                m_do_not_change = true;
                adjustServerPrefix();
                m_do_not_change = false;

            }

        });
        m_server.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 6670411745575147230L;

            public void valueChange(ValueChangeEvent event) {

                if (m_do_not_change) {
                    return;
                }
                m_do_not_change = true;
                adjustSSL();
                m_do_not_change = false;

            }
        });
        adjustSSL();
    }

    /**
     * Sets the combo box for workplace.<p>
     *
     * @param allSites alls available sites
     * @param combo combo box to fill
     * @param nullselect if true, nothing is selected
     * @param defaultValue if set, this value gets chosen
     * @param sslMode CmsSSLMode
     * @return BeanItemContainer
     */
    private static BeanItemContainer<CmsSite> setUpWorkplaceComboBox(
        List<CmsSite> allSites,
        final ComboBox combo,
        boolean nullselect,
        String defaultValue,
        CmsSSLMode sslMode) {

        final List<CmsSite> modSites = new ArrayList<CmsSite>();
        CmsSite siteWithDefaultURL = null;

        String defaultURL = defaultValue;

        for (CmsSite site : allSites) {
            CmsSite si = new CmsSite("dummy", site.getUrl());
            si.setSSLMode(site.getSSLMode());
            modSites.add(si);
            if (defaultValue != null) {
                if (defaultURL.equals(si.getUrl())) { //SSL sensitive ('http'!='https')
                    siteWithDefaultURL = si;
                }
            }
        }
        if (defaultValue != null) {
            if (siteWithDefaultURL == null) {
                siteWithDefaultURL = new CmsSite("dummy", defaultURL);
                siteWithDefaultURL.setSSLMode(sslMode);
                modSites.add(0, siteWithDefaultURL);
            }
        }

        final BeanItemContainer<CmsSite> objects = new BeanItemContainer<CmsSite>(CmsSite.class, modSites);
        combo.setContainerDataSource(objects);
        combo.setNullSelectionAllowed(nullselect);
        combo.setItemCaptionPropertyId("url");
        combo.setValue(siteWithDefaultURL);
        combo.setNewItemsAllowed(true);
        combo.setImmediate(true);
        combo.setNewItemHandler(new NewItemHandler() {

            private static final long serialVersionUID = -4760590374697520609L;

            public void addNewItem(String newItemCaption) {

                CmsSite newItem = new CmsSite("dummy", newItemCaption);
                newItem.setSSLMode(newItemCaption.contains("https:") ? CmsSSLMode.MANUAL : CmsSSLMode.NO);
                objects.addBean(newItem);
                combo.select(newItem);
            }
        });
        return objects;
    }

    /**
     * Gets the server url.<p>
     *
     * @return String
     */
    public String getServer() {

        if (m_server.getValue() == null) {
            return "";
        }
        return ((CmsSite)m_server.getValue()).getUrl();
    }

    /**
     * Gets the SSL Mode.<p>
     *
     * @return CmsSSLMode
     */
    public CmsSSLMode getSSLMode() {

        return (CmsSSLMode)m_encryption.getValue();
    }

    /**
     * Adjustes the server prefixes according to SSL setting.<p>
     */
    protected void adjustServerPrefix() {

        CmsSSLMode mode = (CmsSSLMode)m_encryption.getValue();
        if (simpleReturn(mode)) {
            return;
        }
        String toBeReplaced = "http:";
        String newString = "https:";

        if (mode.equals(CmsSSLMode.NO) | mode.equals(CmsSSLMode.SECURE_SERVER)) {
            toBeReplaced = "https:";
            newString = "http:";
        }

        if (((CmsSite)m_server.getValue()).getUrl().contains(toBeReplaced)) {

            String newURL = ((CmsSite)m_server.getValue()).getUrl().replaceAll(toBeReplaced, newString);
            CmsSite newSite = new CmsSite("dummy", newURL);
            if (!m_serverContainer.containsId(newSite)) {
                m_serverContainer.addItem(newSite);
            }
            m_server.select(newSite);
        }

    }

    /**
     * Adjustes the SSL according to server name.<p>
     */
    protected void adjustSSL() {

        if (simpleReturn(null)) {
            return;
        }
        CmsSSLMode siteMode = ((CmsSite)m_server.getValue()).getSSLMode();
        m_encryption.setValue(siteMode.equals(CmsSSLMode.SECURE_SERVER) ? CmsSSLMode.NO : siteMode); //Set mode according to site, don't allow Secure Server

    }

    /**
     * Checks if adjust methods can early return.<p>
     *
     * @param mode to be checked.
     * @return true if no adjustment is needed
     */
    private boolean simpleReturn(CmsSSLMode mode) {

        if (m_server.getValue() == null) {
            return true;
        }

        if (mode != null) {

            if (mode.equals(CmsSSLMode.NO)) {
                if (((CmsSite)m_server.getValue()).getUrl().contains("http:")) {
                    return true;
                }
            } else {
                if (((CmsSite)m_server.getValue()).getUrl().contains("https:")) {
                    return true;
                }
            }
        }
        return false;
    }

}
