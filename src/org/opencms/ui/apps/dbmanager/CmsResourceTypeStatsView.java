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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.CmsVaadinUtils.PropertyId;

import org.apache.commons.logging.Log;

import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the database statistic view.<p>
 */
public class CmsResourceTypeStatsView extends VerticalLayout {

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsResourceTypeStatsView.class.getName());

    /**Result list.*/
    static CmsResourceTypeStatResultList results;

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5884776533727327584L;

    /**CmsObject.*/
    CmsObject m_cms;

    /**Vaadin component.*/
    Button m_ok;

    /**Vaadin component.*/
    VerticalLayout m_resLayout;

    /**Vaadin component.*/
    ComboBox m_resType;

    /**Vaadin component.*/
    Panel m_results;

    /**Vaadin component.*/
    ComboBox m_siteSelect;

    /**
     * public constructor.<p>
     */
    public CmsResourceTypeStatsView() {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        results = CmsResourceTypeStatResultList.init(results);
        results.setVerticalLayout(m_resLayout, true);
        if (results.isEmpty()) {
            m_results.setVisible(false);
        }
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());

            setClickListener();

            setupResourceType();

            final IndexedContainer availableSites = CmsVaadinUtils.getAvailableSitesContainer(m_cms, "caption");

            m_siteSelect.setContainerDataSource(availableSites);
            if (availableSites.getItem(m_cms.getRequestContext().getSiteRoot()) != null) {
                m_siteSelect.setValue(m_cms.getRequestContext().getSiteRoot());
            }
            m_siteSelect.setNullSelectionAllowed(false);
            m_siteSelect.setItemCaptionPropertyId("caption");
        } catch (CmsException e1) {
            LOG.error("Unable to get CmsObject", e1);
        }
    }

    /**
     * Sets the cms object to selected site and returns it.<p>
     *
     * @return CmsObject
     */
    protected CmsObject getCmsObject() {

        m_cms.getRequestContext().setSiteRoot((String)m_siteSelect.getValue());
        return m_cms;
    }

    /**
     * Returns the type which is selected by UI.<p>
     *
     * @return I_CmsResourceType
     */
    protected I_CmsResourceType getType() {

        return (I_CmsResourceType)m_resType.getValue();
    }

    /**
     * Sets the click listener for the ok button.<p>
     */
    private void setClickListener() {

        m_ok.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 6621475980210242125L;

            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {

                try {
                    CmsResourceFilter filter = getType() == null
                    ? CmsResourceFilter.ALL
                    : CmsResourceFilter.requireType(getType());
                    results.addResult(
                        new CmsResourceTypeStatResult(
                            getType(),
                            (String)m_siteSelect.getValue(),
                            getCmsObject().readResources("/", filter, true).size()));
                    m_results.setVisible(true);
                    results.setVerticalLayout(m_resLayout, false);
                } catch (CmsException e) {
                    LOG.error("Unable to read resource tree", e);
                }
            }
        });
    }

    /**
     * Sets up the combo box for choosing the resource type.<p>
     */
    private void setupResourceType() {

        IndexedContainer resTypes = CmsVaadinUtils.getResourceTypesContainer();
        resTypes.addContainerFilter(CmsVaadinUtils.FILTER_NO_FOLDERS);
        m_resType.setContainerDataSource(resTypes);
        m_resType.setItemCaptionPropertyId(PropertyId.caption);
        m_resType.setItemIconPropertyId(PropertyId.icon);
    }
}
