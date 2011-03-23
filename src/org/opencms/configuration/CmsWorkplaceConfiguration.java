/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsWorkplaceConfiguration.java,v $
 * Date   : $Date: 2011/03/23 14:51:41 $
 * Version: $Revision: 1.60 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.db.CmsExportPoint;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceCustomFoot;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceUserInfoBlock;
import org.opencms.workplace.CmsWorkplaceUserInfoEntry;
import org.opencms.workplace.CmsWorkplaceUserInfoManager;
import org.opencms.workplace.I_CmsDialogHandler;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.workplace.editors.I_CmsPreEditorActionDefinition;
import org.opencms.workplace.explorer.CmsExplorerContextMenu;
import org.opencms.workplace.explorer.CmsExplorerContextMenuItem;
import org.opencms.workplace.explorer.CmsExplorerTypeAccess;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.menu.CmsMenuRule;
import org.opencms.workplace.explorer.menu.CmsMenuRuleTranslator;
import org.opencms.workplace.explorer.menu.I_CmsMenuItemRule;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.workplace.tools.CmsToolRootHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * Import/Export master configuration class.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.60 $
 * 
 * @since 6.0.0
 */
public class CmsWorkplaceConfiguration extends A_CmsXmlConfiguration {

    /** The "autosetnavigation" attribute. */
    public static final String A_AUTOSETNAVIGATION = "autosetnavigation";

    /** The "autosettitle" attribute. */
    public static final String A_AUTOSETTITLE = "autosettitle";

    /** The "info" attribute. */
    public static final String A_INFO = "info";

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

    /** The "rules" attribute. */
    public static final String A_RULES = "rules";

    /** The "shownavigation" attribute. */
    public static final String A_SHOWNAVIGATION = "shownavigation";

    /** The "target" attribute. */
    public static final String A_TARGET = "target";

    /** The attribute name of the widget attribute for the user-info node. */
    public static final String A_WIDGET = "widget";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-workplace.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-workplace.xml";

    /** The name of the access control node. */
    public static final String N_ACCESSCONTROL = "accesscontrol";

    /** The name of the access entry node. */
    public static final String N_ACCESSENTRY = "accessentry";

    /** The name of the "allow broken relations" node. */
    public static final String N_ALLOWBROKENRELATIONS = "allowbrokenrelations";

    /** The name of the autolock node. */
    public static final String N_AUTOLOCK = "autolock";

    /** The name of the background color node. */
    public static final String N_BACKGROUNDCOLOR = "background-color";

    /** The node name of the buttonstyle node. */
    public static final String N_BUTTONSTYLE = "buttonstyle";

    /** The name of the color node. */
    public static final String N_COLOR = "color";

    /** The name of the context menu node. */
    public static final String N_CONTEXTMENU = "contextmenu";

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

    /** The node name of the gallery preferences node. */
    public static final String N_GALLERIESPREFERENCES = "galleries-preferences";

    /** The node name of the galleries start setting node. */
    public static final String N_STARTGALLERIES = "startgalleries";

    /** The node name of the start gallery node. */
    public static final String N_STARTGALLERY = "startgallery";

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

    /** The node name of the helptext node. */
    public static final String N_HELPTEXT = "helptext";

    /** The node name of the info-block node. */
    public static final String N_INFOBLOCK = "info-block";

    /** The subname of the rfsfilesettings/isLogfile node. */
    public static final String N_ISLOGFILE = "isLogfile";

    /** The node name of the key node. */
    public static final String N_KEY = "key";

    /** The name of the "labeled folders" node. */
    public static final String N_LABELEDFOLDERS = "labeledfolders";

    /** The node name of the locale node. */
    public static final String N_LOCALE = "locale";

    /** The name of the "localized folders" node. */
    public static final String N_LOCALIZEDFOLDERS = "localizedfolders";

    /** The node name of the lockedby column node. */
    public static final String N_LOCKEDBY = "show-lockedby";

    /** The name of the "max file upload size" node. */
    public static final String N_MAXUPLOADSIZE = "maxfileuploadsize";

    /** The name of the "menuitemrule" node. */
    public static final String N_MENUITEMRULE = "menuitemrule";

    /** The name of the "menurule" node. */
    public static final String N_MENURULE = "menurule";

    /** The name of the "menurules" node. */
    public static final String N_MENURULES = "menurules";

    /** The name of the context menu node. */
    public static final String N_MULTICONTEXTMENU = "multicontextmenu";

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

    /** The node name of the project node. */
    public static final String N_PROJECT = "project";

    /** The node name of the publish button appearance node. */
    public static final String N_PUBLISHBUTTONAPPEARANCE = "publishbuttonappearance";

    /** The node name of the list all projects node. */
    public static final String N_LISTALLPROJECTS = "listallprojects";

    /** The node name of the publish notification node. */
    public static final String N_PUBLISHNOTIFICATION = "publishnotification";

    /** The name of the "publish related resources" node. */
    public static final String N_PUBLISHRELATEDRESOURCES = "publishrelatedresources";

    /** The node name of the report type node. */
    public static final String N_REPORTTYPE = "reporttype";

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

    /** The node name of the state column node. */
    public static final String N_STATE = "show-state";

    /** The name of the text node. */
    public static final String N_TEXT = "text";

    /** The node name of the title column node. */
    public static final String N_TITLE = "show-title";

    /** The node name of the tool-manager node. */
    public static final String N_TOOLMANAGER = "tool-manager";

    /** The node name of the type column node. */
    public static final String N_TYPE = "show-type";

    /** The node name of the uploadapplet node. */
    public static final String N_UPLOADAPPLET = "uploadapplet";

    /** The node name of the uri node. */
    public static final String N_URI = "uri";

    /** The node name of the user created node. */
    public static final String N_USERCREATED = "show-usercreated";

