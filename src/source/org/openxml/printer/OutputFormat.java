/**
 * The contents of this file are subject to the OpenXML Public
 * License Version 1.0; you may not use this file except in
 * compliance with the License. You may obtain a copy of the
 * License at http://www.openxml.org/license/
 *
 * THIS SOFTWARE AND DOCUMENTATION IS PROVIDED ON AN "AS IS" BASIS
 * WITHOUT WARRANTY OF ANY KIND EITHER EXPRESSED OR IMPLIED,
 * INCLUDING AND WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE
 * AND DOCUMENTATION IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGING. SEE THE LICENSE FOR THE
 * SPECIFIC LANGUAGE GOVERNING RIGHTS AND LIMITATIONS UNDER THE
 * LICENSE.
 *
 * The Initial Developer of this code under the License is
 * OpenXML.org. Portions created by OpenXML.org and/or Assaf Arkin
 * are Copyright (C) 1998, 1999 OpenXML.org. All Rights Reserved.
 *
 * $Id: OutputFormat.java,v 1.1 2000/01/13 13:44:20 a.lucas Exp $
 */


package source.org.openxml.printer;


import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
// import org.w3c.dom.html.HTMLDocument;


/**
 * Specifies an output format to control the printer. Based on the
 * XSLT specification for output format, plus additional parameters.
 * Used to select the suitable printer and determine how the
 * document should be formatted on output.
 * <p>
 * The two interesting constructors are:
 * <ul>
 * <li>{@link #OutputFormat(String,String,boolean)} creates a format
 *  for the specified method (XML, HTML, etc), encoding and indentation
 * <li>{@link #OutputFormat(Document,String,boolean)} creates a format
 *  compatible with the document type (XML, HTML), encoding and indentation
 * </ul>
 * 
 *
 * @version $Revision: 1.1 $ $Date: 2000/01/13 13:44:20 $
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 *         <a href="mailto:visco@exoffice.com">Keith Visco</a>
 * @see Printer
 */
public class OutputFormat
{


    /**
     * Returns the method specified for this output format.
     * Typically the method will be <tt>xml</tt>, <tt>html</tt>
     * or <tt>text</tt>, but it might be other values.
     * If no method was specified, null will be returned
     * and the most suitable method will be determined for
     * the document by calling {@link #whichMethod}.
     *
     * @return The specified output method, or null
     */
    public String getMethod()
    {
        return _method;
    }


    /**
     * Sets the method for this output format.
     *
     * @see #getMethod
     * @param method The output method, or null
     */
    public void setMethod( String method )
    {
	_method = method;
    }


    /**
     * Returns the version for this output method.
     * If no version was specified, will return null
     * and the default version number will be used.
     * If the printer does not support that particular
     * version, it should default to a supported version.
     *
     * @return The specified method version, or null
     */
    public String getVersion()
    {
	return _version;
    }


    /**
     * Sets the version for this output method.
     * For XML the value would be "1.0", for HTML
     * it would be "4.0".
     *
     * @see #getVersion
     * @param version The output method version, or null
     */
    public void setVersion( String version )
    {
	_version = version;
    }


    /**
     * Returns the indentation specified. If no indentation
     * was specified, zero is returned and the document
     * should not be indented.
     *
     * @return The indentation or zero
     * @see #setIndenting
     */
    public int getIndent()
    {
	return _indent;
    }


    /**
     * Returns true if indentation was specified.
     */
    public boolean getIndenting()
    {
	return ( _indent > 0 );
    }


    /**
     * Sets the indentation. The document will not be
     * indented if the indentation is set to zero.
     * Calling {@link #setIndenting} will reset this
     * value to zero (off) or the default (on).
     *
     * @param indent The indentation, or zero
     */
    public void setIndent( int indent )
    {
	if ( indent < 0 )
	    _indent = 0;
	else
	    _indent = indent;
    }


