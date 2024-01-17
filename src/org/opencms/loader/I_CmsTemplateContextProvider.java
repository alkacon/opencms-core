/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for template context providers.<p>
 *
 * Implementations of this class are used to dynamically determine a template used for rendering
 * a page at runtime, e.g. there could be one template for desktop browsers and another for mobile
 * browsers.
 *
 * It is possible to override the template using a cookie containing the name of the template context
 * which should be used as a value. The cookie name is determined by the template context provider.<p>
 */
public interface I_CmsTemplateContextProvider {

    /**
     * Gets a map of all template contexts, with the template context names as keys.<p>
     *
     * @return the map of template context providers
     */
    Map<String, CmsTemplateContext> getAllContexts();

    /**
     * Customizes the label for the default template context option.
     *
     * <p>If null is returned, the default label for the default template will be used.
     *
     * If null is returned, a default
     * @param locale the locale for i18n
     * @return the label for the default template context option
     */
    default String getDefaultLabel(Locale locale) {

        return null;
    }

    /**
     * Returns the style sheet to be used for the editor.<p>
     *
     * @param cms the current CMS context
     * @param editedResourcePath the path of the edited resource
     *
     * @return the path of the style sheet to be used for the resource
     */
    String getEditorStyleSheet(CmsObject cms, String editedResourcePath);

    /**
     * Returns a set of structure ids of dynamic functions that are allowed to appear in the gallery dialog search results, or null if the dynamic function results should not be restricted.
     *
     *  <p>Note: Functions may be  excluded from the gallery dialog search results for other reasons even if they appear in the set returned by this method.
     *
     * @param cms the current CMS context
     * @param templateContext the current template context key
     * @return the set of ids of dynamic functions to allow in the gallery search results, or null if function results shouldn't be restricted
     */
    default Set<CmsUUID> getFunctionsForGallery(CmsObject cms, String templateContext) {

        return null;
    }

    /**
     * Gets the label to use for the menu entry in the given locale.
     *
     *  <p>If this returns null, the default menu entry label is used.
     *
     * @param locale the locale for which we want the menu entry label
     * @return the menu entry label
     */
    default String getMenuLabel(Locale locale) {

        return null;
    }

    /**
     * Special integer that indicates the position for the template context selection in the context menu.
     *
     * <p>Currently can only return 0 or 1.
     *
     * @return the context menu position
     */
    default int getMenuPosition() {

        return 0;
    }

    /**
     * Gets the name of the cookie which should be used for overriding the template context.<p>
     *
     * @return the name of the cookie used for overriding the template context
     */
    String getOverrideCookieName();

    /**
     * Gets the template compatibility for the given template context key.
     *
     * <p>The template compatibility controls which elements are visible as gallery search results when the template
     * with the given key is active. If the template.compatibility property on a resource is set (possibly inherited from a parent folder), but does not contain the
     * compatibility value returned by this method, it will not show up in gallery search results when the template referenced by
     * templateContextKey is active.
     *
     * @param templateContextKey the key for a template context
     * @return a template compatibility string (should not contain whitespace or punctuation)
     */
    default String getTemplateCompatibility(String templateContextKey) {

        return null;
    }

    /**
     * Determines the template context from the current CMS context, request, and resource.<p>
     *
     * @param cms the CMS context
     * @param request the current request
     * @param resource the resource being rendered
     *
     * @return the current template context
     */
    CmsTemplateContext getTemplateContext(CmsObject cms, HttpServletRequest request, CmsResource resource);

    /**
     * Initializes the context provider using a CMS object.<p>
     *
     * <p>Initialization always happens in the Online project.
     *
     * @param cms the current CMS context
     * @param config the template context provider configuration
     */
    void initialize(CmsObject cms, String config);

    /**
     * Checks if the template context should be hidden in the GUI.
     *
     * @param key the key of a template context
     * @return true if the template context should be hidden in the GUI
     */
    default boolean isHiddenContext(String key) {

        return false;
    }

    /**
     * Checks if the 'templateContexts' setting should be ignored when rendering container elements (i.e. they will be rendered by default, regardless of the setting value).
     *
     * @return true if the templateContexts setting should be ignored by the cms:container tag
     */
    default boolean isIgnoreTemplateContextsSetting() {
        return false;
    }

    /**
     * Gets the value which should be used instead of a property which was read from the template resource.<p>
     *
     * This is needed because before template context providers, it was common to store template-specific configuration in
     * a property on the template JSP. Since template context providers make the result ambiguous, this method is intended
     * as a replacement.<p>
     *
     * @param cms the CMS context to use
     * @param propertyName the name of the property
     * @param fallbackValue the value to return if no value is found or an error occurs
     *
     * @return the common property value
     *
     * @throws CmsException if something goes wrong
     */
    String readCommonProperty(CmsObject cms, String propertyName, String fallbackValue) throws CmsException;

    /**
     * Checks if the context menu option for switching the template should be shown for the given user context.
     *
     * @param cms the CmsObject to check
     * @return true if the context menu option should be visible
     */
    default boolean shouldShowContextMenuOption(CmsObject cms) {

        return true;
    }

    /**
     * Controls whether template context select options should be shown in the element settings dialog.
     *
     * @param cms the CmsObject for the current user
     * @return true if the template context select options should be shown in the element settings dialog
     */
    default boolean shouldShowElementTemplateContextSelection(CmsObject cms) {

        return true;
    }

}
