/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.editors.messagebundle;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

/**
 * The class contains the logic behind the message translation editor.
 * In particular it reads / writes the involved files and provides the contents as {@link IndexedContainer}.
 */
public class CmsMessageBundleEditorModel {

    /** Types of bundles editable by the Editor. */
    public enum BundleType {
        /** A bundle of type propertyvfsbundle. */
        PROPERTY, /** A bundle of type xmlvfsbundle. */
        XML, /** A bundle descriptor. */
        DESCRIPTOR;

        /**
         * An adjusted version of what is typically Enum.valueOf().
         * @param value the resource type name that should be transformed into BundleType
         * @return The bundle type for the resource type name, or null, if the resource has no bundle type.
         */
        public static BundleType toBundleType(String value) {

            if (null == value) {
                return null;
            }
            if (value.equals(PROPERTY.toString())) {
                return PROPERTY;
            }
            if (value.equals(XML.toString())) {
                return XML;
            }
            if (value.equals(DESCRIPTOR.toString())) {
                return DESCRIPTOR;
            }

            return null;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            switch (this) {
                case PROPERTY:
                    return "propertyvfsbundle";
                case XML:
                    return "xmlvfsbundle";
                case DESCRIPTOR:
                    return "bundledescriptor";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    /** Property name for the container column with the keys. */
    public static final String PROPERTY_ID_KEY = "key";
    /** Property name for the container column with the descriptions. */
    public static final String PROPERTY_ID_DESC = "description";
    /** Property name for the container column with the default values. */
    public static final String PROPERTY_ID_DEFAULT = "default";
    /** Property name for the container column with the translations. */
    public static final String PROPERTY_ID_TRANSLATION = "translation";
    /** Post fix for bundle descriptors, which must obey the name scheme {basename}_desc. */
    private static final String DESCRIPTOR_POSTFIX = "_desc";
    /** CmsObject for read / write operations. */
    private CmsObject m_cms;
    /** The file currently edited. */
    private Map<Locale, CmsFile> m_bundleFiles;
    /** The resource that was opened with the editor. */
    private CmsResource m_resource;
    /** The bundle descriptor resource. */
    private CmsResource m_desc;
    /** The bundle descriptor as unmarshalled XML Content. */
    private CmsXmlContent m_descContent;
    /** The xml bundle edited (or null, if a property bundle is edited). */
    private CmsXmlContent m_xmlBundle;
    /** The already loaded localizations. */
    private Map<Locale, Properties> m_localizations;
    /** The bundle's base name. */
    private String m_basename;
    /** The site path to the folder where the edited resource is in. */
    private String m_sitepath;
    /** The currently edited locale. */
    private Locale m_locale;
    /** The type of the loaded bundle. */
    private BundleType m_bundleType;
    /** Messages used by the GUI. */
    CmsMessages m_messages;

    /** Containers holding the keys for each locale. */
    private Map<Locale, IndexedContainer> m_containers;

    /** The available locales. */
    private Collection<Locale> m_locales;

    /**
     *
     * @param cms the {@link CmsObject} used for reading / writing.
     * @param resource the file that is opened for editing.
     * @throws CmsException thrown if reading some of the involved {@link CmsResource}s is not possible.
     */
    public CmsMessageBundleEditorModel(CmsObject cms, CmsResource resource)
    throws CmsException {

        if (cms == null) {
            throw new CmsException(Messages.get().container(Messages.ERROR_LOADING_BUNDLE_CMS_OBJECT_NULL_0));
        }

        if (resource == null) {
            throw new CmsException(Messages.get().container(Messages.ERROR_LOADING_BUNDLE_FILENAME_NULL_0));
        }

        m_cms = cms;
        m_resource = resource;

        m_containers = new HashMap<Locale, IndexedContainer>();
        m_bundleFiles = new HashMap<Locale, CmsFile>();
        m_localizations = new HashMap<Locale, Properties>();

        m_bundleType = initBundleType();

        if (m_bundleType.equals(BundleType.XML)) {
            initXmlBundle();
        }

        setResourceInformation();

        m_locales = OpenCms.getLocaleManager().getAvailableLocales(m_cms, resource);

        initDescriptor();

    }

    /** Returns the type of the currently edited bundle.
     * @return the type of the currently edited bundle.
     */
    public Object getBundleType() {

        return m_bundleType;
    }

    /**
     * Returns the container with the keys for the currently set locale.
     *
     * @return The container with the keys of the currently set locale.
     * @throws CmsException thrown if loading or creating the resource bundle that is loaded in the container fails.
     * @throws IOException thrown if loading or creating the resource bundle that is loaded in the container fails.
     */
    public Container getContainerForCurrentLocale() throws IOException, CmsException {

        if (null == m_containers.get(m_locale)) {
            addContainerForCurrentLocale();
        }
        return m_containers.get(m_locale);
    }

    /**
     * Returns the currently edited locale.
     *
     * @return the currently edited locale.
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the locales available for the specific resource.
     *
     * @return the locales available for the specific resource.
     */
    public Collection<Locale> getLocales() {

        return m_locales;
    }

    /**
     * Saves the messages for all languages that were opened in the editor.
     *
     * @throws CmsException thrown if saving fails.
     */
    public void save() throws CmsException {

        switch (m_bundleType) {
            case PROPERTY:
                saveToPropertyVfsBundle();
                break;

            case XML:
                saveToXmlVfsBundle();

                break;
            default:
                break;
        }

    }

    /**
     * Set the currently edited locale.
     *
     * @param locale the currently edited locale.
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Unlock all files opened for writing.
     *
     * @throws CmsException thrown if unlocking fails.
     */
    public void unlock() throws CmsException {

        for (Locale l : m_bundleFiles.keySet()) {
            CmsFile f = m_bundleFiles.get(l);
            m_cms.unlockResource(f);
        }
    }

    /**
     * Initializes the IndexedContainer shown in the table for the provided locale.
     * Therefore, the involved {@link CmsResource}s will be read, if not already done.
     * @throws IOException thrown if reading of an involved file fails.
     * @throws CmsException thrown if reading of an involved file fails.
     */
    private void addContainerForCurrentLocale() throws IOException, CmsException {

        IndexedContainer container = new IndexedContainer();

        // create properties
        container.addContainerProperty(PROPERTY_ID_KEY, String.class, "");
        container.addContainerProperty(PROPERTY_ID_DESC, String.class, "");
        container.addContainerProperty(PROPERTY_ID_DEFAULT, String.class, "");
        container.addContainerProperty(PROPERTY_ID_TRANSLATION, String.class, "");

        // add entries
        Locale descriptorLocale = new Locale("en");
        CmsXmlContentValueSequence messages = m_descContent.getValueSequence("/Message", descriptorLocale);
        for (int i = 0; i < messages.getElementCount(); i++) {

            String prefix = messages.getValue(i).getPath() + "/";
            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            String key = m_descContent.getValue(prefix + "Key", descriptorLocale).getStringValue(m_cms);
            item.getItemProperty(PROPERTY_ID_KEY).setValue(key);
            item.getItemProperty(PROPERTY_ID_DESC).setValue(
                m_descContent.getValue(prefix + "Description", descriptorLocale).getStringValue(m_cms));
            item.getItemProperty(PROPERTY_ID_DEFAULT).setValue(
                m_descContent.getValue(prefix + "Default", descriptorLocale).getStringValue(m_cms));
            Properties localization = getLocalizationForCurrentLocale();
            String translation = localization.getProperty(key) == null ? "" : localization.getProperty(key);
            item.getItemProperty(PROPERTY_ID_TRANSLATION).setValue(translation);
        }

        m_containers.put(m_locale, container);

    }

    /**
     * Reads the current properties for a language. If not already done, the properties are read from the respective file.
     * @return the properties.
     * @throws IOException thrown if reading the properties from a file fails.
     * @throws CmsException thrown if reading the properties from a file fails.
     */
    private Properties getLocalizationForCurrentLocale() throws IOException, CmsException {

        if (null == m_localizations.get(m_locale)) {
            switch (m_bundleType) {
                case PROPERTY:
                    loadLocalizationForCurrentLocaleFromPropertyBundle();
                    break;
                case XML:
                    loadLocalizationForCurrentLocaleFromXmlBundle();
                    break;
                default:
                    break;
            }
        }
        return m_localizations.get(m_locale);
    }

    /**
     * Init the bundle type member variable.
     * @return the bundle type of the opened resource.
     */
    private BundleType initBundleType() {

        String resourceTypeName = OpenCms.getResourceManager().getResourceType(m_resource).getTypeName();
        return BundleType.toBundleType(resourceTypeName);
    }

    /**
     * Reads the bundle descriptor, sets m_desc and m_descContent.
     * @throws CmsXmlException thrown when unmarshalling fails.
     * @throws CmsException thrown when reading the resource fails or several bundle descriptors for the bundle exist.
     */
    private void initDescriptor() throws CmsXmlException, CmsException {

        CmsSolrQuery query = new CmsSolrQuery();
        query.setResourceTypes(BundleType.DESCRIPTOR.toString());
        query.setFilterQueries("fq=filename:\"" + m_basename + DESCRIPTOR_POSTFIX + "\"");
        query.add("fl", "path");
        CmsSolrResultList results = OpenCms.getSearchManager().getIndexSolr("Solr Offline").search(m_cms, query);
        CmsResource desc = null;
        switch (results.size()) {
            case 0:
                break;
            case 1:
                desc = results.get(0);
                break;
            default:
                String files = "";
                for (CmsResource res : results) {
                    files += " " + res.getRootPath();
                }
                throw new CmsException(Messages.get().container(Messages.ERROR_BUNDLE_DESCRIPTOR_NOT_UNIQUE_1, files));
        }
        if (null != desc) {
            m_descContent = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(desc));
        }
    }

    /**
     * Unmarshals the XML content and adds the file to the bundle files.
     * @throws CmsException thrown if reading the file or unmarshalling fails.
     */
    private void initXmlBundle() throws CmsException {

        CmsFile bundleFile = m_cms.readFile(m_resource);
        m_bundleFiles.put(null, bundleFile);
        m_xmlBundle = CmsXmlContentFactory.unmarshal(m_cms, bundleFile);

    }

    /**
     * Loads the propertyvfsbundle for the provided locale.
     * If the bundle file is not present, it will be created.
     *
     * @throws IOException thrown if loading fails.
     * @throws CmsException thrown if reading or creation fails.
     */
    private void loadLocalizationForCurrentLocaleFromPropertyBundle() throws IOException, CmsException {

        // may throw exception again
        String sitePath = m_sitepath + m_basename + "_" + m_locale.toString();
        CmsFile file = null;
        if (m_cms.existsResource(sitePath)) {
            CmsResource resource = m_cms.readResource(sitePath);
            CmsLockUtil.ensureLock(m_cms, resource);
            file = m_cms.readFile(sitePath);
        } else {
            CmsResource res = m_cms.createResource(
                sitePath,
                OpenCms.getResourceManager().getResourceType(BundleType.PROPERTY.toString()));
            m_cms.lockResource(res);
            file = m_cms.readFile(res);
        }
        m_bundleFiles.put(m_locale, file);
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(file.getContents()));
        m_localizations.put(m_locale, props);

    }

