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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.i18n.CmsLocaleGroup;
import org.opencms.i18n.CmsLocaleGroupService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

/**
 * View used to compare sitemaps across locales.<p>
 */
public class CmsLocaleComparePanel extends VerticalLayout implements I_CmsLocaleCompareContext {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLocaleComparePanel.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** Icon for the main locale option in select boxes. */
    public static final Resource MAIN_LOCALE_ICON = FontOpenCms.CIRCLE_INFO;

    /** The parent layout of the tree. */
    protected CssLayout m_treeContainer = new CssLayout();

    /** The selected comparison locale. */
    private Locale m_comparisonLocale;

    /** The comparison locale selector. */
    private ComboBox m_comparisonLocaleSelector;

    /** The current root of the tree. */
    private CmsResource m_currentRoot;

    /** Flag which is set while the user switches one of the locales. */
    private boolean m_handlingLocaleChange;

    /** The root locale (locale of the root resource). */
    private Locale m_rootLocale;

    /** The root locale selector. */
    private ComboBox m_rootLocaleSelector;

    /**
     * Creates a new instance.<p>
     *
     * @param id the id of a sitemap entry
     */
    public CmsLocaleComparePanel(String id) {
        super();
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(A_CmsUI.getCmsObject());
        A_CmsUI.get().setLocale(locale);
        try {
            initialize(new CmsUUID(id), null);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * @see org.opencms.ui.sitemap.I_CmsLocaleCompareContext#getComparisonLocale()
     */
    public Locale getComparisonLocale() {

        return m_comparisonLocale;

    }

    /**
     * Gets the locales selectable as comparison locales.<p>
     *
     * @return the possible comparison locales
     */
    public List<Locale> getComparisonLocales() {

        CmsObject cms = A_CmsUI.getCmsObject();
        cms.getLocaleGroupService();
        List<Locale> result = CmsLocaleGroupService.getPossibleLocales(cms, m_currentRoot);
        return result;
    }

    /**
     * @see org.opencms.ui.sitemap.I_CmsLocaleCompareContext#getLocaleGroup()
     */
    public CmsLocaleGroup getLocaleGroup() {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            CmsLocaleGroupService service = cms.getLocaleGroupService();
            return service.readLocaleGroup(m_currentRoot);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * @see org.opencms.ui.sitemap.I_CmsLocaleCompareContext#getRoot()
     */
    public CmsResource getRoot() {

        return m_currentRoot;
    }

    /**
     * @see org.opencms.ui.sitemap.I_CmsLocaleCompareContext#getRootLocale()
     */
    public Locale getRootLocale() {

        return m_rootLocale;
    }

    /**
     * Initializes the locale comparison view.<p>
     *
     * @param id the structure id of the currrent sitemap root entry
     * @param initialComparisonLocale if not null, the initially selected ccomparison locale
     *
     * @throws CmsException if something goes wrong
     */
    public void initialize(CmsUUID id, Locale initialComparisonLocale) throws CmsException {

        removeAllComponents();
        CmsObject cms = A_CmsUI.getCmsObject();
        CmsResource res = cms.readResource(id);
        m_currentRoot = res;
        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(res.getRootPath());

        Locale rootLocale = OpenCms.getLocaleManager().getDefaultLocale(cms, res);
        m_rootLocale = rootLocale;
        Locale mainLocale = site.getMainTranslationLocale(null);
        List<Locale> secondaryLocales = site.getSecondaryTranslationLocales();

        List<Locale> possibleLocaleSelections = getMainLocaleSelectOptions(cms, res, mainLocale, secondaryLocales);
        m_rootLocaleSelector = new ComboBox();
        m_rootLocaleSelector.addStyleName("o-sitemap-localeselect");
        m_rootLocaleSelector.setNullSelectionAllowed(false);
        for (Locale selectableLocale : possibleLocaleSelections) {
            m_rootLocaleSelector.addItem(selectableLocale);
            m_rootLocaleSelector.setItemIcon(selectableLocale, FontOpenCms.SPACE);
            m_rootLocaleSelector.setItemCaption(
                selectableLocale,
                selectableLocale.getDisplayName(A_CmsUI.get().getLocale()));
        }
        m_rootLocaleSelector.setItemIcon(mainLocale, MAIN_LOCALE_ICON);
        m_rootLocaleSelector.setValue(m_rootLocale);
        m_rootLocaleSelector.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                if (!m_handlingLocaleChange) {
                    m_handlingLocaleChange = true;
                    try {
                        Locale newLocale = (Locale)(event.getProperty().getValue());
                        switchToLocale(newLocale);
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        CmsErrorDialog.showErrorDialog(e);
                    } finally {
                        m_handlingLocaleChange = false;
                    }
                }
            }
        });

        m_comparisonLocaleSelector = new ComboBox();
        m_comparisonLocaleSelector.addStyleName("o-sitemap-localeselect");
        m_comparisonLocaleSelector.setNullSelectionAllowed(false);

        List<Locale> comparisonLocales = getComparisonLocales();
        Locale selectedComparisonLocale = null;
        for (Locale comparisonLocale : comparisonLocales) {
            m_comparisonLocaleSelector.addItem(comparisonLocale);
            m_comparisonLocaleSelector.setItemIcon(comparisonLocale, FontOpenCms.SPACE);
            m_comparisonLocaleSelector.setItemCaption(
                comparisonLocale,
                comparisonLocale.getDisplayName(A_CmsUI.get().getLocale()));
            if ((selectedComparisonLocale == null) && !comparisonLocale.equals(m_rootLocale)) {
                selectedComparisonLocale = comparisonLocale;
            }
            if ((initialComparisonLocale != null)
                && comparisonLocale.equals(initialComparisonLocale)
                && !comparisonLocale.equals(m_rootLocale)) {
                // if an initial comparison locale is given, it should have priority over the first comparison locale
                selectedComparisonLocale = comparisonLocale;
            }

        }
        m_comparisonLocale = selectedComparisonLocale;
        m_comparisonLocaleSelector.setValue(selectedComparisonLocale);
        m_comparisonLocaleSelector.setItemIcon(mainLocale, MAIN_LOCALE_ICON);

        m_comparisonLocaleSelector.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                if (!m_handlingLocaleChange) {
                    m_handlingLocaleChange = true;
                    try {
                        Locale locale = (Locale)(event.getProperty().getValue());
                        if (m_rootLocale.equals(locale)) {
                            Locale oldComparisonLocale = m_comparisonLocale;
                            if (getLocaleGroup().getResourcesByLocale().keySet().contains(oldComparisonLocale)) {
                                m_comparisonLocale = locale;
                                switchToLocale(oldComparisonLocale);
                                updateLocaleWidgets();
                            } else {
                                Notification.show(
                                    CmsVaadinUtils.getMessageText(
                                        Messages.GUI_LOCALECOMPARE_CANNOT_SWITCH_COMPARISON_LOCALE_0));
                                m_comparisonLocaleSelector.setValue(oldComparisonLocale);
                            }
                        } else {
                            m_comparisonLocale = locale;
                            updateLocaleWidgets();
                            initTree(m_currentRoot);
                        }

                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        CmsErrorDialog.showErrorDialog(e);
                    } finally {
                        m_handlingLocaleChange = false;
                    }
                }
            }
        });

        CssLayout localeSelectors = new CssLayout();
        localeSelectors.addStyleName(OpenCmsTheme.SITEMAP_LOCALE_BAR);

        m_rootLocaleSelector.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_MAIN_LOCALE_0));
        m_comparisonLocaleSelector.setCaption(
            CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_COMPARISON_LOCALE_0));

        localeSelectors.setWidth("100%");
        localeSelectors.addComponent(m_rootLocaleSelector);
        localeSelectors.addComponent(m_comparisonLocaleSelector);
        // localeSelectors.setComponentAlignment(wrapper2, Alignment.MIDDLE_RIGHT);

        setSpacing(true);
        addComponent(localeSelectors);
        addComponent(m_treeContainer);
        m_treeContainer.setWidth("100%");
        initTree(res);
    }

    /**
     * @see org.opencms.ui.sitemap.I_CmsLocaleCompareContext#refreshAll()
     */
    public void refreshAll() {

        try {
            initialize(m_currentRoot.getStructureId(), m_comparisonLocale);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Switches the root locale to the given value.<p>
     *
     * @param locale the new root locale
     * @throws CmsException if something goes wrong
     */
    public void switchToLocale(Locale locale) throws CmsException {

        CmsObject cms = A_CmsUI.getCmsObject();
        CmsLocaleGroupService groupService = cms.getLocaleGroupService();
        CmsLocaleGroup localeGroup = groupService.readDefaultFileLocaleGroup(m_currentRoot);
        Collection<CmsResource> resources = localeGroup.getResourcesForLocale(locale);
        if (resources.isEmpty()) {
            LOG.error(
                "Can not switch to locale "
                    + locale
                    + ": no page found in locale group of "
                    + m_currentRoot.getRootPath());
        }
        CmsResource localeVariant = resources.iterator().next();
        if (!localeVariant.isFolder()) {
            CmsResource parentFolder = cms.readParentFolder(localeVariant.getStructureId());
            if (m_comparisonLocale.equals(locale)) {
                m_comparisonLocale = m_rootLocale;
                m_rootLocale = locale;
            } else {
                m_rootLocale = locale;
            }
            updateLocaleWidgets();
            initTree(parentFolder);
        } else {
            LOG.error("locale variant should not be a folder: " + localeVariant.getRootPath());
        }
    }

    /**
     * Initializes the tree with the given resource as a root.<p>
     *
     * @param rootRes the new tree root resource
     * @throws CmsException if something goes wrong
     */
    protected void initTree(CmsResource rootRes) throws CmsException {

        m_currentRoot = rootRes;
        m_treeContainer.removeAllComponents();
        showHeader();
        CmsSitemapTreeController controller = new CmsSitemapTreeController(
            A_CmsUI.getCmsObject(),
            rootRes,
            this,
            m_treeContainer);

        // no need to escape a structure id
        JavaScript.eval(CmsGwtConstants.VAR_LOCALE_ROOT + "='" + rootRes.getStructureId() + "'");

        CmsSitemapUI ui = (CmsSitemapUI)(A_CmsUI.get());
        ui.getSitemapExtension().setSitemapTreeController(controller);
        CmsSitemapTreeNode root1 = controller.createRootNode();
        controller.initEventHandlers(root1);
        m_treeContainer.addComponent(root1);
        controller.onClickOpen(root1);
    }

    /**
     * Shows the current loale values in their corresponding widgets.
     */
    protected void updateLocaleWidgets() {

        m_rootLocaleSelector.setValue(m_rootLocale);
        m_comparisonLocaleSelector.setValue(m_comparisonLocale);
    }

    /**
     * Gets the possible locale values selectable as main locale.<p>
     *
     * @param cms the CMS context
     * @param res the resource
     * @param mainLocale the main locale
     * @param secondaryLocales the secondary locales
     *
     * @return the possible locale selections
     */
    private List<Locale> getMainLocaleSelectOptions(
        CmsObject cms,
        CmsResource res,
        Locale mainLocale,
        List<Locale> secondaryLocales) {

        try {
            CmsLocaleGroup localeGroup = cms.getLocaleGroupService().readDefaultFileLocaleGroup(res);
            List<Locale> result = Lists.newArrayList();
            if (localeGroup.hasLocale(mainLocale)) {
                result.add(mainLocale);
            }
            for (Locale locale : secondaryLocales) {
                if (localeGroup.hasLocale(locale)) {
                    result.add(locale);
                }
            }
            return result;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Lists.newArrayList();
        }
    }

    /**
     * Shows the header for the currently selected sitemap root.<p>
     *
     * @throws CmsException if something goes wrong
     */
    private void showHeader() throws CmsException {

        CmsSitemapUI ui = (CmsSitemapUI)A_CmsUI.get();
        String title = null;
        String description = null;
        String path = null;
        String locale = m_rootLocale.toString();
        CmsObject cms = A_CmsUI.getCmsObject();
        CmsResource targetRes = getRoot();
        if (targetRes.isFolder()) {
            targetRes = cms.readDefaultFile(targetRes, CmsResourceFilter.IGNORE_EXPIRATION);
            if (targetRes == null) {
                targetRes = getRoot();
            }
        }
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, getRoot());
        title = resUtil.getTitle();
        description = resUtil.getGalleryDescription(A_CmsUI.get().getLocale());
        path = OpenCms.getLinkManager().getServerLink(
            cms,
            cms.getRequestContext().removeSiteRoot(targetRes.getRootPath()));
        String iconClasses = CmsIconUtil.getResourceIconClasses(
            OpenCms.getResourceManager().getResourceType(getRoot()).getTypeName(),
            false);
        ui.getSitemapExtension().showInfoHeader(title, description, path, locale, iconClasses);
    }
}
