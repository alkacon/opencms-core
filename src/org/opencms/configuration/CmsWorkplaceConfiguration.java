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

import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementDeleteMode;
import org.opencms.ade.upload.I_CmsUploadRestriction;
import org.opencms.ade.upload.I_CmsVirusScanner;
import org.opencms.configuration.preferences.I_CmsPreference;
import org.opencms.db.CmsExportPoint;
import org.opencms.file.types.CmsResourceTypeSubsitemapContentFolder;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsAccountInfo;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceUserInfoBlock;
import org.opencms.workplace.CmsWorkplaceUserInfoEntry;
import org.opencms.workplace.CmsWorkplaceUserInfoManager;
import org.opencms.workplace.I_CmsDialogHandler;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.workplace.editors.I_CmsPreEditorActionDefinition;
import org.opencms.workplace.explorer.CmsExplorerTypeAccess;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsIconRule;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.workplace.tools.CmsToolRootHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester3.CallMethodRule;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;

import org.dom4j.Element;
import org.xml.sax.Attributes;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Import/Export master configuration class.<p>
 *
 * @since 6.0.0
 */
public class CmsWorkplaceConfiguration extends A_CmsXmlConfiguration {

    /** The name of the attribute addinfo-key for account infos. */
    public static final String A_ADD_INFO_KEY = "addinfo-key";

    /** The "autosetnavigation" attribute. */
    public static final String A_AUTOSETNAVIGATION = "autosetnavigation";

    /** The "autosettitle" attribute. */
    public static final String A_AUTOSETTITLE = "autosettitle";

    /** The name of the attribute containing the name of the big icon. */
    public static final String A_BIGICON = "bigicon";

    /** The name of the attribute containing the name of the big icon style class. */
    public static final String A_BIGICONSTYLE = "bigiconstyle";

    /** The 'creatable' attribute. */
    public static final String A_CREATABLE = "creatable";

    /** The 'description' attribute. */
    public static final String A_DESCRIPTION = "description";

    /** The "displayByRepository" attribute. */
    public static final String A_DISPLAY_BY_REPOSITORY = "displayByRepository";

    /** The "displayCollapsed" attribute. */
    public static final String A_DISPLAY_COLLAPSED = "displayCollapsed";

    /** The name of the attribute editable for account infos. */
    public static final String A_EDITABLE = "editable";

    /** The 'error' attribute. */
    public static final String A_ERROR = "error";

    /** The name of the attribute for file extensions in icon rules. */
    public static final String A_EXTENSION = "extension";

    /** The name of the attribute field for account infos. */
    public static final String A_FIELD = "field";

    /** The "info" attribute. */
    public static final String A_INFO = "info";

    /** The isview attribute. */
    public static final String A_ISVIEW = "isview";

    /** The name pattern attrribute. */
    public static final String A_NAME_PATTERN = "name-pattern";

    /** The 'widget-config' attribute. */
    public static final String A_NICE_NAME = "nice-name";

    /** The attribute name of the optional attribute for the user-info node. */
    public static final String A_OPTIONAL = "optional";

    /** The "page" attribute. */
    public static final String A_PAGE = "page";

    /** The "params" attribute. */
    public static final String A_PARAMS = "params";

    /** The "parent" attribute. */
    public static final String A_PARENT = "parent";

    /** The "path" attribute. */
    public static final String A_PATH = "path";

    /** The "permissions" attribute. */
    public static final String A_PERMISSIONS = "permissions";

    /** The "principal" attribute. */
    public static final String A_PRINCIPAL = "principal";

    /** The "reference" attribute. */
    public static final String A_REFERENCE = "reference";

    /** The "replace" attribute. */
    public static final String A_REPLACE = "replace";

    /** The "rule" attribute. */
    public static final String A_RULE = "rule";

    /** The 'rule-regex' attribute. */
    public static final String A_RULE_REGEX = "rule-regex";

    /** The "rules" attribute. */
    public static final String A_RULES = "rules";

    /** The "shownavigation" attribute. */
    public static final String A_SHOWNAVIGATION = "shownavigation";

    /** The name of the attribute containing the name of the small icon style class. */
    public static final String A_SMALLICONSTYLE = "smalliconstyle";

    /** The "tab" attribute. */
    public static final String A_TAB = "tab";

    /** The "target" attribute. */
    public static final String A_TARGET = "target";

    /** Name of the attribute used to determine order of types in element views. */
    public static final String A_VIEW_ORDER = "view-order";

    /** The attribute name of the widget attribute for the user-info node. */
    public static final String A_WIDGET = "widget";

    /** The 'widget-config' attribute. */
    public static final String A_WIDGET_CONFIG = "widget-config";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-workplace.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-workplace.xml";

    /** Name of the acacia-unlock node. */
    public static final String N_ACACIA_UNLOCK = "acacia-unlock";

    /** The name of the access control node. */
    public static final String N_ACCESSCONTROL = "accesscontrol";

    /** The name of the access entry node. */
    public static final String N_ACCESSENTRY = "accessentry";

    /** The name of the account info node. */
    public static final String N_ACCOUNTINFO = "account-info";

    /** The name of the account infos node. */
    public static final String N_ACCOUNTINFOS = "account-infos";

    /** The "allow-element-author-to-work-in-galleries" element */
    public static final String N_ALLOW_ELEMENT_AUTHOR_TO_WORK_IN_GALLERIES = "allow-element-author-to-work-in-galleries";

    /** The name of the "allow broken relations" node. */
    public static final String N_ALLOWBROKENRELATIONS = "allowbrokenrelations";

    /** The name of the autolock node. */
    public static final String N_AUTOLOCK = "autolock";

    /** The name of the background color node. */
    public static final String N_BACKGROUNDCOLOR = "background-color";

    /** The node name of the buttonstyle node. */
    public static final String N_BUTTONSTYLE = "buttonstyle";

    /** The name of the category display options node. */
    public static final String N_CATEGORYDISPLAYOPTIONS = "categorydisplayoptions";

    /** The name of the category folder node. */
    public static final String N_CATEGORYFOLDER = "categoryfolder";

    /** The name of the color node. */
    public static final String N_COLOR = "color";

    /** The node name of the datecreated column node. */
    public static final String N_DATECREATED = "show-datecreated";

    /** The node name of the date expired column node. */
    public static final String N_DATEEXPIRED = "show-dateexpired";

    /** The node name of the datelastmodified column node. */
    public static final String N_DATELASTMODIFIED = "show-datelastmodified";

    /** The node name of the date released  column node. */
    public static final String N_DATERELEASED = "show-datereleased";

    /** The name of the default access control node. */
    public static final String N_DEFAULTACCESSCONTROL = "defaultaccesscontrol";

    /** The name of the node for the default locale. */
    public static final String N_DEFAULTLOCALE = "defaultlocale";

    /** The node name of the default preferences node. */
    public static final String N_DEFAULTPREFERENCES = "default-preferences";

    /** The name of the default properties node. */
    public static final String N_DEFAULTPROPERTIES = "defaultproperties";

    /** The name of the default properties on structure node. */
    public static final String N_DEFAULTPROPERTIESONSTRUCTURE = "defaultpropertiesonstructure";

    /** The name of the default property node. */
    public static final String N_DEFAULTPROPERTY = "defaultproperty";

    /** Individual workplace handler node name. */
    public static final String N_DIALOGHANDLER = "dialoghandler";

    /** The main workplace handler node name. */
    public static final String N_DIALOGHANDLERS = "dialoghandlers";

    /** The node name of the dialogs defaultsettings node. */
    public static final String N_DIALOGSDEFAULTSETTINGS = "dialogs-defaultsettings";

    /** The node name of the dialogs preferences node. */
    public static final String N_DIALOGSPREFERENCES = "dialogs-preferences";

    /** The node name of the direct edit provider node. */
    public static final String N_DIRECTEDITPROVIDER = "directeditprovider";

    /** The node name of the directedit style node. */
    public static final String N_DIRECTEDITSTYLE = "directeditstyle";

    /** The node name of the directpublish node. */
    public static final String N_DIRECTPUBLISH = "directpublish";

    /** The name of the edit options node. */
    public static final String N_EDITOPTIONS = "editoptions";

    /** The node name of the editor node. */
    public static final String N_EDITOR = "editor";

    /** The name of the editor action node. */
    public static final String N_EDITORACTION = "editoraction";

    /** The name of the editor css handler node. */
    public static final String N_EDITORCSSHANDLER = "editorcsshandler";

    /** The name of the editor css handlers node. */
    public static final String N_EDITORCSSHANDLERS = "editorcsshandlers";

