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

package org.opencms.util;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.xml.CmsXmlException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Provides utility functions for XSLT transformations.<p>
 *
 * TODO: This class is apparently customer specific and should probably be removed from the core!
 *
 * @since 6.2.1
 */
public final class CmsXsltUtil {

    /** The delimiter to end a tag. */
    public static final String TAG_END_DELIMITER = ">";

    /** The delimiter to start a tag. */
    public static final String TAG_START_DELIMITER = "<";

    /** The delimiter to separate the text. */
    public static final char TEXT_DELIMITER = '"';

    /** the delimiters, the csv data can be separated with.*/
    static final String[] DELIMITERS = {";", ",", "\t"};

    /**
     * Hides the public constructor.<p>
     */
    private CmsXsltUtil() {

        // noop
    }

    /**
     * Returns the delimiter that most often occures in the CSV content and is therefore best applicable for the CSV data .<p>
     *
     * @param csvData the comma separated values
     *
     * @return the delimiter that is best applicable for the CSV data
     */
    public static String getPreferredDelimiter(String csvData) {

        String bestMatch = "";
        int bestMatchCount = 0;
        // find for each delimiter, how often it occures in the String csvData
        for (int i = 0; i < DELIMITERS.length; i++) {
            int currentCount = csvData.split(DELIMITERS[i]).length;
            if (currentCount > bestMatchCount) {
                bestMatch = DELIMITERS[i];
                bestMatchCount = currentCount;
            }
        }
        return bestMatch;
    }

    /**
     * Changes content from CSV to xml/html.<p>
     *
     * The method does not use DOM4J, because iso-8859-1 code ist not transformed correctly.
     *
     * @param cms the cms object
     * @param xsltFile the XSLT transformation file
     * @param csvContent the csv content to transform
     * @param delimiter delimiter used to separate csv fields
     *
     * @return the transformed xml
     *
     * @throws CmsXmlException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    public static String transformCsvContent(CmsObject cms, String xsltFile, String csvContent, String delimiter)
    throws CmsException, CmsXmlException {

        String xmlContent = "";
        try {
            xmlContent = getTableHtml(csvContent, delimiter);
        } catch (IOException e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_CSV_XML_TRANSFORMATION_FAILED_0));
        }

        // if xslt file parameter is set, transform the raw html and set the css stylesheet property
        // of the converted file to that of the stylesheet
        if (xsltFile != null) {
            xmlContent = transformXmlContent(cms, xsltFile, xmlContent);
        }

        return xmlContent;
    }

    /**
     * Applies a XSLT Transformation to the content.<p>
     *
     * The method does not use DOM4J, because iso-8859-1 code ist not transformed correctly.
     *
     * @param cms the cms object
     * @param xsltFile the XSLT transformation file
     * @param xmlContent the XML content to transform
     *
     * @return the transformed xml
     *
     * @throws CmsXmlException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    public static String transformXmlContent(CmsObject cms, String xsltFile, String xmlContent)
    throws CmsException, CmsXmlException {

        // JAXP reads data
        Source xmlSource = new StreamSource(new StringReader(xmlContent));
        String xsltString = new String(cms.readFile(xsltFile).getContents());
        Source xsltSource = new StreamSource(new StringReader(xsltString));
        String result = null;

        try {
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans = transFact.newTransformer(xsltSource);

            StringWriter writer = new StringWriter();
            trans.transform(xmlSource, new StreamResult(writer));
            result = writer.toString();
        } catch (Exception exc) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_CSV_XML_TRANSFORMATION_FAILED_0));
        }

        // cut of the prefacing declaration '<?xml version="1.0" encoding="UTF-8"?>'
        if (result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
            return result.substring(38);
        } else {
            return result;
        }
    }

    /**
     * Converts a delimiter separated format string int o colgroup html fragment.<p>
     *
     * @param formatString the formatstring to convert
     * @param delimiter the delimiter the formats (l,r or c) are delimited with
     *
     * @return the resulting colgroup HTML
     */
    private static String getColGroup(String formatString, String delimiter) {

        StringBuffer colgroup = new StringBuffer(128);
        String[] formatStrings = formatString.split(delimiter);
        colgroup.append("<colgroup>");
        for (int i = 0; i < formatStrings.length; i++) {
            colgroup.append("<col align=\"");
            char align = formatStrings[i].trim().charAt(0);
            switch (align) {
                case 'l':
                    colgroup.append("left");
                    break;
                case 'c':
                    colgroup.append("center");
                    break;
                case 'r':
                    colgroup.append("right");
                    break;
                default:
                    throw new RuntimeException("invalid format option");
            }
            colgroup.append("\"/>");
        }
        return colgroup.append("</colgroup>").toString();
    }

