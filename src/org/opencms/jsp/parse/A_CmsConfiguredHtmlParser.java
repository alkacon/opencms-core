/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/parse/A_CmsConfiguredHtmlParser.java,v $
 * Date   : $Date: 2006/01/11 13:37:59 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp.parse;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.I_CmsHtmlNodeVisitor;

import org.htmlparser.util.ParserException;

/**
 * Base class for all classes that are specified for the &lt;cms:parse parserClass="name"
 * param="config" /&gt; tag in the parserClass Attribute.
 * <p>
 * 
 * Entry point for the tag implementation ({@link org.opencms.jsp.CmsJspTagParse}). It will
 * provide a valid {@link org.opencms.file.CmsObject} and it's configuration parameter String to an
 * internal (and unknown) {@link org.opencms.util.I_CmsHtmlNodeVisitor} implementation along with
 * the entry point {@link #doParse(String, String)} for parsing.
 * <p>
 * 
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.1.7
 * 
 */

public abstract class A_CmsConfiguredHtmlParser {

    /** The internal cms object for accessing core functionality. */
    private CmsObject m_cmsObject;

    /** The attribute value of the attribute param of the &lt;cms:parse&gt; tag. */
    private String m_param;

    /** The internal visitor implementation that will do the parsing. */
    private I_CmsHtmlNodeVisitor m_visitor;

    /**
     * Default constructor that initializes the internal visitor by using the abstract template
     * method {@link #createVisitorInstance()}.
     * <p>
     */
    protected A_CmsConfiguredHtmlParser() {

        m_visitor = createVisitorInstance();
    }

    /**
     * Returns the result of subsequent parsing to the &lt;cms:parse&lt; tag implementation.
     * <p>
     * 
     * {@link #setVisitor(I_CmsHtmlNodeVisitor)} has to be invoked with a non null value before this
     * method or it will always fail to parse.
     * <p>
     * 
     * Override this method to perform further configurations. Keep the exception contract in mind
     * to avoid serving ugly HTML pages to potential clients.
     * <p>
     * 
     * @param encoding the encoding to use for parsing.
     * 
     * @param html the html content to parse.
     * 
     * @return the result of subsequent parsing to the &lt;cms:parse&lt; tag implementation.
     * 
     * @throws ParserException if sth. goes wrong at parsing.
     * 
     * @throws CmsException if sth. goes wrong at accessing OpenCms core functionality.
     */
    public String doParse(String html, String encoding) throws ParserException, CmsException {

        String result = "";
        m_visitor.process(html, encoding);
        result = m_visitor.getResult();
        // fool Checkstyle - we need to grant this exception for more complex subclasses that e.g.
        // have to be configured from VFS:
        if (false) {
            throw new CmsException(null);
        }
        return result;
    }

    /**
     * Sets the internal cms object for accessing core functionality.
     * <p>
     * This will be invokde by the &tl;cms:parse&gt; tag implementation.
     * <p>
     * 
     * @param cmsObject the internal cms object for accessing core functionality to set.
     */
    public void setCmsObject(CmsObject cmsObject) {

        m_cmsObject = cmsObject;
    }

    /**
     * The attribute value of the attribute param of the &lt;cms:parse&gt; tag.
     * <p>
     * 
     * Will be set by the &lt;cms:parse&gt; implementation.
     * <p>
     * 
     * @param param the param to set.
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * Subclasses have to create their desired instance for parsing the html here.
     * <p>
     * 
     * @return the instance to be used for parsing the html.
     */
    protected abstract I_CmsHtmlNodeVisitor createVisitorInstance();

    /**
     * Returns the internal cms object for accessing core functionality.
     * <p>
     * 
     * This value will be initialized by the &lt;cms:parse&gt; tag.
     * <p>
     * 
     * @return the internal cms object for accessing core functionality.
     */
    protected CmsObject getCmsObject() {

        return m_cmsObject;
    }

    /**
     * Returns the param.
     * <p>
     * 
     * @return the param
     */
    protected String getParam() {

        return m_param;
    }

    /**
     * Returns the visitor.
     * <p>
     * 
     * @return the visitor
     */
    protected I_CmsHtmlNodeVisitor getVisitor() {

        return m_visitor;
    }

    /**
     * Sets the visitor.
     * <p>
     * This has to be done from the constructor of subclasses!
     * <p>
     * 
     * @param visitor the visitor to set
     */
    protected void setVisitor(I_CmsHtmlNodeVisitor visitor) {

        m_visitor = visitor;
    }
}
