/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsExcelExtractor.java,v $
 * Date   : $Date: 2004/02/11 15:58:55 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the m_terms of the GNU Lesser General Public
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
package org.opencms.search.documents;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.InputStream;

/**
 * This class extracts text from MS Excel files by using the POI library.
 *
 * @author James Shipley
 */
public class CmsExcelExtractor {
        
        /**
         * Constructor
         */
        public CmsExcelExtractor() {
            // empty
        }

        /**
         * Gets the text from a MS Excel document.
         *
         * @param in The InputStream representing the MS Excel file.
         * @return the raw text
         * @throws Exception if something goes wrong
         */
        public String extractText(InputStream in) throws Exception {
            StringBuffer sb = new StringBuffer();
            HSSFWorkbook wb = new HSSFWorkbook(in, false);
            int numSheets = wb.getNumberOfSheets();

            for (int i=0; i<numSheets; ++i) {
                HSSFSheet sheet = wb.getSheetAt(i);
                for (int j=sheet.getFirstRowNum(); j<=sheet.getLastRowNum(); ++j) {
                    HSSFRow row = sheet.getRow(j);
                    if (row != null) {
                        for (int k=row.getFirstCellNum(); k<= row.getLastCellNum(); ++k) {
                            HSSFCell cell = row.getCell((short)k);
                            if (cell!=null) {
                                int type = cell.getCellType();
                                if (type==HSSFCell.CELL_TYPE_STRING) {
                                    String str = cell.getStringCellValue();
                                    str=str.trim();
                                    str=replace(str, "\n", "");
                                    sb.append(str).append(" ");
                                }
                            }
                            // We will ignore all other types - numbers, forumlas, etc.
                            // as these don't hold alot of meaning outside of their tabular context.
                            // else if(type==, CELL_TYPE_NUMERIC, CELL_TYPE_FORMULA, CELL_TYPE_BOOLEAN, CELL_TYPE_ERROR
                        } // cells
                        //sb.append("\n"); // break on each row
                    }
                } // rows
                sb.append("\n"); // break on each sheet
            } // sheets
            return sb.toString();
        } // end extractText(InputStream in) method

        /**
         * Replaces oldString with newString in the given line.<p>
         * 
         * @param line the line
         * @param oldString the old string
         * @param newString the new string
         * @return the replaced line
         */
        // This should really be made 'static' and moved into a utility class,
        // included here to simplify things
        private final String replace(String line, String oldString, String newString) {
            if (line == null) {
                return null;
            }
            int i = 0;
            if ((i = line.indexOf(oldString, i)) >= 0) {
                char[] line2 = line.toCharArray(); char[] newString2 = newString.toCharArray(); int oLength = oldString.length();
                StringBuffer buf = new StringBuffer(line2.length); buf.append(line2, 0, i).append(newString2); i += oLength;
                int j = i;
                while ((i = line.indexOf(oldString, i)) > 0) {
                    buf.append(line2, j, i - j).append(newString2); i += oLength; j = i;
                }
                buf.append(line2, j, line2.length - j); return buf.toString();
            }
            return line;
        }

}
