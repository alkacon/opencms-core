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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.configuration;

import org.opencms.db.CmsUserExportSettings;
import org.opencms.importexport.CmsExtendedHtmlImportDefault;
import org.opencms.importexport.CmsImportExportManager;
import org.opencms.importexport.CmsImportExportManager.TimestampMode;
import org.opencms.importexport.I_CmsImport;
import org.opencms.importexport.I_CmsImportExportHandler;
import org.opencms.main.CmsLog;
import org.opencms.repository.CmsRepositoryFilter;
import org.opencms.repository.CmsRepositoryManager;
import org.opencms.repository.I_CmsRepository;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.staticexport.CmsStaticExportExportRule;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.staticexport.CmsStaticExportRfsRule;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * Import / export master configuration class.<p>
 *
 * @since 6.0.0
 */
public class CmsImportExportConfiguration extends A_CmsXmlConfiguration {

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-importexport.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-importexport.xml";

    /**  The node name of the column node. */
    public static final String N_COLUMN = "column";

    /**  The node name of the columns node. */
    public static final String N_COLUMNS = "columns";

    /** Node that indicates page conversion. */
    public static final String N_CONVERT = "convert";

    /**  The main configuration node for the extended html import. */
    public static final String N_EXTHTMLIMPORT = "extendedhtmlimport";

    /**  The node name of the html import destination node. */
    public static final String N_EXTHTMLIMPORT_DESTINATION = "destination";

    /**  The node name of the html import download gallery node. */
    public static final String N_EXTHTMLIMPORT_DOWNLOAD = "download";

    /**  The node name of the html import template node. */
    public static final String N_EXTHTMLIMPORT_ELEMENT = "element";

    /**  The node name of the html import encoding node. */
    public static final String N_EXTHTMLIMPORT_ENCODING = "encoding";

    /**  The node name of the html import image gallery node. */
    public static final String N_EXTHTMLIMPORT_EXTERNALLINK = "externallink";

    /**  The node name of the html import galleries node. */
    public static final String N_EXTHTMLIMPORT_GALLERIES = "galleries";

    /**  The node name of the html import image gallery node. */
    public static final String N_EXTHTMLIMPORT_IMAGE = "image";

    /**  The node name of the html import input node. */
    public static final String N_EXTHTMLIMPORT_INPUT = "input";

    /**  The node name of the html import overwritefiles node. */
    public static final String N_EXTHTMLIMPORT_KEEPBROKENLINKS = "keepbrokenlinks";

    /**  The node name of the html import locale node. */
    public static final String N_EXTHTMLIMPORT_LOCALE = "locale";

    /**  The node name of the html import overwritefiles node. */
    public static final String N_EXTHTMLIMPORT_OVERWRITE = "overwritefiles";

    /**  The node name of the html import pattern node. */
    public static final String N_EXTHTMLIMPORT_PATTERN = "pattern";

    /**  The node name of the html import end pattern node. */
    public static final String N_EXTHTMLIMPORT_PATTERN_END = "end";

    /**  The node name of the html import start pattern node. */
    public static final String N_EXTHTMLIMPORT_PATTERN_START = "start";

    /**  The node name of the html import settings node. */
    public static final String N_EXTHTMLIMPORT_SETTINGS = "settings";

    /**  The node name of the html import template node. */
    public static final String N_EXTHTMLIMPORT_TEMPLATE = "template";

    /** The node name of the repository filter node. */
    public static final String N_FILTER = "filter";

    /** Node that contains a list of properties ignored during import. */
    public static final String N_IGNOREDPROPERTIES = "ignoredproperties";

    /** The import immutable resources node. */
    public static final String N_IMMUTABLES = "immutables";

    /** The node name of the import sub-configuration. */
    public static final String N_IMPORT = "import";

    /** The node name of the export sub-configuration. */
    public static final String N_EXPORT = "export";

    /** The node name of the defaultexporttimestamps sub-configuration. */
    public static final String N_EXPORT_DEFAULTTIMESTAMPMODES = "defaulttimestampmodes";

    /** The node name of the timestamp sub-configuration. */
    public static final String N_EXPORT_TIMESTAMPMODE = "timestampmode";

    /** The node name of the resourcetype sub-configuration. */
    public static final String N_EXPORT_RESOURCETYPENAME = "resourcetypename";

    /** The main configuration node name. */
    public static final String N_IMPORTEXPORT = "importexport";

    /** The node name of an individual import/export handler. */
    public static final String N_IMPORTEXPORTHANDLER = "importexporthandler";

    /** Master node for import/export handlers. */
    public static final String N_IMPORTEXPORTHANDLERS = "importexporthandlers";

    /** The node name of an individual import version class. */
    public static final String N_IMPORTVERSION = "importversion";

    /** Master node for import version class names. */
    public static final String N_IMPORTVERSIONS = "importversions";

    /**  The node name of the static export handler node. */
    public static final String N_LINKSUBSTITUTION_HANDLER = "linksubstitutionhandler";

    /** Node the contains an optional URL of old web application. */
    public static final String N_OLDWEBAPPURL = "oldwebappurl";

    /** The import overwrite node name. */
    public static final String N_OVERWRITE = "overwrite";

    /** The node name of the repository params node. */
    public static final String N_PARAMS = "params";

    /** An individual principal translation node. */
    public static final String N_PRINCIPALTRANSLATION = "principaltranslation";

    /** The principal translation node. */
    public static final String N_PRINCIPALTRANSLATIONS = "principaltranslations";

    /** The node name of the repository filter regex node. */
    public static final String N_REGEX = "regex";

    /** The node name of the repositories node. */
    public static final String N_REPOSITORIES = "repositories";

    /** The node name of the repository node. */
    public static final String N_REPOSITORY = "repository";

    /**  The node name of the separator node. */
    public static final String N_SEPARATOR = "separator";

    /**  The main configuration node for static export name. */
    public static final String N_STATICEXPORT = "staticexport";

