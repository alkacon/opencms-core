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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessageException;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLockUtil.LockedFile;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.BundleType;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.Descriptor;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.EditMode;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.EditorState;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.TableProperty;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.IndexedContainer;

/**
 * The class contains the logic behind the message translation editor.
 * In particular it reads / writes the involved files and provides the contents as {@link IndexedContainer}.
 */
public class CmsMessageBundleEditorModel {

    /** Wrapper for the configurable messages for the column headers of the message bundle editor. */
    public static final class ConfigurableMessages {

        /** The messages from the default message bundle. */
        CmsMessages m_defaultMessages;
        /** The messages from a configured message bundle, overwriting the ones from the default bundle. */
        CmsMessages m_configuredMessages;

        /**
         * Default constructor.
         * @param defaultMessages the default messages.
         * @param locale the locale in which the messages are requested.
         * @param configuredBundle the base name of the configured message bundle (can be <code>null</code>).
         */
        public ConfigurableMessages(CmsMessages defaultMessages, Locale locale, String configuredBundle) {
            m_defaultMessages = defaultMessages;
            if (null != configuredBundle) {
                CmsMessages bundle = new CmsMessages(configuredBundle, locale);
                if (null != bundle.getResourceBundle()) {
                    m_configuredMessages = bundle;
                }
            }
        }

        /**
         * Returns the localized column header.
         * @param column the column's property (name).
         * @return the localized columen header.
         */
        public String getColumnHeader(TableProperty column) {

            switch (column) {
                case DEFAULT:
                    return getMessage(Messages.GUI_COLUMN_HEADER_DEFAULT_0);
                case DESCRIPTION:
                    return getMessage(Messages.GUI_COLUMN_HEADER_DESCRIPTION_0);
                case KEY:
                    return getMessage(Messages.GUI_COLUMN_HEADER_KEY_0);
                case OPTIONS:
                    return "";
                case TRANSLATION:
                    return getMessage(Messages.GUI_COLUMN_HEADER_TRANSLATION_0);
                default:
                    throw new IllegalArgumentException();
            }
        }

        /**
         * Returns the message for the key, either from the configured bundle, or - if not found - from the default bundle.
         *
         * @param key message key.
         * @return the message for the key.
         */
        private String getMessage(String key) {

            if (null != m_configuredMessages) {
                try {
                    return m_configuredMessages.getString(key);
                } catch (@SuppressWarnings("unused") CmsMessageException e) {
                    // do nothing - use default messages
                }
            }
            try {
                return m_defaultMessages.getString(key);
            } catch (@SuppressWarnings("unused") CmsMessageException e) {
                return "???" + key + "???";
            }
        }

    }

    /** Comparator that compares strings case insensitive. */
    static class CmsCaseInsensitivePropertyValueComparator implements Comparator<Object> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {

            int r = 0;
            // Normal non-null comparison
            if ((o1 != null) && (o2 != null)) {
                if ((o1 instanceof String) && (o2 instanceof String)) {
                    return ((String)o1).compareToIgnoreCase((String)o2);
                } else {
                    // Assume the objects can be cast to Comparable, throw
                    // ClassCastException otherwise.
                    r = ((Comparable<Object>)o1).compareTo(o2);
                }
            } else if (o1 == o2) {
                // Objects are equal if both are null
                r = 0;
            } else {
                if (o1 == null) {
                    r = -1; // null is less than non-null
                } else {
                    r = 1; // non-null is greater than null
                }
            }

            return r;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMessageBundleEditorModel.class);

    /** The property for configuring the message bundle used for localizing the bundle descriptors entries. */
    public static final String PROPERTY_BUNDLE_DESCRIPTOR_LOCALIZATION = "bundle.descriptor.messages";
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
    private Map<Locale, Map<String, String>> m_localizations;
    /** The bundle's base name. */
    private String m_basename;
    /** The site path to the folder where the edited resource is in. */
    private String m_sitepath;
    /** The currently edited locale. */
    private Locale m_locale;
    /** The type of the loaded bundle. */
    private CmsMessageBundleEditorTypes.BundleType m_bundleType;
    /** Messages used by the GUI. */
    CmsMessages m_messages;
    /** The complete key set as map from keys to the number of occurrences. */
    CmsMessageBundleEditorTypes.KeySet m_keyset;