    /**
     * Sets the indentation on and off. When set on, the default
     * indentation level and default line wrapping is used
     * (see {@link #DEFAULT_INDENT} and {@link #DEFAULT_LINE_WIDTH}).
     * To specify a different indentation level or line wrapping,
     * use {@link #setIndent} and {@link #setLineWidth}.
     *
     * @param on True if indentation should be on
     */
    public void setIndenting( boolean on )
    {
	if ( on ) {
	    _indent = DEFAULT_INDENT;
	    _lineWidth = DEFAULT_LINE_WIDTH;
	} else {
	    _indent = 0;
	    _lineWidth = 0;
	}
    }


    /**
     * Returns the specified encoding. If no encoding was
     * specified, the default is always "UTF8".
     *
     * @return The encoding
     */
    public String getEncoding()
    {
	return _encoding;
    }


    /**
     * Sets the encoding for this output method. If no
     * encoding was specified, the default is always "UTF8".
     * Make sure the encoding is compatible with the one
     * used by the {@link java.io.Writer}.
     *
     * @see #getEncoding
     * @param encoding The encoding, or null
     */
    public void setEncoding( String encoding )
    {
	_encoding = encoding;
    }


    /**
     * Returns the specified media type, or null.
     * To determine the media type based on the
     * document type, use {@link #whichMediaType}.
     *
     * @return The specified media type, or null
     */
    public String getMediaType()
    {
	return _mediaType;
    }


    /**
     * Sets the media type.
     *
     * @see #getMediaType
     * @param mediaType The specified media type
     */
    public void setMediaType( String mediaType )
    {
	_mediaType = mediaType;
    }


    /**
     * Sets the document type public and system identifiers.
     * No <tt>DOCTYPE</tt> will be printed if both identifiers
     * are null. A system identifier is required if a public
     * identified is specified.
     *
     * @param publicId The public identifier, or null
     * @param systemId The system identifier, or null
     */
    public void setDoctype( String publicId, String systemId )
    {
	_doctypePublic = publicId;
	_doctypeSystem = systemId;
    }


    /**
     * Returns the specified document type public identifier,
     * or null.
     */
    public String getDoctypePublic()
    {
	return _doctypePublic;
    }


    /**
     * Returns the specified document type system identifier,
     * or null.
     */
    public String getDoctypeSystem()
    {
	return _doctypeSystem;
    }


    /**
     * Returns true if the XML document declaration should
     * be ommited. The default is false.
     */
    public boolean getOmitXMLDeclaration()
    {
	return _omitXmlDeclaration;
    }


    /**
     * Sets XML declaration omitting on and off.
     *
     * @param omit True if XML declaration should be ommited
     */
    public void setOmitXMLDeclaration( boolean omit )
    {
	_omitXmlDeclaration = omit;
    }


    /**
     * Returns true if the document type is standalone.
     * The default is false.
     */
    public boolean getStandalone()
    {
	return _standalone;
    }


    /**
     * Sets document DTD standalone. The public and system
     * identifiers must be null for the document to be printed
     * as standalone.
     *
     * @param standalone True if document DTD is standalone
     */
    public void setStandalone( boolean standalone )
    {
	_standalone = standalone;
    }


    /**
     * Returns a list of all the elements whose text node children
     * should be output as CDATA, or null if no such elements were
     * specified.
     */
    public String[] getCDataElements()
    {
	return _cdataElements;
    }


    /**
     * Returns true if the text node children of the given elements
     * should be output as CDATA.
     *
     * @param tagName The element's tag name
     * @return True if should print as CDATA
     */
    public boolean isCDataElement( String tagName )
    {
	int i;

	if ( _cdataElements == null )
	    return false;
	for ( i = 0 ; i < _cdataElements.length ; ++i )
	    if ( _cdataElements[ i ].equals( tagName ) )
		return true;
	return false;
    }


    /**
     * Sets the list of elements for which text node children
     * should be output as CDATA.
     *
     * @param cdataElements List of CDATA element tag names
     */
    public void setCDataElements( String[] cdataElements )
    {
	_cdataElements = cdataElements;
    }


    /**
     * Returns a list of all the elements whose text node children
     * should be output unescaped (no character references), or null
     * if no such elements were specified.
     */
    public String[] getNonEscapingElements()
    {
	return _nonEscapingElements;
    }


