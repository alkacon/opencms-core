/*
 * File   :
 * Date   : 
 * Version: 
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

package org.opencms.modules.htmlimport;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.workplace.CmsHelperMastertemplates;

import java.util.Vector;


/**
 * This class contains some utility methods for the HTMLImport Backoffice.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */
public class HtmlImportBackoffice {
    


  /**
   * Creates a selectbox with all available templates in the OpenCms system.<p>
   * 
   * @param cms the current CmsObject
   * @param selectedTemplate the preselcted template
   * @return HTML-Code for the selectbox, containing all templates
   */
  public static String getTemplates(CmsObject cms, String selectedTemplate) {
      // the output buffer
      StringBuffer output = new StringBuffer();
      // those two vectors store name and path of the templates
      Vector names = new Vector();
      Vector values = new Vector();
      // contains the number of the selected for the selectbox
      int selectedValue = 0;
      
      try {
          //get all available Templates. Use the old exsiting method for that
          selectedValue = CmsHelperMastertemplates.getTemplates(cms, names, values, selectedTemplate).intValue();
      } catch (CmsException e) {

        System.err.println(e);          
      }

      // now generate the HTML code for the selectbox
      output.append("<select  name=\"template\"  width=80 size=\"1\">");

      // loop through all results and build the entries
      for (int i=0; i<names.size(); i++) {
          output.append("<option ");
          if (selectedValue == i) {
              output.append("selected ");
          }
          output.append("value=\"");
          output.append((String)values.elementAt(i));
          output.append("\">");
          output.append((String)names.elementAt(i));
      }      
      output.append("</select>");
       
      return new String(output);
  }
  
  
}