    /**
     * Loads the localization for the current locale from a bundle of type xmlvfsbundle.
     * It assumes, the content has already been unmarshalled before.
     */
    private void loadLocalizationForCurrentLocaleFromXmlBundle() {

        Collection<I_CmsXmlContentValue> messages = m_xmlBundle.getSubValues("/", m_locale);
        Properties props = new Properties();
        if (null != messages) {
            for (I_CmsXmlContentValue msg : messages) {
                String msgpath = msg.getPath();
                props.put(
                    m_xmlBundle.getStringValue(m_cms, msgpath + "Key", m_locale),
                    m_xmlBundle.getStringValue(m_cms, msgpath + "Value", m_locale));
            }
        }
        m_localizations.put(m_locale, props);
    }

    /**
     * Saves messages to a propertyvfsbundle file.
     *
     * @throws CmsException thrown if writing to the file fails.
     */
    private void saveToPropertyVfsBundle() throws CmsException {

        for (Locale l : m_locales) {
            Container c = m_containers.get(l);
            if (null != c) {
                StringBuffer content = new StringBuffer();
                for (Object itemId : c.getItemIds()) {
                    String key = c.getContainerProperty(itemId, PROPERTY_ID_KEY).toString();
                    String value = c.getContainerProperty(itemId, PROPERTY_ID_TRANSLATION).toString();
                    content.append(key).append("=").append(value).append("\n");
                }
                byte[] contentBytes = content.toString().getBytes();
                CmsFile file = m_bundleFiles.get(l);
                file.setContents(contentBytes);
                m_cms.writeFile(m_bundleFiles.get(l));
            }
        }
    }

