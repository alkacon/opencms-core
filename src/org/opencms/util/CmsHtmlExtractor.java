/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsHtmlExtractor.java,v $
 * Date   : $Date: 2005/03/31 10:32:12 $
 * Version: $Revision: 1.3 $
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
// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Derrick Oswald
//
// Revision Control Information
//
// $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsHtmlExtractor.java,v $
// $Author: a.kandzior $
// $Date: 2005/03/31 10:32:12 $
// $Revision: 1.3 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package org.opencms.util;

import org.opencms.staticexport.CmsLinkProcessor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

/**
 * Extracts plain text from HTML.<p>
 * 
 * @version $Revision: 1.3 $ 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * Created on December 23, 2002, 5:01 PM
 */
public final class CmsHtmlExtractor {

    /**
     * Hides the public constructor.<p>
     */
    private CmsHtmlExtractor() {
        // hides the public constructor
    }
    
    /**
     * Extract the text from a HTML page.<p>
     *
     * @param content the html content
     * @param encoding the encoding of the content
     *
     * @return the extracted text from the page
     * @throws ParserException if the parsing of the HTML failed
     * @throws UnsupportedEncodingException if the given encoding is not supported
     */
    public static String extractText(String content, String encoding) throws ParserException, UnsupportedEncodingException  {

        // we must make sure that the content passed to the parser always is 
        // a "valid" HTML page, i.e. is surrounded by <html><body>...</body></html> 
        // otherwise you will get strange results for some specific HTML constructs
        StringBuffer newContent = new StringBuffer(content.length() + 32);
        
        newContent.append(CmsLinkProcessor.C_HTML_START);
        newContent.append(content);
        newContent.append(CmsLinkProcessor.C_HTML_END);

        // make sure the Lexer uses the right encoding
        InputStream in = new ByteArrayInputStream(newContent.toString().getBytes(encoding));

        // use the stream based version to process the results
        return extractText(in, encoding);
    }
    
    /**
     * Extract the text from a HTML page.<p>
     *
     * @param in the html content input stream
     * @param encoding the encoding of the content
     *
     * @return the extracted text from the page
     * @throws ParserException if the parsing of the HTML failed
     * @throws UnsupportedEncodingException if the given encoding is not supported
     */
    public static String extractText(InputStream in, String encoding) throws ParserException, UnsupportedEncodingException {
        
        Parser parser = new Parser();
        Lexer lexer = new Lexer();
        Page page = new Page(in, encoding);
        lexer.setPage(page);
        parser.setLexer(lexer);
        
        StringBean stringBean = new StringBean();
        // stringBean.setParser(parser);
                       
        parser.visitAllNodesWith(stringBean);
        
        return stringBean.getStrings();      
    }
}