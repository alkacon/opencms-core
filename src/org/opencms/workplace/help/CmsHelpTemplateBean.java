/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/help/CmsHelpTemplateBean.java,v $
 * Date   : $Date: 2005/06/03 15:48:29 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.workplace.help;

import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsPropertyUtils;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.I_CmsWpConstants;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.ExtendedProperties;

/**
 * The bean that provides methods to build the HTML for the single online help frames.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.9.2
 */
public class CmsHelpTemplateBean extends CmsDialog {
    
    /** File name of the default help file to load. */
    public static final String DEFAULT_HELPFILE = "index.html";
    
    /** File name of the help mappings properties file(s). */
    public static final String HELPMAPPINGS_FILENAME = "mappings.properties";
    
    /** The name of the help module. */
    public static final String MODULE_NAME = "org.opencms.workplace.help";
    
    /** Request parameter name for the buildframe flag parameter. */
    public static final String PARAM_BUILDFRAME = "buildframe";
    
    /** Request parameter name for the helpresource uri. */
    public static final String PARAM_HELPRESOURCE = "helpresource";
    
    /** Request parameter name for the homelink in head frame. */
    public static final String PARAM_HOMELINK = "homelink";
    
    /** Request parameter name for the workplaceresource uri. */
    public static final String PARAM_WORKPLACERESOURCE = "workplaceresource";
    
    /** Value of the NavInfo property indicating the start folder of the help. */
    public static final String PROPERTY_VALUE_HELPSTART = "help.start";
    
    /** VFS path to the help folder, contains a macro for the Locale which has to be resolved. */
    public static final String PATH_HELP = I_CmsWpConstants.C_VFS_PATH_LOCALES + "${" + CmsMacroResolver.KEY_REQUEST_LOCALE +"}/help/";
    
    /** Relative RFS path of the help mappings property file(s). */
    public static final String RFS_HELPMAPPINGS = "classes/" + MODULE_NAME.replace('.', '/') + "/" + HELPMAPPINGS_FILENAME;
    
    /** Absolute path to used JSP templates. */
    public static final String TEMPLATEPATH = I_CmsWpConstants.C_VFS_PATH_MODULES + MODULE_NAME + "/jsptemplates/";
    
    /** Request parameter for the help build frameset flag. */
    private String m_paramBuildframe;
    
    /** Request parameter for the help resource to display. */
    private String m_paramHelpresource;
    
    /** Request parameter for the home link to use in the head frame. */
    private String m_paramHomelink;
    
