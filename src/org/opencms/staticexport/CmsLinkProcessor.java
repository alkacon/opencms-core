/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkProcessor.java,v $
 * Date   : $Date: 2004/06/29 14:38:57 $
 * Version: $Revision: 1.22 $
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
package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;

/**
 * Handles the link replacement required e.g. to process elements for XML pages.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.22 $
 * @since 5.3
 */
public class CmsLinkProcessor {
    
    /** HTML end. */
    private static final String C_HTML_END = "</body></html>";
    
    /** HTML start. */
    private static final String C_HTML_START = "<html><body>";
    
    /** Processing mode "process links". */
    private static final int C_PROCESS_LINKS = 1;
  
    /** Processing mode "replace links". */
    private static final int C_REPLACE_LINKS = 0;

    /** The current cms instance. */
    private CmsObject m_cms;
    
    /** The link table used for link macro replacements. */
    private CmsLinkTable m_linkTable;
    
    /** Current processing mode. */
    private int m_mode;
    
    /** Indicates if links should be generated for editing purposes. */
    private boolean m_processEditorLinks;
    
    /** The relative path for relative links, if not set, relative links are treated as external links. */
    private String m_relativePath;   
    
    /**
     * Creates a new CmsLinkProcessor.<p>
     * 
     * @param linkTable the link table to use
     */
    public CmsLinkProcessor (CmsLinkTable linkTable) {        
        m_linkTable = linkTable;
    }
    
    /**
     * Starts link processing for the given content in processing mode.<p>
     * 
     * Macros are replaced by links.<p>
     * 
     * @param cms the cms object
     * @param content the content to process
     * @param encoding the encoding to use
     * @param processEditorLinks flag to process links for editing purposes
     * @return the processed content with replaced macros
     * 
     * @throws ParserException if something goes wrong
     */
    public String processLinks(CmsObject cms, String content, String encoding, boolean processEditorLinks) throws ParserException {
                
        m_processEditorLinks = processEditorLinks;
        m_mode = C_PROCESS_LINKS;
        m_cms = cms;        

        String result = processContent(content, encoding);
        return result;
    }
    
    /**
     * Starts link processing for the given content in replacement mode.<p>
     * 
     * Links are replaced by macros.<p>
     * 
     * @param cms the cms object
     * @param content the content to process
     * @param encoding the encoding to use
     * @param relativePath additional path for links with relative path
     * @return the processed content with replaced links
     * 
     * @throws ParserException if something goes wrong
     */
    public String replaceLinks(CmsObject cms, String content, String encoding, String relativePath) throws ParserException {
                                
        m_relativePath = relativePath;
        m_mode = C_REPLACE_LINKS;
        m_cms = cms; 
        
        String result = processContent(content, encoding);
        return result;
    }
    
    /**
     * Process an image tag.<p>
     * 
     * @param imageTag the tag to process
     */
    protected void processImageTag(ImageTag imageTag) {             
        switch (m_mode) {
            case C_REPLACE_LINKS:
                if (imageTag.getAttribute("src") != null) {
                    String targetUri = imageTag.getImageURL();   
                    String internalUri = CmsLinkManager.getSitePath(m_cms, m_relativePath, targetUri);
                    
                    if (internalUri != null) {
                        imageTag.setImageURL(replaceLink(m_linkTable.addLink(imageTag.getTagName(), internalUri, true)));
                    } else {
                        imageTag.setImageURL(replaceLink(m_linkTable.addLink(imageTag.getTagName(), targetUri, false)));
                    }
                }
                break;
                
            case C_PROCESS_LINKS:
                if (imageTag.getAttribute("src") != null) {
                    String imageUrl = imageTag.getImageURL();
                    CmsLink link = m_linkTable.getLink(getLinkName(imageUrl));
                    if (link != null) {
                        // default case: do macro replacement from link table
                        imageTag.setImageURL(processLink(link));
                    } else {
                        // might happen if the HTML is malformed, this prevents a NPE
                        imageTag.setImageURL(imageUrl);
                    }
                }
                break;
                
            default:
                // noop
                break;
        }
    }
    
