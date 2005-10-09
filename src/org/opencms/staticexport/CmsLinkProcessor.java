/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkProcessor.java,v $
 * Date   : $Date: 2005/10/10 16:11:03 $
 * Version: $Revision: 1.45 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

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
 * @author Carsten Weinholz 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.45 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLinkProcessor {

    /** HTML end. */
    public static final String HTML_END = "</body></html>";

    /** HTML start. */
    public static final String HTML_START = "<html><body>";

    /** End of a macro. */
    private static final String MACRO_END = "}";

    /** Start of a macro. */
    private static final String MACRO_START = "${";

    /** Processing mode "process links". */
    private static final int PROCESS_LINKS = 1;

    /** Processing mode "replace links". */
    private static final int REPLACE_LINKS = 0;

    /** The current cms instance. */
    private CmsObject m_cms;

    /** The selected encoding. */
    private String m_encoding;

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
     * @param cms the cms object
     * @param linkTable the link table to use
     * @param encoding the encoding to use
     * @param relativePath additional path for links with relative path (only used in "replace" mode)
     */
    public CmsLinkProcessor(CmsObject cms, CmsLinkTable linkTable, String encoding, String relativePath) {

        m_cms = cms;
        m_linkTable = linkTable;
        m_encoding = encoding;
        m_processEditorLinks = ((null != m_cms) && (null != m_cms.getRequestContext().getAttribute(
            CmsRequestContext.ATTRIBUTE_EDITOR)));
        m_relativePath = relativePath;
    }

    /**
     * Returns the link table this lik processor was initialized with.<p>
     * 
     * @return the link table this lik processor was initialized with
     */
    public CmsLinkTable getLinkTable() {

        return m_linkTable;
    }

    /**
     * Starts link processing for the given content in processing mode.<p>
     * 
     * Macros are replaced by links.<p>
     * 
     * @param content the content to process
     * @return the processed content with replaced macros
     * 
     * @throws ParserException if something goes wrong
     */
    public String processLinks(String content) throws ParserException {

        m_mode = PROCESS_LINKS;

        String result = processContent(content, m_encoding);
        return result;
    }

    /**
     * Starts link processing for the given content in replacement mode.<p>
     * 
     * Links are replaced by macros.<p>
     * 
     * @param content the content to process
     * @return the processed content with replaced links
     * 
     * @throws ParserException if something goes wrong
     */
    public String replaceLinks(String content) throws ParserException {

        m_mode = REPLACE_LINKS;

        String result = processContent(content, m_encoding);
        return result;
    }

    /**
     * Process an image tag.<p>
     * 
     * @param imageTag the tag to process
     */
    protected void processImageTag(ImageTag imageTag) {

        switch (m_mode) {

            case REPLACE_LINKS:
                if (imageTag.getAttribute("src") != null) {

                    String targetUri = imageTag.getImageURL();
                    String internalUri = CmsLinkManager.getSitePath(m_cms, m_relativePath, targetUri);

                    boolean hasAltAttrib = (imageTag.getAttribute("alt") != null);
                    String title = null;

                    if (internalUri != null) {
                        imageTag.setImageURL(replaceLink(m_linkTable.addLink(imageTag.getTagName(), internalUri, true)));

                        if (!hasAltAttrib && (m_cms != null)) {
                            try {
                                title = m_cms.readPropertyObject(
                                    internalUri,
                                    CmsPropertyDefinition.PROPERTY_TITLE,
                                    false).getValue("\"\"");
                            } catch (CmsException e) {
                                title = "\"\"";
                            }

                            imageTag.setAttribute("alt", title);
                        }
                    } else {
                        imageTag.setImageURL(replaceLink(m_linkTable.addLink(imageTag.getTagName(), targetUri, false)));

                        if (!hasAltAttrib) {
                            imageTag.setAttribute("alt", "\"\"");
                        }
                    }
                }
                break;

            case PROCESS_LINKS:
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
     * Unescapes all <code>&amp;</code>, that is replaces them with a <code>&</code>.<p>
     * 
     * @param source the String to unescape
     * @return the unescaped String
     */
    public static String unescapeLink(String source) {
     
        if (source == null) {
            return null;
        }
        return CmsStringUtil.substitute(source, "&amp;", "&");
        
    }
    
    /**
     * Escapes all <code>&</code>, e.g. replaces them with a <code>&amp;</code>.<p>
     * 
     * @param source the String to escape
     * @return the escaped String
     */
    public static String escapeLink(String source) {

        if (source == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(source.length() * 2);
        int terminatorIndex;
        for (int i = 0; i < source.length(); ++i) {
            char ch = source.charAt(i);
            switch (ch) {
                case '&':
                    // don't escape already escaped &amps;
                    if ((terminatorIndex = source.indexOf(";", i)) > 0) {
                        String substr = source.substring(i + 1, terminatorIndex);
                        if ("amp".equals(substr)) {
                            result.append(ch);
                        } else {
                            result.append("&amp;");
                        }
                    } else {
                        result.append("&amp;");
                    }
                    break;
                default:
                    result.append(ch);
            }
        }
        return new String(result);
    }

    /**
     * Process a link tag.<p>
     * 
     * @param linkTag the tag to process
     */
    protected void processLinkTag(LinkTag linkTag) {

        switch (m_mode) {

            case PROCESS_LINKS:
                if (linkTag.getAttribute("href") != null) {
                    CmsLink link = m_linkTable.getLink(getLinkName(linkTag.getLink()));
                    if (link != null) {
                        linkTag.setLink(CmsLinkProcessor.escapeLink(processLink(link)));
                    }
                }
                break;

            case REPLACE_LINKS:
                if (linkTag.getAttribute("href") != null) {
                    String targetUri = linkTag.extractLink();

                    if (CmsStringUtil.isNotEmpty(targetUri)) {
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

        if (CmsStringUtil.isNotEmpty(macro)) {
            if (macro.startsWith(MACRO_START) && macro.endsWith(MACRO_END)) {
                // make sure the macro really is a macro
                return macro.substring(MACRO_START.length(), macro.length() - MACRO_END.length());
            }
        }
        return "";
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
        result.append(MACRO_START);
        result.append(name);
        result.append(MACRO_END);
        return result.toString();
    }

    /**
     * Initializes the parser and processes the content input.<p>
     * 
     * @param content the content to process
     * @param encoding the encoding to use
     * @return the processed content with replaced links
     * 
     * @throws ParserException if something goes wrong
     */
    private String processContent(String content, String encoding) throws ParserException {

        // we must make sure that the content passed to the parser always is 
        // a "valid" HTML page, i.e. is surrounded by <html><body>...</body></html> 
        // otherwise you will get strange results for some specific HTML constructs
        StringBuffer newContent = new StringBuffer(content.length() + 32);

        newContent.append(HTML_START);
        newContent.append(content);
        newContent.append(HTML_END);

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
            throw new ParserException(Messages.get().key(Messages.ERR_INVALID_ENCODING_1, encoding));
        }
        lexer.setPage(page);
        parser.setLexer(lexer);
        parser.visitAllNodesWith(visitor);
        // remove the addition HTML  
        String result = visitor.getHtml();
        if (result.length() > 0) {
            return result.substring(HTML_START.length(), result.length() - HTML_END.length());
        } else {
            return result;
        }
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
            // cms may be null for unit tests
            if ((m_cms == null) || (link.getUri().length() == 0) || (link.getUri().charAt(0) == '#')) {
                return link.getUri();
            }

            // Explanation why the "m_processEditorLinks" variable is required:
            // If the VFS is browsed in the root site, this indicates that a user has switched
            // the context to the / in the Workplace. In this case the workplace site must be 
            // the active site. If normal link processing would be used, the site root in the link
            // would be replaced with server name / port for the other sites. But if a user clicks
            // on such a link he would leave the workplace site and loose his session. 
            // A result is that the "direct edit" mode does not work since he in not longer logged in.      
            // Therefore if the user is NOT in the editor, but in the root site, the links are generated
            // without server name / port. However, if the editor is opened, the links are generated 
            // _with_ server name / port so that the source code looks identical to code
            // that would normally created when running in a regular site.

            // we are in the opencms root site but not in edit mode - use link as stored
            if (!m_processEditorLinks && (m_cms.getRequestContext().getSiteRoot().length() == 0)) {
                return OpenCms.getLinkManager().substituteLink(m_cms, link.getUri());
            }

            // otherwise get the desired site root from the stored link
            // if there is no site root, we have a /system link (or the site was deleted),
            // return the link prefixed with the opencms context
            String siteRoot = link.getSiteRoot();
            if (siteRoot == null) {
                return OpenCms.getLinkManager().substituteLink(m_cms, link.getUri());
            }

            // return the link with the server prefix, if necessary 
            return OpenCms.getLinkManager().substituteLink(m_cms, link.getVfsUri(), siteRoot);
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