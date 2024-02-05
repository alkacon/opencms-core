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

package org.opencms.ui;

import org.opencms.ade.galleries.CmsSiteSelectorOptionBuilder;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.configuration.preferences.CmsLanguagePreference;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.file.types.A_CmsResourceTypeFolderBase;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.user.CmsOUHandler;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.logging.Log;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.Version;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Panel;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Vaadin utility functions.<p>
 *
 */
@SuppressWarnings("deprecation")
public final class CmsVaadinUtils {

    /**
     * Helper class for building option groups.<p>
     */
    public static class OptionGroupBuilder {

        /** The option group being built. */
        private OptionGroup m_optionGroup = new OptionGroup();

        /**
         * Adds an option.<p>
         *
         * @param key the option key
         * @param text the option text
         *
         * @return this instance
         */
        public OptionGroupBuilder add(String key, String text) {

            m_optionGroup.addItem(key);
            m_optionGroup.setItemCaption(key, text);
            return this;
        }

        /**
         * Returns the option group.<p>
         *
         * @return the option group
         */
        public OptionGroup build() {

            return m_optionGroup;
        }

        /**
         * Adds horizontal style to option group.<p>
         *
         * @return this instance
         */
        public OptionGroupBuilder horizontal() {

            m_optionGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
            return this;
        }
    }

    /** Container property ids. */
    public static enum PropertyId {
        /** The caption id. */
        caption,
        /** The icon id. */
        icon,
        /** The is folder id. */
        isFolder,
        /** The is XML content id. */
        isXmlContent
    }

    /** Container filter for the resource type container to show not folder types only. */
    public static final Filter FILTER_NO_FOLDERS = new Filter() {

        private static final long serialVersionUID = 1L;

        public boolean appliesToProperty(Object propertyId) {

            return PropertyId.isFolder.equals(propertyId);
        }

        public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {

            return !((Boolean)item.getItemProperty(PropertyId.isFolder).getValue()).booleanValue();
        }
    };

    /** Container filter for the resource type container to show XML content types only. */
    public static final Filter FILTER_XML_CONTENTS = new Filter() {

        private static final long serialVersionUID = 1L;

        public boolean appliesToProperty(Object propertyId) {

            return PropertyId.isXmlContent.equals(propertyId);
        }

        public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {

            return ((Boolean)item.getItemProperty(PropertyId.isXmlContent).getValue()).booleanValue();
        }
    };

    /** The combo box label item property id. */
    public static final String PROPERTY_LABEL = "label";

    /** The combo box value item property id. */
    public static final String PROPERTY_VALUE = "value";

    /** The Vaadin bootstrap script, with some macros to be dynamically replaced later. */
    protected static final String BOOTSTRAP_SCRIPT = "vaadin.initApplication(\"%(elementId)\", {\n"
        + "        \"browserDetailsUrl\": \"%(vaadinServlet)\",\n"
        + "        \"serviceUrl\": \"%(vaadinServlet)\",\n"
        + "        \"widgetset\": \"org.opencms.ui.WidgetSet\",\n"
        + "        \"theme\": \"opencms\",\n"
        + "        \"versionInfo\": {\"vaadinVersion\": \"%(vaadinVersion)\"},\n"
        + "        \"vaadinDir\": \"%(vaadinDir)\",\n"
        + "        \"heartbeatInterval\": 30,\n"
        + "        \"debug\": false,\n"
        + "        \"standalone\": false,\n"
        + "        \"authErrMsg\": {\n"
        + "            \"message\": \"Take note of any unsaved data, \"+\n"
        + "                       \"and <u>click here<\\/u> to continue.\",\n"
        + "            \"caption\": \"Authentication problem\"\n"
        + "        },\n"
        + "        \"comErrMsg\": {\n"
        + "            \"message\": \"Take note of any unsaved data, \"+\n"
        + "                       \"and <u>click here<\\/u> to continue.\",\n"
        + "            \"caption\": \"Communication problem\"\n"
        + "        },\n"
        + "        \"sessExpMsg\": {\n"
        + "            \"message\": \"Take note of any unsaved data, \"+\n"
        + "                       \"and <u>click here<\\/u> to continue.\",\n"
        + "            \"caption\": \"Session Expired\"\n"
        + "        }\n"
        + "    });";

    /** The logger of this class. */
    private static final Log LOG = CmsLog.getLog(CmsVaadinUtils.class);

    /**
     * Hidden default constructor for utility class.<p>
     */
    private CmsVaadinUtils() {

    }