    /** The node name of the editors general options node. */
    public static final String N_EDITORGENERALOPTIONS = "editors-generaloptions";

    /** The name of the editor handler node. */
    public static final String N_EDITORHANDLER = "editorhandler";

    /** The name of the editorprecondition node. */
    public static final String N_EDITORPRECONDITION = "editorprecondition";

    /** The name of the editorpreconditions node. */
    public static final String N_EDITORPRECONDITIONS = "editorpreconditions";

    /** The node name of the editors preferences node. */
    public static final String N_EDITORPREFERENCES = "editors-preferences";

    /** The node name of the editors preferred editors node. */
    public static final String N_EDITORPREFERREDEDITORS = "editors-preferrededitors";

    /** The element delete mode node name. */
    public static final String N_ELEMENT_DELETE_MODE = "element-delete-mode";

    /** The name of the "enable advanced property tabs" node. */
    public static final String N_ENABLEADVANCEDPROPERTYTABS = "enableadvancedpropertytabs";

    /** The subname of the rfsfilesettings/enabled node. */
    public static final String N_ENABLED = "enabled";

    /** The name of the "user management enabled" node. */
    public static final String N_ENABLEUSERMGMT = "enableusermanagement";

    /** The node name of the file entries node. */
    public static final String N_ENTRIES = "entries";

    /** The name of the entry node. */
    public static final String N_ENTRY = "entry";

    /** The node name of the file entryoptions node. */
    public static final String N_ENTRYOPTIONS = "entryoptions";

    /** The name of the exclude-pattern node. */
    public static final String N_EXCLUDEPATTERN = "exclude-pattern";

    /** The name of the expand inherited permissions node. */
    public static final String N_EXPANDPERMISSIONSINHERITED = "expand-permissionsinherited";

    /** The name of the expand user permissions node. */
    public static final String N_EXPANDPERMISSIONSUSER = "expand-permissionsuser";

    /** The node name of the explorer displayoptions node. */
    public static final String N_EXPLORERDISPLAYOPTIONS = "explorer-displayoptions";

    /** The node name of the explorer generaloptions node. */
    public static final String N_EXPLORERGENERALOPTIONS = "explorer-generaloptions";

    /** The node name of the explorer preferences node. */
    public static final String N_EXPLORERPREFERENCES = "explorer-preferences";

    /** The name of the explorer type node. */
    public static final String N_EXPLORERTYPE = "explorertype";

    /** The name of the explorer types node. */
    public static final String N_EXPLORERTYPES = "explorertypes";

    /** The node name of the file copy node. */
    public static final String N_FILECOPY = "filecopy";

    /** The node name of the file deletion node. */
    public static final String N_FILEDELETION = "filedeletion";

    /** The subname of the rfsfilesettings/fileEncoding node. */
    public static final String N_FILEENCODING = "fileEncoding";

    /** The subname of the rfsfilesettings/filePath node. */
    public static final String N_FILEPATH = "filePath";

    /** The node name of the start folder node. */
    public static final String N_FOLDER = "folder";

    /** The node name of the folder copy node. */
    public static final String N_FOLDERCOPY = "foldercopy";

    /** The node name of the gallery preferences node. */
    public static final String N_GALLERIESPREFERENCES = "galleries-preferences";

    /** Node name. */
    public static final String N_GALLERY_DEFAULT_SCOPE = "gallery-default-scope";

    /** The node name of the group-translation node. */
    public static final String N_GROUP_TRANSLATION = "group-translation";

    /** The node name of the helptext node. */
    public static final String N_HELPTEXT = "helptext";

    /** The name of the icon rule node. */
    public static final String N_ICONRULE = "iconrule";

    /** The name of the icon rules node. */
    public static final String N_ICONRULES = "iconrules";

    /** The node name of the info-block node. */
    public static final String N_INFOBLOCK = "info-block";

    /** The subname of the rfsfilesettings/isLogfile node. */
    public static final String N_ISLOGFILE = "isLogfile";

    /** Name of the "keep alive" setting node. */
    public static final String N_KEEP_ALIVE = "keep-alive";

    /** The node name of the key node. */
    public static final String N_KEY = "key";

    /** The name of the "labeled folders" node. */
    public static final String N_LABELEDFOLDERS = "labeledfolders";

    /** The node name of the list all projects node. */
    public static final String N_LISTALLPROJECTS = "listallprojects";

    /** The node name of the locale node. */
    public static final String N_LOCALE = "locale";

    /** The name of the "localized folders" node. */
    public static final String N_LOCALIZEDFOLDERS = "localizedfolders";

    /** The node name of the lockedby column node. */
    public static final String N_LOCKEDBY = "show-lockedby";

    /** The name of the "max file upload size" node. */
    public static final String N_MAXUPLOADSIZE = "maxfileuploadsize";

    /** The node name of the navtext column node. */
    public static final String N_NAVTEXT = "show-navtext";

    /** The name of the "create new folder with index page" node. */
    public static final String N_NEWFOLDERCREATEINDEXPAGE = "newfolder-createindexpage";

    /** The name of the "create new folder with edit properties" node. */
    public static final String N_NEWFOLDEREDITPROPERTIES = "newfolder-editproperties";

    /** The name of the new resource node. */
    public static final String N_NEWRESOURCE = "newresource";

    /** The node name of the permissions column node. */
    public static final String N_PERMISSIONS = "show-permissions";

    /** The name of the inherit permissions on folder node. */
    public static final String N_PERMISSIONSINHERITONFOLDER = "permissions-inheritonfolder";

    /** The name of the editor handler node. */
    public static final String N_POSTUPLOADHANDLER = "postuploadhandler";

    /** The node name for a preference. */
    public static final String N_PREFERENCE = "preference";

    /** The name of the preference-tab element. */
    public static final String N_PREFERENCE_TAB = "preference-tab";

    /** The node name of the project node. */
    public static final String N_PROJECT = "project";

    /** The node name of the publish button appearance node. */
    public static final String N_PUBLISHBUTTONAPPEARANCE = "publishbuttonappearance";

    /** The node name of the publish notification node. */
    public static final String N_PUBLISHNOTIFICATION = "publishnotification";

    /** The name of the "publish related resources" node. */
    public static final String N_PUBLISHRELATEDRESOURCES = "publishrelatedresources";

    /** The node name of the report type node. */
    public static final String N_REPORTTYPE = "reporttype";

    /** The node name of the gallery upload folder handler node. */
    public static final String N_REPOSITORY_FOLDER = "repositoryfolder";

    /** The node name of the restrict explorer view node. */
    public static final String N_RESTRICTEXPLORERVIEW = "restrictexplorerview";

    /** The node name of the rfsfileviewsettings node. */
    public static final String N_RFSFILEVIEWESETTINGS = "rfsfileviewsettings";

    /** The node name of the root node. */
    public static final String N_ROOT = "root";

    /** The subname of the rfsfilesettings/rootPath node. */
    public static final String N_ROOTPATH = "rootPath";

    /** The node name of the roots node. */
    public static final String N_ROOTS = "roots";

    /** The node name of the searchindex-name node. */
    public static final String N_SEARCHINDEXNAME = "searchindex-name";

    /** The node name of the searchview-style node. */
    public static final String N_SEARCHVIEWSTYLE = "searchview-style";

    /** The name of the separator node. */
    public static final String N_SEPARATOR = "separator";

    /** The node name of the show lock node. */
    public static final String N_SHOWEXPORTSETTINGS = "showexportsettings";

    /** The node name of the "show file upload button" option. */
    public static final String N_SHOWFILEUPLOADBUTTON = "show-fileuploadbutton";

    /** The node name of the show lock node. */
    public static final String N_SHOWLOCK = "showlock";

    /** The node name of the show messages node. */
    public static final String N_SHOWMESSAGES = "showmessages";

    /** The name of the "create new folder with index page" node. */
    public static final String N_SHOWUPLOADTYPEDIALOG = "show-uploadtypedialog";

    /** The node name of the size column node. */
    public static final String N_SIZE = "show-size";

    /** The node name of the galleries start setting node. */
    public static final String N_STARTGALLERIES = "startgalleries";

    /** The node name of the start gallery node. */
    public static final String N_STARTGALLERY = "startgallery";

    /** The node name of the state column node. */
    public static final String N_STATE = "show-state";

    /** The node name for the subsitemap creation mode setting. */
    public static final String N_SUBSITEMAP_CREATION_MODE = "subsitemap-creation-mode";

    /** The name of the synchronization node. */
    public static final String N_SYNCHRONIZATION = "synchronization";

    /** The name of the text node. */
    public static final String N_TEXT = "text";

    /** The node name of the title column node. */
    public static final String N_TITLE = "show-title";

