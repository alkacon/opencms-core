/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkProcessor.java,v $
 * Date   : $Date: 2004/01/20 09:54:55 $
 * Version: $Revision: 1.7 $
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

import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;

import com.opencms.file.CmsObject;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.StringNode;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.scanners.ImageScanner;
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.3
 */
public class CmsLinkProcessor extends NodeVisitor {
  
    /** Processing modes */
    private static final int C_REPLACE_LINKS = 0;
    private static final int C_PROCESS_LINKS = 1;
    
    /** Current processing mode */
    private int m_mode;
    
    /** The parser */
    private Parser m_parser;
    
    /** The link table used for link macro replacements */
    private CmsLinkTable m_linkTable;
    
    /** The processed content */
    private StringBuffer m_result;

    /** The current cms instance */
    private CmsObject m_cms;
    
    /** The relative path for relative links, if not set, relative links are treated as external links */
    private String m_relativePath;
    
    /** Indicates if links should be generated for editing purposes */
    private boolean m_processEditorLinks;
    
    /**
     * Creates a new CmsLinkProcessor.<p>
     * 
     * @param linkTable the link table to use
     */
    public CmsLinkProcessor (CmsLinkTable linkTable) {
        super(true, true);
        
        m_linkTable = linkTable;
        m_parser = new Parser();
        m_parser.addScanner(new LinkScanner());
        m_parser.addScanner(new ImageScanner(ImageTag.IMAGE_TAG_FILTER));
    }
    
    /**
     * Starts link processing for the given content in replacement mode.<p>
     * Links are replaced by macros.
     * 
     * @param cms the cms object
     * @param content the content to process
     * @param relativePath additional path for links with relative path
     * @return the processed content with replaced links
     * 
     * @throws ParserException if something goes wrong
     */
    public String replaceLinks(CmsObject cms, String content, String relativePath) throws ParserException {
        Lexer lexer = new Lexer(content);
        
        m_relativePath = relativePath;
        m_mode = C_REPLACE_LINKS;
        m_cms = cms; 
        
        m_result = new StringBuffer();
        m_parser.setLexer(lexer);
        m_parser.visitAllNodesWith(this);
        
        return m_result.toString();
    }
    
    /**
     * Starts link processing for the given content in processing mode.<p>
     * Macros are replaced by links.
     * 
     * @param cms the cms object
     * @param content the content to process
     * @param processEditorLinks flag to process links for editing purposes
     * @return the processed content with replaced macros
     * 
     * @throws ParserException if something goes wrong
     */
    public String processLinks(CmsObject cms, String content, boolean processEditorLinks) throws ParserException {
        Lexer lexer = new Lexer(content);
        
        m_processEditorLinks = processEditorLinks;
        m_mode = C_PROCESS_LINKS;
        m_cms = cms;
        
        m_result = new StringBuffer();
        m_parser.setLexer(lexer);
        m_parser.visitAllNodesWith(this);
        
        return m_result.toString();        
    }
    
    /**
     * Visitor method to process a single link.<p>
     * 
     * @param linkTag the tag to process
     * 
     * @see org.htmlparser.visitors.NodeVisitor#visitLinkTag(org.htmlparser.tags.LinkTag)
     */
    public void visitLinkTag(LinkTag linkTag) {
        switch (m_mode) {
            case C_REPLACE_LINKS:

                String targetUri = linkTag.extractLink(); 
                if (!"".equals(targetUri)) {
                
                    String internalUri = CmsLinkManager.getSitePath(m_cms, m_relativePath, targetUri);
                    if (internalUri != null) {
                        linkTag.setLink(replaceLink(m_linkTable.addLink(linkTag.getTagName(), internalUri, true)));
                    } else {
                        linkTag.setLink(replaceLink(m_linkTable.addLink(linkTag.getTagName(), targetUri, false)));
                    }
                }
                
                break;
                
            case C_PROCESS_LINKS:
                
                linkTag.setLink(processLink(m_linkTable.getLink(getLinkName(linkTag.getLink()))));
                break;           
                
            default:
                // noop
                break;
        }
    }

    
    /**
     * Visitor method to process an image tag.<p>
     * 
     * @param imageTag the tag to process
     * 
     * @see org.htmlparser.visitors.NodeVisitor#visitImageTag(org.htmlparser.tags.ImageTag)
     */
    public void visitImageTag(ImageTag imageTag) {
              
        switch (m_mode) {
            case C_REPLACE_LINKS:

                String targetUri = imageTag.getImageURL();   
                String internalUri = CmsLinkManager.getSitePath(m_cms, m_relativePath, targetUri);
                
                if (internalUri != null) {
                    imageTag.setImageURL(replaceLink(m_linkTable.addLink(imageTag.getTagName(), internalUri, true)));
                } else {
                    imageTag.setImageURL(replaceLink(m_linkTable.addLink(imageTag.getTagName(), targetUri, false)));
                }
                break;
                
            case C_PROCESS_LINKS:
                
                imageTag.setImageURL(processLink(m_linkTable.getLink(getLinkName(imageTag.getImageURL()))));
                break;
                
            default:
                // noop
                break;
        }
    }