    /** Containers holding the keys for each locale. */
    private IndexedContainer m_container;

    /** The available locales. */
    private Collection<Locale> m_locales;
    /** Map from edit mode to the editor state. */
    private Map<CmsMessageBundleEditorTypes.EditMode, EditorState> m_editorState;
    /** Flag, indicating if a master edit mode is available. */
    private boolean m_hasMasterMode;
    /** The current edit mode. */
    private CmsMessageBundleEditorTypes.EditMode m_editMode;
    /** Descriptor file, if edited besides a bundle. */
    private LockedFile m_descFile;

    /** The configured resource bundle used for the column headings of the bundle descriptor. */
    private String m_configuredBundle;

    /** Flag, indicating if the locale of the bundle that is edited has switched on opening. */
    private boolean m_switchedLocaleOnOpening;

    /** Flag, indicating if at least one default value is present in the current descriptor. */
    private boolean m_hasDefault;

    /** Flag, indicating if at least one description is present in the current descriptor. */
    private boolean m_hasDescription;

    /** Flag, indicating if the descriptor should be removed when editing is cancelled. */
    private boolean m_removeDescriptorOnCancel;

    /**
     *
     * @param cms the {@link CmsObject} used for reading / writing.
     * @param resource the file that is opened for editing.
     * @throws CmsException thrown if reading some of the involved {@link CmsResource}s is not possible.
     * @throws IOException initialization of a property bundle fails
     */
    public CmsMessageBundleEditorModel(CmsObject cms, CmsResource resource)
    throws CmsException, IOException {

        if (cms == null) {
            throw new CmsException(Messages.get().container(Messages.ERR_LOADING_BUNDLE_CMS_OBJECT_NULL_0));
        }

        if (resource == null) {
            throw new CmsException(Messages.get().container(Messages.ERR_LOADING_BUNDLE_FILENAME_NULL_0));
        }

        m_cms = cms;
        m_resource = resource;
        m_editMode = CmsMessageBundleEditorTypes.EditMode.DEFAULT;

        m_bundleFiles = new HashMap<Locale, LockedFile>();
        m_localizations = new HashMap<Locale, Map<String, String>>();
        m_keyset = new CmsMessageBundleEditorTypes.KeySet();

        m_bundleType = initBundleType();

        m_locales = initLocales();

        //IMPORTANT: The order of the following method calls is important.

        if (m_bundleType.equals(CmsMessageBundleEditorTypes.BundleType.XML)) {
            initXmlBundle();
        }

        setResourceInformation();

        initDescriptor();

        if (m_bundleType.equals(CmsMessageBundleEditorTypes.BundleType.PROPERTY)) {
            initPropertyBundle();
        }

        initHasMasterMode();

        initEditorStates();

    }

    /**
     * Converts {@link Properties} to {@link Map} from {@link String} to {@link String}.
     *
     * @param props the properties to convert.
     * @return the resulting map.
     */
    private static Map<String, String> propertiesToMap(Properties props) {

        if (null == props) {
            return new HashMap<String, String>();
        }
        Map<String, String> map = new HashMap<String, String>(props.size());
        for (Object key : props.keySet()) {
            map.put((String)key, props.getProperty((String)key));
        }
        return map;
    }

