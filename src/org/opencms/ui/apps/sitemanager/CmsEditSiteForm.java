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
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFolderSubSitemap;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
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
    class FolderPathValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 2269520781911597613L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String enteredName = (String)value;
            if (FORBIDDEN_FOLDER_NAMES.contains(enteredName)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_FORBIDDEN_1, enteredName));
            }

            if (m_alreadyUsedFolderPath.contains(getParentFolder() + enteredName)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_ALREADYUSED_1, enteredName));
            }
            try {
                CmsResource.checkResourceName(enteredName);
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_EMPTY_0));
            }
        }
    }

    /**
     * Validator for the parent field.<p>
     */
    class ParentFolderValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 5217828150841769662L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            try {
                m_clonedCms.getRequestContext().setSiteRoot("");
                m_clonedCms.readResource(getParentFolder());
            } catch (CmsException e) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_PARENTFOLDER_NOT_EXIST_0));
            }

            if (!(getParentFolder()).startsWith(CmsSiteManager.PATH_SITES)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_WRONGPARENT_0));
            }

            if (!getSiteTemplatePath().isEmpty()) {
                if (ensureFoldername(getParentFolder()).equals(ensureFoldername(getSiteTemplatePath()))) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_EQUAL_SITETEMPLATE_0));
                }
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

    /**
     * Validator for Site Template selection field.<p>
     */
    class SiteTemplateValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -8730991818750657154L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String pathToCheck = (String)value;
            if (pathToCheck == null) {
                return;
            }
            if (pathToCheck.isEmpty()) { //Empty -> no template chosen, ok
                return;
            }
            if (!getParentFolder().isEmpty() & !getFieldFolder().isEmpty()) {
                String rootPath = "/" + ensureFoldername(getParentFolder()) + ensureFoldername(getFieldFolder());

                if (m_clonedCms.existsResource(rootPath)) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SITETEMPLATE_OVERWRITE_0));
                }
            }
            try {
                m_clonedCms.readResource(pathToCheck + CmsADEManager.CONTENT_FOLDER_NAME);
                //                m_clonedCms.readResource(pathToCheck + CmsSiteManager.MACRO_FOLDER);
            } catch (CmsException e) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SITETEMPLATE_INVALID_0));
            }
        }

    }

    /**
     * Validator for the title field.<p>
     */
    class TitleValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7878441125879949490L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (((String)value).isEmpty()) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_TITLE_EMPTY_0));
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

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsEditSiteForm.class.getName());

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

    /**vaadin serial id.*/
    private static final long serialVersionUID = -1011525709082939562L;

    /**List of all folder names already used for sites. */
    List<String> m_alreadyUsedFolderPath = new ArrayList<String>();

    /**List of all urls already used for sites.*/
    List<CmsSiteMatcher> m_alreadyUsedURL = new ArrayList<CmsSiteMatcher>();

    /**cloned cms obejct.*/
    CmsObject m_clonedCms;

    /**vaadin component. */
    Upload m_fieldUploadFavIcon;

    /**Needed to check if favicon was changed. */
    int m_imageCounter;

    /**OutputStream to store the uploaded favicon temporarily. */
    ByteArrayOutputStream m_os = new ByteArrayOutputStream(5500);

    /**current site which is supposed to be edited, null if site should be added.*/
    CmsSite m_site;

    /**vaadin component.*/
    TabSheet m_tab;

    /**button to add aliases.*/
    private Button m_addAlias;

    /**button to add parameter.*/
    private Button m_addParameter;

    /**vaadin component.*/
    private FormLayout m_aliases;

    /**automatic setted folder name.*/
    private String m_autoSetFolderName;

    /**Map to connect vaadin text fields with bundle keys.*/
    private Map<TextField, String> m_bundleComponentKeyMap;

    /**vaadin component.*/
    private FormLayout m_bundleValues;

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
    private CmsPathSelectField m_fieldLoadSiteTemplate;

    /**vaadin component.*/
    private ComboBox m_fieldPosition;

    /**vaadin component.*/
    private TextField m_fieldSecureServer;

    /**vaadin component.*/
    private ComboBox m_fieldSelectOU;

    /**vaadin component.*/
    private CheckBox m_fieldWebServer;

    /**boolean indicates if folder name was changed by user.*/
    private boolean m_isFolderNameTouched;

    /** CmsSiteManager which calls Form.*/
    private CmsSiteManager m_manager;

    /**vaadin component.*/
    private Button m_ok;

    /**main Panel.*/
    private Panel m_panel;

    /**vaadin component.*/
    private FormLayout m_parameter;

    /**vaadin component.*/
    private TextField m_simpleFieldFolderName;

    /**vaadin component.*/
    private CmsPathSelectField m_simpleFieldParentFolderName;

    /**vaadin component.*/
    private TextField m_simpleFieldServer;

    /**vaadin component.*/
    private ComboBox m_simpleFieldTemplate;

    /**vaadin component.*/
    private TextField m_simpleFieldTitle;

    /**
     * Constructor.<p>
     * Use this to create a new site.<p>
     *
     * @param manager the site manager instance
     */
    public CmsEditSiteForm(CmsSiteManager manager) {
        m_isFolderNameTouched = false;
        m_autoSetFolderName = "";
        try {
            m_clonedCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            m_clonedCms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            LOG.error("Error cloning cms object", e);
        }
        List<CmsSite> allSites = OpenCms.getSiteManager().getAvailableSites(m_clonedCms, true);

        for (CmsSite site : allSites) {
            if (site.getSiteMatcher() != null) {
                m_alreadyUsedFolderPath.add(site.getSiteRoot());
                m_alreadyUsedURL.add(site.getSiteMatcher());
            }
        }

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        if (!OpenCms.getSiteManager().isConfigurableWebServer()) {
            m_fieldWebServer.setVisible(false);
            m_fieldWebServer.setValue(new Boolean(true));

        }

        m_simpleFieldParentFolderName.setValue(CmsSiteManager.PATH_SITES);
        m_simpleFieldParentFolderName.setUseRootPaths(true);
        m_simpleFieldParentFolderName.setCmsObject(m_clonedCms);
        m_simpleFieldParentFolderName.setResourceFilter(CmsResourceFilter.DEFAULT_FOLDERS);

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

                setupValidators();
                if (isValidInputSimple() & isValidInputSiteTemplate()) {
                    submit();
                    cancel();
                    return;
                }
                if (isValidInputSimple()) {
                    m_tab.setSelectedTab(4);
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

        m_fieldCreateOU.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -2837270577662919541L;

            public void valueChange(ValueChangeEvent event) {

                toggleSelectOU();

            }
        });

        m_tab.addStyleName(ValoTheme.TABSHEET_FRAMED);
        m_tab.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

        setUpComboBoxPosition();
        setUpComboBoxTemplate();
        setUpOUComboBox();

        m_fieldSecureServer.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -2837270577662919541L;

            public void valueChange(ValueChangeEvent event) {

                toggleSecureServer();
            }
        });
        m_fieldExclusiveURL.setEnabled(false);
        m_fieldExclusiveError.setEnabled(false);
        Receiver uploadReceiver = new FavIconReceiver();

        m_fieldWebServer.setValue(new Boolean(true));

        m_fieldUploadFavIcon.setReceiver(uploadReceiver);
        m_fieldUploadFavIcon.setButtonCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SELECT_FILE_0));
        m_fieldUploadFavIcon.setImmediate(true);
        m_fieldUploadFavIcon.addSucceededListener((SucceededListener)uploadReceiver);
        m_fieldUploadFavIcon.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FAVICON_NEW_0));
        m_fieldFavIcon.setVisible(false);

        m_simpleFieldTitle.addBlurListener(new BlurListener() {

            private static final long serialVersionUID = -4147179568264310325L;

            public void blur(BlurEvent event) {

                if (!isFolderNameTouched()) {
                    String niceName = OpenCms.getResourceManager().getNameGenerator().getUniqueFileName(
                        m_clonedCms,
                        "/sites",
                        getFieldTitle());
                    setFolderNameState(niceName);
                    setFieldFolder(niceName);
                }

            }
        });

        m_simpleFieldFolderName.addBlurListener(new BlurListener() {

            private static final long serialVersionUID = 2080245499551324408L;

            public void blur(BlurEvent event) {

                setFolderNameState(null);

            }
        });

        m_fieldLoadSiteTemplate.addValidator(new SiteTemplateValidator());

        m_fieldLoadSiteTemplate.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -5859547073423161234L;

            public void valueChange(ValueChangeEvent event) {

                clearMessageBundle();
                loadMessageBundle();

            }
        });

        m_fieldLoadSiteTemplate.setUseRootPaths(true);
        m_fieldLoadSiteTemplate.setCmsObject(m_clonedCms);
        m_fieldLoadSiteTemplate.setResourceFilter(CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder());
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
        m_site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        m_panel.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_CONFIGURATION_EDIT_1, m_site.getTitle()));

        m_tab.removeTab(m_tab.getTab(4));
        m_simpleFieldTitle.removeTextChangeListener(null);

        m_simpleFieldParentFolderName.setEnabled(false);
        m_simpleFieldParentFolderName.setValue(
            siteRoot.substring(0, siteRoot.length() - siteRoot.split("/")[siteRoot.split("/").length - 1].length()));

        m_simpleFieldFolderName.removeAllValidators(); //can not be changed

        m_fieldCreateOU.setVisible(false);

        unableOUComboBox();

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
            CmsProperty template = m_clonedCms.readPropertyObject(
                siteRoot,
                CmsPropertyDefinition.PROPERTY_TEMPLATE,
                false);
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

        return siteRoot.split("/")[siteRoot.split("/").length - 1];
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
     * Clears the message bundle and removes related text fields from UI.<p>
     */
    void clearMessageBundle() {

        if (m_bundleComponentKeyMap != null) {
            Set<TextField> setBundles = m_bundleComponentKeyMap.keySet();

            for (TextField field : setBundles) {
                m_bundleValues.removeComponent(field);
            }
            m_bundleComponentKeyMap.clear();
        }
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
     * Returns the value of the site-folder.<p>
     *
     * @return String of folder path.
     */
    String getFieldFolder() {

        return m_simpleFieldFolderName.getValue();
    }

    /**
     * Reads title field.<p>
     *
     * @return title as string.
     */
    String getFieldTitle() {

        return m_simpleFieldTitle.getValue();
    }

    /**
     * Returns parent folder.<p>
     *
     * @return parent folder as string
     */
    String getParentFolder() {

        return m_simpleFieldParentFolderName.getValue();
    }

    /**
     * Returns the value of the site template field.<p>
     *
     * @return string root path
     */
    String getSiteTemplatePath() {

        return m_fieldLoadSiteTemplate.getValue();
    }

    /**
     * Checks if folder name was touched.<p>
     *
     * Considered as touched if side is edited or value of foldername was changed by user.<p>
     *
     * @return boolean true means Folder value was set by user or existing site and should not be changed by title-listener
     */
    boolean isFolderNameTouched() {

        if (m_site != null) {
            return true;
        }
        if (m_autoSetFolderName.equals(getFieldFolder())) {
            return false;
        }
        return m_isFolderNameTouched;
    }

    /**
     * Checks if all required fields are set correctly at first Tab.<p>
     *
     * @return true if all inputs are valid.
     */
    boolean isValidInputSimple() {

        return (m_simpleFieldFolderName.isValid()
            & m_simpleFieldServer.isValid()
            & m_simpleFieldTitle.isValid()
            & m_simpleFieldParentFolderName.isValid());
    }

    /**
     * Checks if all required fields are set correctly at site template tab.<p>
     *
     * @return true if all inputs are valid.
     */
    boolean isValidInputSiteTemplate() {

        return (m_fieldLoadSiteTemplate.isValid());
    }

    /**
     * Loads message bundle from bundle defined inside the site-template which is used to create new site.<p>
     */
    void loadMessageBundle() {

        //Check if chosen site template is valid and not empty
        if (!m_fieldLoadSiteTemplate.isValid()
            | m_fieldLoadSiteTemplate.isEmpty()
            | !CmsSiteManager.isFolderWithMacros(m_clonedCms, m_fieldLoadSiteTemplate.getValue())) {
            return;
        }
        try {
            m_bundleComponentKeyMap = new HashMap<TextField, String>();

            //Get resource of the descriptor.
            CmsResource descriptor = m_clonedCms.readResource(
                m_fieldLoadSiteTemplate.getValue()
                    + CmsSiteManager.MACRO_FOLDER
                    + "/"
                    + CmsSiteManager.BUNDLE_NAME
                    + "_desc");
            //Read related bundle

            Properties resourceBundle = getLocalizedBundle();
            Map<String, String[]> bundleKeyDescriptorMap = CmsMacroResolver.getBundleMapFromResources(
                resourceBundle,
                descriptor,
                m_clonedCms);

            for (String key : bundleKeyDescriptorMap.keySet()) {

                //Create TextField
                TextField field = new TextField();
                field.setCaption(bundleKeyDescriptorMap.get(key)[0]);
                field.setValue(bundleKeyDescriptorMap.get(key)[1]);
                field.setWidth("100%");

                //Add vaadin component to UI and keep related key in HashMap
                m_bundleValues.addComponent(field);
                m_bundleComponentKeyMap.put(field, key);
            }
        } catch (CmsException | IOException e) {
            LOG.error("Error reading bundle", e);
        }
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
            CmsResource favicon = m_clonedCms.readResource(m_site.getSiteRoot() + "/" + CmsSiteManager.FAVICON);
            setCurrentFavIcon(m_clonedCms.readFile(favicon).getContents()); //FavIcon was found -> give it to the UI
        } catch (CmsException e) {
            //no favicon, do nothing
        }
    }

    /**
     * Sets the folder field.<p>
     *
     * @param newValue value of the field
     */
    void setFieldFolder(String newValue) {

        m_simpleFieldFolderName.setValue(newValue);
    }

    /**
     * Sets the folder Name state to recognize if folder field was touched.<p>
     *
     * @param setFolderName name of folder set by listener from title.
     */
    void setFolderNameState(String setFolderName) {

        if (setFolderName == null) {
            if (m_simpleFieldFolderName.getValue().isEmpty()) {
                m_isFolderNameTouched = false;
                return;
            }
            m_isFolderNameTouched = true;
        } else {
            m_autoSetFolderName = setFolderName;
        }
    }

    /**
     * Setup validators which get called on click.<p>
     * Site-template gets validated separately.<p>
     */
    void setupValidators() {

        if (m_simpleFieldServer.getValidators().size() == 0) {
            if (m_site == null) {
                m_simpleFieldFolderName.addValidator(new FolderPathValidator());
                m_simpleFieldParentFolderName.addValidator(new ParentFolderValidator());
            }
            m_simpleFieldServer.addValidator(new ServerValidator());
            m_simpleFieldTitle.addValidator(new TitleValidator());
        }
    }

    /**
     * Saves the entered site-data as a CmsSite object.<p>
     */
    void submit() {

        try {
            // switch to root site
            m_clonedCms.getRequestContext().setSiteRoot("");

            // create the site root path
            String siteRoot = "/" + ensureFoldername(getParentFolder()) + ensureFoldername(getFieldFolder());

            CmsResource siteRootResource = null;

            if (m_fieldLoadSiteTemplate.getValue().isEmpty()) {

                siteRootResource = createSiteRootIfNeeded(siteRoot);
                String sitePath = m_clonedCms.getSitePath(siteRootResource);

                // create sitemap configuration
                String contentFolder = CmsStringUtil.joinPaths(sitePath, CmsADEManager.CONTENT_FOLDER_NAME + "/");
                String sitemapConfig = CmsStringUtil.joinPaths(contentFolder, CmsADEManager.CONFIG_FILE_NAME);
                if (!m_clonedCms.existsResource(sitemapConfig)) {
                    createSitemapContentFolder(m_clonedCms, siteRootResource, contentFolder);
                }
                createIndexHTML(siteRoot);
            } else {
                Map<String, String> bundle = getBundleMap();
                CmsMacroResolver.copyAndResolveMacro(
                    m_clonedCms,
                    m_fieldLoadSiteTemplate.getValue(),
                    siteRoot,
                    bundle,
                    true);

                siteRootResource = m_clonedCms.readResource(siteRoot);

                adjustFolderType(siteRootResource);
            }

            setTemplate(siteRootResource);

            saveFavIcon(siteRoot);

            handleOU(siteRootResource);

            // update the site manager state
            CmsSite newSite = getSiteFromForm();
            OpenCms.getSiteManager().updateSite(m_clonedCms, m_site, newSite);
            // update the workplace server if the changed site was the workplace server
            if ((m_site != null) && m_site.getUrl().equals(OpenCms.getSiteManager().getWorkplaceServer())) {
                OpenCms.getSiteManager().updateGeneralSettings(
                    m_clonedCms,
                    OpenCms.getSiteManager().getDefaultUri(),
                    Collections.singletonList(newSite.getUrl()),
                    OpenCms.getSiteManager().getSharedFolder());
            }
            // write the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
            try {
                m_clonedCms.unlockResource(siteRootResource);
            } catch (CmsLockException e) {
                LOG.info("Unlock resource failed", e);
            }

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
     * Toogles the select OU combo box depending on create ou check box.<p>
     */
    void toggleSelectOU() {

        boolean create = m_fieldCreateOU.getValue().booleanValue();

        m_fieldSelectOU.setEnabled(!create);
        m_fieldSelectOU.select(null);
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
            m_clonedCms.writeResource(siteRootResource);
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

        if (!m_clonedCms.existsResource(siteRoot + INDEX_HTML)) {
            //Create index.html
            I_CmsResourceType containerType = OpenCms.getResourceManager().getResourceType(
                org.opencms.file.types.CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME);
            m_clonedCms.createResource(siteRoot + INDEX_HTML, containerType);
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
                CmsResource newFolder = m_clonedCms.createResource(
                    contentFolder + NEW,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.RESOURCE_TYPE_NAME));
                I_CmsResourceType containerType = OpenCms.getResourceManager().getResourceType(
                    org.opencms.file.types.CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME);
                CmsResource modelPage = m_clonedCms.createResource(newFolder.getRootPath() + BLANK_HTML, containerType);
                String defTitle = Messages.get().getBundle(m_clonedCms.getRequestContext().getLocale()).key(
                    Messages.GUI_DEFAULT_MODEL_TITLE_1,
                    getFieldTitle());
                String defDes = Messages.get().getBundle(m_clonedCms.getRequestContext().getLocale()).key(
                    Messages.GUI_DEFAULT_MODEL_DESCRIPTION_1,
                    getFieldTitle());
                CmsProperty prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, defTitle, defTitle);
                m_clonedCms.writePropertyObject(modelPage.getRootPath(), prop);
                prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, defDes, defDes);
                m_clonedCms.writePropertyObject(modelPage.getRootPath(), prop);
                CmsFile file = m_clonedCms.readFile(configFile);
                CmsXmlContent con = CmsXmlContentFactory.unmarshal(m_clonedCms, file);
                con.addValue(m_clonedCms, MODEL_PAGE, Locale.ENGLISH, 0);
                I_CmsXmlContentValue val = con.getValue(MODEL_PAGE_PAGE, Locale.ENGLISH);
                val.setStringValue(m_clonedCms, modelPage.getRootPath());
                file.setContents(con.marshal());
                m_clonedCms.writeFile(file);
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

        CmsResource siteRootResource;

        // check if the site root already exists
        try {
            // take the existing site and do not perform any OU related actions
            siteRootResource = m_clonedCms.readResource(siteRoot);
        } catch (CmsVfsResourceNotFoundException e) {
            // not create a new site folder and the according OU if option is checked checked
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeFolderSubSitemap.TYPE_SUBSITEMAP);
            siteRootResource = m_clonedCms.createResource(siteRoot, type);
            CmsProperty folderTitle = new CmsProperty(
                CmsPropertyDefinition.PROPERTY_TITLE,
                getFieldTitle(),
                getFieldTitle());
            m_clonedCms.writePropertyObject(siteRoot, folderTitle);

        }
        return siteRootResource;
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
     * Returns the correct varaint of a resource name accoreding to locale.<p>
     *
     * @param path where the considered resource is.
     * @param baseName of the resource
     * @return localized name of resource
     */
    private String getAvailableLocalVariant(String path, String baseName) {

        A_CmsUI.get();
        List<String> localVariations = CmsLocaleManager.getLocaleVariants(
            baseName,
            UI.getCurrent().getLocale(),
            false,
            true);

        for (String name : localVariations) {
            if (m_clonedCms.existsResource(path + name)) {
                return name;
            }
        }
        return null; //TODO throw exception
    }

    /**
     * Reads out bundle values from UI and stores keys with values in HashMap.<p>
     *
     * @return hash map
     */
    private Map<String, String> getBundleMap() {

        Map<String, String> bundles = new HashMap<String, String>();

        if (m_bundleComponentKeyMap != null) {
            Set<TextField> fields = m_bundleComponentKeyMap.keySet();

            for (TextField field : fields) {
                bundles.put(m_bundleComponentKeyMap.get(field), field.getValue());
            }
        }
        return bundles;
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

        Object value = m_simpleFieldTemplate.getValue();
        if (value != null) {
            return (String)value;
        }
        return "";
    }

    /**
     * Gets localized property object.<p>
     *
     * @return Properties object
     * @throws CmsException exception
     * @throws IOException exception
     */
    private Properties getLocalizedBundle() throws CmsException, IOException {

        CmsResource bundleResource = m_clonedCms.readResource(
            m_fieldLoadSiteTemplate.getValue()
                + CmsSiteManager.MACRO_FOLDER
                + "/"
                + getAvailableLocalVariant(
                    m_fieldLoadSiteTemplate.getValue() + CmsSiteManager.MACRO_FOLDER + "/",
                    CmsSiteManager.BUNDLE_NAME));

        Properties ret = new Properties();
        InputStreamReader reader = new InputStreamReader(
            new ByteArrayInputStream(m_clonedCms.readFile(bundleResource).getContents()),
            StandardCharsets.UTF_8);
        ret.load(reader);

        return ret;
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

        String siteRoot = "/" + ensureFoldername(getParentFolder()) + ensureFoldername(getFieldFolder());
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
     * Handles OU related operations.<p>
     * Creates OU, adds site root to existing OU or does nothing.<p>
     *
     * @param siteRootResource Resource representing root folder
     */
    private void handleOU(CmsResource siteRootResource) {

        String ouDescription = "OU for: %(site)";

        if (m_fieldCreateOU.isVisible() & (m_fieldCreateOU.getValue()).booleanValue()) {
            try {
                OpenCms.getOrgUnitManager().createOrganizationalUnit(
                    m_clonedCms,
                    "/" + siteRootResource.getName(),
                    ouDescription.replace("%(site)", getFieldTitle() + " [" + siteRootResource.getRootPath() + "]"),
                    0,
                    siteRootResource.getRootPath());
            } catch (CmsDataAccessException e) {
                LOG.info("Can't create OU, an OU with same name exists. The existing OU is chosen for the new site");
                try {
                    OpenCms.getOrgUnitManager().addResourceToOrgUnit(
                        m_clonedCms,
                        "/" + siteRootResource.getName(),
                        siteRootResource.getRootPath());
                } catch (CmsException e2) {
                    LOG.info("Resource is already added to OU");
                }
            } catch (CmsException e) {
                LOG.error("Error on creating new OU", e);
            }
        }

        if (m_fieldSelectOU.isEnabled() & (m_fieldSelectOU.getValue() != null)) {
            try {
                OpenCms.getOrgUnitManager().addResourceToOrgUnit(
                    m_clonedCms,
                    (String)m_fieldSelectOU.getValue(),
                    siteRootResource.getRootPath());
            } catch (CmsException e) {
                LOG.error("Error on adding resource to OU", e);
            }
        }
    }

    /**
     * Saves the favicon if one was uploaded.<p>
     *
     * @param siteRoot of site
     */
    private void saveFavIcon(String siteRoot) {

        if (m_imageCounter > 0) {
            saveIcon(m_clonedCms, siteRoot);
        }
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
     * Sets the selected template as property to site root folder.<p>
     *
     * @param siteRootResource Resource representing root folder
     */
    private void setTemplate(CmsResource siteRootResource) {

        try {
            m_clonedCms.lockResource(siteRootResource);
            // add template  property
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getFieldTemplate())) {
                CmsProperty prop = new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_TEMPLATE,
                    getFieldTemplate(),
                    getFieldTemplate());
                m_clonedCms.writePropertyObject(siteRootResource.getRootPath(), prop);
            }
            m_clonedCms.unlockResource(siteRootResource);
        } catch (CmsException e) {
            LOG.error("Error on adding template", e);
        }
    }

    /**
     * Set the combo box for the position.<p>
     * Copied from workplace tool.<p>
     */
    private void setUpComboBoxPosition() {

        m_fieldPosition.removeAllItems();

        List<CmsSite> sites = new ArrayList<CmsSite>();
        List<PositionComboBoxElementBean> beanList = new ArrayList<PositionComboBoxElementBean>();
        for (CmsSite site : OpenCms.getSiteManager().getAvailableSites(m_clonedCms, true)) {
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
            List<CmsResource> templates = m_clonedCms.readResources(
                "/system/",
                CmsResourceFilter.DEFAULT.addRequireType(templateType));
            for (CmsResource res : templates) {
                m_simpleFieldTemplate.addItem(res.getRootPath());
            }
            if (!templates.isEmpty()) {
                m_simpleFieldTemplate.setValue(templates.get(0).getRootPath());
            }
            m_simpleFieldTemplate.setNullSelectionAllowed(false);

        } catch (CmsException e) {
            // should not happen
        }
    }

    /**
     * Fill ComboBox for OU selection.<p>
     */
    private void setUpOUComboBox() {

        try {
            m_clonedCms.getRequestContext().setSiteRoot("");
            List<CmsOrganizationalUnit> ous = OpenCms.getOrgUnitManager().getOrganizationalUnits(
                m_clonedCms,
                "/",
                true);
            for (CmsOrganizationalUnit ou : ous) {
                m_fieldSelectOU.addItem(ou.getName());
            }
            m_fieldSelectOU.setNewItemsAllowed(false);
        } catch (CmsException e) {
            LOG.error("Error on reading OUs", e);
        }
    }

    /**
     * Selects the OU of the site (if site has an OU), and disables the ComboBox.<p>
     */
    private void unableOUComboBox() {

        try {
            m_clonedCms.getRequestContext().setSiteRoot("");
            List<CmsOrganizationalUnit> ous = OpenCms.getOrgUnitManager().getOrganizationalUnits(
                m_clonedCms,
                "/",
                true);
            for (CmsOrganizationalUnit ou : ous) {
                List<CmsResource> res = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    m_clonedCms,
                    ou.getName());
                for (CmsResource resource : res) {
                    if (resource.getRootPath().equals(m_site.getSiteRoot() + "/")) {
                        m_fieldSelectOU.select(ou.getName());
                    }
                }
            }

        } catch (CmsException e) {
            LOG.error("Error on reading OUs", e);
        }
        m_fieldSelectOU.setEnabled(false);
    }
}
