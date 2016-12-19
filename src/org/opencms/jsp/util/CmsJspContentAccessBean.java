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

package org.opencms.jsp.util;

import org.opencms.ade.contenteditor.CmsContentService;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsConstantMap;
import org.opencms.util.CmsUUID;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.page.CmsXmlPageFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/**
 * Allows access to the individual elements of an XML content, usually used inside a loop of a
 * <code>&lt;cms:contentload&gt;</code> tag.<p>
 *
 * The implementation is optimized for performance and uses lazy initializing of the
 * requested values as much as possible.<p>
 *
 * @since 7.0.2
 *
 * @see org.opencms.jsp.CmsJspTagContentAccess
 */
public class CmsJspContentAccessBean {

    /**
     * Provides Booleans that indicate if a specified locale is available in the XML content,
     * the input is assumed to be a String that represents a Locale.<p>
     */
    public class CmsHasLocaleTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            return Boolean.valueOf(getRawContent().hasLocale(CmsJspElFunctions.convertLocale(input)));
        }
    }

    /**
     * Provides Booleans that indicate if a specified path exists in the XML content,
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsHasLocaleValueTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Locale locale = CmsJspElFunctions.convertLocale(input);
            Map<String, Boolean> result;
            if (getRawContent().hasLocale(locale)) {
                result = CmsCollectionsGenericWrapper.createLazyMap(new CmsHasValueTransformer(locale));
            } else {
                result = CmsConstantMap.CONSTANT_BOOLEAN_FALSE_MAP;
            }
            return result;
        }
    }

    /**
     * Provides a Map with Booleans that indicate if a specified path exists in the XML content in the selected Locale,
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsHasValueTransformer implements Transformer {

        /** The selected locale. */
        private Locale m_selectedLocale;

        /**
         * Constructor with a locale.<p>
         *
         * @param locale the locale to use
         */
        public CmsHasValueTransformer(Locale locale) {

            m_selectedLocale = locale;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            return Boolean.valueOf(getRawContent().hasValue(String.valueOf(input), m_selectedLocale));
        }
    }

    /**
     * Transformer used for the 'imageDnd' EL attribute which is used to annotate images which can be replaced by drag and drop.<p>
     */
    public class CmsImageDndTransformer implements Transformer {

        /**
         * Creates a new instance.<p>
         */
        public CmsImageDndTransformer() {

            // do nothing

        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            String result;
            if (CmsJspContentAccessValueWrapper.isDirectEditEnabled(getCmsObject())) {
                result = createImageDndAttr(
                    getRawContent().getFile().getStructureId(),
                    String.valueOf(input),
                    String.valueOf(getLocale()));
            } else {
                result = "";
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user access the list of element names from the selected locale in an XML content,
     * the input is assumed to be a String that represents a Locale.<p>
     */
    public class CmsLocaleNamesTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Locale locale = CmsLocaleManager.getLocale(String.valueOf(input));

            return getRawContent().getNames(locale);
        }
    }

    /**
     * Provides a Map which lets the user access the RDFA tags for all values in the selected locale in an XML content,
     * the input is assumed to be a String that represents a Locale.<p>
     */
    public class CmsLocaleRdfaTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Locale locale = CmsLocaleManager.getLocale(String.valueOf(input));
            Map<String, String> result;
            if (getRawContent().hasLocale(locale)) {
                result = CmsCollectionsGenericWrapper.createLazyMap(new CmsRdfaTransformer(locale));
            } else {
                // return a map that always returns an empty string
                result = CmsConstantMap.CONSTANT_EMPTY_STRING_MAP;
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user access sub value Lists from the selected locale in an XML content,
     * the input is assumed to be a String that represents a Locale.<p>
     */
    public class CmsLocaleSubValueListTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Locale locale = CmsJspElFunctions.convertLocale(input);
            Map<String, List<CmsJspContentAccessValueWrapper>> result;
            if (getRawContent().hasLocale(locale)) {
                result = CmsCollectionsGenericWrapper.createLazyMap(new CmsSubValueListTransformer(locale));
            } else {
                result = CmsConstantMap.CONSTANT_EMPTY_LIST_MAP;
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user access value Lists from the selected locale in an XML content,
     * the input is assumed to be a String that represents a Locale.<p>
     */
    public class CmsLocaleValueListTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Locale locale = CmsJspElFunctions.convertLocale(input);
            Map<String, List<CmsJspContentAccessValueWrapper>> result;
            if (getRawContent().hasLocale(locale)) {
                result = CmsCollectionsGenericWrapper.createLazyMap(new CmsValueListTransformer(locale));
            } else {
                result = CmsConstantMap.CONSTANT_EMPTY_LIST_MAP;
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user access a value from the selected locale in an XML content,
     * the input is assumed to be a String that represents a Locale.<p>
     */
    public class CmsLocaleValueTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Locale locale = CmsLocaleManager.getLocale(String.valueOf(input));
            Map<String, CmsJspContentAccessValueWrapper> result;
            if (getRawContent().hasLocale(locale)) {
                result = CmsCollectionsGenericWrapper.createLazyMap(new CmsValueTransformer(locale));
            } else {
                result = CONSTANT_NULL_VALUE_WRAPPER_MAP;
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user access the RDFA tag for a value in an XML content,
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsRdfaTransformer implements Transformer {

        /** The selected locale. */
        private Locale m_selectedLocale;

        /**
         * Constructor with a locale.<p>
         *
         * @param locale the locale to use
         */
        public CmsRdfaTransformer(Locale locale) {

            m_selectedLocale = locale;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            if (CmsJspContentAccessValueWrapper.isDirectEditEnabled(getCmsObject())) {
                return CmsContentService.getRdfaAttributes(getRawContent(), m_selectedLocale, String.valueOf(input));
            } else {
                return "";
            }
        }
    }

    /**
     * Provides a Map which lets the user access sub value Lists in an XML content,
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsSubValueListTransformer implements Transformer {

        /** The selected locale. */
        private Locale m_selectedLocale;

        /**
         * Constructor with a locale.<p>
         *
         * @param locale the locale to use
         */
        public CmsSubValueListTransformer(Locale locale) {

            m_selectedLocale = locale;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            List<I_CmsXmlContentValue> values = getRawContent().getSubValues(String.valueOf(input), m_selectedLocale);
            List<CmsJspContentAccessValueWrapper> result = new ArrayList<CmsJspContentAccessValueWrapper>();
            Iterator<I_CmsXmlContentValue> i = values.iterator();
            while (i.hasNext()) {
                // XML content API offers List of values only as Objects, must iterate them and create Strings
                I_CmsXmlContentValue value = i.next();
                result.add(CmsJspContentAccessValueWrapper.createWrapper(getCmsObject(), value, null, null));
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user access value Lists in an XML content,
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsValueListTransformer implements Transformer {

        /** The selected locale. */
        private Locale m_selectedLocale;

        /**
         * Constructor with a locale.<p>
         *
         * @param locale the locale to use
         */
        public CmsValueListTransformer(Locale locale) {

            m_selectedLocale = locale;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            List<I_CmsXmlContentValue> values = getRawContent().getValues(String.valueOf(input), m_selectedLocale);
            List<CmsJspContentAccessValueWrapper> result = new ArrayList<CmsJspContentAccessValueWrapper>();
            Iterator<I_CmsXmlContentValue> i = values.iterator();
            while (i.hasNext()) {
                // XML content API offers List of values only as Objects, must iterate them and create Strings
                I_CmsXmlContentValue value = i.next();
                result.add(CmsJspContentAccessValueWrapper.createWrapper(getCmsObject(), value, null, null));
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user access a value in an XML content,
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsValueTransformer implements Transformer {

        /** The selected locale. */
        private Locale m_selectedLocale;

        /**
         * Constructor with a locale.<p>
         *
         * @param locale the locale to use
         */
        public CmsValueTransformer(Locale locale) {

            m_selectedLocale = locale;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            I_CmsXmlContentValue value = getRawContent().getValue(String.valueOf(input), m_selectedLocale);
            return CmsJspContentAccessValueWrapper.createWrapper(
                getCmsObject(),
                value,
                getRawContent(),
                (String)input,
                m_selectedLocale);
        }
    }

    /** Constant Map that always returns the {@link CmsJspContentAccessValueWrapper#NULL_VALUE_WRAPPER}.*/
    protected static final Map<String, CmsJspContentAccessValueWrapper> CONSTANT_NULL_VALUE_WRAPPER_MAP = new CmsConstantMap<String, CmsJspContentAccessValueWrapper>(
        CmsJspContentAccessValueWrapper.NULL_VALUE_WRAPPER);

    /** The OpenCms context of the current user. */
    private CmsObject m_cms;

    /** The XML content to access. */
    private I_CmsXmlDocument m_content;

    /** The lazy initialized map for the "has locale" check. */
    private Map<String, Boolean> m_hasLocale;

    /** The lazy initialized map for the "has locale value" check. */
    private Map<String, Map<String, Boolean>> m_hasLocaleValue;

    /** Lazy map for imageDnd annotations. */
    private Map<String, String> m_imageDnd;

    /** The locale used for accessing entries from the XML content, this may be a fallback default locale. */
    private Locale m_locale;

    /** The lazy initialized with the locale names. */
    private Map<String, List<String>> m_localeNames;

    /** Lazy initialized map of RDFA maps by locale. */
    private Map<String, Map<String, String>> m_localeRdfa;

    /** The lazy initialized with the locale sub value lists. */
    private Map<String, Map<String, List<CmsJspContentAccessValueWrapper>>> m_localeSubValueList;

    /** The lazy initialized with the locale value. */
    private Map<String, Map<String, CmsJspContentAccessValueWrapper>> m_localeValue;

    /** The lazy initialized with the locale value lists. */
    private Map<String, Map<String, List<CmsJspContentAccessValueWrapper>>> m_localeValueList;

    /** The original locale requested for accessing entries from the XML content. */
    private Locale m_requestedLocale;

    /** Resource the XML content is created from. */
    private CmsResource m_resource;

    /** The categories assigned to the resource. */
    private CmsJspCategoryAccessBean m_categories;

    /**
     * No argument constructor, required for a JavaBean.<p>
     *
     * You must call {@link #init(CmsObject, Locale, I_CmsXmlDocument, CmsResource)} and provide the
     * required values when you use this constructor.<p>
     *
     * @see #init(CmsObject, Locale, I_CmsXmlDocument, CmsResource)
     */
    public CmsJspContentAccessBean() {

        // must call init() manually later
    }

    /**
     * Creates a content access bean based on a Resource, using the current request context locale.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param resource the resource to create the content from
     */
    public CmsJspContentAccessBean(CmsObject cms, CmsResource resource) {

        this(cms, cms.getRequestContext().getLocale(), resource);
    }

    /**
     * Creates a content access bean based on a Resource.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param locale the Locale to use when accessing the content
     * @param resource the resource to create the content from
     */
    public CmsJspContentAccessBean(CmsObject cms, Locale locale, CmsResource resource) {

        init(cms, locale, null, resource);
    }

    /**
     * Creates a content access bean based on an XML content object.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param locale the Locale to use when accessing the content
     * @param content the content to access
     */
    public CmsJspContentAccessBean(CmsObject cms, Locale locale, I_CmsXmlDocument content) {

        init(cms, locale, content, content.getFile());
    }

    /**
     * Generates the HTML attribute "data-imagednd" that enables the ADE image drag and drop feature.<p>
     *
     * @param structureId the structure ID of the XML document to insert the image
     * @param locale the locale to generate the image in
     * @param imagePath the XML path to the image source node.
     *
     * @return the HTML attribute "data-imagednd" that enables the ADE image drag and drop feature
     */
    protected static String createImageDndAttr(CmsUUID structureId, String imagePath, String locale) {

        String attrValue = structureId + "|" + imagePath + "|" + locale;
        String escapedAttrValue = CmsEncoder.escapeXml(attrValue);
        return ("data-imagednd=\"" + escapedAttrValue + "\"");
    }

    /**
     * Returns the OpenCms user context this bean was initialized with.<p>
     *
     * @return the OpenCms user context this bean was initialized with
     */
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns the raw VFS file object the content accessed by this bean was created from.<p>
     *
     * This can be used to access information from the raw file on a JSP.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Root path of the resource: ${content.file.rootPath}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return the raw VFS file object the content accessed by this bean was created from
     */
    public CmsFile getFile() {

        return getRawContent().getFile();
    }

    /**
     * Returns the site path of the current resource, that is the result of
     * {@link CmsObject#getSitePath(CmsResource)} with the resource
     * obtained by {@link #getFile()}.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Site path of the resource: "${content.filename}";
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return the site path of the current resource
     *
     * @see CmsObject#getSitePath(CmsResource)
     */
    public String getFilename() {

        return m_cms.getSitePath(getRawContent().getFile());
    }

    /**
     * Returns a lazy initialized Map that provides Booleans that indicate if a specified Locale is available
     * in the XML content.<p>
     *
     * The provided Map key is assumed to be a String that represents a Locale.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.hasLocale['de']}" &gt;
     *         The content has a "de" Locale!
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a lazy initialized Map that provides Booleans that indicate if a specified Locale is available
     *      in the XML content
     */
    public Map<String, Boolean> getHasLocale() {

        if (m_hasLocale == null) {
            m_hasLocale = CmsCollectionsGenericWrapper.createLazyMap(new CmsHasLocaleTransformer());
        }
        return m_hasLocale;
    }

    /**
     * Returns a lazy initialized Map that provides a Map that provides Booleans that
     * indicate if a value (xpath) is available in the XML content in the selected locale.<p>
     *
     * The first provided Map key is assumed to be a String that represents the Locale,
     * the second provided Map key is assumed to be a String that represents the xpath to the value.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.hasLocaleValue['de']['Title']}" &gt;
     *         The content has a "Title" value in the "de" Locale!
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * Please note that you can also test if a locale value exists like this:<pre>
     * &lt;c:if test="${content.value['de']['Title'].exists}" &gt; ... &lt;/c:if&gt;</pre>
     *
     * @return a lazy initialized Map that provides a Map that provides Booleans that
     *      indicate if a value (xpath) is available in the XML content in the selected locale
     *
     * @see #getHasValue()
     */
    public Map<String, Map<String, Boolean>> getHasLocaleValue() {

        if (m_hasLocaleValue == null) {
            m_hasLocaleValue = CmsCollectionsGenericWrapper.createLazyMap(new CmsHasLocaleValueTransformer());
        }
        return m_hasLocaleValue;
    }

    /**
     * Returns a lazy initialized Map that provides Booleans that
     * indicate if a value (xpath) is available in the XML content in the current locale.<p>
     *
     * The provided Map key is assumed to be a String that represents the xpath to the value.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.hasValue['Title']}" &gt;
     *         The content has a "Title" value in the current locale!
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * Please note that you can also test if a value exists like this:<pre>
     * &lt;c:if test="${content.value['Title'].exists}" &gt; ... &lt;/c:if&gt;</pre>
     *
     * @return a lazy initialized Map that provides Booleans that
     *      indicate if a value (xpath) is available in the XML content in the current locale
     *
     * @see #getHasLocaleValue()
     */
    public Map<String, Boolean> getHasValue() {

        return getHasLocaleValue().get(getLocale());
    }

    /**
     * Returns the structure ID of the current resource, that is the ID of
     * the resource obtained by {@link #getFile()}.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Site path of the resource: "${content.id}";
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return the structure ID of the current resource
     *
     * @see CmsResource#getStructureId()
     */
    public CmsUUID getId() {

        return getRawContent().getFile().getStructureId();
    }

    /**
     * Gets the lazy imageDnd map.<p>
     *
     * @return the lazy imageDnd map
     */
    public Map<String, String> getImageDnd() {

        if (m_imageDnd == null) {
            m_imageDnd = CmsCollectionsGenericWrapper.createLazyMap(new CmsImageDndTransformer());
        }
        return m_imageDnd;
    }

    /**
     * Returns <code>true</code> in case the current user is allowed to edit the XML content.<p>
     *
     * If the check is performed from the online project, the user context is internally switched to an offline
     * project. So this may return <code>true</code> even if the user is currently in the online project.
     *
     * "Allowed to edit" here requires "read" and "write" permission for the VFS resource the XML content was created from.
     * It also requires that the VFS resource is not locked by another user.
     * Moreover, the user must be able to access at least one "offline" project.<p>
     *
     * Intended for quick checks to for example show / hide edit buttons for user generated content.<p>
     *
     * @return <code>true</code> in case the current user is allowed to edit the XML content
     */
    public boolean getIsEditable() {

        boolean result = false;
        try {
            CmsObject cms;
            if (m_cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                // we are in the online project, which means we must first switch to an offline project
                // otherwise write permission checks will always return false
                cms = OpenCms.initCmsObject(m_cms);
                List<CmsProject> projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(
                    cms,
                    cms.getRequestContext().getOuFqn(),
                    false);
                if ((projects != null) && (projects.size() > 0)) {
                    // there is at least one project available
                    for (CmsProject p : projects) {
                        // need to iterate because the online project will be part of the result list
                        if (!p.isOnlineProject()) {
                            cms.getRequestContext().setCurrentProject(p);
                            break;
                        }
                    }
                }
            } else {
                // not in the online project, so just use the current project
                cms = m_cms;
            }

            result = cms.hasPermissions(
                m_resource,
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            if (result) {
                // still need to check the lock status
                CmsLock lock = cms.getLock(m_resource);
                if (!lock.isLockableBy(cms.getRequestContext().getCurrentUser())) {
                    // resource is locked from a different user
                    result = false;
                }
            }
        } catch (CmsException e) {
            // should not happen, in case it does just assume not editable
        }
        return result;
    }

    /**
     * Returns the Locale this bean is using for content access, this may be a default fall back Locale.<p>
     *
     * @return the Locale this bean is using for content access, this may be a default fall back Locale
     */
    public Locale getLocale() {

        // check the content if the locale has not been set yet
        if (m_locale == null) {
            getRawContent();
        }
        return m_locale;
    }

    /**
     * Returns a lazy initialized Map that provides a List with all available elements paths (Strings)
     * used in this document in the selected locale.<p>
     *
     * The provided Map key is assumed to be a String that represents the Locale.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:forEach items="${content.localeNames['de']}" var="elem"&gt;
     *         &lt;c:out value="${elem}" /&gt;
     *     &lt;/c:forEach&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a lazy initialized Map that provides a Map that provides
     *      values from the XML content in the selected locale
     *
     * @see #getNames()
     */
    public Map<String, List<String>> getLocaleNames() {

        if (m_localeNames == null) {
            m_localeNames = CmsCollectionsGenericWrapper.createLazyMap(new CmsLocaleNamesTransformer());
        }
        return m_localeNames;
    }

    /**
     * Returns the map of RDFA maps by locale.<p>
     *
     * @return the map of RDFA maps by locale
     */
    public Map<String, Map<String, String>> getLocaleRdfa() {

        if (m_localeRdfa == null) {
            m_localeRdfa = CmsCollectionsGenericWrapper.createLazyMap(new CmsLocaleRdfaTransformer());
        }
        return m_localeRdfa;
    }

    /**
     * Returns a lazy initialized Map that provides a Map that provides Lists of direct sub values
     * from the XML content in the selected locale.<p>
     *
     * The first provided Map key is assumed to be a String that represents the Locale,
     * the second provided Map key is assumed to be a String that represents the xpath to the value.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:forEach var="item" items="${content.localeSubValueList['de']['Items']}"&gt;
     *         ${item}
     *     &lt;/c:forEach&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a lazy initialized Map that provides a Map that provides Lists of direct sub values
     *      from the XML content in the selected locale
     *
     * @see #getLocaleValue()
     */
    public Map<String, Map<String, List<CmsJspContentAccessValueWrapper>>> getLocaleSubValueList() {

        if (m_localeSubValueList == null) {
            m_localeSubValueList = CmsCollectionsGenericWrapper.createLazyMap(new CmsLocaleSubValueListTransformer());
        }
        return m_localeSubValueList;
    }

    /**
     * Returns a lazy initialized Map that provides a Map that provides
     * values from the XML content in the selected locale.<p>
     *
     * The first provided Map key is assumed to be a String that represents the Locale,
     * the second provided Map key is assumed to be a String that represents the xpath to the value.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     The Title in Locale "de": ${content.localeValue['de']['Title']}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a lazy initialized Map that provides a Map that provides
     *      values from the XML content in the selected locale
     *
     * @see #getValue()
     */
    public Map<String, Map<String, CmsJspContentAccessValueWrapper>> getLocaleValue() {

        if (m_localeValue == null) {
            m_localeValue = CmsCollectionsGenericWrapper.createLazyMap(new CmsLocaleValueTransformer());
        }
        return m_localeValue;
    }

    /**
     * Returns a lazy initialized Map that provides a Map that provides Lists of values
     * from the XML content in the selected locale.<p>
     *
     * The first provided Map key is assumed to be a String that represents the Locale,
     * the second provided Map key is assumed to be a String that represents the xpath to the value.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:forEach var="teaser" items="${content.localeValueList['de']['Teaser']}"&gt;
     *         ${teaser}
     *     &lt;/c:forEach&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a lazy initialized Map that provides a Map that provides Lists of values
     *      from the XML content in the selected locale
     *
     * @see #getLocaleValue()
     */
    public Map<String, Map<String, List<CmsJspContentAccessValueWrapper>>> getLocaleValueList() {

        if (m_localeValueList == null) {
            m_localeValueList = CmsCollectionsGenericWrapper.createLazyMap(new CmsLocaleValueListTransformer());
        }
        return m_localeValueList;
    }

    /**
     * Returns a list with all available elements paths (Strings) used in this document
     * in the current locale.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:forEach items="${content.names}" var="elem"&gt;
     *         &lt;c:out value="${elem}" /&gt;
     *     &lt;/c:forEach&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a list with all available elements paths (Strings) used in this document in the current locale
     *
     * @see #getLocaleNames()
     */
    public List<String> getNames() {

        return getLocaleNames().get(getLocale());
    }

    /**
     * Returns the raw XML content object that is accessed by this bean.<p>
     *
     * @return the raw XML content object that is accessed by this bean
     */
    public I_CmsXmlDocument getRawContent() {

        if (m_content == null) {
            // content has not been provided, must unmarshal XML first
            CmsFile file;
            try {
                file = m_cms.readFile(m_resource);
                if (CmsResourceTypeXmlPage.isXmlPage(file)) {
                    // this is an XML page
                    m_content = CmsXmlPageFactory.unmarshal(m_cms, file);
                } else {
                    // this is an XML content
                    m_content = CmsXmlContentFactory.unmarshal(m_cms, file);
                }
            } catch (CmsException e) {
                // this usually should not happen, as the resource already has been read by the current user
                // and we just upgrade it to a File
                throw new CmsRuntimeException(
                    Messages.get().container(Messages.ERR_XML_CONTENT_UNMARSHAL_1, m_resource.getRootPath()),
                    e);
            }
        }

        // make sure a valid locale is used
        if (m_locale == null) {
            m_locale = OpenCms.getLocaleManager().getBestMatchingLocale(
                m_requestedLocale,
                OpenCms.getLocaleManager().getDefaultLocales(),
                m_content.getLocales());
        }

        return m_content;
    }

    /**
     * Returns RDFA by value name map.<p>
     *
     * @return RDFA by value name map
     */
    public Map<String, String> getRdfa() {

        return getLocaleRdfa().get(getLocale());
    }

    /**
     * Reads and returns the categories assigned to the content's VFS resource.
     * @return the categories assigned to the content's VFS resource.
     */
    public CmsJspCategoryAccessBean getReadCategories() {

        if (null == m_categories) {
            m_categories = readCategories();
        }
        return m_categories;
    }

    /**
     * Returns a lazy initialized Map that provides Lists of direct sub values
     * of the given value from the XML content in the current locale.<p>
     *
     * The provided Map key is assumed to be a String that represents the xpath to the value.
     * Use this method in case you want to iterate over a List of sub values from the XML content.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:forEach var="teaser" items="${content.subValueList['Items']}"&gt;
     *         ${item}
     *     &lt;/c:forEach&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a lazy initialized Map that provides Lists of values from the XML content in the current locale
     *
     * @see #getLocaleValueList()
     */
    public Map<String, List<CmsJspContentAccessValueWrapper>> getSubValueList() {

        return getLocaleSubValueList().get(getLocale());
    }

    /**
     * Returns a lazy initialized Map that provides values from the XML content in the current locale.<p>
     *
     * The provided Map key is assumed to be a String that represents the xpath to the value.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     The Title: ${content.value['Title']}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a lazy initialized Map that provides values from the XML content in the current locale
     *
     * @see #getLocaleValue()
     */
    public Map<String, CmsJspContentAccessValueWrapper> getValue() {

        return getLocaleValue().get(getLocale());
    }

    /**
     * Returns a lazy initialized Map that provides Lists of values from the XML content in the current locale.<p>
     *
     * The provided Map key is assumed to be a String that represents the xpath to the value.
     * Use this method in case you want to iterate over a List of values form the XML content.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:forEach var="teaser" items="${content.valueList['Teaser']}"&gt;
     *         ${teaser}
     *     &lt;/c:forEach&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a lazy initialized Map that provides Lists of values from the XML content in the current locale
     *
     * @see #getLocaleValueList()
     */
    public Map<String, List<CmsJspContentAccessValueWrapper>> getValueList() {

        return getLocaleValueList().get(getLocale());
    }

    /**
     * Returns an instance of a VFS access bean,
     * initialized with the OpenCms user context this bean was created with.<p>
     *
     * @return an instance of a VFS access bean,
     *      initialized with the OpenCms user context this bean was created with
     */
    public CmsJspVfsAccessBean getVfs() {

        return CmsJspVfsAccessBean.create(m_cms);
    }

    /**
     * Initialize this instance.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param locale the Locale to use when accessing the content
     * @param content the XML content to access
     * @param resource the resource to create the content from
     */
    public void init(CmsObject cms, Locale locale, I_CmsXmlDocument content, CmsResource resource) {

        m_cms = cms;
        m_requestedLocale = locale;
        m_content = content;
        m_resource = resource;
    }

    /**
     * Reads the categories assigned to the content's VFS resource.
     * @return the categories assigned to the content's VFS resource.
     */
    private CmsJspCategoryAccessBean readCategories() {

        return new CmsJspCategoryAccessBean(getCmsObject(), m_resource);
    }
}