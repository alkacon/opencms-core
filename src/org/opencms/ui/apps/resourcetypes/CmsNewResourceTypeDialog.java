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

import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
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
import org.opencms.ui.apps.modules.CmsModuleApp;
import org.opencms.ui.apps.modules.edit.CmsEditModuleForm;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsVfsBundleLoaderXml;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

import com.google.common.collect.Lists;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;

/**
 * Dialog to edit or create resourcetypes.<p>
 */
@SuppressWarnings("deprecation")
public class CmsNewResourceTypeDialog extends CmsBasicDialog {

    /**
     * XPath elements.
     */
    public static class XMLPath {

        /**Module Config Constant.  */
        private static final String CONFIG_RESOURCETYPE = "ResourceType";

        /**Module Config Constant.  */
        private static final String CONFIG_RESOURCETYPE_TYPENAME = "/TypeName";

        /**Module Config Constant.  */
        private static final String CONFIG_RESOURCETYPE_NAMEPATTERN = "/NamePattern";

        /**
         * Adds the count to the path, e.g., <code>"Title" + num(2)</code> results in <code>"Title[2]"</code>.
         * @param i the count to add to the XPath
         * @return the argument wrapped in square brackets.
         */
        public static String num(int i) {

            return "[" + i + "]";
        }
    }

    /**
     * Validator for the bundle resource field.<p>
     */
    class BundleValidator implements Validator {