    /**
     * Visitor method to process a string node.<p>
     * 
     * @param stringNode the string node to process
     * 
     * @see org.htmlparser.visitors.NodeVisitor#visitStringNode(org.htmlparser.StringNode)
     */
    public void visitStringNode(StringNode stringNode) {
        // process only those nodes that won't be processed by an end tag,
        // nodes without parents or parents without an end tag, since
        // the complete processing of all children should happen before
        // we turn this node back into html text
        if (null == stringNode.getParent ()) {
            m_result.append(stringNode.toHtml());
        }
    }
    
    
    /**
     * Visitor method to process a tag (start).<p>
     * 
     * @param tag the tag to process
     * 
     * @see org.htmlparser.visitors.NodeVisitor#visitTag(org.htmlparser.tags.Tag)
     */
    public void visitTag(Tag tag) {
        // process only those nodes that won't be processed by an end tag,
        // nodes without parents or parents without an end tag, since
        // the complete processing of all children should happen before
        // we turn this node back into html text
        if (null == tag.getParent ()
            && (!(tag instanceof CompositeTag) || null == ((CompositeTag)tag).getEndTag ())) {
            m_result.append(tag.toHtml());
        }
    }

    /**
     * Visitor method to process a tag (end).<p>
     * 
     * @param tag the tag to process
     * 
     * @see org.htmlparser.visitors.NodeVisitor#visitEndTag(org.htmlparser.tags.Tag)
     */
    public void visitEndTag(Tag tag) {
        Node parent;
        
        parent = tag.getParent ();
        if (null == parent) {
            m_result.append(tag.toHtml());
        } else if (parent instanceof CompositeTag) {
            // write the parent and its children only when the end tag is reached
            if (tag == ((CompositeTag)parent).getEndTag()) {
                m_result.append(parent.toHtml());
            }
        } else {
            m_result.append(parent.toHtml());
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
    
    /**
     * Returns the processed link of a given link.<p>
     * 
     * @param link the link
     * @return processed link
     */
    private String processLink(CmsLink link) {

        if (link.isInternal()) {

            CmsSite site = null;
            
            // we are in the opencms root site but not in edit mode - use link as stored
            if (!m_processEditorLinks && "".equals(m_cms.getRequestContext().getSiteRoot())) {
                return OpenCms.getLinkManager().substituteLink(m_cms, link.getUri());    
            }

            // otherwise get the desired site root from the stored link
            // - if there is no site root, we have a system link (or the site was deleted),
            // return the link prefixed with the opencms context
            String siteRoot = link.getSiteRoot();
            if (siteRoot == null) {
                return OpenCms.getLinkManager().substituteLink(m_cms, link.getUri());
            } else {
                site = CmsSiteManager.getSite(siteRoot);
            }

            // if we are in the desired site, relative links are generated
            // if the link is processed for editing purposes, relative links are generated, too
            // otherwise, links are generated as absolute links
            if (m_cms.getRequestContext().getSiteRoot().equals(siteRoot) || m_processEditorLinks) {
                return OpenCms.getLinkManager().substituteLink(m_cms, link.getVfsUri());
            } else {
                return site.getUrl() + OpenCms.getLinkManager().substituteLink(m_cms, link.getVfsUri());
            }
        } else {
            
            // don't touch external links
            return link.getUri();
        }
    }
      
    /**
     * Internal method to create a macro name ${name}.<p>
     * 
     * @param name the name of the macro
     * 
     * @return the macro string
     */
    private String newMacro (String name) {        
        return "${" + name + "}";
    }
    
    /**
     * Internal method to get the name of a macro string.<p>
     * 
     * @param macro the macro string
     * 
     * @return the name of the macro
     */
    private String getLinkName (String macro) {        
        return macro.substring(2, macro.length()-1);
    }
}