    /** Request parameter for the current workplace resource. */
    private String m_paramWorkplaceresource;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsHelpTemplateBean(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHelpTemplateBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Returns the java script method to open the online help popup window.<p>
     * 
     * @param locale the current users workplace Locale
     * @return the java script method to open the online help popup window
     */
    public static String buildOnlineHelpJavaScript(Locale locale) {
        
        StringBuffer result = new StringBuffer(16);
        
        result.append("function openOnlineHelp(wpUri) {\n");
        result.append("\tif (wpUri == null || wpUri == \"\") {\n");
        result.append("\t\tif (top.body != null && top.body.explorer_body != null) {\n");
        result.append("\t\t\t// determine currently shown explorer item\n");
        result.append("\t\t\ttry {\n");
        result.append("\t\t\t\twpUri = top.body.explorer_body.explorer_files.location.pathname;\n");
        result.append("\t\t\t} catch (e) {}\n");
        result.append("\t\t} else if (top.body != null && top.body.admin_content != null) {\n");
        result.append("\t\t\t// determine currently shown administration item\n");
        result.append("\t\t\tvar parameters = \"\";\n");
        result.append("\t\t\ttry {\n");
        result.append("\t\t\t\tparameters = decodeURIComponent(top.body.admin_content.tool_title.location.search);\n");
        result.append("\t\t\t} catch (e) {\n");
        result.append("\t\t\t\ttry {\n");
        result.append("\t\t\t\t\tparameters = decodeURIComponent(top.body.admin_content.location.search);\n");
        result.append("\t\t\t\t} catch (e) {}\n");
        result.append("\t\t\t}\n");
        result.append("\t\t\tvar pathIndex = parameters.lastIndexOf(\"path=\");\n");
        result.append("\t\t\tif (pathIndex != -1) {\n");
        result.append("\t\t\t\tparameters = parameters.substring(pathIndex + 5);\n");
        result.append("\t\t\t\tif (parameters.indexOf(\"&\") != -1) {\n");
        result.append("\t\t\t\t\tparameters = parameters.substring(0, parameters.indexOf(\"&\"));\n");
        result.append("\t\t\t\t}\n");
        result.append("\t\t\t\twpUri = parameters + \"/\";\n");
        result.append("\t\t\t} else {\n");
        result.append("\t\t\t\twpUri = \"/administration/\"\n");
        result.append("\t\t\t}\n");
        result.append("\t\t}\n");
        result.append("\t}\n");
        result.append("\twindow.open(\"../locales/");
        result.append(locale);
        result.append("/help/index.html?").append(PARAM_BUILDFRAME).append("=true");
        result.append("&").append(PARAM_WORKPLACERESOURCE).append("=\" + wpUri, \"cmsonlinehelp\", ");
        result.append("\"toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width=600,height=450\");\n");
        result.append("}\n");
               
        return result.toString();
    }
    
    /**
     * Returns the HTML for the body frame of the online help.<p>
     * 
     * @return the HTML for the body frame of the online help
     */
    public String displayBody() {
        
        StringBuffer result = new StringBuffer(256);
        
        // store current project
        CmsProject project = getJsp().getRequestContext().currentProject();
        try {
            // change to online project to increase display performance
            getJsp().getRequestContext().setCurrentProject(getCms().readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
        } catch (CmsException e) {
            // failed to switch to project
        } finally {
            result.append(buildHtmlHelpStart("onlinehelp.css", true));
            result.append("<body>\n");
            result.append("<a name=\"top\"></a>\n");
            result.append("<table class=\"helpcontent\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
            result.append("<tr>\n");
            result.append("\t<td class=\"helpnav\">\n");
            result.append("\t\t<a class=\"navhelphead\" href=\"javascript:top.body.location.href=top.head.homeLink;\">");
            result.append(key(Messages.GUI_HELP_NAVIGATION_HEAD_0));
            result.append("</a>\n");
            result.append(buildHtmlHelpNavigation());
            result.append("</td>\n");
            result.append("\t<td class=\"helpcontent\">\n");
            result.append("\t\t<h1>");
            result.append(getJsp().property(I_CmsConstants.C_PROPERTY_TITLE, getParamHelpresource(), key(Messages.GUI_HELP_FRAMESET_TITLE_0)));
            result.append("</h1>\n");
            result.append(getJsp().getContent(getParamHelpresource(), "body", getLocale()));
            result.append("\t</td>\n");
            result.append("</tr>\n");
            result.append("</table>\n");
            result.append(buildHtmlHelpEnd());
            // set back to current project
            getJsp().getRequestContext().setCurrentProject(project);
        }

        return result.toString();
    }
    
    /**
     * Returns the HTML for the head frame of the online help.<p>
     * 
     * @return the HTML for the head frame of the online help
     */
    public String displayHead() {

        StringBuffer result = new StringBuffer(16);
        
        int buttonStyle = getSettings().getUserSettings().getWorkplaceButtonStyle();
        
        // store current project
        CmsProject project = getJsp().getRequestContext().currentProject();
        try {
            // change to online project to increase display performance
            getJsp().getRequestContext().setCurrentProject(getCms().readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
        } catch (CmsException e) {
            // failed to switch to project
        } finally {
            String resourcePath = getJsp().link("/system/modules/" + MODULE_NAME + "/resources/");
            
            result.append(buildHtmlHelpStart("workplace.css", false));
            result.append("<body class=\"buttons-head\" unselectable=\"on\">\n");
            
            // store home link in JS variable to use it in body frame
            result.append("<script type=\"text/javascript\">\n<!--\n");
            result.append("\tvar homeLink = \"");
            result.append(getParamHomelink());
            result.append("\";\n");
            result.append("//-->\n</script>\n");
            
            result.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
            result.append("<tr>\n");
            result.append("\t<td align=\"left\">\n");
            
            // display navigation buttons
            result.append(buttonBar(HTML_START));
            result.append(buttonBarStartTab(0, 5));
            result.append(button("javascript:history.back();", null, "back.png", Messages.GUI_HELP_BUTTON_BACK_0, buttonStyle, resourcePath));
            result.append(button("javascript:history.forward();", null, "next.png", Messages.GUI_HELP_BUTTON_NEXT_0, buttonStyle, resourcePath));
            result.append(button("javascript:top.body.location.href='" + getParamHomelink() + "';", null, "contents.png", Messages.GUI_HELP_BUTTON_CONTENTS_0, buttonStyle, resourcePath));
            result.append(buttonBar(HTML_END));
            
            result.append("</td>\n");
            result.append("\t<td align=\"right\" width=\"100%\">\n");  
            
            // display close button
            result.append(buttonBar(HTML_START));
            result.append(buttonBarSeparator(5, 0));
            result.append(button("javascript:top.close();", null, "close", Messages.GUI_HELP_BUTTON_CLOSE_0, buttonStyle, resourcePath));
            result.append(buttonBar(HTML_END));
    
            result.append("\t</td>\n");
            result.append("\t<td>&nbsp;</td>\n");
            result.append("<td>");
            
            // display logo
            result.append("<span style=\"display: block; width: 80px; height: 22px; background-image: url(\'");
            result.append(getSkinUri());
            result.append("commons/workplace.gif");
            result.append("\'); \"></span>");
            //result.append("<img src=\"");
            //result.append(getSkinUri());
            //result.append("commons/workplace.gif\" border=\"0\" alt=\"\">");
            result.append("</td>");
            result.append("</tr>\n");
            result.append("</table>\n");
            result.append(buildHtmlHelpEnd());
            
            // set back to current project
            getJsp().getRequestContext().setCurrentProject(project);
        }

        return result.toString();
    }
    
    /**
     * Generates the HTML for the online help frameset or redirects to the help body, depending on the build frameset flag.<p>
     * 
     * @return the HTML for the online help frameset or an empty String (redirect)
     * @throws IOException if redirection fails
     */
    public String displayHelp() throws IOException {
        
        String result = "";
        // store current project
        CmsProject project = getJsp().getRequestContext().currentProject();
        try {
            // change to online project to increase display performance
            getJsp().getRequestContext().setCurrentProject(getCms().readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
        } catch (CmsException e) {
            // failed to switch to project
        } finally {
            
            if (isBuildFrameset()) {
                // build the online help frameset
                result = displayFrameset(); 
                // set back to current project
                getJsp().getRequestContext().setCurrentProject(project);
            } else {
                // redirect to the help body
                StringBuffer bodyLink = new StringBuffer(8);
                bodyLink.append(TEMPLATEPATH);
                bodyLink.append("help_body.jsp?");
                bodyLink.append(CmsHelpTemplateBean.PARAM_HELPRESOURCE);
                bodyLink.append("=");
                bodyLink.append(getJsp().getRequestContext().getUri());
                bodyLink.append("&");
                bodyLink.append(I_CmsConstants.C_PARAMETER_LOCALE);
                bodyLink.append("=");
                bodyLink.append(getLocale());
                String redirectLink = getJsp().link(bodyLink.toString());
                // set back to current project
                getJsp().getRequestContext().setCurrentProject(project);
                getJsp().getResponse().sendRedirect(redirectLink);
            }            
        }
        return result;
    }

    /**
     * Returns the buildframe parameter indicating if the frameset should be generated.<p>
     *
     * @return the buildframe parameter indicating if the frameset should be generated
     */
    public String getParamBuildframe() {

        return m_paramBuildframe;
    }
    
    /**
     * Returns the helpresource parameter value.<p>
     *
     * @return the helpresource parameter value
     */
    public String getParamHelpresource() {

        return m_paramHelpresource;
    }
    
    /**
     * Returns the workplaceresource parameter value.<p>
     *
     * @return the workplaceresource parameter value
     */
    public String getParamWorkplaceresource() {

        return m_paramWorkplaceresource;
    }
    
    /**
     * Sets the buildframe parameter indicating if the frameset should be generated.<p>
     *
     * @param buildframe the buildframe parameter indicating if the frameset should be generated
     */
    public void setParamBuildframe(String buildframe) {

        m_paramBuildframe = buildframe;
    }
    
    /**
     * Sets the helpresource parameter value.<p>
     *
     * @param helpresource the helpresource parameter value
     */
    public void setParamHelpresource(String helpresource) {

        m_paramHelpresource = helpresource;
    }
    
    /**
     * Sets the workplaceresource parameter value.<p>
     *
     * @param workplaceresource the workplaceresource parameter value
     */
    public void setParamWorkplaceresource(String workplaceresource) {
        
        m_paramWorkplaceresource = workplaceresource;
    }
    
    /**
     * Returns the HTML for the end of the page.<p>
     * 
     * @return the HTML for the end of the page
     */
    protected String buildHtmlHelpEnd() {
        
        StringBuffer result = new StringBuffer(4);
        result.append("</body>\n");
        result.append("</html>");
        return result.toString();
    }
    
    /**
     * Returns the HTML to build the navigation of the online help folder.<p>
     * 
     * @return the HTML to build the navigation of the online help folder
     */
    protected String buildHtmlHelpNavigation() {

        StringBuffer result = new StringBuffer(16);
        // determine current URI
        String currentUri = getParamHelpresource();
        // determine level of help start folder
        int helpLevel = CmsResource.getPathLevel(PATH_HELP);
 
        String startFolder = CmsResource.getFolderPath(currentUri);
        while (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startFolder)) {
            try {
                String prop = getCms().readPropertyObject(startFolder, CmsJspNavElement.C_PROPERTY_NAVINFO, false).getValue(null);
                if (PROPERTY_VALUE_HELPSTART.equals(prop)) {
                    helpLevel = CmsResource.getPathLevel(startFolder);
                    break;
                }
            } catch (CmsException e) {
                // error reading property value, ignor
            }
            startFolder = CmsResource.getParentFolder(startFolder);
        }

        // get a list of all pages / subfolders in the help folder
        List navList = getJsp().getNavigation().getNavigationTreeForFolder(currentUri, helpLevel, 99);
        Iterator i = navList.iterator();
        
        while (i.hasNext()) {
            CmsJspNavElement nav = (CmsJspNavElement)i.next();
            // calculate level to display
            int level = nav.getNavTreeLevel() - (helpLevel - 1);
            if (nav.getResourceName().equals(currentUri) || (nav.isFolderLink() && currentUri.equals(nav.getResourceName() + "index.html"))) {
                result.append("\t\t<span class=\"navhelpcurrent\" style=\"padding-left: ");
                result.append(level * 10);
                result.append("px; background-position: ");
                result.append((level - 1) * 10);
                result.append("px 1px;\">");
                result.append(nav.getNavText());
                result.append("</span><br style=\"clear:left\">\n");       
            } else {
                result.append("\t\t<a class=\"navhelp\" style=\"padding-left: ");
                result.append(level * 10);
                result.append("px; background-position: ");
                result.append((level - 1) * 10);
                result.append("px 1px;\" href=\"");
                if (nav.isFolderLink()) {
                    // append file name to folder links to avoid static export issues
                    result.append(getJsp().link(nav.getResourceName() + "index.html"));
                } else {
                    result.append(getJsp().link(nav.getResourceName()));
                }
                result.append("\">");
                result.append(nav.getNavText());
                result.append("</a><br style=\"clear:left\">\n");
            }           
        }
        return result.toString();
    }
    
    /**
     * Returns the HTML for the start of the page.<p>
     * 
     * @param cssFile the CSS file name to use
     * @param transitional if true, transitional doctype is used
     * @return the HTML for the start of the page
     */
    protected String buildHtmlHelpStart(String cssFile, boolean transitional) {
        
        StringBuffer result = new StringBuffer(8);
        if (transitional) {
            result.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
        } else {
            result.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
        }
        result.append("<html>\n");
        result.append("<head>\n");
        result.append("\t<title>");
        if (CmsStringUtil.isNotEmpty(getParamHelpresource())) {
            result.append(getJsp().property(I_CmsConstants.C_PROPERTY_TITLE, getParamHelpresource(), key(Messages.GUI_HELP_FRAMESET_TITLE_0)));
        } else {
            result.append(key(Messages.GUI_HELP_FRAMESET_TITLE_0));
        }
        result.append("</title>\n");
        result.append("\t<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        result.append(getStyleUri(getJsp(), cssFile)).append("\">\n");
        result.append("</head>\n");
        return result.toString();
    }
    
    /**
     * Returns the HTML to build the frameset for the online help popup window.<p>
     * 
     * @return the HTML to build the frameset for the online help popup window
     */
    protected String displayFrameset() {

        StringBuffer result = new StringBuffer(8);
        result.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
        result.append("<html>\n");
        result.append("<head>\n");
        result.append("\t<title>");
        result.append(key(Messages.GUI_HELP_FRAMESET_TITLE_0));
        result.append("</title>\n");
        
        // script to avoid frameset display errors
        result.append("<script type=\"text/javascript\">\n<!--\n");
        result.append("\t if (window.name == \"body\") {\n");
        result.append("\t\ttop.location.href = \"" + getJsp().link(getJsp().getRequestContext().getUri()) + "\";\n");
        result.append("\t}\n");
        result.append("//-->\n</script>\n");  
        result.append("<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
        result.append(getEncoding());
        result.append("\">\n");
        
        result.append("</head>\n\n");
        result.append("<frameset rows=\"24,*\" border=\"0\" frameborder=\"0\" framespacing=\"0\">\n");
        result.append("\t<frame name=\"head\" src=\"");
        
        
        StringBuffer headLink = new StringBuffer(8);
        headLink.append(TEMPLATEPATH);
        headLink.append("help_head.jsp?");
        headLink.append(I_CmsConstants.C_PARAMETER_LOCALE);
        headLink.append("=");
        headLink.append(getLocale());
        headLink.append("&");
        headLink.append(PARAM_HOMELINK);
        headLink.append("=");
        headLink.append(getParamHomelink());
        result.append(getJsp().link(headLink.toString()));
        result.append("\" scrolling=\"no\" noresize>\n");
        result.append("\t<frame name=\"body\" src=\"");
        StringBuffer bodyLink = new StringBuffer(8);
        bodyLink.append(TEMPLATEPATH);
        bodyLink.append("help_body.jsp?");
        bodyLink.append(CmsHelpTemplateBean.PARAM_HELPRESOURCE);
        bodyLink.append("=");
        bodyLink.append(getJsp().getRequestContext().getUri());
        bodyLink.append("&");
        bodyLink.append(I_CmsConstants.C_PARAMETER_LOCALE);
        bodyLink.append("=");
        bodyLink.append(getLocale());
        result.append(getJsp().link(bodyLink.toString()));
        result.append("\" scrolling=\"auto\" noresize>\n");
        result.append("</frameset>\n\n");
        result.append("<body></body>\n");
        result.append("</html>");

        return result.toString();
    }
    
    /**
     * Determines the mapped help page for a given workplace resource URI.<p>
     * 
     * If a mapping information is found, the requested URI is set to the found value.<p>
     * 
     * If no workplace resource URI is given, nothing is changed.<p>
     */
    protected void getMappedHelpUri() {
        
        if (CmsStringUtil.isNotEmpty(getParamWorkplaceresource())) {
            // found a workplace resource parameter, try to get a mapping for it
            String helpResource = null;
            if (getCms().existsResource(resolveMacros(getParamWorkplaceresource()), CmsResourceFilter.requireType(CmsResourceTypeXmlPage.getStaticTypeId()))) {
                // given workplace resource is a page in VFS, use it as start point
                helpResource = resolveMacros(getParamWorkplaceresource());
                setParamHomelink(getJsp().link(helpResource));
            } else {
                // given workplace resource does not exist, resolve mapping
                try {
                    // try to read the mappings from the current module
                    String absolutePath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(RFS_HELPMAPPINGS);
                    ExtendedProperties props = CmsPropertyUtils.loadProperties(absolutePath);
           
                    String wpResource = getParamWorkplaceresource();
                    if (wpResource.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
                        // remove context from workplace path
                        wpResource = wpResource.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
                    }
                    // determine mapping for workplace resource
                    while (wpResource != null && CmsStringUtil.isEmpty(helpResource)) {
                        helpResource = props.getString(wpResource, null);
                        wpResource = CmsResource.getParentFolder(wpResource);                
                    } 
                } catch (IOException e) {
                    // no mappings found in module, ignore              
                }           
                
                if (CmsStringUtil.isEmpty(helpResource)) {
                    // no mapping found, use default help URI
                    helpResource = DEFAULT_HELPFILE;
                }
                // create path to the help resource
                helpResource = resolveMacros(PATH_HELP) + helpResource;
                if (! getCms().existsResource(helpResource, CmsResourceFilter.IGNORE_EXPIRATION)) {
                    helpResource = resolveMacros(PATH_HELP) + DEFAULT_HELPFILE;
                }
                setParamHomelink(getJsp().link(resolveMacros(PATH_HELP) + DEFAULT_HELPFILE));
            }
            // set URI to found help page URI
            getJsp().getRequestContext().setUri(helpResource);
        }
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        addMessages(Messages.get().getBundleName());
        addMessages(org.opencms.workplace.Messages.get().getBundleName());
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        
        // determine initial page to show on frameset generation
        if (isBuildFrameset()) {
            // get the mapped URI
            getMappedHelpUri();
        }
    }
    
    /**
     * Returns true if the online help frameset has to be generated.<p>
     * 
     * @return true if the online help frameset has to be generated, otherwise false
     */
    protected boolean isBuildFrameset() {

        return Boolean.valueOf(getParamBuildframe()).booleanValue();
    }
    
    /**
     * Returns the homelink parameter value.<p>
     *
     * @return the homelink parameter value
     */
    public String getParamHomelink() {

        return m_paramHomelink;
    }
    
    /**
     * Sets the homelink parameter value.<p>
     *
     * @param homelink the homelink parameter value
     */
    public void setParamHomelink(String homelink) {

        m_paramHomelink = homelink;
    }
    
}