/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditor.java,v $
 * Date   : $Date: 2003/11/20 13:03:07 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.editor;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.workplace.CmsHelperMastertemplates;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.workplace.CmsDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Provides methods for building the file editors of OpenCms.<p> 
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1.12
 */
public abstract class CmsEditor extends CmsDialog {
    
    public static final String C_PATH_EDITORS = C_PATH_WORKPLACE + "editors/";
    
    public static final String EDITOR_SAVE = "save";
    public static final String EDITOR_EXIT = "exit";
    public static final String EDITOR_SAVEEXIT = "saveexit";
    public static final String EDITOR_CHANGE_TEMPLATE = "changetemplate";
    public static final String EDITOR_CHANGE_BODY = "changebody";
    public static final String EDITOR_SHOW = "show";
    public static final String EDITOR_PREVIEW = "preview";
    
    public static final int ACTION_SAVE = 121;
    public static final int ACTION_EXIT = 122;
    public static final int ACTION_SAVEEXIT = 123;
    public static final int ACTION_CHANGE_TEMPLATE = 124;
    public static final int ACTION_CHANGE_BODY = 125;
    public static final int ACTION_SHOW = 126;
    public static final int ACTION_PREVIEW = 127;
    
    private String m_paramEditormode;
    private String m_paramBodyelement;
    private String m_paramDirectedit;
    private String m_paramPageTitle;
    private String m_paramPageTemplate;
    private String m_paramTempFile;
    private String m_paramContent;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditor(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Returns the editor mode parameter.<p>
     *  
     * @return the editor mode parameter
     */
    public String getParamEditormode() {
        return m_paramEditormode;
    }

    /**
     * Sets the editor mode parameter.<p>
     * 
     * @param mode the editor mode parameter
     */
    public void setParamEditormode(String mode) {
        m_paramEditormode = mode;
    }

    /**
     * Returns the current body element name.<p>
     * 
     * @return the current body element name
     */
    public String getParamBodyelement() {
        return m_paramBodyelement;
    }

    /**
     * Sets the current body element name.<p>
     * 
     * @param element the current body element name
     */
    public void setParamBodyelement(String element) {
        m_paramBodyelement = element;
    }

    /**
     * Returns the direct edit flag parameter.<p>
     *  
     * @return the direct edit flag parameter
     */
    public String getParamDirectedit() {
        return m_paramDirectedit;
    }

    /**
     * Sets the direct edit flag parameter.<p>
     * 
     * @param direct the direct edit flag parameter
     */
    public void setParamDirectedit(String direct) {
        m_paramDirectedit = direct;
    }
    
    /**
     * Returns the page title.<p>
     * 
     * @return the page title
     */
    public String getParamPagetitle() {
        return m_paramPageTitle;
    }
    
    /**
     * Sets the page title.<p>
     * 
     * @param pageTitle the page title
     */
    public void setParamPagetitle(String pageTitle) {
        m_paramPageTitle = pageTitle;
    }
    
    /**
     * Returns the page template.<p>
     * 
     * @return the page template
     */
    public String getParamPagetemplate() {
        return m_paramPageTemplate;
    }
    
    /**
     * Sets the page template.<p>
     * 
     * @param pageTemplate the page template
     */
    public void setParamPagetemplate(String pageTemplate) {
        m_paramPageTemplate = pageTemplate;
    }
    
    /**
     * Returns the name of the temporary file.<p>
     * 
     * @return the name of the temporary file
     */
    public String getParamTempfile() {
        return m_paramTempFile;
    }
    
    /**
     * Sets the name of the temporary file.<p>
     * 
     * @param fileName the name of the temporary file
     */
    public void setParamTempfile(String fileName) {
        m_paramTempFile = fileName;
    }
    
    /**
     * Returns the content of the editor.<p>
     * @return the content of the editor
     */
    public String getParamContent() {
        return m_paramContent;
    }
    
    /**
     * Sets the content of the editor.<p>
     * 
     * @param content the content of the editor
     */
    public void setParamContent(String content) {
        m_paramContent = content;
    }
    
    /**
     * Writes the content of the temporary file back to the original file.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void commitTempFile() throws CmsException {
        //      get the current project id
        int curProject = getSettings().getProject();
        // get the temporary file project id
        int tempProject = 0;
        try {
            tempProject = Integer.parseInt(getCms().getRegistry().getSystemValue("tempfileproject"));
        } catch (Exception e) {
            throw new CmsException("Can not read projectId of tempfileproject for creating temporary file for editing! "+e.toString());
        }
        // set current project to tempfileproject
        getCms().getRequestContext().setCurrentProject(tempProject);
        CmsFile tempFile = getCms().readFile(getParamTempfile());
        Map properties = getCms().readProperties(getParamTempfile());
        // set current project
        getCms().getRequestContext().setCurrentProject(curProject);
        CmsFile orgFile = getCms().readFile(getParamResource());
        orgFile.setContents(tempFile.getContents());
        getCms().writeFile(orgFile);
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String keyName = (String)keys.next();
            getCms().writeProperty(getParamResource(), keyName, (String)properties.get(keyName));
        }
        // don't forget to clear the cache.
        A_CmsXmlContent.clearFileCache(getCms().getRequestContext().currentProject().getName()+":"
                +  getCms().getRequestContext().addSiteRoot(getParamResource()));
    }

    
    /**
     * Creates the temporary file which is needed while working in an editor.<p>
     * 
     * @return the file name of the temporary file
     * @throws CmsException if something goes wrong
     */
    protected String createTempFile() throws CmsException {
        // read the selected file
        CmsResource file = getCms().readFileHeader(getParamResource());
        // get the current project id
        int curProject = getSettings().getProject();
        // get the temporary file project id
        int tempProject = 0;
        try {
            tempProject = Integer.parseInt(getCms().getRegistry().getSystemValue("tempfileproject"));
        } catch (Exception e) {
            throw new CmsException("Can not read projectId of tempfileproject for creating temporary file for editing! "+e.toString());
        }
        
        String temporaryFilename = CmsResource.getFolderPath(getCms().readAbsolutePath(file)) + I_CmsConstants.C_TEMP_PREFIX + file.getName();
        boolean ok = true;
        
        getCms().getRequestContext().setCurrentProject(tempProject);
        
        try {
            getCms().copyResource(getCms().readAbsolutePath(file), temporaryFilename, false, true, I_CmsConstants.C_COPY_AS_NEW);
        } catch (CmsException e) {
            if ((e.getType() == CmsException.C_FILE_EXISTS) || (e.getType() != CmsException.C_SQL_ERROR)) {
                try {
                    // try to re-use the old temporary file
                    getCms().changeLockedInProject(tempProject, temporaryFilename);
                    getCms().lockResource(temporaryFilename, true);
                } catch (Exception ex) {
                    ok = false;
                }
            } else {
                throw e;
            }
        }

        String extendedTempFile = temporaryFilename;

        int loop = 0;
        while (!ok) {
            ok = true;
            extendedTempFile = temporaryFilename + loop;
    
            try {
                getCms().copyResource(getCms().readAbsolutePath(file), extendedTempFile);
                // cms.chmod(extendedTempFile, 91);
            } catch (CmsException e) {
                if ((e.getType() != CmsException.C_FILE_EXISTS) && (e.getType() != CmsException.C_SQL_ERROR)) {
                    getCms().getRequestContext().setCurrentProject(curProject);
                    // This was not a file-exists-exception.
                    // Very bad. We should not continue here since we may run
                    // into an endless looooooooooooooooooooooooooooooooooop.
                    throw e;
                }

                // temp file could not be created
                loop++;
                ok = false;
            }
        }

        getCms().getRequestContext().setCurrentProject(curProject);
        // Oh how lucky we are! We have found a temporary file!
        temporaryFilename = extendedTempFile;

        return temporaryFilename;
    }
    
    /**
     * Gets all views available in the workplace screen.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * <code>names</code> will contain language specific view descriptions
     * and <code>values</code> will contain the correspondig URL for each
     * of these views after returning from this method.
     * <P>
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current workplace view in the vectors.
     * @throws CmsException
     */

    public String buildSelectBody() throws CmsException {
        Vector names = new Vector();
        Vector values = new Vector();
        I_CmsSession session = getCms().getRequestContext().getSession(true);
        String currentBodySection = getParamBodyelement();
//        String bodyClassName = (String)parameters.get("bodyclass");
//        String tempBodyFilename = (String)session.getValue("te_tempbodyfile");
//        Object tempObj = CmsTemplateClassManager.getClassInstance(bodyClassName);
//        CmsXmlTemplate bodyElementClassObject = (CmsXmlTemplate)tempObj;
//        CmsXmlTemplateFile bodyTemplateFile = bodyElementClassObject.getOwnTemplateFile(cms,
//                tempBodyFilename, C_BODY_ELEMENT, parameters, null);
//        Vector allBodys = bodyTemplateFile.getAllSections();
//        int loop = 0;
//        int currentBodySectionIndex = 0;
//        int numBodys = allBodys.size();
//        for(int i = 0;i < numBodys;i++) {
//            String bodyname = (String)allBodys.elementAt(i);
//            String encodedBodyname = Encoder.escapeXml(bodyname);
//            if(bodyname.equals(currentBodySection)) {
//                currentBodySectionIndex = loop;
//            }
//            values.addElement(encodedBodyname);
//            names.addElement(encodedBodyname);
//            loop++;
//        }
        return "";
    }
    
    /**
     * Builds the html for the font face select box of the WYSIWYG editor.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the font face select box
     */
    public String buildSelectFonts(String attributes) {
        List names = new ArrayList();
        for (int i=0; i<I_CmsWpConstants.C_SELECTBOX_FONTS.length; i++) {
            String value = I_CmsWpConstants.C_SELECTBOX_FONTS[i];
            names.add(value);
        }        
        return buildSelect(attributes, names, names, -1, false);
    }

    /**
     * Builds the html for the page template select box.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the page template select box
     */
    public String buildSelectTemplates(String attributes) {
        Vector names = new Vector();
        Vector values = new Vector();
        Integer selectedValue = new Integer(-1);
        try {
            selectedValue = CmsHelperMastertemplates.getTemplates(getCms(), names, values, getParamPagetemplate(), -1);
        } catch (CmsException e) {
            // ignore this exception
        }
        if (selectedValue.intValue() == -1) {
            // no template found -> use the given one
            // first clean the vectors
            names.removeAllElements();
            values.removeAllElements();
            // now add the current template
            String name = getParamPagetemplate();
            try { // to read the title of this template
                name = getCms().readProperty(name, I_CmsConstants.C_PROPERTY_TITLE);
            } catch (CmsException exc) {
                // ignore this exception - the title for this template was not readable
            }
            names.add(name);
            values.add(getParamPagetemplate());
        }
        return buildSelect(attributes, names, values, selectedValue.intValue(), false);
    }
    
    /**
     * Performs the change template action.<p>
     */
    public void actionChangeBodyElement() {
    
    }
   
    /**
     * Performs the change template action.<p>
     */
    public void actionChangeTemplate() {
        
    }
    
    /**
     * Performs the exit editor action and deletes the temporary file.<p>
     */
    public void actionExit() {
        // the "exit editor" action
    }
    
    public void actionPreview() {
        // TODO: save temporary file...
        try {
            getCms().getRequestContext().getResponse().sendCmsRedirect(getParamTempfile());
        }
        catch(IOException e) {
            // do nothing...
        }
    }
    
    /**
     * Performs the save content action.<p>
     */
    public void actionSave() {
        
    }

}
