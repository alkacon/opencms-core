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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsHtmlParser;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ObjectTag;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

/**
 * Implements the HTML parser node visitor pattern to
 * exchange all links on the page.<p>
 *
 * @since 6.0.0
 */
public class CmsLinkProcessor extends CmsHtmlParser {

    /** Constant for the attribute name. */
    public static final String ATTRIBUTE_HREF = "href";

    /** Constant for the attribute name. */
    public static final String ATTRIBUTE_SRC = "src";

    /** Constant for the attribute name. */
    public static final String ATTRIBUTE_VALUE = "value";

    /** HTML end. */
    public static final String HTML_END = "</body></html>";

    /** HTML start. */
    public static final String HTML_START = "<html><body>";

    /** Constant for the tag name. */
    public static final String TAG_AREA = "AREA";

    /** Constant for the tag name. */
    public static final String TAG_EMBED = "EMBED";

    /** Constant for the tag name. */
    public static final String TAG_IFRAME = "IFRAME";

    /** Constant for the tag name. */
    public static final String TAG_PARAM = "PARAM";

    /** List of attributes that may contain links for the embed tag. */
    private static final String[] EMBED_TAG_LINKED_ATTRIBS = new String[] {ATTRIBUTE_SRC, "pluginurl", "pluginspage"};

    /** List of attributes that may contain links for the object tag ("codebase" has to be first). */
    private static final String[] OBJECT_TAG_LINKED_ATTRIBS = new String[] {"codebase", "data", "datasrc"};

    /** Processing mode "process links" (macros to links). */
    private static final int PROCESS_LINKS = 1;

    /** Processing mode "replace links" (links to macros).  */
    private static final int REPLACE_LINKS = 0;

    /** The current users OpenCms context, containing the users permission and site root context. */
    private CmsObject m_cms;

    /** The selected encoding to use for parsing the HTML. */
    private String m_encoding;

    /** The link table used for link macro replacements. */
    private CmsLinkTable m_linkTable;

    /** Current processing mode. */
    private int m_mode;

    /** The relative path for relative links, if not set, relative links are treated as external links. */
    private String m_relativePath;

    /** Another OpenCms context based on the current users OpenCms context, but with the site root set to '/'. */
    private CmsObject m_rootCms;

    /**
     * Creates a new link processor.<p>
     *
     * @param cms the current users OpenCms context
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
     * Unescapes all <code>&amp;amp;</code>, that is replaces them with a <code>&</code>.<p>
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
    @Override
    public void visitTag(Tag tag) {

        if (tag instanceof LinkTag) {
            processLinkTag((LinkTag)tag);
        } else if (tag instanceof ImageTag) {
            processImageTag((ImageTag)tag);
        } else if (tag instanceof ObjectTag) {
            processObjectTag((ObjectTag)tag);
        } else {
            // there are no specialized tag classes for these tags :(
            if (TAG_EMBED.equals(tag.getTagName())) {
                processEmbedTag(tag);
            } else if (TAG_AREA.equals(tag.getTagName())) {
                processAreaTag(tag);
            } else if (TAG_IFRAME.equals(tag.getTagName())) {
                String src = tag.getAttribute(ATTRIBUTE_SRC);
                if ((src != null) && !src.startsWith("//")) {
                    // link processing does not work for protocol-relative URLs, which were once used in Youtube embed
                    // codes.
                    processLink(tag, ATTRIBUTE_SRC, CmsRelationType.HYPERLINK);
                }
            }
        }
        // append text content of the tag (may have been changed by above methods)
        super.visitTag(tag);
    }

    /**
     * Process an area tag.<p>
     *
     * @param tag the tag to process
     */
    protected void processAreaTag(Tag tag) {

        processLink(tag, ATTRIBUTE_HREF, CmsRelationType.HYPERLINK);
    }

    /**
     * Process an embed tag.<p>
     *
     * @param tag the tag to process
     */
    protected void processEmbedTag(Tag tag) {

        for (int i = 0; i < EMBED_TAG_LINKED_ATTRIBS.length; i++) {
            String attr = EMBED_TAG_LINKED_ATTRIBS[i];
            processLink(tag, attr, CmsRelationType.EMBEDDED_OBJECT);
        }
    }

    /**
     * Process an image tag.<p>
     *
     * @param tag the tag to process
     */
    protected void processImageTag(ImageTag tag) {

        processLink(tag, ATTRIBUTE_SRC, CmsRelationType.valueOf(tag.getTagName()));
    }

