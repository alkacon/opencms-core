/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsHttpServlet.java,v $
* Date   : $Date: 2001/11/23 13:22:49 $
* Version: $Revision: 1.20 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.boot.*;
import com.opencms.file.*;
import com.opencms.util.*;

/**
 * This class is the main servlet of the OpenCms system.
 * <p>
 * From here, all other operations are invoked.
 * It initializes the Servlet and processes all requests send to the OpenCms.
 * Any incoming request is handled in multiple steps:
 * <ul>
 * <li>The requesting user is authenticated and a CmsObject with the user information
 * is created. The CmsObject is needed to access all functions of the OpenCms, limited by
 * the actual user rights. If the user is not identified, it is set to the default (guest)
 * user. </li>
 * <li>The requested document is loaded into the OpenCms and depending on its type and the
 * users rights to display or modify it, it is send to one of the OpenCms launchers do
 * display it. </li>
 * <li>
 * The document is forwared to a template class which is selected by the launcher and the
 * output is generated.
 * </li>
 * </ul>
 * <p>
 * The class overloades the standard Servlet methods doGet and doPost to process
 * Http requests.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.20 $ $Date: 2001/11/23 13:22:49 $
 *
 * */
public class OpenCmsHttpServlet extends HttpServlet implements I_CmsConstants,I_CmsLogChannels {

    /**
     * The name of the redirect entry in the configuration file.
     */
    static final String C_PROPERTY_REDIRECT = "redirect";

    /**
     * The name of the redirect location entry in the configuration file.
     */
    static final String C_PROPERTY_REDIRECTLOCATION = "redirectlocation";

    /**
     * The configuration for the OpenCms servlet.
     */
    private Configurations m_configurations;

    /**
     * The session storage for all active users.
     */
    private CmsCoreSession m_sessionStorage;

    /**
     * The reference to the OpenCms system.
     */
    private A_OpenCms m_opencms;

    /**
     * Storage for redirects.
     */
    private Vector m_redirect = new Vector();

    /**
     * Storage for redirect locations.
     */
    private Vector m_redirectlocation = new Vector();

    /**
     * Storage for the clusterurl
     */
    private String m_clusterurl = null;