    /**
     * Creates a descriptor for the currently edited message bundle.
     * @return <code>true</code> if the descriptor could be created, <code>false</code> otherwise.
     */
    public boolean addDescriptor() {

        saveLocalization();
        IndexedContainer oldContainer = m_container;
        try {
            createAndLockDescriptorFile();
            unmarshalDescriptor();
            updateBundleDescriptorContent();
            m_hasMasterMode = true;
            m_container = createContainer();
            m_editorState.put(EditMode.DEFAULT, getDefaultState());
            m_editorState.put(EditMode.MASTER, getMasterState());
        } catch (CmsException | IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            if (m_descContent != null) {
                m_descContent = null;
            }
            if (m_descFile != null) {
                m_descFile = null;
            }
            if (m_desc != null) {
                try {
                    m_cms.deleteResource(m_desc, CmsResourceDeleteMode.valueOf(1));
                } catch (CmsException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
                m_desc = null;
            }
            m_hasMasterMode = false;
            m_container = oldContainer;
            return false;
        }
        m_removeDescriptorOnCancel = true;
        return true;
    }

    /**
     * When the descriptor was added while editing, but the change was not saved, it has to be removed
     * when the editor is closed.
     * @throws CmsException thrown when deleting the descriptor resource fails
     */
    public void deleteDescriptorIfNecessary() throws CmsException {

        if (m_removeDescriptorOnCancel && (m_desc != null)) {
            m_cms.deleteResource(m_desc, CmsResourceDeleteMode.valueOf(2));
        }

    }

    /** Returns the a set with all keys that are used at least in one translation.
     * @return the a set with all keys that are used at least in one translation.
     */
    public Set<String> getAllUsedKeys() {

        return m_keyset.getKeySet();
    }

    /** Returns the type of the currently edited bundle.
     * @return the type of the currently edited bundle.
     */
    public BundleType getBundleType() {

        return m_bundleType;
    }

    /**
     * Returns the configured bundle, or the provided default bundle.
     * @param defaultMessages the default bundle
     * @param locale the preferred locale
     * @return the configured bundle or, if not found, the default bundle.
     */
    public ConfigurableMessages getConfigurableMessages(CmsMessages defaultMessages, Locale locale) {

        return new ConfigurableMessages(defaultMessages, locale, m_configuredBundle);

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
     * Returns the editable columns for the current edit mode.
     * @return the editable columns for the current edit mode.
     */
    public List<TableProperty> getEditableColumns() {

        return m_editorState.get(m_editMode).getEditableColumns();
    }

    /**
     * Returns the editable columns for the provided edit mode.
     * @param mode the edit mode.
     * @return the editable columns for the provided edit mode.
     */
    public List<TableProperty> getEditableColumns(CmsMessageBundleEditorTypes.EditMode mode) {

        return m_editorState.get(mode).getEditableColumns();
    }

    /**
     * Returns the site path for the edited bundle file.
     *
     * @return the site path for the edited bundle file.
     */
    public String getEditedFilePath() {

        switch (getBundleType()) {
            case DESCRIPTOR:
                return m_cms.getSitePath(m_desc);
            case PROPERTY:
                return null != m_bundleFiles.get(getLocale())
                ? m_cms.getSitePath(m_bundleFiles.get(getLocale()).getFile())
                : m_cms.getSitePath(m_resource);
            case XML:
                return m_cms.getSitePath(m_resource);
            default:
                throw new IllegalArgumentException();
        }
    }

    /** Returns the current edit mode.
     * @return the current edit mode.
     */
    public CmsMessageBundleEditorTypes.EditMode getEditMode() {

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

    /**
     * Returns a flag, indicating if the locale has been switched on opening.
     * @return a flag, indicating if the locale has been switched on opening.
     */
    public boolean getSwitchedLocaleOnOpening() {

        return m_switchedLocaleOnOpening;
    }

    /**
     * Returns a flag, indicating if the descriptor specifies any default values.
     * @return flag, indicating if the descriptor specifies any default values.
     */
    public boolean hasDefaultValues() {

        return m_hasDefault;
    }

    /**
     * Returns a flag, indicating if the descriptor specifies any descriptions.
     * @return a flag, indicating if the descriptor specifies any descriptions.
     */
    public boolean hasDescriptionValues() {

        return m_hasDescription;
    }

    /** Returns a flag, indicating if a bundle descriptor is present.
     * @return flag, indicating if a bundle descriptor is present.
     */
    public boolean hasDescriptor() {

        return !m_bundleType.equals(CmsMessageBundleEditorTypes.BundleType.DESCRIPTOR) && (m_descContent != null);
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
    public boolean isShowOptionsColumn(CmsMessageBundleEditorTypes.EditMode mode) {

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
     * @return flag, indicating if the mode could be changed.
     */
    public boolean setEditMode(CmsMessageBundleEditorTypes.EditMode mode) {

        try {
            if ((mode == CmsMessageBundleEditorTypes.EditMode.MASTER) && (null == m_descFile)) {
                m_descFile = LockedFile.lockResource(m_cms, m_desc);
            }
            m_editMode = mode;
        } catch (@SuppressWarnings("unused") CmsException e) {
            return false;
        }
        return true;
    }

    /**
     * Set the currently edited locale.
     *
     * @param locale the currently edited locale.
     * @throws CmsException  thrown if reading a bundle resource fails.
     * @throws IOException thrown if reading a bundle resource fails.
     */
    public void setLocale(Locale locale) throws IOException, CmsException {

        adjustExistingContainer(locale);
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
            f.unlock();
        }
        if (null != m_descFile) {
            m_descFile.unlock();
        }
    }

    /**
     * Adjusts the locale for an already existing container by first saving the translation for the current locale and the loading the values of the new locale.
     *
     * @param locale the locale for which the container should be adjusted.
     * @throws IOException thrown if a bundle resource must be read and reading fails.
     * @throws CmsException thrown if a bundle resource must be read and reading fails.
     */
    private void adjustExistingContainer(Locale locale) throws IOException, CmsException {

        saveLocalization();
        replaceValues(locale);

    }

    /**
     * Creates a descriptor for the bundle in the same folder where the bundle files are located.
     * @throws CmsException thrown if creation fails.
     */
    private void createAndLockDescriptorFile() throws CmsException {

        String sitePath = m_sitepath + m_basename + CmsMessageBundleEditorTypes.Descriptor.POSTFIX;
        m_desc = m_cms.createResource(
            sitePath,
            OpenCms.getResourceManager().getResourceType(CmsMessageBundleEditorTypes.BundleType.DESCRIPTOR.toString()));
        m_descFile = LockedFile.lockResource(m_cms, m_desc);
        m_descFile.setCreated(true);
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

        if (m_bundleType.equals(CmsMessageBundleEditorTypes.BundleType.DESCRIPTOR)) {
            container = createContainerForDescriptorEditing();
        } else {
            if (hasDescriptor()) {
                container = createContainerForBundleWithDescriptor();
            } else {
                container = createContainerForBundleWithoutDescriptor();
            }
        }
        container.setItemSorter(new DefaultItemSorter(new CmsCaseInsensitivePropertyValueComparator()));
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
        container.addContainerProperty(TableProperty.KEY, String.class, "");
        container.addContainerProperty(TableProperty.DESCRIPTION, String.class, "");
        container.addContainerProperty(TableProperty.DEFAULT, String.class, "");
        container.addContainerProperty(TableProperty.TRANSLATION, String.class, "");

        // add entries
        Map<String, String> localization = getLocalization(m_locale);
        CmsXmlContentValueSequence messages = m_descContent.getValueSequence(Descriptor.N_MESSAGE, Descriptor.LOCALE);
        String descValue;
        boolean hasDescription = false;
        String defaultValue;
        boolean hasDefault = false;
        for (int i = 0; i < messages.getElementCount(); i++) {

            String prefix = messages.getValue(i).getPath() + "/";
            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            String key = m_descContent.getValue(prefix + Descriptor.N_KEY, Descriptor.LOCALE).getStringValue(m_cms);
            item.getItemProperty(TableProperty.KEY).setValue(key);
            String translation = localization.get(key);
            item.getItemProperty(TableProperty.TRANSLATION).setValue(null == translation ? "" : translation);
            descValue = m_descContent.getValue(prefix + Descriptor.N_DESCRIPTION, Descriptor.LOCALE).getStringValue(
                m_cms);
            item.getItemProperty(TableProperty.DESCRIPTION).setValue(descValue);
            hasDescription = hasDescription || !descValue.isEmpty();
            defaultValue = m_descContent.getValue(prefix + Descriptor.N_DEFAULT, Descriptor.LOCALE).getStringValue(
                m_cms);
            item.getItemProperty(TableProperty.DEFAULT).setValue(defaultValue);
            hasDefault = hasDefault || !defaultValue.isEmpty();
        }

        m_hasDefault = hasDefault;
        m_hasDescription = hasDescription;
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
        container.addContainerProperty(TableProperty.KEY, String.class, "");
        container.addContainerProperty(TableProperty.TRANSLATION, String.class, "");

        // add entries
        Map<String, String> localization = getLocalization(m_locale);
        Set<String> keySet = m_keyset.getKeySet();
        for (String key : keySet) {

            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            item.getItemProperty(TableProperty.KEY).setValue(key);
            String translation = localization.get(key);
            item.getItemProperty(TableProperty.TRANSLATION).setValue(null == translation ? "" : translation);
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
        container.addContainerProperty(TableProperty.KEY, String.class, "");
        container.addContainerProperty(TableProperty.DESCRIPTION, String.class, "");
        container.addContainerProperty(TableProperty.DEFAULT, String.class, "");

        // add entries
        CmsXmlContentValueSequence messages = m_descContent.getValueSequence(
            "/" + Descriptor.N_MESSAGE,
            Descriptor.LOCALE);
        for (int i = 0; i < messages.getElementCount(); i++) {

            String prefix = messages.getValue(i).getPath() + "/";
            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            String key = m_descContent.getValue(prefix + Descriptor.N_KEY, Descriptor.LOCALE).getStringValue(m_cms);
            item.getItemProperty(TableProperty.KEY).setValue(key);
            item.getItemProperty(TableProperty.DESCRIPTION).setValue(
                m_descContent.getValue(prefix + Descriptor.N_DESCRIPTION, Descriptor.LOCALE).getStringValue(m_cms));
            item.getItemProperty(TableProperty.DEFAULT).setValue(
                m_descContent.getValue(prefix + Descriptor.N_DEFAULT, Descriptor.LOCALE).getStringValue(m_cms));
        }

        return container;

    }

    /**
     * Creates the default editor state for editing a bundle with descriptor.
     * @return the default editor state for editing a bundle with descriptor.
     */
    private EditorState getDefaultState() {

        List<TableProperty> cols = new ArrayList<TableProperty>(1);
        cols.add(TableProperty.TRANSLATION);

        return new EditorState(cols, false);
    }

    /**
     * Reads the current properties for a language. If not already done, the properties are read from the respective file.
     * @param locale the locale for which the localization should be returned.
     * @return the properties.
     * @throws IOException thrown if reading the properties from a file fails.
     * @throws CmsException thrown if reading the properties from a file fails.
     */
    private Map<String, String> getLocalization(Locale locale) throws IOException, CmsException {

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
     * Returns the master mode's editor state for editing a bundle with descriptor.
     * @return the master mode's editor state for editing a bundle with descriptor.
     */
    private EditorState getMasterState() {

        List<TableProperty> cols = new ArrayList<TableProperty>(4);
        cols.add(TableProperty.KEY);
        cols.add(TableProperty.DESCRIPTION);
        cols.add(TableProperty.DEFAULT);
        cols.add(TableProperty.TRANSLATION);
        return new EditorState(cols, true);
    }

    /**
     * Init the bundle type member variable.
     * @return the bundle type of the opened resource.
     */
    private CmsMessageBundleEditorTypes.BundleType initBundleType() {

        String resourceTypeName = OpenCms.getResourceManager().getResourceType(m_resource).getTypeName();
        return CmsMessageBundleEditorTypes.BundleType.toBundleType(resourceTypeName);
    }

    /**
     * Reads the bundle descriptor, sets m_desc and m_descContent.
     * @throws CmsXmlException thrown when unmarshalling fails.
     * @throws CmsException thrown when reading the resource fails or several bundle descriptors for the bundle exist.
     */
    private void initDescriptor() throws CmsXmlException, CmsException {

        if (m_bundleType.equals(CmsMessageBundleEditorTypes.BundleType.DESCRIPTOR)) {
            m_desc = m_resource;
            m_descFile = LockedFile.lockResource(m_cms, m_desc);
        } else {
            m_desc = CmsMessageBundleEditorTypes.getDescriptor(m_cms, m_basename);
        }

        unmarshalDescriptor();

    }

    /**
     * Initializes the editor states for the different modes, depending on the type of the opened file.
     */
    private void initEditorStates() {

        m_editorState = new HashMap<CmsMessageBundleEditorTypes.EditMode, EditorState>();
        List<TableProperty> cols = null;
        switch (m_bundleType) {
            case PROPERTY:
            case XML:
                if (hasDescriptor()) { // bundle descriptor is present, keys are not editable in default mode, maybe master mode is available
                    m_editorState.put(CmsMessageBundleEditorTypes.EditMode.DEFAULT, getDefaultState());
                    if (hasMasterMode()) { // the bundle descriptor is editable
                        m_editorState.put(CmsMessageBundleEditorTypes.EditMode.MASTER, getMasterState());
                    }
                } else { // no bundle descriptor given - implies no master mode
                    cols = new ArrayList<TableProperty>(1);
                    cols.add(TableProperty.KEY);
                    cols.add(TableProperty.TRANSLATION);
                    m_editorState.put(CmsMessageBundleEditorTypes.EditMode.DEFAULT, new EditorState(cols, true));
                }
                break;
            case DESCRIPTOR:
                cols = new ArrayList<TableProperty>(3);
                cols.add(TableProperty.KEY);
                cols.add(TableProperty.DESCRIPTION);
                cols.add(TableProperty.DEFAULT);
                m_editorState.put(CmsMessageBundleEditorTypes.EditMode.DEFAULT, new EditorState(cols, true));
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
     * Initializes the key set for property bundles. That requires to load all localizations, i.e., all the files making up the bundle.
     *
     * @throws CmsLoaderException thrown if loading a bundle file fails.
     * @throws CmsException thrown if loading a bundle file fails.
     * @throws IOException thrown if loading a bundle file fails.
     */
    private void initKeySetForPropertyBundle() throws CmsLoaderException, CmsException, IOException {

        Collection<CmsResource> translations = m_cms.readResources(
            m_sitepath,
            CmsResourceFilter.DEFAULT.addRequireType(
                OpenCms.getResourceManager().getResourceType(
                    CmsMessageBundleEditorTypes.BundleType.PROPERTY.toString())));
        for (CmsResource resource : translations) {
            String baseName = resource.getName();
            String localeSuffix = CmsStringUtil.getLocaleSuffixForName(baseName);
            if ((null != localeSuffix) && !localeSuffix.isEmpty()) {
                baseName = baseName.substring(
                    0,
                    baseName.lastIndexOf(localeSuffix) - (1 /* cut off trailing underscore, too*/));
            }
            if (baseName.equals(m_basename)) {
                Properties props = new Properties();
                props.load(new ByteArrayInputStream(m_cms.readFile(resource).getContents()));
                m_keyset.updateKeySet(null, propertiesToMap(props).keySet());
            }
        }
    }

    /**
     * Initialization required for editing an xml bundle.
     */
    private void initKeySetForXmlBundle() {

        for (Locale l : m_xmlBundle.getLocales()) {
            Set<String> keys = new HashSet<String>();
            for (I_CmsXmlContentValue msg : m_xmlBundle.getValueSequence("Message", l).getValues()) {
                String msgpath = msg.getPath();
                keys.add(m_xmlBundle.getStringValue(m_cms, msgpath + "/Key", l));
            }
            m_keyset.updateKeySet(null, keys);
        }

    }

    /**
     * Initializes the locales that can be selected via the language switcher in the bundle editor.
     * @return the locales for which keys can be edited.
     */
    private Collection<Locale> initLocales() {

        Collection<Locale> locales = null;
        switch (m_bundleType) {
            case DESCRIPTOR:
                locales = new ArrayList<Locale>(1);
                locales.add(Descriptor.LOCALE);
                break;
            case XML:
            case PROPERTY:
                locales = OpenCms.getLocaleManager().getAvailableLocales(m_cms, m_resource);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return locales;

    }

    /**
     * Initialization necessary for editing a property bundle.
     *
     * @throws CmsLoaderException thrown if loading a bundle file fails.
     * @throws CmsException thrown if loading a bundle file fails.
     * @throws IOException thrown if loading a bundle file fails.
     */
    private void initPropertyBundle() throws CmsLoaderException, CmsException, IOException {

        initKeySetForPropertyBundle();

    }

    /**
     * Unmarshals the XML content and adds the file to the bundle files.
     * @throws CmsException thrown if reading the file or unmarshalling fails.
     */
    private void initXmlBundle() throws CmsException {

        LockedFile bundleFile = LockedFile.lockResource(m_cms, m_resource);
        m_bundleFiles.put(null, bundleFile);
        m_xmlBundle = CmsXmlContentFactory.unmarshal(m_cms, bundleFile.getFile());
        initKeySetForXmlBundle();

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
            if (!OpenCms.getResourceManager().getResourceType(resource).getTypeName().equals(
                CmsMessageBundleEditorTypes.BundleType.PROPERTY.toString())) {
                throw new CmsException(
                    new CmsMessageContainer(
                        Messages.get(),
                        Messages.ERR_RESOURCE_HAS_WRONG_TYPE_2,
                        locale.getDisplayName(),
                        resource.getRootPath()));
            }
            file = LockedFile.lockResource(m_cms, resource);
        } else {
            CmsResource res = m_cms.createResource(
                sitePath,
                OpenCms.getResourceManager().getResourceType(
                    CmsMessageBundleEditorTypes.BundleType.PROPERTY.toString()));
            file = LockedFile.lockResource(m_cms, res);
            file.setCreated(true);
        }
        m_bundleFiles.put(locale, file);
        Properties props = new Properties();
        props.load(new InputStreamReader(new ByteArrayInputStream(file.getFile().getContents()), file.getEncoding()));
        Map<String, String> properties = propertiesToMap(props);
        m_localizations.put(locale, properties);

    }

    /**
     * Loads the localization for the current locale from a bundle of type xmlvfsbundle.
     * It assumes, the content has already been unmarshalled before.
     * @param locale the locale for which the localization should be loaded
     */
    private void loadLocalizationFromXmlBundle(Locale locale) {

        CmsXmlContentValueSequence messages = m_xmlBundle.getValueSequence("Message", locale);
        Map<String, String> props = new HashMap<String, String>();
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
     * @param locale the locale for which translations should be loaded.
     * @throws IOException thrown if loading the localization from a bundle resource fails.
     * @throws CmsException thrown if loading the localization from a bundle resource fails.
     */
    private void replaceValues(Locale locale) throws IOException, CmsException {

        Map<String, String> localization = getLocalization(locale);
        if (hasDescriptor()) {
            for (Object itemId : m_container.getItemIds()) {
                Item item = m_container.getItem(itemId);
                String key = item.getItemProperty(TableProperty.KEY).getValue().toString();
                String value = localization.get(key);
                item.getItemProperty(TableProperty.TRANSLATION).setValue(null == value ? "" : value);
            }
        } else {
            m_container.removeAllItems();
            Set<String> keyset = m_keyset.getKeySet();
            for (String key : keyset) {
                Object itemId = m_container.addItem();
                Item item = m_container.getItem(itemId);
                item.getItemProperty(TableProperty.KEY).setValue(key);
                String value = localization.get(key);
                item.getItemProperty(TableProperty.TRANSLATION).setValue(null == value ? "" : value);
            }
            if (m_container.getItemIds().isEmpty()) {
                m_container.addItem();
            }
        }
    }

    /**
     * Saves the current translations from the container to the respective localization.
     */
    private void saveLocalization() {

        Map<String, String> localization = new HashMap<String, String>();
        for (Object itemId : m_container.getItemIds()) {
            Item item = m_container.getItem(itemId);
            String key = item.getItemProperty(TableProperty.KEY).getValue().toString();
            String value = item.getItemProperty(TableProperty.TRANSLATION).getValue().toString();
            if (!(key.isEmpty() || value.isEmpty())) {
                localization.put(key, value);
            }
        }
        m_keyset.updateKeySet(m_localizations.get(m_locale).keySet(), localization.keySet());
        m_localizations.put(m_locale, localization);

    }

    /**
     * Save the values to the bundle descriptor.
     * @throws CmsException thrown if saving fails.
     */
    private void saveToBundleDescriptor() throws CmsException {

        m_removeDescriptorOnCancel = false;
        updateBundleDescriptorContent();
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
            Map<String, String> props = m_localizations.get(l);
            if (null != props) {
                StringBuffer content = new StringBuffer();
                for (String key : props.keySet()) {
                    if (!key.isEmpty()) {
                        String value = props.get(key);
                        if (!value.isEmpty()) {
                            content.append(key).append("=").append(value).append("\n");
                        }
                    }
                }
                LockedFile f = m_bundleFiles.get(l);
                byte[] contentBytes;
                try {
                    contentBytes = content.toString().getBytes(f.getEncoding());
                } catch (UnsupportedEncodingException e) {
                    LOG.error(
                        m_messages.key(
                            Messages.ERR_READING_FILE_UNSUPPORTED_ENCODING_2,
                            f.getFile().getRootPath(),
                            f.getEncoding()),
                        e);
                    contentBytes = content.toString().getBytes();
                }
                CmsFile file = f.getFile();
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
            Map<String, String> props = m_localizations.get(l);
            if (null != props) {
                if (m_xmlBundle.hasLocale(l)) {
                    m_xmlBundle.removeLocale(l);
                }
                m_xmlBundle.addLocale(m_cms, l);
                int i = 0;
                for (String key : props.keySet()) {
                    if (!key.isEmpty()) {
                        String value = props.get(key);
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
        switch (CmsMessageBundleEditorTypes.BundleType.toBundleType(
            OpenCms.getResourceManager().getResourceType(m_resource).getTypeName())) {
            case PROPERTY:
                String localeSuffix = CmsStringUtil.getLocaleSuffixForName(baseName);
                if ((null != localeSuffix) && !localeSuffix.isEmpty()) {
                    baseName = baseName.substring(
                        0,
                        baseName.lastIndexOf(localeSuffix) - (1 /* cut off trailing underscore, too*/));
                    m_locale = CmsLocaleManager.getLocale(localeSuffix);
                }
                if ((null == m_locale) || !m_locales.contains(m_locale)) {
                    m_switchedLocaleOnOpening = true;
                    m_locale = m_locales.iterator().next();
                }
                break;
            case XML:
                m_locale = OpenCms.getLocaleManager().getBestAvailableLocaleForXmlContent(
                    m_cms,
                    m_resource,
                    m_xmlBundle);
                break;
            case DESCRIPTOR:
                m_basename = baseName.substring(
                    0,
                    baseName.length() - CmsMessageBundleEditorTypes.Descriptor.POSTFIX.length());
                m_locale = new Locale("en");
                break;
            default:
                throw new IllegalArgumentException(
                    Messages.get().container(
                        Messages.ERR_UNSUPPORTED_BUNDLE_TYPE_1,
                        CmsMessageBundleEditorTypes.BundleType.toBundleType(
                            OpenCms.getResourceManager().getResourceType(m_resource).getTypeName())).toString());
        }
        m_basename = baseName;

    }

    /**
     * Unmarshals the descriptor content.
     *
     * @throws CmsXmlException thrown if the XML structure of the descriptor is wrong.
     * @throws CmsException thrown if reading the descriptor file fails.
     */
    private void unmarshalDescriptor() throws CmsXmlException, CmsException {

        if (null != m_desc) {

            // unmarshall descriptor
            m_descContent = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(m_desc));

            // configure messages if wanted
            CmsProperty bundleProp = m_cms.readPropertyObject(m_desc, PROPERTY_BUNDLE_DESCRIPTOR_LOCALIZATION, true);
            if (!(bundleProp.isNullProperty() || bundleProp.getValue().trim().isEmpty())) {
                m_configuredBundle = bundleProp.getValue();
            }
        }

    }

    /**
     * Update the descriptor content with values from the editor.
     * @throws CmsXmlException thrown if update fails due to a wrong XML structure (should never happen)
     */
    private void updateBundleDescriptorContent() throws CmsXmlException {

        if (m_descContent.hasLocale(Descriptor.LOCALE)) {
            m_descContent.removeLocale(Descriptor.LOCALE);
        }
        m_descContent.addLocale(m_cms, Descriptor.LOCALE);
        String key;
        Property<Object> descProp;
        String desc;
        Property<Object> defaultValueProp;
        String defaultValue;
        int i = 0;
        for (Object itemId : m_container.getItemIds()) {
            Item item = m_container.getItem(itemId);
            key = item.getItemProperty(TableProperty.KEY).getValue().toString();
            if (!key.isEmpty()) {
                m_descContent.addValue(m_cms, Descriptor.N_MESSAGE, Descriptor.LOCALE, i);
                i++;
                String messagePrefix = Descriptor.N_MESSAGE + "[" + i + "]/";

                m_descContent.getValue(messagePrefix + Descriptor.N_KEY, Descriptor.LOCALE).setStringValue(m_cms, key);

                descProp = item.getItemProperty(TableProperty.DESCRIPTION);
                if ((null != descProp) && (null != descProp.getValue())) {
                    desc = descProp.getValue().toString();
                    m_descContent.getValue(messagePrefix + Descriptor.N_DESCRIPTION, Descriptor.LOCALE).setStringValue(
                        m_cms,
                        desc);
                }

                defaultValueProp = item.getItemProperty(TableProperty.DEFAULT);
                if ((null != defaultValueProp) && (null != defaultValueProp.getValue())) {
                    defaultValue = defaultValueProp.getValue().toString();
                    m_descContent.getValue(messagePrefix + Descriptor.N_DEFAULT, Descriptor.LOCALE).setStringValue(
                        m_cms,
                        defaultValue);
                }
            }
        }

    }
}