    /**
     * Process a tag having a link in the given attribute, considering the link as the given type.<p>
     *
     * @param tag the tag to process
     * @param attr the attribute
     * @param type the link type
     */
    protected void processLink(Tag tag, String attr, CmsRelationType type) {

        if (tag.getAttribute(attr) == null) {
            return;
        }
        CmsLink link = null;

        switch (m_mode) {
            case PROCESS_LINKS:
                // macros are replaced with links
                link = m_linkTable.getLink(CmsMacroResolver.stripMacro(tag.getAttribute(attr)));
                if (link != null) {
                    // link management check
                    String l = link.getLink(m_cms);
                    if (TAG_PARAM.equals(tag.getTagName())) {
                        // HACK: to distinguish link parameters the link itself has to end with '&' or '?'
                        // another solution should be a kind of macro...
                        if (!l.endsWith(CmsRequestUtil.URL_DELIMITER)
                            && !l.endsWith(CmsRequestUtil.PARAMETER_DELIMITER)) {
                            if (l.indexOf(CmsRequestUtil.URL_DELIMITER) > 0) {
                                l += CmsRequestUtil.PARAMETER_DELIMITER;
                            } else {
                                l += CmsRequestUtil.URL_DELIMITER;
                            }
                        }
                    }
                    // set the real target
                    tag.setAttribute(attr, CmsEncoder.escapeXml(l));

                    // In the Online project, remove href attributes with broken links from A tags.
                    if (tag.getTagName().equalsIgnoreCase("A")
                        && m_cms.getRequestContext().isOnlineOrEditDisabled()
                        && link.isInternal()
                        && (link.getResource() == null)) {
                        // getResource() == null could either mean checkConsistency has not been called, or that the link is broken.
                        // so we have to call checkConsistency to eliminate the first possibility.
                        link.checkConsistency(m_cms);
                        if (link.getResource() == null) {
                            tag.removeAttribute(ATTRIBUTE_HREF);
                            tag.setAttribute(CmsGwtConstants.ATTR_DEAD_LINK_MARKER, "true");
                        }
                    }
                }
                break;
            case REPLACE_LINKS:
                // links are replaced with macros
                String targetUri = tag.getAttribute(attr);
                if (CmsStringUtil.isNotEmpty(targetUri)) {
                    String internalUri = null;
                    if (!CmsMacroResolver.isMacro(targetUri)) {
                        m_cms.getRequestContext().setAttribute(
                            CmsDefaultLinkSubstitutionHandler.DONT_USE_CURRENT_SITE_FOR_WORKPLACE_REQUESTS,
                            "true");
                        internalUri = OpenCms.getLinkManager().getRootPath(m_cms, targetUri, m_relativePath);
                    }
                    // HACK: to distinguish link parameters the link itself has to end with '&' or '?'
                    // another solution should be a kind of macro...
                    if (!TAG_PARAM.equals(tag.getTagName())
                        || targetUri.endsWith(CmsRequestUtil.URL_DELIMITER)
                        || targetUri.endsWith(CmsRequestUtil.PARAMETER_DELIMITER)) {
                        if (internalUri != null) {
                            internalUri = rewriteUri(internalUri);
                            // this is an internal link
                            link = m_linkTable.addLink(type, internalUri, true);
                            // link management check
                            link.checkConsistency(m_cms);

                            if ("IMG".equals(tag.getTagName()) || TAG_AREA.equals(tag.getTagName())) {
                                // now ensure the image has the "alt" attribute set
                                setAltAttributeFromTitle(tag, internalUri);
                            }
                        } else {
                            // this is an external link
                            link = m_linkTable.addLink(type, targetUri, false);
                        }
                    }
                    if (link != null) {
                        tag.setAttribute(attr, CmsMacroResolver.formatMacro(link.getName()));
                    }
                }
                break;
            default: // empty
        }
    }

    /**
     * Process a link tag.<p>
     *
     * @param tag the tag to process
     */
    protected void processLinkTag(LinkTag tag) {

        processLink(tag, ATTRIBUTE_HREF, CmsRelationType.valueOf(tag.getTagName()));
    }

    /**
     * Process an object tag.<p>
     *
     * @param tag the tag to process
     */
    protected void processObjectTag(ObjectTag tag) {

        CmsRelationType type = CmsRelationType.valueOf(tag.getTagName());
        for (int i = 0; i < OBJECT_TAG_LINKED_ATTRIBS.length; i++) {
            String attr = OBJECT_TAG_LINKED_ATTRIBS[i];
            processLink(tag, attr, type);
            if ((i == 0) && (tag.getAttribute(attr) != null)) {
                // if code base is available, the other attributes are relative to it, so do not process them
                break;
            }
        }
        SimpleNodeIterator itChildren = tag.children();
        while (itChildren.hasMoreNodes()) {
            Node node = itChildren.nextNode();
            if (node instanceof Tag) {
                Tag childTag = (Tag)node;
                if (TAG_PARAM.equals(childTag.getTagName())) {
                    processLink(childTag, ATTRIBUTE_VALUE, type);
                }
            }
        }
    }

    /**
     * Ensures that the given tag has the "alt" attribute set.<p>
     *
     * if not set, it will be set from the title of the given resource.<p>
     *
     * @param tag the tag to set the alt attribute for
     * @param internalUri the internal URI to get the title from
     */
    protected void setAltAttributeFromTitle(Tag tag, String internalUri) {

        boolean hasAltAttrib = (tag.getAttribute("alt") != null);
        if (!hasAltAttrib) {
            String value = null;
            if ((internalUri != null) && (m_rootCms != null)) {
                // internal image: try to read the "alt" text from the "Title" property
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
            @SuppressWarnings("unchecked")
            Vector<Attribute> attrs = tag.getAttributesEx();
            // first element is always the tag name
            attrs.add(1, new Attribute(" "));
            attrs.add(2, new Attribute("alt", value == null ? "" : value, '"'));
        }
    }

    /**
     * Use the {@link org.opencms.file.wrapper.CmsObjectWrapper} to restore the link in the VFS.<p>
     *
     * @param internalUri the internal URI to restore
     *
     * @return the restored URI
     */
    private String rewriteUri(String internalUri) {

        // if an object wrapper is used, rewrite the uri
        if (m_cms != null) {
            Object obj = m_cms.getRequestContext().getAttribute(CmsObjectWrapper.ATTRIBUTE_NAME);
            if (obj != null) {
                CmsObjectWrapper wrapper = (CmsObjectWrapper)obj;
                return wrapper.restoreLink(internalUri);
            }
        }

        return internalUri;
    }
}