    /** The node name of the tool-manager node. */
    public static final String N_TOOLMANAGER = "tool-manager";

    /** The node name of the type column node. */
    public static final String N_TYPE = "show-type";

    /** The node name for the upload restriction configuration. */
    public static final String N_UPLOAD_RESTRICTION = "upload-restriction";

    /** The node name of the uploadapplet node. */
    public static final String N_UPLOADAPPLET = "uploadapplet";

    /** The node name of the uri node. */
    public static final String N_URI = "uri";

    /** The name of the user-lists node. */
    public static final String N_USER_LISTS = "user-lists";

    /** The node name of the user created node. */
    public static final String N_USERCREATED = "show-usercreated";

    /** The node name of the user-info node. */
    public static final String N_USERINFO = "user-info";

    /** The node name of the user-infos node. */
    public static final String N_USERINFOS = "user-infos";

    /** The node name of the user lastmodified node. */
    public static final String N_USERLASTMODIFIED = "show-userlastmodified";

    /** The node name of the virus-scanner node. */
    public static final String N_VIRUS_SCANNER = "virus-scanner";

    /** The subname of the rfsfilesettings/windowSize node. */
    public static final String N_WINDOWSIZE = "windowSize";

    /** The node name of the master workplace node. */
    public static final String N_WORKPLACE = "workplace";

    /** The node name of the workplace general options node. */
    public static final String N_WORKPLACEGENERALOPTIONS = "workplace-generaloptions";

    /** The node name of the workplace preferences node. */
    public static final String N_WORKPLACEPREFERENCES = "workplace-preferences";

    /** The node name of the workplace-search node. */
    public static final String N_WORKPLACESEARCH = "workplace-search";

    /** The node name of the workplace startup settings node. */
    public static final String N_WORKPLACESTARTUPSETTINGS = "workplace-startupsettings";

    /** The node name of the view node. */
    public static final String N_WORKPLACEVIEW = "workplaceview";

    /** The name of the xmlcontentautocorrection node. */
    public static final String N_XMLCONTENTAUTOCORRECTION = "xmlcontentautocorrection";

    /** The elementview attribute. */
    private static final String A_ELEMENTVIEW = "elementview";

    /** The requiredOnUpload attribute. */
    private static final String A_REQUIRED_ON_UPLOAD = "requiredOnUpload";

    /** Configuration node name for the role required to edit the sitemap configuration. */
    private static final String N_SITEMAP_CONFIG_EDIT_ROLE = "sitemap-config-edit-role";

    /** Configuration node name for setting the default value for the 'use formatter keys' in sitemap configurations created with new subsitemap folders. */
    private static final String N_USE_FORMATTER_KEYS_FOR_NEW_SITES = "use-formatter-keys-for-new-sites";

    /** The configured workplace manager. */
    private CmsWorkplaceManager m_workplaceManager;

