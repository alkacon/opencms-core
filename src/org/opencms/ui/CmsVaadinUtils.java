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

package org.opencms.ui;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.declarative.Design;

/**
 * Vaadin utility functions.<p>
 *
 */
public final class CmsVaadinUtils {

    /** The logger of this class. */
    private static final Logger LOG = Logger.getLogger(CmsVaadinUtils.class);

    /**
     * Hidden default constructor for utility class.<p>
     */
    private CmsVaadinUtils() {

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
     * Builds an IndexedContainer containing the sites selectable by the current user.<p>
     *
     * @param cms the CMS context
     * @param captionPropertyName the name of the property used to store captions
     *
     * @return the container with the available sites
     */
    public static IndexedContainer getAvailableSitesContainer(CmsObject cms, String captionPropertyName) {

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(
            cms,
            true,
            true,
            cms.getRequestContext().getOuFqn());
        final IndexedContainer availableSites = new IndexedContainer();
        availableSites.addContainerProperty(captionPropertyName, String.class, null);
        Locale locale = A_CmsUI.get().getLocale();
        for (CmsSite site : sites) {
            Item siteItem = availableSites.addItem(site.getSiteRoot());
            String title = CmsWorkplace.substituteSiteTitleStatic(site.getTitle(), locale);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = site.getSiteRoot();
            }
            siteItem.getItemProperty(captionPropertyName).setValue(title);
        }
        String currentSiteRoot = cms.getRequestContext().getSiteRoot();
        if (!availableSites.containsId(currentSiteRoot)) {
            availableSites.addItem(currentSiteRoot).getItemProperty(captionPropertyName).setValue(currentSiteRoot);
        }
        return availableSites;
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
     * Reads the declarative design for a component and localizes it using a messages object.<p>
     *
     * The design will need to be located in the same directory as the component's class and have '.html' as a file extension.
     *
     * @param component the component for which to read the design
     * @param messages the message bundle to use for localization
     * @param macros the macros to use on the HTML template
     */
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
                callback.run();
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

}
