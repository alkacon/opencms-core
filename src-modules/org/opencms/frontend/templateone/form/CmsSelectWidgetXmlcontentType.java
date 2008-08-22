/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsSelectWidgetXmlcontentType.java,v $
 * Date   : $Date: 2008/08/22 13:28:43 $
 * Version: $Revision: 1.14 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.templateone.form;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsMacroResolver;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

/**
 * 
 * A select widget that recursively collects all {@link org.opencms.xml.content.CmsXmlContent} resources of a given type
 * (name) under a given path and creates select options that contain the xmlcontents field value specified by a name
 * (xpath) as display String and the xmlcontents path (given) as the value.
 * <p>
 * 
 * The configuration String has to be of the following form: <br>
 * 
 * <pre>
 *    &quot;folder=&lt;vfspath&gt;|displayOptionMacro=&lt;macro&gt;|resourcetypeName=&lt;typename&gt;|sortMacro=&lt;macro&gt;[|propertyname=propertyvalue]*
 * </pre>
 * 
 * where
 * 
 * <pre>
 *    &lt;macro&gt;
 * </pre>
 * 
 * is a String containing valid OpenCms macros or xpath expression in the form:
 * 
 * <pre>
 * &quot;You are viewing: %(property.Title) &quot;
 * </pre>
 * 
 * or
 * 
 * <pre>
 * &quot;%(xpath.Firstname) %(xpath.Lastname), Nocakla inc.&quot;
 * </pre>
 * 
 * in which the xpath macros will be replaced with {@link org.opencms.xml.A_CmsXmlDocument#getValue(String, Locale)}
 * 
 * <pre>
 *    &lt;vfspath&gt;
 * </pre>
 * 
 * is a valid resource path to a folder in the VFS where search is started from,
 * 
 * <pre>
 *    &lt;typename&gt;
 * </pre>
 * 
 * is a resource type name defined in opencms-modules.xml and
 * 
 * <pre>
 *    [|propertyname = propertyvalue]*
 * </pre>
 * 
 * is a arbitrary number of properties value mappings that have to exist on the resources to show.
 * <p>
 * 
 * 
 * <h3>Please note</h3>
 * <p>
 * <ul>
 * <li>The widget will not offer xmlcontents that are in a different locale than the current page that displays it.
 * <br>
 * Only if the "matching" xmlcontent has defined a language node for the locale that is set on the page for this widget
 * and the xpath expression to display is not empty, the xmlcontent will be selectable. </li>
 * <li>If sortMacro is missing the values will be sorted alphabetically by their resolved display option (from the
 * displayOptionMacro).</li>
 * </ul>
 * </p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.14 $
 * 
 * @since 6.1.3
 * 
 */
public class CmsSelectWidgetXmlcontentType extends CmsSelectWidget {

    /**
     * A {@link CmsSelectWidgetOption} that is bundled with a corresponding resource that may be selected.
     * <p>
     * 
     * @author Achim Westermann
     * 
     * @version $Revision: 1.14 $
     * 
     * @since 6.1.6
     * 
     */
    private static final class CmsResourceSelectWidgetOption extends CmsSelectWidgetOption {

        /** The resource to select. */
        private CmsResource m_resource;

        /**
         * Creates a non-default select option with the resource to select, the resource's name as the display text and
         * no help text.
         * <p>
         * 
         * @param resource the resource of this selection
         * 
         */
        public CmsResourceSelectWidgetOption(CmsResource resource) {

            this(resource, false);

        }

        /**
         * Creates a select option with the resource to select, the resource's name as the display text and no help text
         * that is potentially the default selection (argument isDefault).<p>
         * 
         * @param resource the resource of this selection
         * @param isDefault true, if this option is the default option (preselected)
         * 
         */
        public CmsResourceSelectWidgetOption(CmsResource resource, boolean isDefault) {

            this(resource, isDefault, resource.getName());

        }

        /**
         * 
         * Creates a select option with the resource to select, the given optionText as the display text and no help
         * text that is potentially the default selection (argument isDefault).
         * <p>
         * 
         * @param resource the resource of this selection
         * @param isDefault true, if this option is the default option (preselected)
         * @param optionText the text to display for this option
         */
        public CmsResourceSelectWidgetOption(CmsResource resource, boolean isDefault, String optionText) {

            this(resource, isDefault, optionText, null);

        }

