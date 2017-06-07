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

package org.opencms.workplace.tools.modules;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.ade.configuration.formatters.CmsFormatterBeanParser;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
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
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.Messages;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.content.CmsVfsBundleLoaderXml;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

/**
 * The report thread to add a new resource type to a module and publish all related resources.<p>
 */
public class CmsModuleAddResourceTypeThread extends A_CmsReportThread {

    /** Message key prefix. */
    private static final String KEY_PREFIX_DESCRIPTION = "desc.";

    /** Message key prefix. */
    private static final String KEY_PREFIX_NAME = "fileicon.";

    /** Message key prefix. */
    private static final String KEY_PREFIX_TITLE = "title.";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleAddResourceTypeThread.class);

    /** Message bundle folder name. */
    private static final String PATH_I18N = "i18n";

    /** Message properties file encoding. */
    private static final String PROPERTIES_ENCODING = "ISO-8859-1";

    /** Message properties file name. */
    private static final String PROPERTIES_FILE_NAME = "workplace.properties";

    /** Sample file. */
    private static final String SAMPLE_FORMATTER = "/system/modules/org.opencms.workplace.tools.modules/samples/sample-formatter.jsp";

    /** Sample file. */
    private static final String SAMPLE_ICON_BIG = "/system/modules/org.opencms.workplace.tools.modules/samples/sample-icon_big.png";

    /** Sample file. */
    private static final String SAMPLE_ICON_SMALL = "/system/modules/org.opencms.workplace.tools.modules/samples/sample-icon.png";

    /** Sample file. */
    private static final String SAMPLE_SCHEMA = "/system/modules/org.opencms.workplace.tools.modules/samples/sample-schema.xsd";

    /** The sample schema type name. */
    private static final String SAMPLE_SCHEMA_TYPE_NAME = "SampleType";

    /** Message bundle file name suffix. */
    private static final String SUFFIX_BUNDLE_FILE = ".workplace";

    /** The resource type information. */
    private CmsResourceTypeInfoBean m_resInfo;

    /**
     * Constructor.<p>
     *
     * @param cms the cms context
     * @param resInfo the resource type information
     */
    protected CmsModuleAddResourceTypeThread(CmsObject cms, CmsResourceTypeInfoBean resInfo) {

        super(cms, resInfo.getName());
        m_resInfo = resInfo;
        initHtmlReport(cms.getRequestContext().getLocale());
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

        CmsObject cms = getCms();
        CmsProject currentProject = cms.getRequestContext().getCurrentProject();
        try {
            CmsProject workProject = cms.createProject(
                "Add_resource_type_project",
                "Add resource type project",
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                CmsProject.PROJECT_TYPE_TEMPORARY);
            cms.getRequestContext().setCurrentProject(workProject);
            CmsModule module = (CmsModule)OpenCms.getModuleManager().getModule(m_resInfo.getModuleName()).clone();
            String moduleFolder = CmsStringUtil.joinPaths("/system/modules/", m_resInfo.getModuleName());
            copySampleFiles(module, moduleFolder);
            List<I_CmsResourceType> types = new ArrayList<I_CmsResourceType>(module.getResourceTypes());
            // create the new resource type
            CmsResourceTypeXmlContent type = new CmsResourceTypeXmlContent();
            type.addConfigurationParameter(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA, m_resInfo.getSchema());
            type.setAdditionalModuleResourceType(true);
            type.setModuleName(m_resInfo.getModuleName());
            type.initConfiguration(
                m_resInfo.getName(),
                String.valueOf(m_resInfo.getId()),
                CmsResourceTypeXmlContent.class.getName());
            types.add(type);
            module.setResourceTypes(types);

            List<CmsExplorerTypeSettings> settings = new ArrayList<CmsExplorerTypeSettings>(module.getExplorerTypes());
            // create the matching explorer type
            CmsExplorerTypeSettings setting = new CmsExplorerTypeSettings();
            setting.setTypeAttributes(
                m_resInfo.getName(),
                m_resInfo.getNiceName(),
                m_resInfo.getSmallIcon(),
                m_resInfo.getBigIcon(),
                "xmlcontent",
                null,
                "false",
                null,
                null);
            setting.setNewResourceUri("newresource_xmlcontent.jsp?newresourcetype=" + m_resInfo.getName());
            setting.setNewResourcePage("structurecontent");
            setting.setAutoSetNavigation("false");
            setting.setAutoSetTitle("false");
            setting.setNewResourceOrder("10");
            setting.setAddititionalModuleExplorerType(true);
            addTypeMessages(setting, moduleFolder);
            settings.add(setting);
            module.setExplorerTypes(settings);
            createSampleFormatter(moduleFolder);
            // now unlock and publish the project
            getReport().println(
                Messages.get().container(Messages.RPT_PUBLISH_PROJECT_BEGIN_0),
                I_CmsReport.FORMAT_HEADLINE);
            cms.unlockProject(workProject.getUuid());
            OpenCms.getPublishManager().publishProject(cms, getReport());
            OpenCms.getPublishManager().waitWhileRunning();

            getReport().println(
                Messages.get().container(Messages.RPT_PUBLISH_PROJECT_END_0),
                I_CmsReport.FORMAT_HEADLINE);

            // write the module configuration, init resource types, explorer types and clear OpenCms caches
            OpenCms.getModuleManager().updateModule(cms, module);
            OpenCms.getResourceManager().initialize(cms);
            OpenCms.getWorkplaceManager().addExplorerTypeSettings(module);
            OpenCms.fireCmsEvent(
                new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.<String, Object> emptyMap()));
            // re-initialize the workplace
            OpenCms.getWorkplaceManager().initialize(getCms());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            getReport().addError(e);
        } finally {
            cms.getRequestContext().setCurrentProject(currentProject);
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
        : CmsFileUtil.getEncoding(getCms(), propertiesFile);
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
        getCms().writeFile(propertiesFile);
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
        CmsObject cms = getCms();
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
        CmsObject cms = getCms();
        // check if any messages to set
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getNiceName())) {
            String key = KEY_PREFIX_NAME + m_resInfo.getName();
            messages.put(key, m_resInfo.getNiceName());
            setting.setKey(key);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getDescription())) {
            String key = KEY_PREFIX_DESCRIPTION + m_resInfo.getName();
            messages.put(key, m_resInfo.getDescription());
            setting.setInfo(key);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getTitle())) {
            String key = KEY_PREFIX_TITLE + m_resInfo.getName();
            messages.put(key, m_resInfo.getTitle());
            setting.setTitleKey(key);
        }
        if (!messages.isEmpty()) {
            // add the messages to the module's workplace bundle

            //1. check if the bundle exists as raw Java bundle
            String workplacePropertiesFile = CmsStringUtil.joinPaths(
                moduleFolder,
                CmsModulesEditBase.PATH_CLASSES,
                m_resInfo.getModuleName().replace(".", "/"),
                PROPERTIES_FILE_NAME);
            if (cms.existsResource(workplacePropertiesFile)) {
                addMessagesToPropertiesFile(messages, cms.readFile(workplacePropertiesFile), true);
            } else {
                //2. check if the bundle exists as XML bundle
                String vfsBundleFileName = CmsStringUtil.joinPaths(
                    moduleFolder,
                    PATH_I18N,
                    m_resInfo.getModuleName() + SUFFIX_BUNDLE_FILE);
                OpenCms.getLocaleManager();
                if (cms.existsResource(vfsBundleFileName)) {
                    addMessagesToVfsBundle(messages, cms.readFile(vfsBundleFileName));
                } else {
                    //3. check if the bundle exists as property bundle
                    // we always write to the default locale
                    String propertyBundleFileName = vfsBundleFileName + "_" + CmsLocaleManager.getDefaultLocale();
                    if (!cms.existsResource(propertyBundleFileName)) {
                        //if non of the checked bundles exist, create one.
                        String bundleFolder = CmsStringUtil.joinPaths(moduleFolder, PATH_I18N);
                        if (!cms.existsResource(bundleFolder)) {
                            cms.createResource(
                                bundleFolder,
                                OpenCms.getResourceManager().getResourceType(
                                    CmsResourceTypeFolder.getStaticTypeName()));
                        }
                        CmsResource res = cms.createResource(
                            propertyBundleFileName,
                            OpenCms.getResourceManager().getResourceType(CmsVfsBundleManager.TYPE_PROPERTIES_BUNDLE),
                            null,
                            null);
                        cms.writeResource(res);
                    }
                    addMessagesToPropertiesFile(messages, cms.readFile(propertyBundleFileName), false);
                }

            }
        }
    }

    /**
     * Copies sample schema and resource type icons and adds the resources to the module.<p>
     *
     * @param module the module
     * @param moduleFolder the module folder name
     *
     * @throws CmsIllegalArgumentException in case something goes wrong copying the resources
     * @throws CmsException in case something goes wrong copying the resources
     */
    private void copySampleFiles(CmsModule module, String moduleFolder)
    throws CmsIllegalArgumentException, CmsException {

        CmsObject cms = getCms();
        List<String> moduleResource = new ArrayList<String>(module.getResources());
        if (!cms.existsResource(moduleFolder)) {
            cms.createResource(moduleFolder, CmsResourceTypeFolder.getStaticTypeId());
            moduleResource.add(moduleFolder);
        }
        String schemaFolder = CmsStringUtil.joinPaths(moduleFolder, "schemas");
        if (!cms.existsResource(schemaFolder)) {
            cms.createResource(schemaFolder, CmsResourceTypeFolder.getStaticTypeId());
        }
        String schemaFile = CmsStringUtil.joinPaths(schemaFolder, m_resInfo.getName() + ".xsd");
        if (!cms.existsResource(schemaFile)) {
            cms.copyResource(SAMPLE_SCHEMA, schemaFile, CmsResource.COPY_AS_NEW);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getSchemaTypeName())) {
                // replace the sample schema type name with the provided name
                try {
                    CmsFile schema = cms.readFile(schemaFile);
                    OpenCms.getLocaleManager();
                    String schemaContent = new String(
                        schema.getContents(),
                        CmsLocaleManager.getResourceEncoding(cms, schema));
                    schemaContent = schemaContent.replaceAll(SAMPLE_SCHEMA_TYPE_NAME, m_resInfo.getSchemaTypeName());
                    schema.setContents(schemaContent.getBytes());
                    cms.writeFile(schema);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    getReport().addError(e);
                }
            }

        }
        m_resInfo.setSchema(schemaFile);
        String filetypesFolder = "/system/workplace/resources/filetypes/";
        String smallIcon = CmsStringUtil.joinPaths(filetypesFolder, m_resInfo.getName() + ".png");
        if (!cms.existsResource(smallIcon)) {
            cms.copyResource(SAMPLE_ICON_SMALL, smallIcon, CmsResource.COPY_AS_NEW);
            moduleResource.add(smallIcon);
        }
        m_resInfo.setSmallIcon(m_resInfo.getName() + ".png");
        String bigIcon = CmsStringUtil.joinPaths(filetypesFolder, m_resInfo.getName() + "_big.png");
        if (!cms.existsResource(bigIcon)) {
            cms.copyResource(SAMPLE_ICON_BIG, bigIcon, CmsResource.COPY_AS_NEW);
            moduleResource.add(bigIcon);
        }
        m_resInfo.setBigIcon(m_resInfo.getName() + "_big.png");
        module.setResources(moduleResource);
    }

    /**
     * Copies the sample formatter JSP, creates the associated formatter and module configuration.<p>
     *
     * @param moduleFolder the module folder name
     *
     * @throws CmsIllegalArgumentException in case something goes wrong copying the resources
     * @throws CmsException in case something goes wrong copying the resources
     */
    private void createSampleFormatter(String moduleFolder) throws CmsIllegalArgumentException, CmsException {

        CmsObject cms = getCms();
        String formatterFolder = CmsStringUtil.joinPaths(moduleFolder, CmsModulesEditBase.PATH_FORMATTERS);
        if (!cms.existsResource(formatterFolder)) {
            cms.createResource(formatterFolder, CmsResourceTypeFolder.getStaticTypeId());
        }
        String formatterJSP = CmsStringUtil.joinPaths(formatterFolder, m_resInfo.getName() + "-formatter.jsp");
        if (!cms.existsResource(formatterJSP)) {
            cms.copyResource(SAMPLE_FORMATTER, formatterJSP, CmsResource.COPY_AS_NEW);
        }
        String formatterConfig = CmsStringUtil.joinPaths(
            formatterFolder,
            m_resInfo.getName() + "-formatter-config.xml");
        if (!cms.existsResource(formatterConfig)) {

            cms.createResource(
                formatterConfig,
                OpenCms.getResourceManager().getResourceType(
                    CmsFormatterConfigurationCache.TYPE_FORMATTER_CONFIG).getTypeId());
            CmsFile configFile = cms.readFile(formatterConfig);
            CmsXmlContent configContent = CmsXmlContentFactory.unmarshal(cms, configFile);
            if (!configContent.hasLocale(CmsConfigurationReader.DEFAULT_LOCALE)) {
                configContent.addLocale(cms, CmsConfigurationReader.DEFAULT_LOCALE);
            }
            I_CmsXmlContentValue typeValue = configContent.getValue(
                CmsFormatterBeanParser.N_TYPE,
                CmsConfigurationReader.DEFAULT_LOCALE);
            typeValue.setStringValue(cms, m_resInfo.getName());
            I_CmsXmlContentValue formatterValue = configContent.getValue(
                CmsFormatterBeanParser.N_JSP,
                CmsConfigurationReader.DEFAULT_LOCALE);
            formatterValue.setStringValue(cms, formatterJSP);
            I_CmsXmlContentValue formatterNameValue = configContent.getValue(
                CmsFormatterBeanParser.N_NICE_NAME,
                CmsConfigurationReader.DEFAULT_LOCALE);
            formatterNameValue.setStringValue(
                cms,
                "Sample formatter for "
                    + (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getNiceName())
                    ? m_resInfo.getNiceName()
                    : m_resInfo.getName()));
            // set matching container width to '-1' to fit everywhere
            configContent.addValue(cms, CmsFormatterBeanParser.N_MATCH, CmsConfigurationReader.DEFAULT_LOCALE, 0);
            configContent.addValue(
                cms,
                CmsFormatterBeanParser.N_MATCH + "/" + CmsFormatterBeanParser.N_WIDTH,
                CmsConfigurationReader.DEFAULT_LOCALE,
                0);
            I_CmsXmlContentValue widthValue = configContent.getValue(
                CmsFormatterBeanParser.N_MATCH
                    + "/"
                    + CmsFormatterBeanParser.N_WIDTH
                    + "/"
                    + CmsFormatterBeanParser.N_WIDTH,
                CmsConfigurationReader.DEFAULT_LOCALE);
            widthValue.setStringValue(cms, "-1");

            // enable the formatter
            I_CmsXmlContentValue enabledValue = configContent.getValue(
                CmsFormatterBeanParser.N_AUTO_ENABLED,
                CmsConfigurationReader.DEFAULT_LOCALE);
            enabledValue.setStringValue(cms, Boolean.TRUE.toString());
            configFile.setContents(configContent.marshal());
            cms.writeFile(configFile);
        }
        String moduleConfig = CmsStringUtil.joinPaths(moduleFolder, ".config");
        if (!cms.existsResource(moduleConfig)) {
            cms.createResource(
                moduleConfig,
                OpenCms.getResourceManager().getResourceType(CmsADEManager.MODULE_CONFIG_TYPE).getTypeId());
        }
        CmsFile moduleConfigFile = cms.readFile(moduleConfig);
        lockTemporary(moduleConfigFile);
        CmsXmlContent moduleConfigContent = CmsXmlContentFactory.unmarshal(cms, moduleConfigFile);
        I_CmsXmlContentValue resourceTypeValue = moduleConfigContent.addValue(
            cms,
            CmsConfigurationReader.N_RESOURCE_TYPE,
            CmsConfigurationReader.DEFAULT_LOCALE,
            0);
        I_CmsXmlContentValue typeValue = moduleConfigContent.getValue(
            resourceTypeValue.getPath() + "/" + CmsConfigurationReader.N_TYPE_NAME,
            CmsConfigurationReader.DEFAULT_LOCALE);
        typeValue.setStringValue(cms, m_resInfo.getName());
        moduleConfigFile.setContents(moduleConfigContent.marshal());
        cms.writeFile(moduleConfigFile);
    }

    /**
     * Locks the given resource temporarily.<p>
     *
     * @param resource the resource to lock
     *
     * @throws CmsException if locking fails
     */
    private void lockTemporary(CmsResource resource) throws CmsException {

        CmsObject cms = getCms();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsLock lock = cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            cms.lockResourceTemporary(resource);
        } else if (!lock.isOwnedInProjectBy(user, cms.getRequestContext().getCurrentProject())) {
            cms.changeLock(resource);
        }
    }
}
