/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.sites;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFolderSubSitemap;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Dialog for showing the sites details.<p>
 *
 * @since 9.0.0
 */
public class CmsSiteDetailDialog extends CmsWidgetDialog {

    /** The module name constant. */
    public static final String MODULE_NAME = "org.opencms.workplace.tools.sites";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Module parameter constant for the create OU default flag. */
    public static final String PARAM_CREATE_OU = "createou";

    /** Module parameter constant for the web server script. */
    public static final String PARAM_OU_DESCRIPTION = "oudescription";

    /** The dialog action for editing a site. */
    protected static final String DIALOG_EDIT = "edit";

    /** Constant. */
    private static final String BLANK_HTML = "blank.html";

    /** Dialog new action parameter value. */
    private static final String DIALOG_NEW = "new";

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsSiteDetailDialog.class.getName());

    /** Constant. */
    private static final String MODEL_PAGE = "ModelPage";

    /** Constant. */
    private static final String MODEL_PAGE_PAGE = "ModelPage/Page";

    /** Constant. */
    private static final String NEW = ".templates/";

    /** Signals whether to create an OU or not. */
    private boolean m_createou;

    /** The OU description text. */
    private String m_ouDescription;

    /** The edit action to perform. */
    private String m_paramEditaction;

    /** The sites parameter. */
    private String m_paramSites;

    /** The dialog object. */
    private CmsSiteBean m_site;

    /** The name of the sites root folder. */
    private String m_sitename;

    /** The template property. */
    private String m_template;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSiteDetailDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSiteDetailDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        try {

            // validate the dialog form
            validateDialog();

            // create a root site clone of the current CMS object.
            CmsObject cms = OpenCms.initCmsObject(getCms());
            cms.getRequestContext().setSiteRoot("");

            // create the site root path
            String siteRoot = "/sites".concat(m_sitename);
            m_site.setSiteRoot(siteRoot);

            CmsResource siteRootResource = null;
            String sitePath = null;
            // check if the site root already exists
            try {
                // take the existing site and do not perform any OU related actions
                siteRootResource = cms.readResource(siteRoot);
                sitePath = cms.getSitePath(siteRootResource);
            } catch (CmsVfsResourceNotFoundException e) {
                // not create a new site folder and the according OU if option is checked checked
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeFolderSubSitemap.TYPE_SUBSITEMAP);
                siteRootResource = cms.createResource(siteRoot, type);
                sitePath = cms.getSitePath(siteRootResource);
            }

            // add template  property
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getTemplate())) {
                CmsProperty prop = new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_TEMPLATE,
                    getTemplate(),
                    getTemplate());
                cms.writePropertyObject(siteRoot, prop);
            }

            // create OU
            if (m_createou) {
                OpenCms.getOrgUnitManager().createOrganizationalUnit(
                    cms,
                    "/" + siteRootResource.getName(),
                    m_ouDescription.replace("%(site)", m_site.getTitle() + " [" + m_site.getSiteRoot() + "]"),
                    0,
                    siteRootResource.getRootPath());
            }

            // create sitemap configuration
            String contentFolder = CmsStringUtil.joinPaths(sitePath, CmsADEManager.CONTENT_FOLDER_NAME + "/");
            String sitemapConfig = CmsStringUtil.joinPaths(contentFolder, CmsADEManager.CONFIG_FILE_NAME);
            if (!cms.existsResource(sitemapConfig)) {
                CmsResource config = createSitemapContentFolder(cms, siteRootResource);
                if (config != null) {
                    try {
                        CmsResource newFolder = cms.createResource(
                            contentFolder + NEW,
                            OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.RESOURCE_TYPE_NAME));
                        I_CmsResourceType containerType = OpenCms.getResourceManager().getResourceType(
                            org.opencms.file.types.CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME);
                        CmsResource modelPage = cms.createResource(newFolder.getRootPath() + BLANK_HTML, containerType);
                        String defTitle = Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(
                            Messages.GUI_DEFAULT_MODEL_TITLE_1,
                            m_site.getTitle());
                        String defDes = Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(
                            Messages.GUI_DEFAULT_MODEL_DESCRIPTION_1,
                            m_site.getTitle());
                        CmsProperty prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, defTitle, defTitle);
                        cms.writePropertyObject(modelPage.getRootPath(), prop);
                        prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, defDes, defDes);
                        cms.writePropertyObject(modelPage.getRootPath(), prop);
                        CmsFile file = cms.readFile(config);
                        CmsXmlContent con = CmsXmlContentFactory.unmarshal(cms, file);
                        con.addValue(cms, MODEL_PAGE, Locale.ENGLISH, 0);
                        I_CmsXmlContentValue val = con.getValue(MODEL_PAGE_PAGE, Locale.ENGLISH);
                        val.setStringValue(cms, modelPage.getRootPath());
                        file.setContents(con.marshal());
                        cms.writeFile(file);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }

            // update the site manager state
            CmsSite newSite = m_site.toCmsSite();
            OpenCms.getSiteManager().updateSite(getCms(), m_site.getOriginalSite(), newSite);
            // update the workplace server if the changed site was the workplace server
            if ((m_site.getOriginalSite() != null)
                && m_site.getOriginalSite().getUrl().equals(OpenCms.getSiteManager().getWorkplaceServer())) {
                OpenCms.getSiteManager().updateGeneralSettings(
                    getCms(),
                    OpenCms.getSiteManager().getDefaultUri(),
                    newSite.getUrl(),
                    OpenCms.getSiteManager().getSharedFolder());
            }
            // write the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
            // refresh the list of sites
            Map<?, ?> objects = (Map<?, ?>)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsSitesOverviewList.class.getName());
            }
        } catch (Exception e) {
            addCommitError(e);
        }
    }

    /**
     * @see org.opencms.workplace.CmsDialog#getCancelAction()
     */
    @Override
    public String getCancelAction() {

        // set the default action
        setParamPage(getPages().get(0));
        return DIALOG_SET;
    }

    /**
     * Returns the paramEditAction.<p>
     *
     * @return the paramEditAction
     */
    public String getParamEditaction() {

        return m_paramEditaction;
    }

    /**
     * Returns the paramSites.<p>
     *
     * @return the paramSites
     */
    public String getParamSites() {

        return m_paramSites;
    }

    /**
     * Returns the paramSitename.<p>
     *
     * @return the paramSitename
     */
    public String getSitename() {

        return m_sitename;
    }

    /**
     * Returns the template property.<p>
     *
     * @return the template property to set
     */
    public String getTemplate() {

        return m_template;
    }

    /**
     * Returns the paramCreateou.<p>
     *
     * @return the paramCreateou
     */
    public boolean isCreateou() {

        return m_createou;
    }

    /**
     * Sets the paramCreateou.<p>
     *
     * @param createou the paramCreateou to set
     */
    public void setCreateou(boolean createou) {

        m_createou = createou;
    }

    /**
     * Sets the paramEditAction.<p>
     *
     * @param paramEditaction the paramEditAction to set
     */
    public void setParamEditaction(String paramEditaction) {

        m_paramEditaction = paramEditaction;
    }

    /**
     * Sets the paramSites.<p>
     *
     * @param paramSites the paramSites to set
     */
    public void setParamSites(String paramSites) {

        m_paramSites = paramSites;
    }

    /**
     * Sets the site name.<p>
     *
     * @param sitename the site name to set
     */
    public void setSitename(String sitename) {

        m_sitename = sitename;
    }

    /**
     * Sets the the template property.<p>
     *
     * @param template the template property to set
     */
    public void setTemplate(String template) {

        m_template = template;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        String title = m_site.getTitle() != null
        ? m_site.getTitle()
        : Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(Messages.GUI_SITES_NEW_SITE_TITLE_0);

        // only show the position if editing a site or creating a new site
        int count = getParamEditaction() == null ? 4 : 5;
        // +1 if favicon present
        count = m_site.getFavicon() != null ? ++count : count;

        // site info
        result.append(
            dialogBlockStart(
                Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(
                    Messages.GUI_SITES_DETAIL_INFO_1,
                    title)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, count));

        // site parameters
        if (DIALOG_EDIT.equals(getParamEditaction()) || DIALOG_NEW.equals(getParamEditaction())) {
            result.append(createDialogRowsHtml(++count, count));
        } else if (!m_site.getParameters().isEmpty()) {
            result.append(createDialogRowsHtml(++count, (count + m_site.getParameters().size()) - 1));
            count += m_site.getParameters().size() - 1;
        }

        // +2 for OU check box and template property when creating a new site
        if (DIALOG_NEW.equals(getParamEditaction())) {
            result.append(createDialogRowsHtml(++count, count + 1));
            count += 1;
        }

        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        if ((getParamEditaction() != null) || CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_site.getSecureUrl())) {
            // secure site
            result.append(
                dialogBlockStart(
                    Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(
                        Messages.GUI_SITES_DETAIL_SECURE_1,
                        title)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(++count, count + 2));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            count += 2;
        }

        // site aliases
        if ((DIALOG_EDIT.equals(getParamEditaction()) || DIALOG_NEW.equals(getParamEditaction()))
            || !m_site.getAliases().isEmpty()) {
            result.append(
                dialogBlockStart(
                    Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(
                        Messages.GUI_SITES_DETAIL_ALIASES_1,
                        title)));
            result.append(createWidgetTableStart());
            if (DIALOG_EDIT.equals(getParamEditaction()) || DIALOG_NEW.equals(getParamEditaction())) {
                result.append(createDialogRowsHtml(++count, count));
            } else if (!m_site.getAliases().isEmpty()) {
                result.append(createDialogRowsHtml(++count, (count + m_site.getAliases().size()) - 1));
            }
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        // close the beginning table
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initSite();
        setKeyPrefix(CmsSitesOverviewList.KEY_PREFIX_SITES);

        if (DIALOG_NEW.equals(getParamEditaction()) || DIALOG_EDIT.equals(getParamEditaction())) {
            // edit or new
            // site info
            addWidget(new CmsWidgetDialogParameter(m_site, "title", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "sitename", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "server", PAGES[0], new CmsInputWidget()));
            addWidget(
                new CmsWidgetDialogParameter(
                    m_site,
                    "errorPage",
                    PAGES[0],
                    new CmsVfsFileWidget(true, "", true, false)));
            addWidget(
                new CmsWidgetDialogParameter(m_site, "position", PAGES[0], new CmsSelectWidget(createNavOpts(m_site))));
            addWidget(new CmsWidgetDialogParameter(m_site, "webserver", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "parameters", PAGES[0], new CmsInputWidget()));
            if (DIALOG_NEW.equals(getParamEditaction())) {
                addWidget(new CmsWidgetDialogParameter(this, "createou", PAGES[0], new CmsCheckboxWidget()));
                addWidget(new CmsWidgetDialogParameter(this, "template", PAGES[0], createTemplateSelect()));
            }

            if (m_site.getFavicon() != null) {
                try {
                    CmsObject clone = OpenCms.initCmsObject(getCms());
                    clone.getRequestContext().setSiteRoot("");

                    CmsDisplayWidget dis = new CmsDisplayWidget(
                        "<img src='"
                            + OpenCms.getLinkManager().getOnlineLink(clone, m_site.getFavicon())
                            + "' border='0' width='16' height='16' />");
                    addWidget(new CmsWidgetDialogParameter(m_site, "favicon", PAGES[0], dis));
                } catch (Exception e) {
                    // noop
                }
            }

            // secure site
            addWidget(new CmsWidgetDialogParameter(m_site, "secureUrl", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveUrl", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveError", PAGES[0], new CmsCheckboxWidget()));

            // site aliases
            addWidget(new CmsWidgetDialogParameter(m_site, "aliases", PAGES[0], new CmsInputWidget()));
        } else {
            // display site
            addWidget(new CmsWidgetDialogParameter(m_site, "title", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "siteRoot", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "server", PAGES[0], new CmsDisplayWidget()));
            CmsWidgetDialogParameter errorPage;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_site.getErrorPage())) {
                errorPage = new CmsWidgetDialogParameter(
                    m_site,
                    "errorPage",
                    PAGES[0],
                    new CmsDisplayWidget(
                        Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(
                            Messages.GUI_SITES_ERROR_PAGE_NOT_AVAILABLE_0)));
            } else {
                errorPage = new CmsWidgetDialogParameter(m_site, "errorPage", PAGES[0], new CmsDisplayWidget());
            }
            addWidget(errorPage);
            addWidget(new CmsWidgetDialogParameter(m_site, "webserver", PAGES[0], new CmsDisplayWidget()));
            int count = 5;
            for (String parameter : m_site.getParameters().keySet()) {
                String output = parameter + "=" + m_site.getParameters().get(parameter);
                CmsWidgetDialogParameter widget = new CmsWidgetDialogParameter(
                    output,
                    output,
                    Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(
                        Messages.GUI_SITES_DETAIL_PARAMETERS_0) + " [" + (count - 4) + "]",
                    new CmsDisplayWidget(),
                    PAGES[0],
                    1,
                    1,
                    count);
                addWidget(widget);
                count++;
            }
            if (m_site.getFavicon() != null) {
                try {
                    CmsObject clone = OpenCms.initCmsObject(getCms());
                    clone.getRequestContext().setSiteRoot("");

                    CmsDisplayWidget dis = new CmsDisplayWidget(
                        "<img src='"
                            + OpenCms.getLinkManager().getOnlineLink(clone, m_site.getFavicon())
                            + "' border='0' width='16' height='16' />");
                    addWidget(new CmsWidgetDialogParameter(m_site, "favicon", PAGES[0], dis));
                } catch (Exception e) {
                    // noop
                }
            }

            if (m_site.hasSecureServer()) {
                addWidget(new CmsWidgetDialogParameter(m_site, "secureUrl", PAGES[0], new CmsDisplayWidget()));
                addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveUrl", PAGES[0], new CmsDisplayWidget()));
                addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveError", PAGES[0], new CmsDisplayWidget()));
            }
            count = 0;
            for (String aliasUrl : m_site.getAliases()) {
                CmsWidgetDialogParameter alias = new CmsWidgetDialogParameter(
                    aliasUrl,
                    aliasUrl,
                    Messages.get().getBundle(getCms().getRequestContext().getLocale()).key(
                        Messages.GUI_SITES_DETAIL_LABEL_ALIAS_0) + " [" + (count + 1) + "]",
                    new CmsDisplayWidget(),
                    PAGES[0],
                    1,
                    1,
                    count);
                addWidget(alias);
                count++;
            }

        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        // save the current state of the site (may be changed because of the widget values)
        setDialogObject(m_site);
    }

    /**
     * Build select options for the position.<p>
     *
     * @param currSite the current selected site
     *
     * @return the select options
     */
    private List<CmsSelectWidgetOption> createNavOpts(CmsSiteBean currSite) {

        List<CmsSite> sites = new ArrayList<CmsSite>();
        for (CmsSite site : OpenCms.getSiteManager().getAvailableSites(getCms(), true)) {
            if (site.getSiteMatcher() != null) {
                sites.add(site);
            }
        }

        float maxValue = 0;
        float nextPos = 0;

        // calculate value for the first navigation position
        float firstValue = 1;
        if (sites.size() > 0) {
            try {
                maxValue = sites.get(0).getPosition();
            } catch (Exception e) {
                // should usually never happen
            }
        }

        if (maxValue != 0) {
            firstValue = maxValue / 2;
        }

        List<String> options = new ArrayList<String>(sites.size() + 1);
        List<String> values = new ArrayList<String>(sites.size() + 1);

        // add the first entry: before first element
        options.add(getMessages().key(org.opencms.workplace.commons.Messages.GUI_CHNAV_POS_FIRST_0));
        values.add(firstValue + "");

        // show all present navigation elements in box
        for (int i = 0; i < sites.size(); i++) {
            String navText = sites.get(i).getTitle();
            float navPos = sites.get(i).getPosition();
            String siteRoot = sites.get(i).getSiteRoot();
            // get position of next nav element
            nextPos = navPos + 2;
            if ((i + 1) < sites.size()) {
                nextPos = sites.get(i + 1).getPosition();
            }
            // calculate new position of current nav element
            float newPos;
            if ((nextPos - navPos) > 1) {
                newPos = navPos + 1;
            } else {
                newPos = (navPos + nextPos) / 2;
            }
            // check new maxValue of positions and increase it
            if (navPos > maxValue) {
                maxValue = navPos;
            }
            // if the element is the current file, mark it in select box
            if ((currSite != null) && (currSite.getSiteRoot() != null) && currSite.getSiteRoot().equals(siteRoot)) {
                options.add(
                    CmsEncoder.escapeHtml(
                        getMessages().key(
                            org.opencms.workplace.commons.Messages.GUI_CHNAV_POS_CURRENT_1,
                            new Object[] {sites.get(i).getSiteRoot()})));
                values.add("-1");
            } else {
                options.add(CmsEncoder.escapeHtml(navText + " [" + sites.get(i).getSiteRoot() + "/]"));
                values.add(newPos + "");
            }
        }

        // add the entry: at the last position
        options.add(getMessages().key(org.opencms.workplace.commons.Messages.GUI_CHNAV_POS_LAST_0));
        values.add((maxValue + 1) + "");

        // add the entry: no change
        options.add(getMessages().key(org.opencms.workplace.commons.Messages.GUI_CHNAV_NO_CHANGE_0));
        if ((currSite != null) && (currSite.getPosition() == Float.MAX_VALUE)) {
            // current resource has no valid position, use "last position"
            values.add((maxValue + 1) + "");
        } else {
            // current resource has valid position, use "-1" for no change
            values.add("-1");
        }
        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        for (int i = 0; i < values.size(); i++) {
            String val = values.get(i);
            String opt = options.get(i);
            result.add(new CmsSelectWidgetOption(val, false, opt));
        }
        return result;
    }

    /**
     * Helper method for creating the .content folder of a sub-sitemap.<p>
     *
     * @param cms the current CMS context
     * @param subSitemapFolder the sub-sitemap folder in which the .content folder should be created
     *
     * @return the created folder
     *
     * @throws CmsException if something goes wrong
     * @throws CmsLoaderException if something goes wrong
     */
    private CmsResource createSitemapContentFolder(CmsObject cms, CmsResource subSitemapFolder)
    throws CmsException, CmsLoaderException {

        CmsResource configFile = null;
        String sitePath = cms.getSitePath(subSitemapFolder);
        String folderName = CmsStringUtil.joinPaths(sitePath, CmsADEManager.CONTENT_FOLDER_NAME + "/");
        String sitemapConfigName = CmsStringUtil.joinPaths(folderName, CmsADEManager.CONFIG_FILE_NAME);
        if (!cms.existsResource(folderName)) {
            cms.createResource(
                folderName,
                OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_FOLDER_TYPE));
        }
        I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_TYPE);
        if (cms.existsResource(sitemapConfigName)) {
            configFile = cms.readResource(sitemapConfigName);
            if (!OpenCms.getResourceManager().getResourceType(configFile).getTypeName().equals(
                configType.getTypeName())) {
                throw new CmsException(
                    Messages.get().container(
                        Messages.ERR_CREATING_SUB_SITEMAP_WRONG_CONFIG_FILE_TYPE_2,
                        sitemapConfigName,
                        CmsADEManager.CONFIG_TYPE));
            }
        } else {
            configFile = cms.createResource(
                sitemapConfigName,
                OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_TYPE));
        }
        return configFile;
    }

    /**
     * Creates a template select combo widget.<p>
     *
     * @return the widget
     */
    private CmsComboWidget createTemplateSelect() {

        List<CmsSelectWidgetOption> options = new ArrayList<CmsSelectWidgetOption>();
        try {
            I_CmsResourceType templateType = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeJsp.getContainerPageTemplateTypeName());
            List<CmsResource> templates = getCms().readResources(
                "/system/",
                CmsResourceFilter.DEFAULT.addRequireType(templateType));
            for (CmsResource res : templates) {
                options.add(new CmsSelectWidgetOption(res.getRootPath()));
            }

        } catch (CmsException e) {
            LOG.error(e.getMessage(), e);
        }
        return new CmsComboWidget(options);
    }

    /**
     * Checks if there are at least one character in the folder name,
     * also ensures that it starts and ends with a '/'.<p>
     *
     * @param resourcename folder name to check (complete path)
     *
     * @return the validated folder name
     *
     * @throws CmsIllegalArgumentException if the folder name is empty or <code>null</code>
     */
    private String ensureFoldername(String resourcename) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmpty(resourcename)) {
            throw new CmsIllegalArgumentException(
                org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_BAD_RESOURCENAME_1, resourcename));
        }
        if (!CmsResource.isFolder(resourcename)) {
            resourcename = resourcename.concat("/");
        }
        if (resourcename.charAt(0) != '/') {
            resourcename = "/".concat(resourcename);
        }
        return resourcename;
    }

    /**
     * Initializes the dialog site object.<p>
     */
    private void initSite() {

        Object o = null;
        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // this is the initial dialog call
            if (CmsStringUtil.isNotEmpty(m_paramSites)) {
                // edit an existing site, get it from manager
                o = OpenCms.getSiteManager().getSiteForSiteRoot(m_paramSites);
            }
        } else {
            // this is not the initial call, get site from session
            o = getDialogObject();
        }
        if (o instanceof CmsSite) {
            // reuse site stored in session
            m_site = new CmsSiteBean((CmsSite)o);
        } else if (o instanceof CmsSiteBean) {
            // create a new site
            m_site = (CmsSiteBean)o;
        } else if (DIALOG_NEW.equals(getParamEditaction())) {
            m_site = new CmsSiteBean();
        } else {
            try {
                getToolManager().jspForwardTool(this, "/sites", new HashMap<String, String[]>());
            } catch (Exception e) {
                // noop
            }
        }

        if (!m_site.hasSecureServer()) {
            m_site.setSecureUrl("");
        }
        try {
            CmsObject clone = OpenCms.initCmsObject(getCms());
            clone.getRequestContext().setSiteRoot("");
            String iconPath = m_site.getSiteRoot() + "/" + CmsSiteFaviconDialog.ICON_NAME;
            if (clone.existsResource(iconPath)) {
                m_site.setFavicon(iconPath);
            }
        } catch (Throwable t) {
            // noop
        }

        if (m_site.getSiteRoot() != null) {
            setSitename(CmsResource.getName(m_site.getSiteRoot()));
        }

        CmsModule module = OpenCms.getModuleManager().getModule(MODULE_NAME);
        m_createou = Boolean.valueOf(module.getParameter(PARAM_CREATE_OU, Boolean.FALSE.toString())).booleanValue();
        m_ouDescription = module.getParameter(PARAM_OU_DESCRIPTION, "OU for: %(site)");
        setDialogObject(m_site);
    }

    /**
     * Validates the dialog before the commit action is performed.<p>
     *
     * @throws Exception if sth. goes wrong
     */
    private void validateDialog() throws Exception {

        CmsResource.checkResourceName(m_sitename);
        m_sitename = ensureFoldername(m_sitename);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_site.getServer())) {
            // the server's URL must not be empty or null
            throw new CmsException(Messages.get().container(Messages.ERR_SERVER_URL_NOT_EMPTY_0));
        }
    }
}