    /**
     * Saves messages to a xmlvfsbundle file.
     *
     * @throws CmsException thrown if writing to the file fails.
     */
    private void saveToXmlVfsBundle() throws CmsException {

        for (Locale l : m_locales) {
            Container c = m_containers.get(l);
            if (null != c) {
                m_xmlBundle.removeLocale(l);
                m_xmlBundle.addLocale(m_cms, l);
                int i = 0;
                for (Object itemId : c.getItemIds()) {
                    i++;
                    String key = c.getContainerProperty(itemId, PROPERTY_ID_KEY).toString();
                    String value = c.getContainerProperty(itemId, PROPERTY_ID_TRANSLATION).toString();
                    m_xmlBundle.addValue(m_cms, "Message", l, i);
                    m_xmlBundle.addValue(m_cms, "Message[" + i + "]/Key", l, 1).setStringValue(m_cms, key);
                    m_xmlBundle.addValue(m_cms, "Message[" + i + "]/Value", l, 1).setStringValue(m_cms, value);
                }
            }
            CmsFile bundleFile = m_bundleFiles.get(null);
            bundleFile.setContents(m_xmlBundle.marshal());
            m_cms.writeFile(m_bundleFiles.get(null));
        }
    }

    /** Extract site path, base name and locale from the resource opened with the editor. */
    private void setResourceInformation() {

        String sitePath = m_cms.getSitePath(m_resource);
        int pathEnd = sitePath.lastIndexOf('/') + 1;
        String baseName = sitePath.substring(pathEnd);
        m_sitepath = sitePath.substring(0, pathEnd);
        switch (BundleType.toBundleType(OpenCms.getResourceManager().getResourceType(m_resource).getTypeName())) {
            case PROPERTY:
                String localeSuffix = CmsStringUtil.getLocaleSuffixForName(baseName);
                if ((null != localeSuffix) && !localeSuffix.isEmpty()) {
                    baseName = baseName.substring(
                        0,
                        baseName.lastIndexOf(localeSuffix) - (1 /* cut off trailing underscore, too*/));
                    m_locale = CmsLocaleManager.getLocale(localeSuffix);
                } else {
                    m_locale = new Locale("default");
                }
                break;
            case XML:
                m_locale = OpenCms.getLocaleManager().getBestAvailableLocaleForXmlContent(
                    m_cms,
                    m_resource,
                    m_xmlBundle);
                break;
            case DESCRIPTOR:
                m_basename = baseName.substring(0, baseName.length() - DESCRIPTOR_POSTFIX.length());
                m_locale = new Locale("en");
                break;
            default:
                throw new IllegalArgumentException(
                    Messages.get().container(
                        Messages.ERROR_UNSUPPORTED_BUNDLE_TYPE_1,
                        BundleType.toBundleType(
                            OpenCms.getResourceManager().getResourceType(m_resource).getTypeName())).toString());
        }
        m_basename = baseName;

    }
}