    /**
     * Returns true if the text node children of the given elements
     * should be output unescaped.
     *
     * @param tagName The element's tag name
     * @return True if should print unescaped
     */
    public boolean isNonEscapingElement( String tagName )
    {
	int i;

	if ( _nonEscapingElements == null )
	    return false;
	for ( i = 0 ; i < _nonEscapingElements.length ; ++i )
	    if ( _nonEscapingElements[ i ].equals( tagName ) )
		return true;
	return false;
    }


    /**
     * Sets the list of elements for which text node children
     * should be output unescaped (no character references).
     *
     * @param nonEscapingElements List of unescaped element tag names
     */
    public void setNonEscapingElements( String[] nonEscapingElements )
    {
	_nonEscapingElements = nonEscapingElements;
    }



    /**
     * Returns a specific line separator to use. The default is the
     * Web line separator (<tt>\n</tt>). A string is returned to
     * support double codes (CR + LF).
     *
     * @return The specified line separator
     */
    public String getLineSeparator()
    {
	return _lineSeparator;
    }


    /**
     * Sets the line separator. The default is the Web line separator
     * (<tt>\n</tt>). The machine's line separator can be obtained
     * from the system property <tt>line.separator</tt>, but is only
     * useful if the document is edited on machines of the same type.
     * For general documents, use the Web line separator.
     *
     * @param lineSeparator The specified line separator
     */
    public void setLineSeparator( String lineSeparator )
    {
	if ( lineSeparator == null )
	    _lineSeparator =  LINE_SEPARATOR_WEB;
	else
	    _lineSeparator = lineSeparator;
    }


    /**
     * Returns true if the default behavior for this format is to
     * preserve spaces. All elements that do not specify otherwise
     * or specify the default behavior will be formatted based on
     * this rule. All elements that specify space preserving will
     * always preserve space.
     */
    public boolean getPreserveSpace()
    {
	return _preserve;
    }


    /**
     * Sets space preserving as the default behavior. The default is
     * space stripping and all elements that do not specify otherwise
     * or use the default value will not preserve spaces.
     *
     * @param preserve True if spaces should be preserved
     */
    public void setPreserveSpace( boolean preserve )
    {
	_preserve = preserve;
    }


    /**
     * Return the selected line width for breaking up long lines.
     * When indenting, and only when indenting, long lines will be
     * broken at space boundaries based on this line width.
     * No line wrapping occurs if this value is zero.
     */
    public int getLineWidth()
    {
	return _lineWidth;
    }


    /**
     * Sets the line width. If zero then no line wrapping will
     * occur. Calling {@link #setIndenting} will reset this
     * value to zero (off) or the default (on).
     *
     * @param lineWidth The line width to use, zero for default
     * @see #getLineWidth
     * @see #setIndenting
     */
    public void setLineWidth( int lineWidth )
    {
	if ( lineWidth <= 0 )
	    _lineWidth = 0;
	else
	    _lineWidth = lineWidth;
    }


    /**
     * Returns the last printable character based on the selected
     * encoding. Control characters and non-printable characters
     * are always printed as character references.
     */
    public char getLastPrintable()
    {
	if ( getEncoding() != null &&
	     ( getEncoding().equalsIgnoreCase( "ASCII" ) ) )
	    return 0xFF;
	else
	    return 0xFFFF;
    }