    /**
     * Adds the explorer type rules to the given digester.<p>
     *
     * @param digester the digester to add the rules to
     */
    public static void addExplorerTypeXmlRules(Digester digester) {

        //TODO: remove workflow nodes from the dtd, there are just there for compatibility reasons

        // add explorer type settings
        digester.addObjectCreate("*/" + N_EXPLORERTYPE, CmsExplorerTypeSettings.class);
        digester.addSetNext("*/" + N_EXPLORERTYPE, "addExplorerTypeSetting");

        digester.addCallMethod("*/" + N_EXPLORERTYPE, "setTypeAttributes", 11);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 0, A_NAME);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 1, A_KEY);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 2, A_ICON);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 3, A_BIGICON);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 4, A_SMALLICONSTYLE);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 5, A_BIGICONSTYLE);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 6, A_REFERENCE);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 7, A_ELEMENTVIEW);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 8, A_ISVIEW);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 9, A_NAME_PATTERN);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 10, A_VIEW_ORDER);

        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_ICONRULES + "/" + N_ICONRULE, "addIconRule", 5);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ICONRULES + "/" + N_ICONRULE, 0, A_EXTENSION);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ICONRULES + "/" + N_ICONRULE, 1, A_ICON);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ICONRULES + "/" + N_ICONRULE, 2, A_BIGICON);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ICONRULES + "/" + N_ICONRULE, 3, A_SMALLICONSTYLE);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ICONRULES + "/" + N_ICONRULE, 4, A_BIGICONSTYLE);

        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setCreatable", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_CREATABLE);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setNewResourceOrder", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_ORDER);

        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setAutoSetNavigation", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_AUTOSETNAVIGATION);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setAutoSetTitle", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_AUTOSETTITLE);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setInfo", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_INFO);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setTitleKey", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_KEY);

        digester.addObjectCreate("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL, CmsExplorerTypeAccess.class);
        digester.addSetNext("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL, "setAccess");

        digester.addCallMethod(
            "*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL + "/" + N_ACCESSENTRY,
            "addAccessEntry",
            2);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL + "/" + N_ACCESSENTRY, 0, A_PRINCIPAL);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL + "/" + N_ACCESSENTRY, 1, A_PERMISSIONS);

        digester.addCallMethod(
            "*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES,
            "setPropertyDefaults",
            2);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES, 0, A_ENABLED);
        digester.addCallParam(
            "*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES,
            1,
            A_SHOWNAVIGATION);

        digester.addCallMethod(
            "*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES + "/" + N_PROPERTY,
            "addProperty",
            2);
        digester.addCallParam(
            "*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES + "/" + N_PROPERTY,
            0,
            A_NAME);

        digester.addCallParam(
            "*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES + "/" + N_PROPERTY,
            1,
            A_REQUIRED_ON_UPLOAD);

        digester.addCallMethod(
            "*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES + "/" + N_DEFAULTPROPERTY,
            "addProperty",
            2);
        digester.addCallParam(
            "*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES + "/" + N_DEFAULTPROPERTY,
            0,
            A_NAME);
        digester.addCallParam(
            "*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES + "/" + N_DEFAULTPROPERTY,
            1,
            A_REQUIRED_ON_UPLOAD);

        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS, "setEditOptions");
    }

    /**
     * Creates the xml output for explorer type nodes.<p>
     *
     * @param startNode the startnode to add all rescource types to
     * @param explorerTypes the list of explorer types
     * @param module true if the XML tree for the module config should be generated, false otherwise
     */
    public static void generateExplorerTypesXml(
        Element startNode,
        List<CmsExplorerTypeSettings> explorerTypes,
        boolean module) {

        // we need the default access node later to check if the explorer type is an individual setting
        CmsExplorerTypeAccess defaultAccess = null;
        if (OpenCms.getWorkplaceManager() != null) {
            defaultAccess = OpenCms.getWorkplaceManager().getDefaultAccess();
        }
        // get the menu rule translator to eliminate eventual legacy menu rules
        Iterator<CmsExplorerTypeSettings> i = explorerTypes.iterator();
        while (i.hasNext()) {
            // create an explorer type node
            CmsExplorerTypeSettings settings = i.next();

            if (settings.isAddititionalModuleExplorerType() == module) {
                Element explorerTypeElement = startNode.addElement(N_EXPLORERTYPE);
                explorerTypeElement.addAttribute(A_NAME, settings.getName());
                explorerTypeElement.addAttribute(A_KEY, settings.getKey());
                String originalIcon = settings.getOriginalIcon();
                if (CmsStringUtil.isNotEmpty(originalIcon)) {
                    explorerTypeElement.addAttribute(A_ICON, settings.getOriginalIcon());
                }
                if (CmsStringUtil.isNotEmpty(settings.getBigIcon())) {
                    explorerTypeElement.addAttribute(A_BIGICON, settings.getBigIcon());
                }
                if (CmsStringUtil.isNotEmpty(settings.getSmallIconStyle())) {
                    explorerTypeElement.addAttribute(A_SMALLICONSTYLE, settings.getSmallIconStyle());
                }
                if (CmsStringUtil.isNotEmpty(settings.getBigIconStyle())) {
                    explorerTypeElement.addAttribute(A_BIGICONSTYLE, settings.getBigIconStyle());
                }

                if (settings.getReference() != null) {
                    explorerTypeElement.addAttribute(A_REFERENCE, settings.getReference());
                }

                if (settings.getElementView() != null) {
                    explorerTypeElement.addAttribute(A_ELEMENTVIEW, settings.getElementView());
                }

                if (settings.isView()) {
                    explorerTypeElement.addAttribute(A_ISVIEW, "true");
                }

                if (settings.getNamePattern() != null) {
                    explorerTypeElement.addAttribute(A_NAME_PATTERN, settings.getNamePattern());
                }

                if (settings.getViewOrder(false) != null) {
                    explorerTypeElement.addAttribute(A_VIEW_ORDER, "" + settings.getViewOrder(false));
                }

                // create subnode <newresource>
                Element newResElement = explorerTypeElement.addElement(N_NEWRESOURCE);
                newResElement.addAttribute(A_CREATABLE, String.valueOf(settings.isCreatable()));
                newResElement.addAttribute(A_ORDER, settings.getNewResourceOrder());
                newResElement.addAttribute(A_AUTOSETNAVIGATION, String.valueOf(settings.isAutoSetNavigation()));
                newResElement.addAttribute(A_AUTOSETTITLE, String.valueOf(settings.isAutoSetTitle()));
                newResElement.addAttribute(A_INFO, settings.getInfo());
                newResElement.addAttribute(A_KEY, settings.getTitleKey());
                // create subnode <accesscontrol>
                CmsExplorerTypeAccess access = settings.getAccess();
                if (access != defaultAccess) {
                    // don't output the node if this is in fact the default access settings
                    List<String> accessEntries = new ArrayList<String>(access.getAccessEntries().keySet());
                    // sort accessEntries
                    Collections.sort(accessEntries);
                    if (accessEntries.size() > 0) {
                        Element accessControlElement = explorerTypeElement.addElement(N_ACCESSCONTROL);
                        Iterator<String> k = accessEntries.iterator();
                        while (k.hasNext()) {
                            String key = k.next();
                            String value = settings.getAccess().getAccessEntries().get(key);
                            Element accessEntryElement = accessControlElement.addElement(N_ACCESSENTRY);
                            accessEntryElement.addAttribute(A_PRINCIPAL, key);
                            accessEntryElement.addAttribute(A_PERMISSIONS, value);
                        }
                    }
                }
                // create subnode <editoptions>
                if (settings.hasEditOptions()) {
                    Element editOptionsElement = explorerTypeElement.addElement(N_EDITOPTIONS);
                    Element defaultPropertiesElement = editOptionsElement.addElement(N_DEFAULTPROPERTIES);
                    defaultPropertiesElement.addAttribute(A_ENABLED, String.valueOf(settings.isPropertiesEnabled()));
                    defaultPropertiesElement.addAttribute(
                        A_SHOWNAVIGATION,
                        String.valueOf(settings.isShowNavigation()));
                    Iterator<String> m = settings.getProperties().iterator();
                    while (m.hasNext()) {
                        Element propElem = defaultPropertiesElement.addElement(N_DEFAULTPROPERTY);
                        String propName = m.next();
                        if (settings.isPropertyRequiredOnUpload(propName)) {
                            propElem.addAttribute(A_REQUIRED_ON_UPLOAD, "true");
                        }
                        propElem.addAttribute(A_NAME, propName);

                    }
                }

                Map<String, CmsIconRule> iconRules = settings.getIconRules();
                if ((iconRules != null) && !iconRules.isEmpty()) {
                    Element iconRulesElem = explorerTypeElement.addElement(N_ICONRULES);
                    for (Map.Entry<String, CmsIconRule> entry : iconRules.entrySet()) {
                        CmsIconRule rule = entry.getValue();
                        Element ruleElem = iconRulesElem.addElement(N_ICONRULE);
                        String icon = rule.getIcon();
                        String bigIcon = rule.getBigIcon();
                        String extension = rule.getExtension();
                        ruleElem.addAttribute(A_EXTENSION, extension);
                        if (icon != null) {
                            ruleElem.addAttribute(A_ICON, icon);
                        }
                        if (bigIcon != null) {
                            ruleElem.addAttribute(A_BIGICON, bigIcon);
                        }
                        if (rule.getSmallIconStyle() != null) {
                            ruleElem.addAttribute(A_SMALLICONSTYLE, rule.getSmallIconStyle());
                        }
                        if (rule.getBigIconStyle() != null) {
                            ruleElem.addAttribute(A_BIGICONSTYLE, rule.getBigIconStyle());
                        }
                    }
                }

            }
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester3.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add finish rule
        digester.addCallMethod("*/" + N_WORKPLACE, "initializeFinished");

        // generic <param> parameter rules
        digester.addCallMethod(
            "*/" + I_CmsXmlConfiguration.N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 1);

        // creation of the import/export manager
        digester.addObjectCreate("*/" + N_WORKPLACE, CmsWorkplaceManager.class);
        // import/export manager finished
        digester.addSetNext("*/" + N_WORKPLACE, "setWorkplaceManager");

        String path = "*/" + N_WORKPLACE + "/" + N_KEEP_ALIVE;
        digester.addCallMethod(path, "setKeepAlive", 0);

        // add exclude patterns
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_SYNCHRONIZATION + "/" + N_EXCLUDEPATTERN,
            "addSynchronizeExcludePattern",
            0);

        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_GALLERY_DEFAULT_SCOPE, "setGalleryDefaultScope", 0);

        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_ELEMENT_DELETE_MODE, "setElementDeleteMode", 0);

        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_USER_LISTS, "setUserListMode", 1);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_USER_LISTS, 0, A_MODE);

        // add default locale rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_DEFAULTLOCALE, "setDefaultLocale", 0);

        // add default properties on structure setting
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_DEFAULTPROPERTIESONSTRUCTURE,
            "setDefaultPropertiesOnStructure",
            0);

        // add default properties on structure setting
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_ENABLEADVANCEDPROPERTYTABS,
            "setEnableAdvancedPropertyTabs",
            0);

        // add category folder rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_CATEGORYFOLDER, "setCategoryFolder", 0);

        // add category display options
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_CATEGORYDISPLAYOPTIONS, "setCategoryDisplayOptions", 2);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_CATEGORYDISPLAYOPTIONS, 0, A_DISPLAY_BY_REPOSITORY);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_CATEGORYDISPLAYOPTIONS, 1, A_DISPLAY_COLLAPSED);

        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_GROUP_TRANSLATION, "setGroupTranslationClass", 1);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_GROUP_TRANSLATION, 0, A_CLASS);

        // add rules for dialog handlers
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER, "addDialogHandler");
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);

        // add rules for editor handler
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_EDITORHANDLER,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_EDITORHANDLER, "setEditorHandler");

        // add rules for editor handler
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_POSTUPLOADHANDLER,
            A_CLASS,
            CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_POSTUPLOADHANDLER, "setPostUploadHandler");

        // add rules for editor action handler
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_EDITORACTION,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_EDITORACTION, "setEditorAction");

        // add rules for editor css handler classes
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_EDITORCSSHANDLERS + "/" + N_EDITORCSSHANDLER,
            "addEditorCssHandler",
            1);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EDITORCSSHANDLERS + "/" + N_EDITORCSSHANDLER, 0, A_CLASS);

        // add rules for pre editor action classes
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_EDITORPRECONDITIONS + "/" + N_EDITORPRECONDITION,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addSetNext(
            "*/" + N_WORKPLACE + "/" + N_EDITORPRECONDITIONS + "/" + N_EDITORPRECONDITION,
            "addPreEditorConditionDefinition");

        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_EDITORPRECONDITIONS + "/" + N_EDITORPRECONDITION,
            "setResourceTypeName",
            1);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EDITORPRECONDITIONS + "/" + N_EDITORPRECONDITION, 0, A_NAME);

        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_EDITORPRECONDITIONS + "/" + N_EDITORPRECONDITION,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);

        // add rules for direct edit provider
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_DIRECTEDITPROVIDER,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_DIRECTEDITPROVIDER,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_DIRECTEDITPROVIDER, "setDirectEditProvider");

        // add rules for the workplace export points
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, "addExportPoint", 2);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, 0, A_URI);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, 1, A_DESTINATION);

        // add autolock rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_AUTOLOCK, "setAutoLock", 0);

        // acacia-unlock
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_ACACIA_UNLOCK, "setAcaciaUnlock", 0);

        // add XML content auto correction rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_XMLCONTENTAUTOCORRECTION, "setXmlContentAutoCorrect", 0);

        // add user management enabled rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_ENABLEUSERMGMT, "setUserManagementEnabled", 0);

        // add max file upload size rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_MAXUPLOADSIZE, "setFileMaxUploadSize", 0);

        // add labeled folders rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_LABELEDFOLDERS + "/" + N_RESOURCE, "addLabeledFolder", 1);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_LABELEDFOLDERS + "/" + N_RESOURCE, 0, A_URI);

        // set the gallery upload folder handler
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_REPOSITORY_FOLDER,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_REPOSITORY_FOLDER, "setRepositoryFolderHandler");

        // add localized folders rule
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_LOCALIZEDFOLDERS + "/" + N_RESOURCE,
            "addLocalizedFolder",
            1);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_LOCALIZEDFOLDERS + "/" + N_RESOURCE, 0, A_URI);

        // add fileViewSettings rules
        String xPathPrefix = "*/" + N_RFSFILEVIEWESETTINGS;
        digester.addObjectCreate(xPathPrefix, CmsRfsFileViewer.class);
        digester.addBeanPropertySetter(xPathPrefix + "/" + N_ROOTPATH);
        digester.addBeanPropertySetter(xPathPrefix + "/" + N_FILEPATH);
        digester.addBeanPropertySetter(xPathPrefix + "/" + N_ENABLED);
        digester.addBeanPropertySetter(xPathPrefix + "/" + N_FILEENCODING);
        digester.addBeanPropertySetter(xPathPrefix + "/" + N_ISLOGFILE);
        digester.addBeanPropertySetter(xPathPrefix + "/" + N_WINDOWSIZE);

        // Cms specific rule similar to SetNextRule with implicit first CmsObject argument (remains null).
        digester.addRule(xPathPrefix, new CmsSetNextRule("setFileViewSettings", CmsRfsFileViewer.class));

        digester.addRule("*/" + N_WORKPLACE + "/" + N_UPLOAD_RESTRICTION, new Rule() {

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                String className = attributes.getValue(A_CLASS);
                Class<? extends I_CmsUploadRestriction> cls = Class.forName(
                    className,
                    false,
                    getClass().getClassLoader()).asSubclass(I_CmsUploadRestriction.class);
                digester.push(cls.newInstance());
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                I_CmsUploadRestriction restriction = (I_CmsUploadRestriction)digester.pop();
                ((CmsWorkplaceManager)digester.peek()).setUploadRestriction(restriction);
            }
        });

        digester.addRule("*/" + N_WORKPLACE + "/" + N_VIRUS_SCANNER, new Rule() {

            private boolean m_enabled;

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                String className = attributes.getValue(A_CLASS);
                m_enabled = false;
                if (className != null) {
                    m_enabled = Boolean.parseBoolean(attributes.getValue(A_ENABLED));
                    Class<? extends I_CmsVirusScanner> cls = Class.forName(
                        className,
                        false,
                        getClass().getClassLoader()).asSubclass(I_CmsVirusScanner.class);
                    digester.push(cls.newInstance());
                }
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                if (digester.peek() instanceof I_CmsVirusScanner) { // there may be no virus scanner on the stack if the class name was empty or invalid
                    I_CmsVirusScanner scanner = (I_CmsVirusScanner)(digester.pop());
                    scanner.initConfiguration();
                    CmsWorkplaceManager wpMan = ((CmsWorkplaceManager)digester.peek());
                    wpMan.setVirusScanner(scanner);
                    wpMan.setVirusScannerEnabled(m_enabled);
                }
            }

        });

        // add explorer type rules
        addExplorerTypeXmlRules(digester);
        addDefaultAccessControlRules(digester);
        addUserInfoRules(digester);
        addAccountInfoRules(digester);
        addDefaultPreferencesRules(digester);

        addToolManagerRules(digester);
        CmsAdditionalLogFolderConfig.ADD_LOG_FOLDER_HELPER.addRules(digester);
        digester.addSetNext(
            CmsAdditionalLogFolderConfig.ADD_LOG_FOLDER_HELPER.getBasePath(),
            "setAdditionalLogFolderConfiguration");

        digester.addRule("*/" + N_WORKPLACE + "/" + N_USE_FORMATTER_KEYS_FOR_NEW_SITES, new Rule() {

            @Override
            public void body(String namespace, String name, String text) throws Exception {

                CmsResourceTypeSubsitemapContentFolder.setEnableNewPageFormatByDefault(Boolean.parseBoolean(text));
            }
        });

        digester.addRule("*/" + N_WORKPLACE + "/" + N_SITEMAP_CONFIG_EDIT_ROLE, new Rule() {

            @Override
            public void body(String namespace, String name, String text) throws Exception {

                CmsWorkplaceManager wpManager = (CmsWorkplaceManager)(digester.peek());
                wpManager.setSitemapConfigEditRole(text);
            }
        });

        digester.addRule("*/" + N_WORKPLACE + "/" + N_ALLOW_ELEMENT_AUTHOR_TO_WORK_IN_GALLERIES, new Rule() {

            @Override
            public void body(String namespace, String name, String text) throws Exception {

                CmsWorkplaceManager wpManager = (CmsWorkplaceManager)(digester.peek());
                wpManager.setAllowElementAuthorToWorkInGalleries(Boolean.valueOf(text).booleanValue());
            }
        });

    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // generate workplace node and subnodes
        Element workplaceElement = parent.addElement(N_WORKPLACE);
        // add default locale
        workplaceElement.addElement(N_DEFAULTLOCALE).setText(m_workplaceManager.getDefaultLocale().toString());

        // add <localizedfolders> subnode
        Element localizedElement = workplaceElement.addElement(N_LOCALIZEDFOLDERS);
        Iterator<String> localizedIterator = m_workplaceManager.getLocalizedFolders().iterator();
        while (localizedIterator.hasNext()) {
            // add <resource uri=""/> element(s)
            localizedElement.addElement(N_RESOURCE).addAttribute(A_URI, localizedIterator.next());
        }

        // add <dialoghandlers> subnode
        Element dialogElement = workplaceElement.addElement(N_DIALOGHANDLERS);
        Map<String, I_CmsDialogHandler> dialogs = m_workplaceManager.getDialogHandler();
        String[] keys = dialogs.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        for (int j = 0; j < keys.length; j++) {
            String name = keys[j];
            // add <dialoghandler> subnode with class attribute
            Element dialogHandler = dialogElement.addElement(N_DIALOGHANDLER).addAttribute(
                A_CLASS,
                dialogs.get(name).getClass().getName());
            I_CmsDialogHandler daialogHandlerConfig = dialogs.get(name);
            CmsParameterConfiguration handlerConfig = daialogHandlerConfig.getConfiguration();
            if (handlerConfig != null) {
                handlerConfig.appendToXml(dialogHandler);
            }
        }

        // add miscellaneous editor subnodes
        workplaceElement.addElement(N_EDITORHANDLER).addAttribute(
            A_CLASS,
            m_workplaceManager.getEditorHandler().getClass().getName());
        workplaceElement.addElement(N_EDITORACTION).addAttribute(
            A_CLASS,
            m_workplaceManager.getEditorActionHandler().getClass().getName());

        if (m_workplaceManager.getPostUploadHandler() != null) {
            workplaceElement.addElement(N_POSTUPLOADHANDLER).addAttribute(
                A_CLASS,
                m_workplaceManager.getPostUploadHandler().getClass().getName());
        }

        if (m_workplaceManager.getEditorCssHandlers().size() > 0) {
            Element editorCssHandlers = workplaceElement.addElement(N_EDITORCSSHANDLERS);
            Iterator<I_CmsEditorCssHandler> it = m_workplaceManager.getEditorCssHandlers().iterator();
            while (it.hasNext()) {
                I_CmsEditorCssHandler current = it.next();
                Element handler = editorCssHandlers.addElement(N_EDITORCSSHANDLER);
                handler.addAttribute(A_CLASS, current.getClass().getName());
            }
        }

        if (m_workplaceManager.getPreEditorConditionDefinitions().size() > 0) {
            Element editorPreActions = workplaceElement.addElement(N_EDITORPRECONDITIONS);
            Iterator<I_CmsPreEditorActionDefinition> it = m_workplaceManager.getPreEditorConditionDefinitions().iterator();
            while (it.hasNext()) {
                I_CmsPreEditorActionDefinition current = it.next();
                Element action = editorPreActions.addElement(N_EDITORPRECONDITION);
                action.addAttribute(A_NAME, current.getResourceTypeName());
                action.addAttribute(A_CLASS, current.getClass().getName());
                // get the configuration parameters
                CmsParameterConfiguration config = current.getConfiguration();
                if ((config != null) && (config.size() > 0)) {
                    // append the configuration parameters
                    config.appendToXml(action);
                }
            }
        }

        I_CmsConfigurationParameterHandler deProvider = m_workplaceManager.getDirectEditProvider();
        Element deProviderNode = workplaceElement.addElement(N_DIRECTEDITPROVIDER).addAttribute(
            A_CLASS,
            deProvider.getClass().getName());
        CmsParameterConfiguration deProviderConfig = deProvider.getConfiguration();
        if (deProviderConfig != null) {
            deProviderConfig.appendToXml(deProviderNode);
        }

        // add <exportpoints> subnode
        Element resourceloadersElement = workplaceElement.addElement(N_EXPORTPOINTS);
        Set<CmsExportPoint> points = m_workplaceManager.getExportPoints();
        Iterator<CmsExportPoint> exportPoints = points.iterator();
        while (exportPoints.hasNext()) {
            CmsExportPoint point = exportPoints.next();
            resourceloadersElement.addElement(N_EXPORTPOINT).addAttribute(A_URI, point.getUri()).addAttribute(
                A_DESTINATION,
                point.getConfiguredDestination());
        }

        // add miscellaneous configuration nodes
        workplaceElement.addElement(N_AUTOLOCK).setText(String.valueOf(m_workplaceManager.autoLockResources()));
        String acaciaUnlock = m_workplaceManager.getAcaciaUnlock();
        if (acaciaUnlock != null) {
            workplaceElement.addElement(N_ACACIA_UNLOCK).setText(acaciaUnlock);
        }

        workplaceElement.addElement(N_XMLCONTENTAUTOCORRECTION).setText(
            String.valueOf(m_workplaceManager.isXmlContentAutoCorrect()));
        workplaceElement.addElement(N_ENABLEUSERMGMT).setText(String.valueOf(m_workplaceManager.showUserGroupIcon()));
        workplaceElement.addElement(N_DEFAULTPROPERTIESONSTRUCTURE).setText(
            String.valueOf(m_workplaceManager.isDefaultPropertiesOnStructure()));
        workplaceElement.addElement(N_ENABLEADVANCEDPROPERTYTABS).setText(
            String.valueOf(m_workplaceManager.isEnableAdvancedPropertyTabs()));

        // add <categoryfolder> node
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_workplaceManager.getCategoryFolder())
            && !CmsCategoryService.REPOSITORY_BASE_FOLDER.equals(m_workplaceManager.getCategoryFolder())) {
            workplaceElement.addElement(N_CATEGORYFOLDER).setText(
                String.valueOf(m_workplaceManager.getCategoryFolder()));
        }

        // add <categorydisplayoptions> node
        if (m_workplaceManager.isDisplayCategoriesByRepository()
            || m_workplaceManager.isDisplayCategorySelectionCollapsed()) {
            Element categoryDisplayOptions = workplaceElement.addElement(N_CATEGORYDISPLAYOPTIONS);
            if (m_workplaceManager.isDisplayCategoriesByRepository()) {
                categoryDisplayOptions.addAttribute(A_DISPLAY_BY_REPOSITORY, "true");
            }
            if (m_workplaceManager.isDisplayCategorySelectionCollapsed()) {
                categoryDisplayOptions.addAttribute(A_DISPLAY_COLLAPSED, "true");
            }
        }

        String groupTranslationClass = m_workplaceManager.getGroupTranslationClass();
        if (groupTranslationClass != null) {
            Element groupTranslationElement = workplaceElement.addElement(N_GROUP_TRANSLATION);
            groupTranslationElement.addAttribute(A_CLASS, groupTranslationClass);
        }

        workplaceElement.addElement(N_MAXUPLOADSIZE).setText(String.valueOf(m_workplaceManager.getFileMaxUploadSize()));

        // add <labeledfolders> resource list
        Element labeledElement = workplaceElement.addElement(N_LABELEDFOLDERS);
        Iterator<String> sitesFolders = m_workplaceManager.getLabelSiteFolders().iterator();
        while (sitesFolders.hasNext()) {
            labeledElement.addElement(N_RESOURCE).addAttribute(A_URI, sitesFolders.next());
        }
        // add the <galleryupload> node
        workplaceElement.addElement(N_REPOSITORY_FOLDER).addAttribute(
            A_CLASS,
            m_workplaceManager.getRepositoryFolderHandler().getClass().getName());

        // add <rfsfileviewsettings> node
        CmsRfsFileViewer viewSettings = m_workplaceManager.getFileViewSettings();
        Element fileViewElement = workplaceElement.addElement(N_RFSFILEVIEWESETTINGS);
        String rootPath = viewSettings.getRootPath();
        if (rootPath != null) {
            fileViewElement.addElement(N_ROOTPATH).setText(rootPath);
        }
        String filePath = viewSettings.getFilePath();
        if (filePath != null) {
            fileViewElement.addElement(N_FILEPATH).setText(filePath);
        }
        fileViewElement.addElement(N_ENABLED).setText(String.valueOf(viewSettings.isEnabled()));
        fileViewElement.addElement(N_FILEENCODING).setText(viewSettings.getFileEncoding());
        fileViewElement.addElement(N_ISLOGFILE).setText(String.valueOf(viewSettings.getIsLogfile()));
        fileViewElement.addElement(N_WINDOWSIZE).setText(String.valueOf(viewSettings.getWindowSize()));

        // add <explorertypes> node
        Element explorerTypesElement = workplaceElement.addElement(N_EXPLORERTYPES);
        List<CmsExplorerTypeSettings> explorerTypes = m_workplaceManager.getWorkplaceExplorerTypeSettings();
        generateExplorerTypesXml(explorerTypesElement, explorerTypes, false);

        // add the <defaultaccesscontrol> node
        Element defaultAccessControlElement = explorerTypesElement.addElement(N_DEFAULTACCESSCONTROL);
        // create subnode <accesscontrol>
        List<String> accessEntries = new ArrayList<String>();
        // sort accessEntries
        CmsExplorerTypeAccess access = m_workplaceManager.getDefaultAccess();
        Iterator<String> iter = access.getAccessEntries().keySet().iterator();
        while (iter.hasNext()) {
            accessEntries.add(iter.next());
        }
        Collections.sort(accessEntries);

        if (accessEntries.size() > 0) {
            Element accessControlElement = defaultAccessControlElement.addElement(N_ACCESSCONTROL);
            Iterator<String> k = accessEntries.iterator();

            while (k.hasNext()) {
                String key = k.next();
                String value = m_workplaceManager.getDefaultAccess().getAccessEntries().get(key);
                Element accessEntryElement = accessControlElement.addElement(N_ACCESSENTRY);
                accessEntryElement.addAttribute(A_PRINCIPAL, key);
                accessEntryElement.addAttribute(A_PERMISSIONS, value);
            }
        }

        // add the user-infos node
        if (m_workplaceManager.getUserInfoManager() != null) {
            Element userInfosElement = workplaceElement.addElement(N_USERINFOS);
            Iterator<CmsWorkplaceUserInfoBlock> infoBlocks = m_workplaceManager.getUserInfoManager().getBlocks().iterator();
            while (infoBlocks.hasNext()) {
                CmsWorkplaceUserInfoBlock block = infoBlocks.next();
                Element infoBlockElement = userInfosElement.addElement(N_INFOBLOCK);
                infoBlockElement.addAttribute(A_NAME, block.getTitle());

                Iterator<CmsWorkplaceUserInfoEntry> itEntries = block.getEntries().iterator();
                while (itEntries.hasNext()) {
                    CmsWorkplaceUserInfoEntry entry = itEntries.next();
                    Element userInfoElement = infoBlockElement.addElement(N_USERINFO);
                    userInfoElement.addAttribute(A_KEY, entry.getKey());
                    if (entry.getType() != null) {
                        userInfoElement.addAttribute(A_TYPE, entry.getType());
                    }
                    if (entry.getWidget() != null) {
                        userInfoElement.addAttribute(A_WIDGET, entry.getWidget());
                    }
                    if (entry.getParams() != null) {
                        userInfoElement.addAttribute(A_PARAMS, entry.getParams());
                    }
                    if (entry.getOptional() != null) {
                        userInfoElement.addAttribute(A_OPTIONAL, entry.getOptional());
                    }
                }
            }
        }

        List<CmsAccountInfo> accountInfos = m_workplaceManager.getConfiguredAccountInfos();
        if (accountInfos != null) {
            Element infosElement = workplaceElement.addElement(N_ACCOUNTINFOS);
            for (CmsAccountInfo info : accountInfos) {
                Element infoElement = infosElement.addElement(N_ACCOUNTINFO);
                infoElement.addAttribute(A_FIELD, info.getField().name());
                if (info.isAdditionalInfo()) {
                    infoElement.addAttribute(A_ADD_INFO_KEY, info.getAddInfoKey());
                }
                infoElement.addAttribute(A_EDITABLE, Boolean.toString(info.isEditable()));
            }
        }

        // add the <default-preferences> user settings main node
        Element defaultPreferences = workplaceElement.addElement(N_DEFAULTPREFERENCES);

        Multimap<String, I_CmsPreference> prefsByTab = Multimaps.index(
            m_workplaceManager.getDefaultUserSettings().getPreferences().values(),
            new Function<I_CmsPreference, String>() {

                public String apply(I_CmsPreference input) {

                    return input.getTab();
                }
            });

        for (String tabName : new String[] {
            CmsGwtConstants.TAB_BASIC,
            CmsGwtConstants.TAB_EXTENDED,
            CmsGwtConstants.TAB_HIDDEN}) {
            Element preferenceTab = defaultPreferences.addElement(N_PREFERENCE_TAB).addAttribute("name", tabName);
            for (I_CmsPreference pref : prefsByTab.get(tabName)) {
                Element elem = pref.createConfigurationItem();
                if (elem != null) {
                    preferenceTab.add(elem);
                }
            }
        }

        // add the tool-manager node
        Element toolManagerElement = workplaceElement.addElement(N_TOOLMANAGER);
        Element rootsElement = toolManagerElement.addElement(N_ROOTS);
        Iterator<CmsToolRootHandler> it = m_workplaceManager.getToolManager().getToolRoots().iterator();
        while (it.hasNext()) {
            CmsToolRootHandler root = it.next();
            Element rootElement = rootsElement.addElement(N_ROOT);
            rootElement.addElement(N_KEY).addText(root.getKey());
            rootElement.addElement(N_URI).addText(root.getUri());
            rootElement.addElement(N_NAME).addText(root.getName());
            rootElement.addElement(N_HELPTEXT).addText(root.getHelpText());
        }
        String userListsMode = m_workplaceManager.getUserListModeString();
        if (userListsMode != null) {
            Element userListsElem = workplaceElement.addElement(N_USER_LISTS);
            userListsElem.addAttribute(A_MODE, userListsMode);
        }

        Boolean keepAlive = m_workplaceManager.isKeepAlive(false);
        if (keepAlive != null) {
            workplaceElement.addElement(N_KEEP_ALIVE).setText(keepAlive.toString());
        }

        String defaultScope = m_workplaceManager.getGalleryDefaultScopeString();
        if (defaultScope != null) {
            workplaceElement.addElement(N_GALLERY_DEFAULT_SCOPE).setText(defaultScope);
        }
        ElementDeleteMode deleteMode = m_workplaceManager.getElementDeleteMode();
        if (deleteMode != null) {
            workplaceElement.addElement(N_ELEMENT_DELETE_MODE).setText(deleteMode.name());
        }

        CmsAdditionalLogFolderConfig.ADD_LOG_FOLDER_HELPER.generateXml(
            workplaceElement,
            m_workplaceManager.getAdditionalLogFolderConfiguration());

        boolean useKeysForNewSites = CmsResourceTypeSubsitemapContentFolder.isEnableNewPageFormatByDefault();
        workplaceElement.addElement(N_USE_FORMATTER_KEYS_FOR_NEW_SITES).setText("" + useKeysForNewSites);

        I_CmsUploadRestriction restriction = m_workplaceManager.getUploadRestriction();
        Element uploadRestrictionElem = workplaceElement.addElement(N_UPLOAD_RESTRICTION);
        uploadRestrictionElem.addAttribute(A_CLASS, restriction.getClass().getName());
        restriction.getConfiguration().appendToXml(uploadRestrictionElem);

        I_CmsVirusScanner virusScanner = m_workplaceManager.getVirusScanner();
        Element virusScannerElem = workplaceElement.addElement(N_VIRUS_SCANNER);
        boolean enabled = false;
        if (virusScanner != null) {
            virusScannerElem.addAttribute(A_CLASS, virusScanner.getClass().getName());
            enabled = m_workplaceManager.isVirusScannerEnabled();
            virusScanner.getConfiguration().appendToXml(virusScannerElem);
        }
        virusScannerElem.addAttribute(A_ENABLED, "" + enabled);

        String sitemapConfigEditRole = m_workplaceManager.getSitemapConfigEditRole();
        if (sitemapConfigEditRole != null) {
            workplaceElement.addElement(N_SITEMAP_CONFIG_EDIT_ROLE).addText(sitemapConfigEditRole);
        }

        boolean allowElementAuthorToWorkInGalleries = m_workplaceManager.isAllowElementAuthorToWorkInGalleries();
        workplaceElement.addElement(N_ALLOW_ELEMENT_AUTHOR_TO_WORK_IN_GALLERIES).addText(
            "" + allowElementAuthorToWorkInGalleries);

        // return the configured node
        return workplaceElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the initialized workplace manager.<p>
     *
     * @return the initialized workplace manager
     */
    public CmsWorkplaceManager getWorkplaceManager() {

        return m_workplaceManager;
    }

    /**
     * Will be called when configuration of this object is finished.<p>
     */
    public void initializeFinished() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_FINISHED_0));
        }
    }

    /**
     * Sets the generated workplace manager.<p>
     *
     * @param manager the workplace manager to set
     */
    public void setWorkplaceManager(CmsWorkplaceManager manager) {

        m_workplaceManager = manager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_INIT_FINISHED_0));
        }
    }

    /**
     * Adds the digester rules for account info nodes.<p>
     *
     * @param digester the digester
     */
    protected void addAccountInfoRules(Digester digester) {

        // add account info
        String rulePath = "*/" + N_ACCOUNTINFOS + "/" + N_ACCOUNTINFO;
        digester.addCallMethod(rulePath, "addAccountInfo", 3);
        digester.addCallParam(rulePath, 0, A_FIELD);
        digester.addCallParam(rulePath, 1, A_ADD_INFO_KEY);
        digester.addCallParam(rulePath, 2, A_EDITABLE);
    }

    /**
     * Adds the digester rules for the defaultaccesscontrol node.<p>
     *
     * @param digester the digester object
     */
    protected void addDefaultAccessControlRules(Digester digester) {

        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_EXPLORERTYPES + "/" + N_DEFAULTACCESSCONTROL + "/" + N_ACCESSCONTROL,
            CmsExplorerTypeAccess.class);
        digester.addSetNext(
            "*/" + N_WORKPLACE + "/" + N_EXPLORERTYPES + "/" + N_DEFAULTACCESSCONTROL + "/" + N_ACCESSCONTROL,
            "setDefaultAccess");

        digester.addCallMethod(
            "*/"
                + N_WORKPLACE
                + "/"
                + N_EXPLORERTYPES
                + "/"
                + N_DEFAULTACCESSCONTROL
                + "/"
                + N_ACCESSCONTROL
                + "/"
                + N_ACCESSENTRY,
            "addAccessEntry",
            2);
        digester.addCallParam(
            "*/"
                + N_WORKPLACE
                + "/"
                + N_EXPLORERTYPES
                + "/"
                + N_DEFAULTACCESSCONTROL
                + "/"
                + N_ACCESSCONTROL
                + "/"
                + N_ACCESSENTRY,
            0,
            A_PRINCIPAL);
        digester.addCallParam(
            "*/"
                + N_WORKPLACE
                + "/"
                + N_EXPLORERTYPES
                + "/"
                + N_DEFAULTACCESSCONTROL
                + "/"
                + N_ACCESSCONTROL
                + "/"
                + N_ACCESSENTRY,
            1,
            A_PERMISSIONS);
    }

    /**
     * Adds the digester rules for the default-preferences node.<p>
     *
     * @param digester the digester object
     */
    protected void addDefaultPreferencesRules(Digester digester) {

        // creation of the default user settings
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_DEFAULTPREFERENCES, CmsDefaultUserSettings.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_DEFAULTPREFERENCES, "setDefaultUserSettings");

        // TODO: most of these settings are not user dependent, so they should not be stored in the CmsDefaultUserSettings class

        // add workplace preferences general options rules
        String xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_WORKPLACEPREFERENCES
            + "/"
            + N_WORKPLACEGENERALOPTIONS;
        digester.addCallMethod(xPathPrefix + "/" + N_BUTTONSTYLE, "setWorkplaceButtonStyle", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_REPORTTYPE, "setWorkplaceReportType", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_UPLOADAPPLET, "setUploadVariant", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_LISTALLPROJECTS, "setListAllProjects", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_PUBLISHNOTIFICATION, "setShowPublishNotification", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_PUBLISHBUTTONAPPEARANCE, "setPublishButtonAppearance", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_SHOWFILEUPLOADBUTTON, "setShowFileUploadButton", 0);

        // add allow broken relations rule
        digester.addCallMethod(xPathPrefix + "/" + N_ALLOWBROKENRELATIONS, "setAllowBrokenRelations", 0);

        // add publish related resources rule
        digester.addCallMethod(xPathPrefix + "/" + N_PUBLISHRELATEDRESOURCES, "setPublishRelatedResourcesMode", 0);

        // add rules for the new folder dialog settings
        digester.addCallMethod(xPathPrefix + "/" + N_NEWFOLDEREDITPROPERTIES, "setNewFolderEditProperties", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_NEWFOLDERCREATEINDEXPAGE, "setNewFolderCreateIndexPage", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_SHOWUPLOADTYPEDIALOG, "setShowUploadTypeDialog", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_SUBSITEMAP_CREATION_MODE, "setSubsitemapCreationMode", 0);

        // add workplace preferences startup settings rules
        xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_WORKPLACEPREFERENCES
            + "/"
            + N_WORKPLACESTARTUPSETTINGS;
        digester.addCallMethod(xPathPrefix + "/" + N_LOCALE, "setLocale", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_PROJECT, "setStartProject", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_WORKPLACEVIEW, "setStartView", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_FOLDER, "setStartFolder", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_SITE, "setStartSite", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_RESTRICTEXPLORERVIEW, "setRestrictExplorerView", 0);

        // add workplace search rules
        xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_WORKPLACEPREFERENCES
            + "/"
            + N_WORKPLACESEARCH;
        digester.addCallMethod(xPathPrefix + "/" + N_SEARCHINDEXNAME, "setWorkplaceSearchIndexName", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_SEARCHVIEWSTYLE, "setWorkplaceSearchViewStyle", 0);

        // add explorer preferences generaloptions rules
        xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_EXPLORERPREFERENCES
            + "/"
            + N_EXPLORERGENERALOPTIONS;
        digester.addCallMethod(xPathPrefix + "/" + N_BUTTONSTYLE, "setExplorerButtonStyle", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_ENTRIES, "setExplorerFileEntries", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_ENTRYOPTIONS, "setExplorerFileEntryOptions", 0);

        // add explorer display options rules
        xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_EXPLORERPREFERENCES
            + "/"
            + N_EXPLORERDISPLAYOPTIONS;
        digester.addCallMethod(xPathPrefix + "/" + N_TITLE, "setShowExplorerFileTitle", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_NAVTEXT, "setShowExplorerFileNavText", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_TYPE, "setShowExplorerFileType", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_DATELASTMODIFIED, "setShowExplorerFileDateLastModified", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_DATECREATED, "setShowExplorerFileDateCreated", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_LOCKEDBY, "setShowExplorerFileLockedBy", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_PERMISSIONS, "setShowExplorerFilePermissions", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_SIZE, "setShowExplorerFileSize", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_STATE, "setShowExplorerFileState", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_USERLASTMODIFIED, "setShowExplorerFileUserLastModified", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_USERCREATED, "setShowExplorerFileUserCreated", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_DATERELEASED, "setShowExplorerFileDateReleased", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_DATEEXPIRED, "setShowExplorerFileDateExpired", 0);

        // add dialog preferences rules
        xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_DIALOGSPREFERENCES
            + "/"
            + N_DIALOGSDEFAULTSETTINGS;
        digester.addCallMethod(xPathPrefix + "/" + N_FILECOPY, "setDialogCopyFileMode", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_FOLDERCOPY, "setDialogCopyFolderMode", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_FILEDELETION, "setDialogDeleteFileMode", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_DIRECTPUBLISH, "setDialogPublishSiblings", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_SHOWLOCK, "setShowLockDialog", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_SHOWEXPORTSETTINGS, "setShowExportSettingsDialog", 0);
        digester.addCallMethod(
            xPathPrefix + "/" + N_PERMISSIONSINHERITONFOLDER,
            "setDialogPermissionsInheritOnFolder",
            0);
        digester.addCallMethod(
            xPathPrefix + "/" + N_EXPANDPERMISSIONSINHERITED,
            "setDialogExpandInheritedPermissions",
            0);
        digester.addCallMethod(xPathPrefix + "/" + N_EXPANDPERMISSIONSUSER, "setDialogExpandUserPermissions", 0);

        // add editor generaloptions rules
        xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_EDITORPREFERENCES
            + "/"
            + N_EDITORGENERALOPTIONS;
        digester.addCallMethod(xPathPrefix + "/" + N_BUTTONSTYLE, "setEditorButtonStyle", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_DIRECTEDITSTYLE, "setDirectEditButtonStyle", 0);

        // add editor preferrededitor rules
        xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_EDITORPREFERENCES
            + "/"
            + N_EDITORPREFERREDEDITORS;
        digester.addCallMethod(xPathPrefix + "/" + N_EDITOR, "setPreferredEditor", 2);
        digester.addCallParam(xPathPrefix + "/" + N_EDITOR, 0, A_TYPE);
        digester.addCallParam(xPathPrefix + "/" + N_EDITOR, 1, A_VALUE);

        // add startgallery rules
        xPathPrefix = "*/"
            + N_WORKPLACE
            + "/"
            + N_DEFAULTPREFERENCES
            + "/"
            + N_GALLERIESPREFERENCES
            + "/"
            + N_STARTGALLERIES;
        digester.addCallMethod(xPathPrefix + "/" + N_STARTGALLERY, "setStartGallery", 2);
        digester.addCallParam(xPathPrefix + "/" + N_STARTGALLERY, 0, A_TYPE);
        digester.addCallParam(xPathPrefix + "/" + N_STARTGALLERY, 1, A_PATH);

        digester.addRule("*/" + N_WORKPLACE + "/" + N_DEFAULTPREFERENCES + "/preference-tab", new Rule() {

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                getDigester().push(attributes.getValue("name"));
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                getDigester().pop();
            }
        });

        String prefPath = "*/" + N_WORKPLACE + "/" + N_DEFAULTPREFERENCES + "/preference-tab/" + N_PREFERENCE;
        digester.addRule(prefPath, new CallMethodRule(1, "addPreference", 9));
        digester.addCallParam(prefPath, 0, A_NAME);
        digester.addCallParam(prefPath, 1, A_VALUE);
        digester.addCallParam(prefPath, 2, A_WIDGET);
        digester.addCallParam(prefPath, 3, A_WIDGET_CONFIG);
        digester.addCallParam(prefPath, 4, A_NICE_NAME);
        digester.addCallParam(prefPath, 5, A_DESCRIPTION);
        digester.addCallParam(prefPath, 6, A_RULE_REGEX);
        digester.addCallParam(prefPath, 7, A_ERROR);
        digester.addCallParam(prefPath, 8, 0);
    }

    /**
     * Adds the digester rules for the tool-manager node.<p>
     *
     * @param digester the digester object
     */
    protected void addToolManagerRules(Digester digester) {

        // add tool-manager
        String rulePath = "*/" + N_TOOLMANAGER;
        digester.addObjectCreate(rulePath, CmsToolManager.class);
        digester.addSetNext(rulePath, "setToolManager");
        // add tool-manager roots
        rulePath += "/" + N_ROOTS + "/" + N_ROOT;
        digester.addObjectCreate(rulePath, CmsToolRootHandler.class);
        digester.addSetNext(rulePath, "addToolRoot");
        digester.addBeanPropertySetter(rulePath + "/" + N_KEY);
        digester.addBeanPropertySetter(rulePath + "/" + N_URI);
        digester.addBeanPropertySetter(rulePath + "/" + N_NAME);
        digester.addBeanPropertySetter(rulePath + "/" + N_HELPTEXT, "helpText");
    }

    /**
     * Adds the digester rules for the user-infos node.<p>
     *
     * @param digester the digester object
     */
    protected void addUserInfoRules(Digester digester) {

        // add user additional information
        String rulePath = "*/" + N_USERINFOS;
        digester.addObjectCreate(rulePath, CmsWorkplaceUserInfoManager.class);
        digester.addSetNext(rulePath, "setUserInfoManager");
        // create a new block
        rulePath += "/" + N_INFOBLOCK;
        digester.addObjectCreate(rulePath, CmsWorkplaceUserInfoBlock.class);
        // set the title
        digester.addCallMethod(rulePath, "setTitle", 1);
        digester.addCallParam(rulePath, 0, A_NAME);
        // add a new entry
        digester.addCallMethod(rulePath + "/" + N_USERINFO, "addEntry", 5);
        digester.addCallParam(rulePath + "/" + N_USERINFO, 0, A_KEY);
        digester.addCallParam(rulePath + "/" + N_USERINFO, 1, A_TYPE);
        digester.addCallParam(rulePath + "/" + N_USERINFO, 2, A_WIDGET);
        digester.addCallParam(rulePath + "/" + N_USERINFO, 3, A_PARAMS);
        digester.addCallParam(rulePath + "/" + N_USERINFO, 4, A_OPTIONAL);
        // add the new created block
        digester.addSetNext(rulePath, "addBlock");
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_INIT_0));
        }
    }

}