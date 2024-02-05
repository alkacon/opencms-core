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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolderSubSitemap;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.site.CmsAlternativeSiteRootMapping;
import org.opencms.site.CmsSSLMode;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.site.CmsSiteMatcher.RedirectMode;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.ui.report.CmsReportWidget;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsPath;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.google.common.base.Supplier;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.server.StreamResource;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.Upload;
import com.vaadin.v7.ui.Upload.Receiver;
import com.vaadin.v7.ui.Upload.SucceededEvent;
import com.vaadin.v7.ui.Upload.SucceededListener;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the Form to edit or add a site.<p>
 */
public class CmsEditSiteForm extends CmsBasicDialog {

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
     *Validator for server field.<p>
     */
    class AliasValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 9014118214418269697L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String enteredServer = (String)value;
            if (enteredServer == null) {
                return;
            }
            if (enteredServer.isEmpty()) {
                return;
            }
            if (m_alreadyUsedURL.contains(new CmsSiteMatcher(enteredServer))) {
                if (!OpenCms.getSiteManager().getSites().get(new CmsSiteMatcher(enteredServer)).equals(m_site)) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SERVER_ALREADYUSED_1, enteredServer));
                }
            }
            if ((new CmsSiteMatcher(enteredServer)).equals(new CmsSiteMatcher(getFieldServer()))) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SERVER_EQUAL_ALIAS_0));
            }
            if (isDoubleAlias(enteredServer)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SERVER_ALREADYUSED_1, enteredServer));
            }
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

            //            if (m_alreadyUsedFolderPath.contains(getParentFolder() + enteredName)) {
            if (OpenCms.getSiteManager().getSiteForRootPath(getParentFolder() + enteredName) != null) {
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
            if (OpenCms.getSiteManager().getSiteForRootPath(
                CmsFileUtil.removeTrailingSeparator(getParentFolder())) != null) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_SITE_FOLDERNAME_ALREADYUSED_1,
                        CmsFileUtil.removeTrailingSeparator(getParentFolder())));
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
     * Validator for parent OU.<p>
     */
    class SelectOUValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -911831798529729185L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String OU = (String)value;
            if (OU.equals("/")) {
                return; //ok
            }

            if (OU.split("/").length < 2) {
                return; //ou is under root
            }

            OU = OU.split("/")[0] + "/";

            if (getParentFolder().isEmpty() | getFieldFolder().isEmpty()) {
                return; //not ok, but gets catched in an other validator
            }

            String rootPath = "/" + ensureFoldername(getParentFolder()) + ensureFoldername(getFieldFolder());

            boolean ok = false;

            try {
                List<CmsResource> res = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(m_clonedCms, OU);
                for (CmsResource resource : res) {
                    if (rootPath.startsWith(resource.getRootPath())) {
                        ok = true;
                    }
                }

            } catch (CmsException e) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_OU_INVALID_0));
            }
            if (!ok) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_OU_INVALID_0));
            }
        }

    }

    /**
     * Validator for parent OU.<p>
     */
    class SelectParentOUValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -911831798529729185L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String parentOU = (String)value;
            if (parentOU.equals("/")) {
                return; //ok
            }

            if (getParentFolder().isEmpty() | getFieldFolder().isEmpty()) {
                return; //not ok, but gets catched in an other validator
            }

            String rootPath = "/" + ensureFoldername(getParentFolder()) + ensureFoldername(getFieldFolder());

            boolean ok = false;

            try {
                List<CmsResource> res = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    m_clonedCms,
                    parentOU);
                for (CmsResource resource : res) {
                    if (rootPath.startsWith(resource.getRootPath())) {
                        ok = true;
                    }
                }

            } catch (CmsException e) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_PARENTOU_INVALID_0));
            }
            if (!ok) {
                throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_PARENTOU_INVALID_0));
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
     * Validator for site root (in case of editing a site, fails for broken sites.<p>
     */
    class SiteRootValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7499390905843603642L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        @Deprecated
        public void validate(Object value) throws InvalidValueException {

            CmsSite parentSite = m_manager.getElement(CmsFileUtil.removeTrailingSeparator((String)value));
            if (parentSite != null) {
                if (!parentSite.equals(m_site)) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(
                            Messages.GUI_SITE_FOLDERNAME_ALREADYUSED_1,
                            CmsFileUtil.removeTrailingSeparator((String)value)));
                }
            }

            CmsProject currentProject = m_clonedCms.getRequestContext().getCurrentProject();
            try {

                m_clonedCms.getRequestContext().setCurrentProject(
                    m_clonedCms.readProject(CmsProject.ONLINE_PROJECT_ID));
                m_clonedCms.readResource((String)value);

            } catch (CmsException e) {
                m_clonedCms.getRequestContext().setCurrentProject(currentProject);
                if (!m_clonedCms.existsResource((String)value)) {
                    throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SITEROOT_WRONG_0));
                }
            }

            m_clonedCms.getRequestContext().setCurrentProject(currentProject);
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

            if (CmsStringUtil.isEmptyOrWhitespaceOnly((String)value)) {
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

    /**vaadin serial id.*/
    private static final long serialVersionUID = -1011525709082939562L;

    protected CmsPathSelectField m_altSiteRoot;

    protected FormLayout m_altSiteRootPathContainer;

    protected TextField m_altSiteRootSuffix;

    /**Flag to block change events. */
    protected boolean m_blockChange;

    protected Button m_clearAltSiteRoot;

    /**List of all folder names already used for sites. */
    List<String> m_alreadyUsedFolderPath = new ArrayList<String>();

    /**List of all urls already used for sites.*/
    Set<CmsSiteMatcher> m_alreadyUsedURL = new HashSet<CmsSiteMatcher>();

    /**cloned cms obejct.*/
    CmsObject m_clonedCms;

    /**vaadin component.*/
    ComboBox m_fieldSelectOU;

    /**vaadin coponent.*/
    ComboBox m_fieldSelectParentOU;

    /**vaadin component. */
    Upload m_fieldUploadFavIcon;

    /**Needed to check if favicon was changed. */
    int m_imageCounter;

    /** The site manager instance.*/
    CmsSiteManager m_manager;

    /**OutputStream to store the uploaded favicon temporarily. */
    ByteArrayOutputStream m_os = new ByteArrayOutputStream(5500);

    /**current site which is supposed to be edited, null if site should be added.*/
    CmsSite m_site;

    /**vaadin component.*/
    TabSheet m_tab;

    /**button to add parameter.*/
    private Button m_addParameter;

    /**vaadin component.*/
    private VerticalLayout m_aliases;

    /**Edit group for workplace servers.*/
    private CmsEditableGroup m_aliasGroup;

    private CmsEditableGroup m_alternativeSiteRootPrefixGroup;

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

    /**vaadin component. */
    private CheckBox m_fieldKeepTemplate;

    /**vaadin component.*/
    private CmsPathSelectField m_fieldLoadSiteTemplate;

    /**vaadin component.*/
    private ComboBox m_fieldPosition;

    /**vaadin component.*/
    private TextField m_fieldSecureServer;

    /**vaadin component.*/
    private CheckBox m_fieldWebServer;

    /**vaadin component. */
    private Panel m_infoSiteRoot;

    /**boolean indicates if folder name was changed by user.*/
    private boolean m_isFolderNameTouched;

    /**vaadin component.*/
    private Button m_ok;

    /**Click listener for ok button. */
    private Button.ClickListener m_okClickListener;

    /**vaadin component.*/
    private FormLayout m_parameter;

    /**Panel holding the report widget.*/
    private Panel m_report;

    /**Vaadin component. */
    private ComboBox m_simpleFieldEncryption;

    /**vaadin component.*/
    private TextField m_simpleFieldFolderName;

    /**vaadin component.*/
    private CmsPathSelectField m_simpleFieldParentFolderName;

    /**vaadin component.*/
    private TextField m_simpleFieldServer;

    /**vaadin component.*/
    private CmsPathSelectField m_simpleFieldSiteRoot;

    /**vaadin component.*/
    private ComboBox m_simpleFieldTemplate;

    /**vaadin component.*/
    private TextField m_simpleFieldTitle;

    private ComboBox m_subsiteSelectionEnabled;

    /**List of templates. */
    private List<CmsResource> m_templates;

    /**Layout for the report widget. */
    private FormLayout m_threadReport;

    /**
     * Constructor.<p>
     * Use this to create a new site.<p>
     *
     * @param manager the site manager instance
     * @param cms the CmsObject
     */
    public CmsEditSiteForm(CmsObject cms, CmsSiteManager manager) {

        m_isFolderNameTouched = false;
        m_blockChange = true;
        m_autoSetFolderName = "";
        m_clonedCms = cms;

        List<CmsSite> allSites = manager.getAllElements();
        allSites.addAll(manager.getCorruptedSites());

        for (CmsSite site : allSites) {
            if (site.getSiteMatcher() != null) {
                m_alreadyUsedFolderPath.add(site.getSiteRoot());
            }
        }

        m_alreadyUsedURL.addAll(OpenCms.getSiteManager().getSites().keySet());

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_tab.setHeight("400px");
        m_infoSiteRoot.setVisible(false);
        m_simpleFieldSiteRoot.setVisible(false);

        if (!OpenCms.getSiteManager().isConfigurableWebServer()) {
            m_fieldWebServer.setVisible(false);
            m_fieldWebServer.setValue(Boolean.valueOf(true));

        }

        m_fieldKeepTemplate.setVisible(false);
        m_fieldKeepTemplate.setValue(Boolean.FALSE);

        m_simpleFieldParentFolderName.setValue(CmsSiteManager.PATH_SITES);
        m_simpleFieldParentFolderName.setUseRootPaths(true);
        m_simpleFieldParentFolderName.setCmsObject(m_clonedCms);
        m_simpleFieldParentFolderName.requireFolder();
        m_simpleFieldParentFolderName.setResourceFilter(CmsResourceFilter.DEFAULT_FOLDERS);
        m_simpleFieldParentFolderName.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 4043563040462776139L;

            public void valueChange(ValueChangeEvent event) {

                try {
                    String folderPath = m_simpleFieldParentFolderName.getValue();
                    if (CmsResourceTypeFolderSubSitemap.TYPE_SUBSITEMAP.equals(
                        OpenCms.getResourceManager().getResourceType(
                            m_clonedCms.readResource(folderPath)).getTypeName())) {
                        String newFolderName = folderPath.split("/")[folderPath.split("/").length - 1];
                        m_simpleFieldFolderName.setValue(newFolderName);
                        m_isFolderNameTouched = true;
                        if (m_simpleFieldTitle.isEmpty()) {
                            CmsProperty title = m_clonedCms.readPropertyObject(
                                m_clonedCms.readResource(folderPath),
                                "Title",
                                false);
                            if (!CmsProperty.getNullProperty().equals(title)) {
                                m_simpleFieldTitle.setValue(title.getValue());
                            }
                        }
                        setTemplateFieldForSiteroot(folderPath);
                        m_simpleFieldParentFolderName.setValue(
                            m_simpleFieldParentFolderName.getValue().substring(
                                0,
                                folderPath.length() - 1 - newFolderName.length()));
                    }
                } catch (CmsException e) {
                    // Resource was not found. Not ok, but will be validated later
                }
                setUpOUComboBox(m_fieldSelectParentOU);
                setUpOUComboBox(m_fieldSelectOU);
            }

        });

        m_manager = manager;

        m_addParameter.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6814134727761004218L;

            public void buttonClick(ClickEvent event) {

                addParameter(null);
            }
        });

        m_okClickListener = new ClickListener() {

            private static final long serialVersionUID = 6814134727761004218L;

            public void buttonClick(ClickEvent event) {

                validateAndSubmit();
            }
        };

        m_ok.addClickListener(m_okClickListener);

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -276802394623141951L;

            public void buttonClick(ClickEvent event) {

                closeDialog(false);
            }
        });

        m_fieldCreateOU.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -2837270577662919541L;

            public void valueChange(ValueChangeEvent event) {

                toggleSelectOU();

            }
        });

        setUpComboBoxPosition();
        setUpComboBoxTemplate();
        setUpComboBoxSSL();
        setupSubsiteSelectionMode();
        setUpOUComboBox(m_fieldSelectOU);
        setUpOUComboBox(m_fieldSelectParentOU);

        m_fieldSecureServer.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -2837270577662919541L;

            public void valueChange(ValueChangeEvent event) {

                toggleSecureServer();
            }
        });
        m_fieldExclusiveURL.setEnabled(false);
        m_fieldExclusiveError.setEnabled(false);
        Receiver uploadReceiver = new FavIconReceiver();

        m_fieldWebServer.setValue(Boolean.valueOf(true));

        m_fieldUploadFavIcon.setReceiver(uploadReceiver);
        m_fieldUploadFavIcon.setButtonCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SELECT_FILE_0));
        m_fieldUploadFavIcon.setImmediate(true);
        m_fieldUploadFavIcon.addSucceededListener((SucceededListener)uploadReceiver);
        m_fieldUploadFavIcon.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FAVICON_NEW_0));
        m_fieldFavIcon.setVisible(false);

        m_simpleFieldTitle.addBlurListener(new BlurListener() {

            private static final long serialVersionUID = -4147179568264310325L;

            public void blur(BlurEvent event) {

                if (!getFieldTitle().isEmpty() & !isFolderNameTouched()) {
                    String niceName = OpenCms.getResourceManager().getNameGenerator().getUniqueFileName(
                        m_clonedCms,
                        "/sites",
                        getFieldTitle().toLowerCase());
                    setFolderNameState(niceName);
                    setFieldFolder(niceName);
                }

            }
        });

        m_simpleFieldFolderName.addBlurListener(new BlurListener() {

            private static final long serialVersionUID = 2080245499551324408L;

            public void blur(BlurEvent event) {

                checkTemplate();
                setFolderNameState(null);

            }
        });

        m_fieldLoadSiteTemplate.addValidator(new SiteTemplateValidator());

        m_fieldLoadSiteTemplate.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -5859547073423161234L;

            public void valueChange(ValueChangeEvent event) {

                resetFields();
                loadMessageBundle();
                m_manager.centerWindow();

            }
        });

        m_fieldLoadSiteTemplate.setUseRootPaths(true);
        m_fieldLoadSiteTemplate.setCmsObject(m_clonedCms);
        m_fieldLoadSiteTemplate.requireFolder();
        m_fieldLoadSiteTemplate.setResourceFilter(CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder());

        m_fieldSelectParentOU.setEnabled(false);

        m_report.setVisible(false);
        m_blockChange = false;

        m_aliasGroup = new CmsEditableGroup(m_aliases, new Supplier<Component>() {

            public Component get() {

                return createAliasComponent("", CmsSiteMatcher.RedirectMode.temporary);

            }

        }, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_ALIAS_0));

        m_alternativeSiteRootPrefixGroup = new CmsEditableGroup(m_altSiteRootPathContainer, () -> {
            TextField pathField = new TextField();
            return pathField;

        }, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_PATH_0));
        m_clearAltSiteRoot.addClickListener(evt -> {
            m_alternativeSiteRootPrefixGroup.removeAll();
            m_altSiteRootSuffix.setValue("");
            m_altSiteRoot.setValue("");
        });
        m_aliasGroup.init();
        m_alternativeSiteRootPrefixGroup.init();

    }

    /**
     * Constructor.<p>
     * Used to edit existing site.<p>
     *
     * @param manager the manager instance
     * @param siteRoot of site to edit
     * @param cms the CmsObject
     */
    public CmsEditSiteForm(CmsObject cms, CmsSiteManager manager, String siteRoot) {

        this(cms, manager);
        m_site = manager.getElement(siteRoot);
        setFieldsForSite(true);

    }

    /**
     * Creates an IndexedContaienr for use in SSL mode selection widgets.<p>
     *
     * @param captionProp the name of the property to use for captions
     * @param includeOldStyle true if the old-style secure server mode should be included
     * @param currentValue the current value of the mode (may be null)
     *
     * @return the container with the SSL mode items
     */
    protected static IndexedContainer getSSLModeContainer(
        String captionProp,
        boolean includeOldStyle,
        CmsSSLMode currentValue) {

        IndexedContainer res = new IndexedContainer();
        res.addContainerProperty(captionProp, String.class, "");
        boolean isLetsEncrypt = currentValue == CmsSSLMode.LETS_ENCRYPT;
        boolean letsEncryptConfigured = (OpenCms.getLetsEncryptConfig() != null)
            && OpenCms.getLetsEncryptConfig().isValidAndEnabled();
        boolean skipLetsEncrypt = !letsEncryptConfigured && !isLetsEncrypt;

        for (CmsSSLMode mode : CmsSSLMode.availableModes(includeOldStyle, !skipLetsEncrypt)) {
            Item item = res.addItem(mode);
            item.getItemProperty(captionProp).setValue(mode.getLocalizedMessage());
        }
        return res;
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
     * Checks if site root exists in on and offline repository.<p>
     */
    protected void checkOnOfflineSiteRoot() {

        try {
            CmsObject cmsOnline = OpenCms.initCmsObject(m_clonedCms);
            cmsOnline.getRequestContext().setCurrentProject(m_clonedCms.readProject(CmsProject.ONLINE_PROJECT_ID));

            String rootPath = m_simpleFieldSiteRoot.getValue();
            if (cmsOnline.existsResource(rootPath) & !m_clonedCms.existsResource(rootPath)) {
                m_ok.setEnabled(false);
                m_infoSiteRoot.setVisible(true);
                return;
            }

            if (!m_site.getSiteRootUUID().isNullUUID()) {
                if (m_clonedCms.existsResource(m_site.getSiteRootUUID()) & !m_clonedCms.existsResource(rootPath)) {
                    m_ok.setEnabled(false);
                    m_infoSiteRoot.setVisible(true);
                    return;
                }
            }

        } catch (CmsException e) {
            LOG.error("Can not initialize CmsObject", e);
        }
        m_ok.setEnabled(true);
        m_infoSiteRoot.setVisible(false);
    }

    /**
     * Checks the Template Property of the site root and fills the form field.<p>
     */
    protected void checkTemplate() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_simpleFieldFolderName.getValue())) {
            return;
        }
        if (!m_clonedCms.existsResource(getSiteRoot())) {
            return;
        }
        try {
            String templateValue = m_clonedCms.readPropertyObject(
                getSiteRoot(),
                CmsPropertyDefinition.PROPERTY_TEMPLATE,
                false).getValue();
            m_simpleFieldTemplate.addItem(templateValue);
            m_simpleFieldTemplate.setValue(templateValue);
        } catch (CmsException e) {
            //
        }
    }

    /**
     * Creates field for aliases.<p>
     *
     * @param alias url
     * @param red redirect
     * @return component
     */
    protected FormLayout createAliasComponent(String alias, CmsSiteMatcher.RedirectMode redirectMode) {

        FormLayout layout = new FormLayout();
        TextField field = new TextField(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ALIAS_0));
        field.setWidth("100%");
        field.setValue(alias);
        field.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ALIAS_HELP_0));
        ComboBox redirectSelection = new ComboBox();
        redirectSelection.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ALIAS_REDIRECT_0));
        redirectSelection.setWidth("100%");
        redirectSelection.addItem(RedirectMode.none);
        redirectSelection.addItem(RedirectMode.temporary);
        redirectSelection.addItem(RedirectMode.permanent);
        redirectSelection.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
        redirectSelection.setItemCaption(
            RedirectMode.none,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_REDIRECT_MODE_NONE_0));
        redirectSelection.setItemCaption(
            RedirectMode.temporary,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_REDIRECT_MODE_TEMPORARY_0));
        redirectSelection.setItemCaption(
            RedirectMode.permanent,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_REDIRECT_MODE_PERMANENT_0));
        redirectSelection.setNullSelectionAllowed(false);
        redirectSelection.setNewItemsAllowed(false);
        redirectSelection.setTextInputAllowed(false);
        redirectSelection.setValue(redirectMode);
        // CheckBox redirect = new CheckBox(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ALIAS_REDIRECT_0), red);
        redirectSelection.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ALIAS_REDIRECT_HELP_0));
        layout.addComponent(field);
        layout.addComponent(redirectSelection);
        return layout;
    }

    /**
     * Reads server field.<p>
     *
     * @return server as string
     */
    protected String getFieldServer() {

        return m_simpleFieldServer.getValue();
    }

    /**
     * Handles SSL changes.<p>
     */
    protected void handleSSLChange() {

        String toBeReplaced = "http:";
        String newString = "https:";
        CmsSSLMode mode = (CmsSSLMode)m_simpleFieldEncryption.getValue();
        if (mode == null) {
            // mode is null if this is triggered by setContainerDataSource
            return;
        }
        if (mode.equals(CmsSSLMode.NO) | mode.equals(CmsSSLMode.SECURE_SERVER)) {
            toBeReplaced = "https:";
            newString = "http:";
        }
        m_simpleFieldServer.setValue(m_simpleFieldServer.getValue().replaceAll(toBeReplaced, newString));
        m_fieldSecureServer.setVisible(mode.equals(CmsSSLMode.SECURE_SERVER));
        m_fieldExclusiveError.setVisible(mode.equals(CmsSSLMode.SECURE_SERVER));
        m_fieldExclusiveURL.setVisible(mode.equals(CmsSSLMode.SECURE_SERVER));

    }

    /**
     * Sets the template field depending on current set site root field(s).<p>
     */
    protected void setTemplateField() {

        setTemplateFieldForSiteroot(getSiteRoot());
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
     * Closes the dialog.<p>
     *
     * @param updateTable <code>true</code> to update the site table
     */
    void closeDialog(boolean updateTable) {

        m_manager.closeDialogWindow(updateTable);
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
     * Checks if an alias was entered twice.<p>
     *
     * @param aliasName to check
     * @return true if it was defined double
     */
    boolean isDoubleAlias(String aliasName) {

        CmsSiteMatcher testAlias = new CmsSiteMatcher(aliasName);
        int count = 0;
        for (Component c : m_aliases) {
            if (c instanceof CmsRemovableFormRow<?>) {
                String alName = (String)((CmsRemovableFormRow<? extends AbstractField<?>>)c).getInput().getValue();
                if (testAlias.equals(new CmsSiteMatcher(alName))) {
                    count++;
                }
            }
        }
        return count > 1;
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
     * Are the aliase valid?<p>
     *
     * @return true if ok
     */
    boolean isValidAliase() {

        boolean ret = true;

        for (I_CmsEditableGroupRow row : m_aliasGroup.getRows()) {
            FormLayout layout = (FormLayout)(row.getComponent());
            TextField field = (TextField)layout.getComponent(0);
            ret = ret & field.isValid();
        }
        return ret;
    }

    boolean isValidAltSiteRootSettings() {

        return m_altSiteRoot.isValid();
    }

    /**
     * Checks if all required fields are set correctly at first Tab.<p>
     *
     * @return true if all inputs are valid.
     */
    boolean isValidInputSimple() {

        return (m_simpleFieldFolderName.isValid()
            & m_simpleFieldServer.isValid()
            & m_simpleFieldSiteRoot.isValid()
            & m_simpleFieldTitle.isValid()
            & m_simpleFieldParentFolderName.isValid()
            & m_fieldSelectOU.isValid()
            & m_simpleFieldSiteRoot.isValid());
    }

    /**
     * Checks if all required fields are set correctly at site template tab.<p>
     *
     * @return true if all inputs are valid.
     */
    boolean isValidInputSiteTemplate() {

        return (m_fieldLoadSiteTemplate.isValid() & m_fieldSelectParentOU.isValid());
    }

    /**
     * Checks if manual secure server is valid.<p>
     *
     * @return boolean
     */
    boolean isValidSecureServer() {

        if (m_fieldSecureServer.isVisible()) {
            return m_fieldSecureServer.isValid();
        }
        return true;
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
     * Clears the message bundle and removes related text fields from UI.<p>
     */
    void resetFields() {

        if (m_bundleComponentKeyMap != null) {
            Set<TextField> setBundles = m_bundleComponentKeyMap.keySet();

            for (TextField field : setBundles) {
                m_bundleValues.removeComponent(field);
            }
            m_bundleComponentKeyMap.clear();
        }
        m_fieldKeepTemplate.setVisible(!CmsStringUtil.isEmptyOrWhitespaceOnly(m_fieldLoadSiteTemplate.getValue()));
        m_fieldKeepTemplate.setValue(
            Boolean.valueOf(!CmsStringUtil.isEmptyOrWhitespaceOnly(m_fieldLoadSiteTemplate.getValue())));
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
        }, ""));
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
     * Enables the ok button after finishing report thread.<p>
     */
    void setOkButtonEnabled() {

        m_ok.setEnabled(true);
        m_ok.setCaption(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CLOSE_0));
        m_ok.removeClickListener(m_okClickListener);
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 5637556711524961424L;

            public void buttonClick(ClickEvent event) {

                closeDialog(true);
            }
        });
    }

    /**
     * Fill ComboBox for OU selection.<p>
     * @param combo combo box
     */
    void setUpOUComboBox(ComboBox combo) {

        combo.removeAllItems();
        try {
            if (m_site != null) {
                String siteOu = getSiteOU();
                combo.addItem(siteOu);
                combo.select(siteOu);
                combo.setEnabled(false);
            } else {
                combo.addItem("/");

                m_clonedCms.getRequestContext().setSiteRoot("");
                List<CmsOrganizationalUnit> ous = OpenCms.getOrgUnitManager().getOrganizationalUnits(
                    m_clonedCms,
                    "/",
                    true);

                for (CmsOrganizationalUnit ou : ous) {

                    if (ouIsOK(ou)) {
                        combo.addItem(ou.getName());
                    }

                }
                combo.select("/");
            }

        } catch (CmsException e) {
            LOG.error("Error on reading OUs", e);
        }
        combo.setNullSelectionAllowed(false);
        combo.setTextInputAllowed(true);
        combo.setFilteringMode(FilteringMode.CONTAINS);
        combo.setNewItemsAllowed(false);

    }

    /**
     * Setup for the aliase validator.<p>
     */
    void setupValidatorAliase() {

        for (I_CmsEditableGroupRow row : m_aliasGroup.getRows()) {
            FormLayout layout = (FormLayout)(row.getComponent());
            TextField field = (TextField)layout.getComponent(0);
            field.removeAllValidators();
            field.addValidator(new AliasValidator());
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
            if ((m_simpleFieldSiteRoot != null) && m_simpleFieldSiteRoot.isVisible()) {
                m_simpleFieldSiteRoot.addValidator(value -> {
                    String siteRoot = (String)value;
                    try {
                        OpenCms.getSiteManager().validateSiteRoot(siteRoot);
                    } catch (Exception e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                        throw new InvalidValueException(e.getMessage());
                    }
                });
            }
            m_simpleFieldServer.addValidator(new ServerValidator());
            if (m_fieldSecureServer.isVisible()) {
                m_fieldSecureServer.addValidator(new AliasValidator());
            }
            m_simpleFieldTitle.addValidator(new TitleValidator());
            if (m_site == null) {
                m_fieldSelectOU.addValidator(new SelectOUValidator());
            }
            if (m_fieldCreateOU.getValue().booleanValue()) {
                m_fieldSelectParentOU.addValidator(new SelectParentOUValidator());
            }

            m_altSiteRoot.addValidator(fieldValue -> {
                String path = (String)fieldValue;
                if (path == null) {
                    return;
                }
                path = path.trim();
                CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(path);
                if (site != null) {
                    if (site.isGenerated()) {
                        if (!m_site.getSiteMatcher().equals(site.getSiteMatcher())) {
                            throw new InvalidValueException(
                                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_ALREADYUSED_1, path));
                        }
                    } else {
                        throw new InvalidValueException(
                            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FOLDERNAME_ALREADYUSED_1, path));
                    }
                }

                for (String forbiddenPrefix : new String[] {OpenCms.getSiteManager().getSharedFolder(), "/system"}) {
                    if (CmsStringUtil.isPrefixPath(forbiddenPrefix, path)) {
                        throw new InvalidValueException("Forbidden path for alternative site root rule.");
                    }
                }

                try {
                    CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                    cms.getRequestContext().setSiteRoot("");
                    cms.readResource(path);
                } catch (CmsException e) {
                    throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
                }
            });
        }
    }

    /**
     * Saves the entered site-data as a CmsSite object.<p>
     */
    void submit() {

        // switch to root site
        m_clonedCms.getRequestContext().setSiteRoot("");

        CmsSite site = getSiteFromForm();

        if (m_site == null) {

            //Show report field and hide form fields
            m_report.setVisible(true);
            m_tab.setVisible(false);
            m_ok.setEnabled(false);
            m_ok.setVisible(true);
            //Change cancel caption to close (will not interrupt site creation anymore)
            m_cancel.setVisible(false);
            setOkButtonEnabled();
            m_cancel.setCaption(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CLOSE_0));

            Map<String, String> bundle = getBundleMap();

            boolean createOU = m_fieldCreateOU.isEnabled() & m_fieldCreateOU.getValue().booleanValue();
            CmsCreateSiteThread createThread = new CmsCreateSiteThread(
                m_clonedCms,
                m_manager,
                site,
                m_site,
                m_fieldLoadSiteTemplate.getValue(),
                getFieldTemplate(),
                createOU,
                (String)m_fieldSelectParentOU.getValue(),
                (String)m_fieldSelectOU.getValue(),
                m_os,
                bundle,
                new Runnable() {

                    public void run() {

                    }

                });

            CmsReportWidget report = new CmsReportWidget(createThread);

            report.setWidth("100%");
            report.setHeight("350px");

            m_threadReport.addComponent(report);
            createThread.start();
        } else {
            if (!site.getSiteRoot().equals(m_site.getSiteRoot())) {
                m_manager.deleteElements(Collections.singletonList(m_site.getSiteRoot()));
            }
            m_manager.writeElement(site);
            m_manager.closeDialogWindow(true);
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
        m_fieldSelectParentOU.setEnabled(create);
        m_fieldSelectOU.select("/");
    }

    private void fillAlternativeSiteRootTab(CmsAlternativeSiteRootMapping alternativeSiteRootMapping) {

        if (alternativeSiteRootMapping != null) {
            m_altSiteRoot.setValue(alternativeSiteRootMapping.getSiteRoot().asString());
            m_altSiteRootSuffix.setValue(alternativeSiteRootMapping.getTitleSuffix());
            for (CmsPath prefix : alternativeSiteRootMapping.getPrefixes()) {
                TextField prefixField = new TextField();
                prefixField.setValue(prefix.asString());
                m_alternativeSiteRootPrefixGroup.addRow(prefixField);
            }
        }
    }

    /**
     * Reads out all aliases from the form.<p>
     *
     * @return a List of CmsSiteMatcher
     */
    private List<CmsSiteMatcher> getAliases() {

        List<CmsSiteMatcher> ret = new ArrayList<CmsSiteMatcher>();
        for (I_CmsEditableGroupRow row : m_aliasGroup.getRows()) {
            FormLayout layout = (FormLayout)(row.getComponent());
            ComboBox box = (ComboBox)(layout.getComponent(1));
            TextField field = (TextField)layout.getComponent(0);
            CmsSiteMatcher matcher = new CmsSiteMatcher(field.getValue());
            matcher.setRedirectMode((RedirectMode)(box.getValue()));
            ret.add(matcher);
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

        //First look for a bundle with the locale of the folder..
        try {
            CmsProperty propLoc = m_clonedCms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_LOCALE, true);
            if (!propLoc.isNullProperty()) {
                if (m_clonedCms.existsResource(path + baseName + "_" + propLoc.getValue())) {
                    return baseName + "_" + propLoc.getValue();
                }
            }
        } catch (CmsException e) {
            LOG.error("Can not read locale property", e);
        }

        //If no bundle was found with the locale of the folder, or the property was not set, search for other locales
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

        return null;
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
     * Reads ComboBox with Template information.<p>
     *
     * @return string of chosen template path.
     */
    private String getFieldTemplate() {

        if (m_fieldKeepTemplate.getValue().booleanValue()) {
            return ""; //No template property will be changed
        }
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
                String[] parameterStringArray = ((String)((CmsRemovableFormRow<? extends AbstractField<?>>)c).getInput().getValue()).split(
                    "=");
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

        String siteRoot = getSiteRoot();
        CmsSiteMatcher matcher = (CmsStringUtil.isNotEmpty(m_fieldSecureServer.getValue())
            & m_simpleFieldEncryption.getValue().equals(CmsSSLMode.SECURE_SERVER))
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
            aliases,
            ((Boolean)m_subsiteSelectionEnabled.getValue()).booleanValue());
        ret.setParameters((SortedMap<String, String>)getParameter());
        ret.setSSLMode((CmsSSLMode)m_simpleFieldEncryption.getValue());
        String alternativeSiteRoot = m_altSiteRoot.getValue();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(alternativeSiteRoot)) {
            String suffix = m_altSiteRootSuffix.getValue();
            List<String> prefixPaths = new ArrayList<>();
            for (I_CmsEditableGroupRow prefixRow : m_alternativeSiteRootPrefixGroup.getRows()) {
                TextField prefixField = (TextField)(prefixRow.getComponent());
                prefixPaths.add(prefixField.getValue());
            }
            ret.setAlternativeSiteRootMapping(
                Optional.of(new CmsAlternativeSiteRootMapping(alternativeSiteRoot, prefixPaths, suffix)));
        }

        return ret;
    }

    /**
     * Get ou name for current site.<p>
     *
     * @return Full ou name
     */
    private String getSiteOU() {

        try {
            m_clonedCms.getRequestContext().setSiteRoot("");
            CmsResource resource = m_clonedCms.readResource(m_site.getSiteRoot());
            List<CmsRelation> relations = m_clonedCms.getRelationsForResource(resource, CmsRelationFilter.SOURCES);
            for (CmsRelation relation : relations) {
                if (relation.getSourcePath().startsWith("/system/orgunits/")) {
                    return (relation.getSourcePath().substring("/system/orgunits/".length()));
                }
            }
        } catch (CmsException e) {
            LOG.error("Error on reading OUs", e);
        }
        return "/";
    }

    /**
     * Gets the site root.<p>
     * Usable for new sites and for existing sites.
     *
     * @return site root string
     */
    private String getSiteRoot() {

        String res;

        if (m_simpleFieldSiteRoot.isVisible()) {
            res = m_simpleFieldSiteRoot.getValue();
        } else {
            res = "/" + ensureFoldername(getParentFolder()) + ensureFoldername(getFieldFolder());
            res = res.endsWith("/") ? res.substring(0, res.length() - 1) : res;
        }
        return res;
    }

    /**
     * Checks if given Ou has resources matching to currently set parent folder.<p>
     *
     * @param ou to check
     * @return true if ou is ok for parent folder
     */
    private boolean ouIsOK(CmsOrganizationalUnit ou) {

        try {
            for (CmsResource res : OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                m_clonedCms,
                ou.getName())) {

                if (m_simpleFieldParentFolderName.getValue().startsWith(res.getRootPath())) {
                    return true;
                }

            }
        } catch (CmsException e) {
            LOG.error("Unable to read Resources for Org Unit", e);
        }
        return false;
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
     * Sets the fields for a given site (m_site).<p>
     *
     * @param enableAll if true, the site is editable
     */
    private void setFieldsForSite(boolean enableAll) {

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_site.getSiteRoot())) {
            setTemplateFieldForSiteroot(m_site.getSiteRoot());
            m_simpleFieldTemplate.setEnabled(false);
        }

        m_simpleFieldSiteRoot.setVisible(true);
        m_simpleFieldSiteRoot.setValue(m_site.getSiteRoot());
        m_simpleFieldSiteRoot.setCmsObject(m_clonedCms);
        m_simpleFieldSiteRoot.addValidator(new SiteRootValidator());
        m_simpleFieldSiteRoot.setEnabled(enableAll);
        m_simpleFieldSiteRoot.addValueChangeListener(new ValueChangeListener() {

            /**vaadin serial id. */
            private static final long serialVersionUID = 4680456758446195524L;

            public void valueChange(ValueChangeEvent event) {

                setTemplateField();
                checkOnOfflineSiteRoot();
            }

        });
        m_simpleFieldParentFolderName.setVisible(false);
        m_simpleFieldFolderName.setVisible(false);
        String title = m_site.getTitle();
        if (title == null) {
            title = "";
        }
        CmsResourceInfo resourceInfo = new CmsResourceInfo(
            title,
            m_site.getSiteRoot(),
            m_manager.getFavIcon(m_site.getSiteRoot()));
        resourceInfo.addStyleName("o-res-site-info");
        displayResourceInfoDirectly(Collections.singletonList(resourceInfo));

        m_tab.removeTab(m_tab.getTab(5));

        m_simpleFieldTitle.removeTextChangeListener(null);
        m_simpleFieldTitle.setEnabled(enableAll);
        m_simpleFieldParentFolderName.setEnabled(false);
        m_simpleFieldParentFolderName.setValue(
            m_site.getSiteRoot().substring(
                0,
                m_site.getSiteRoot().length()
                    - m_site.getSiteRoot().split("/")[m_site.getSiteRoot().split("/").length - 1].length()));

        m_simpleFieldFolderName.removeAllValidators(); //can not be changed
        m_fieldCreateOU.setVisible(false);

        m_alreadyUsedURL.remove(m_site.getSiteMatcher().forDifferentScheme("https")); //Remove current url to avoid validation problem
        m_alreadyUsedURL.remove(m_site.getSiteMatcher().forDifferentScheme("http"));

        setFieldTitle(title);
        setFieldFolder(getFolderNameFromSiteRoot(m_site.getSiteRoot()));
        m_subsiteSelectionEnabled.setValue(Boolean.valueOf(m_site.isSubsiteSelectionEnabled()));
        m_simpleFieldFolderName.setEnabled(false);
        m_simpleFieldTitle.setEnabled(enableAll);

        setFieldServer(m_site.getUrl());
        m_simpleFieldServer.setEnabled(enableAll);
        if (m_site.hasSecureServer()) {
            m_fieldSecureServer.setValue(m_site.getSecureUrl());
        }
        if (m_site.getErrorPage() != null) {
            m_fieldErrorPage.setValue(m_site.getErrorPage());
        }
        m_fieldWebServer.setValue(Boolean.valueOf(m_site.isWebserver()));
        m_fieldWebServer.setEnabled(enableAll);
        m_fieldExclusiveURL.setValue(Boolean.valueOf(m_site.isExclusiveUrl()));
        m_fieldExclusiveURL.setEnabled(enableAll);
        m_fieldExclusiveError.setValue(Boolean.valueOf(m_site.isExclusiveError()));
        m_fieldExclusiveError.setEnabled(enableAll);

        Map<String, String> siteParameters = m_site.getParameters();
        for (Entry<String, String> parameter : siteParameters.entrySet()) {
            addParameter(getParameterString(parameter));
        }

        List<CmsSiteMatcher> siteAliases = m_site.getAliases();

        for (CmsSiteMatcher siteMatcher : siteAliases) {
            if (enableAll) {
                m_aliasGroup.addRow(createAliasComponent(siteMatcher.getUrl(), siteMatcher.getRedirectMode()));
            } else {
                Component c = createAliasComponent(siteMatcher.getUrl(), siteMatcher.getRedirectMode());
                c.setEnabled(false);
                m_aliases.addComponent(c);
            }
        }

        setTemplateField();

        setUpComboBoxPosition();

        if (!m_fieldSecureServer.isEmpty()) {
            m_fieldExclusiveURL.setEnabled(true && enableAll);
            m_fieldExclusiveError.setEnabled(true && enableAll);
        }
        setFaviconIfExist();
        checkOnOfflineSiteRoot();
        m_fieldUploadFavIcon.setVisible(false);
        m_simpleFieldEncryption.setContainerDataSource(getSSLModeContainer("caption", true, m_site.getSSLMode()));
        m_simpleFieldEncryption.select(m_site.getSSLMode());
        m_simpleFieldEncryption.setEnabled(enableAll);

        m_fieldErrorPage.setEnabled(enableAll);
        m_addParameter.setVisible(enableAll);
        m_fieldPosition.setEnabled(enableAll);

        fillAlternativeSiteRootTab(m_site.getAlternativeSiteRootMapping().orElse(null));
    }

    /**
     * Sets the title field.<p>
     *
     * @param newValue value of the field.
     */
    private void setFieldTitle(String newValue) {

        m_simpleFieldTitle.setValue(newValue);
    }

    private void setTemplateFieldForSiteroot(String siteroot) {

        try {
            CmsProperty prop = m_clonedCms.readPropertyObject(siteroot, CmsPropertyDefinition.PROPERTY_TEMPLATE, false);
            if (!prop.isNullProperty()) {
                if (!m_templates.contains(prop.getValue())) {
                    m_simpleFieldTemplate.addItem(prop.getValue());
                }
                m_simpleFieldTemplate.select(prop.getValue());
            } else {
                if (!m_templates.isEmpty()) {
                    m_simpleFieldTemplate.setValue(m_templates.get(0).getRootPath());
                }
            }
        } catch (CmsException e) {
            LOG.error("Unable to read template property.", e);
            m_simpleFieldTemplate.setValue(null);
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
     * Sets up the ComboBox for the SSL Mode.<p>
     */
    private void setUpComboBoxSSL() {

        IndexedContainer container = getSSLModeContainer("caption", true, null);

        m_simpleFieldEncryption.setContainerDataSource(container);
        m_simpleFieldEncryption.setItemCaptionPropertyId("caption");
        m_simpleFieldEncryption.setNullSelectionAllowed(false);
        m_simpleFieldEncryption.setNewItemsAllowed(false);
        m_simpleFieldEncryption.select(CmsSSLMode.getDefault());

        m_simpleFieldEncryption.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 3267990233897064320L;

            public void valueChange(ValueChangeEvent event) {

                if (m_blockChange) {
                    return;
                }
                handleSSLChange();
            }
        });

        m_fieldSecureServer.setVisible(CmsSSLMode.getDefault().equals(CmsSSLMode.SECURE_SERVER));
        m_fieldExclusiveError.setVisible(CmsSSLMode.getDefault().equals(CmsSSLMode.SECURE_SERVER));
        m_fieldExclusiveURL.setVisible(CmsSSLMode.getDefault().equals(CmsSSLMode.SECURE_SERVER));

    }

    /**
     * Sets the combobox for the template.<p>
     */
    private void setUpComboBoxTemplate() {

        try {
            I_CmsResourceType templateType = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeJsp.getContainerPageTemplateTypeName());
            m_templates = m_clonedCms.readResources("/system/", CmsResourceFilter.DEFAULT.addRequireType(templateType));
            for (CmsResource res : m_templates) {
                m_simpleFieldTemplate.addItem(res.getRootPath());
            }

            if (!m_templates.isEmpty()) {
                m_simpleFieldTemplate.setValue(m_templates.get(0).getRootPath());
            }

            m_simpleFieldTemplate.setNewItemsAllowed(true);
            m_simpleFieldTemplate.setNullSelectionAllowed(true);

        } catch (CmsException e) {
            // should not happen
        }
    }

    /**
     * Sets up the select box for the subsite selection mode.
     */
    private void setupSubsiteSelectionMode() {

        m_subsiteSelectionEnabled.addItem(Boolean.FALSE);
        m_subsiteSelectionEnabled.addItem(Boolean.TRUE);
        m_subsiteSelectionEnabled.setValue(Boolean.FALSE);
        m_subsiteSelectionEnabled.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
        m_subsiteSelectionEnabled.setNullSelectionAllowed(false);
        m_subsiteSelectionEnabled.setItemCaption(
            Boolean.FALSE,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SUBSITE_SELECTION_DISABLED_0));
        m_subsiteSelectionEnabled.setItemCaption(
            Boolean.TRUE,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SUBSITE_SELECTION_ENABLED_0));
    }

    private void validateAndSubmit() {

        setupValidators();
        setupValidatorAliase();
        if (isValidInputSimple()
            & isValidInputSiteTemplate()
            & isValidAliase()
            & isValidSecureServer()
            & isValidAltSiteRootSettings()) {
            submit();
            return;
        }
        if (!isValidInputSimple()) {
            m_tab.setSelectedTab(0);
        }

        if (!isValidSecureServer()) {
            m_tab.setSelectedTab(1);
        }

        if (!isValidAliase()) {
            m_tab.setSelectedTab(3);
        }
        if (!isValidAltSiteRootSettings()) {
            m_tab.setSelectedTab(4);
        }

        if (!isValidInputSiteTemplate()) {
            m_tab.setSelectedTab(5);
        }
    }
}
