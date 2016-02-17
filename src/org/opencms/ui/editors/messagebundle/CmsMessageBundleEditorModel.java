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
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

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

    /** The different edit modes. */
    enum EditMode {
        /** Editing the messages and the descriptor. */
        MASTER, /** Only editing messages. */
        DEFAULT
    }

    /**
     * The editor state holds the information on what columns of the editors table
     * should be editable and if the options column should be shown.
     * The state depends on the loaded bundle and the edit mode.
     */
    final class EditorState {

        /** The editable columns (from left to right).*/
        private List<String> m_editableColumns;
        /** Flag, indicating if the options column should be shown. */
        private boolean m_showOptions;

        /** Constructor, setting all the state information directly.
         * @param editableColumns the property ids of the editable columns (from left to right)
         * @param showOptions flag, indicating if the options column should be shown.
         */
        public EditorState(List<String> editableColumns, boolean showOptions) {
            m_editableColumns = editableColumns;
            m_showOptions = showOptions;
        }

        /** Returns the editable columns from left to right (as there property ids).
         * @return the editable columns from left to right (as there property ids).
         */
        public List<String> getEditableColumns() {

            return m_editableColumns;
        }

        /** Returns a flag, indicating if the options column should be shown.
         * @return a flag, indicating if the options column should be shown.
         */
        public boolean isShowOptions() {

            return m_showOptions;
        }
    }

    /** Helper to handle the lock reports together with the files. */
    private static final class LockedFile {

        /** The file that was read. */
        private CmsFile m_file;
        /** The lock action record from locking the file. */
        private CmsLockActionRecord m_lockRecord;
        /** Flag, indicating if the file was newly created. */
        private boolean m_new;

        /** Private constructor.
         * @param cms the cms user context.
         * @param resource the resource to lock and read.
         */
        private LockedFile(CmsObject cms, CmsResource resource) {
            try {
                m_lockRecord = CmsLockUtil.ensureLock(cms, resource);
                m_file = cms.readFile(resource);
                m_new = false;
            } catch (CmsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * Lock and read a file.
         * @param cms the cms user context.
         * @param resource the resource to lock and read.
         * @return the read file with the lock action record.
         */
        public static LockedFile lockResource(CmsObject cms, CmsResource resource) {

            return new LockedFile(cms, resource);
        }

        /** Returns the file.
         * @return the file.
         */
        public CmsFile getFile() {

            return m_file;
        }

        /** Returns the lock action record.
         * @return the lock action record.
         */
        public CmsLockActionRecord getLockActionRecord() {

            return m_lockRecord;
        }

        /**
         * Returns a flag, indicating if the file is newly created.
         * @return flag, indicating if the file is newly created.
         */
        public boolean isCreated() {

            return m_new;
        }

        /**
         * Set the flag, indicating if the file was newly created.
         * @param isNew flag, indicating if the file was newly created.
         */
        public void setCreated(boolean isNew) {

            m_new = isNew;

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
    /** The locale of the bundle descriptor. */
    private static final Locale LOCALE_BUNDLE_DESCRIPTOR = new Locale("en");
    /** CmsObject for read / write operations. */
    private CmsObject m_cms;
    /** The file currently edited. */
    private Map<Locale, LockedFile> m_bundleFiles;
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
    private IndexedContainer m_container;

    /** The available locales. */
    private Collection<Locale> m_locales;
    /** Map from edit mode to the editor state. */
    private Map<EditMode, EditorState> m_editorState;
    /** Flag, indicating if a master edit mode is available. */
    private boolean m_hasMasterMode;
    /** The current edit mode. */
    private EditMode m_editMode;
    /** Descriptor file, if edited besides a bundle. */
    private LockedFile m_descFile;

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
        m_editMode = EditMode.DEFAULT;

        m_bundleFiles = new HashMap<Locale, LockedFile>();
        m_localizations = new HashMap<Locale, Properties>();

        m_bundleType = initBundleType();

        if (m_bundleType.equals(BundleType.XML)) {
            initXmlBundle();
        }

        setResourceInformation();

        if (m_bundleType.equals(BundleType.DESCRIPTOR)) {
            m_locales = new ArrayList<Locale>(1);
            m_locales.add(LOCALE_BUNDLE_DESCRIPTOR);
        } else {
            m_locales = OpenCms.getLocaleManager().getAvailableLocales(m_cms, resource);
        }

        initDescriptor();

        initHasMasterMode();

        initEditorStates();

    }

    /** Returns the type of the currently edited bundle.
     * @return the type of the currently edited bundle.
     */
    public Object getBundleType() {

        return m_bundleType;
    }

    /**
     * Returns the container filled according to the current locale.
     * @return the container filled according to the current locale.
     * @throws IOException thrown if reading a bundle resource fails.
     * @throws CmsException thrown if reading a bundle resource fails.
     */
    public IndexedContainer getContainerForCurrentLocale() throws IOException, CmsException {

        if (null == m_container) {
            m_container = createContainer();
        }
        return m_container;
    }

    /**
     * Returns the editable columns for the provided edit mode.
     * @param mode the edit mode.
     * @return the editable columns for the provided edit mode.
     */
    public List<String> getEditableColumns(EditMode mode) {

        return m_editorState.get(mode).getEditableColumns();
    }

    /** Returns the current edit mode.
     * @return the current edit mode.
     */
    public EditMode getEditMode() {

        return m_editMode;
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

    /** Returns a flag, indicating if a bundle descriptor is present.
     * @return flag, indicating if a bundle descriptor is present.
     */
    public boolean hasDescriptor() {

        return !m_bundleType.equals(BundleType.DESCRIPTOR) && (m_descContent != null);
    }

    /**
     * Returns a flag, indicating if a master edit mode is available.
     * @return a flag, indicating if a master edit mode is available.
     */
    public boolean hasMasterMode() {

        return m_hasMasterMode;
    }

    /**
     * Returns a flag, indicating if the options column (with add and delete option for rows)
     * should be shown in the given edit mode.
     * @param mode the edit mode for which the column option is requested.
     * @return a flag, indicating if the options column (with add and delete option for rows)
     */
    public boolean isShowOptionsColumn(EditMode mode) {

        return m_editorState.get(mode).isShowOptions();
    }

    /**
     * Saves the messages for all languages that were opened in the editor.
     *
     * @throws CmsException thrown if saving fails.
     */
    public void save() throws CmsException {

        switch (m_bundleType) {
            case PROPERTY:
                saveLocalization();
                saveToPropertyVfsBundle();
                break;

            case XML:
                saveLocalization();
                saveToXmlVfsBundle();

                break;

            case DESCRIPTOR:
                break;
            default:
                throw new IllegalArgumentException();
        }
        if (null != m_descFile) {
            saveToBundleDescriptor();
        }

    }

    /**
     * Set the edit mode.
     * @param mode the edit mode to set.
     */
    public void setEditMode(EditMode mode) {

        if ((mode == EditMode.MASTER) && (null == m_descFile)) {
            m_descFile = LockedFile.lockResource(m_cms, m_desc);
        }
        m_editMode = mode;
    }

    /**
     * Set the currently edited locale.
     *
     * @param locale the currently edited locale.
     * @throws CmsException  thrown if reading a bundle resource fails.
     * @throws IOException thrown if reading a bundle resource fails.
     */
    public void setLocale(Locale locale) throws IOException, CmsException {

        adjustExistingContainerForLocale(locale);
        m_locale = locale;
    }

    /**
     * Unlock all files opened for writing.
     *
     * @throws CmsException thrown if unlocking fails.
     */
    public void unlock() throws CmsException {

        for (Locale l : m_bundleFiles.keySet()) {
            LockedFile f = m_bundleFiles.get(l);
            if (!f.getLockActionRecord().getChange().equals(LockChange.unchanged) || f.isCreated()) {
                m_cms.unlockResource(f.getFile());
            }
        }
        if (null != m_descFile) {
            if (!m_descFile.getLockActionRecord().getChange().equals(LockChange.unchanged)) {
                m_cms.unlockResource(m_desc);
            }
        }
    }

    /**
     * Adjusts the locale for an already existing container by first saving the translation for the current locale and the loading the values of the new locale.
     *
     * @param newLocale the locale where to switch to.
     * @throws IOException thrown if a bundle resource must be read and reading fails.
     * @throws CmsException thrown if a bundle resource must be read and reading fails.
     */
    private void adjustExistingContainerForLocale(Locale newLocale) throws IOException, CmsException {

        saveLocalization();
        replaceValues(newLocale);

    }

    /**
     * Initializes the IndexedContainer shown in the table for the current locale.
     * Therefore, the involved {@link CmsResource}s will be read, if not already done.
     * @return the created container
     * @throws IOException thrown if reading of an involved file fails.
     * @throws CmsException thrown if reading of an involved file fails.
     */
    private IndexedContainer createContainer() throws IOException, CmsException {

        IndexedContainer container = null;

        if (m_bundleType.equals(BundleType.DESCRIPTOR)) {
            container = createContainerForDescriptorEditing();
        } else {
            if (hasDescriptor()) {
                container = createContainerForBundleWithDescriptor();
            } else {
                container = createContainerForBundleWithoutDescriptor();
            }
        }
        return container;
    }

    /**
     * Creates the container for a bundle with descriptor.
     * @return the container for a bundle with descriptor.
     * @throws IOException thrown if reading the bundle fails.
     * @throws CmsException thrown if reading the bundle fails.
     */
    private IndexedContainer createContainerForBundleWithDescriptor() throws IOException, CmsException {

        IndexedContainer container = new IndexedContainer();

        // create properties
        container.addContainerProperty(PROPERTY_ID_KEY, String.class, "");
        container.addContainerProperty(PROPERTY_ID_DESC, String.class, "");
        container.addContainerProperty(PROPERTY_ID_DEFAULT, String.class, "");
        container.addContainerProperty(PROPERTY_ID_TRANSLATION, String.class, "");

        // add entries
        Properties localization = getLocalization(m_locale);
        CmsXmlContentValueSequence messages = m_descContent.getValueSequence("/Message", LOCALE_BUNDLE_DESCRIPTOR);
        for (int i = 0; i < messages.getElementCount(); i++) {

            String prefix = messages.getValue(i).getPath() + "/";
            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            String key = m_descContent.getValue(prefix + "Key", LOCALE_BUNDLE_DESCRIPTOR).getStringValue(m_cms);
            item.getItemProperty(PROPERTY_ID_KEY).setValue(key);
            item.getItemProperty(PROPERTY_ID_DESC).setValue(
                m_descContent.getValue(prefix + "Description", LOCALE_BUNDLE_DESCRIPTOR).getStringValue(m_cms));
            item.getItemProperty(PROPERTY_ID_DEFAULT).setValue(
                m_descContent.getValue(prefix + "Default", LOCALE_BUNDLE_DESCRIPTOR).getStringValue(m_cms));
            String translation = localization.getProperty(key);
            item.getItemProperty(PROPERTY_ID_TRANSLATION).setValue(null == translation ? "" : translation);
        }

        return container;

    }

    /**
     * Creates the container for a bundle without descriptor.
     * @return the container for a bundle without descriptor.
     * @throws IOException thrown if reading the bundle fails.
     * @throws CmsException thrown if reading the bundle fails.
     */
    private IndexedContainer createContainerForBundleWithoutDescriptor() throws IOException, CmsException {

        IndexedContainer container = new IndexedContainer();

        // create properties
        container.addContainerProperty(PROPERTY_ID_KEY, String.class, "");
        container.addContainerProperty(PROPERTY_ID_TRANSLATION, String.class, "");

        // add entries
        Properties localization = getLocalization(m_locale);
        for (Object key : localization.keySet()) {

            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            item.getItemProperty(PROPERTY_ID_KEY).setValue(key);
            String translation = localization.getProperty((String)key);
            item.getItemProperty(PROPERTY_ID_TRANSLATION).setValue(null == translation ? "" : translation);
        }

        return container;
    }

    /**
     * Creates the container for a bundle descriptor.
     * @return the container for a bundle descriptor.
     */
    private IndexedContainer createContainerForDescriptorEditing() {

        IndexedContainer container = new IndexedContainer();

        // create properties
        container.addContainerProperty(PROPERTY_ID_KEY, String.class, "");
        container.addContainerProperty(PROPERTY_ID_DESC, String.class, "");
        container.addContainerProperty(PROPERTY_ID_DEFAULT, String.class, "");

        // add entries
        CmsXmlContentValueSequence messages = m_descContent.getValueSequence("/Message", LOCALE_BUNDLE_DESCRIPTOR);
        for (int i = 0; i < messages.getElementCount(); i++) {

            String prefix = messages.getValue(i).getPath() + "/";
            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            String key = m_descContent.getValue(prefix + "Key", LOCALE_BUNDLE_DESCRIPTOR).getStringValue(m_cms);
            item.getItemProperty(PROPERTY_ID_KEY).setValue(key);
            item.getItemProperty(PROPERTY_ID_DESC).setValue(
                m_descContent.getValue(prefix + "Description", LOCALE_BUNDLE_DESCRIPTOR).getStringValue(m_cms));
            item.getItemProperty(PROPERTY_ID_DEFAULT).setValue(
                m_descContent.getValue(prefix + "Default", LOCALE_BUNDLE_DESCRIPTOR).getStringValue(m_cms));
        }

        return container;

    }

    /**
     * Reads the current properties for a language. If not already done, the properties are read from the respective file.
     * @param locale the locale for which the localization should be returned.
     * @return the properties.
     * @throws IOException thrown if reading the properties from a file fails.
     * @throws CmsException thrown if reading the properties from a file fails.
     */
    private Properties getLocalization(Locale locale) throws IOException, CmsException {

        if (null == m_localizations.get(locale)) {
            switch (m_bundleType) {
                case PROPERTY:
                    loadLocalizationFromPropertyBundle(locale);
                    break;
                case XML:
                    loadLocalizationFromXmlBundle(locale);
                    break;
                case DESCRIPTOR:
                    return null;
                default:
                    break;
            }
        }
        return m_localizations.get(locale);
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

        if (m_bundleType.equals(BundleType.DESCRIPTOR)) {
            m_desc = m_resource;
            m_descContent = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(m_desc));
            m_descFile = LockedFile.lockResource(m_cms, m_desc);
        } else {
            CmsSolrQuery query = new CmsSolrQuery();
            query.setResourceTypes(BundleType.DESCRIPTOR.toString());
            query.setFilterQueries("filename:\"" + m_basename + DESCRIPTOR_POSTFIX + "\"");
            query.add("fl", "path");
            CmsSolrResultList results = OpenCms.getSearchManager().getIndexSolr("Solr Offline").search(
                m_cms,
                query,
                true,
                null,
                true,
                null);
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
                    throw new CmsException(
                        Messages.get().container(Messages.ERROR_BUNDLE_DESCRIPTOR_NOT_UNIQUE_1, files));
            }
            if (null != desc) {
                m_desc = desc;
                m_descContent = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(desc));
            }
        }
    }

    /**
     * Initializes the editor states for the different modes, depending on the type of the opened file.
     */
    private void initEditorStates() {

        m_editorState = new HashMap<EditMode, EditorState>();
        List<String> cols = null;
        switch (m_bundleType) {
            case PROPERTY:
            case XML:
                if (hasDescriptor()) { // bundle descriptor is present, keys are not editable in default mode, maybe master mode is available
                    cols = new ArrayList<String>(1);
                    cols.add(PROPERTY_ID_TRANSLATION);
                    m_editorState.put(EditMode.DEFAULT, new EditorState(cols, false));
                    if (hasMasterMode()) { // the bundle descriptor is editable
                        cols = new ArrayList<String>(4);
                        cols.add(PROPERTY_ID_KEY);
                        cols.add(PROPERTY_ID_DESC);
                        cols.add(PROPERTY_ID_DEFAULT);
                        cols.add(PROPERTY_ID_TRANSLATION);
                        m_editorState.put(EditMode.MASTER, new EditorState(cols, true));
                    }
                } else { // no bundle descriptor given - implies no master mode
                    cols = new ArrayList<String>(1);
                    cols.add(PROPERTY_ID_KEY);
                    cols.add(PROPERTY_ID_TRANSLATION);
                    m_editorState.put(EditMode.DEFAULT, new EditorState(cols, true));
                }
                break;
            case DESCRIPTOR:
                cols = new ArrayList<String>(3);
                cols.add(PROPERTY_ID_KEY);
                cols.add(PROPERTY_ID_DESC);
                cols.add(PROPERTY_ID_DEFAULT);
                m_editorState.put(EditMode.DEFAULT, new EditorState(cols, true));
                break;
            default:
                throw new IllegalArgumentException();
        }

    }

    /**
     * Initializes the information on an available master mode.
     * @throws CmsException thrown if the write permission check on the bundle descriptor fails.
     */
    private void initHasMasterMode() throws CmsException {

        if (hasDescriptor()
            && m_cms.hasPermissions(m_desc, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL)) {
            m_hasMasterMode = true;
        } else {
            m_hasMasterMode = false;
        }
    }

    /**
     * Unmarshals the XML content and adds the file to the bundle files.
     * @throws CmsException thrown if reading the file or unmarshalling fails.
     */
    private void initXmlBundle() throws CmsException {

        LockedFile bundleFile = LockedFile.lockResource(m_cms, m_resource);
        m_bundleFiles.put(null, bundleFile);
        m_xmlBundle = CmsXmlContentFactory.unmarshal(m_cms, bundleFile.getFile());

    }

    /**
     * Loads the propertyvfsbundle for the provided locale.
     * If the bundle file is not present, it will be created.
     * @param locale the locale for which the localization should be loaded
     *
     * @throws IOException thrown if loading fails.
     * @throws CmsException thrown if reading or creation fails.
     */
    private void loadLocalizationFromPropertyBundle(Locale locale) throws IOException, CmsException {

        // may throw exception again
        String sitePath = m_sitepath + m_basename + "_" + locale.toString();
        LockedFile file = null;
        if (m_cms.existsResource(sitePath)) {
            CmsResource resource = m_cms.readResource(sitePath);
            file = LockedFile.lockResource(m_cms, resource);
        } else {
            CmsResource res = m_cms.createResource(
                sitePath,
                OpenCms.getResourceManager().getResourceType(BundleType.PROPERTY.toString()));
            file = LockedFile.lockResource(m_cms, res);
            file.setCreated(true);
        }
        m_bundleFiles.put(locale, file);
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(file.getFile().getContents()));
        m_localizations.put(locale, props);

    }

    /**
     * Loads the localization for the current locale from a bundle of type xmlvfsbundle.
     * It assumes, the content has already been unmarshalled before.
     * @param locale the locale for which the localization should be loaded
     */
    private void loadLocalizationFromXmlBundle(Locale locale) {

        CmsXmlContentValueSequence messages = m_xmlBundle.getValueSequence("Message", locale);
        Properties props = new Properties();
        if (null != messages) {
            for (I_CmsXmlContentValue msg : messages.getValues()) {
                String msgpath = msg.getPath();
                props.put(
                    m_xmlBundle.getStringValue(m_cms, msgpath + "/Key", locale),
                    m_xmlBundle.getStringValue(m_cms, msgpath + "/Value", locale));
            }
        }
        m_localizations.put(locale, props);
    }

    /**
     * Replaces the translations in an existing container with the translations for the provided locale.
     * @param newLocale the locale for which translations should be loaded.
     * @throws IOException thrown if loading the localization from a bundle resource fails.
     * @throws CmsException thrown if loading the localization from a bundle resource fails.
     */
    private void replaceValues(Locale newLocale) throws IOException, CmsException {

        Properties localization = getLocalization(newLocale);
        for (Object itemId : m_container.getItemIds()) {
            Item item = m_container.getItem(itemId);
            String key = item.getItemProperty(PROPERTY_ID_KEY).getValue().toString();
            String value = localization.getProperty(key);
            item.getItemProperty(PROPERTY_ID_TRANSLATION).setValue(null == value ? "" : value);
        }
    }

    /**
     * Saves the current translations from the container to the respective localization.
     */
    private void saveLocalization() {

        Properties localization = new Properties();
        for (Object itemId : m_container.getItemIds()) {
            Item item = m_container.getItem(itemId);
            String key = item.getItemProperty(PROPERTY_ID_KEY).getValue().toString();
            String value = item.getItemProperty(PROPERTY_ID_TRANSLATION).getValue().toString();
            localization.setProperty(key, value);
        }
        m_localizations.put(m_locale, localization);

    }

    /**
     * Save the values to the bundle descriptor.
     * @throws CmsException thrown if saving fails.
     */
    private void saveToBundleDescriptor() throws CmsException {

        m_descContent.removeLocale(LOCALE_BUNDLE_DESCRIPTOR);
        m_descContent.addLocale(m_cms, LOCALE_BUNDLE_DESCRIPTOR);
        String key;
        String desc;
        String defaultValue;
        int i = 0;
        for (Object itemId : m_container.getItemIds()) {
            Item item = m_container.getItem(itemId);
            key = item.getItemProperty(PROPERTY_ID_KEY).getValue().toString();
            if (!key.isEmpty()) {
                desc = item.getItemProperty(PROPERTY_ID_DESC).getValue().toString();
                defaultValue = item.getItemProperty(PROPERTY_ID_DEFAULT).getValue().toString();
                m_descContent.addValue(m_cms, "Message", LOCALE_BUNDLE_DESCRIPTOR, i);
                i++;
                m_descContent.getValue("Message[" + i + "]/Key", LOCALE_BUNDLE_DESCRIPTOR).setStringValue(m_cms, key);
                m_descContent.getValue("Message[" + i + "]/Description", LOCALE_BUNDLE_DESCRIPTOR).setStringValue(
                    m_cms,
                    desc);
                m_descContent.getValue("Message[" + i + "]/Default", LOCALE_BUNDLE_DESCRIPTOR).setStringValue(
                    m_cms,
                    defaultValue);
            }
        }
        m_descFile.getFile().setContents(m_descContent.marshal());
        m_cms.writeFile(m_descFile.getFile());

    }

    /**
     * Saves messages to a propertyvfsbundle file.
     *
     * @throws CmsException thrown if writing to the file fails.
     */
    private void saveToPropertyVfsBundle() throws CmsException {

        for (Locale l : m_locales) {
            Properties props = m_localizations.get(l);
            if (null != props) {
                StringBuffer content = new StringBuffer();
                for (Object k : props.keySet()) {
                    String key = (String)k;
                    if (!key.isEmpty()) {
                        String value = props.getProperty(key);
                        if (!value.isEmpty()) {
                            content.append(key).append("=").append(value).append("\n");
                        }
                    }
                }
                byte[] contentBytes = content.toString().getBytes();
                CmsFile file = m_bundleFiles.get(l).getFile();
                file.setContents(contentBytes);
                m_cms.writeFile(file);
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
            Properties props = m_localizations.get(l);
            if (null != props) {
                m_xmlBundle.removeLocale(l);
                m_xmlBundle.addLocale(m_cms, l);
                int i = 0;
                for (Object k : props.keySet()) {
                    String key = (String)k;
                    if (!key.isEmpty()) {
                        String value = props.getProperty(key);
                        if (!value.isEmpty()) {
                            m_xmlBundle.addValue(m_cms, "Message", l, i);
                            i++;
                            m_xmlBundle.getValue("Message[" + i + "]/Key", l).setStringValue(m_cms, key);
                            m_xmlBundle.getValue("Message[" + i + "]/Value", l).setStringValue(m_cms, value);
                        }
                    }
                }
            }
            CmsFile bundleFile = m_bundleFiles.get(null).getFile();
            bundleFile.setContents(m_xmlBundle.marshal());
            m_cms.writeFile(bundleFile);
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