    /**  The node name of the static export acceptcharset node. */
    public static final String N_STATICEXPORT_ACCEPTCHARSET = "acceptcharset";

    /**  The node name of the static export acceptlanguage node. */
    public static final String N_STATICEXPORT_ACCEPTLANGUAGE = "acceptlanguage";

    /**  The node name of the static export default node. */
    public static final String N_STATICEXPORT_DEFAULT = "defaultpropertyvalue";

    /**  The node name of the static export defualtsuffix node. */
    public static final String N_STATICEXPORT_DEFAULTSUFFIXES = "defaultsuffixes";

    /**  The node name of the static export rule description nodes. */
    public static final String N_STATICEXPORT_DESCRIPTION = "description";

    /**  The node name of the static export export-rule export node. */
    public static final String N_STATICEXPORT_EXPORT = "export-resources";

    /**  The node name of the static export exportbackups node. */
    public static final String N_STATICEXPORT_EXPORTBACKUPS = "exportbackups";

    /**  The node name of the static export exportheaders node. */
    public static final String N_STATICEXPORT_EXPORTHEADERS = "exportheaders";

    /**  The node name of the static export exportpath node. */
    public static final String N_STATICEXPORT_EXPORTPATH = "exportpath";

    /**  The node name of the static export export-rule node. */
    public static final String N_STATICEXPORT_EXPORTRULE = "export-rule";

    /**  The node name of the static export export-rules node. */
    public static final String N_STATICEXPORT_EXPORTRULES = "export-rules";

    /**  The node name of the static export exporturl node. */
    public static final String N_STATICEXPORT_EXPORTURL = "exporturl";

    /**  The node name of the static export exportworkpath node. */
    public static final String N_STATICEXPORT_EXPORTWORKPATH = "exportworkpath";

    /**  The node name of the static export handler node. */
    public static final String N_STATICEXPORT_HANDLER = "staticexporthandler";

    /**  The node name of the static export header node. */
    public static final String N_STATICEXPORT_HEADER = "header";

    /**  The node name of the static export export-rule modified node. */
    public static final String N_STATICEXPORT_MODIFIED = "modified-resources";

    /**  The node name of the static export rule name nodes. */
    public static final String N_STATICEXPORT_NAME = "name";

    /**  The node name of the static export plainoptimization node. */
    public static final String N_STATICEXPORT_PLAINOPTIMIZATION = "plainoptimization";

    /**  The node name of the static export regex node. */
    public static final String N_STATICEXPORT_REGEX = "regex";

    /**  The node name of the static export related-system-res node. */
    public static final String N_STATICEXPORT_RELATED_SYSTEM_RES = "related-system-res";

    /**  The node name of the static export relativelinks node. */
    public static final String N_STATICEXPORT_RELATIVELINKS = "userelativelinks";

    /**  The node name of the static export remoteaddr node. */
    public static final String N_STATICEXPORT_REMOTEADDR = "remoteaddr";

    /**  The node name of the static export rendersettings node. */
    public static final String N_STATICEXPORT_RENDERSETTINGS = "rendersettings";

    /**  The node name of the static export requestheaders node. */
    public static final String N_STATICEXPORT_REQUESTHEADERS = "requestheaders";

    /**  The node name of the static export resourcestorender node. */
    public static final String N_STATICEXPORT_RESOURCESTORENDER = "resourcestorender";

    /**  The node name of the static export rfx-prefix node. */
    public static final String N_STATICEXPORT_RFS_PREFIX = "rfs-prefix";

    /**  The node name of the static export rfx-rule node. */
    public static final String N_STATICEXPORT_RFS_RULE = "rfs-rule";

    /**  The node name of the static export rfx-rules node. */
    public static final String N_STATICEXPORT_RFS_RULES = "rfs-rules";

    /**  The node name of the static export rfx-rule source node. */
    public static final String N_STATICEXPORT_SOURCE = "source";

    /**  The node name of the static export suffix node. */
    public static final String N_STATICEXPORT_SUFFIX = "suffix";

    /**  The node name of the static export testresource node. */
    public static final String N_STATICEXPORT_TESTRESOURCE = "testresource";

    /**  The node name of the static export export-rule export uri node. */
    public static final String N_STATICEXPORT_URI = "uri";

    /**  The node name of the static export vfx-prefix node. */
    public static final String N_STATICEXPORT_VFS_PREFIX = "vfs-prefix";

    /**  The node name of the user csv export node. */
    public static final String N_USERCSVEXPORT = "usercsvexport";

    /** The configured import/export manager. */
    private CmsImportExportManager m_importExportManager;

    /** The configured repository manager. */
    private CmsRepositoryManager m_repositoryManager;

    /** The configured static export manager. */
    private CmsStaticExportManager m_staticExportManager;

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add finish rule
        digester.addCallMethod("*/" + N_IMPORTEXPORT, "initializeFinished");

        // creation of the import/export manager
        digester.addObjectCreate("*/" + N_IMPORTEXPORT, CmsImportExportManager.class);
        // import/export manager finished
        digester.addSetNext("*/" + N_IMPORTEXPORT, "setImportExportManager");

        // add rules for import/export handlers
        digester.addObjectCreate(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORTEXPORTHANDLERS + "/" + N_IMPORTEXPORTHANDLER,
            A_CLASS,
            CmsConfigurationException.class);
        digester.addSetNext(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORTEXPORTHANDLERS + "/" + N_IMPORTEXPORTHANDLER,
            "addImportExportHandler");

        // overwrite rule
        digester.addCallMethod(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_OVERWRITE,
            "setOverwriteCollidingResources",
            0);

