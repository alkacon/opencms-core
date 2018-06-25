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

package org.opencms.ui.apps.resourcetypes;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsVfsBundleManager;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.report.CmsLogReport;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.content.CmsVfsBundleLoaderXml;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

import com.google.common.collect.Lists;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;

/**
 * Dialog to edit or create resourcetypes.<p>
 */
public class CmsEditResourceTypeDialog extends CmsBasicDialog {

    /**
     * Validator for the title field.<p>
     */
    class IDValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7878441125879949490L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (((String)value).isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_ID_0));
            }
            int id = Integer.parseInt((String)value);
            if (!CmsResourceTypeApp.isResourceTypeIdFree(id)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_ID_0));
            }

        }

    }

    /**
     * Validator for the title field.<p>
     */
    class NameValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7878441125879949490L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (((String)value).isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_NAME_0));
            }
            if (!CmsResourceTypeApp.isResourceTypeNameFree((String)value)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_NAME_0));
            }

        }

    }

    /**
     * Validator for the title field.<p>
     */
    class ResourceValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7878441125879949490L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if ((value == null) || ((String)value).isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_RESORUCE_0));
            }

            String resource = (String)value;
            if (!resource.startsWith("/system/")) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_RESORUCE_0));
            }
            if (CmsResource.getName(resource).equals(SCHEMA) || CmsResource.getName(resource).equals(FORMATTER)) {
                if (!m_cms.existsResource(CmsResource.getParentFolder(resource))) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_RESORUCE_0));
                }
                return;
            }
            if (!m_cms.existsResource((String)value)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_RESORUCE_0));
            }

        }

    }

    /** Message bundle folder name. */
    protected static final String PATH_I18N = "i18n";

    /** Formatter folder name.*/
    private static final String FORMATTER = "formatters/";

    private static final String ICON_MODE_CSS = "css";

    private static final String ICON_MODE_FILE = "file";

    /** Message key prefix. */
    private static final String KEY_PREFIX_DESCRIPTION = "desc.";

    /** Message key prefix. */
    private static final String KEY_PREFIX_FORMATTER = "formatter.name.detail";

    /** Message key prefix. */
    private static final String KEY_PREFIX_NAME = "fileicon.";

    /** Message key prefix. */
    private static final String KEY_PREFIX_TITLE = "title.";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditResourceTypeDialog.class);

    /**Sample path. */
    private static final String PATH_SAMPLE = "/system/modules/org.opencms.base/copyresources/";

    /** Message properties file encoding. */
    private static final String PROPERTIES_ENCODING = "ISO-8859-1";
    /** Message properties file name. */
    private static final String PROPERTIES_FILE_NAME = "workplace.properties";
    /** Sample formatter. */
    private static final String SAMPLE_FORMATTER = PATH_SAMPLE + "sample-formatter.jsp";

    /** Sample formatter config. */
    private static final String SAMPLE_FORMATTER_CONFIG = PATH_SAMPLE + "sample-formatter.xml";

    /** Sample schema. */
    private static final String SAMPLE_SCHEMA = PATH_SAMPLE + "sample-schema.xsd";

    /** Schema folder name.*/
    private static final String SCHEMA = "shemas/";
    /** Message bundle file name suffix. */
    private static final String SUFFIX_BUNDLE_FILE = ".messages";

    /**CmsObject. */
    CmsObject m_cms;

    /** vaadin component.*/
    private TextField m_bigIconCSS;

    /** vaadin component.*/
    private CmsPathSelectField m_bigIconFile;

    /** vaadin component.*/
    private Button m_cancel;

    /** vaadin component.*/
    private Button m_chooseModule;

    /** vaadin component.*/
    private OptionGroup m_iconMode;

    /** vaadin component.*/
    private Label m_moduleName;

    /** vaadin component.*/
    private Button m_ok;

    private CmsPathSelectField m_parentFormatter;

    /** vaadin component.*/
    private CmsPathSelectField m_parentSchema;

    /** vaadin component.*/
    private TextField m_schema;

    /** vaadin component.*/
    private TextField m_smallIconCSS;

    /** vaadin component.*/
    private CmsPathSelectField m_smallIconFile;

    /** vaadin component.*/
    private I_CmsResourceType m_type;

    /** vaadin component.*/
    private TextArea m_typeDescription;

    /** vaadin component.*/
    private TextField m_typeID;

    /** vaadin component.*/
    private TextField m_typeName;

    /** vaadin component.*/
    private TextField m_typeShortName;

    /** vaadin component.*/
    private TextField m_typeTitle;

    /**
     * public constructor.<p>
     *
     * @param window window
     * @param app app
     */
    public CmsEditResourceTypeDialog(final Window window, CmsResourceTypeApp app) {

        init(window, app);

    }

    /**
     * Public cosntructor.<p>
     *
     * @param window window
     * @param app app
     * @param type type to be edited
     */
    public CmsEditResourceTypeDialog(final Window window, CmsResourceTypeApp app, I_CmsResourceType type) {

        m_type = type;
        init(window, app);

    }

    /**
     * Get parent folder of messages.<p>
     *
     * @param moduleName name of module
     * @return path name
     */
    protected static String getMessageParentFolder(String moduleName) {

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        for (String resource : module.getResources()) {
            if (resource.contains(PATH_I18N + "/" + module.getName())) {
                return resource.substring(0, resource.indexOf(PATH_I18N));
            }
        }
        return "/system/modules/" + module.getName();
    }

    /**
     * Tries to find the module folder under /system/modules for a given module.<p>
     *
     * @param moduleName the name of the module
     *
     * @return the module folder, or null if this module doesn't have one
     */
    private static String getModuleFolder(String moduleName) {

        if (moduleName == null) {
            return null;
        }
        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        if (module != null) {
            for (String resource : module.getResources()) {
                if (CmsStringUtil.comparePaths("/system/modules/" + moduleName, resource)) {
                    return resource;
                }
            }
        }
        return null;
    }

    /**
     * Opens the module select dialog.<p>
     *
     * @param window window
     */
    protected void openModuleSelect(Window window) {

        window.setContent(new CmsMoveResourceTypeDialog(this));
        window.center();
    }

    /**
     * Sets the module name.<p>
     *
     * @param moduleName to be set
     * @param dialog dialog
     */
    protected void setModule(String moduleName, CmsBasicDialog dialog) {

        if (moduleName != null) {
            m_moduleName.setValue(moduleName);
        }
        CmsVaadinUtils.getWindow(dialog).setContent(this);
        String folderName = getModuleFolder(moduleName);
        if (folderName != null) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_parentFormatter.getValue())) {
                m_parentFormatter.setValue(folderName + FORMATTER);
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_parentSchema.getValue())) {
                m_parentSchema.setValue(folderName + SCHEMA);
            }
        }
        m_ok.setEnabled(m_moduleName != null);
        m_parentFormatter.removeAllValidators();
        m_parentSchema.removeAllValidators();
        m_parentFormatter.addValidator(new ResourceValidator());
        m_parentSchema.addValidator(new ResourceValidator());
    }

    /**
     * Sets the visibility of the icon fields.<p>
     */
    protected void setVisibilityOfIconFields() {

        if (ICON_MODE_CSS.equals(m_iconMode.getValue())) {
            m_smallIconCSS.setVisible(true);
            m_bigIconCSS.setVisible(true);
            m_smallIconFile.setVisible(false);
            m_bigIconFile.setVisible(false);
            return;
        }
        if (ICON_MODE_FILE.equals(m_iconMode.getValue())) {
            m_smallIconCSS.setVisible(false);
            m_bigIconCSS.setVisible(false);
            m_smallIconFile.setVisible(true);
            m_bigIconFile.setVisible(true);
            return;
        }
        m_smallIconCSS.setVisible(false);
        m_bigIconCSS.setVisible(false);
        m_smallIconFile.setVisible(false);
        m_bigIconFile.setVisible(false);
    }

    /**
     * Submit the entered data.<p>
     *
     * @param window window
     * @param app app
     */
    protected void submit(Window window, CmsResourceTypeApp app) {

        if (isValid()) {
            CmsModule module = (CmsModule)OpenCms.getModuleManager().getModule(m_moduleName.getValue()).clone();
            if (m_type == null) {
                createResourceType(module);
            } else {
                saveResourceType(module);
            }
            try {
                OpenCms.getModuleManager().updateModule(m_cms, module);
                OpenCms.getResourceManager().initialize(m_cms);
                OpenCms.getWorkplaceManager().addExplorerTypeSettings(module);
                // re-initialize the workplace
                OpenCms.getWorkplaceManager().initialize(m_cms);
            } catch (CmsException e) {
                LOG.error("Unable to save resource type", e);
            }
            window.close();
            app.reload();

        }
    }

    /**
     * Adds the given messages to the workplace properties file.<p>
     *
     * @param messages the messages
     * @param propertiesFile the properties file
     * @param forcePropertyFileEncoding flag, indicating if encoding {@link #PROPERTIES_ENCODING} should be forced
     *
     * @throws CmsException if writing the properties fails
     * @throws UnsupportedEncodingException in case of encoding issues
     */
    private void addMessagesToPropertiesFile(
        Map<String, String> messages,
        CmsFile propertiesFile,
        boolean forcePropertyFileEncoding)
    throws CmsException, UnsupportedEncodingException {

        lockTemporary(propertiesFile);
        String encoding = forcePropertyFileEncoding
        ? PROPERTIES_ENCODING
        : CmsFileUtil.getEncoding(m_cms, propertiesFile);
        StringBuffer contentBuffer = new StringBuffer();
        contentBuffer.append(new String(propertiesFile.getContents(), encoding));
        for (Entry<String, String> entry : messages.entrySet()) {
            contentBuffer.append("\n");
            contentBuffer.append(entry.getKey());
            contentBuffer.append("=");
            contentBuffer.append(entry.getValue());
        }
        contentBuffer.append("\n");
        propertiesFile.setContents(contentBuffer.toString().getBytes(encoding));
        m_cms.writeFile(propertiesFile);
    }

    /**
     * Adds the given messages to the vfs message bundle.<p>
     *
     * @param messages the messages
     * @param vfsBundleFile the bundle file
     *
     * @throws CmsException if something goes wrong writing the file
     */
    private void addMessagesToVfsBundle(Map<String, String> messages, CmsFile vfsBundleFile) throws CmsException {

        lockTemporary(vfsBundleFile);
        CmsObject cms = m_cms;
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, vfsBundleFile);
        Locale locale = CmsLocaleManager.getDefaultLocale();
        if (!content.hasLocale(locale)) {
            content.addLocale(cms, locale);
        }
        Element root = content.getLocaleNode(locale);
        for (Entry<String, String> entry : messages.entrySet()) {
            Element message = root.addElement(CmsVfsBundleLoaderXml.N_MESSAGE);
            Element key = message.addElement(CmsVfsBundleLoaderXml.N_KEY);
            key.setText(entry.getKey());
            Element value = message.addElement(CmsVfsBundleLoaderXml.N_VALUE);
            value.setText(entry.getValue());
        }
        content.initDocument();
        vfsBundleFile.setContents(content.marshal());
        cms.writeFile(vfsBundleFile);
    }

    /**
     * Adds the given resource to the module if necessary.<p>
     *
     * @param resourcePath to be added
     * @param module module
     */
    private void addResourceToModule(String resourcePath, CmsModule module) {

        List<String> currentRessources = Lists.newArrayList(module.getResources());
        for (String resource : currentRessources) {
            if (resourcePath.startsWith(resource)) {
                return;
            }
        }
        currentRessources.add(resourcePath);
        module.setResources(currentRessources);
    }

    /**
     * Adds the explorer type messages to the modules workplace bundle.<p>
     *
     * @param setting the explorer type settings
     * @param moduleFolder the module folder name
     *
     * @throws CmsException if writing the bundle fails
     * @throws UnsupportedEncodingException in case of encoding issues
     */
    private void addTypeMessages(CmsExplorerTypeSettings setting, String moduleFolder)
    throws CmsException, UnsupportedEncodingException {

        Map<String, String> messages = new HashMap<String, String>();

        // check if any messages to set
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_typeName.getValue())) {
            String key = KEY_PREFIX_NAME + m_typeShortName.getValue();
            messages.put(key, m_typeName.getValue());
            setting.setKey(key);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_typeDescription.getValue())) {
            String key = KEY_PREFIX_DESCRIPTION + m_typeShortName.getValue();
            messages.put(key, m_typeDescription.getValue());
            setting.setInfo(key);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_typeTitle.getValue())) {
            String key = KEY_PREFIX_TITLE + m_typeShortName.getValue();
            messages.put(key, m_typeTitle.getValue());
            String key2 = KEY_PREFIX_FORMATTER.replace("name", m_typeShortName.getValue());
            messages.put(key2, m_typeTitle.getValue());
            setting.setTitleKey(key);
        }

        if (!messages.isEmpty()) {

            //2. check if the bundle exists as XML bundle
            String vfsBundleFileName = CmsStringUtil.joinPaths(
                moduleFolder,
                PATH_I18N,
                m_moduleName.getValue() + SUFFIX_BUNDLE_FILE);
            OpenCms.getLocaleManager();
            if (m_cms.existsResource(vfsBundleFileName)) {
                addMessagesToVfsBundle(messages, m_cms.readFile(vfsBundleFileName));
            } else {
                //3. check if the bundle exists as property bundle
                // we always write to the default locale
                String propertyBundleFileName = vfsBundleFileName + "_" + CmsLocaleManager.getDefaultLocale();
                if (!m_cms.existsResource(propertyBundleFileName)) {
                    //if non of the checked bundles exist, create one.
                    String bundleFolder = CmsStringUtil.joinPaths(moduleFolder, PATH_I18N);
                    if (!m_cms.existsResource(bundleFolder)) {
                        m_cms.createResource(
                            bundleFolder,
                            OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
                    }
                    CmsResource res = m_cms.createResource(
                        propertyBundleFileName,
                        OpenCms.getResourceManager().getResourceType(CmsVfsBundleManager.TYPE_PROPERTIES_BUNDLE),
                        null,
                        null);
                    m_cms.writeResource(res);
                }
                addMessagesToPropertiesFile(messages, m_cms.readFile(propertyBundleFileName), false);
            }

        }
    }

    private void adjustFormatterConfig(String formatterPath, String formatterConfigPath)
    throws CmsException, IOException {

        CmsResource formatter = m_cms.readResource(formatterPath);
        CmsResource config = m_cms.readResource(formatterConfigPath);

        CmsFile file = m_cms.readFile(config);
        String encoding = CmsLocaleManager.getResourceEncoding(m_cms, config);
        String contents = new String(file.getContents(), encoding);
        Pattern patt = Pattern.compile("(?s)<Jsp>.*</Jsp>");
        Matcher m = patt.matcher(contents);
        if (m.find()) {
            System.out.println("found");
        }
        String newContentString = m.replaceAll(
            "<Jsp><link type=\"WEAK\"><target><![CDATA[" + formatter.getRootPath() + "]]></target></link></Jsp>");

        file.setContents(
            Pattern.compile("a-sample").matcher(newContentString).replaceAll(m_typeShortName.getValue()).getBytes());
        CmsLockUtil.ensureLock(m_cms, file);
        m_cms.writeFile(file);
        CmsLockUtil.tryUnlock(m_cms, file);

    }

    private void createResourceType(CmsModule module) {

        CmsProject currentProject = m_cms.getRequestContext().getCurrentProject();
        try {

            CmsProject workProject = m_cms.createProject(
                "Add_resource_type_project",
                "Add resource type project",
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                CmsProject.PROJECT_TYPE_TEMPORARY);
            m_cms.getRequestContext().setCurrentProject(workProject);

            if (!m_cms.existsResource(m_parentSchema.getValue())) {

                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeFolder.RESOURCE_TYPE_NAME);
                CmsResource res = m_cms.createResource(m_parentSchema.getValue(), type);
                m_cms.unlockResource(res);

            }
            if (!m_cms.existsResource(m_parentFormatter.getValue())) {

                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeFolder.RESOURCE_TYPE_NAME);
                CmsResource res = m_cms.createResource(m_parentFormatter.getValue(), type);
                m_cms.unlockResource(res);

            }

            String formatterPath = m_parentFormatter.getValue() + m_typeShortName.getValue() + ".jsp";
            String formatterConfigPath = m_parentFormatter.getValue() + m_typeShortName.getValue() + ".xml";
            String schemaPath = m_parentSchema.getValue() + m_typeShortName.getValue() + ".xsd";
            if (!m_cms.existsResource(formatterPath)) {
                m_cms.copyResource(SAMPLE_FORMATTER, formatterPath);
            }
            if (!m_cms.existsResource(schemaPath)) {
                m_cms.copyResource(SAMPLE_SCHEMA, schemaPath);
            }
            if (!m_cms.existsResource(formatterConfigPath)) {
                m_cms.copyResource(SAMPLE_FORMATTER_CONFIG, formatterConfigPath);
            }
            CmsLockUtil.tryUnlock(m_cms, m_cms.readResource(formatterPath));
            CmsLockUtil.tryUnlock(m_cms, m_cms.readResource(formatterConfigPath));
            CmsLockUtil.tryUnlock(m_cms, m_cms.readResource(schemaPath));
            addResourceToModule(formatterPath, module);
            addResourceToModule(formatterConfigPath, module);
            addResourceToModule(schemaPath, module);
            adjustFormatterConfig(formatterPath, formatterConfigPath);

            List<I_CmsResourceType> types = new ArrayList<I_CmsResourceType>(module.getResourceTypes());
            // create the new resource type
            CmsResourceTypeXmlContent type = new CmsResourceTypeXmlContent();
            type.addConfigurationParameter(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA, schemaPath);
            type.setAdditionalModuleResourceType(true);
            type.setModuleName(module.getName());
            type.initConfiguration(
                m_typeShortName.getValue(),
                m_typeID.getValue(),
                CmsResourceTypeXmlContent.class.getName());
            types.add(type);
            module.setResourceTypes(types);

            List<CmsExplorerTypeSettings> settings = new ArrayList<CmsExplorerTypeSettings>(module.getExplorerTypes());
            // create the matching explorer type
            CmsExplorerTypeSettings setting = new CmsExplorerTypeSettings();
            setting.setTypeAttributes(
                m_typeShortName.getValue(),
                m_typeName.getValue(), //ToDo nicename
                m_iconMode.getValue().equals(ICON_MODE_FILE) ? CmsResource.getName(m_smallIconFile.getValue()) : null,
                m_iconMode.getValue().equals(ICON_MODE_FILE) ? CmsResource.getName(m_bigIconFile.getValue()) : null,
                m_iconMode.getValue().equals(ICON_MODE_CSS) ? m_smallIconCSS.getValue() : null,
                m_iconMode.getValue().equals(ICON_MODE_CSS) ? m_bigIconCSS.getValue() : null,
                "xmlcontent",
                null,
                "false",
                null,
                null);
            setting.setNewResourceUri("newresource_xmlcontent.jsp?newresourcetype=" + m_typeShortName.getValue());
            setting.setNewResourcePage("structurecontent");
            setting.setAutoSetNavigation("false");
            setting.setAutoSetTitle("false");
            setting.setNewResourceOrder("10");
            setting.setAddititionalModuleExplorerType(true);
            addTypeMessages(setting, getMessageParentFolder(m_moduleName.getValue()));
            settings.add(setting);
            module.setExplorerTypes(settings);
            m_cms.unlockProject(workProject.getUuid());
            OpenCms.getPublishManager().publishProject(
                m_cms,
                new CmsLogReport(m_cms.getRequestContext().getLocale(), getClass()));
            OpenCms.getPublishManager().waitWhileRunning();
        } catch (Exception e) {
            LOG.error("Unable to create resource type", e);
        } finally {
            m_cms.getRequestContext().setCurrentProject(currentProject);
        }
    }

    private int getFreeId() {

        int tryID = (int)(10000 + (Math.random() * 90000));
        while (!CmsResourceTypeApp.isResourceTypeIdFree(tryID)) {
            tryID = (int)(10000 + (Math.random() * 90000));
        }
        return tryID;
    }

    private void init(Window window, CmsResourceTypeApp app) {

        CmsObject rootCms = null;
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            rootCms = OpenCms.initCmsObject(m_cms);
            rootCms.getRequestContext().setSiteRoot("");
        } catch (CmsException e1) {
            m_cms = A_CmsUI.getCmsObject();
            rootCms = m_cms;
        }
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        if (m_type == null) {
            m_typeID.addValidator(new IDValidator());
            m_typeShortName.addValidator(new NameValidator());
        } else {
            m_typeID.setEnabled(false);
            m_typeShortName.setEnabled(false);
        }
        m_smallIconFile.setCmsObject(rootCms);
        m_bigIconFile.setCmsObject(rootCms);
        m_smallIconFile.requireFile();
        m_bigIconFile.requireFile();

        m_smallIconFile.setBasePath("/system/workplace/resources/filetypes/");
        m_bigIconFile.setBasePath("/system/workplace/resources/filetypes/");
        m_bigIconFile.setValue("/system/workplace/resources/filetypes/jsp-search_big.png");
        m_smallIconFile.setValue("/system/workplace/resources/filetypes/jsp-search.png");
        m_smallIconCSS.setValue("ap-icon-16-a-contact");
        m_bigIconCSS.setValue("ap-icon-24-a-contact");
        m_iconMode.setValue(ICON_MODE_CSS);
        m_iconMode.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                setVisibilityOfIconFields();

            }

        });

        if (m_type != null) {
            setIconFields(m_type);
            m_chooseModule.setVisible(false);
            m_moduleName.setValue(m_type.getModuleName());
            m_typeID.setValue(String.valueOf(m_type.getTypeId()));
            m_typeShortName.setValue(m_type.getTypeName());
            m_parentFormatter.setVisible(false);
            m_parentSchema.setVisible(false);
            m_typeDescription.setVisible(false);
            m_typeName.setVisible(false);
            m_typeTitle.setVisible(false);
            for (I_CmsResourceType resType : OpenCms.getModuleManager().getModule(
                m_type.getModuleName()).getResourceTypes()) {

            }
            if (m_type instanceof CmsResourceTypeXmlContent) {
                CmsResourceTypeXmlContent typeXML = (CmsResourceTypeXmlContent)m_type;
                m_schema.setValue(typeXML.getSchema());
                m_schema.setEnabled(false);
            } else {
                m_schema.setVisible(false);
            }
            m_ok.setEnabled(true);
        } else {
            m_schema.setVisible(false);
            m_chooseModule.addClickListener(e -> openModuleSelect(window));
            m_typeID.setValue(String.valueOf(getFreeId()));
            m_ok.setEnabled(false);
        }

        m_ok.addClickListener(e -> submit(window, app));
        m_cancel.addClickListener(e -> window.close());

        setVisibilityOfIconFields();

    }

    private boolean isValid() {

        if (m_type != null) {
            return true;
        }
        return m_typeID.isValid()
            && m_typeShortName.isValid()
            && m_parentFormatter.isValid()
            && m_parentSchema.isValid();
    }

    /**
     * Locks the given resource temporarily.<p>
     *
     * @param resource the resource to lock
     *
     * @throws CmsException if locking fails
     */
    private void lockTemporary(CmsResource resource) throws CmsException {

        CmsUser user = m_cms.getRequestContext().getCurrentUser();
        CmsLock lock = m_cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            m_cms.lockResourceTemporary(resource);
        } else if (!lock.isOwnedInProjectBy(user, m_cms.getRequestContext().getCurrentProject())) {
            m_cms.changeLock(resource);
        }
    }

    private void saveResourceType(CmsModule module) {

        List<CmsExplorerTypeSettings> typeSettings = Lists.newArrayList(
            OpenCms.getModuleManager().getModule(m_type.getModuleName()).getExplorerTypes());
        for (CmsExplorerTypeSettings setting : typeSettings) {
            if (!setting.getName().equals(m_type.getTypeName())) {
                continue;
            }
            setting.setBigIcon(
                ICON_MODE_CSS.equals(m_iconMode.getValue())
                ? null
                : CmsStringUtil.isEmptyOrWhitespaceOnly(m_bigIconFile.getValue()) ? null : m_bigIconFile.getValue());
            setting.setIcon(
                ICON_MODE_CSS.equals(m_iconMode.getValue())
                ? null
                : CmsStringUtil.isEmptyOrWhitespaceOnly(m_smallIconFile.getValue())
                ? null
                : m_smallIconFile.getValue());
            setting.setBigIconStyle(
                ICON_MODE_CSS.equals(m_iconMode.getValue())
                ? CmsStringUtil.isEmptyOrWhitespaceOnly(m_bigIconCSS.getValue()) ? null : m_bigIconCSS.getValue()
                : null);
            setting.setSmallIconStyle(
                ICON_MODE_CSS.equals(m_iconMode.getValue())
                ? CmsStringUtil.isEmptyOrWhitespaceOnly(m_smallIconCSS.getValue()) ? null : m_smallIconCSS.getValue()
                : null);
            break;
        }
        module.setExplorerTypes(typeSettings);
        OpenCms.getWorkplaceManager().addExplorerTypeSettings(module);
    }

    private void setIconFields(I_CmsResourceType type) {

        List<CmsExplorerTypeSettings> typeSettings = Lists.newArrayList(
            OpenCms.getModuleManager().getModule(type.getModuleName()).getExplorerTypes());
        for (CmsExplorerTypeSettings setting : typeSettings) {
            if (!setting.getName().equals(type.getTypeName())) {
                continue;
            }
            boolean cssIconStyle = setting.getBigIcon() == null;

            if (cssIconStyle) {
                m_iconMode.setValue(ICON_MODE_CSS);
                if (setting.getBigIconStyle() == null) {
                    m_smallIconCSS.setValue("");
                    m_bigIconCSS.setValue("");
                } else {
                    m_smallIconCSS.setValue(setting.getSmallIconStyle());
                    m_bigIconCSS.setValue(setting.getBigIconStyle());
                }
            } else {
                m_iconMode.setValue(ICON_MODE_FILE);
                m_smallIconFile.setValue(setting.getIcon());
                m_bigIconFile.setValue(setting.getBigIcon());
            }
            break;
        }
    }

}
