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
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class for the Form to edit or add a site.<p>
 */
public class CmsEditSiteForm extends VerticalLayout {

    /**
     *  Bean for the ComboBox to edit the position.<p>
     */
    public class PositionComboBoxElementBean {

        /**Position of site in List. */
        private float m_position;

        /**Title of site to show. */
        private String m_title;

        /**
         * Constructor. <p>
         *
         * @param title of site
         * @param position of site
         */
        public PositionComboBoxElementBean(String title, float position) {
            m_position = position;
            m_title = title;
        }

        /**
         * Getter for position.<p>
         *
         * @return float position
         */
        public float getPosition() {

            return m_position;
        }

        /**
         * Getter for title.<p>
         *
         * @return String title
         */
        public String getTitle() {

            return m_title;
        }
    }

    /**
     * Receiver class for upload of favicon.<p>
     */
    class FavIconReceiver implements Receiver, SucceededListener {

        /**vaadin serial id. */
        private static final long serialVersionUID = 688021741970679734L;

        /**
         * @see com.vaadin.ui.Upload.Receiver#receiveUpload(java.lang.String, java.lang.String)
         */
        public OutputStream receiveUpload(String filename, String mimeType) {

            m_os.reset();
            if (!mimeType.startsWith("image")) {
                return new ByteArrayOutputStream(0);
            }
            return m_os;
        }