        // convert rule
        digester.addCallMethod("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_CONVERT, "setConvertToXmlPage", 0);

        // old webapp rule
        digester.addCallMethod("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_OLDWEBAPPURL, "setOldWebAppUrl", 0);

        // add rules for the import versions
        digester.addObjectCreate(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IMPORTVERSIONS + "/" + N_IMPORTVERSION,
            A_CLASS,
            CmsConfigurationException.class);
        digester.addSetNext(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IMPORTVERSIONS + "/" + N_IMPORTVERSION,
            "addImportVersionClass");

        // add rules for the import immutables
        digester.addCallMethod(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IMMUTABLES + "/" + N_RESOURCE,
            "addImmutableResource",
            1);
        digester.addCallParam("*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IMMUTABLES + "/" + N_RESOURCE, 0, A_URI);

        // add rules for the import principal translations
        digester.addCallMethod(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_PRINCIPALTRANSLATIONS + "/" + N_PRINCIPALTRANSLATION,
            "addImportPrincipalTranslation",
            3);
        digester.addCallParam(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_PRINCIPALTRANSLATIONS + "/" + N_PRINCIPALTRANSLATION,
            0,
            A_TYPE);
        digester.addCallParam(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_PRINCIPALTRANSLATIONS + "/" + N_PRINCIPALTRANSLATION,
            1,
            A_FROM);
        digester.addCallParam(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_PRINCIPALTRANSLATIONS + "/" + N_PRINCIPALTRANSLATION,
            2,
            A_TO);

        // add rules for the ignored properties
        digester.addCallMethod(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IGNOREDPROPERTIES + "/" + N_PROPERTY,
            "addIgnoredProperty",
            1);
        digester.addCallParam(
            "*/" + N_IMPORTEXPORT + "/" + N_IMPORT + "/" + N_IGNOREDPROPERTIES + "/" + N_PROPERTY,
            0,
            A_NAME);

        // add rules for export settings
        digester.addCallMethod(
            "*/"
                + N_IMPORTEXPORT
                + "/"
                + N_EXPORT
                + "/"
                + N_EXPORT_DEFAULTTIMESTAMPMODES
                + "/"
                + N_EXPORT_TIMESTAMPMODE,
            "addDefaultTimestampMode",
            1);

        digester.addCallParam(
            "*/"
                + N_IMPORTEXPORT
                + "/"
                + N_EXPORT
                + "/"
                + N_EXPORT_DEFAULTTIMESTAMPMODES
                + "/"
                + N_EXPORT_TIMESTAMPMODE,
            0,
            A_MODE);

        digester.addCallMethod(
            "*/"
                + N_IMPORTEXPORT
                + "/"
                + N_EXPORT
                + "/"
                + N_EXPORT_DEFAULTTIMESTAMPMODES
                + "/"
                + N_EXPORT_TIMESTAMPMODE
                + "/"
                + N_EXPORT_RESOURCETYPENAME,
            "addResourceTypeForDefaultTimestampMode",
            1);

        digester.addCallParam(
            "*/"
                + N_IMPORTEXPORT
                + "/"
                + N_EXPORT
                + "/"
                + N_EXPORT_DEFAULTTIMESTAMPMODES
                + "/"
                + N_EXPORT_TIMESTAMPMODE
                + "/"
                + N_EXPORT_RESOURCETYPENAME,
            0);

        // creation of the static export manager
        digester.addObjectCreate("*/" + N_STATICEXPORT, CmsStaticExportManager.class);
        // static export manager finished
        digester.addSetNext("*/" + N_STATICEXPORT, "setStaticExportManager");
        // export enabled role
        digester.addCallMethod("*/" + N_STATICEXPORT, "setExportEnabled", 1);
        digester.addCallParam("*/" + N_STATICEXPORT, 0, A_ENABLED);
        // export handler rule
        digester.addCallMethod("*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_HANDLER, "setHandler", 0);
        // link substitution handler rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_LINKSUBSTITUTION_HANDLER,
            "setLinkSubstitutionHandler",
            0);
        // exportpath rule
        digester.addCallMethod("*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_EXPORTPATH, "setExportPath", 0);
        // exportworkpath rule
        digester.addCallMethod("*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_EXPORTWORKPATH, "setExportWorkPath", 0);
        // exportbackups rule
        digester.addCallMethod("*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_EXPORTBACKUPS, "setExportBackups", 0);
        // default property rule
        digester.addCallMethod("*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_DEFAULT, "setDefault", 0);
        // export suffix rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_DEFAULTSUFFIXES + "/" + N_STATICEXPORT_SUFFIX,
            "setExportSuffix",
            1);
        digester.addCallParam(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_DEFAULTSUFFIXES + "/" + N_STATICEXPORT_SUFFIX,
            0,
            A_KEY);
        // header rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_EXPORTHEADERS + "/" + N_STATICEXPORT_HEADER,
            "setExportHeader",
            0);
        // accept-language rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_REQUESTHEADERS + "/" + N_STATICEXPORT_ACCEPTLANGUAGE,
            "setAcceptLanguageHeader",
            0);
        // accept-charset rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_REQUESTHEADERS + "/" + N_STATICEXPORT_ACCEPTCHARSET,
            "setAcceptCharsetHeader",
            0);
        // accept-charset rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_REQUESTHEADERS + "/" + N_STATICEXPORT_REMOTEADDR,
            "setRemoteAddr",
            0);
        // rfs-prefix rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_RENDERSETTINGS + "/" + N_STATICEXPORT_RFS_PREFIX,
            "setRfsPrefix",
            0);
        // vfs-prefix rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_RENDERSETTINGS + "/" + N_STATICEXPORT_VFS_PREFIX,
            "setVfsPrefix",
            0);
        // relative links rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_RENDERSETTINGS + "/" + N_STATICEXPORT_RELATIVELINKS,
            "setRelativeLinks",
            0);
        // exporturl rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_RENDERSETTINGS + "/" + N_STATICEXPORT_EXPORTURL,
            "setExportUrl",
            0);
        // plain export optimization rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_RENDERSETTINGS + "/" + N_STATICEXPORT_PLAINOPTIMIZATION,
            "setPlainExportOptimization",
            0);
        // test resource rule
        digester.addCallMethod(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_RENDERSETTINGS + "/" + N_STATICEXPORT_TESTRESOURCE,
            "setTestResource",
            1);
        digester.addCallParam(
            "*/" + N_STATICEXPORT + "/" + N_STATICEXPORT_RENDERSETTINGS + "/" + N_STATICEXPORT_TESTRESOURCE,
            0,
            A_URI);
        // resources to export rule
        digester.addCallMethod(
            "*/"
                + N_STATICEXPORT
                + "/"
                + N_STATICEXPORT_RENDERSETTINGS
                + "/"
                + N_STATICEXPORT_RESOURCESTORENDER
                + "/"
                + N_STATICEXPORT_REGEX,
            "setExportFolderPattern",
            0);

        // export-rules configuration
        String exportRulePath = "*/"
            + N_STATICEXPORT
            + "/"
            + N_STATICEXPORT_RENDERSETTINGS
            + "/"
            + N_STATICEXPORT_RESOURCESTORENDER
            + "/"
            + N_STATICEXPORT_EXPORTRULES
            + "/"
            + N_STATICEXPORT_EXPORTRULE;
        digester.addCallMethod(exportRulePath, "addExportRule", 2);
        digester.addCallParam(exportRulePath + "/" + N_STATICEXPORT_NAME, 0);
        digester.addCallParam(exportRulePath + "/" + N_STATICEXPORT_DESCRIPTION, 1);
        digester.addCallMethod(
            exportRulePath + "/" + N_STATICEXPORT_MODIFIED + "/" + N_STATICEXPORT_REGEX,
            "addExportRuleRegex",
            1);
        digester.addCallParam(exportRulePath + "/" + N_STATICEXPORT_MODIFIED + "/" + N_STATICEXPORT_REGEX, 0);
        digester.addCallMethod(
            exportRulePath + "/" + N_STATICEXPORT_EXPORT + "/" + N_STATICEXPORT_URI,
            "addExportRuleUri",
            1);
        digester.addCallParam(exportRulePath + "/" + N_STATICEXPORT_EXPORT + "/" + N_STATICEXPORT_URI, 0);

        // rfs-rules configuration
        String rfsRulePath = "*/"
            + N_STATICEXPORT
            + "/"
            + N_STATICEXPORT_RENDERSETTINGS
            + "/"
            + N_STATICEXPORT_RFS_RULES
            + "/"
            + N_STATICEXPORT_RFS_RULE;
        digester.addCallMethod(rfsRulePath, "addRfsRule", 8);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_NAME, 0);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_DESCRIPTION, 1);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_SOURCE, 2);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_RFS_PREFIX, 3);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_EXPORTPATH, 4);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_EXPORTWORKPATH, 5);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_EXPORTBACKUPS, 6);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_RELATIVELINKS, 7);
        // rfs-rule related system resources
        digester.addCallMethod(
            rfsRulePath + "/" + N_STATICEXPORT_RELATED_SYSTEM_RES + "/" + N_STATICEXPORT_REGEX,
            "addRfsRuleSystemRes",
            1);
        digester.addCallParam(rfsRulePath + "/" + N_STATICEXPORT_RELATED_SYSTEM_RES + "/" + N_STATICEXPORT_REGEX, 0);

        // add rules for the user data export
        digester.addObjectCreate("*/" + N_USERCSVEXPORT, CmsUserExportSettings.class);
        digester.addCallMethod("*/" + N_USERCSVEXPORT + "/" + N_SEPARATOR, "setSeparator", 0);
        digester.addCallMethod("*/" + N_USERCSVEXPORT + "/" + N_COLUMNS + "/" + N_COLUMN, "addColumn", 0);
        digester.addSetNext("*/" + N_USERCSVEXPORT, "setUserExportSettings");

        // creation of the static repository manager
        digester.addObjectCreate("*/" + N_REPOSITORIES, CmsRepositoryManager.class);
        digester.addCallMethod("*/" + N_REPOSITORIES, I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_REPOSITORIES, "setRepositoryManager");

        // repository
        digester.addObjectCreate("*/" + N_REPOSITORIES + "/" + N_REPOSITORY, A_CLASS, CmsConfigurationException.class);

        // repository name
        digester.addCallMethod("*/" + N_REPOSITORIES + "/" + N_REPOSITORY, "setName", 1);
        digester.addCallParam("*/" + N_REPOSITORIES + "/" + N_REPOSITORY, 0, A_NAME);

        // repository params
        digester.addCallMethod(
            "*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_PARAMS + "/" + N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam(
            "*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_PARAMS + "/" + N_PARAM,
            0,
            I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_PARAMS + "/" + N_PARAM, 1);

        // repository filter
        digester.addObjectCreate(
            "*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_FILTER,
            CmsRepositoryFilter.class);

        // repository filter type
        digester.addCallMethod("*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_FILTER, "setType", 1);
        digester.addCallParam("*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_FILTER, 0, A_TYPE);

        // repository filter rules
        digester.addCallMethod(
            "*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_FILTER + "/" + N_REGEX,
            "addFilterRule",
            1);
        digester.addCallParam("*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_FILTER + "/" + N_REGEX, 0);

        digester.addSetNext("*/" + N_REPOSITORIES + "/" + N_REPOSITORY + "/" + N_FILTER, "setFilter");

        digester.addSetNext("*/" + N_REPOSITORIES + "/" + N_REPOSITORY, "addRepositoryClass");

        // create at least a repository manager though no repositories are configured
        if (m_repositoryManager == null) {
            m_repositoryManager = new CmsRepositoryManager(false);
        }
        // creation of the extended HTML importer
        digester.addObjectCreate("*/" + N_EXTHTMLIMPORT, CmsExtendedHtmlImportDefault.class);
        // extended HTML importer  finished
        digester.addSetNext("*/" + N_EXTHTMLIMPORT, "setExtendedHtmlImportManager");
        digester.addCallMethod("*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_DESTINATION, "setDestinationDir", 0);
        digester.addCallMethod("*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_INPUT, "setInputDir", 0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_GALLERIES + "/" + N_EXTHTMLIMPORT_DOWNLOAD,
            "setDownloadGallery",
            0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_GALLERIES + "/" + N_EXTHTMLIMPORT_IMAGE,
            "setImageGallery",
            0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_GALLERIES + "/" + N_EXTHTMLIMPORT_EXTERNALLINK,
            "setLinkGallery",
            0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_SETTINGS + "/" + N_EXTHTMLIMPORT_TEMPLATE,
            "setTemplate",
            0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_SETTINGS + "/" + N_EXTHTMLIMPORT_ELEMENT,
            "setElement",
            0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_SETTINGS + "/" + N_EXTHTMLIMPORT_LOCALE,
            "setLocale",
            0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_SETTINGS + "/" + N_EXTHTMLIMPORT_ENCODING,
            "setEncoding",
            0);
        digester.addCallMethod(
            "*/"
                + N_EXTHTMLIMPORT
                + "/"
                + N_EXTHTMLIMPORT_SETTINGS
                + "/"
                + N_EXTHTMLIMPORT_PATTERN
                + "/"
                + N_EXTHTMLIMPORT_PATTERN_START,
            "setStartPattern",
            0);
        digester.addCallMethod(
            "*/"
                + N_EXTHTMLIMPORT
                + "/"
                + N_EXTHTMLIMPORT_SETTINGS
                + "/"
                + N_EXTHTMLIMPORT_PATTERN
                + "/"
                + N_EXTHTMLIMPORT_PATTERN_END,
            "setEndPattern",
            0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_SETTINGS + "/" + N_EXTHTMLIMPORT_OVERWRITE,
            "setOverwrite",
            0);
        digester.addCallMethod(
            "*/" + N_EXTHTMLIMPORT + "/" + N_EXTHTMLIMPORT_SETTINGS + "/" + N_EXTHTMLIMPORT_KEEPBROKENLINKS,
            "setKeepBrokenLinks",
            0);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // generate import/export node and subnodes
        Element importexportElement = parent.addElement(N_IMPORTEXPORT);

        Element resourceloadersElement = importexportElement.addElement(N_IMPORTEXPORTHANDLERS);
        List<I_CmsImportExportHandler> handlers = m_importExportManager.getImportExportHandlers();
        Iterator<I_CmsImportExportHandler> handlerIt = handlers.iterator();
        while (handlerIt.hasNext()) {
            I_CmsImportExportHandler handler = handlerIt.next();
            // add the handler node
            Element loaderNode = resourceloadersElement.addElement(N_IMPORTEXPORTHANDLER);
            loaderNode.addAttribute(A_CLASS, handler.getClass().getName());
        }

        Element importElement = importexportElement.addElement(N_IMPORT);

        // <overwrite> node
        importElement.addElement(N_OVERWRITE).setText(
            String.valueOf(m_importExportManager.overwriteCollidingResources()));

        // <convert> node
        importElement.addElement(N_CONVERT).setText(String.valueOf(m_importExportManager.convertToXmlPage()));

        // <oldwebappurl> node
        if (m_importExportManager.getOldWebAppUrl() != null) {
            importElement.addElement(N_OLDWEBAPPURL).setText(m_importExportManager.getOldWebAppUrl());
        }

        // <importversions> node
        Element resourcetypesElement = importElement.addElement(N_IMPORTVERSIONS);
        Iterator<I_CmsImport> importVersions = m_importExportManager.getImportVersionClasses().iterator();
        while (importVersions.hasNext()) {
            resourcetypesElement.addElement(N_IMPORTVERSION).addAttribute(
                A_CLASS,
                importVersions.next().getClass().getName());
        }

        // <immutables> node
        Element immutablesElement = importElement.addElement(N_IMMUTABLES);
        Iterator<String> immutables = m_importExportManager.getImmutableResources().iterator();
        while (immutables.hasNext()) {
            String uri = immutables.next();
            immutablesElement.addElement(N_RESOURCE).addAttribute(A_URI, uri);
        }

        // <principaltranslations> node
        Element principalsElement = importElement.addElement(N_PRINCIPALTRANSLATIONS);
        Iterator<String> userTranslationKeys = m_importExportManager.getImportUserTranslations().keySet().iterator();
        while (userTranslationKeys.hasNext()) {
            String from = userTranslationKeys.next();
            String to = m_importExportManager.getImportUserTranslations().get(from);
            principalsElement.addElement(N_PRINCIPALTRANSLATION).addAttribute(
                A_TYPE,
                I_CmsPrincipal.PRINCIPAL_USER).addAttribute(A_FROM, from).addAttribute(A_TO, to);
        }
        Iterator<String> groupTranslationKeys = m_importExportManager.getImportGroupTranslations().keySet().iterator();
        while (groupTranslationKeys.hasNext()) {
            String from = groupTranslationKeys.next();
            String to = m_importExportManager.getImportGroupTranslations().get(from);
            principalsElement.addElement(N_PRINCIPALTRANSLATION).addAttribute(
                A_TYPE,
                I_CmsPrincipal.PRINCIPAL_GROUP).addAttribute(A_FROM, from).addAttribute(A_TO, to);
        }

        // <ignoredproperties> node
        Element propertiesElement = importElement.addElement(N_IGNOREDPROPERTIES);
        Iterator<String> ignoredProperties = m_importExportManager.getIgnoredProperties().iterator();
        while (ignoredProperties.hasNext()) {
            String property = ignoredProperties.next();
            propertiesElement.addElement(N_PROPERTY).addAttribute(A_NAME, property);
        }

        // <export> node
        Element exportElement = importexportElement.addElement(N_EXPORT);
        Map<TimestampMode, List<String>> defaultTimestampModes = m_importExportManager.getDefaultTimestampModes();
        if (!defaultTimestampModes.isEmpty()) {

            // <defaulttimestampmodes>
            Element defaultTimestampModesElement = exportElement.addElement(N_EXPORT_DEFAULTTIMESTAMPMODES);
            for (TimestampMode mode : defaultTimestampModes.keySet()) {

                // <timestampmode>
                Element timestampModeElement = defaultTimestampModesElement.addElement(N_EXPORT_TIMESTAMPMODE);
                timestampModeElement.addAttribute(A_MODE, mode.toString().toLowerCase());
                for (String resourcetypeName : defaultTimestampModes.get(mode)) {

                    // <resourcetypename>
                    Element resourcetypeElement = timestampModeElement.addElement(N_EXPORT_RESOURCETYPENAME);
                    resourcetypeElement.addText(resourcetypeName);
                }
            }
        }

        // <staticexport> node
        Element staticexportElement = parent.addElement(N_STATICEXPORT);
        staticexportElement.addAttribute(A_ENABLED, m_staticExportManager.getExportEnabled());

        // <staticexporthandler> node
        staticexportElement.addElement(N_STATICEXPORT_HANDLER).addText(
            m_staticExportManager.getHandler().getClass().getName());

        // <linksubstitutionhandler> node
        staticexportElement.addElement(N_LINKSUBSTITUTION_HANDLER).addText(
            m_staticExportManager.getLinkSubstitutionHandler().getClass().getName());

        // <exportpath> node
        String exportPathUnmodified = m_staticExportManager.getExportPathForConfiguration();
        // cut path seperator
        if (exportPathUnmodified.endsWith(File.separator)) {
            exportPathUnmodified = exportPathUnmodified.substring(0, exportPathUnmodified.length() - 1);
        }
        staticexportElement.addElement(N_STATICEXPORT_EXPORTPATH).addText(exportPathUnmodified);

        // <exportworkpath> node
        String exportWorkPathUnmodified = m_staticExportManager.getExportWorkPathForConfiguration();
        if (exportWorkPathUnmodified != null) {
            // cut path seperator
            if (exportWorkPathUnmodified.endsWith(File.separator)) {
                exportWorkPathUnmodified = exportWorkPathUnmodified.substring(0, exportWorkPathUnmodified.length() - 1);
            }
            staticexportElement.addElement(N_STATICEXPORT_EXPORTWORKPATH).addText(exportWorkPathUnmodified);
        }

        // <exportbackups> node
        if (m_staticExportManager.getExportBackups() != null) {
            String exportBackupsUnmodified = String.valueOf(m_staticExportManager.getExportBackups());
            staticexportElement.addElement(N_STATICEXPORT_EXPORTBACKUPS).addText(exportBackupsUnmodified);
        }

        // <defaultpropertyvalue> node
        staticexportElement.addElement(N_STATICEXPORT_DEFAULT).addText(m_staticExportManager.getDefault());

        // <defaultsuffixes> node and its <suffix> sub nodes
        Element defaultsuffixesElement = staticexportElement.addElement(N_STATICEXPORT_DEFAULTSUFFIXES);

        Iterator<String> exportSuffixes = m_staticExportManager.getExportSuffixes().iterator();
        while (exportSuffixes.hasNext()) {
            String suffix = exportSuffixes.next();
            Element suffixElement = defaultsuffixesElement.addElement(N_STATICEXPORT_SUFFIX);
            suffixElement.addAttribute(A_KEY, suffix);
        }

        // <exportheaders> node and its <header> sub nodes
        Iterator<String> exportHandlers = m_staticExportManager.getExportHeaders().iterator();
        if (exportHandlers.hasNext()) {
            Element exportheadersElement = staticexportElement.addElement(N_STATICEXPORT_EXPORTHEADERS);
            while (exportHandlers.hasNext()) {
                String header = exportHandlers.next();
                exportheadersElement.addElement(N_STATICEXPORT_HEADER).addText(header);
            }
        }
        // <requestheaders> node and the <acceptlanguage> and <acceptcharset> node
        String acceptlanguage = m_staticExportManager.getAcceptLanguageHeader();
        String acceptcharset = m_staticExportManager.getAcceptCharsetHeader();
        String remoteaddr = m_staticExportManager.getRemoteAddr();
        if ((acceptlanguage != null) || (acceptcharset != null) || (remoteaddr != null)) {
            Element requestheadersElement = staticexportElement.addElement(N_STATICEXPORT_REQUESTHEADERS);
            if (acceptlanguage != null) {
                requestheadersElement.addElement(N_STATICEXPORT_ACCEPTLANGUAGE).addText(acceptlanguage);
            }
            if (acceptcharset != null) {
                requestheadersElement.addElement(N_STATICEXPORT_ACCEPTCHARSET).addText(acceptcharset);
            }
            if (remoteaddr != null) {
                requestheadersElement.addElement(N_STATICEXPORT_REMOTEADDR).addText(remoteaddr);
            }
        }

        // <rendersettings> node
        Element rendersettingsElement = staticexportElement.addElement(N_STATICEXPORT_RENDERSETTINGS);

        // <rfsPrefix> node
        rendersettingsElement.addElement(N_STATICEXPORT_RFS_PREFIX).addText(
            m_staticExportManager.getRfsPrefixForConfiguration());

        // <vfsPrefix> node
        rendersettingsElement.addElement(N_STATICEXPORT_VFS_PREFIX).addText(
            m_staticExportManager.getVfsPrefixForConfiguration());

        // <userelativelinks> node
        rendersettingsElement.addElement(N_STATICEXPORT_RELATIVELINKS).addText(
            m_staticExportManager.getRelativeLinks());

        // <exporturl> node
        rendersettingsElement.addElement(N_STATICEXPORT_EXPORTURL).addText(
            m_staticExportManager.getExportUrlForConfiguration());

        // <plainoptimization> node
        rendersettingsElement.addElement(N_STATICEXPORT_PLAINOPTIMIZATION).addText(
            m_staticExportManager.getPlainExportOptimization());

        // <testresource> node
        Element testresourceElement = rendersettingsElement.addElement(N_STATICEXPORT_TESTRESOURCE);
        testresourceElement.addAttribute(A_URI, m_staticExportManager.getTestResource());

        // <resourcestorender> node and <regx> subnodes
        Element resourcetorenderElement = rendersettingsElement.addElement(N_STATICEXPORT_RESOURCESTORENDER);

        Iterator<String> exportFolderPatterns = m_staticExportManager.getExportFolderPatterns().iterator();
        while (exportFolderPatterns.hasNext()) {
            String pattern = exportFolderPatterns.next();
            resourcetorenderElement.addElement(N_STATICEXPORT_REGEX).addText(pattern);
        }

        if (!m_staticExportManager.getExportRules().isEmpty()) {
            // <export-rules> node
            Element exportRulesElement = resourcetorenderElement.addElement(N_STATICEXPORT_EXPORTRULES);

            Iterator<CmsStaticExportExportRule> exportRules = m_staticExportManager.getExportRules().iterator();
            while (exportRules.hasNext()) {
                CmsStaticExportExportRule rule = exportRules.next();
                // <export-rule> node
                Element exportRuleElement = exportRulesElement.addElement(N_STATICEXPORT_EXPORTRULE);
                exportRuleElement.addElement(N_STATICEXPORT_NAME).addText(rule.getName());
                exportRuleElement.addElement(N_STATICEXPORT_DESCRIPTION).addText(rule.getDescription());
                // <modified-resources> node and <regex> subnodes
                Element modifiedElement = exportRuleElement.addElement(N_STATICEXPORT_MODIFIED);
                Iterator<Pattern> itMods = rule.getModifiedResources().iterator();
                while (itMods.hasNext()) {
                    Pattern regex = itMods.next();
                    modifiedElement.addElement(N_STATICEXPORT_REGEX).addText(regex.pattern());
                }
                // <export-resources> node and <uri> subnodes
                Element exportResourcesElement = exportRuleElement.addElement(N_STATICEXPORT_EXPORT);
                Iterator<String> itExps = rule.getExportResourcePatterns().iterator();
                while (itExps.hasNext()) {
                    String uri = itExps.next();
                    exportResourcesElement.addElement(N_STATICEXPORT_URI).addText(uri);
                }
            }
        }

        if (!m_staticExportManager.getRfsRules().isEmpty()) {
            // <rfs-rules> node
            Element rfsRulesElement = rendersettingsElement.addElement(N_STATICEXPORT_RFS_RULES);

            Iterator<CmsStaticExportRfsRule> rfsRules = m_staticExportManager.getRfsRules().iterator();
            while (rfsRules.hasNext()) {
                CmsStaticExportRfsRule rule = rfsRules.next();
                // <rfs-rule> node and subnodes
                Element rfsRuleElement = rfsRulesElement.addElement(N_STATICEXPORT_RFS_RULE);
                rfsRuleElement.addElement(N_STATICEXPORT_NAME).addText(rule.getName());
                rfsRuleElement.addElement(N_STATICEXPORT_DESCRIPTION).addText(rule.getDescription());
                rfsRuleElement.addElement(N_STATICEXPORT_SOURCE).addText(rule.getSource().pattern());
                rfsRuleElement.addElement(N_STATICEXPORT_RFS_PREFIX).addText(rule.getRfsPrefixConfigured());
                rfsRuleElement.addElement(N_STATICEXPORT_EXPORTPATH).addText(rule.getExportPathConfigured());
                if (rule.getExportWorkPathConfigured() != null) {
                    rfsRuleElement.addElement(N_STATICEXPORT_EXPORTWORKPATH).addText(
                        rule.getExportWorkPathConfigured());
                }
                if (rule.getExportBackups() != null) {
                    rfsRuleElement.addElement(N_STATICEXPORT_EXPORTBACKUPS).addText(
                        String.valueOf(rule.getExportBackups()));
                }
                if (rule.getUseRelativeLinks() != null) {
                    rfsRuleElement.addElement(N_STATICEXPORT_RELATIVELINKS).addText(
                        rule.getUseRelativeLinks().toString());
                }
                Element relatedSystemRes = rfsRuleElement.addElement(N_STATICEXPORT_RELATED_SYSTEM_RES);
                Iterator<Pattern> itSystemRes = rule.getRelatedSystemResources().iterator();
                while (itSystemRes.hasNext()) {
                    Pattern sysRes = itSystemRes.next();
                    relatedSystemRes.addElement(N_STATICEXPORT_REGEX).addText(sysRes.pattern());
                }
            }

        }

        if (m_importExportManager.getUserExportSettings() != null) {
            // <usercsvexport>
            Element userExportElement = parent.addElement(N_USERCSVEXPORT);

            userExportElement.addElement(N_SEPARATOR).setText(
                m_importExportManager.getUserExportSettings().getSeparator());
            Element exportColumns = userExportElement.addElement(N_COLUMNS);
            List<String> exportColumnList = m_importExportManager.getUserExportSettings().getColumns();
            Iterator<String> itExportColumnList = exportColumnList.iterator();
            while (itExportColumnList.hasNext()) {
                exportColumns.addElement(N_COLUMN).setText(itExportColumnList.next());
            }
            // </usercsvexport>
        }

        if (m_repositoryManager.isConfigured()) {
            List<I_CmsRepository> repositories = m_repositoryManager.getRepositories();
            if (repositories != null) {

                // <repositories> node
                Element repositoriesElement = parent.addElement(N_REPOSITORIES);

                Iterator<I_CmsRepository> repositoriesIt = repositories.iterator();
                while (repositoriesIt.hasNext()) {

                    // <repository> node
                    I_CmsRepository repository = repositoriesIt.next();
                    Element repositoryElement = repositoriesElement.addElement(N_REPOSITORY);
                    repositoryElement.addAttribute(A_NAME, repository.getName());
                    repositoryElement.addAttribute(A_CLASS, repository.getClass().getName());

                    // <params> node
                    CmsParameterConfiguration config = repository.getConfiguration();
                    if ((config != null) && (config.size() > 0)) {
                        Element paramsElement = repositoryElement.addElement(N_PARAMS);
                        config.appendToXml(paramsElement);
                    }

                    // <filter> node
                    CmsRepositoryFilter filter = repository.getFilter();
                    if (filter != null) {
                        List<Pattern> rules = filter.getFilterRules();
                        if (rules.size() > 0) {
                            Element filterElement = repositoryElement.addElement(N_FILTER);
                            filterElement.addAttribute(A_TYPE, filter.getType());

                            // <regex> nodes
                            Iterator<Pattern> it = rules.iterator();
                            while (it.hasNext()) {
                                Pattern rule = it.next();
                                filterElement.addElement(N_REGEX).addText(rule.pattern());
                            }
                        }
                    }
                }
            }
        }
        CmsExtendedHtmlImportDefault htmlimport = m_importExportManager.getExtendedHtmlImportDefault(true);
        if (htmlimport != null) {
            // <extendedhtmlimport>
            Element htmlImportElement = parent.addElement(N_EXTHTMLIMPORT);
            // <destination> node
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(htmlimport.getDestinationDir())) {
                htmlImportElement.addElement(N_EXTHTMLIMPORT_DESTINATION).setText(htmlimport.getDestinationDir());
            }
            // <input> node
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(htmlimport.getInputDir())) {
                htmlImportElement.addElement(N_EXTHTMLIMPORT_INPUT).setText(htmlimport.getInputDir());
            }

            // <galleries> node
            Element galleryElement = htmlImportElement.addElement(N_EXTHTMLIMPORT_GALLERIES);
            // <download> node
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(htmlimport.getDownloadGallery())) {
                galleryElement.addElement(N_EXTHTMLIMPORT_DOWNLOAD).setText(htmlimport.getDownloadGallery());
            }
            // <image> node
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(htmlimport.getImageGallery())) {
                galleryElement.addElement(N_EXTHTMLIMPORT_IMAGE).setText(htmlimport.getImageGallery());
            }
            // <externallink> node
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(htmlimport.getLinkGallery())) {
                galleryElement.addElement(N_EXTHTMLIMPORT_EXTERNALLINK).setText(htmlimport.getLinkGallery());
            }

            // <settings> node
            Element settingElement = htmlImportElement.addElement(N_EXTHTMLIMPORT_SETTINGS);
            // <template> node
            settingElement.addElement(N_EXTHTMLIMPORT_TEMPLATE).setText(htmlimport.getTemplate());
            // <element> node
            settingElement.addElement(N_EXTHTMLIMPORT_ELEMENT).setText(htmlimport.getElement());
            // <locale> node
            settingElement.addElement(N_EXTHTMLIMPORT_LOCALE).setText(htmlimport.getLocale());
            // <encoding> node
            settingElement.addElement(N_EXTHTMLIMPORT_ENCODING).setText(htmlimport.getEncoding());

            // <pattern> node
            Element patternElement = settingElement.addElement(N_EXTHTMLIMPORT_PATTERN);
            // <start> node
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(htmlimport.getStartPattern())) {
                patternElement.addElement(N_EXTHTMLIMPORT_PATTERN_START).setText(htmlimport.getStartPattern());
            }
            // <end> node
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(htmlimport.getEndPattern())) {
                patternElement.addElement(N_EXTHTMLIMPORT_PATTERN_END).setText(htmlimport.getEndPattern());
            }

            // <overwrite> node
            settingElement.addElement(N_EXTHTMLIMPORT_OVERWRITE).setText(htmlimport.getOverwrite());
            // <keepbrokenlinks> node
            settingElement.addElement(N_EXTHTMLIMPORT_KEEPBROKENLINKS).setText(htmlimport.getKeepBrokenLinks());
            // </extendedhtmlimport>
        }

        // return the configured node
        return importexportElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the initialized import/export manager.<p>
     *
     * @return the initialized import/export manager
     */
    public CmsImportExportManager getImportExportManager() {

        return m_importExportManager;
    }

    /**
     * Returns the initialized repository manager.<p>
     *
     * @return the initialized repository manager
     */
    public CmsRepositoryManager getRepositoryManager() {

        return m_repositoryManager;
    }

    /**
     * Returns the initialized static export manager.<p>
     *
     * @return the initialized static export manager
     */
    public CmsStaticExportManager getStaticExportManager() {

        return m_staticExportManager;
    }

    /**
     * Will be called when configuration of this object is finished.<p>
     */
    public void initializeFinished() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_IMPORT_CONFIG_FINISHED_0));
        }
    }

    /**
     * Sets the extendedHtmlImportManager.<p>
     *
     * @param extendedHtmlImportManager the extendedHtmlImportManager to set
     */
    public void setExtendedHtmlImportManager(CmsExtendedHtmlImportDefault extendedHtmlImportManager) {

        m_importExportManager.setExtendedHtmlImportDefault(extendedHtmlImportManager);
    }

    /**
     * Sets the generated import/export manager.<p>
     *
     * @param manager the import/export manager to set
     */
    public void setImportExportManager(CmsImportExportManager manager) {

        m_importExportManager = manager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_IMPORT_MANAGER_0));
        }
    }

    /**
     * Sets the generated repository manager.<p>
     *
     * @param manager the repository manager to set
     */
    public void setRepositoryManager(CmsRepositoryManager manager) {

        m_repositoryManager = manager;
    }

    /**
     * Sets the generated static export manager.<p>
     *
     * @param manager the static export manager to set
     */
    public void setStaticExportManager(CmsStaticExportManager manager) {

        m_staticExportManager = manager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STATEXP_MANAGER_0));
        }
    }

    /**
     * Sets the user settings for export and import.<p>
     *
     * @param userExportSettings the user settings for export and import
     */
    public void setUserExportSettings(CmsUserExportSettings userExportSettings) {

        m_importExportManager.setUserExportSettings(userExportSettings);
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_IMPORT_CONFIG_INIT_0));
        }
    }

}