    /**
     * Determine the output method for the specified document.
     * If the document is an instance of {@link org.w3c.dom.html.HTMLDocument}
     * then the method is said to be <tt>html</tt>. If the root
     * element is 'html' and all text nodes preceding the root
     * element are all whitespace, then the method is said to be
     * <tt>html</tt>. Otherwise the method is <tt>xml</tt>.
     *
     * @param doc The document to check
     * @return The suitable method
     */
    public static String whichMethod( Document doc )
    {
        Node    node;
	String  value;
	int     i;

	// If document is derived from HTMLDocument then the default
	// method is html.
	// if ( doc instanceof HTMLDocument )
	//    return METHOD_HTML;
	// Lookup the root element and the text nodes preceding it.
	// If root element is html and all text nodes contain whitespace
	// only, the method is html.
        node = doc.getFirstChild();
        while ( node != null ) {
	    // If the root element is html, the method is html.
	    if ( node.getNodeType() == Node.ELEMENT_NODE ) {
		if ( node.getNodeName().equalsIgnoreCase( "html" ) )
		    return METHOD_HTML;
		else
		    return METHOD_XML;
	    }
	    else
	    if ( node.getNodeType() == Node.TEXT_NODE ) {
		// If a text node preceding the root element contains
		// only whitespace, this might be html, otherwise it's
		// definitely xml.
		value = node.getNodeValue();
		for ( i = 0 ; i < value.length() ; ++i )
		    if ( value.charAt( i ) != 0x20 && value.charAt( i ) != 0x0A &&
			 value.charAt( i ) != 0x09 && value.charAt( i ) != 0x0D )
			return METHOD_XML;
	    }
	    node = node.getNextSibling();
	}
	// Anything else, the method is xml.
	return METHOD_XML;
    }


    /**
     * Returns the document type public identifier
     * specified for this document, or null.
     */
    public static String whichDoctypePublic( Document doc )
    {
	DocumentType doctype;

	/* FIXME: Uncomment when DOM Level 2 is available
	doctype = doc.getDoctype();
	if ( doctype != null ) {
	    // Note on catch: DOM Level 1 does not specify this method
	    // and the code will throw a NoSuchMethodError
	    try {
		return doctype.getPublicID();
	    } catch ( Error except ) {  }
	}
	*/
	// if ( doc instanceof HTMLDocument )
	//    return DOCTYPE_XHTML_PUBLIC;
	return null;
    }


    /**
     * Returns the document type system identifier
     * specified for this document, or null.
     */
    public static String whichDoctypeSystem( Document doc )
    {
	DocumentType doctype;

	/* FIXME: Uncomment when DOM Level 2 is available
	doctype = doc.getDoctype();
	if ( doctype != null ) {
	    // Note on catch: DOM Level 1 does not specify this method
	    // and the code will throw a NoSuchMethodError
	    try {
		return doctype.getSystemID();
	    } catch ( Error except ) { }
	}
	*/
	// if ( doc instanceof HTMLDocument )
	//    return DOCTYPE_XHTML_SYSTEM;
	return null;
    }


    /**
     * Returns the suitable media format for a document
     * output with the specified method.
     */
    public static String whichMediaType( String method )
    {
	if ( method.equalsIgnoreCase( METHOD_XML ) )
	    return "text/xml";
	if ( method.equalsIgnoreCase( METHOD_HTML ) )
	    return "text/html";
	if ( method.equalsIgnoreCase( METHOD_TEXT ) )
	    return "text/plain";
	return null;
    }


    /**
     * Constructs a new output format with the default values.
     */
    public OutputFormat()
    {
    }


    /**
     * Constructs a new output format with the default values for
     * the specified method and encoding. If <tt>indent</tt>
     * is true, the document will be pretty printed with the default
     * indentation level and default line wrapping.
     *
     * @param method The specified output method
     * @param encoding The specified encoding
     * @param indenting True for pretty printing
     * @see #setEncoding
     * @see #setIndenting
     * @see #setMethod
     */
    public OutputFormat( String method, String encoding, boolean indenting )
    {
	setMethod( method );
	setEncoding( encoding );
	setIndenting( indenting );
    }


    /**
     * Constructs a new output format with the proper method,
     * document type identifiers and media type for the specified
     * document.
     *
     * @param doc The document to output
     * @see #whichMethod
     */
    public OutputFormat( Document doc )
    {
	setMethod( whichMethod( doc ) );
	setDoctype( whichDoctypePublic( doc ), whichDoctypeSystem( doc ) );
	setMediaType( whichMediaType( getMethod() ) );
    }
    

    /**
     * Constructs a new output format with the proper method,
     * document type identifiers and media type for the specified
     * document, and with the specified encoding. If <tt>indent</tt>
     * is true, the document will be pretty printed with the default
     * indentation level and default line wrapping.
     *
     * @param doc The document to output
     * @param encoding The specified encoding
     * @param indenting True for pretty printing
     * @see #setEncoding
     * @see #setIndenting
     * @see #whichMethod
     */
    public OutputFormat( Document doc, String encoding, boolean indenting )
    {
	this( doc );
	setEncoding( encoding );
	setIndenting( indenting );
    }