    /** The node name of the user-info node. */
    public static final String N_USERINFO = "user-info";

    /** The node name of the user-infos node. */
    public static final String N_USERINFOS = "user-infos";

    /** The node name of the user lastmodified node. */
    public static final String N_USERLASTMODIFIED = "show-userlastmodified";

    /** The subname of the rfsfilesettings/windowSize node. */
    public static final String N_WINDOWSIZE = "windowSize";

    /** The node name of the master workplace node. */
    public static final String N_WORKPLACE = "workplace";

    /** The name of the workplace custom foot node. */
    public static final String N_WORKPLACECUSTOMFOOT = "workplace-customfoot";

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

    /**
     * Adds the explorer type rules to the given digester.<p>
     * 
     * @param digester the digester to add the rules to
     */
    public static void addExplorerTypeXmlRules(Digester digester) {

        // remove workflow nodes from the dtd, there are just there for compatibility reasons
        int todo;

        // add explorer type settings
        digester.addObjectCreate("*/" + N_EXPLORERTYPE, CmsExplorerTypeSettings.class);
        digester.addSetNext("*/" + N_EXPLORERTYPE, "addExplorerTypeSetting");

        digester.addCallMethod("*/" + N_EXPLORERTYPE, "setTypeAttributes", 4);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 0, A_NAME);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 1, A_KEY);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 2, A_ICON);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 3, A_REFERENCE);

        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setNewResourceHandlerClassName", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_HANDLER);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setNewResourceUri", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_URI);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setNewResourceOrder", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_ORDER);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setNewResourcePage", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_PAGE);

        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setAutoSetNavigation", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_AUTOSETNAVIGATION);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setAutoSetTitle", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_AUTOSETTITLE);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setInfo", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_INFO);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setDescriptionImage", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_ICON);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setTitleKey", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_KEY);

        digester.addObjectCreate("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL, CmsExplorerTypeAccess.class);
        digester.addSetNext("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL, "setAccess");

        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL + "/" + N_ACCESSENTRY, "addAccessEntry", 2);
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

        digester.addCallMethod("*/"
            + N_EXPLORERTYPE
            + "/"
            + N_EDITOPTIONS
            + "/"
            + N_DEFAULTPROPERTIES
            + "/"
            + N_PROPERTY, "addProperty", 1);
        digester.addCallParam("*/"
            + N_EXPLORERTYPE
            + "/"
            + N_EDITOPTIONS
            + "/"
            + N_DEFAULTPROPERTIES
            + "/"
            + N_PROPERTY, 0, A_NAME);

        digester.addCallMethod("*/"
            + N_EXPLORERTYPE
            + "/"
            + N_EDITOPTIONS
            + "/"
            + N_DEFAULTPROPERTIES
            + "/"
            + N_DEFAULTPROPERTY, "addProperty", 1);
        digester.addCallParam("*/"
            + N_EXPLORERTYPE
            + "/"
            + N_EDITOPTIONS
            + "/"
            + N_DEFAULTPROPERTIES
            + "/"
            + N_DEFAULTPROPERTY, 0, A_NAME);

        addContextMenuItemRules(digester, "*");

        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU, "createContextMenu");
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS, "setEditOptions");
    }

    /**
     * Creates the xml output for explorer type nodes.<p>
     * 
     * @param startNode the startnode to add all rescource types to
     * @param explorerTypes the list of explorer types
     * @param module true if the XML tree for the module config should be generated, false otherwise
     */
    public static void generateExplorerTypesXml(Element startNode, List explorerTypes, boolean module) {

        // we need the default access node later to check if the explorer type is an individual setting
        CmsExplorerTypeAccess defaultAccess = null;
        if (OpenCms.getWorkplaceManager() != null) {
            defaultAccess = OpenCms.getWorkplaceManager().getDefaultAccess();
        }
        // get the menu rule translator to eliminate eventual legacy menu rules
        CmsMenuRuleTranslator menuRuleTranslator = new CmsMenuRuleTranslator();
        Iterator i = explorerTypes.iterator();
        while (i.hasNext()) {
            // create an explorer type node
            CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)i.next();

            if (settings.isAddititionalModuleExplorerType() == module) {
                Element explorerTypeElement = startNode.addElement(N_EXPLORERTYPE);
                explorerTypeElement.addAttribute(A_NAME, settings.getName());
                explorerTypeElement.addAttribute(A_KEY, settings.getKey());
                explorerTypeElement.addAttribute(A_ICON, settings.getIcon());
                if (settings.getReference() != null) {
                    explorerTypeElement.addAttribute(A_REFERENCE, settings.getReference());
                }
                // create subnode <newresource>
                Element newResElement = explorerTypeElement.addElement(N_NEWRESOURCE);
                if (CmsStringUtil.isNotEmpty(settings.getNewResourcePage())) {
                    newResElement.addAttribute(A_PAGE, settings.getNewResourcePage());
                }
                newResElement.addAttribute(A_HANDLER, settings.getNewResourceHandlerClassName());
                newResElement.addAttribute(A_URI, CmsEncoder.escapeXml(settings.getNewResourceUri()));
                newResElement.addAttribute(A_ORDER, settings.getNewResourceOrder());
                newResElement.addAttribute(A_AUTOSETNAVIGATION, String.valueOf(settings.isAutoSetNavigation()));
                newResElement.addAttribute(A_AUTOSETTITLE, String.valueOf(settings.isAutoSetTitle()));
                newResElement.addAttribute(A_INFO, settings.getInfo());
                newResElement.addAttribute(A_ICON, settings.getDescriptionImage());
                newResElement.addAttribute(A_KEY, settings.getTitleKey());
                // create subnode <accesscontrol>            
                CmsExplorerTypeAccess access = settings.getAccess();
                if (access != defaultAccess) {
                    // don't output the node if this is in fact the default access settings
                    List accessEntries = new ArrayList(access.getAccessEntries().keySet());
                    // sort accessEntries
                    Collections.sort(accessEntries);
                    if (accessEntries.size() > 0) {
                        Element accessControlElement = explorerTypeElement.addElement(N_ACCESSCONTROL);
                        Iterator k = accessEntries.iterator();
                        while (k.hasNext()) {
                            String key = (String)k.next();
                            String value = (String)settings.getAccess().getAccessEntries().get(key);
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
                    defaultPropertiesElement.addAttribute(A_SHOWNAVIGATION, String.valueOf(settings.isShowNavigation()));
                    Iterator m = settings.getProperties().iterator();
                    while (m.hasNext()) {
                        defaultPropertiesElement.addElement(N_DEFAULTPROPERTY).addAttribute(A_NAME, (String)m.next());
                    }
                    Element contextMenuElement = editOptionsElement.addElement(N_CONTEXTMENU);
                    m = settings.getContextMenuEntries().iterator();
                    while (m.hasNext()) {
                        CmsExplorerContextMenuItem item = (CmsExplorerContextMenuItem)m.next();
                        generateContextMenuItemXml(contextMenuElement, menuRuleTranslator, item);
                    }
                }
            }
        }
    }

    /**
     * Adds the context menu item rules to the given digester.<p>
     *  
     * @param digester the digester to add the rules to
     * @param xPathPrefix the path prefix (should be the path to the contextmenu or the multicontextmenu node)
     */
    protected static void addContextMenuItemRules(Digester digester, String xPathPrefix) {

        // add the rules for an entry item
        String xPath = xPathPrefix + "/" + N_ENTRY;
        digester.addObjectCreate(xPath, CmsExplorerContextMenuItem.class);
        digester.addCallMethod(xPath, "setKey", 1);
        digester.addCallParam(xPath, 0, A_KEY);
        digester.addCallMethod(xPath, "setUri", 1);
        digester.addCallParam(xPath, 0, A_URI);
        digester.addCallMethod(xPath, "setRules", 1);
        digester.addCallParam(xPath, 0, A_RULES);
        digester.addCallMethod(xPath, "setRule", 1);
        digester.addCallParam(xPath, 0, A_RULE);
        digester.addCallMethod(xPath, "setTarget", 1);
        digester.addCallParam(xPath, 0, A_TARGET);
        digester.addSetNext(xPath, "addContextMenuEntry");

        // add the rules for a separator item
        xPath = xPathPrefix + "/" + N_SEPARATOR;
        digester.addObjectCreate(xPath, CmsExplorerContextMenuItem.class);
        digester.addSetNext(xPath, "addContextMenuSeparator");
    }

    /**
     * Creates the xml output for context menu item nodes and eventual subnodes.<p>
     * 
     * @param parentElement the parent element to add the item node to
     * @param menuRuleTranslator the menu rule translator to use for legacy rules
     * @param item the context menu item to create the node for
     */
    protected static void generateContextMenuItemXml(
        Element parentElement,
        CmsMenuRuleTranslator menuRuleTranslator,
        CmsExplorerContextMenuItem item) {

        Element itemElement;
        if (CmsExplorerContextMenuItem.TYPE_ENTRY.equals(item.getType())) {
            // create an <entry> node
            itemElement = parentElement.addElement(N_ENTRY);
            itemElement.addAttribute(A_KEY, item.getKey());
            itemElement.addAttribute(A_URI, item.getUri());
            if (item.getTarget() != null) {
                itemElement.addAttribute(A_TARGET, item.getTarget());
            }
            String rule = item.getRule();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(rule)) {
                itemElement.addAttribute(A_RULE, rule);
            } else {
                String legacyRules = item.getRules();
                if (CmsStringUtil.isNotEmpty(legacyRules) && menuRuleTranslator.hasMenuRule(legacyRules)) {
                    itemElement.addAttribute(A_RULE, menuRuleTranslator.getMenuRuleName(legacyRules));
                } else {
                    itemElement.addAttribute(A_RULES, legacyRules);
                }
            }
            if (item.isParentItem()) {
                Iterator i = item.getSubItems().iterator();
                while (i.hasNext()) {
                    CmsExplorerContextMenuItem subItem = (CmsExplorerContextMenuItem)i.next();
                    generateContextMenuItemXml(itemElement, menuRuleTranslator, subItem);
                }
            }
        } else {
            // create a <separator> node
            parentElement.addElement(N_SEPARATOR);
        }
    }

    /** The configured workplace manager. */
    private CmsWorkplaceManager m_workplaceManager;

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
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

        // add rules for dialog handlers
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER,
            A_CLASS,
            CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER, "addDialogHandler");
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);

        // add rules for editor handler
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_EDITORHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_EDITORHANDLER, "setEditorHandler");

        // add rules for editor action handler
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_EDITORACTION, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_EDITORACTION, "setEditorAction");

        // add rules for editor css handler classes
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_EDITORCSSHANDLERS + "/" + N_EDITORCSSHANDLER,
            "addEditorCssHandler",
            1);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EDITORCSSHANDLERS + "/" + N_EDITORCSSHANDLER, 0, A_CLASS);

        // add rules for pre editor action classes
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_EDITORPRECONDITIONS + "/" + N_EDITORPRECONDITION,
            "addPreEditorConditionDefinition",
            2);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EDITORPRECONDITIONS + "/" + N_EDITORPRECONDITION, 0, A_NAME);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EDITORPRECONDITIONS + "/" + N_EDITORPRECONDITION, 1, A_CLASS);

        // add rules for direct edit provider
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_DIRECTEDITPROVIDER,
            A_CLASS,
            CmsConfigurationException.class);
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

        // add XML content auto correction rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_XMLCONTENTAUTOCORRECTION, "setXmlContentAutoCorrect", 0);

        // add user management enabled rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_ENABLEUSERMGMT, "setUserManagementEnabled", 0);

        // add max file upload size rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_MAXUPLOADSIZE, "setFileMaxUploadSize", 0);

        // add labeled folders rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_LABELEDFOLDERS + "/" + N_RESOURCE, "addLabeledFolder", 1);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_LABELEDFOLDERS + "/" + N_RESOURCE, 0, A_URI);

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

        // add explorer type rules
        addExplorerTypeXmlRules(digester);
        addDefaultAccessControlRules(digester);
        addMultiContextMenuRules(digester);
        addContextMenuRules(digester);

        addUserInfoRules(digester);
        addDefaultPreferencesRules(digester);

        // the customized workplace foot
        xPathPrefix = "*/" + N_WORKPLACE + "/" + N_WORKPLACECUSTOMFOOT;
        digester.addObjectCreate(xPathPrefix, CmsWorkplaceCustomFoot.class);
        digester.addCallMethod(xPathPrefix + "/" + N_BACKGROUNDCOLOR, "setBackgroundColor", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_COLOR, "setColor", 0);
        digester.addCallMethod(xPathPrefix + "/" + N_TEXT, "setText", 2);
        digester.addCallParam(xPathPrefix + "/" + N_TEXT, 0);
        digester.addCallParam(xPathPrefix + "/" + N_TEXT, 1, A_REPLACE);
        digester.addSetNext(xPathPrefix, "setCustomFoot");

        addToolManagerRules(digester);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // generate workplace node and subnodes
        Element workplaceElement = parent.addElement(N_WORKPLACE);
        Iterator i;

        // add default locale
        workplaceElement.addElement(N_DEFAULTLOCALE).setText(m_workplaceManager.getDefaultLocale().toString());

        // add <localizedfolders> subnode
        Element localizedElement = workplaceElement.addElement(N_LOCALIZEDFOLDERS);
        Iterator localizedIterator = m_workplaceManager.getLocalizedFolders().iterator();
        while (localizedIterator.hasNext()) {
            // add <resource uri=""/> element(s)
            localizedElement.addElement(N_RESOURCE).addAttribute(A_URI, (String)localizedIterator.next());
        }

        // add <dialoghandlers> subnode
        Element dialogElement = workplaceElement.addElement(N_DIALOGHANDLERS);
        Map dialogs = m_workplaceManager.getDialogHandler();
        String[] keys = (String[])dialogs.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        for (int j = 0; j < keys.length; j++) {
            String name = keys[j];
            // add <dialoghandler> subnode with class attribute
            Element dialogHandler = dialogElement.addElement(N_DIALOGHANDLER).addAttribute(
                A_CLASS,
                dialogs.get(name).getClass().getName());
            I_CmsDialogHandler daialogHandlerConfig = (I_CmsDialogHandler)dialogs.get(name);
            Map handlerParams = daialogHandlerConfig.getConfiguration();
            if (handlerParams != null) {
                Iterator it = handlerParams.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    // name attribute of <param> Element
                    String nameAttr = (String)entry.getKey();
                    if (List.class.isInstance(entry.getValue())) {
                        List values = (List)entry.getValue();
                        for (Iterator iValues = values.listIterator(); iValues.hasNext();) {
                            // add <param> node
                            Element paramNode = dialogHandler.addElement(N_PARAM);
                            // set the name attribute
                            paramNode.addAttribute(A_NAME, nameAttr);
                            // set the text of <param> node
                            String text = (String)iValues.next();
                            paramNode.addText(text);
                        }
                    } else {
                        Element paramNode = dialogHandler.addElement(N_PARAM);
                        // set the name attribute
                        paramNode.addAttribute(A_NAME, nameAttr);
                        // set the text of <param> node
                        paramNode.addText(entry.getValue().toString());
                    }
                }
            }

        }

        // add miscellaneous editor subnodes
        workplaceElement.addElement(N_EDITORHANDLER).addAttribute(
            A_CLASS,
            m_workplaceManager.getEditorHandler().getClass().getName());
        workplaceElement.addElement(N_EDITORACTION).addAttribute(
            A_CLASS,
            m_workplaceManager.getEditorActionHandler().getClass().getName());

        if (m_workplaceManager.getEditorCssHandlers().size() > 0) {
            Element editorCssHandlers = workplaceElement.addElement(N_EDITORCSSHANDLERS);
            Iterator it = m_workplaceManager.getEditorCssHandlers().iterator();
            while (it.hasNext()) {
                I_CmsEditorCssHandler current = (I_CmsEditorCssHandler)it.next();
                Element handler = editorCssHandlers.addElement(N_EDITORCSSHANDLER);
                handler.addAttribute(A_CLASS, current.getClass().getName());
            }
        }

        if (m_workplaceManager.getPreEditorConditionDefinitions().size() > 0) {
            Element editorPreActions = workplaceElement.addElement(N_EDITORPRECONDITIONS);
            Iterator it = m_workplaceManager.getPreEditorConditionDefinitions().iterator();
            while (it.hasNext()) {
                I_CmsPreEditorActionDefinition current = (I_CmsPreEditorActionDefinition)it.next();
                Element action = editorPreActions.addElement(N_EDITORPRECONDITION);
                action.addAttribute(A_NAME, current.getResourceTypeName());
                action.addAttribute(A_CLASS, current.getClass().getName());
            }
        }

        I_CmsConfigurationParameterHandler deProvider = m_workplaceManager.getDirectEditProvider();
        Element deProviderNode = workplaceElement.addElement(N_DIRECTEDITPROVIDER).addAttribute(
            A_CLASS,
            deProvider.getClass().getName());
        Map deProviderConfig = deProvider.getConfiguration();
        if (deProviderConfig != null) {
            Iterator it = deProviderConfig.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = (String)entry.getKey();
                String value = (String)entry.getValue();
                Element paramNode = deProviderNode.addElement(N_PARAM);
                paramNode.addAttribute(A_NAME, name);
                paramNode.addText(value);
            }
        }

        // add <exportpoints> subnode
        Element resourceloadersElement = workplaceElement.addElement(N_EXPORTPOINTS);
        Set points = m_workplaceManager.getExportPoints();
        i = points.iterator();
        while (i.hasNext()) {
            CmsExportPoint point = (CmsExportPoint)i.next();
            resourceloadersElement.addElement(N_EXPORTPOINT).addAttribute(A_URI, point.getUri()).addAttribute(
                A_DESTINATION,
                point.getConfiguredDestination());
        }

        // add miscellaneous configuration nodes
        workplaceElement.addElement(N_AUTOLOCK).setText(String.valueOf(m_workplaceManager.autoLockResources()));
        workplaceElement.addElement(N_XMLCONTENTAUTOCORRECTION).setText(
            String.valueOf(m_workplaceManager.isXmlContentAutoCorrect()));
        workplaceElement.addElement(N_ENABLEUSERMGMT).setText(String.valueOf(m_workplaceManager.showUserGroupIcon()));
        workplaceElement.addElement(N_DEFAULTPROPERTIESONSTRUCTURE).setText(
            String.valueOf(m_workplaceManager.isDefaultPropertiesOnStructure()));
        workplaceElement.addElement(N_ENABLEADVANCEDPROPERTYTABS).setText(
            String.valueOf(m_workplaceManager.isEnableAdvancedPropertyTabs()));
        workplaceElement.addElement(N_MAXUPLOADSIZE).setText(String.valueOf(m_workplaceManager.getFileMaxUploadSize()));

        // add <labeledfolders> resource list
        Element labeledElement = workplaceElement.addElement(N_LABELEDFOLDERS);
        i = m_workplaceManager.getLabelSiteFolders().iterator();
        while (i.hasNext()) {
            labeledElement.addElement(N_RESOURCE).addAttribute(A_URI, (String)i.next());
        }

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
        List explorerTypes = m_workplaceManager.getExplorerTypeSettings();
        generateExplorerTypesXml(explorerTypesElement, explorerTypes, false);

        // add the <defaultaccesscontrol> node
        Element defaultAccessControlElement = explorerTypesElement.addElement(N_DEFAULTACCESSCONTROL);
        // create subnode <accesscontrol>            
        List accessEntries = new ArrayList();
        // sort accessEntries   
        CmsExplorerTypeAccess access = m_workplaceManager.getDefaultAccess();
        Iterator iter = access.getAccessEntries().keySet().iterator();
        while (iter.hasNext()) {
            accessEntries.add(iter.next());
        }
        Collections.sort(accessEntries);

        if (accessEntries.size() > 0) {
            Element accessControlElement = defaultAccessControlElement.addElement(N_ACCESSCONTROL);
            Iterator k = accessEntries.iterator();

            while (k.hasNext()) {
                String key = (String)k.next();
                String value = (String)m_workplaceManager.getDefaultAccess().getAccessEntries().get(key);
                Element accessEntryElement = accessControlElement.addElement(N_ACCESSENTRY);
                accessEntryElement.addAttribute(A_PRINCIPAL, key);
                accessEntryElement.addAttribute(A_PERMISSIONS, value);
            }
        }

        // add the <multicontextmenu> node
        i = m_workplaceManager.getMultiContextMenu().getAllEntries().iterator();
        if (i.hasNext()) {
            // only generate the node if entries are defined
            Element contextMenuElement = explorerTypesElement.addElement(N_MULTICONTEXTMENU);
            // get the menu rule translator to eliminate eventual legacy menu rules
            CmsMenuRuleTranslator menuRuleTranslator = new CmsMenuRuleTranslator();
            while (i.hasNext()) {
                CmsExplorerContextMenuItem item = (CmsExplorerContextMenuItem)i.next();
                generateContextMenuItemXml(contextMenuElement, menuRuleTranslator, item);
            }
        }

        // add <menurules> node and subnodes
        if (m_workplaceManager.getMenuRules().size() > 0) {
            Element rulesElement = explorerTypesElement.addElement(N_MENURULES);
            i = m_workplaceManager.getMenuRules().iterator();
            while (i.hasNext()) {
                // create a <menurule> element for each rule set
                CmsMenuRule rule = (CmsMenuRule)i.next();
                Element ruleElement = rulesElement.addElement(N_MENURULE);
                ruleElement.addAttribute(A_NAME, rule.getName());
                Iterator it = rule.getMenuItemRules().iterator();
                while (it.hasNext()) {
                    // create a <menuitemrule> element for each configured item rule
                    I_CmsMenuItemRule itemRule = (I_CmsMenuItemRule)it.next();
                    Element itemRuleElement = ruleElement.addElement(N_MENUITEMRULE);
                    itemRuleElement.addAttribute(A_CLASS, itemRule.getClass().getName());
                }
            }
        }

        // add the user-infos node
        if (m_workplaceManager.getUserInfoManager() != null) {
            Element userInfosElement = workplaceElement.addElement(N_USERINFOS);
            i = m_workplaceManager.getUserInfoManager().getBlocks().iterator();
            while (i.hasNext()) {
                CmsWorkplaceUserInfoBlock block = (CmsWorkplaceUserInfoBlock)i.next();
                Element infoBlockElement = userInfosElement.addElement(N_INFOBLOCK);
                infoBlockElement.addAttribute(A_NAME, block.getTitle());

                Iterator itEntries = block.getEntries().iterator();
                while (itEntries.hasNext()) {
                    CmsWorkplaceUserInfoEntry entry = (CmsWorkplaceUserInfoEntry)itEntries.next();
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

        // add the <default-preferences> user settings main node
        Element defaultPreferences = workplaceElement.addElement(N_DEFAULTPREFERENCES);
        // add the <workplace-preferences> node
        Element workplacePreferences = defaultPreferences.addElement(N_WORKPLACEPREFERENCES);
        // add the <workplace-generaloptions> node
        Element workplaceGeneraloptions = workplacePreferences.addElement(N_WORKPLACEGENERALOPTIONS);
        // add the <buttonstyle> node
        workplaceGeneraloptions.addElement(N_BUTTONSTYLE).setText(
            m_workplaceManager.getDefaultUserSettings().getWorkplaceButtonStyleString());
        // add the <reporttype> node
        workplaceGeneraloptions.addElement(N_REPORTTYPE).setText(
            m_workplaceManager.getDefaultUserSettings().getWorkplaceReportType());
        // add the <uploadapplet> node
        workplaceGeneraloptions.addElement(N_UPLOADAPPLET).setText(
            m_workplaceManager.getDefaultUserSettings().getUploadAppletString());
        // add the <publishbuttonappearance> node if not empty
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_workplaceManager.getDefaultUserSettings().getPublishButtonAppearance())) {
            workplaceGeneraloptions.addElement(N_PUBLISHBUTTONAPPEARANCE).setText(
                m_workplaceManager.getDefaultUserSettings().getPublishButtonAppearance());
        }
        // add the list all projects option
        workplaceGeneraloptions.addElement(N_LISTALLPROJECTS).setText(
            m_workplaceManager.getDefaultUserSettings().getListAllProjectsString());
        // add the <publishnotification> node
        workplaceGeneraloptions.addElement(N_PUBLISHNOTIFICATION).setText(
            m_workplaceManager.getDefaultUserSettings().getShowPublishNotificationString());
        // add the <show-fileuploadbutton> node
        workplaceGeneraloptions.addElement(N_SHOWFILEUPLOADBUTTON).setText(
            m_workplaceManager.getDefaultUserSettings().getShowFileUploadButtonString());
        // add the <allowbrokenrelations> node
        workplaceGeneraloptions.addElement(N_ALLOWBROKENRELATIONS).setText(
            String.valueOf(m_workplaceManager.getDefaultUserSettings().isAllowBrokenRelations()));
        // add the <publishrelatedresources> node
        if (m_workplaceManager.getDefaultUserSettings().getPublishRelatedResources() != null) {
            workplaceGeneraloptions.addElement(N_PUBLISHRELATEDRESOURCES).setText(
                m_workplaceManager.getDefaultUserSettings().getPublishRelatedResources().toString());
        }
        // add the configuration for new folders
        // <newfolder-editproperties>
        workplaceGeneraloptions.addElement(N_NEWFOLDEREDITPROPERTIES).setText(
            m_workplaceManager.getDefaultUserSettings().getNewFolderEditProperties().toString());
        // <newfolder-createindexpage>
        workplaceGeneraloptions.addElement(N_NEWFOLDERCREATEINDEXPAGE).setText(
            m_workplaceManager.getDefaultUserSettings().getNewFolderCreateIndexPage().toString());
        // <show-uploadtypedialog>
        workplaceGeneraloptions.addElement(N_SHOWUPLOADTYPEDIALOG).setText(
            m_workplaceManager.getDefaultUserSettings().getShowUploadTypeDialog().toString());

        // add the <workplace-startupsettings> node
        Element workplaceStartupsettings = workplacePreferences.addElement(N_WORKPLACESTARTUPSETTINGS);
        // add the <locale> node
        workplaceStartupsettings.addElement(N_LOCALE).setText(
            m_workplaceManager.getDefaultUserSettings().getLocale().toString());
        // add the <project> node
        workplaceStartupsettings.addElement(N_PROJECT).setText(
            m_workplaceManager.getDefaultUserSettings().getStartProject());
        // add the <view> node
        workplaceStartupsettings.addElement(N_WORKPLACEVIEW).setText(
            m_workplaceManager.getDefaultUserSettings().getStartView());
        // add the <folder> node
        workplaceStartupsettings.addElement(N_FOLDER).setText(
            m_workplaceManager.getDefaultUserSettings().getStartFolder());
        // add the <site> node
        workplaceStartupsettings.addElement(N_SITE).setText(m_workplaceManager.getDefaultUserSettings().getStartSite());
        // add the <restrictexplorerview> node
        workplaceStartupsettings.addElement(N_RESTRICTEXPLORERVIEW).setText(
            m_workplaceManager.getDefaultUserSettings().getRestrictExplorerViewString());

        // add the <workplace-search> node
        Element workplaceSearch = workplacePreferences.addElement(N_WORKPLACESEARCH);
        // add the <searchindex-name> node
        workplaceSearch.addElement(N_SEARCHINDEXNAME).setText(
            m_workplaceManager.getDefaultUserSettings().getWorkplaceSearchIndexName());
        // add the <searchview-explorer> node
        workplaceSearch.addElement(N_SEARCHVIEWSTYLE).setText(
            m_workplaceManager.getDefaultUserSettings().getWorkplaceSearchViewStyle().toString());

        // add the <explorer-preferences> node
        Element explorerPreferences = defaultPreferences.addElement(N_EXPLORERPREFERENCES);
        // add the <explorer-generaloptions> node
        Element explorerGeneraloptions = explorerPreferences.addElement(N_EXPLORERGENERALOPTIONS);
        // add the <buttonstyle> node
        explorerGeneraloptions.addElement(N_BUTTONSTYLE).setText(
            m_workplaceManager.getDefaultUserSettings().getExplorerButtonStyleString());
        // add the <reporttype> node
        explorerGeneraloptions.addElement(N_ENTRIES).setText(
            "" + m_workplaceManager.getDefaultUserSettings().getExplorerFileEntries());
        // add the <entryoptions> node
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_workplaceManager.getDefaultUserSettings().getExporerFileEntryOptions())) {
            explorerGeneraloptions.addElement(N_ENTRYOPTIONS).setText(
                m_workplaceManager.getDefaultUserSettings().getExporerFileEntryOptions());
        }
        // add the <explorer-displayoption> node
        Element explorerDisplayoptions = explorerPreferences.addElement(N_EXPLORERDISPLAYOPTIONS);
        // add the <show-title> node
        explorerDisplayoptions.addElement(N_TITLE).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileTitle());
        // add the <show-navtext> node
        explorerDisplayoptions.addElement(N_NAVTEXT).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileNavText());
        // add the <show-type> node
        explorerDisplayoptions.addElement(N_TYPE).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileType());
        // add the <show-datelastmodified> node
        explorerDisplayoptions.addElement(N_DATELASTMODIFIED).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileDateLastModified());
        // add the <show-datecreated> node
        explorerDisplayoptions.addElement(N_DATECREATED).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileDateCreated());
        // add the <show-lockedby> node
        explorerDisplayoptions.addElement(N_LOCKEDBY).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileLockedBy());
        // add the <show-permissions> node
        explorerDisplayoptions.addElement(N_PERMISSIONS).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFilePermissions());
        // add the <show-size> node
        explorerDisplayoptions.addElement(N_SIZE).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileSize());
        // add the <show-state> node
        explorerDisplayoptions.addElement(N_STATE).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileState());
        // add the <show-userlastmodified> node
        explorerDisplayoptions.addElement(N_USERLASTMODIFIED).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileUserLastModified());
        // add the <show-usercreated> node
        explorerDisplayoptions.addElement(N_USERCREATED).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileUserCreated());
        // add the <show-datereleased> node
        explorerDisplayoptions.addElement(N_DATERELEASED).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileDateReleased());
        // add the <show-dateexpired> node
        explorerDisplayoptions.addElement(N_DATEEXPIRED).setText(
            m_workplaceManager.getDefaultUserSettings().getShowExplorerFileDateExpired());

        // add the <dialog-preferences> node
        Element dialogPreferences = defaultPreferences.addElement(N_DIALOGSPREFERENCES);
        // add the <dialog-defaultsettings> node
        Element dialogDefaultSettings = dialogPreferences.addElement(N_DIALOGSDEFAULTSETTINGS);
        // add the <filecopy> node
        dialogDefaultSettings.addElement(N_FILECOPY).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogCopyFileModeString());
        // add the <foldercopy> node
        dialogDefaultSettings.addElement(N_FOLDERCOPY).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogCopyFolderModeString());
        // add the <filedeletion> node
        dialogDefaultSettings.addElement(N_FILEDELETION).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogDeleteFileModeString());
        // add the <directpublish> node
        dialogDefaultSettings.addElement(N_DIRECTPUBLISH).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogPublishSiblingsString());
        // add the <showlock> node
        dialogDefaultSettings.addElement(N_SHOWLOCK).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogShowLockString());
        // add the <showexportsettings> node
        dialogDefaultSettings.addElement(N_SHOWEXPORTSETTINGS).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogShowExportSettingsString());
        // add the <expand-permissionsuser> node
        dialogDefaultSettings.addElement(N_EXPANDPERMISSIONSUSER).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogExpandUserPermissionsString());
        // add the <expand-permissionsinherited> node
        dialogDefaultSettings.addElement(N_EXPANDPERMISSIONSINHERITED).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogExpandInheritedPermissionsString());
        // add the <permissions-inheritonfolder> node
        dialogDefaultSettings.addElement(N_PERMISSIONSINHERITONFOLDER).setText(
            m_workplaceManager.getDefaultUserSettings().getDialogPermissionsInheritOnFolderString());

        // add the <editors-preferences> node
        Element editorsPreferences = defaultPreferences.addElement(N_EDITORPREFERENCES);
        // add the <editors-generaloptions> node
        Element editorGeneraloptions = editorsPreferences.addElement(N_EDITORGENERALOPTIONS);
        // add the <buttonstyle> node
        editorGeneraloptions.addElement(N_BUTTONSTYLE).setText(
            m_workplaceManager.getDefaultUserSettings().getEditorButtonStyleString());
        // add the <directedit> node
        editorGeneraloptions.addElement(N_DIRECTEDITSTYLE).setText(
            m_workplaceManager.getDefaultUserSettings().getDirectEditButtonStyleString());
        // add the <editors-preferrededitors> node
        Element editorPreferrededitors = editorsPreferences.addElement(N_EDITORPREFERREDEDITORS);
        // add the <editor> nodes
        Iterator editors = m_workplaceManager.getDefaultUserSettings().getEditorSettings().entrySet().iterator();
        while (editors.hasNext()) {
            Map.Entry e = (Map.Entry)editors.next();
            String type = (String)e.getKey();
            String value = (String)e.getValue();
            Element editor = editorPreferrededitors.addElement(N_EDITOR);
            editor.addAttribute(A_TYPE, type);
            editor.addAttribute(A_VALUE, value);
        }

        if (!m_workplaceManager.getDefaultUserSettings().getStartGalleriesSettings().isEmpty()) {
            //add the <galleries-preferences> node
            Element galleriesPreferences = defaultPreferences.addElement(N_GALLERIESPREFERENCES);
            //add the <startgalleries> node
            Element galleryStartGalleries = galleriesPreferences.addElement(N_STARTGALLERIES);
            //add the <startgallery> nodes
            Iterator startGalleries = m_workplaceManager.getDefaultUserSettings().getStartGalleriesSettings().entrySet().iterator();
            while (startGalleries.hasNext()) {
                Map.Entry e = (Map.Entry)startGalleries.next();
                String type = (String)e.getKey();
                String path = (String)e.getValue();
                Element startGallery = galleryStartGalleries.addElement(N_STARTGALLERY);
                startGallery.addAttribute(A_TYPE, type);
                startGallery.addAttribute(A_PATH, path);

            }
        }

        if (m_workplaceManager.getCustomFoot() != null) {
            // add the <workplace-footcustom> node
            Element workplaceFootCustom = workplaceElement.addElement(N_WORKPLACECUSTOMFOOT);
            workplaceFootCustom.addElement(N_COLOR).setText(m_workplaceManager.getCustomFoot().getColor());
            workplaceFootCustom.addElement(N_BACKGROUNDCOLOR).setText(
                m_workplaceManager.getCustomFoot().getBackgroundColor());
            Element textElement = workplaceFootCustom.addElement(N_TEXT);
            textElement.addAttribute(A_REPLACE, Boolean.toString(m_workplaceManager.getCustomFoot().isReplaceDefault()));
            textElement.setText(m_workplaceManager.getCustomFoot().getText());
        }

        // add the tool-manager node
        Element toolManagerElement = workplaceElement.addElement(N_TOOLMANAGER);
        Element rootsElement = toolManagerElement.addElement(N_ROOTS);
        Iterator it = m_workplaceManager.getToolManager().getToolRoots().iterator();
        while (it.hasNext()) {
            CmsToolRootHandler root = (CmsToolRootHandler)it.next();
            Element rootElement = rootsElement.addElement(N_ROOT);
            rootElement.addElement(N_KEY).addText(root.getKey());
            rootElement.addElement(N_URI).addText(root.getUri());
            rootElement.addElement(N_NAME).addText(root.getName());
            rootElement.addElement(N_HELPTEXT).addText(root.getHelpText());
        }

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
     * Adds the digester rules for the menurules node.<p>
     * 
     * @param digester the digester object
     */
    protected void addContextMenuRules(Digester digester) {

        // add menu rules
        digester.addObjectCreate(
            "*/" + N_WORKPLACE + "/" + N_EXPLORERTYPES + "/" + N_MENURULES + "/" + N_MENURULE,
            CmsMenuRule.class);
        digester.addSetNext(
            "*/" + N_WORKPLACE + "/" + N_EXPLORERTYPES + "/" + N_MENURULES + "/" + N_MENURULE,
            "addMenuRule");
        // set the name of the menu rule
        digester.addCallMethod(
            "*/" + N_WORKPLACE + "/" + N_EXPLORERTYPES + "/" + N_MENURULES + "/" + N_MENURULE,
            "setName",
            1);
        digester.addCallParam(
            "*/" + N_WORKPLACE + "/" + N_EXPLORERTYPES + "/" + N_MENURULES + "/" + N_MENURULE,
            0,
            A_NAME);
        // add a single menu item rule to the list of rules
        digester.addCallMethod("*/"
            + N_WORKPLACE
            + "/"
            + N_EXPLORERTYPES
            + "/"
            + N_MENURULES
            + "/"
            + N_MENURULE
            + "/"
            + N_MENUITEMRULE, "addMenuItemRuleName", 1);
        digester.addCallParam("*/"
            + N_WORKPLACE
            + "/"
            + N_EXPLORERTYPES
            + "/"
            + N_MENURULES
            + "/"
            + N_MENURULE
            + "/"
            + N_MENUITEMRULE, 0, A_CLASS);
    }

    /**
     * Adds the digester rules for the defaultaccesscontrol node.<p>
     * 
     * @param digester the digester object
     */
    protected void addDefaultAccessControlRules(Digester digester) {

        digester.addObjectCreate("*/"
            + N_WORKPLACE
            + "/"
            + N_EXPLORERTYPES
            + "/"
            + N_DEFAULTACCESSCONTROL
            + "/"
            + N_ACCESSCONTROL, CmsExplorerTypeAccess.class);
        digester.addSetNext("*/"
            + N_WORKPLACE
            + "/"
            + N_EXPLORERTYPES
            + "/"
            + N_DEFAULTACCESSCONTROL
            + "/"
            + N_ACCESSCONTROL, "setDefaultAccess");

        digester.addCallMethod("*/"
            + N_WORKPLACE
            + "/"
            + N_EXPLORERTYPES
            + "/"
            + N_DEFAULTACCESSCONTROL
            + "/"
            + N_ACCESSCONTROL
            + "/"
            + N_ACCESSENTRY, "addAccessEntry", 2);
        digester.addCallParam("*/"
            + N_WORKPLACE
            + "/"
            + N_EXPLORERTYPES
            + "/"
            + N_DEFAULTACCESSCONTROL
            + "/"
            + N_ACCESSCONTROL
            + "/"
            + N_ACCESSENTRY, 0, A_PRINCIPAL);
        digester.addCallParam("*/"
            + N_WORKPLACE
            + "/"
            + N_EXPLORERTYPES
            + "/"
            + N_DEFAULTACCESSCONTROL
            + "/"
            + N_ACCESSCONTROL
            + "/"
            + N_ACCESSENTRY, 1, A_PERMISSIONS);
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
        int todo = 0;

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
        digester.addCallMethod(xPathPrefix + "/" + N_UPLOADAPPLET, "setUploadApplet", 0);
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
        digester.addCallMethod(xPathPrefix + "/" + N_ENTRYOPTIONS, "setExporerFileEntryOptions", 0);

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

    }

    /**
     * Adds the digester rules for the multicontextmenu node.<p>
     * 
     * @param digester the digester object
     */
    protected void addMultiContextMenuRules(Digester digester) {

        // add multi context menu
        String xPath = "*/" + N_WORKPLACE + "/" + N_EXPLORERTYPES + "/" + N_MULTICONTEXTMENU;
        digester.addObjectCreate(xPath, CmsExplorerContextMenu.class);
        digester.addSetNext(xPath, "setMultiContextMenu");
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