    /**
     * Process a link tag.<p>
     * 
     * @param linkTag the tag to process
     */
    protected void processLinkTag(LinkTag linkTag) {
        switch (m_mode) {
            
            case C_PROCESS_LINKS:
                if (linkTag.getAttribute("href") != null) {
                    CmsLink link = m_linkTable.getLink(getLinkName(linkTag.getLink()));
                    if (link != null) {
                        linkTag.setLink(processLink(link));
                    }
                }
                break;
            
            case C_REPLACE_LINKS:
                if (linkTag.getAttribute("href") != null) {
                    String targetUri = linkTag.extractLink(); 
                    
                    if (!"".equals(targetUri)) {                    
                        String internalUri = CmsLinkManager.getSitePath(m_cms, m_relativePath, targetUri);
                        if (internalUri != null) {
                            linkTag.setLink(replaceLink(m_linkTable.addLink(linkTag.getTagName(), internalUri, true)));
                        } else {
                            linkTag.setLink(replaceLink(m_linkTable.addLink(linkTag.getTagName(), targetUri, false)));
                        }
                    }
                }
                break;
                                
            default:
                // noop
                break;
        }
    }
    
    /**
     * Internal method to get the name of a macro string.<p>
     * 
     * @param macro the macro string
     * 
     * @return the name of the macro
     */
    private String getLinkName(String macro) {        
        return macro.substring(2, macro.length()-1);
    }
      
    /**
     * Internal method to create a macro name ${name}.<p>
     * 
     * @param name the name of the macro
     * 
     * @return the macro string
     */
    private String newMacro(String name) {
        StringBuffer result = new StringBuffer(name.length() + 4);
        result.append("${");
        result.append(name);
        result.append("}");
        return result.toString();
    }
    
    /**
     * Initialized the parser and processes the content input.<p>
     * 
     * @param content the content to process
     * @param encoding the encoding to use
     * @return the processed content with replaced links
     * 
     * @throws ParserException if something goes wrong
     */    
    private String processContent(String content, String encoding) throws ParserException  {
        // we must make sure that the content passed to the parser always is 
        // a "valid" HTML page, i.e. is surrounded by <html><body>...</body></html> 
        // otherwise you will get strange results for some specific HTML constructs
        StringBuffer newContent = new StringBuffer(content.length() + 32);
        
        newContent.append(C_HTML_START);
        newContent.append(content);
        newContent.append(C_HTML_END);
        
        // create the link visitor
        CmsLinkVisitor visitor = new CmsLinkVisitor(this);
        // create the parser and parse the input
        Parser parser = new Parser();        
        Lexer lexer = new Lexer();
        Page page;
        try {
            // make sure the Lexer uses the right encoding
            InputStream stream = new ByteArrayInputStream(newContent.toString().getBytes(encoding));
            page = new Page(stream, encoding);
        } catch (UnsupportedEncodingException e) {
            // fall back to default encoding, should not happen since all xml pages must have a valid encoding    
            throw new ParserException("Invalid encoding for HTML content parsing '" + encoding + "'");
        }
        lexer.setPage(page);
        parser.setLexer(lexer);      
        parser.visitAllNodesWith(visitor);
        // remove the addition HTML  
        String result = visitor.getHtml();
        return result.substring(C_HTML_START.length(), result.length() - C_HTML_END.length());        
    }
    
    /**
     * Returns the processed link of a given link.<p>
     * 
     * @param link the link
     * @return processed link
     */
    private String processLink(CmsLink link) {

        if (link.isInternal()) {

            // if we have a local link, leave it unchanged
            if (link.getUri().charAt(0) == '#') {
                return link.getUri();
            }
            
            // we are in the opencms root site but not in edit mode - use link as stored
            if (!m_processEditorLinks && (m_cms.getRequestContext().getSiteRoot().length() == 0)) {
                return OpenCms.getLinkManager().substituteLink(m_cms, link.getUri());    
            }

            // otherwise get the desired site root from the stored link
            // - if there is no site root, we have a system link (or the site was deleted),
            // return the link prefixed with the opencms context
            String siteRoot = link.getSiteRoot();
            if (siteRoot == null) {
                return OpenCms.getLinkManager().substituteLink(m_cms, link.getUri());
            }
            
            // check if the link has to be returned as relative or absolute link
            if (m_cms.getRequestContext().getSiteRoot().equals(siteRoot)) {
                // if we are in the desired site, relative links are generated
                return OpenCms.getLinkManager().substituteLink(m_cms, link.getVfsUri());
            } else {
                // otherwise, links are generated as absolute links
                return CmsSiteManager.getSite(siteRoot).getUrl() 
                    + OpenCms.getLinkManager().substituteLink(m_cms, link.getVfsUri());
            }
        } else {            
            // don't touch external links
            return link.getUri();
        }
    }
    
    /**
     * Returns the replacement string for a given link.<p>
     * 
     * @param link the link
     * @return the replacement
     */
    private String replaceLink(CmsLink link) { 
        return newMacro(link.getName());
    }
}
