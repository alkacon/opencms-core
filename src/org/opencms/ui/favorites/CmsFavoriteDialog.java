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

package org.opencms.ui.favorites;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.ui.components.editablegroup.CmsDefaultActionHandler;
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.CmsEditableGroupButtons;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;

/**
 * Dialog which shows the list of favorites for the current user and allows them to jump to individual favorites,
 * edit the list, or add the current location to the favorite list.
 */
public class CmsFavoriteDialog extends CmsBasicDialog implements CmsEditableGroup.I_RowBuilder {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFavoriteDialog.class);

    /** The Add button. */
    private Button m_addButton;

    /** The Cancel button. */
    private Button m_cancelButton;

    /** The favorite context. */
    private I_CmsFavoriteContext m_context;

    /** The container layout for the favorite widgets. */
    private VerticalLayout m_favContainer;

    /** The Save button. */
    private Button m_saveButton;

    /** Load/save handler for favorites. */
    private CmsFavoriteDAO m_favDao;

    /** Current favorite location. */
    private Optional<CmsFavoriteEntry> m_currentLocation;

    /** The group for the favorite widgets. */
    private CmsEditableGroup m_group;

    /** Map of project labels. */
    private Map<CmsUUID, String> m_projectLabels = new HashMap<>();

    /** Container for the sites, used for their labels. */
    private IndexedContainer m_sitesContainer;

    /**
     * Creates a new dialog instance.
     *
     * @param context the favorite context
     * @param favDao the favorite load/save handler
     */
    public CmsFavoriteDialog(I_CmsFavoriteContext context, CmsFavoriteDAO favDao) {

        super();
        m_favDao = favDao;
        m_context = context;
        context.setDialog(this);
        m_sitesContainer = CmsVaadinUtils.getAvailableSitesContainer(A_CmsUI.getCmsObject(), "caption");
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        List<CmsFavoriteEntry> entries = m_favDao.loadFavorites();
        m_cancelButton.addClickListener(evt -> m_context.close());
        m_saveButton.addClickListener(evt -> onClickSave());
        m_favContainer.addLayoutClickListener(evt -> {
            CmsFavoriteEntry entry = getEntry(evt.getChildComponent());
            m_context.openFavorite(entry);
        });
        m_currentLocation = context.getFavoriteForCurrentLocation();
        m_addButton.setEnabled(m_currentLocation.isPresent());
        m_addButton.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_FAVORITES_ADD_BUTTON_0));
        m_addButton.addClickListener(evt -> onClickAdd());
        m_favContainer.removeAllComponents();
        m_group = new CmsEditableGroup(m_favContainer, null, null);
        m_group.setAddButtonVisible(false);
        m_group.setRowBuilder(this);
        for (CmsFavoriteEntry favEntry : entries) {
            Component favInfo;
            try {
                favInfo = createFavInfo(favEntry);
                m_group.addRow(favInfo);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }

        }
        m_group.getAddButton().setVisible(false);

    }

    /**
     * @see org.opencms.ui.components.editablegroup.CmsEditableGroup.I_RowBuilder#buildRow(org.opencms.ui.components.editablegroup.CmsEditableGroup, com.vaadin.ui.Component)
     */
    public CmsFavInfo buildRow(CmsEditableGroup group, Component component) {

        CmsFavInfo info = (CmsFavInfo)component;
        CmsEditableGroupButtons buttons = new CmsEditableGroupButtons(new CmsDefaultActionHandler(m_group, info));
        info.setButtons(buttons);
        return info;
    }

    /**
     * @see org.opencms.ui.components.CmsBasicDialog#enableMaxHeight()
     */
    @Override
    protected void enableMaxHeight() {
        // do nothing here, we want to disable the max height mechanism
    }

    /**
     * Gets the favorite entries corresponding to the currently displayed favorite widgets.
     *
     * @return the list of favorite entries
     */
    List<CmsFavoriteEntry> getEntries() {

        List<CmsFavoriteEntry> result = new ArrayList<>();
        for (I_CmsEditableGroupRow row : m_group.getRows()) {
            CmsFavoriteEntry entry = ((CmsFavInfo)row).getEntry();
            result.add(entry);
        }
        return result;
    }

    /**
     * Creates a favorite widget for a favorite entry.
     *
     * @param entry the favorite entry
     * @return the favorite widget
     *
     * @throws CmsException if something goes wrong
     */
    private CmsFavInfo createFavInfo(CmsFavoriteEntry entry) throws CmsException {

        String title = "foo";
        String subtitle = "";
        CmsFavInfo result = new CmsFavInfo(entry);
        CmsObject cms = A_CmsUI.getCmsObject();
        String project = getProject(cms, entry);
        String site = getSite(cms, entry);
        try {
            CmsUUID idToLoad = entry.getDetailId() != null ? entry.getDetailId() : entry.getStructureId();
            CmsResource resource = cms.readResource(idToLoad, CmsResourceFilter.IGNORE_EXPIRATION.addRequireVisible());
            CmsResourceUtil resutil = new CmsResourceUtil(cms, resource);
            switch (entry.getType()) {
                case explorerFolder:
                    title = CmsStringUtil.isEmpty(resutil.getTitle())
                    ? CmsResource.getName(resource.getRootPath())
                    : resutil.getTitle();
                    break;
                case page:
                    title = resutil.getTitle();
                    break;
            }
            subtitle = resource.getRootPath();
            CmsResourceIcon icon = result.getResourceIcon();
            icon.initContent(resutil, CmsResource.STATE_UNCHANGED, false, false);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        result.getTopLine().setValue(title);
        result.getBottomLine().setValue(subtitle);
        result.getProjectLabel().setValue(project);
        result.getSiteLabel().setValue(site);

        return result;

    }

    /**
     * Gets the favorite entry for a given row.
     *
     * @param row the widget used to display the favorite
     * @return the favorite entry for the widget
     */
    private CmsFavoriteEntry getEntry(Component row) {

        if (row instanceof CmsFavInfo) {

            return ((CmsFavInfo)row).getEntry();

        }
        return null;

    }

    /**
     * Gets the project name for a favorite entry.
     *
     * @param cms the CMS context
     * @param entry the favorite entry
     * @return the project name for the favorite entry
     * @throws CmsException if something goes wrong
     */
    private String getProject(CmsObject cms, CmsFavoriteEntry entry) throws CmsException {

        String result = m_projectLabels.get(entry.getProjectId());
        if (result == null) {
            result = cms.readProject(entry.getProjectId()).getName();
            m_projectLabels.put(entry.getProjectId(), result);
        }
        return result;
    }

    /**
     * Gets the site label for the entry.
     *
     * @param cms the current CMS context
     * @param entry the entry
     * @return the site label for the entry
     */
    private String getSite(CmsObject cms, CmsFavoriteEntry entry) {

        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(entry.getSiteRoot());
        Item item = m_sitesContainer.getItem(entry.getSiteRoot());
        if (item != null) {
            return (String)(item.getItemProperty("caption").getValue());
        }
        String result = entry.getSiteRoot();
        if (site != null) {
            if (!CmsStringUtil.isEmpty(site.getTitle())) {
                result = site.getTitle();
            }
        }
        return result;
    }

    /**
     * The click handler for the add button.
     */
    private void onClickAdd() {

        if (m_currentLocation.isPresent()) {
            CmsFavoriteEntry entry = m_currentLocation.get();
            List<CmsFavoriteEntry> entries = getEntries();
            entries.add(entry);
            try {
                m_favDao.saveFavorites(entries);
            } catch (Exception e) {
                CmsErrorDialog.showErrorDialog(e);
            }
            m_context.close();
        }
    }

    /**
     * The click handler for the save button.
     */
    private void onClickSave() {

        List<CmsFavoriteEntry> entries = getEntries();
        try {
            m_favDao.saveFavorites(entries);
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
        }
        m_context.close();
    }

}
