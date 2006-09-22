/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkProcessor.java,v $
 * Date   : $Date: 2006/09/22 16:19:12 $
 * Version: $Revision: 1.47 $
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
import org.opencms.util.CmsHtmlParser;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;

/**
 * Implements the HTML parser node visitor pattern to
 * exchange all links on the page.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.47 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLinkProcessor extends CmsHtmlParser {

    /** HTML end. */
    public static final String HTML_END = "</body></html>";

    /** HTML start. */
    public static final String HTML_START = "<html><body>";

    /** Processing mode "process links". */
    private static final int PROCESS_LINKS = 1;

    /** Processing mode "replace links". */
    private static final int REPLACE_LINKS = 0;

    /** The current users cms instance, containing the users permission and site root context. */
    private CmsObject m_cms;

    /** The selected encoding to use for parsing the HTML. */
    private String m_encoding;

    /** The link table used for link macro replacements. */
    private CmsLinkTable m_linkTable;

    /** Current processing mode. */
    private int m_mode;

    /** Indicates if links should be generated for editing purposes. */
    private boolean m_processEditorLinks;

    /** The relative path for relative links, if not set, relative links are treated as external links. */
    private String m_relativePath;

    /** Another cms instance based on the current users cms instance, but with the site root set to '/'. */
    private CmsObject m_rootCms;

    /**
     * Creates a new link processor.<p>
     * 
     * @param cms the cms object
     * @param linkTable the link table to use
     * @param encoding the encoding to use for parsing the HTML content
     * @param relativePath additional path for links with relative path (only used in "replace" mode)
     */
    public CmsLinkProcessor(CmsObject cms, CmsLinkTable linkTable, String encoding, String relativePath) {

        // echo mode must be on for link processor
        super(true);

        m_cms = cms;
        if (m_cms != null) {
            try {
                m_rootCms = OpenCms.initCmsObject(cms);
                m_rootCms.getRequestContext().setSiteRoot("/");
            } catch (CmsException e) {
                // this should not happen
                m_rootCms = null;
            }
        }
        m_linkTable = linkTable;
        m_encoding = encoding;
        m_processEditorLinks = ((null != m_cms) && (null != m_cms.getRequestContext().getAttribute(
            CmsRequestContext.ATTRIBUTE_EDITOR)));
        m_relativePath = relativePath;
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
                    terminatorIndex = source.indexOf(';', i);
                    if (terminatorIndex > 0) {
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
     * Returns the link table this link processor was initialized with.<p>
     * 
     * @return the link table this link processor was initialized with
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
        return process(content, m_encoding);
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
        return process(content, m_encoding);
    }

    /**
     * Visitor method to process a tag (start).<p>
     * 
     * @param tag the tag to process
     */
    public void visitTag(Tag tag) {

        if (tag instanceof LinkTag) {
            processLinkTag((LinkTag)tag);
        } else if (tag instanceof ImageTag) {
            processImageTag((ImageTag)tag);
        }
        // append text content of the tag (may have been changed by above methods)
        super.visitTag(tag);
    }

    /**
     * Process an image tag.<p>
     * 
     * @param tag the tag to process
     */
    protected void processImageTag(ImageTag tag) {

        if (tag.getAttribute("src") != null) {

            CmsLink link;
            switch (m_mode) {

                case PROCESS_LINKS:
                    // macros are replaced with links
                    link = m_linkTable.getLink(CmsMacroResolver.stripMacro(tag.getImageURL()));
                    if (link != null) {
                        tag.setImageURL(processLink(link));
                    }
                    break;

                case REPLACE_LINKS:
                    // links are replaced with macros
                    String targetUri = tag.getImageURL();
                    if (CmsStringUtil.isNotEmpty(targetUri)) {
                        String internalUri = CmsLinkManager.getSitePath(m_cms, m_relativePath, targetUri);
                        if (internalUri != null) {
                            // this is an internal link
                            link = m_linkTable.addLink(tag.getTagName(), internalUri, true);
                        } else {
                            // this is an external link
                            link = m_linkTable.addLink(tag.getTagName(), targetUri, false);
                        }
                        tag.setImageURL(CmsMacroResolver.formatMacro(link.getName()));

                        // now ensure the image has the "alt" attribute set
                        boolean hasAltAttrib = (tag.getAttribute("alt") != null);
                        if (!hasAltAttrib) {
                            String value = null;
                            if ((internalUri != null) && (m_rootCms != null)) {
                                // internal image: try to read the alt text from the "Title" property
                                try {
                                    value = m_rootCms.readPropertyObject(
                                        internalUri,
                                        CmsPropertyDefinition.PROPERTY_TITLE,
                                        false).getValue();
                                } catch (CmsException e) {
                                    // property can't be read, ignore
                                }
                            }
                            // some editors add a "/" at the end of the tag, we must make sure to insert before that
                            Vector attrs = tag.getAttributesEx();
                            // first element is always the tag name
                            attrs.add(1, new Attribute(" "));
                            attrs.add(2, new Attribute("alt", value == null ? "" : value, '"'));
                        }
                    }
                    break;

                default: // noop
            }
        }
    }

    /**
     * Process a link tag.<p>
     * 
     * @param tag the tag to process
     */
    protected void processLinkTag(LinkTag tag) {

        if (tag.getAttribute("href") != null) {
            // href attribute is required

            CmsLink link;
            switch (m_mode) {

                case PROCESS_LINKS:
                    // macros are replaced with links
                    link = m_linkTable.getLink(CmsMacroResolver.stripMacro(tag.getLink()));
                    if (link != null) {
                        tag.setLink(escapeLink(processLink(link)));
                    }
                    break;

                case REPLACE_LINKS:
                    // links are replaced with macros
                    String targetUri = tag.extractLink();
                    if (CmsStringUtil.isNotEmpty(targetUri)) {
                        String internalUri = CmsLinkManager.getSitePath(m_cms, m_relativePath, targetUri);
                        if (internalUri != null) {
                            // this is an internal link
                            link = m_linkTable.addLink(tag.getTagName(), internalUri, true);
                        } else {
                            // this is an external link
                            link = m_linkTable.addLink(tag.getTagName(), targetUri, false);
                        }
                        tag.setLink(CmsMacroResolver.formatMacro(link.getName()));
                    }
                    break;

                default: // noop
            }
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
}