        /**Vaadin serial id. */
        private static final long serialVersionUID = 7872665683495080792L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (!m_cms.existsResource((String)value)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_RESORUCE_0));
            }

            try {
                CmsResource res = m_cms.readResource((String)value);
                if (!OpenCms.getResourceManager().getResourceType(res).equals(
                    OpenCms.getResourceManager().getResourceType("propertyvfsbundle"))) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_RESORUCE_NO_VFSBUNDLE_0));

                }
            } catch (CmsException e) {
                LOG.error("Unable to read resource", e);
            }

        }

    }

    /**
     * Validator for the title field.<p>
     */

    class IDValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7878441125879949490L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
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
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
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
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if ((value == null) || ((String)value).isEmpty()) {
                return;
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

    /**Serial id.*/
    private static final long serialVersionUID = 8775619886579477116L;

    /** Message bundle folder name. */
    protected static final String PATH_I18N = "i18n";

    /** Formatter folder name.*/
    private static final String FORMATTER = "formatters/";

    /**Message key schema. */
    private static final String MESSAGE_KEY_FORMATTER = "type.%s.%s";

    /**key: name.*/
    private static final String MESSAGE_KEY_FORMATTER_NAME = "name";

    /**key: description. */
    private static final String MESSAGE_KEY_FORMATTER_DESCRIPTION = "description";

    /**key: title. */
    private static final String MESSAGE_KEY_FORMATTER_TITLE = "title";

    /**key: formatter. */
    private static final String MESSAGE_KEY_FORMATTER_FORMATTER = "formatter";

    /** Logger instance for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsNewResourceTypeDialog.class);

    /**Sample path. */
    private static final String PATH_SAMPLE = "/system/modules/org.opencms.base/copyresources/";

    /** Message properties file encoding. */
    private static final String PROPERTIES_ENCODING = "ISO-8859-1";

    /** Sample formatter. */
    private static final String SAMPLE_FORMATTER = PATH_SAMPLE + "sample-formatter.jsp";

    /** Message bundle file name suffix. */
    private static final String SUFFIX_BUNDLE_FILE = ".messages";

    /** Sample schema. */
    private static final String SAMPLE_SCHEMA = PATH_SAMPLE + "sample-schema.xsd";

    /**Sample schema element type. */
    private static final String SAMPLE_TYPE_SCHEMA_ELEMENT = "SampleType";

    /** Schema folder name.*/
    private static final String SCHEMA = "schemas/";

    /** Default small icon.*/
    static final String ICON_SMALL_DEFAULT = "oc-icon-16-default";

    /** Default big icon.*/
    static final String ICON_BIG_DEFAULT = "oc-icon-24-default";

    /**CmsObject. */
    CmsObject m_cms;

    /** vaadin component.*/
    private Button m_cancel;

    /** vaadin component.*/
    private Button m_ok;

    /**vaadin component. */
    private CmsPathSelectField m_parentFormatter;

    /** vaadin component.*/
    private CmsPathSelectField m_parentSchema;

    /** vaadin component.*/
    private TextArea m_typeDescription;

    /** vaadin component.*/
    private TextField m_typeID;

    /** vaadin component.*/
    private TextField m_typeName;

    /** vaadin component.*/
    private TextField m_typeShortName;

    /** vaadin component.*/
    private TextField m_typeXPathName;

    /** Module of the new resource type.*/
    private CmsModule m_module;

    /** Vaadin component.*/
    private CmsPathSelectField m_bundle;

    /** Vaadin component.*/
    private CmsPathSelectField m_config;

    /**
     * Public cosntructor.<p>
     *
     * @param window window
     * @param app app
     */
    public CmsNewResourceTypeDialog(final Window window, CmsResourceTypeApp app) {

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
     * Creates the parents of nested XML elements if necessary.
     * @param xmlContent the XML content that is edited.
     * @param xmlPath the path of the (nested) element, for which the parents should be created
     * @param l the locale for which the XML content is edited.
     */
    protected void createParentXmlElements(CmsXmlContent xmlContent, String xmlPath, Locale l) {

        if (CmsXmlUtils.isDeepXpath(xmlPath)) {
            String parentPath = CmsXmlUtils.removeLastXpathElement(xmlPath);
            if (null == xmlContent.getValue(parentPath, l)) {
                createParentXmlElements(xmlContent, parentPath, l);
                xmlContent.addValue(m_cms, parentPath, l, CmsXmlUtils.getXpathIndexInt(parentPath) - 1);
            }
        }

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

        Window window = CmsVaadinUtils.getWindow(dialog);
        window.setContent(this);
        m_module = OpenCms.getModuleManager().getModule(moduleName).clone();
        CmsResourceInfo resInfo = new CmsResourceInfo(
            m_module.getName(),
            m_module.getNiceName(),
            CmsModuleApp.Icons.RESINFO_ICON);
        displayResourceInfoDirectly(Arrays.asList(resInfo));
        fillFields();
    }

    /**
     * Submit the entered data.<p>
     *
     * @param window window
     * @param app app
     */
    protected void submit(Window window, CmsResourceTypeApp app) {

        if (isValid()) {
            createResourceType();
            try {
                OpenCms.getModuleManager().updateModule(m_cms, m_module);
                OpenCms.getResourceManager().initialize(m_cms);
                OpenCms.getWorkplaceManager().addExplorerTypeSettings(m_module);
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
        contentBuffer.append(new String(propertiesFile.getContents(), encoding).trim());
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

        Map<String, String> messages = new TreeMap<String, String>();

        // check if any messages to set
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_typeName.getValue())) {
            String key = String.format(MESSAGE_KEY_FORMATTER, m_typeShortName.getValue(), MESSAGE_KEY_FORMATTER_NAME);
            messages.put(key, m_typeName.getValue());
            setting.setKey(key);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_typeDescription.getValue())) {
            String key = String.format(
                MESSAGE_KEY_FORMATTER,
                m_typeShortName.getValue(),
                MESSAGE_KEY_FORMATTER_DESCRIPTION);
            messages.put(key, m_typeDescription.getValue());
            setting.setInfo(key);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_typeName.getValue())) {
            String key = String.format(MESSAGE_KEY_FORMATTER, m_typeShortName.getValue(), MESSAGE_KEY_FORMATTER_TITLE);
            messages.put(key, m_typeName.getValue());
            String key2 = String.format(
                MESSAGE_KEY_FORMATTER,
                m_typeShortName.getValue(),
                MESSAGE_KEY_FORMATTER_FORMATTER);
            messages.put(key2, m_typeName.getValue());
            setting.setTitleKey(key);
        }

        if (!messages.isEmpty()) {
            addMessagesToPropertiesFile(messages, m_cms.readFile(m_bundle.getValue()), false);
        }
    }

    /**
     *
     * Adjustes formatter config.<p>
     *
     * @param formatterPath path of formatter
     * @param formatterConfigPath config path
     * @throws CmsException exception
     */

    private void adjustFormatterConfig(String formatterPath, String formatterConfigPath) throws CmsException {

        CmsResource config = m_cms.readResource(formatterConfigPath);

        CmsFile file = m_cms.readFile(config);

        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(m_cms, file);
        Locale l = new Locale("en");
        if (!xmlContent.hasLocale(l)) {
            xmlContent.addLocale(m_cms, l);
        }
        CmsResource formatter = m_cms.readResource(formatterPath);

        I_CmsXmlContentValue v = xmlContent.getValue("Jsp", l);
        v.setStringValue(m_cms, formatter.getRootPath());

        String xmlPath = "NiceName";
        v = xmlContent.getValue(xmlPath, l);
        v.setStringValue(m_cms, m_typeName.getValue());

        xmlPath = "Type";
        v = xmlContent.getValue(xmlPath, l);
        v.setStringValue(m_cms, m_typeShortName.getValue());

        xmlPath = "AutoEnabled";
        v = xmlContent.getValue(xmlPath, l);
        v.setStringValue(m_cms, "true");

        xmlPath = "Match/Width/Width";
        createParentXmlElements(xmlContent, xmlPath, l);
        v = xmlContent.getValue(xmlPath, l);
        v.setStringValue(m_cms, "-1");

        file.setContents(xmlContent.marshal());
        CmsLockUtil.ensureLock(m_cms, file);
        m_cms.writeFile(file);
        CmsLockUtil.tryUnlock(m_cms, file);

    }

    /**
     * Adjustes module config.<p>
     */
    private void adjustModuleConfig() {

        Locale l = CmsLocaleManager.getLocale("en");
        try {
            CmsResource config = m_cms.readResource(m_config.getValue());
            CmsFile configFile = m_cms.readFile(config);
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(m_cms, configFile);
            int number = xmlContent.getIndexCount(XMLPath.CONFIG_RESOURCETYPE, l) + 1;
            createParentXmlElements(
                xmlContent,
                XMLPath.CONFIG_RESOURCETYPE + XMLPath.num(number) + XMLPath.CONFIG_RESOURCETYPE_TYPENAME,
                l);
            I_CmsXmlContentValue v = xmlContent.getValue(
                XMLPath.CONFIG_RESOURCETYPE + XMLPath.num(number) + XMLPath.CONFIG_RESOURCETYPE_TYPENAME,
                l);
            v.setStringValue(m_cms, m_typeShortName.getValue());
            v = xmlContent.addValue(
                m_cms,
                XMLPath.CONFIG_RESOURCETYPE + XMLPath.num(number) + XMLPath.CONFIG_RESOURCETYPE_NAMEPATTERN,
                l,
                CmsXmlUtils.getXpathIndexInt(
                    XMLPath.CONFIG_RESOURCETYPE + XMLPath.num(number) + XMLPath.CONFIG_RESOURCETYPE_NAMEPATTERN) - 1);
            v.setStringValue(m_cms, getNamePattern());
            configFile.setContents(xmlContent.marshal());

            CmsLockUtil.ensureLock(m_cms, configFile);
            m_cms.writeFile(configFile);
            CmsLockUtil.tryUnlock(m_cms, configFile);
        } catch (CmsException e) {
            LOG.error("Can't read module config resource", e);
        }
    }

    /**
     * Adjustes schema.<p>
     *
     * @param schemaPath path to schema resource
     * @param newElementString new Element name
     */
    private void adjustSchema(String schemaPath, String newElementString) {

        newElementString = newElementString.substring(0, 1).toUpperCase() + newElementString.substring(1);
        try {
            CmsFile file = m_cms.readFile(schemaPath);

            CmsMacroResolver macroResolver = new CmsMacroResolver();
            macroResolver.setKeepEmptyMacros(true);

            macroResolver.addMacro(SAMPLE_TYPE_SCHEMA_ELEMENT, newElementString);
            String bundleName = m_bundle.getValue();

            bundleName = bundleName.split("/")[bundleName.split("/").length - 1];
            if (bundleName.contains("_")) {
                bundleName = bundleName.split("_")[0];
            }
            macroResolver.addMacro("ResourceBundle", bundleName);
            macroResolver.addMacro("typeName", m_typeShortName.getValue());
            String encoding = m_cms.readPropertyObject(
                file,
                CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());
            String newContent = macroResolver.resolveMacros(new String(file.getContents(), encoding));
            // update the content
            try {
                file.setContents(newContent.getBytes(encoding));
            } catch (UnsupportedEncodingException e) {
                try {
                    file.setContents(newContent.getBytes(Charset.defaultCharset().toString()));
                } catch (UnsupportedEncodingException e1) {
                    file.setContents(newContent.getBytes());
                }
            }
            // write the target file
            CmsLockUtil.ensureLock(m_cms, file);
            m_cms.writeFile(file);
            CmsLockUtil.tryUnlock(m_cms, file);
        } catch (

        CmsException e) {
            LOG.error("Unable to read schema definition", e);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to fetch encoding", e);
        }
    }

    /**
     * Creates the new resource type.<p>
     */
    private void createResourceType() {

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
                I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType(
                    CmsFormatterConfigurationCache.TYPE_FORMATTER_CONFIG);
                List<CmsProperty> props = new ArrayList<CmsProperty>();
                props.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, "en", null));
                props.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_AVAILABLE_LOCALES, "en", null));

                m_cms.createResource(formatterConfigPath, configType, null, props);
            }
            CmsLockUtil.tryUnlock(m_cms, m_cms.readResource(formatterPath));
            CmsLockUtil.tryUnlock(m_cms, m_cms.readResource(formatterConfigPath));
            CmsLockUtil.tryUnlock(m_cms, m_cms.readResource(schemaPath));
            addResourceToModule(formatterPath, m_module);
            addResourceToModule(formatterConfigPath, m_module);
            addResourceToModule(schemaPath, m_module);
            adjustFormatterConfig(formatterPath, formatterConfigPath);
            adjustSchema(schemaPath, m_typeXPathName.getValue());
            adjustModuleConfig();

            List<I_CmsResourceType> types = new ArrayList<I_CmsResourceType>(m_module.getResourceTypes());
            // create the new resource type
            CmsResourceTypeXmlContent type = new CmsResourceTypeXmlContent();
            type.addConfigurationParameter(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA, schemaPath);
            type.setAdditionalModuleResourceType(true);
            type.setModuleName(m_module.getName());
            type.initConfiguration(
                m_typeShortName.getValue(),
                m_typeID.getValue(),
                CmsResourceTypeXmlContent.class.getName());
            types.add(type);
            m_module.setResourceTypes(types);

            List<CmsExplorerTypeSettings> settings = new ArrayList<CmsExplorerTypeSettings>(
                m_module.getExplorerTypes());
            // create the matching explorer type
            CmsExplorerTypeSettings setting = new CmsExplorerTypeSettings();
            setting.setTypeAttributes(
                m_typeShortName.getValue(),
                m_typeName.getValue(), //ToDo nicename
                null,
                null,
                ICON_SMALL_DEFAULT,
                ICON_BIG_DEFAULT,
                CmsResourceTypeXmlContent.getStaticTypeName(),
                null,
                "false",
                null,
                null);
            setting.setAutoSetNavigation("false");
            setting.setAutoSetTitle("false");
            setting.setNewResourceOrder("10");
            setting.setAddititionalModuleExplorerType(true);
            addTypeMessages(setting, getMessageParentFolder(m_module.getName()));
            settings.add(setting);
            m_module.setExplorerTypes(settings);
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

    /**
     * Fills the fields.<p>
     */
    private void fillFields() {

        String folderName = getModuleFolder(m_module.getName());
        if (folderName != null) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_parentFormatter.getValue())) {
                m_parentFormatter.setValue(folderName + FORMATTER);
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_parentSchema.getValue())) {
                m_parentSchema.setValue(folderName + SCHEMA);
            }
        }
        if (m_cms.existsResource(folderName + CmsEditModuleForm.CONFIG_FILE)) {
            m_config.setValue(folderName + CmsEditModuleForm.CONFIG_FILE);
        }
        m_parentFormatter.removeAllValidators();
        m_parentSchema.removeAllValidators();
        m_config.removeAllValidators();
        m_config.addValidator(new ResourceValidator());
        m_bundle.addValidator(new BundleValidator());
        m_parentFormatter.addValidator(new ResourceValidator());
        m_parentSchema.addValidator(new ResourceValidator());
        CmsResource bundle = getMessageBundle();
        if (bundle != null) {
            m_bundle.setValue(bundle.getRootPath());

        }
    }

    /**
     * Returns the property string for all available locales.<p>
     *
     * @return String to use in properties
     */
    private String getAvailableLocalString() {

        String res = "";

        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            res += locale.toString() + ",";
        }
        if (res.endsWith(",")) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }

    /**
     * Returns a random valid resource type id.<p>
     *
     * @return valid id
     */
    private int getFreeId() {

        int tryID = (int)(10000 + (Math.random() * 90000));
        while (!CmsResourceTypeApp.isResourceTypeIdFree(tryID)) {
            tryID = (int)(10000 + (Math.random() * 90000));
        }
        return tryID;
    }

    /**
     * Gets the message bundle.<p>
     * @return Message bundle resource
     */
    private CmsResource getMessageBundle() {

        OpenCms.getLocaleManager();
        String localString = CmsLocaleManager.getDefaultLocale().toString();
        List<String> moduleResource = m_module.getResources();
        for (String resourcePath : moduleResource) {
            if (resourcePath.contains(PATH_I18N) && resourcePath.endsWith(localString)) {
                try {
                    return m_cms.readResource(resourcePath);
                } catch (CmsException e) {
                    LOG.error("Can not read message bundle", e);
                }
            }
        }
        String moduleFolder = getModuleFolder(m_module.getName());
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(moduleFolder)) {
            return null;
        }
        String bundlePath = CmsStringUtil.joinPaths(
            moduleFolder,
            PATH_I18N,
            m_module.getName() + SUFFIX_BUNDLE_FILE + "_" + localString);
        if (m_cms.existsResource(bundlePath)) {
            try {
                return m_cms.readResource(bundlePath);
            } catch (CmsException e) {
                LOG.error("No bundle found for module", e);
            }
        }
        return null;
    }

    /**
     * Creates a name pattern for the resource type.<p>
     *
     * @return String name-pattern
     */
    private String getNamePattern() {

        String niceName = m_typeShortName.getValue();
        if (m_typeShortName.getValue().contains("-")) {
            int maxLength = 0;
            String[] nameParts = niceName.split("-");
            for (int i = 0; i < nameParts.length; i++) {
                if (nameParts[i].length() > maxLength) {
                    maxLength = nameParts[i].length();
                    niceName = nameParts[i];
                }
            }
        }
        return niceName + "_%(number).xml";
    }

    /**
     * Initializes the form.<p>
     *
     * @param window Window
     * @param app app
     */
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

        m_typeID.addValidator(new IDValidator());
        m_typeShortName.addValidator(new NameValidator());

        m_parentSchema.setCmsObject(rootCms);
        m_parentFormatter.setCmsObject(rootCms);
        m_bundle.setCmsObject(rootCms);
        m_config.setCmsObject(rootCms);

        m_parentSchema.setRequired(true);
        m_parentFormatter.setRequired(true);
        m_typeID.setRequired(true);
        m_typeDescription.setRequired(true);
        m_typeShortName.setRequired(true);
        m_typeXPathName.setRequired(true);
        m_bundle.setRequired(true);

        m_typeName.setRequired(true);

        m_parentSchema.setRequiredError(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_NOT_EMPTY_0));
        m_parentFormatter.setRequiredError(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_NOT_EMPTY_0));
        m_typeID.setRequiredError(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_NOT_EMPTY_0));
        m_typeDescription.setRequiredError(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_NOT_EMPTY_0));
        m_typeShortName.setRequiredError(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_NOT_EMPTY_0));
        m_typeXPathName.setRequiredError(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_NOT_EMPTY_0));
        m_bundle.setRequiredError(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_NOT_EMPTY_0));

        m_typeName.setRequiredError(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_NOT_EMPTY_0));

        m_typeID.setValue(String.valueOf(getFreeId()));
        m_ok.addClickListener(e -> submit(window, app));
        m_cancel.addClickListener(e -> window.close());

    }

    /**
     * Checks if form is valid.<p>
     *
     * @return true if form is valid
     */
    private boolean isValid() {

        return m_typeID.isValid()
            && m_typeShortName.isValid()
            && m_parentFormatter.isValid()
            && m_parentSchema.isValid()
            && m_bundle.isValid()
            && m_config.isValid()
            && m_typeDescription.isValid()
            && m_typeName.isValid()
            && m_typeXPathName.isValid();
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
}
