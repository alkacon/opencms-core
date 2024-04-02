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

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFolderSubSitemap;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.site.CmsSite;
import org.opencms.ui.apps.I_CmsCRUDApp;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Report thread to save site configurations.<p>
 */
public class CmsCreateSiteThread extends A_CmsReportThread {

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsCreateSiteThread.class.getName());

    /** Constant. */
    private static final String BLANK_HTML = "blank.html";

    /**default index.html which gets created.*/
    private static final String INDEX_HTML = "index.html";

    /** Constant. */
    private static final String MODEL_PAGE = "ModelPage";

    /** Constant. */
    private static final String MODEL_PAGE_PAGE = "ModelPage/Page";

    /** Constant. */
    private static final String NEW = ".templates/";

    /**Map holding key, values for macros.*/
    private Map<String, String> m_bundle;

    /**CmsObject(root-site).*/
    private CmsObject m_cms;

    /**CmsObject(root-site,online). */
    private CmsObject m_cmsOnline;

    /**Indicates if a OU should be created. */
    private boolean m_createOU;

    /**Runnable which gets used after finishing thread.*/
    private Runnable m_finished;

    /**Manager app. */
    private I_CmsCRUDApp<CmsSite> m_manager;

    /**Old version of site to overwrite. */
    private CmsSite m_oldSite;

    /**FavIcon byte data. */
    private ByteArrayOutputStream m_os;

    /**Parent OU. */
    private String m_parentOU;

    /**Selected OU.*/
    private String m_selectedOU;

    /**Site to save. */
    private CmsSite m_site;

    /**Source to copy resources from. */
    private String m_source;

    /**Template to set as property for the site. */
    private String m_template;

    /**
     * Constructor for Class.
     *
     * @param cms CmsObject
     * @param manager manager
     * @param site to be saved
     * @param oldSite to be overwritten
     * @param source to copy resources from
     * @param template to be set as property to root-folder of site
     * @param createOU indicates if OU should be generated
     * @param parentOU if createOU==true, sets the parent of the new OU
     * @param selectedOU set an existing OU
     * @param os ByteOutputStream with FavIcon data
     * @param bundle for macro resolving
     * @param finished runnable which gets called when thread done
     */
    protected CmsCreateSiteThread(
        CmsObject cms,
        I_CmsCRUDApp<CmsSite> manager,
        CmsSite site,
        CmsSite oldSite,
        String source,
        String template,
        boolean createOU,
        String parentOU,
        String selectedOU,
        ByteArrayOutputStream os,
        Map<String, String> bundle,
        Runnable finished) {

        super(cms, "createSite");
        m_cms = cms;
        m_cmsOnline = getOnlineCmsObject(cms);
        m_source = source;
        m_template = template;
        m_bundle = bundle;
        m_site = site;
        m_createOU = createOU;
        m_oldSite = oldSite;
        m_parentOU = parentOU;
        m_selectedOU = selectedOU;
        m_os = os;
        m_finished = finished;
        m_manager = manager;
        initHtmlReport(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getRequestContext()));
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        try {

            if (m_oldSite == null) {
                getReport().println(
                    Messages.get().container(Messages.RPT_SITE_START_NEW_1, CmsEncoder.escapeXml(m_site.getTitle())),
                    I_CmsReport.FORMAT_HEADLINE);
            } else {
                getReport().println(
                    Messages.get().container(Messages.RPT_SITE_START_EDIT_1, CmsEncoder.escapeXml(m_site.getTitle())),
                    I_CmsReport.FORMAT_HEADLINE);
            }
            CmsResource siteRootResource = null;

            if (m_source.isEmpty()) {
                //Don't copy an existing folder, but create a new one
                siteRootResource = createSiteRootIfNeeded(m_site.getSiteRoot());
                String sitePath = m_cms.getSitePath(siteRootResource);

                // create sitemap configuration
                String contentFolder = CmsStringUtil.joinPaths(sitePath, CmsADEManager.CONTENT_FOLDER_NAME + "/");
                String sitemapConfig = CmsStringUtil.joinPaths(contentFolder, CmsADEManager.CONFIG_FILE_NAME);
                if (!m_cms.existsResource(sitemapConfig)) {
                    createSitemapContentFolder(m_cms, siteRootResource, contentFolder);
                    createIndexHTML(ensureFoldername(siteRootResource.getRootPath()));
                }
            } else {
                //Copy existing folder to new siteroot and resolve macros
                CmsMacroResolver.copyAndResolveMacro(
                    m_cms,
                    m_source,
                    m_site.getSiteRoot(),
                    m_bundle,
                    true,
                    CmsResource.COPY_AS_NEW,
                    getReport());

                siteRootResource = m_cms.readResource(m_site.getSiteRoot());

                adjustFolderType(siteRootResource);
                setFolderTitle(siteRootResource);
            }

            setTemplate(siteRootResource);

            saveFavIcon(ensureFoldername(m_site.getSiteRoot()));

            handleOU(siteRootResource);

            try {
                m_cms.unlockResource(siteRootResource);
            } catch (CmsLockException e) {
                LOG.info("Unlock resource failed", e);
            }
        } catch (CmsException e) {
            LOG.error("Error creating site", e);
            getReport().println(Messages.get().container(Messages.RPT_SITE_ERROR_0), I_CmsReport.FORMAT_ERROR);
            getReport().println(e);
            getReport().println();
            getReport().println(
                Messages.get().container(Messages.RPT_SITE_FINISH_WARNING_0),
                I_CmsReport.FORMAT_WARNING);
            m_finished.run();
            return;
        }
        getReport().println(Messages.get().container(Messages.RPT_SITE_FINISH_0), I_CmsReport.FORMAT_OK);
        m_finished.run();
        m_manager.writeElement(m_site);
    }

    /**
     * Checks if there are at least one character in the folder name,
     * also ensures that it ends with a '/' and doesn't start with '/'.<p>
     *
     * @param resourcename folder name to check (complete path)
     * @return the validated folder name
     * @throws CmsIllegalArgumentException if the folder name is empty or <code>null</code>
     */
    String ensureFoldername(String resourcename) {

        if (CmsStringUtil.isEmpty(resourcename)) {
            return "";
        }
        if (!CmsResource.isFolder(resourcename)) {
            resourcename = resourcename.concat("/");
        }
        if (resourcename.charAt(0) == '/') {
            resourcename = resourcename.substring(1);
        }
        return resourcename;
    }

    /**
     * Changes the folder type if necessary:<p>
     *
     * Type Folder -> Type SubsitemapFolder.<p>
     * others -> no change.<p>
     *
     * @param siteRootResource resource to be changed
     * @throws CmsLoaderException exception
     * @throws CmsException exception
     */
    @SuppressWarnings("deprecation")
    private void adjustFolderType(CmsResource siteRootResource) throws CmsLoaderException, CmsException {

        if (OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeFolder.RESOURCE_TYPE_NAME) == OpenCms.getResourceManager().getResourceType(
                siteRootResource)) {

            siteRootResource.setType(
                OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeFolderSubSitemap.TYPE_SUBSITEMAP).getTypeId());
            m_cms.writeResource(siteRootResource);
        }
    }

    /**
     * Creates new index html if no one was found.<p>
     *
     * @param siteRoot of new site
     * @throws CmsIllegalArgumentException exception
     * @throws CmsException exception
     */
    private void createIndexHTML(String siteRoot) throws CmsIllegalArgumentException, CmsException {

        if (!m_cms.existsResource(siteRoot + INDEX_HTML)) {
            //Create index.html
            I_CmsResourceType containerType = OpenCms.getResourceManager().getResourceType(
                org.opencms.file.types.CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME);
            m_cms.createResource(siteRoot + INDEX_HTML, containerType);
        }
    }

    /**
    * Helper method for creating the .content folder of a sub-sitemap.<p>
    *
    * @param cms the current CMS context
    * @param subSitemapFolder the sub-sitemap folder in which the .content folder should be created
    * @param contentFolder the content folder path
    * @throws CmsException if something goes wrong
    * @throws CmsLoaderException if something goes wrong
    */
    private void createSitemapContentFolder(CmsObject cms, CmsResource subSitemapFolder, String contentFolder)
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

        if (configFile != null) {
            try {
                CmsResource newFolder = m_cms.createResource(
                    contentFolder + NEW,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.RESOURCE_TYPE_NAME));
                I_CmsResourceType containerType = OpenCms.getResourceManager().getResourceType(
                    org.opencms.file.types.CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME);
                CmsResource modelPage = m_cms.createResource(newFolder.getRootPath() + BLANK_HTML, containerType);
                String defTitle = Messages.get().getBundle(m_cms.getRequestContext().getLocale()).key(
                    Messages.GUI_DEFAULT_MODEL_TITLE_1,
                    m_site.getTitle());
                String defDes = Messages.get().getBundle(m_cms.getRequestContext().getLocale()).key(
                    Messages.GUI_DEFAULT_MODEL_DESCRIPTION_1,
                    m_site.getTitle());
                CmsProperty prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, defTitle, defTitle);
                m_cms.writePropertyObject(modelPage.getRootPath(), prop);
                prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, defDes, defDes);
                m_cms.writePropertyObject(modelPage.getRootPath(), prop);
                CmsFile file = m_cms.readFile(configFile);
                CmsXmlContent con = CmsXmlContentFactory.unmarshal(m_cms, file);
                con.addValue(m_cms, MODEL_PAGE, Locale.ENGLISH, 0);
                I_CmsXmlContentValue val = con.getValue(MODEL_PAGE_PAGE, Locale.ENGLISH);
                val.setStringValue(m_cms, modelPage.getRootPath());
                file.setContents(con.marshal());
                m_cms.writeFile(file);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Creates root-folder for the new site.<p>
     * If folder exist, the existing one will be returned.<p>
     *
     * @param siteRoot path to be created resp. read.
     * @return site root folder
     * @throws CmsException exception
     */
    private CmsResource createSiteRootIfNeeded(String siteRoot) throws CmsException {

        CmsResource siteRootResource = null;

        // check if the site root already exists
        try {
            // take the existing site and do not perform any OU related actions
            if (m_cms.existsResource(siteRoot)) {
                siteRootResource = m_cms.readResource(siteRoot);
            } else {
                CmsResource onlineVersion = m_cmsOnline.readResource(siteRoot);
                siteRootResource = m_cms.readResource(onlineVersion.getStructureId());
            }

        } catch (CmsVfsResourceNotFoundException e) {
            // not create a new site folder and the according OU if option is checked checked
            getReport().println(
                Messages.get().container(Messages.RPT_SITE_CREATE_RESOURCES_0),
                I_CmsReport.FORMAT_DEFAULT);
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeFolderSubSitemap.TYPE_SUBSITEMAP);
            siteRootResource = m_cms.createResource(siteRoot, type);
            CmsProperty folderTitle = new CmsProperty(
                CmsPropertyDefinition.PROPERTY_TITLE,
                m_site.getTitle(),
                m_site.getTitle());
            m_cms.writePropertyObject(siteRoot, folderTitle);

        }
        return siteRootResource;
    }

    /**
     * Creates online version of given CmsObject.<p>
     *
     * @param cms given CmsObject
     * @return online CmsObject
     */
    private CmsObject getOnlineCmsObject(CmsObject cms) {

        CmsObject res = null;
        try {
            res = OpenCms.initCmsObject(cms);
            res.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        } catch (CmsException e) {
            LOG.error("Cannot create CmsObject", e);
        }
        return res;
    }

    /**
     * Handles OU related operations.<p>
     * Creates OU, adds site root to existing OU or does nothing.<p>
     *
     * @param siteRootResource Resource representing root folder
     */
    private void handleOU(CmsResource siteRootResource) {

        String ouName = null;
        String ouDescription = "OU for: %(site)";

        if (m_createOU) {
            getReport().println(
                Messages.get().container(Messages.RPT_SITE_CREATE_OU_1, m_parentOU + siteRootResource.getName()),
                I_CmsReport.FORMAT_DEFAULT);
            try {
                OpenCms.getOrgUnitManager().createOrganizationalUnit(
                    m_cms,
                    m_parentOU + siteRootResource.getName(),
                    ouDescription.replace("%(site)", m_site.getTitle() + " [" + siteRootResource.getRootPath() + "]"),
                    0,
                    siteRootResource.getRootPath());
                ouName = m_parentOU + siteRootResource.getName();
            } catch (CmsDataAccessException e) {
                LOG.info("Can't create OU, an OU with same name exists. The existing OU is chosen for the new site");
                try {
                    OpenCms.getOrgUnitManager().addResourceToOrgUnit(
                        m_cms,
                        m_parentOU + siteRootResource.getName(),
                        siteRootResource.getRootPath());
                    ouName = m_parentOU + siteRootResource.getName();
                } catch (CmsException e2) {
                    LOG.info("Resource is already added to OU");
                    ouName = m_parentOU + siteRootResource.getName();
                }
            } catch (CmsException e) {
                LOG.error("Error on creating new OU", e);
            }
        }

        if ((m_oldSite == null) & (m_selectedOU != null) & !m_selectedOU.equals("/")) {
            getReport().println(
                Messages.get().container(Messages.RPT_SITE_ADD_OU_1, m_selectedOU),
                I_CmsReport.FORMAT_DEFAULT);
            try {
                OpenCms.getOrgUnitManager().addResourceToOrgUnit(m_cms, m_selectedOU, siteRootResource.getRootPath());
                ouName = m_selectedOU.substring(0, (m_selectedOU).length() - 1);
            } catch (CmsException e) {
                LOG.error("Error on adding resource to OU", e);
            }
        }

        try {
            m_cms.lockResource(siteRootResource);
        } catch (CmsException e) {
            LOG.error("unable to lock resource", e);
        }

        if (ouName != null) {
            try {

                m_cms.chacc(
                    siteRootResource.getRootPath(),
                    I_CmsPrincipal.PRINCIPAL_GROUP,
                    ouName + "/Users",
                    "+r+w+v+c+i+o+d");
            } catch (CmsException e) {
                LOG.error("Error on setting permission for OU.", e);
            }
        }
        try {
            m_cms.unlockResource(siteRootResource);
        } catch (CmsException e) {
            LOG.error("unable to unlock resource");
        }
    }

    /**
     * Saves outputstream of favicon as resource.<p>
     *
     * @param siteRoot site root of considered site.
     */
    private void saveFavIcon(String siteRoot) {

        if (m_os == null) {
            return;
        }
        if (m_os.size() == 0) {
            return;
        }

        getReport().println(Messages.get().container(Messages.RPT_SITE_SET_FAVICON_0), I_CmsReport.FORMAT_DEFAULT);
        CmsResource favicon = null;
        try {
            favicon = m_cms.createResource(
                siteRoot + CmsSiteManager.FAVICON,
                OpenCms.getResourceManager().getResourceType(CmsResourceTypeImage.getStaticTypeName()));
        } catch (CmsVfsResourceAlreadyExistsException e) {
            //OK, Resource already there
            try {
                favicon = m_cms.readResource(siteRoot + CmsSiteManager.FAVICON);
            } catch (CmsException e2) {
                //no, it wasn't..
                getReport().println(
                    Messages.get().container(Messages.RPT_SITE_ERROR_FAVICON_0),
                    I_CmsReport.FORMAT_ERROR);
                getReport().println(e);
                getReport().println(e2);
                return;
            }
        } catch (CmsIllegalArgumentException | CmsException e) {
            getReport().println(Messages.get().container(Messages.RPT_SITE_ERROR_FAVICON_0), I_CmsReport.FORMAT_ERROR);
            getReport().println(e);
            return;
        }
        try {
            m_cms.lockResource(siteRoot + CmsSiteManager.FAVICON);
            CmsFile faviconFile = new CmsFile(favicon);
            faviconFile.setContents(m_os.toByteArray());
            m_cms.writeFile(faviconFile);
            m_cms.unlockResource(siteRoot + CmsSiteManager.FAVICON);
        } catch (CmsException e) {
            getReport().println(Messages.get().container(Messages.RPT_SITE_ERROR_FAVICON_0), I_CmsReport.FORMAT_ERROR);
            getReport().println(e);
            return;
        }

    }

    /**
     * Updates title property of site root resource in case of copy from template.<p>
     *
     * @param res root resource to set titel for
     */
    private void setFolderTitle(CmsResource res) {

        try {
            CmsProperty titleProperty = m_cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false);
            if (!titleProperty.isNullProperty()) {
                titleProperty.setValue(m_site.getTitle(), CmsProperty.TYPE_INDIVIDUAL);
                m_cms.writePropertyObject(res.getRootPath(), titleProperty);
            } else {
                LOG.error("Editing title property of site root resource was not possible");
                getReport().println(
                    Messages.get().container(Messages.RPT_SITE_ERROR_TITLE_0),
                    I_CmsReport.FORMAT_ERROR);
            }
        } catch (CmsException e) {
            LOG.error("Editing title property of site root resource was not possible", e);
            getReport().println(Messages.get().container(Messages.RPT_SITE_ERROR_TITLE_0), I_CmsReport.FORMAT_ERROR);
            getReport().println(e);
        }

    }

    /**
     * Sets the selected template as property to site root folder.<p>
     *
     * @param siteRootResource Resource representing root folder
     */
    private void setTemplate(CmsResource siteRootResource) {

        try {
            m_cms.lockResource(siteRootResource);
            // add template  property
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_template)) {
                CmsProperty prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_TEMPLATE, m_template, null);
                m_cms.writePropertyObject(siteRootResource.getRootPath(), prop);
            }
            m_cms.unlockResource(siteRootResource);
        } catch (CmsException e) {
            LOG.error("Error on adding template", e);
        }
    }

}