        /**
         * Creates a select option with the resource to select, the given optionText as the display text and the given
         * help text that is potentially the default selection (argument isDefault).
         * <p>
         * 
         * @param resource the resource of this selection
         * @param isDefault true, if this option is the default option (preselected)
         * @param optionText the text to display for this option
         * @param helpText the help text to display
         */
        public CmsResourceSelectWidgetOption(CmsResource resource, boolean isDefault, String optionText, String helpText) {

            super(resource.getRootPath(), isDefault, optionText, helpText);
            m_resource = resource;

        }

        /**
         * Returns the resource that is selectable.
         * <p>
         * 
         * @return the resource that is selectable.
         */
        CmsResource getResource() {

            return m_resource;
        }

    }

    /**
     * Compares two {@link CmsSelectWidgetXmlcontentType.CmsResourceSelectWidgetOption} instances by any resource
     * related value that may be accessed via a {@link CmsMacroResolver} (except message keys).
     * <p>
     * 
     * @author Achim Westermann
     * 
     * @version $Revision: 1.14 $
     * 
     * @since 6.1.6
     * 
     */
    private static final class CmsResourceSelectWidgetOptionComparator implements Comparator, Serializable {

        /** Serial UID required for safe serialization. */
        private static final long serialVersionUID = -4078389792834878256L;

        /** The {@link CmsMacroResolver} compatible macro to resolve for comparison. */
        private String m_comparatorMacro;

        /** To access resource related values with the {@link CmsMacroResolver} for comparison. */
        private CmsObject m_macroCmsObjectInner;

        /** The {@link CmsMacroResolver} to use for macro resolvation for comparison. * */
        private CmsMacroResolver m_macroResolverInner;

        /**
         * Creates a comparator that will resolve the {@link CmsResource} related values with the given macro
         * expression.
         * <p>
         * 
         * @param cms will be cloned and used for macro - resolution
         * @param comparatorMacro the macro to use to find the resource related strings to compare.
         * 
         * @throws CmsException if something goes wrong
         * 
         * @see CmsMacroResolver
         */
        CmsResourceSelectWidgetOptionComparator(CmsObject cms, String comparatorMacro)
        throws CmsException {

            if (CmsStringUtil.isEmpty(comparatorMacro)) {
                m_comparatorMacro = I_CmsMacroResolver.MACRO_DELIMITER
                    + ""
                    + I_CmsMacroResolver.MACRO_START
                    + "opencms.filename)";
            } else {
                m_comparatorMacro = comparatorMacro;
            }
            m_macroCmsObjectInner = OpenCms.initCmsObject(cms);
            m_macroCmsObjectInner.getRequestContext().setSiteRoot("/");
            m_macroResolverInner = new CmsMacroResolver();
            m_macroResolverInner.setCmsObject(m_macroCmsObjectInner);
        }

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {

            CmsResourceSelectWidgetOption option1 = (CmsResourceSelectWidgetOption)o1;
            CmsResourceSelectWidgetOption option2 = (CmsResourceSelectWidgetOption)o2;
            CmsResource resource1 = option1.getResource();
            CmsResource resource2 = option2.getResource();

            String sort1, sort2;

            // fool the macro resolver:
            CmsRequestContext requestContext = m_macroCmsObjectInner.getRequestContext();
            requestContext.setUri(resource1.getRootPath());
            // implant the resource name for macro "%(opencms.filename):
            m_macroResolverInner.setResourceName(resource1.getName());
            sort1 = m_macroResolverInner.resolveMacros(m_comparatorMacro);
            requestContext.setUri(resource2.getRootPath());
            m_macroResolverInner.setResourceName(resource2.getName());
            sort2 = m_macroResolverInner.resolveMacros(m_comparatorMacro);
            return sort1.compareTo(sort2);
        }

    }

    /**
     * Configuration parameter for construction of the option display value by a macro containing xpath macros for the
     * xmlcontent.
     */
    public static final String CONFIGURATION_OPTION_DISPLAY_MACRO = "displayOptionMacro";

    /**
     * Configuration parameter for choosing the macro to sort the display options by.
     */
    public static final String CONFIGURATION_OPTION_SORT_MACRO = "sortMacro";

    /** Configuration parameter to set the name of the resource types to accept. */
    public static final String CONFIGURATION_RESOURCETYPENAME = "resourcetypeName";

