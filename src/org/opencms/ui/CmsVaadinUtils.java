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
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.types.A_CmsResourceTypeFolderBase;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Version;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Vaadin utility functions.<p>
 *
 */
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
    private static final Logger LOG = Logger.getLogger(CmsVaadinUtils.class);

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

        CmsSiteSelectorOptionBuilder optBuilder = new CmsSiteSelectorOptionBuilder(cms);
        optBuilder.addNormalSites(true, (new CmsUserSettings(cms)).getStartFolder());
        optBuilder.addSharedSite();
        IndexedContainer availableSites = new IndexedContainer();
        availableSites.addContainerProperty(captionPropertyName, String.class, null);
        for (CmsSiteSelectorOption option : optBuilder.getOptions()) {
            Item siteItem = availableSites.addItem(option.getSiteRoot());
            siteItem.getItemProperty(captionPropertyName).setValue(option.getMessage());
        }
        String currentSiteRoot = cms.getRequestContext().getSiteRoot();
        if (!availableSites.containsId(currentSiteRoot)) {
            availableSites.addItem(currentSiteRoot).getItemProperty(captionPropertyName).setValue(currentSiteRoot);
        }
        return availableSites;
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
        Locale locale = A_CmsUI.get().getLocale();
        List<CmsProject> projects = getAvailableProjects(cms);
        boolean isSingleOu = isSingleOu(projects);
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
            Item projectItem = result.addItem(project.getUuid());
            projectItem.getItemProperty(captionPropertyName).setValue(projectName);
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
     * Returns the available resource types container.<p>
     *
     * @return the resource types container
     */
    public static IndexedContainer getResourceTypesContainer() {

        IndexedContainer types = new IndexedContainer();
        types.addContainerProperty(PropertyId.caption, String.class, null);
        types.addContainerProperty(PropertyId.icon, Resource.class, null);
        types.addContainerProperty(PropertyId.isFolder, Boolean.class, null);
        types.addContainerProperty(PropertyId.isXmlContent, Boolean.class, null);
        for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
            CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                type.getTypeName());
            if (typeSetting != null) {
                Item typeItem = types.addItem(type);
                typeItem.getItemProperty(PropertyId.caption).setValue(
                    CmsVaadinUtils.getMessageText(typeSetting.getKey()));
                typeItem.getItemProperty(PropertyId.icon).setValue(
                    new ExternalResource(
                        CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + typeSetting.getIcon())));
                typeItem.getItemProperty(PropertyId.isXmlContent).setValue(
                    Boolean.valueOf(type instanceof CmsResourceTypeXmlContent));
                typeItem.getItemProperty(PropertyId.isFolder).setValue(
                    Boolean.valueOf(type instanceof A_CmsResourceTypeFolderBase));
            }
        }

        return types;
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
     * Gets the link to the (new) workplace.<p>
     *
     * @return the link to the workplace
     */
    public static String getWorkplaceLink() {

        return CmsStringUtil.joinPaths("/", OpenCms.getSystemInfo().getContextPath(), "workplace");
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

        return OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale());
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
     * Creates a new option group builder.<p>
     *
     * @return a new option group builder
     */
    public static OptionGroupBuilder startOptionGroup() {

        return new OptionGroupBuilder();
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