    /**
     * Converts CSV data to XML.<p>
     *
     * @return a XML representation of the CSV data
     *
     * @param csvData the CSV data to convert
     * @param delimiter the delimiter to separate the values with
     *
     * @throws IOException if there is an IO problem
     */
    private static String getTableHtml(String csvData, String delimiter) throws IOException {

        String lineSeparator = System.getProperty("line.separator");
        int tmpindex = csvData.indexOf(lineSeparator);
        String formatString = (tmpindex >= 0) ? csvData.substring(0, tmpindex) : csvData;

        if (delimiter == null) {
            delimiter = getPreferredDelimiter(csvData);
        }

        StringBuffer xml = new StringBuffer("<table>");
        if (isFormattingInformation(formatString, delimiter)) {
            // transform formatting to HTML colgroup
            xml.append(getColGroup(formatString, delimiter));
            // cut of first line
            csvData = csvData.substring(formatString.length() + lineSeparator.length());
        }

        String line;
        BufferedReader br = new BufferedReader(new StringReader(csvData));
        while ((line = br.readLine()) != null) {
            xml.append("<tr>\n");

            // must use tokenizer with delimiters include in order to handle empty cells appropriately
            StringTokenizer t = new StringTokenizer(line, delimiter, true);
            boolean hasValue = false;
            while (t.hasMoreElements()) {
                String item = (String)t.nextElement();
                if (!hasValue) {
                    xml.append("\t<td>");
                    hasValue = true;
                }
                if (!item.equals(delimiter)) {

                    // remove enclosing delimiters
                    item = removeStringDelimiters(item);

                    // in order to allow links, lines starting and ending with tag delimiters (< ...>) remains unescaped
                    if (item.startsWith(TAG_START_DELIMITER) && item.endsWith(TAG_END_DELIMITER)) {
                        xml.append(item);
                    } else {
                        xml.append(CmsStringUtil.escapeHtml(item));
                    }
                } else {
                    xml.append("</td>\n");
                    hasValue = false;
                }
            }
            if (hasValue) {
                xml.append("</td>\n");
            } else {
                xml.append("<td></td>\n");
            }

            xml.append("</tr>\n");
        }

        return xml.append("</table>").toString();
    }

    /**
     * Tests if the given string is a <code>delimiter</code> separated list of formatting information.<p>
     *
     * @param formatString the string to check
     * @param delimiter the list separators
     *
     * @return true if the string is a <code>delimiter</code> separated list of Formatting Information
     */
    private static boolean isFormattingInformation(String formatString, String delimiter) {

        String[] formatStrings = formatString.split(delimiter);
        for (int i = 0; i < formatStrings.length; i++) {
            if (!formatStrings[i].trim().matches("[lcr]")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the string delimiters from a key (as well as any white space
     * outside the delimiters).<p>
     *
     * @param key the key (including delimiters)
     *
     * @return the key without delimiters
     */
    private static String removeStringDelimiters(String key) {

        String k = key.trim();
        if (CmsStringUtil.isNotEmpty(k)) {
            if (k.charAt(0) == TEXT_DELIMITER) {
                k = k.substring(1);
            }
            if (k.charAt(k.length() - 1) == TEXT_DELIMITER) {
                k = k.substring(0, k.length() - 1);
            }
        }
        // replace excel protected quotations marks ("") by single quotation marks
        k = CmsStringUtil.substitute(k, "\"\"", "\"");
        return k;
    }
}