    /** Configuration parameter to set the top folder in the VFS to search for xmlcontent resources. */
    public static final String CONFIGURATION_TOPFOLDER = "folder";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSelectWidgetXmlcontentType.class);

    /** Only used for the macro resolver to resolve macros for the collected XML contents. */
    protected CmsObject m_macroCmsObject;

    /** The macro resolver to use. */
    protected CmsMacroResolver m_macroResolver;

    /**
     * The macro to search for the display String of the options in xmlcontent files found below the folder to search
     * in.
     */
    private String m_displayOptionMacro;

    /** A map filled with properties and their values that have to exist on values to display. */
    private Map m_filterProperties;

    /** The resource folder under which the xmlcontent resources will be searched. */
    private CmsResource m_resourceFolder;

    /** The type id of xmlcontent resources to use. */
    private int m_resourceTypeID;

    /**
     * The macro that describes the {@link CmsResource} - related value to use for sorting of the select widget options.
     */
    private String m_sortMacro;

    /**
     * Creates an unconfigured widget that has to be configured by
     * {@link org.opencms.widgets.A_CmsWidget#setConfiguration(String)} before any html output API call is triggered.
     * <p>
     * 
     */
    public CmsSelectWidgetXmlcontentType() {

        this("");
    }

    /**
     * Creates an instance with the given configuration.
     * <p>
     * 
     * @param configuration see the class description for the format
     */
    public CmsSelectWidgetXmlcontentType(String configuration) {

        super(configuration);
        m_filterProperties = new HashMap();

    }

    /**
     * Returns the displayOptionXpathMacro.
     * <p>
     * 
     * @return the displayOptionXpathMacro
     */
    public String getDisplayOptionMacro() {

        return m_displayOptionMacro;
    }

    /**
     * Returns the resourceFolder under which xmlcontent resources will be investigated recursively.
     * <p>
     * 
     * @return the resourceFolder
     */
    public CmsResource getResourceFolder() {

        return m_resourceFolder;
    }

    /**
     * Returns the resource type id.
     * <p>
     * 
     * @return the resourceTypeID
     */
    public int getResourceTypeID() {

        return m_resourceTypeID;
    }

    /**
     * @see org.opencms.widgets.CmsSelectWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsSelectWidgetXmlcontentType(getConfiguration());
    }

    /**
     * Returns the list of configured select options, parsing the configuration String if required.
     * <p>
     * 
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog of this widget
     * @param param the widget parameter of this dialog
     * 
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject,
     *      org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     * 
     * @return the list of configured select options.
     * 
     * @throws CmsIllegalArgumentException if the "folder" property of the configuration does not denote a folder within the VFS
     */
    protected List parseSelectOptions(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param)
    throws CmsIllegalArgumentException {

        Locale dialogContentLocale = ((I_CmsXmlContentValue)param).getLocale();
        Locale resourceLocale;
        if (m_macroCmsObject == null) {
            try {
                m_macroCmsObject = OpenCms.initCmsObject(cms);
                m_macroCmsObject.getRequestContext().setSiteRoot("/");
            } catch (CmsException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.ERR_SELECTWIDGET_INTERNAL_CONFIGURATION_2,
                        new Object[] {getClass().getName(), getConfiguration()}));
                }
                return Collections.EMPTY_LIST;

            }
        }
        if (m_macroResolver == null) {
            m_macroResolver = new CmsMacroResolver();
            m_macroResolver.setCmsObject(m_macroCmsObject);
            m_macroResolver.setKeepEmptyMacros(true);
        }

        List selectOptions = getSelectOptions();
        if (selectOptions == null) {
            String configuration = getConfiguration();
            if (configuration == null) {
                // workaround: use the default value to parse the options
                configuration = param.getDefault(cms);
            }
            try {
                // parse configuration to members
                parseConfigurationInternal(configuration, cms, param);

                // build the set of sorted options
                SortedSet sortOptions = new TreeSet(new CmsResourceSelectWidgetOptionComparator(
                    m_macroCmsObject,
                    m_sortMacro));
                CmsSelectWidgetOption option;
                List resources;
                // collect all subresources of resource folder
                CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(getResourceTypeID());
                CmsRequestContext context = cms.getRequestContext();
                String oldSiteroot = context.getSiteRoot();
                context.setSiteRoot("/");
                resources = cms.readResources(m_resourceFolder.getRootPath(), filter, true);
                context.setSiteRoot(oldSiteroot);
                if (resources.size() == 0) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(
                            Messages.LOG_ERR_SELECTWIDGET_NO_RESOURCES_FOUND_3,
                            configuration,
                            m_resourceFolder.getRootPath(),
                            OpenCms.getResourceManager().getResourceType(getResourceTypeID()).getTypeName()));
                    }
                }

                Iterator itResources = resources.iterator();
                CmsResource resource;

                String displayName;
                // inner loop vars :
                while (itResources.hasNext()) {

                    resource = (CmsResource)itResources.next();
                    // don't make resources selectable that have a different locale than the current editor language.
                    // we read the locale node of the xmlcontent instance matching the resources
                    // locale property (or top level locale).
                    resourceLocale = CmsLocaleManager.getLocale(cms.readPropertyObject(
                        resource,
                        CmsPropertyDefinition.PROPERTY_LOCALE,
                        true).getValue());

                    // Only show select options for resources that are in the same locale as the current
                    // editor locale (e.g. when switching to german, offer the german siblings)
                    if (dialogContentLocale.equals(resourceLocale)) {
                        // macro resolvation within hasFilterProperty will resolve values to the
                        // current request
                        if (hasFilterProperty(resource, cms)) {

                            // implant the uri to the special cms object for resolving macros from
                            // the collected xml contents:
                            m_macroCmsObject.getRequestContext().setUri(resource.getRootPath());
                            // implant the resource for macro "%(opencms.filename)"
                            m_macroResolver.setResourceName(resource.getName());
                            // implant the messages
                            m_macroResolver.setMessages(widgetDialog.getMessages());
                            // filter out unwanted resources - if no filter properties are defined,
                            // every resource collected here is ok:
                            displayName = m_macroResolver.resolveMacros(getDisplayOptionMacro());
                            // deal with a bug of the macro resolver: it will return "" if it gets
                            // "%(unknown.thing)":
                            if (CmsStringUtil.isEmptyOrWhitespaceOnly(displayName)) {
                                // it was a "%(xpath.field)" expression only and swallowed by macro
                                // resolver:
                                displayName = resolveXpathMacros(cms, resource, getDisplayOptionMacro());
                            } else {
                                // there was more than one xpath macro: allow further replacements
                                // within partly resolved macro:
                                displayName = resolveXpathMacros(cms, resource, displayName);
                            }
                            // final check:
                            if (CmsStringUtil.isEmpty(displayName)) {
                                displayName = resource.getName();
                            }

                            displayName = resolveXpathMacros(cms, resource, displayName);

                            if (!CmsStringUtil.isEmpty(displayName)) {

                                // now everything required is there:
                                option = new CmsResourceSelectWidgetOption(resource, false, displayName);
                                sortOptions.add(option);
                            }
                        }
                    }
                }
                selectOptions = new LinkedList(sortOptions);

            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.ERR_SELECTWIDGET_CONFIGURATION_2,
                        getClass(),
                        configuration), e);
                }
            }

            if ((selectOptions == Collections.EMPTY_LIST) || (selectOptions == null)) {
                selectOptions = new ArrayList();
            }

            // no method to add the parsed option list....
            // Caution: if it is decided to return a copy of the list we are doomed unless
            // setSelectOptions is set to protected!
            List pOptions = getSelectOptions();
            if (pOptions != null) {
                pOptions.clear();
            }
            Iterator it = selectOptions.iterator();
            while (it.hasNext()) {
                addSelectOption((CmsSelectWidgetOption)it.next());
            }
        }

        return selectOptions;
    }

    private boolean hasFilterProperty(CmsResource resource, CmsObject cms) throws CmsException {

        boolean result = false;
        Iterator itFilterProperties;
        Map.Entry entry;
        CmsProperty property;
        // filter out unwanted resources - if no filter properties are defined, every
        // resource collected here is ok:
        if (m_filterProperties.size() > 0) {
            itFilterProperties = m_filterProperties.entrySet().iterator();
            while (itFilterProperties.hasNext()) {
                entry = (Map.Entry)itFilterProperties.next();
                property = cms.readPropertyObject(resource, (String)entry.getKey(), true);
                if (property == CmsProperty.getNullProperty()) {
                    continue;
                } else {
                    // check if value is ok:
                    if (property.getValue().equals(entry.getValue())) {
                        // Ok, resource granted:
                        result = true;
                        break;

                    } else {
                        // Failed, try further filter properties for match:
                    }
                }
            }
        } else {
            // don't filter if now filter props configured
            result = true;
        }

        return result;
    }

    /**
     * Parses the configuration and puts it to the member variables.
     * <p>
     * 
     * Only invoked if options were not parsed before in this instance.
     * <p>
     * 
     * @param configuration
     *            the configuration (with resolved macros).
     * 
     * @param cms
     *            needed to read the resource folder to use.
     * 
     * @param param
     *            allows to access the resource currently being rendered.
     * 
     * 
     * @throws CmsIllegalArgumentException
     *             if the configuration is invalid.
     * 
     */
    private void parseConfigurationInternal(String configuration, CmsObject cms, I_CmsWidgetParameter param) {

        // prepare for macro resolvation of property value against the resource currently
        // rendered
        // implant the uri to the special cms object for resolving macros from the
        // collected xml contents:
        CmsFile file = ((I_CmsXmlContentValue)param).getDocument().getFile();
        m_macroCmsObject.getRequestContext().setUri(file.getRootPath());
        List mappings = CmsStringUtil.splitAsList(configuration, '|');
        Iterator itMappings = mappings.iterator();
        String mapping;
        String[] keyValue;
        String key;
        String value;
        boolean displayMacroFound = false, sortMacroFound = false, folderFound = false, typeFound = false;
        while (itMappings.hasNext()) {
            mapping = (String)itMappings.next();
            keyValue = CmsStringUtil.splitAsArray(mapping, '=');
            if (keyValue.length != 2) {
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_SELECTWIDGET_CONFIGURATION_KEYVALUE_LENGTH_1,
                    mapping));
            }
            key = keyValue[0].trim();
            value = keyValue[1].trim();

            // implant the resource for macro "%(opencms.filename)"
            m_macroResolver.setResourceName(file.getName());
            // check key
            if (CONFIGURATION_OPTION_DISPLAY_MACRO.equals(key)) {
                if (displayMacroFound) {
                    throw new CmsIllegalArgumentException(Messages.get().container(
                        Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_DUPLICATE_2,
                        key,
                        configuration));
                }

                m_displayOptionMacro = value;
                displayMacroFound = true;
            } else if (CONFIGURATION_OPTION_SORT_MACRO.equals(key)) {
                if (sortMacroFound) {
                    throw new CmsIllegalArgumentException(Messages.get().container(
                        Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_DUPLICATE_2,
                        key,
                        configuration));
                }
                m_sortMacro = value;
                sortMacroFound = true;

            } else if (CONFIGURATION_RESOURCETYPENAME.equals(key)) {
                if (typeFound) {
                    throw new CmsIllegalArgumentException(Messages.get().container(
                        Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_DUPLICATE_2,
                        key,
                        configuration));
                }
                // check if resource type name is OK
                // if setResourceType will be implemented copy here and invoke that one
                try {
                    m_resourceTypeID = OpenCms.getResourceManager().getResourceType(value).getTypeId();
                } catch (CmsLoaderException e) {
                    throw new CmsIllegalArgumentException(org.opencms.file.Messages.get().container(
                        org.opencms.file.Messages.ERR_UNKNOWN_RESOURCE_TYPE_1,
                        value), e);
                }
                typeFound = true;

            } else if (CONFIGURATION_TOPFOLDER.equals(key)) {
                if (folderFound) {
                    throw new CmsIllegalArgumentException(Messages.get().container(
                        Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_DUPLICATE_2,
                        key,
                        configuration));
                }

                // allow collector path to contain macros relative to the current resource:
                value = m_macroResolver.resolveMacros(value);

                try {
                    CmsRequestContext context = cms.getRequestContext();
                    String oldSiteRoot = context.getSiteRoot();
                    context.setSiteRoot("/");
                    CmsResource resource = cms.readResource(value);
                    context.setSiteRoot(oldSiteRoot);
                    if (resource.isFile()) {
                        throw new CmsIllegalArgumentException(Messages.get().container(
                            Messages.ERR_SELECTWIDGET_CONFIGURATION_RESOURCE_NOFOLDER_2,
                            value,
                            configuration));
                    }
                    m_resourceFolder = resource;
                } catch (CmsException e) {
                    throw new CmsIllegalArgumentException(Messages.get().container(
                        Messages.ERR_SELECTWIDGET_CONFIGURATION_RESOURCE_INVALID_2,
                        value,
                        configuration), e);
                }

                folderFound = true;
            } else {
                // a property=value definition???

                CmsPropertyDefinition propDef;
                try {
                    propDef = cms.readPropertyDefinition(key);
                } catch (CmsException e) {

                    throw new CmsIllegalArgumentException(Messages.get().container(
                        Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_UNKNOWN_2,
                        key,
                        getClass().getName()), e);
                }
                if (propDef != null) {
                    // a valid property - value combination to filter resources for:
                    // value is potentially a macro that will be compared to the current xml content
                    // resource!
                    value = m_macroResolver.resolveMacros(value);
                    m_filterProperties.put(key, value);

                } else {

                    throw new CmsIllegalArgumentException(Messages.get().container(
                        Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_UNKNOWN_2,
                        key,
                        getClass().getName()));
                }
            }
        }

        // final check wether all has been set
        if (!displayMacroFound) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_MISSING_3,
                CONFIGURATION_OPTION_DISPLAY_MACRO,
                configuration,
                getClass().getName()));
        }
        if (!folderFound) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_MISSING_3,
                CONFIGURATION_TOPFOLDER,
                configuration,
                getClass().getName()));
        }
        if (!typeFound) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_SELECTWIDGET_CONFIGURATION_KEY_MISSING_3,
                CONFIGURATION_RESOURCETYPENAME,
                configuration,
                getClass().getName()));
        }
    }

    /**
     * 
     * Resolves xpath macros of the form <code>"%(xpath.XPATHEXPRESSION)"</code> by the field value of the XML content
     * denoted by the given resource.
     * <p>
     * 
     * File laoding and unmarshalling is only done if the given String contains xpath macros.
     * <p>
     * 
     * @param cms
     *            to access values in the cmsobject
     * @param resource
     *            the resource pointing to an xmlcontent containing the macro values to resolve
     * @param value
     *            the unresolved macro string
     * 
     * @return a String with resolved xpath macros that have been read from the xmlcontent
     * 
     * @throws CmsException
     *             if somehting goes wrong
     */
    private String resolveXpathMacros(CmsObject cms, CmsResource resource, String value) throws CmsException {

        StringBuffer result = new StringBuffer();
        int startmacro = value.indexOf(I_CmsMacroResolver.MACRO_DELIMITER
            + ""
            + I_CmsMacroResolver.MACRO_START
            + "xpath.");
        int stopmacro = 0;
        String xpath;
        if (startmacro != -1) {

            // for the option value we have to unmarshal...
            CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
            // we read the locale node of the xmlcontent instance matching the resources
            // locale property (or top level locale).
            Locale locale = CmsLocaleManager.getLocale(cms.readPropertyObject(
                xmlcontent.getFile(),
                CmsPropertyDefinition.PROPERTY_LOCALE,
                true).getValue());

            while (startmacro != -1) {
                stopmacro = value.indexOf(I_CmsMacroResolver.MACRO_END);
                if (stopmacro == 0) {
                    // TODO: complain about missing closing macro bracket!
                }

                // first cut the prefix of the macro to put it to the result:
                result.append(value.substring(0, startmacro));
                // now replace the macro:
                xpath = value.substring(startmacro + 8, stopmacro);
                // Foreign languages will be invisible!!!
                // List locales = content.getLocales();
                // if (!locales.contains(locale)) {
                // locale = (Locale)locales.get(0);
                // }
                try {
                    result.append(xmlcontent.getValue(xpath, locale).getPlainText(cms));
                } catch (Exception ex) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(
                            Messages.LOG_ERR_SELECTWIDGET_XPATH_INVALID_4,
                            new Object[] {
                                xpath,
                                locale.toString(),
                                xmlcontent.getFile().getRootPath(),
                                ex.getLocalizedMessage()}));
                    }
                }
                // skip over the consumed String of value:
                value = value.substring(stopmacro + 1);

                // take a new start for macro:
                startmacro = value.indexOf(I_CmsMacroResolver.MACRO_DELIMITER
                    + ""
                    + I_CmsMacroResolver.MACRO_START
                    + "xpath.");
            }
        }
        // append trailing value
        result.append(value);
        return result.toString();
    }
}