    /**
     * Holds the output method specified for this document,
     * or null if no method was specified.
     */
    private String _method;


    /**
     * Specifies the version of the output method.
     */
    private String _version;


    /**
     * The indentation level, or zero if no indentation
     * was requested.
     */
    private int _indent = 0;


    /**
     * The encoding to use, if an input stream is used.
     * The default is always UTF-8.
     */
    private String _encoding = DEFAULT_ENCODING;


    /**
     * The specified media type or null.
     */
    private String _mediaType;


    /**
     * The specified document type system identifier, or null.
     */
    private String _doctypeSystem;


    /**
     * The specified document type public identifier, or null.
     */
    private String _doctypePublic;


    /**
     * Ture if the XML declaration should be ommited;
     */
    private boolean _omitXmlDeclaration = false;


    /**
     * True if the document type should be marked as standalone.
     */
    private boolean _standalone = false;


    /**
     * List of element tag names whose text node children must
     * be output as CDATA.
     */
    private String[] _cdataElements;


    /**
     * List of element tag names whose text node children must
     * be output unescaped.
     */
    private String[] _nonEscapingElements;


    /**
     * The selected line separator.
     */
    private String _lineSeparator = LINE_SEPARATOR_WEB;


    /**
     * The line width at which to wrap long lines when indenting.
     */
    private int _lineWidth = DEFAULT_LINE_WIDTH;


    /**
     * True if spaces should be preserved in elements that do not
     * specify otherwise, or specify the default behavior.
     */
    private boolean _preserve = false;


    /**
     * If indentation is turned on, the default identation
     * level is 4.
     *
     * @see #setIndenting(boolean)
     */
    public static final int DEFAULT_INDENT = 4;


    /**
     * The default encoding for Web documents it UTF8.
     *
     * @see #getEncoding()
     */
    public static final String DEFAULT_ENCODING = "UTF8";


    /**
     * The default line width at which to break long lines
     * when identing. This is set to 72.
     */
    public static final int DEFAULT_LINE_WIDTH = 72;


    /**
     * The output method for XML documents.
     */
    public static final String METHOD_XML = "xml";


    /**
     * The output method for HTML documents.
     */
    public static final String METHOD_HTML = "html";


    /**
     * The output method for HTML documents as XHTML.
     */
    public static final String METHOD_XHTML = "xhtml";


    /**
     * The output method for text documents.
     */
    public static final String METHOD_TEXT = "text";


    /**
     * Line separator for Unix systems (<tt>\n</tt>).
     */
    public static final String LINE_SEPARATOR_UNIX = "\n";


    /**
     * Line separator for Windows systems (<tt>\r\n</tt>).
     */
    public static final String LINE_SEPARATOR_WIN = "\r\n";


    /**
     * Line separator for Macintosh systems (<tt>\r</tt>).
     */
    public static final String LINE_SEPARATOR_MAC = "\r";


    /**
     * Line separator for the Web (<tt>\n</tt>).
     */
    public static final String LINE_SEPARATOR_WEB = "\n";


    /**
     * Public identifier for HTML document type.
     */
    public static final String DOCTYPE_HTML_PUBLIC = "-//W3C//DTD HTML 4.0//EN";


    /**
     * System identifier for HTML document type.
     */
    public static final String DOCTYPE_HTML_SYSTEM =
	"http://www.w3.org/TR/WD-html-in-xml/DTD/xhtml1-strict.dtd";


    /**
     * Public identifier for XHTML document type.
     */
    public static final String DOCTYPE_XHTML_PUBLIC = "-//W3C//DTD XHTML 1.0 Strict//EN";


    /**
     * System identifier for XHTML document type.
     */
    public static final String DOCTYPE_XHTML_SYSTEM =
	"http://www.w3.org/TR/WD-html-in-xml/DTD/xhtml1-strict.dtd";


}