        /**
         * @see com.vaadin.ui.Upload.SucceededListener#uploadSucceeded(com.vaadin.ui.Upload.SucceededEvent)
         */
        public void uploadSucceeded(SucceededEvent event) {

            if (m_os.size() <= 1) {
                m_imageCounter = 0;
                m_fieldUploadFavIcon.setComponentError(
                    new UserError(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FAVICON_MIME_0)));
                setFaviconIfExist();
                return;
            }
            if (m_os.size() > 4096) {
                m_fieldUploadFavIcon.setComponentError(
                    new UserError(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FAVICON_SIZE_0)));
                m_imageCounter = 0;
                setFaviconIfExist();
                return;
            }
            m_imageCounter++;
            setCurrentFavIcon(m_os.toByteArray());
        }
    }

    /**
     *Validator for Folder Name field.<p>
     */
    class FolderNameValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 2269520781911597613L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String enteredName = (String)value;
            if (enteredName.isEmpty()) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_EMPTY_0));
            }
            if (FORBIDDEN_FOLDER_NAMES.contains(enteredName)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_FORBIDDEN_1, enteredName));
            }
            if (m_alreadyUsedFolderNames.contains(enteredName)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_ALREADYUSED_1, enteredName));
            }
        }
    }

    /**
     *Validator for server field.<p>
     */
    class ServerValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 9014118214418269697L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String enteredServer = (String)value;
            if (enteredServer.isEmpty()) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SERVER_EMPTY_0));
            }
            if (m_alreadyUsedURL.contains(new CmsSiteMatcher(enteredServer))) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SERVER_ALREADYUSED_1, enteredServer));
            }
        }
    }

    /** The module name constant. */
    public static final String MODULE_NAME = "org.opencms.ui.apps.sitemanager";

    /** Module parameter constant for the web server script. */
    public static final String PARAM_OU_DESCRIPTION = "oudescription";

    /**List of all forbidden folder names as new site-roots.*/
    static final List<String> FORBIDDEN_FOLDER_NAMES = new ArrayList<String>() {

        private static final long serialVersionUID = 8074588073232610426L;

        {
            add("system");
            add(OpenCms.getSiteManager().getSharedFolder().replaceAll("/", ""));
        }
    };

    /** Constant. */
    private static final String BLANK_HTML = "blank.html";

    /**default index.html which gets created.*/
    private static final String INDEX_HTML = "index.html";

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsEditSiteForm.class.getName());

    /** Constant. */
    private static final String MODEL_PAGE = "ModelPage";

    /** Constant. */
    private static final String MODEL_PAGE_PAGE = "ModelPage/Page";

    /** Constant. */
    private static final String NEW = ".templates/";

    /**vaadin serial id.*/
    private static final long serialVersionUID = -1011525709082939562L;

    /**List of all folder names already used for sites. */
    List<String> m_alreadyUsedFolderNames = new ArrayList<String>();

    /**List of all urls already used for sites.*/
    List<CmsSiteMatcher> m_alreadyUsedURL = new ArrayList<CmsSiteMatcher>();

    /**vaadin component. */
    Upload m_fieldUploadFavIcon;

    /**Needed to check if favicon was changed. */
    int m_imageCounter;

    /**OutputStream to store the uploaded favicon temporarily. */
    ByteArrayOutputStream m_os = new ByteArrayOutputStream(5500);

    /**vaadin component.*/
    TabSheet m_tab;

    /**button to add aliases.*/
    private Button m_addAlias;

    /**button to add parameter.*/
    private Button m_addParameter;

    /**vaadin component.*/
    private FormLayout m_aliases;

    /**vaadin component.*/
    private Button m_cancel;

    /**vaadin component.*/
    private CheckBox m_fieldCreateOU;

    /**vaadin component.*/
    private CmsPathSelectField m_fieldErrorPage;

    /**vaadin component.*/
    private CheckBox m_fieldExclusiveError;

    /**vaadin component.*/
    private CheckBox m_fieldExclusiveURL;

    /**vaadin component. */
    private Image m_fieldFavIcon;

    /**vaadin component.*/
    private ComboBox m_fieldPosition;

    /**vaadin component.*/
    private TextField m_fieldSecureServer;

    /**vaadin component.*/
    private CheckBox m_fieldWebServer;

    /** CmsSiteManager which calls Form.*/
    private CmsSiteManager m_manager;

    /**vaadin component.*/
    private Button m_ok;

    /**vaadin component.*/
    private FormLayout m_parameter;

    /**vaadin component.*/
    private TextField m_simpleFieldFolderName;

    /**vaadin component.*/
    private TextField m_simpleFieldServer;

    /**vaadin component.*/
    private ComboBox m_simpleFieldTemplate;

    /**vaadin component.*/
    private TextField m_simpleFieldTitle;

    /**current site which is supposed to be edited, null if site should be added.*/
    private CmsSite m_site;

    /**
     * Constructor.<p>
     * Use this to create a new site.<p>
     *
     * @param manager the site manager instance
     */
    public CmsEditSiteForm(CmsSiteManager manager) {

        List<CmsSite> allSites = OpenCms.getSiteManager().getAvailableSites(A_CmsUI.getCmsObject(), true);

        for (CmsSite site : allSites) {
            if (site.getSiteMatcher() != null) {
                m_alreadyUsedFolderNames.add(getFolderNameFromSiteRoot(site.getSiteRoot()));
                m_alreadyUsedURL.add(site.getSiteMatcher());
            }
        }

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_manager = manager;

        m_addParameter.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6814134727761004218L;

            public void buttonClick(ClickEvent event) {

                addParameter(null);

            }
        });

        m_addAlias.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -276802394623141951L;

            public void buttonClick(ClickEvent event) {

                addAlias(null);

            }

        });

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6814134727761004218L;

            public void buttonClick(ClickEvent event) {

                if (isValidInput()) {
                    submit();
                    cancel();
                    return;
                }
                m_tab.setSelectedTab(0);
            }
        });

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -276802394623141951L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }
        });

        m_tab.addStyleName(ValoTheme.TABSHEET_FRAMED);
        m_tab.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

        setUpComboBoxPosition();
        setUpComboBoxTemplate();
        m_simpleFieldFolderName.addValidator(new FolderNameValidator());
        m_simpleFieldServer.addValidator(new ServerValidator());
        m_fieldSecureServer.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -2837270577662919541L;

            public void valueChange(ValueChangeEvent event) {

                toggleSecureServer();
            }
        });
        m_fieldExclusiveURL.setEnabled(false);
        m_fieldExclusiveError.setEnabled(false);
        Receiver uploadReceiver = new FavIconReceiver();

        m_fieldUploadFavIcon.setReceiver(uploadReceiver);
        m_fieldUploadFavIcon.setButtonCaption("Select file");
        m_fieldUploadFavIcon.setImmediate(true);
        m_fieldUploadFavIcon.addSucceededListener((SucceededListener)uploadReceiver);
        m_fieldUploadFavIcon.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FAVICON_NEW_0));
        m_fieldFavIcon.setVisible(false);
    }

    /**
     * Constructor.<p>
     * Used to edit existing site.<p>
     *
     * @param manager the manager instance
     * @param siteRoot of site to edit
     */
    public CmsEditSiteForm(CmsSiteManager manager, String siteRoot) {
        this(manager);

        m_simpleFieldFolderName.removeAllValidators(); //can not be changed

        m_fieldCreateOU.setVisible(false);

        m_site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);

        m_alreadyUsedURL.remove(m_alreadyUsedURL.indexOf(m_site.getSiteMatcher())); //Remove current url to avoid validation problem

        setFieldTitle(m_site.getTitle());
        setFieldFolder(getFolderNameFromSiteRoot(siteRoot));
        m_simpleFieldFolderName.setEnabled(false);

        setFieldServer(m_site.getUrl());
        if (m_site.hasSecureServer()) {
            m_fieldSecureServer.setValue(m_site.getSecureUrl());
        }
        if (m_site.getErrorPage() != null) {
            m_fieldErrorPage.setValue(m_site.getErrorPage());
        }
        m_fieldWebServer.setValue(new Boolean(m_site.isWebserver()));
        m_fieldExclusiveURL.setValue(new Boolean(m_site.isExclusiveUrl()));
        m_fieldExclusiveError.setValue(new Boolean(m_site.isExclusiveError()));

        Map<String, String> siteParameters = m_site.getParameters();
        for (Entry<String, String> parameter : siteParameters.entrySet()) {
            addParameter(getParameterString(parameter));
        }

        List<CmsSiteMatcher> siteAliases = m_site.getAliases();
        for (CmsSiteMatcher siteMatcher : siteAliases) {
            addAlias(siteMatcher.getUrl());
        }

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            cms.getRequestContext().setSiteRoot("");
            CmsProperty template = cms.readPropertyObject(siteRoot, CmsPropertyDefinition.PROPERTY_TEMPLATE, false);
            if (template.isNullProperty()) {
                m_simpleFieldTemplate.setValue(null);
            } else {
                m_simpleFieldTemplate.setValue(template.getStructureValue());
            }
        } catch (CmsException e) {
            m_simpleFieldTemplate.setValue(null);
        }
        setUpComboBoxPosition();

        if (!m_fieldSecureServer.isEmpty()) {
            m_fieldExclusiveURL.setEnabled(true);
            m_fieldExclusiveError.setEnabled(true);
        }
        setFaviconIfExist();
    }

    /**
     * Returns a Folder Name for a given site-root.<p>
     *
     * @param siteRoot site root of a site
     * @return Folder Name
     */
    static String getFolderNameFromSiteRoot(String siteRoot) {

        return siteRoot.substring(CmsSiteManager.PATH_SITES.length());
    }

    /**
     * Adds a given alias String to the aliase-Vaadin form.<p>
     *
     * @param aliasString alias string which should be added.
     */
    void addAlias(String aliasString) {

        TextField textField = new TextField();
        if (aliasString != null) {
            textField.setValue(aliasString);
        }
        CmsRemovableFormRow<TextField> row = new CmsRemovableFormRow<TextField>(
            textField,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_REMOVE_ALIAS_0));
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ALIAS_0));
        row.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ALIAS_HELP_0));
        m_aliases.addComponent(row);

    }

    /**
     * Add a given parameter to the form layout.<p>
     *
     * @param parameter parameter to add to form
     */
    void addParameter(String parameter) {

        TextField textField = new TextField();
        if (parameter != null) {
            textField.setValue(parameter);
        }
        CmsRemovableFormRow<TextField> row = new CmsRemovableFormRow<TextField>(
            textField,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_REMOVE_PARAMETER_0));
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_PARAMETER_0));
        row.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_PARAMETER_HELP_0));
        m_parameter.addComponent(row);
    }

    /**
     * Cancels site edit.<p>
     */
    void cancel() {

        m_manager.openSubView("", true);
    }

    /**
     * Checks if all required fields are set correctly.<p>
     *
     * @return true if all inputs are valid.
     */
    boolean isValidInput() {

        return (m_simpleFieldFolderName.isValid() & m_simpleFieldServer.isValid());

    }

    /**
     * Sets a new uploaded favicon and changes the caption of the upload button.<p>
     *
     * @param imageData holdings byte array of favicon
     */
    void setCurrentFavIcon(final byte[] imageData) {

        m_fieldFavIcon.setVisible(true);
        m_fieldUploadFavIcon.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FAVICON_CHANGE_0));
        m_fieldFavIcon.setSource(new StreamResource(new StreamResource.StreamSource() {

            private static final long serialVersionUID = -8868657402793427460L;

            public InputStream getStream() {

                return new ByteArrayInputStream(imageData);
            }
        }, String.valueOf(System.currentTimeMillis())));
        m_fieldFavIcon.setImmediate(true);
    }

    /**
     * Tries to read and show the favicon of the site.<p>
     */
    void setFaviconIfExist() {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            CmsResource favicon = cms.readResource(m_site.getSiteRoot() + "/" + CmsSiteManager.FAVICON);
            setCurrentFavIcon(cms.readFile(favicon).getContents()); //FavIcon was found -> give it to the UI
        } catch (CmsException e) {
            //no favicon, do nothing
        }
    }

    /**
     * Saves the entered site-data as a CmsSite object.<p>
     * Code is copied from workplace-tool.<p>
     */
    void submit() {

        try {

            // validate the dialog form
            validateDialog();

            // create a root site clone of the current CMS object.
            CmsObject cms = A_CmsUI.getCmsObject();
            cms.getRequestContext().setSiteRoot("");
            // create the site root path
            String siteRoot = "/sites/" + getFieldFolder();

            CmsResource siteRootResource = null;
            String sitePath = null;
            // check if the site root already exists
            try {
                // take the existing site and do not perform any OU related actions
                siteRootResource = cms.readResource(siteRoot);
                sitePath = cms.getSitePath(siteRootResource); //? siteroot should be ""..
            } catch (CmsVfsResourceNotFoundException e) {
                // not create a new site folder and the according OU if option is checked checked
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeFolder.RESOURCE_TYPE_NAME);
                siteRootResource = cms.createResource(siteRoot, type);
                CmsProperty folderTitle = new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    getFieldTitle(),
                    getFieldTitle());
                cms.writePropertyObject(siteRoot, folderTitle);
                sitePath = cms.getSitePath(siteRootResource);

                //Create index.html
                I_CmsResourceType containerType = OpenCms.getResourceManager().getResourceType(
                    org.opencms.file.types.CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME);
                cms.createResource(siteRoot + INDEX_HTML, containerType);

            }
            cms.lockResource(siteRootResource);
            // add template  property
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getFieldTemplate())) {
                CmsProperty prop = new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_TEMPLATE,
                    getFieldTemplate(),
                    getFieldTemplate());
                cms.writePropertyObject(siteRoot, prop);
            }
            cms.unlockResource(siteRootResource);

            if (m_imageCounter > 0) {
                saveIcon(cms, siteRoot);
            }

            String ouDescription = "OU for: %(site)"; //ToDo don't know what is exactly supposed to be done..

            if (m_fieldCreateOU.isVisible() & (m_fieldCreateOU.getValue()).booleanValue()) {
                OpenCms.getOrgUnitManager().createOrganizationalUnit(
                    cms,
                    "/" + siteRootResource.getName(),
                    ouDescription.replace("%(site)", getFieldTitle() + " [" + siteRoot + "]"),
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
                        String defTitle = Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                            Messages.GUI_DEFAULT_MODEL_TITLE_1,
                            getFieldTitle());
                        String defDes = Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                            Messages.GUI_DEFAULT_MODEL_DESCRIPTION_1,
                            getFieldTitle());
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
                        //                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }

            // update the site manager state
            CmsSite newSite = getSiteFromForm();
            OpenCms.getSiteManager().updateSite(cms, m_site, newSite);
            // update the workplace server if the changed site was the workplace server
            if ((m_site != null) && m_site.getUrl().equals(OpenCms.getSiteManager().getWorkplaceServer())) {
                OpenCms.getSiteManager().updateGeneralSettings(
                    cms,
                    OpenCms.getSiteManager().getDefaultUri(),
                    newSite.getUrl(),
                    OpenCms.getSiteManager().getSharedFolder());
            }
            // write the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);

        } catch (Exception e) {
            LOG.error("Error while saving site.", e);
        }
    }

    /**
     * Toogles secure server options.<p>
     */
    void toggleSecureServer() {

        if (m_fieldSecureServer.isEmpty()) {
            m_fieldExclusiveURL.setEnabled(false);
            m_fieldExclusiveError.setEnabled(false);
            return;
        }
        m_fieldExclusiveURL.setEnabled(true);
        m_fieldExclusiveError.setEnabled(true);
    }

    /**
    * Helper method for creating the .content folder of a sub-sitemap.<p>
    *
    * @param cms the current CMS context
    * @param subSitemapFolder the sub-sitemap folder in which the .content folder should be created
    * @return the created folder
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
     * Checks if there are at least one character in the folder name,
     * also ensures that it ends with a '/' and doesn't start with '/'.<p>
     *
     * @param resourcename folder name to check (complete path)
     * @return the validated folder name
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
        if (resourcename.charAt(0) == '/') {
            resourcename = resourcename.substring(1);
        }
        return resourcename;
    }

    /**
     * Reads out all aliases from the form.<p>
     *
     * @return a List of CmsSiteMatcher
     */
    private List<CmsSiteMatcher> getAliases() {

        List<CmsSiteMatcher> ret = new ArrayList<CmsSiteMatcher>();

        for (Component c : m_aliases) {
            if (c instanceof CmsRemovableFormRow<?>) {
                ret.add(new CmsSiteMatcher(((String)((CmsRemovableFormRow<?>)c).getInput().getValue())));
            }
        }
        return ret;
    }

    /**
     * Returns the value of the site-folder.<p>
     *
     * @return String of folder path.
     */
    private String getFieldFolder() {

        return m_simpleFieldFolderName.getValue();
    }

    /**
     * Reads server field.<p>
     *
     * @return server as string
     */
    private String getFieldServer() {

        return m_simpleFieldServer.getValue();
    }

    /**
     * Reads ComboBox with Template information.<p>
     *
     * @return string of chosen template path.
     */
    private String getFieldTemplate() {

        return (String)m_simpleFieldTemplate.getValue();
    }

    /**
     * Reads title field.<p>
     *
     * @return title as string.
     */
    private String getFieldTitle() {

        return m_simpleFieldTitle.getValue();
    }

    /**
     * Reads parameter from form.<p>
     *
     * @return a Map with Parameter information.
     */
    private Map<String, String> getParameter() {

        Map<String, String> ret = new TreeMap<String, String>();
        for (Component c : m_parameter) {
            if (c instanceof CmsRemovableFormRow<?>) {
                String[] parameterStringArray = ((String)((CmsRemovableFormRow<?>)c).getInput().getValue()).split("=");
                ret.put(parameterStringArray[0], parameterStringArray[1]);
            }
        }
        return ret;
    }

    /**
     * Map entry of parameter to String representation.<p>
     *
     * @param parameter Entry holding parameter info.
     * @return the parameter formatted as string
     */
    private String getParameterString(Entry<String, String> parameter) {

        return parameter.getKey() + "=" + parameter.getValue();
    }

    /**
     * Reads out all forms and creates a site object.<p>
     *
     * @return the site object.
     */
    private CmsSite getSiteFromForm() {

        String siteRoot = "/sites/" + getFieldFolder();
        siteRoot = siteRoot.endsWith("/") ? siteRoot.substring(0, siteRoot.length() - 1) : siteRoot;
        CmsSiteMatcher matcher = CmsStringUtil.isNotEmpty(m_fieldSecureServer.getValue())
        ? new CmsSiteMatcher(m_fieldSecureServer.getValue())
        : null;
        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        CmsUUID uuid = new CmsUUID();
        if ((site != null) && (site.getSiteMatcher() != null)) {
            uuid = (CmsUUID)site.getSiteRootUUID().clone();
        }
        String errorPage = CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_fieldErrorPage.getValue())
        ? m_fieldErrorPage.getValue()
        : null;
        List<CmsSiteMatcher> aliases = getAliases();
        CmsSite ret = new CmsSite(
            siteRoot,
            uuid,
            getFieldTitle(),
            new CmsSiteMatcher(getFieldServer()),
            ((PositionComboBoxElementBean)m_fieldPosition.getValue()).getPosition() == -1
            ? String.valueOf(m_site.getPosition())
            : String.valueOf(((PositionComboBoxElementBean)m_fieldPosition.getValue()).getPosition()),
            errorPage,
            matcher,
            m_fieldExclusiveURL.getValue().booleanValue(),
            m_fieldExclusiveError.getValue().booleanValue(),
            m_fieldWebServer.getValue().booleanValue(),
            aliases);
        ret.setParameters((SortedMap<String, String>)getParameter());

        return ret;
    }

    /**
     * Saves outputstream of favicon as resource.<p>
     *
     * @param cms cms object.
     * @param siteRoot site root of considered site.
     */
    private void saveIcon(CmsObject cms, String siteRoot) {

        CmsResource favicon = null;
        try {
            favicon = cms.createResource(
                siteRoot + CmsSiteManager.FAVICON,
                OpenCms.getResourceManager().getResourceType(CmsResourceTypeImage.getStaticTypeName()));
        } catch (CmsVfsResourceAlreadyExistsException e) {
            //Resource already there
            try {
                favicon = cms.readResource(siteRoot + CmsSiteManager.FAVICON);
            } catch (CmsException e2) {
                //should never happen
            }
        } catch (CmsIllegalArgumentException | CmsException e) {
            // should never happen
        }
        try {
            cms.lockResource(siteRoot + CmsSiteManager.FAVICON);
            CmsFile faviconFile = new CmsFile(favicon);
            faviconFile.setContents(m_os.toByteArray());
            cms.writeFile(faviconFile);
            cms.unlockResource(siteRoot + CmsSiteManager.FAVICON);
        } catch (CmsException e) {
            // should not happen
        }

    }

    /**
     * Sets the folder field.<p>
     *
     * @param newValue value of the field
     */
    private void setFieldFolder(String newValue) {

        m_simpleFieldFolderName.setValue(newValue);
    }

    /**
     * Sets the server field.<p>
     *
     * @param newValue value of the field.
     */
    private void setFieldServer(String newValue) {

        m_simpleFieldServer.setValue(newValue);
    }

    /**
     * Sets the title field.<p>
     *
     * @param newValue value of the field.
     */
    private void setFieldTitle(String newValue) {

        m_simpleFieldTitle.setValue(newValue);
    }

    /**
     * Set the combo box for the position.<p>
     * Copied from workplace tool.<p>
     */
    private void setUpComboBoxPosition() {

        m_fieldPosition.removeAllItems();

        CmsObject cms = A_CmsUI.getCmsObject();
        List<CmsSite> sites = new ArrayList<CmsSite>();
        List<PositionComboBoxElementBean> beanList = new ArrayList<PositionComboBoxElementBean>();
        for (CmsSite site : OpenCms.getSiteManager().getAvailableSites(cms, true)) {
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

        // add the first entry: before first element
        beanList.add(
            new PositionComboBoxElementBean(
                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_CHNAV_POS_FIRST_0),
                firstValue));

        // show all present navigation elements in box
        for (int i = 0; i < sites.size(); i++) {

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
            if ((m_site != null) && (m_site.getSiteRoot() != null) && m_site.getSiteRoot().equals(siteRoot)) {
                beanList.add(
                    new PositionComboBoxElementBean(
                        CmsVaadinUtils.getMessageText(Messages.GUI_SITE_CHNAV_POS_CURRENT_1, m_site.getTitle()),
                        -1));
            } else {
                beanList.add(new PositionComboBoxElementBean(sites.get(i).getTitle(), newPos));
            }
        }

        // add the entry: at the last position
        PositionComboBoxElementBean lastEntry = new PositionComboBoxElementBean(
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_CHNAV_POS_LAST_0),
            maxValue + 1);
        beanList.add(lastEntry);

        // add the entry: no change
        beanList.add(
            new PositionComboBoxElementBean(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_CHNAV_POS_NOCHANGE_0), -1));

        BeanItemContainer<PositionComboBoxElementBean> objects = new BeanItemContainer<PositionComboBoxElementBean>(
            PositionComboBoxElementBean.class,
            beanList);

        m_fieldPosition.setContainerDataSource(objects);
        m_fieldPosition.setItemCaptionPropertyId("title");
        m_fieldPosition.setValue(beanList.get(beanList.size() - 1));
        if (m_site == null) {
            m_fieldPosition.setValue(lastEntry);
        }
    }

    /**
     * Sets the combobox for the template.<p>
     */
    private void setUpComboBoxTemplate() {

        try {
            I_CmsResourceType templateType = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeJsp.getContainerPageTemplateTypeName());
            CmsObject cms = A_CmsUI.getCmsObject();
            List<CmsResource> templates = cms.readResources(
                "/system/",
                CmsResourceFilter.DEFAULT.addRequireType(templateType));
            for (CmsResource res : templates) {
                m_simpleFieldTemplate.addItem(res.getRootPath());
            }
            m_simpleFieldTemplate.setValue(templates.get(0).getRootPath());
            m_simpleFieldTemplate.setNullSelectionAllowed(false);

        } catch (CmsException e) {
            // should not happen
        }
    }

    /**
     * Validates the dialog before the commit action is performed.<p>
     *
     * @throws Exception if sth. goes wrong
     */
    private void validateDialog() throws Exception {

        CmsResource.checkResourceName(getFieldFolder()); //throws Exception
        setFieldFolder(ensureFoldername(getFieldFolder()));
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getFieldFolder())) {
            // the server's URL must not be empty or null
            throw new CmsException(Messages.get().container(Messages.ERR_SERVER_URL_NOT_EMPTY_0));
        }
    }

}