    /**
     * Checks if the requested resource must be redirected to the server docroot and
     * excecutes the redirect if nescessary.
     * @param cms The CmsObject
     * @returns true, if the ressource was redirected
     * @exeption Throws CmsException if something goes wrong.
     */
    private boolean checkRelocation(CmsObject cms) throws CmsException {
        CmsRequestContext context = cms.getRequestContext();

        // check the if the current project is the online project. Only in this project,
        // a redirect is nescessary.
        if(context.currentProject().equals(cms.onlineProject())) {
            String filename = context.getUri();

            // check all redirect locations
            for(int i = 0;i < m_redirect.size();i++) {
                String redirect = (String)m_redirect.elementAt(i);

                // found a match, so redirect
                if(filename.startsWith(redirect)) {
                    String redirectlocation = (String)m_redirectlocation.elementAt(i);
                    String doRedirect = redirectlocation + filename.substring(redirect.length());

                    // try to redirect
                    try {
                        ((HttpServletResponse)context.getResponse().getOriginalResponse()).sendRedirect(doRedirect);
                    }
                    catch(Exception e) {
                        throw new CmsException("Redirect fails :" + doRedirect, CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    // the ressource was redirected, return true
                    return true;
                } else {
                    // the ressource was not redirected, return false
                    return false;
                }
            }
        }
        // not in online-project, or no redirect information found - so no redirect needed
        return false;
    }

    /**
     * Generates a formated exception output. <br>
     * Because the exception could be thrown while accessing the system files,
     * the complete HTML code must be added here!
     * @param e The caught CmsException.
     * @return String containing the HTML code of the error message.
     */
    private String createErrorBox(CmsException e, CmsObject cms) {
        StringBuffer output = new StringBuffer();
        output.append("<HTML>\n");
        output.append("<script language=JavaScript>\n");
        output.append("<!--\n");
        output.append("function show_help()\n");
        output.append("{\n");
        output.append("return '2_2_2_2_5_5_1.html';\n");
        output.append("}\n");
        output.append("var shown=false;\n");
        output.append("function checkBrowser(){\n");
        output.append("this.ver=navigator.appVersion\n");
        output.append("this.dom=document.getElementById?1:0\n");
        output.append("this.ie5=(this.ver.indexOf(\"MSIE 5\")>-1 && this.dom)?1:0;\n");
        output.append("this.ie4=(document.all && !this.dom)?1:0;\n");
        output.append("this.ns5=(this.dom && parseInt(this.ver) >= 5) ?1:0;\n");
        output.append("this.ns4=(document.layers && !this.dom)?1:0;\n");
        output.append("this.bw=(this.ie5 || this.ie4 || this.ns4 || this.ns5)\n");
        output.append("return this\n");
        output.append("}\n");
        output.append("bw=new checkBrowser();\n");
        output.append("function makeObj(obj,nest){\n");
        output.append("nest=(!nest) ? '':'document.'+nest+'.';\n");
        output.append("this.el=bw.dom?document.getElementById(obj):bw.ie4?document.all[obj]:bw.ns4?eval(nest+'document.'+obj):0;\n");
        output.append("this.css=bw.dom?document.getElementById(obj).style:bw.ie4?document.all[obj].style:bw.ns4?eval(nest+'document.'+obj):0;\n");
        output.append("this.scrollHeight=bw.ns4?this.css.document.height:this.el.offsetHeight; \n");
        output.append("this.clipHeight=bw.ns4?this.css.clip.height:this.el.offsetHeight;\n");
        output.append("this.obj = obj + \"Object\";\n");
        output.append("eval(this.obj + \"=this\");\n");
        output.append("return this;\n");
        output.append("}\n");
        output.append("ns = (document.layers)? true:false;\n");
        output.append("ie = (document.all)? true:false;\n");
        output.append("if(ns)\n");
        output.append("{\n");
        output.append("var layerzeigen_01= 'document.layers.';\n");
        output.append("var layerzeigen_02= '.visibility=\"show\"';\n");
        output.append("var layerverstecken_01= 'document.layers.';\n");
        output.append("var layerverstecken_02= '.visibility=\"hide\"';\n");
        output.append("}\n");
        output.append("else\n");
        output.append("{\n");
        output.append("var layerzeigen_01= 'document.all.';\n");
        output.append("var layerzeigen_02= '.style.visibility=\"visible\"';\n");
        output.append("var layerverstecken_01= 'document.all.';\n");
        output.append("var layerverstecken_02= '.style.visibility=\"hidden\"';\n");
        output.append("}\n");
        output.append("function getWindowWidth() {\n");
        output.append("if( ns==true ) {\n");
        output.append("windowWidth = innerWidth;\n");
        output.append("}\n");
        output.append("else if( ie==true ) {\n");
        output.append("windowWidth = document.body.clientWidth;\n");
        output.append("}\n");
        output.append("return windowWidth;\n");
        output.append("}\n");
        output.append("function centerLayer(layerName){\n");
        output.append("totalWidth=getWindowWidth();\n");
        output.append("if (ie) {\n");
        output.append("eval('lyrWidth = document.all[\"'+ layerName +'\"].offsetWidth');\n");
        output.append("eval('document.all[\"'+ layerName +'\"].style.left=Math.round((totalWidth-lyrWidth)/2)');\n");
        output.append("}\n");
        output.append("else if (ns) {\n");
        output.append("eval('lyrWidth = document[\"'+ layerName +'\"].clip.width');\n");
        output.append("eval('document[\"'+ layerName +'\"].left=Math.round((totalWidth-lyrWidth)/2)');\n");
        output.append("}\n");
        output.append("}\n");
        output.append("function justifyLayers(lyr1,lyr2) {\n");
        output.append("if (ie) {\n");
        output.append("eval('y1 = document.all[\"'+ lyr1 +'\"].offsetTop');\n");
        output.append("eval('lyrHeight = document.all[\"'+ lyr1 +'\"].offsetHeight');\n");
        output.append("y2 = y1 + lyrHeight;\n");
        output.append("eval('document.all[\"'+ lyr2 +'\"].style.top = y2-22');\n");
        output.append("}\n");
        output.append("else if (ns) {\n");
        output.append("eval('y1 = document[\"'+ lyr1 +'\"].top');\n");
        output.append("eval('lyrHeight = document[\"'+ lyr1 +'\"].clip.height');\n");
        output.append("y2 = y1 + lyrHeight; \n");
        output.append("eval('document[\"'+ lyr2 +'\"].top = y2-22');\n");
        output.append("}\n");
        output.append("}\n");
        output.append("function chInputTxt(formName,field,txt,layerName)\n");
        output.append("{\n");
        output.append("if (ie || layerName==null)\n");
        output.append("eval('document.' + formName +'.'+ field + '.value=\"'+ txt +'\";');\n");
        output.append("else if (ns && layerName!=null) \n");
        output.append("eval('document[\"'+layerName+'\"].document.forms[\"'+formName+'\"].'+ field + '.value=\"'+ txt +'\";');\n");
        output.append("}\n");
        output.append("function chButtonTxt(formName,field,txt1,txt2,layerName)\n");
        output.append("{\n");
        output.append("if (shown)\n");
        output.append("chInputTxt(formName,field,txt2,layerName);\n");
        output.append("else\n");
        output.append("chInputTxt(formName,field,txt1,layerName);\n");
        output.append("}\n");
        output.append("function newObj(layerName,visibility){\n");
        output.append("eval('oCont=new makeObj(\"'+ layerName +'\")');\n");
        output.append("eval('oCont.css.visibility=\"'+ visibility +'\"');\n");
        output.append("eval('centerLayer(\"'+ layerName +'\")');\n");
        output.append("}\n");
        output.append("function showlyr(welche)\n");
        output.append("{\n");
        output.append("eval(layerzeigen_01+welche+layerzeigen_02);\n");
        output.append("}\n");
        output.append("function hidelyr(welche)\n");
        output.append("{\n");
        output.append("eval(layerverstecken_01+welche+layerverstecken_02);\n");
        output.append("}\n");
        output.append("function toggleLayer(layerName)\n");
        output.append("{\n");
        output.append("if (shown) {\n");
        output.append("eval('hidelyr(\"'+ layerName +'\")');\n");
        output.append("shown=false;\n");
        output.append("}\n");
        output.append("else {\n");
        output.append("eval('showlyr(\"'+ layerName +'\")');\n");
        output.append("shown=true;\n");
        output.append("}\n");
        output.append("}\n");
        output.append("function resized(){\n");
        output.append("pageWidth2=bw.ns4?innerWidth:document.body.offsetWidth;\n");
        output.append("pageHeight2=bw.ns4?innerHeight:document.body.offsetHeight;\n");
        output.append("if(pageWidth!=pageWidth2 || pageHeight!=pageHeight2) location.reload();\n");
        output.append("}\n");
        output.append("function errorinit() {\n");
        output.append("newObj('errormain','hidden');\n");
        output.append("newObj('errordetails','hidden');\n");
        output.append("justifyLayers('errormain','errordetails');\n");
        output.append("showlyr('errormain');\n");
        output.append("pageWidth=bw.ns4?innerWidth:document.body.offsetWidth;\n");
        output.append("pageHeight=bw.ns4?innerHeight:document.body.offsetHeight;\n");
        output.append("window.onresize=resized;\n");
        output.append("}\n");
        output.append("//-->\n");
        output.append("</script>\n");
        output.append("<head>\n");
        output.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=iso-8859-1\">\n");
        output.append("<title>OpenCms-Systemfehler</title>\n");
        output.append("<style type=\"text/css\">\n");
        output.append("TD.dialogtxt\n");
        output.append("{\n");
        output.append("BACKGROUND-COLOR: #c0c0c0;\n");
        output.append("COLOR: #000000;\n");
        output.append("FONT-FAMILY: MS sans serif,arial,helvetica,sans-serif;\n");
        output.append("FONT-SIZE: 8px;\n");
        output.append("FONT-WEIGHT: normal\n");
        output.append("}\n");
        output.append("TD.head\n");
        output.append("{\n");
        output.append("BACKGROUND-COLOR: #000066;\n");
        output.append("COLOR: white;\n");
        output.append("FONT-FAMILY: MS Sans Serif, Arial, helevitca, sans-serif;\n");
        output.append("FONT-SIZE: 8px;\n");
        output.append("FONT-WEIGHT: bold\n");
        output.append("}\n");
        output.append("TD.leerzeile\n");
        output.append("{\n");
        output.append("BACKGROUND-COLOR: #c0c0c0;\n");
        output.append("HEIGHT: 3px;\n");
        output.append("PADDING-BOTTOM: 0px;\n");
        output.append("PADDING-LEFT: 10px;\n");
        output.append("PADDING-RIGHT: 10px\n");
        output.append("}\n");
        output.append("TD.formular\n");
        output.append("{\n");
        output.append("BACKGROUND-COLOR: #c0c0c0;\n");
        output.append("COLOR: black;\n");
        output.append("FONT-FAMILY: MS Sans Serif, Arial, helvetica, sans-serif;\n");
        output.append("FONT-SIZE: 8px;\n");
        output.append("FONT-WEIGHT: bold\n");
        output.append("}\n");
        output.append("INPUT.button\n");
        output.append("{\n");
        output.append("COLOR: black;\n");
        output.append("FONT-FAMILY: MS Sans Serif, Arial, helvetica, sans-serif;\n");
        output.append("FONT-SIZE: 8px;\n");
        output.append("FONT-WEIGHT: normal;\n");
        output.append("WIDTH: 100px;\n");
        output.append("}\n");
        output.append("TEXTAREA.textarea\n");
        output.append("{\n");
        output.append("COLOR: #000000;\n");
        output.append("FONT-FAMILY: MS Sans Serif, Arial, helvetica, sans-serif;\n");
        output.append("FONT-SIZE: 8px;\n");
        output.append("WIDTH: 300px\n");
        output.append("}\n");
        output.append("#errormain{position:absolute; top:78; left:0; width:350; visibility:hidden; z-index:10; }\n");
        output.append("#errordetails{position:absolute; top:78; left:0; width:350; visibility:hidden; z-index:100}\n");
        output.append("</style>\n");
        output.append("</HEAD>\n");
        output.append("<BODY onLoad=\"errorinit();\" bgcolor=\"#ffffff\" marginwidth = 0 marginheight = 0 topmargin=0 leftmargin=0>\n");
        output.append("<div id=\"errormain\">\n");
        output.append("<!-- Tabellenaufbau f?r Dialog mit OK und Abbrechen-->\n");
        output.append("<form id=ERROR name=ERROR>\n");
        output.append("<table cellspacing=0 cellpadding=0 border=2 width=350>\n");
        output.append("<tr><td class=dialogtxt>\n");
        output.append("<table cellspacing=0 cellpadding=5 border=0 width=100% height=100%>\n");
        output.append("<!-- Beginn Tabellenkopf blau-->\n");
        output.append("<tr> \n");
        output.append("<td colspan=2 class=\"head\">Systemfehler</td>\n");
        output.append("</tr>\n");
        output.append("<!-- Ende Tabellenkopf blau-->\n");
        output.append("<!-- Beginn Leerzeile-->\n");
        output.append("<tr> <td colspan=2 class=\"leerzeile\">&nbsp;</td></tr>\n");
        output.append("<!-- Ende Leerzeile-->\n");
        output.append("<tr> \n");
        output.append("<td class=\"dialogtxt\" rowspan=3 valign=top><IMG border=0 src=\"" + cms.getRequestContext().getRequest().getWebAppUrl() + "/pics/system/ic_caution.gif\" width=32 height=32></td>\n");
        output.append("<td class=dialogtxt>Ein Systemfehler ist aufgetreten.</td>\n");
        output.append("</tr>    \n");
        output.append("<tr><td class=dialogtxt>F&uuml;r weitere Informationen klicken Sie auf \"Details anzeigen\" oder kontaktieren Sie Ihren Systemadministrator.</td></tr>\n");
        output.append("<!-- Beginn Leerzeile-->\n");
        output.append("<tr> <td class=\"leerzeile\">&nbsp;</td></tr>\n");
        output.append("<!-- Ende Leerzeile-->\n");
        output.append("<!-- Beginn Buttonleisete-->\n");
        output.append("<tr><td class=dialogtxt colspan=2>\n");
        output.append("<table cellspacing=0 cellpadding=5 width=100%>\n");
        output.append("<tr>\n");
        output.append("<td class=formular align=center><INPUT class=button width = 100 type=\"button\" value=\"OK\" id=OK name=OK onClick=\"history.back();\"></td>\n");
        output.append("<td class=formular align=center><INPUT class=button width = 100 type=\"button\" value=\"Details anzeigen\" id=details name=details onClick=\"toggleLayer('errordetails');chButtonTxt('ERROR','details','Details anzeigen','Details verbergen','errormain')\"></td>\n");
        output.append("</tr>\n");
        output.append("</table>\n");
        output.append("</td>\n");
        output.append("</tr>\n");
        output.append("</table>\n");
        output.append("<!-- Ende Buttonleisete-->   \n");
        output.append("</td></tr>\n");
        output.append("</table>\n");
        output.append("</form>\n");
        output.append("</div>\n");
        output.append("<div id=\"errordetails\">\n");
        output.append("<form id=DETAILS name=DETAILS>\n");
        output.append("<table cellspacing=0 cellpadding=0 border=2 width=350>\n");
        output.append("<tr><td class=dialogtxt>\n");
        output.append("<table cellspacing=1 cellpadding=5 border=0 width=100% height=100%>\n");
        output.append("<tr><td class=formular>Fehlermeldung:</td>\n");
        output.append("<tr><td align=center class=dialogtxt>\n");
        output.append("<textarea wrap=off cols=33 rows=6 style=\"width:330\" class=\"textarea\" onfocus=\"this.blur();\" id=EXCEPTION name=EXCEPTION>");
        output.append(Utils.getStackTrace(e));
        output.append("</textarea>\n");
        output.append("</td></tr>\n");
        output.append("</table>\n");
        output.append("</td></tr>\n");
        output.append("</table>\n");
        output.append("</form>\n");
        output.append("</div> \n");
        output.append("</BODY>\n");
        output.append("</html>\n");
        return output.toString();
    }

    /**
     * Destroys all running threads before closing the VM.
     */
    public void destroy() {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Performing Shutdown....");
        }
        try {
            m_opencms.destroy();
        }
        catch(CmsException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsServlet]" + e.toString());
            }
        }
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsServlet] Shutdown Completed");
        }
    }

    /**
     * Method invoked on each HTML GET request.
     * <p>
     * (Overloaded Servlet API method, requesting a document).
     * Reads the URI received from the client and invokes the appropiate action.
     *
     * @param req   The clints request.
     * @param res   The servlets response.
     * @exception ServletException Thrown if request fails.
     * @exception IOException Thrown if user autherization fails.
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
        // start time of this request
        if(req.getRequestURI().indexOf("system/workplace/action/login.html") > 0) {
            HttpSession session = req.getSession(false);
            if(session != null) {
                session.invalidate();
            }
        }
        CmsObject cms = null;
        CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req);
        CmsResponseHttpServlet cmsRes = new CmsResponseHttpServlet(req, res, m_clusterurl);
        try {
            cms = initUser(cmsReq, cmsRes);
            if( !checkRelocation(cms) ) {
                // no redirect was done - deliver the ressource normally
                CmsFile file = m_opencms.initResource(cms);
                if(file != null) {

                    // If the CmsFile object is null, the resource could not be found.
                    // Stop processing in this case to avoid NullPointerExceptions
                    m_opencms.setResponse(cms, file);
                    m_opencms.showResource(cms, file);
                    updateUser(cms, cmsReq, cmsRes);
                }
            }
        }
        catch(CmsException e) {
            errorHandling(cms, cmsReq, cmsRes, e);
        }
    }

    /**
     * Method invoked on each HTML POST request.
     * <p>
     * (Overloaded Servlet API method, posting a document)
     * The OpenCmsMultipartRequest is invoked to upload a new document into OpenCms.
     *
     * @param req   The clints request.
     * @param res   The servlets response.
     * @exception ServletException Thrown if request fails.
     * @exception IOException Thrown if user autherization fails.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {

        //Check for content type "form/multipart" and decode it
        String type = req.getHeader("content-type");
        CmsObject cms = null;
        if((type != null) && type.startsWith("multipart/form-data")) {


        //  req = new CmsMultipartRequest(req);
        }
        CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req);
        CmsResponseHttpServlet cmsRes = new CmsResponseHttpServlet(req, res, m_clusterurl);
        try {
            cms = initUser(cmsReq, cmsRes);
            if( !checkRelocation(cms) ) {
                // no redirect was done - deliver the ressource normally
                CmsFile file = m_opencms.initResource(cms);
                if(file != null) {

                    // If the CmsFile object is null, the resource could not be found.
                    // Stop processing in this case to avoid NullPointerExceptions
                    m_opencms.setResponse(cms, file);
                    m_opencms.showResource(cms, file);
                    updateUser(cms, cmsReq, cmsRes);
                }
            }
        }
        catch(CmsException e) {
            errorHandling(cms, cmsReq, cmsRes, e);
        }
    }

    /**
     * This method performs the error handling for the OpenCms.
     * All CmsExetions throns in the OpenCms are forwared to this method and are
     * processed here.
     *
     * @param cms The CmsObject
     * @param cmsReq   The clints request.
     * @param cmsRes   The servlets response.
     * @param e The CmsException to be processed.
     */
    private void errorHandling(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes, CmsException e) {
        int errorType = e.getType();
        HttpServletRequest req = (HttpServletRequest)cmsReq.getOriginalRequest();
        HttpServletResponse res = (HttpServletResponse)cmsRes.getOriginalResponse();
        boolean canWrite = ((!cmsRes.isRedirected()) && (!cmsRes.isOutputWritten()));
        try {
            switch(errorType) {

              // access denied error - display login dialog
              case CmsException.C_ACCESS_DENIED:
                  if(canWrite) {
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Access denied. "+e.getStackTrace());
                    }
                    requestAuthorization(req, res);
                  }

                  break;

              // file not found - display 404 error.
              case CmsException.C_NOT_FOUND:
                  if(canWrite) {
                    res.setContentType("text/HTML");

                    //res.getWriter().print(createErrorBox(e));
                    res.sendError(res.SC_NOT_FOUND);
                  }
                  break;

              case CmsException.C_SERVICE_UNAVAILABLE:
                  if(canWrite) {
                    res.sendError(res.SC_SERVICE_UNAVAILABLE, e.toString());
                  }
                  break;

              default:
                  if(canWrite) {
                    // send errorbox, but only if the request was not redirected
                    res.setContentType("text/HTML");

                    // set some HTTP headers preventing proxy servers from caching the error box
                    res.setHeader("Cache-Control", "no-cache");
                    res.setHeader("Pragma", "no-cache");
                    res.getWriter().print(createErrorBox(e, cms));
                  }
            //res.sendError(res.SC_INTERNAL_SERVER_ERROR);
            }
        }
        catch(IOException ex) {

        }
    }

    /**
     * Initialization of the OpenCms servlet.
     * Used instead of a constructor (Overloaded Servlet API method)
     * <p>
     * The connection information for the property database is read from the configuration
     * file and all resource brokers are initialized via the initalizer.
     *
     * @param config Configuration of OpenCms.
     * @exception ServletException Thrown when sevlet initalization fails.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // Collect the configurations
        try {
            ExtendedProperties p = new ExtendedProperties(CmsBase.getPropertiesPath(true));

            // Change path to log file, if given path is not absolute
            String logFile = (String)p.get("log.file");
            if(logFile != null) {
                p.put("log.file", CmsBase.getAbsolutePath(logFile));
            }

            // m_configurations = new Configurations(new ExtendedProperties(CmsBase.getPropertiesPath(true)));
            m_configurations = new Configurations(p);
        }
        catch(Exception e) {
            throw new ServletException(e.getMessage() + ".  Properties file is: " + CmsBase.getBasePath() + "opencms.properties");
        }

        // Initialize the logging
        A_OpenCms.initializeServletLogging(m_configurations);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] logging started");
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] Server Info: " + config.getServletContext().getServerInfo());
            String jdkinfo = System.getProperty("java.vm.name") + " ";
            jdkinfo += System.getProperty("java.vm.version") + " ";
            jdkinfo += System.getProperty("java.vm.info") + " ";
            jdkinfo += System.getProperty("java.vm.vendor") + " ";
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] JDK Info: " + jdkinfo);

            String osinfo = System.getProperty("os.name") + " ";
            osinfo += System.getProperty("os.version") + " ";
            osinfo += System.getProperty("os.arch") + " ";
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] OS Info: " + osinfo);
        }

        // initialize the redirect information
        int count = 0;
        String redirect;
        String redirectlocation;
        while((redirect = (String)m_configurations.getString(C_PROPERTY_REDIRECT + "." + count)) != null) {
            redirectlocation = (String)m_configurations.getString(C_PROPERTY_REDIRECTLOCATION + "." + count);
            m_redirect.addElement(redirect);
            m_redirectlocation.addElement(redirectlocation);
            count++;
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] redirect-rule: " + redirect + " -> " + redirectlocation);
            }
        }
        m_clusterurl = (String)m_configurations.getString(C_CLUSTERURL, "");
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] Clusterurl: " + m_clusterurl);
        }
        try {

            // invoke the OpenCms
            m_opencms = new OpenCms(m_configurations);
        }
        catch(Exception exc) {
            throw new ServletException(Utils.getStackTrace(exc));
        }

        //initalize the session storage
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing session storage");
        }
        m_sessionStorage = new CmsCoreSession();
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing... DONE");
        }

        // create the cms-object for the class-loader to read classes from the vfs
        CmsObject cms = new CmsObject();
        try {
            // m_sessionStorage.getClass().
            m_opencms.initUser(cms, null, null, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE_ID, m_sessionStorage);
        } catch (CmsException exc) {
            throw new ServletException("Error while initializing the cms-object for the classloader", exc);
        }
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing CmsClassLoader ");
        }
        // get the repositories for the classloader
        Vector repositories = new Vector();
        String[] repositoriesFromConfigFile = null;
        String[] repositoriesFromRegistry = null;

        // add repositories from the configuration file
        repositoriesFromConfigFile = cms.getConfigurations().getStringArray("repositories");
        for(int i = 0;i < repositoriesFromConfigFile.length;i++) {
            repositories.addElement(repositoriesFromConfigFile[i]);
        }

        // add the repositories from the registry, if it is available
        I_CmsRegistry reg = null;
        try{
            reg = cms.getRegistry();
        }catch(CmsException e){
             throw new ServletException(e.getMessage());
        }
        CmsClassLoader loader = (CmsClassLoader) (getClass().getClassLoader());
        if(reg != null) {
            repositoriesFromRegistry = reg.getRepositories();
            for(int i = 0;i < repositoriesFromRegistry.length;i++) {
                try {
                    cms.readFileHeader(repositoriesFromRegistry[i]);
                    //repositories.addElement(repositoriesFromRegistry[i]);
                    loader.addRepository(repositoriesFromRegistry[i], CmsClassLoader.C_REPOSITORY_VIRTUAL_FS);
                }
                catch(CmsException e) {
                // this repository was not found, do do not add it to the repository list
                // no exception handling is nescessary here.
                }
            }
        }
        // give the guest-loggedin cms-object and the repositories to the classloader
        // CmsClassLoader loader = new CmsClassLoader();
        //CmsClassLoader loader = (CmsClassLoader) (getClass().getClassLoader());
        //loader.init(cms, repositories);
        loader.init(cms);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing CmsClassLoader... DONE");
        }
    }

    /**
     * This method handled the user authentification for each request sent to the
     * OpenCms. <p>
     *
     * User authentification is done in three steps:
     * <ul>
     * <li> Session Authentification: OpenCms stores all active sessions of authentificated
     * users in an internal storage. During the session authetification phase, it is checked
     * if the session of the active user is stored there. </li>
     * <li> HTTP Autheification: If session authentification fails, it is checked if the current
     * user has loged in using HTTP authentification. If this check is positive, the user account is
     * checked. </li>
     * <li> Default user: When both authentification methods fail, the current user is
     * set to the default (guest) user. </li>
     * </ul>
     *
     * @param req   The clints request.
     * @param res   The servlets response.
     * @return The CmsObject
     * @exception IOException Thrown if user autherization fails.
     */
    private CmsObject initUser(I_CmsRequest cmsReq, I_CmsResponse cmsRes) throws IOException {
        HttpSession session;
        String user = null;
        String group = null;
        Integer project = null;
        String loginParameter;

        // get the original ServletRequest and response
        HttpServletRequest req = (HttpServletRequest)cmsReq.getOriginalRequest();
        HttpServletResponse res = (HttpServletResponse)cmsRes.getOriginalResponse();
        CmsObject cms = new CmsObject();

        //set up the default Cms object
        try {
            m_opencms.initUser(cms, cmsReq, cmsRes, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE_ID, m_sessionStorage);

            // check if a parameter "opencms=login" was included in the request.
            // this is used to force the HTTP-Authentification to appear.
            loginParameter = cmsReq.getParameter("opencms");
            if(loginParameter != null) {
                // do only show the authentication box if user is not already
                // authenticated.
                if(req.getHeader("Authorization") == null) {
                    if(loginParameter.equals("login")) {
                        requestAuthorization(req, res);
                    }
                }
            }

            // check for the clearcache parameter
            loginParameter = cmsReq.getParameter("_clearcache");
            if(loginParameter != null) {
                cms.clearcache();
            }

            // get the actual session
            session = req.getSession(false);

            // there is no session
            if((session == null)) {
                // was there an old session-id?
                String oldSessionId = req.getRequestedSessionId();
                if(oldSessionId != null) {

                    // yes - try to load that session
                    Hashtable sessionData = null;
                    try {
                        sessionData = m_opencms.restoreSession(oldSessionId);
                    }
                    catch(CmsException exc) {
                        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[OpenCmsServlet] cannot restore session: " + com.opencms.util.Utils.getStackTrace(exc));
                        }
                    }

                    // can the session be restored?
                    if(sessionData != null) {

                        // create a new session first
                        session = req.getSession(true);
                        m_sessionStorage.putUser(session.getId(), sessionData);

                        // restore the session-data
                        session.setAttribute(C_SESSION_DATA, sessionData.get(C_SESSION_DATA));
                    }
                }
            }

            // there was a session returned, now check if this user is already authorized
            if(session != null) {
                // get the username
                user = m_sessionStorage.getUserName(session.getId());
                //check if a user was returned, i.e. the user is authenticated
                if(user != null) {
                    group = m_sessionStorage.getCurrentGroup(session.getId());
                    project = m_sessionStorage.getCurrentProject(session.getId());
                    m_opencms.initUser(cms, cmsReq, cmsRes, user, group, project.intValue(), m_sessionStorage);
                }
            }
            else {
                // there was either no session returned or this session was not
                // found in the CmsCoreSession storage
                String auth = req.getHeader("Authorization");

                // User is authenticated, check password
                if(auth != null) {

                    // only do basic authentification
                    if(auth.toUpperCase().startsWith("BASIC ")) {

                        // Get encoded user and password, following after "BASIC "
                        String userpassEncoded = auth.substring(6);

                        // Decode it, using any base 64 decoder
                        sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
                        String userstr = new String(dec.decodeBuffer(userpassEncoded));
                        String username = null;
                        String password = null;
                        StringTokenizer st = new StringTokenizer(userstr, ":");
                        if(st.hasMoreTokens()) {
                            username = st.nextToken();
                        }
                        if(st.hasMoreTokens()) {
                            password = st.nextToken();
                        }
                        // autheification in the DB
                        try {
                            try {
                                // try to login as a user first ...
                                user = cms.loginUser(username, password);
                            } catch(CmsException exc) {
                                // login as user failed, try as webuser ...
                                user = cms.loginWebUser(username, password);
                            }
                            // authentification was successful create a session
                            session = req.getSession(true);
                            OpenCmsServletNotify notify = new OpenCmsServletNotify(session.getId(), m_sessionStorage);
                            session.setAttribute("NOTIFY", notify);
                        }
                        catch(CmsException e) {
                            if(e.getType() == CmsException.C_NO_ACCESS) {

                                // authentification failed, so display a login screen
                                requestAuthorization(req, res);

                            }
                            else {
                                throw e;
                            }
                        }
                    }
                }
            }
        }
        catch(CmsException e) {
            errorHandling(cms, cmsReq, cmsRes, e);
        }
        return cms;
    }

    /**
     * This method sends a request to the client to display a login form.
     * It is needed for HTTP-Authentification.
     *
     * @param req   The clints request.
     * @param res   The servlets response.
     */
    private void requestAuthorization(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("WWW-Authenticate", "BASIC realm=\"OpenCms\"");
        res.setStatus(401);
    }

    /**
     * Updated the the user data stored in the CmsCoreSession after the requested document
     * is processed.<br>
     *
     * This is nescessary if the user data (current group or project) was changed in
     * the requested document. <br>
     *
     * The user data is only updated if the user was authenticated to the system.
     *
     * @param cms The actual CmsObject.
     * @param cmsReq The clints request.
     * @param cmsRes The servlets response.
     * @return The CmsObject
     */
    private void updateUser(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes) throws IOException {
        HttpSession session = null;

        // get the original ServletRequest and response
        HttpServletRequest req = (HttpServletRequest)cmsReq.getOriginalRequest();

        //get the session if it is there
        session = req.getSession(false);

        // if the user was authenticated via sessions, update the information in the
        // sesssion stroage
        if((session != null)) {
            if(!cms.getRequestContext().currentUser().getName().equals(C_USER_GUEST)) {
                Hashtable sessionData = new Hashtable(4);
                sessionData.put(C_SESSION_USERNAME, cms.getRequestContext().currentUser().getName());
                sessionData.put(C_SESSION_CURRENTGROUP, cms.getRequestContext().currentGroup().getName());
                sessionData.put(C_SESSION_PROJECT, new Integer(cms.getRequestContext().currentProject().getId()));
                Hashtable oldData = (Hashtable)session.getAttribute(C_SESSION_DATA);
                if(oldData == null) {
                    oldData = new Hashtable();
                }
                sessionData.put(C_SESSION_DATA, oldData);

                // was there any change on current-user, current-group or current-project?
                boolean dirty = false;
                dirty = dirty || (!sessionData.get(C_SESSION_USERNAME).equals(m_sessionStorage.getUserName(session.getId())));
                dirty = dirty || (!sessionData.get(C_SESSION_CURRENTGROUP).equals(m_sessionStorage.getCurrentGroup(session.getId())));
                dirty = dirty || (!sessionData.get(C_SESSION_PROJECT).equals(m_sessionStorage.getCurrentProject(session.getId())));

                // update the user-data
                m_sessionStorage.putUser(session.getId(), sessionData);

                // was the session changed?
                if((session.getAttribute(C_SESSION_IS_DIRTY) != null) || dirty) {

                    // yes- store it to the database
                    session.removeAttribute(C_SESSION_IS_DIRTY);
                    try {
                        m_opencms.storeSession(session.getId(), sessionData);
                    }
                    catch(CmsException exc) {
                        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[OpenCmsServlet] cannot store session: " + com.opencms.util.Utils.getStackTrace(exc));
                        }
                    }
                }

                // check if the session notify is set, it is nescessary to remove the

                // session from the internal storage on its destruction.
                OpenCmsServletNotify notify = null;
                Object sessionValue = session.getAttribute("NOTIFY");
                if(sessionValue instanceof OpenCmsServletNotify) {
                    notify = (OpenCmsServletNotify)sessionValue;
                    if(notify == null) {
                        notify = new OpenCmsServletNotify(session.getId(), m_sessionStorage);
                        session.setAttribute("NOTIFY", notify);
                    }
                }
                else {
                    notify = new OpenCmsServletNotify(session.getId(), m_sessionStorage);
                    session.setAttribute("NOTIFY", notify);
                }
            }
        }
    }
}