    /**
     * Builds a container for use in combo boxes from a map of key/value pairs, where the keys are options and the values are captions.<p>
     *
     * @param captionProperty the property name to use for captions
     * @param map the map
     * @return the new container
     */
    public static IndexedContainer buildContainerFromMap(String captionProperty, Map<String, String> map) {

        IndexedContainer container = new IndexedContainer();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            container.addItem(entry.getKey()).getItemProperty(captionProperty).setValue(entry.getValue());
        }
        return container;
    }

    /**
     * Centers the parent window of given component.<p>
     *
     * @param component Component as child of window
     */
    public static void centerWindow(Component component) {

        Window window = getWindow(component);
        if (window != null) {
            window.center();
        }
    }

    /**
     * Closes the window containing the given component.
     *
     * @param component a component
     */
    public static void closeWindow(Component component) {

        Window window = getWindow(component);
        if (window != null) {
            window.close();
        }
    }

    /**
     * Creates a click listener which calls a Runnable when activated.<p>
     *
     * @param action the Runnable to execute on a click
     *
     * @return the click listener
     */
    public static Button.ClickListener createClickListener(final Runnable action) {

        return new Button.ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                action.run();
            }
        };
    }

    /**
     * Simple context menu handler for multi-select tables.
     *
     * @param table the table
     * @param menu the table's context menu
     * @param event the click event
     * @param entries the context menu entries
     */
    @SuppressWarnings("unchecked")
    public static <T> void defaultHandleContextMenuForMultiselect(
        Table table,
        CmsContextMenu menu,
        ItemClickEvent event,
        List<I_CmsSimpleContextMenuEntry<Collection<T>>> entries) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {
            if (event.getButton().equals(MouseButton.RIGHT)) {
                Collection<T> oldValue = ((Collection<T>)table.getValue());
                if (oldValue.isEmpty() || !oldValue.contains(event.getItemId())) {
                    table.setValue(new HashSet<Object>(Arrays.asList(event.getItemId())));
                }
                Collection<T> selection = (Collection<T>)table.getValue();
                menu.setEntries(entries, selection);
                menu.openForTable(event, table);
            }
        }

    }

    /**
     * Reads the content of an input stream into a string (using UTF-8 encoding), performs a function on the string, and returns the result
     * again as an input stream.<p>
     *
     * @param stream the stream producing the input data
     * @param transformation the function to apply to the input
     *
     * @return the stream producing the transformed input data
     */
    public static InputStream filterUtf8ResourceStream(InputStream stream, Function<String, String> transformation) {

        try {
            byte[] streamData = CmsFileUtil.readFully(stream);
            String dataAsString = new String(streamData, "UTF-8");
            byte[] transformedData = transformation.apply(dataAsString).getBytes("UTF-8");
            return new ByteArrayInputStream(transformedData);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all groups with blacklist.<p>
     *
     * @param cms CmsObject
     * @param ouFqn ou name
     * @param propCaption property
     * @param propIcon property for icon
     * @param propOu organizational unit
     * @param blackList blacklist
     * @param iconProvider the icon provider
     * @return indexed container
     */
    public static IndexedContainer getAvailableGroupsContainerWithout(
        CmsObject cms,
        String ouFqn,
        String propCaption,
        String propIcon,
        String propOu,
        List<CmsGroup> blackList,
        java.util.function.Function<CmsGroup, CmsCssIcon> iconProvider) {

        if (blackList == null) {
            blackList = new ArrayList<CmsGroup>();
        }
        IndexedContainer res = new IndexedContainer();
        res.addContainerProperty(propCaption, String.class, "");
        res.addContainerProperty(propOu, String.class, "");
        if (propIcon != null) {
            res.addContainerProperty(propIcon, CmsCssIcon.class, null);
        }
        try {
            for (CmsGroup group : OpenCms.getRoleManager().getManageableGroups(cms, ouFqn, true)) {
                if (!blackList.contains(group)) {
                    Item item = res.addItem(group);
                    if (item == null) {
                        continue;
                    }
                    if (iconProvider != null) {
                        item.getItemProperty(propIcon).setValue(iconProvider.apply(group));
                    }
                    item.getItemProperty(propCaption).setValue(group.getSimpleName());
                    item.getItemProperty(propOu).setValue(group.getOuFqn());
                }
            }

        } catch (CmsException e) {
            LOG.error("Unable to read groups", e);
        }
        return res;
    }

    /**
     * Returns the available projects.<p>
     *
     * @param cms the CMS context
     *
     * @return the available projects
     */
    public static List<CmsProject> getAvailableProjects(CmsObject cms) {

        // get all project information
        List<CmsProject> allProjects;
        try {
            String ouFqn = "";
            CmsUserSettings settings = new CmsUserSettings(cms);
            if (!settings.getListAllProjects()) {
                ouFqn = cms.getRequestContext().getCurrentUser().getOuFqn();
            }
            allProjects = new ArrayList<CmsProject>(
                OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, ouFqn, settings.getListAllProjects()));
            Iterator<CmsProject> itProjects = allProjects.iterator();
            while (itProjects.hasNext()) {
                CmsProject prj = itProjects.next();
                if (prj.isHiddenFromSelector()) {
                    itProjects.remove();
                }
            }
        } catch (CmsException e) {
            // should usually never happen
            LOG.error(e.getLocalizedMessage(), e);
            allProjects = Collections.emptyList();
        }
        return allProjects;
    }

    /**
     * Builds an IndexedContainer containing the sites selectable by the current user.<p>
     *
     * @param cms the CMS context
     * @param captionPropertyName the name of the property used to store captions
     *
     * @return the container with the available sites
     */
    public static IndexedContainer getAvailableSitesContainer(CmsObject cms, String captionPropertyName) {

        IndexedContainer availableSites = new IndexedContainer();
        availableSites.addContainerProperty(captionPropertyName, String.class, null);
        for (Map.Entry<String, String> entry : getAvailableSitesMap(cms).entrySet()) {
            Item siteItem = availableSites.addItem(entry.getKey());
            siteItem.getItemProperty(captionPropertyName).setValue(entry.getValue());
        }
        return availableSites;
    }

    /**
     * Gets available sites as a LinkedHashMap, with site roots as keys and site labels as values.
     *
     * @param cms the current CMS context
     * @return the map of available sites
     */
    public static LinkedHashMap<String, String> getAvailableSitesMap(CmsObject cms) {

        CmsSiteSelectorOptionBuilder optBuilder = new CmsSiteSelectorOptionBuilder(cms);
        optBuilder.addNormalSites(true, (new CmsUserSettings(cms)).getStartFolder());
        optBuilder.addSharedSite();
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        for (CmsSiteSelectorOption option : optBuilder.getOptions()) {
            result.put(option.getSiteRoot(), option.getMessage());
        }
        String currentSiteRoot = cms.getRequestContext().getSiteRoot();
        if (!result.containsKey(currentSiteRoot)) {
            result.put(currentSiteRoot, currentSiteRoot);
        }
        return result;
    }

    /**
     * Returns the Javascript code to use for initializing a Vaadin UI.<p>
     *
     * @param cms the CMS context
     * @param elementId the id of the DOM element in which to initialize the UI
     * @param servicePath the UI servlet path
     * @return the Javascript code to initialize Vaadin
     *
     * @throws Exception if something goes wrong
     */
    public static String getBootstrapScript(CmsObject cms, String elementId, String servicePath) throws Exception {

        String script = BOOTSTRAP_SCRIPT;
        CmsMacroResolver resolver = new CmsMacroResolver();
        String context = OpenCms.getSystemInfo().getContextPath();
        String vaadinDir = CmsStringUtil.joinPaths(context, "VAADIN/");
        String vaadinVersion = Version.getFullVersion();
        String vaadinServlet = CmsStringUtil.joinPaths(context, servicePath);
        String vaadinBootstrap = CmsStringUtil.joinPaths(context, "VAADIN/vaadinBootstrap.js");
        resolver.addMacro("vaadinDir", vaadinDir);
        resolver.addMacro("vaadinVersion", vaadinVersion);
        resolver.addMacro("elementId", elementId);
        resolver.addMacro("vaadinServlet", vaadinServlet);
        resolver.addMacro("vaadinBootstrap", vaadinBootstrap);
        script = resolver.resolveMacros(script);
        return script;

    }

    /**
     * Returns the path to the design template file of the given component.<p>
     *
     * @param component the component
     *
     * @return the path
     */
    public static String getDefaultDesignPath(Component component) {

        String className = component.getClass().getName();
        String designPath = className.replace(".", "/") + ".html";
        return designPath;
    }

    /**
     * Gets container with alls groups of a certain user.
     *
     * @param cms cmsobject
     * @param user to find groups for
     * @param caption caption property
     * @param iconProp property
     * @param ou ou
     * @param propStatus status property
     * @param iconProvider the icon provider
     * @return Indexed Container
     */
    public static IndexedContainer getGroupsOfUser(
        CmsObject cms,
        CmsUser user,
        String caption,
        String iconProp,
        String ou,
        String propStatus,
        Function<CmsGroup, CmsCssIcon> iconProvider) {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(caption, String.class, "");
        container.addContainerProperty(ou, String.class, "");
        container.addContainerProperty(propStatus, Boolean.class, Boolean.valueOf(true));
        if (iconProvider != null) {
            container.addContainerProperty(iconProp, CmsCssIcon.class, null);
        }
        try {
            for (CmsGroup group : cms.getGroupsOfUser(user.getName(), true)) {
                Item item = container.addItem(group);
                item.getItemProperty(caption).setValue(group.getSimpleName());
                item.getItemProperty(ou).setValue(group.getOuFqn());
                if (iconProvider != null) {
                    item.getItemProperty(iconProp).setValue(iconProvider.apply(group));
                }
            }
        } catch (CmsException e) {
            LOG.error("Unable to read groups from user", e);
        }
        return container;
    }

    /**
     * Creates a layout with info panel.<p>
     *
     * @param messageString Message to be displayed
     * @return layout
     */
    public static VerticalLayout getInfoLayout(String messageString) {

        VerticalLayout ret = new VerticalLayout();
        ret.setMargin(true);
        ret.addStyleName("o-center");
        ret.setWidth("100%");
        VerticalLayout inner = new VerticalLayout();
        inner.addStyleName("o-workplace-maxwidth");
        Panel panel = new Panel();
        panel.setWidth("100%");

        Label label = new Label(CmsVaadinUtils.getMessageText(messageString));
        label.addStyleName("o-report");
        panel.setContent(label);

        inner.addComponent(panel);
        ret.addComponent(inner);
        return ret;
    }

    /**
     * Get container with languages.<p>
     *
     * @param captionPropertyName name
     * @return indexed container
     */
    public static IndexedContainer getLanguageContainer(String captionPropertyName) {

        IndexedContainer result = new IndexedContainer();
        result.addContainerProperty(captionPropertyName, String.class, "");

        Iterator<Locale> itLocales = OpenCms.getLocaleManager().getAvailableLocales().iterator();
        while (itLocales.hasNext()) {
            Locale locale = itLocales.next();
            Item item = result.addItem(locale);
            item.getItemProperty(captionPropertyName).setValue(locale.getDisplayName(A_CmsUI.get().getLocale()));
        }

        return result;

    }

    /**
     * Gets the message for the current locale and the given key and arguments.<p>
     *
     * @param messages the messages instance
     * @param key the message key
     * @param args the message arguments
     *
     * @return the message text for the current locale
     */
    public static String getMessageText(I_CmsMessageBundle messages, String key, Object... args) {

        return messages.getBundle(A_CmsUI.get().getLocale()).key(key, args);
    }

    /**
     * Gets the workplace message for the current locale and the given key and arguments.<p>
     *
     * @param key the message key
     * @param args the message arguments
     *
     * @return the message text for the current locale
     */
    public static String getMessageText(String key, Object... args) {

        return getWpMessagesForCurrentLocale().key(key, args);
    }

    /**
     * Creates the ComboBox for OU selection.<p>
     * @param cms CmsObject
     * @param baseOu OU
     * @param log Logger object
     *
     * @return ComboBox
     */
    public static ComboBox getOUComboBox(CmsObject cms, String baseOu, Log log) {

        return getOUComboBox(cms, baseOu, log, true);
    }

    /**
     * Creates the ComboBox for OU selection.<p>
     * @param cms CmsObject
     * @param baseOu OU
     * @param log Logger object
     * @param includeWebOU include webou?
     *
     * @return ComboBox
     */
    public static ComboBox getOUComboBox(CmsObject cms, String baseOu, Log log, boolean includeWebOU) {

        ComboBox combo = null;
        try {
            IndexedContainer container = new IndexedContainer();
            container.addContainerProperty("desc", String.class, "");
            for (String ou : CmsOUHandler.getManagableOUs(cms)) {
                if (includeWebOU | !OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou).hasFlagWebuser()) {
                    Item item = container.addItem(ou);
                    if (ou == "") {
                        CmsOrganizationalUnit root = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");
                        item.getItemProperty("desc").setValue(root.getDisplayName(A_CmsUI.get().getLocale()));
                    } else {
                        item.getItemProperty("desc").setValue(
                            OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou).getDisplayName(
                                A_CmsUI.get().getLocale()));
                    }
                }
            }
            combo = new ComboBox(null, container);
            combo.setTextInputAllowed(true);
            combo.setNullSelectionAllowed(false);
            combo.setWidth("379px");
            combo.setInputPrompt(
                Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
            combo.setItemCaptionPropertyId("desc");

            combo.setFilteringMode(FilteringMode.CONTAINS);

            combo.select(baseOu);

        } catch (CmsException e) {
            if (log != null) {
                log.error("Unable to read OU", e);
            }
        }
        return combo;
    }

    /**
     * Gives item id from path.<p>
     *
     * @param cnt to be used
     * @param path to obtain item id from
     * @return item id
     */
    public static String getPathItemId(Container cnt, String path) {

        for (String id : Arrays.asList(path, CmsFileUtil.toggleTrailingSeparator(path))) {
            if (cnt.containsId(id)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Get container for principal.
     *
     * @param cms cmsobject
     * @param list of principals
     * @param captionID caption id
     * @param descID description id
     * @param iconID icon id
     * @param ouID ou id
     * @param icon icon
     * @param iconList iconlist
     * @return indexedcontainer
     */
    public static IndexedContainer getPrincipalContainer(
        CmsObject cms,
        List<? extends I_CmsPrincipal> list,
        String captionID,
        String descID,
        String iconID,
        String ouID,
        String icon,
        List<FontIcon> iconList) {

        IndexedContainer res = new IndexedContainer();

        res.addContainerProperty(captionID, String.class, "");
        res.addContainerProperty(ouID, String.class, "");
        res.addContainerProperty(iconID, FontIcon.class, new CmsCssIcon(icon));
        if (descID != null) {
            res.addContainerProperty(descID, String.class, "");
        }

        for (I_CmsPrincipal group : list) {

            Item item = res.addItem(group);
            item.getItemProperty(captionID).setValue(group.getSimpleName());
            item.getItemProperty(ouID).setValue(group.getOuFqn());
            if (descID != null) {
                item.getItemProperty(descID).setValue(group.getDescription(A_CmsUI.get().getLocale()));
            }
        }

        for (int i = 0; i < iconList.size(); i++) {
            res.getItem(res.getIdByIndex(i)).getItemProperty(iconID).setValue(iconList.get(i));
        }

        return res;
    }

    /**
     * Returns the selectable projects container.<p>
     *
     * @param cms the CMS context
     * @param captionPropertyName the name of the property used to store captions
     *
     * @return the projects container
     */
    public static IndexedContainer getProjectsContainer(CmsObject cms, String captionPropertyName) {

        IndexedContainer result = new IndexedContainer();
        result.addContainerProperty(captionPropertyName, String.class, null);
        for (Map.Entry<CmsUUID, String> entry : getProjectsMap(cms).entrySet()) {
            Item projectItem = result.addItem(entry.getKey());
            projectItem.getItemProperty(captionPropertyName).setValue(entry.getValue());
        }
        return result;
    }

    /**
     * Gets the available projects for the current user as a map, wth project ids as keys and project names as values.
     *
     * @param cms the current CMS context
     * @return the map of projects
     */
    public static LinkedHashMap<CmsUUID, String> getProjectsMap(CmsObject cms) {

        Locale locale = A_CmsUI.get().getLocale();
        List<CmsProject> projects = getAvailableProjects(cms);
        boolean isSingleOu = isSingleOu(projects);
        LinkedHashMap<CmsUUID, String> result = new LinkedHashMap<>();
        for (CmsProject project : projects) {
            String projectName = project.getSimpleName();
            if (!isSingleOu && !project.isOnlineProject()) {
                try {
                    projectName = projectName
                        + " - "
                        + OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, project.getOuFqn()).getDisplayName(
                            locale);
                } catch (CmsException e) {
                    LOG.debug("Error reading project OU.", e);
                    projectName = projectName + " - " + project.getOuFqn();
                }
            }
            result.put(project.getUuid(), projectName);
        }
        return result;

    }

    /**
     * Gets the current Vaadin request, cast to a HttpServletRequest.<p>
     *
     * @return the current request
     */
    public static HttpServletRequest getRequest() {

        return (HttpServletRequest)VaadinService.getCurrentRequest();
    }

    /**
     * Gets list of resource types.<p>
     *
     * @return List
     */
    public static List<I_CmsResourceType> getResourceTypes() {

        List<I_CmsResourceType> res = new ArrayList<I_CmsResourceType>();
        for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
            CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                type.getTypeName());
            if (typeSetting != null) {
                res.add(type);
            }
        }
        return res;
    }

    /**
     * Returns the available resource types container.<p>
     *
     * @return the resource types container
     */
    public static IndexedContainer getResourceTypesContainer() {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(PropertyId.caption, String.class, null);
        container.addContainerProperty(PropertyId.icon, Resource.class, null);
        container.addContainerProperty(PropertyId.isFolder, Boolean.class, null);
        container.addContainerProperty(PropertyId.isXmlContent, Boolean.class, null);
        List<I_CmsResourceType> types = getResourceTypes();
        sortResourceTypes(types);
        for (I_CmsResourceType type : types) {
            CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                type.getTypeName());
            Item typeItem = container.addItem(type);
            String caption = CmsVaadinUtils.getMessageText(typeSetting.getKey()) + " (" + type.getTypeName() + ")";
            typeItem.getItemProperty(PropertyId.caption).setValue(caption);
            typeItem.getItemProperty(PropertyId.icon).setValue(CmsResourceUtil.getSmallIconResource(typeSetting, null));
            typeItem.getItemProperty(PropertyId.isXmlContent).setValue(
                Boolean.valueOf(type instanceof CmsResourceTypeXmlContent));
            typeItem.getItemProperty(PropertyId.isFolder).setValue(
                Boolean.valueOf(type instanceof A_CmsResourceTypeFolderBase));
        }

        return container;
    }

    /**
     * Returns the roles available for a given user.<p>
     *
     * @param cms CmsObject
     * @param user to get available roles for
     * @param captionPropertyName name of caption property
     * @return indexed container
     */
    public static IndexedContainer getRoleContainerForUser(CmsObject cms, CmsUser user, String captionPropertyName) {

        IndexedContainer result = new IndexedContainer();
        result.addContainerProperty(captionPropertyName, String.class, "");
        try {
            List<CmsRole> roles = OpenCms.getRoleManager().getRoles(cms, user.getOuFqn(), false);
            CmsRole.applySystemRoleOrder(roles);
            for (CmsRole role : roles) {
                Item item = result.addItem(role);
                item.getItemProperty(captionPropertyName).setValue(role.getDisplayName(cms, A_CmsUI.get().getLocale()));
            }
        } catch (CmsException e) {
            LOG.error("Unabel to read roles for user", e);
        }
        return result;
    }

    /**
     * Gets the window which contains a given component.<p>
     *
     * @param component the component
     * @return the window containing the component, or null if no component is found
     */
    public static Window getWindow(Component component) {

        if (component == null) {
            return null;
        } else if (component instanceof Window) {
            return (Window)component;
        } else {
            return getWindow(component.getParent());
        }

    }

    /**
     * Get container with workpalce languages.<p>
     *
     * @param captionPropertyName name
     * @return indexed container
     */
    public static IndexedContainer getWorkplaceLanguageContainer(String captionPropertyName) {

        IndexedContainer result = new IndexedContainer();
        result.addContainerProperty(captionPropertyName, String.class, "");
        CmsLanguagePreference.getOptionMapForLanguage().forEach((locale, title) -> {
            Item item = result.addItem(locale);
            item.getItemProperty(captionPropertyName).setValue(title);

        });

        return result;
    }

    /**
     * Gets the link to the (new) workplace.<p>
     *
     * @return the link to the workplace
     */
    public static String getWorkplaceLink() {

        return OpenCms.getSystemInfo().getWorkplaceContext();
    }

    /**
     * Returns the workplace link for the given app.<p>
     *
     * @param appId the app id
     *
     * @return the workplace link
     */
    public static String getWorkplaceLink(String appId) {

        return getWorkplaceLink() + CmsAppWorkplaceUi.WORKPLACE_APP_ID_SEPARATOR + appId;
    }

    /**
     * Returns the workplace link to the given app with the given state.<p>
     *
     * @param appId the app id
     * @param appState the app state
     *
     * @return the workplace link
     */
    public static String getWorkplaceLink(String appId, String appState) {

        return getWorkplaceLink(appId) + CmsAppWorkplaceUi.WORKPLACE_STATE_SEPARATOR + appState;
    }

    /**
     * Returns the workplace link to the given app with the given state including the given request parameters.<p>
     *
     * @param appId the app id
     * @param appState the app state
     * @param requestParameters the request parameters
     *
     * @return the workplace link
     */
    public static String getWorkplaceLink(String appId, String appState, Map<String, String[]> requestParameters) {

        String result = getWorkplaceLink();
        if ((requestParameters != null) && !requestParameters.isEmpty()) {
            boolean first = true;
            for (Entry<String, String[]> param : requestParameters.entrySet()) {
                for (String value : param.getValue()) {
                    if (first) {
                        result += "?";
                    } else {
                        result += "&";
                    }
                    result += param.getKey() + "=" + value;
                    first = false;
                }
            }
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(appId)) {
            result += CmsAppWorkplaceUi.WORKPLACE_APP_ID_SEPARATOR + appId;
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(appState)) {
            result += CmsAppWorkplaceUi.WORKPLACE_STATE_SEPARATOR + appState;
        }
        return result;
    }

    /**
     * Gets external resource from workplace resource folder.<p>
     *
     * @param subPath path relative to workplace resource folder
     *
     * @return the external resource
     */
    public static ExternalResource getWorkplaceResource(String subPath) {

        return new ExternalResource(CmsWorkplace.getResourceUri(subPath));

    }

    /**
     * Gets the workplace messages for the current locale.<p>
     *
     * @return the workplace messages
     */
    public static CmsMessages getWpMessagesForCurrentLocale() {

        Locale locale;
        if (A_CmsUI.get() != null) {
            locale = A_CmsUI.get().getLocale();
        } else {
            if (LOG.isWarnEnabled()) {
                Exception e = new Exception("getWpMessagesForCurrentLocale called from non-Vaadin context");
                LOG.warn(e.getLocalizedMessage(), e);
            }
            locale = Locale.ENGLISH;
        }
        return OpenCms.getWorkplaceManager().getMessages(locale);
    }

    /**
     * Checks if path is itemid in container.<p>
     *
     * @param cnt to be checked
     * @param path as itemid
     * @return true id path is itemid in container
     */
    public static boolean hasPathAsItemId(Container cnt, String path) {

        return cnt.containsId(path) || cnt.containsId(CmsFileUtil.toggleTrailingSeparator(path));
    }

    /**
     * Checks if a button is pressed.<p>
     *
     * @param button the button
     *
     * @return true if the button is pressed
     */
    public static boolean isButtonPressed(Button button) {

        if (button == null) {
            return false;
        }
        List<String> styles = Arrays.asList(button.getStyleName().split(" "));

        return styles.contains(OpenCmsTheme.BUTTON_PRESSED);
    }

    /**
     * Uses the currently set locale to resolve localization macros in the input string using workplace message bundles.<p>
     *
     * @param baseString the string to localize
     *
     * @return the localized string
     */
    public static String localizeString(String baseString) {

        if (baseString == null) {
            return null;
        }
        CmsWorkplaceMessages wpMessages = OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale());
        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setMessages(wpMessages);
        String result = resolver.resolveMacros(baseString);
        return result;
    }

    /**
     * Message accessior function.<p>
     *
     * @return the message for Cancel buttons
     */
    public static String messageCancel() {

        return getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0);
    }

    /**
     * Message accessior function.<p>
     *
     * @return the message for Cancel buttons
     */
    public static String messageClose() {

        return getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CLOSE_0);
    }

    /**
     * Message accessor function.<p>
     *
     * @return the message for OK buttons
     */
    public static String messageOk() {

        return getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0);
    }

    /**
     * Generates the options items for the combo box using the map entry keys as values and the values as labels.<p>
     *
     * @param box the combo box to prepare
     * @param options the box options
     */
    public static void prepareComboBox(ComboBox box, Map<?, String> options) {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(PROPERTY_VALUE, Object.class, null);
        container.addContainerProperty(PROPERTY_LABEL, String.class, "");
        for (Entry<?, String> entry : options.entrySet()) {
            Item item = container.addItem(entry.getKey());
            item.getItemProperty(PROPERTY_VALUE).setValue(entry.getKey());
            item.getItemProperty(PROPERTY_LABEL).setValue(entry.getValue());
        }
        box.setContainerDataSource(container);
        box.setItemCaptionPropertyId(PROPERTY_LABEL);
    }

    /**
     * Reads the declarative design for a component and localizes it using a messages object.<p>
     *
     * The design will need to be located in the same directory as the component's class and have '.html' as a file extension.
     *
     * @param component the component for which to read the design
     * @param messages the message bundle to use for localization
     * @param macros the macros to use on the HTML template
     */
    @SuppressWarnings("resource")
    public static void readAndLocalizeDesign(Component component, CmsMessages messages, Map<String, String> macros) {

        Class<?> componentClass = component.getClass();
        List<Class<?>> classes = Lists.newArrayList();
        classes.add(componentClass);
        classes.addAll(ClassUtils.getAllSuperclasses(componentClass));
        InputStream designStream = null;
        for (Class<?> cls : classes) {
            if (cls.getName().startsWith("com.vaadin")) {
                break;
            }
            String filename = cls.getSimpleName() + ".html";
            designStream = cls.getResourceAsStream(filename);
            if (designStream != null) {
                break;
            }

        }
        if (designStream == null) {
            throw new IllegalArgumentException("Design not found for : " + component.getClass());
        }
        readAndLocalizeDesign(component, designStream, messages, macros);
    }

    /**
     * Reads a layout from a resource, applies basic i18n macro substitution on the contained text, and returns a stream of the transformed
     * data.<p>
     *
     * @param layoutClass the class relative to which the layout resource will be looked up
     * @param relativeName the file name of the layout file
     *
     * @return an input stream which produces the transformed layout resource html
     */
    public static InputStream readCustomLayout(Class<? extends Component> layoutClass, String relativeName) {

        CmsMacroResolver resolver = new CmsMacroResolver() {

            @Override
            public String getMacroValue(String macro) {

                return CmsEncoder.escapeXml(super.getMacroValue(macro));
            }
        };
        resolver.setMessages(CmsVaadinUtils.getWpMessagesForCurrentLocale());
        InputStream layoutStream = CmsVaadinUtils.filterUtf8ResourceStream(
            layoutClass.getResourceAsStream(relativeName),
            resolver.toFunction());
        return layoutStream;
    }

    /**
     * Replaces component with new component.<p>
     *
     * @param component to be replaced
     * @param replacement new component
     */
    public static void replaceComponent(Component component, Component replacement) {

        if (!component.isAttached()) {
            throw new IllegalArgumentException("Component must be attached");
        }
        HasComponents parent = component.getParent();
        if (parent instanceof ComponentContainer) {
            ((ComponentContainer)parent).replaceComponent(component, replacement);
        } else if (parent instanceof SingleComponentContainer) {
            ((SingleComponentContainer)parent).setContent(replacement);
        } else {
            throw new IllegalArgumentException("Illegal class for parent: " + parent.getClass());
        }
    }

    /**
     * Configures a text field to look like a filter box for a table.
     *
     * @param searchBox the text field to configure
     */
    public static void setFilterBoxStyle(TextField searchBox) {

        searchBox.setIcon(FontOpenCms.FILTER);

        searchBox.setPlaceholder(
            org.opencms.ui.apps.Messages.get().getBundle(UI.getCurrent().getLocale()).key(
                org.opencms.ui.apps.Messages.GUI_EXPLORER_FILTER_0));
        searchBox.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
    }

    /**
     * Sets the value of a text field which may be set to read-only mode.<p>
     *
     * When setting a Vaadin field to read-only, you also can't set its value programmatically anymore.
     * So we need to temporarily disable read-only mode, set the value, and then switch back to read-only mode.
     *
     * @param field the field
     * @param value the value to set
     */
    public static <T> void setReadonlyValue(AbstractField<T> field, T value) {

        boolean readonly = field.isReadOnly();
        try {
            field.setReadOnly(false);
            field.setValue(value);
        } finally {
            field.setReadOnly(readonly);
        }
    }

    /**
     * Shows an alert box to the user with the given information, which will perform the given action after the user clicks on OK.<p>
     *
     * @param title the title
     * @param message the message
     *
     * @param callback the callback to execute after clicking OK
     */
    public static void showAlert(String title, String message, final Runnable callback) {

        final Window window = new Window();
        window.setModal(true);
        Panel panel = new Panel();
        panel.setCaption(title);
        panel.setWidth("500px");
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        panel.setContent(layout);
        layout.addComponent(new Label(message));
        Button okButton = new Button();
        okButton.addClickListener(new ClickListener() {

            /** The serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                window.close();
                if (callback != null) {
                    callback.run();
                }
            }
        });
        layout.addComponent(okButton);
        layout.setComponentAlignment(okButton, Alignment.BOTTOM_RIGHT);
        okButton.setCaption(
            org.opencms.workplace.Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        window.setContent(panel);
        window.setClosable(false);
        window.setResizable(false);
        A_CmsUI.get().addWindow(window);

    }

    /**
     * Sorts a list of resource types by their localized explorer type name.
     * @param resourceTypes the resource types
     */
    public static void sortResourceTypes(List<I_CmsResourceType> resourceTypes) {

        Collections.sort(resourceTypes, (type, other) -> {
            CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                type.getTypeName());
            CmsExplorerTypeSettings otherSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                other.getTypeName());
            if ((typeSetting != null) && (otherSetting != null)) {
                String typeName = CmsVaadinUtils.getMessageText(typeSetting.getKey());
                String otherName = CmsVaadinUtils.getMessageText(otherSetting.getKey());
                return typeName.compareTo(otherName);
            } else {
                return -1;
            }
        });
    }

    /**
     * Creates a new option group builder.<p>
     *
     * @return a new option group builder
     */
    public static OptionGroupBuilder startOptionGroup() {

        return new OptionGroupBuilder();
    }

    /**
     * Sets style of a toggle button depending on its current state.<p>
     *
     * @param button the button to update
     */
    public static void toggleButton(Button button) {

        if (isButtonPressed(button)) {
            button.removeStyleName(OpenCmsTheme.BUTTON_PRESSED);
        } else {
            button.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
        }
    }

    /**
     * Updates the component error of a component, but only if it differs from the currently set
     * error.<p>
     *
     * @param component the component
     * @param error the error
     *
     * @return true if the error was changed
     */
    public static boolean updateComponentError(AbstractComponent component, ErrorMessage error) {

        if (component.getComponentError() != error) {
            component.setComponentError(error);
            return true;
        }
        return false;
    }

    /**
     * Visits all descendants of a given component (including the component itself) and applies a predicate
     * to each.<p>
     *
     * If the predicate returns false for a component, no further descendants will be processed.<p>
     *
     * @param component the component
     * @param handler the predicate
     */
    public static void visitDescendants(Component component, Predicate<Component> handler) {

        List<Component> stack = Lists.newArrayList();
        stack.add(component);
        while (!stack.isEmpty()) {
            Component currentComponent = stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
            if (!handler.apply(currentComponent)) {
                return;
            }
            if (currentComponent instanceof HasComponents) {
                List<Component> children = Lists.newArrayList((HasComponents)currentComponent);
                Collections.reverse(children);
                stack.addAll(children);
            }
        }
    }

    /**
     * Waggle the component.<p>
     *
     * @param component to be waggled
     */
    public static void waggleMeOnce(Component component) {

        //TODO Until now, the component gets a waggler class which can not be removed again here..
        component.addStyleName("waggler");
        //Add JavaScript code, which adds the waggle class and removes it after a short time.
        JavaScript.getCurrent().execute(
            "waggler=document.querySelectorAll(\".waggler\")[0];"
                + "waggler.className=waggler.className + \" waggle\";"
                + "setTimeout(function () {\n"
                + "waggler.className=waggler.className.replace(/\\bwaggle\\b/g, \"\");"
                + "    }, 1500);");
    }

    /**
     * Reads the given design and resolves the given macros and localizations.<p>
    
     * @param component the component whose design to read
     * @param designStream stream to read the design from
     * @param messages the message bundle to use for localization in the design (may be null)
     * @param macros other macros to substitute in the macro design (may be null)
     */
    protected static void readAndLocalizeDesign(
        Component component,
        InputStream designStream,
        CmsMessages messages,
        Map<String, String> macros) {

        try {
            byte[] designBytes = CmsFileUtil.readFully(designStream, true);
            final String encoding = "UTF-8";
            String design = new String(designBytes, encoding);
            CmsMacroResolver resolver = new CmsMacroResolver() {

                @Override
                public String getMacroValue(String macro) {

                    String result = super.getMacroValue(macro);
                    // The macro may contain quotes or angle brackets, so we need to escape the values for insertion into the design file
                    return CmsEncoder.escapeXml(result);

                }
            };

            if (macros != null) {
                for (Map.Entry<String, String> entry : macros.entrySet()) {
                    resolver.addMacro(entry.getKey(), entry.getValue());
                }
            }
            if (messages != null) {
                resolver.setMessages(messages);
            }
            String resolvedDesign = resolver.resolveMacros(design);
            Design.read(new ByteArrayInputStream(resolvedDesign.getBytes(encoding)), component);
        } catch (IOException e) {
            throw new RuntimeException("Could not read design", e);
        } finally {
            try {
                designStream.close();
            } catch (IOException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Returns whether only a single OU is visible to the current user.<p>
     *
     * @param projects the selectable projects
     *
     * @return <code>true</code> if only a single OU is visible to the current user
     */
    private static boolean isSingleOu(List<CmsProject> projects) {

        String ouFqn = null;
        for (CmsProject project : projects) {
            if (project.isOnlineProject()) {
                // skip the online project
                continue;
            }
            if (ouFqn == null) {
                // set the first ou
                ouFqn = project.getOuFqn();
            } else if (!ouFqn.equals(project.getOuFqn())) {
                // break if one different ou is found
                return false;
            }
        }
        return true;
    }

}
