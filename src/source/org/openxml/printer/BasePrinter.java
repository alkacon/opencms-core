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
 * $Id: BasePrinter.java,v 1.1 2000/01/13 13:44:20 a.lucas Exp $
 */


package source.org.openxml.printer;


import java.io.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.w3c.dom.*;
// import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * Base class for a printer supporting both DOM and SAX pretty
 * printing of XML/HTML/XHTML documents. Derives classes perform
 * the method-specific printing, this class provides the common
 * printing mechanisms.
 * <p>
 * The printer must be initialized with the proper writer and
 * output format before it can be used by calling {@link #init}.
 * The printer can be reused any number of times, but cannot
 * be used concurrently by two threads.
 * <p>
 * If an output stream is used, the encoding is taken from the
 * output format (defaults to <tt>UTF8</tt>). If a writer is
 * used, make sure the writer uses the same encoding (if applies)
 * as specified in the output format.
 * <p>
 * The printer supports both DOM and SAX. DOM printing is done
 * by calling {@link #print} and SAX printing is done by firing
 * SAX events and using the printer as a document handler.
 * This also applies to derived class.
 * <p>
 * If an I/O exception occurs while printing, the printer
 * will not throw an exception directly, but only throw it
 * at the end of printing (either DOM or SAX's {@link
 * org.xml.sax.DocumentHandler#endDocument}.
 * <p>
 * For elements that are not specified as whitespace preserving,
 * the printer will potentially break long text lines at space
 * boundaries, indent lines, and print elements on separate
 * lines. Line terminators will be regarded as spaces, and
 * spaces at beginning of line will be stripped.
 * <p>
 * When indenting, the printer is capable of detecting seemingly
 * element content, and printing these elements indented on separate
 * lines. An element is printed indented when it is the first or
 * last child of an element, or immediate following or preceding
 * another element.
 * 
 *
 * @version $Revision: 1.1 $ $Date: 2000/01/13 13:44:20 $
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see Printer
 * @see DocumentHandler
 * @see XMLPrinter
 */
abstract class BasePrinter
    extends Printer
    implements DocumentHandler
{


    //-------------------------------//
    // DOM document printing methods //
    //-------------------------------//


    /**
     * Prints the DOM element using the previously specified
     * writer and output format. Throws an exception only if
     * an I/O exception occured while printing.
     *
     * @param elem The element to print
     * @throws IOException An I/O exception occured while
     *   printing
     */
    public void print( Element elem )
        throws IOException
    {
	try {
	    startDocument();
	} catch ( SAXException except ) { }
	printNode( elem );
	flush();
	if ( _exception != null )
	    throw _exception;
    }


    /**
     * Prints the DOM document using the previously specified
     * writer and output format. Throws an exception only if
     * an I/O exception occured while printing.
     *
     * @param doc The document to print
     * @throws IOException An I/O exception occured while
     *   printing
     */
    public void print( Document doc )
        throws IOException
    {
	try {
	    startDocument();
	} catch ( SAXException except ) { }
	printNode( doc );
	printPreRoot();
        flush();
	if ( _exception != null )
	    throw _exception;
    }


    //---------------------------------------//
    // SAX document handler printing methods //
    //---------------------------------------//


    public void characters( char[] chars, int start, int end )
    {
	characters( new String( chars, start, end ), false, false );
    }


    public void ignorableWhitespace( char[] chars, int start, int end )
    {
	int i;

	content();

	// Print ignorable whitespaces only when indenting, after
	// all they are indentation. Cancel the indentation to
	// not indent twice.
	if ( _format.getIndenting() ) {
	    _thisIndent = 0;
	    for ( i = start ; i < end ; ++i ) {
		if ( chars[ i ] == '\n' || chars[ i ] == '\r' )
		    breakLine();
		else
		    _text.append( chars[ i ] );
	    }
	}
    }


    public void processingInstruction( String target, String code )
    {
	int          index;
	StringBuffer buffer;
	ElementState state;

	state = content();
	buffer = new StringBuffer( 40 );

	// Create the processing instruction textual representation.
	// Make sure we don't have '?>' inside either target or code.
	index = target.indexOf( "?>" );
	if ( index >= 0 )
	    buffer.append( "<?" ).append( target.substring( 0, index ) );
	else
	    buffer.append( "<?" ).append( target );
	if ( code != null ) {
	    buffer.append( ' ' );
	    index = code.indexOf( "?>" );
	    if ( index >= 0 )
		buffer.append( code.substring( 0, index ) );
	    else
		buffer.append( code );
	}
	buffer.append( "?>" );

	// If before the root element (or after it), do not print
	// the PI directly but place it in the pre-root vector.
	if ( state == null ) {
	    if ( _preRoot == null )
		_preRoot = new Vector();
	    _preRoot.addElement( buffer.toString() );
	}
	else {
	    indent();
	    printText( buffer, true );
	    unindent();
	}
    }


    public void comment( String text )
    {
	StringBuffer buffer;
	int          index;
	ElementState state;

	state  = content();
	buffer = new StringBuffer( 40 );
	// Create the processing comment textual representation.
	// Make sure we don't have '-->' inside the comment.
	index = text.indexOf( "-->" );
	if ( index >= 0 )
	    buffer.append( "<!--" ).append( text.substring( 0, index ) ).append( "-->" );
	else
	    buffer.append( "<!--" ).append( text ).append( "-->" );

	// If before the root element (or after it), do not print
	// the comment directly but place it in the pre-root vector.
	if ( state == null ) {
	    if ( _preRoot == null )
		_preRoot = new Vector();
	    _preRoot.addElement( buffer.toString() );
	}
	else {
	    indent();
	    printText( buffer, false );
	    unindent();
	}
    }


    /**
     * Called at the end of the document to wrap it up.
     * Will flush the output stream and throw an exception
     * if any I/O error occured while printing.
     *
     * @throws SAXException An I/O exception occured during
     *  printing
     */
    public void endDocument()
        throws SAXException
    {
	// Print all the elements accumulated outside of
	// the root element.
	printPreRoot();
	// Flush the output, this is necessary for buffered output.
        flush();
	// If an exception was thrown during printing, this would
	// be the best time to report it.
	if ( _exception != null )
	    throw new SAXException( _exception );
    }


    public void setDocumentLocator( Locator locator )
    {
	// Nothing to do
    }


    //----------------------------------------//
    // Generic node printing methods methods //
    //---------------------------------------//


    /**
     * Print the DOM node. This method is shared across XML, HTML and XHTML
     * printers and the differences are masked out in a separate {@link
     * #printElement}.
     *
     * @param node The node to print
     * @see #printElement
     */
    protected void printNode( Node node )
    {
	// Based on the node type call the suitable SAX handler.
	// Only comments entities and documents which are not
	// handled by SAX are printed directly.
        switch ( node.getNodeType() ) {
	case Node.TEXT_NODE :
	    characters( node.getNodeValue(), false, false );
	    break;

	case Node.CDATA_SECTION_NODE :
	    characters( node.getNodeValue(), true, false );
	    break;

	case Node.COMMENT_NODE :
	    comment( node.getNodeValue() );
	    break;

	case Node.ENTITY_REFERENCE_NODE :
	    // Entity reference print directly in text, do not break or pause.
	    content();
	    printText( '&' + node.getNodeName() + ';' );
	    break;

	case Node.PROCESSING_INSTRUCTION_NODE :
	    processingInstruction( node.getNodeName(), node.getNodeValue() );
	    break;

	case Node.ELEMENT_NODE :
	    printElement( (Element) node );
	    break;

	case Node.DOCUMENT_NODE :
	case Node.DOCUMENT_FRAGMENT_NODE : {
	    Node child;

	    // By definition this will happen if the node is a document,
	    // document fragment, etc. Just print its contents. It will
	    // work well for other nodes that we do not know how to print.
	    child = node.getFirstChild();
	    while ( child != null ) {
		printNode( child );
		child = child.getNextSibling();
	    }
	    break;
	}

	default:
	    break;
	}
    }


    /**
     * Must be called by a method about to print any type of content.
     * If the element was just opened, the opening tag is closed and
     * will be matched to a closing tag. Returns the current element
     * state with <tt>empty</tt> and <tt>afterElement</tt> set to false.
     *
     * @return The current element state
     */    
    protected ElementState content()
    {
	ElementState state;

	state = getElementState();
	if ( state != null ) {
	    // If this is the first content in the element,
	    // change the state to not-empty and close the
	    // opening element tag.
	    if ( state.empty ) {
		printText( ">" );
		state.empty = false;
	    }
	    // Except for one content type, all of them
	    // are not last element. That one content
	    // type will take care of itself.
	    state.afterElement = false;
	}
	return state;
    }


    /**
     * Called to print the text contents in the prevailing element format.
     * Since this method is capable of printing text as CDATA, it is used
     * for that purpose as well. White space handling is determined by the
     * current element state. In addition, the output format can dictate
     * whether the text is printed as CDATA or unescaped.
     *
     * @param text The text to print
     * @param cdata True is should print as CDATA
     * @param unescaped True is should print unescaped
     */
    protected void characters( String text, boolean cdata, boolean unescaped )
    {
	ElementState state;

	state = content();
	// Check if text should be printed as CDATA section or unescaped
	// based on elements listed in the output format.
	if ( state != null && _format.isCDataElement( state.tagName ) )
	    cdata = true;
	else
	if ( state != null && _format.isNonEscapingElement( state.tagName ) )
	    unescaped = true;
	
	if ( cdata ) {
	    StringBuffer buffer;
	    int          index;
	    int          saveIndent;

	    // Printing a CDATA section. Unlike printing a space,
	    // the text is not escaped, but ']]>' appearing in the
	    // code must be identified and dealt with. The contents
	    // of a text node is considered space preserving.
	    buffer = new StringBuffer( text.length() );
	    index = text.indexOf( "]]>" );
	    while ( index >= 0 ) {
		buffer.append( "<![CDATA[" ).append( text.substring( 0, index + 2 ) ).append( "]]>" );
		text = text.substring( index + 2 );
		index = text.indexOf( "]]>" );
	    }
	    buffer.append( "<![CDATA[" ).append( text ).append( "]]>" );
	    saveIndent = _nextIndent;
	    _nextIndent = 0;
	    printText( buffer, true );
	    _nextIndent = saveIndent;

	} else {

	    int saveIndent;

	    if ( unescaped ) {
		// If the text node of this element should be printed
		// unescaped, then cancel indentation and print it
		// directly without escaping.
		saveIndent = _nextIndent;
		_nextIndent = 0;
		printText( text, true );
		_nextIndent = saveIndent;
		
	    } else if ( state != null && state.preserveSpace ) {
		// If preserving space then hold of indentation so no
		// excessive spaces are printed at line breaks, escape
		// the text content without replacing spaces and print
		// the text breaking only at line breaks.
		saveIndent = _nextIndent;
		_nextIndent = 0;
		printText( escape( text ), true );
		_nextIndent = saveIndent;
		
	    } else {
		// This is the last, but the most common case of
		// printing without preserving spaces. If indentation was
		// requested, line will wrap at space boundaries.
		// All whitespaces will print as space characters.
		printText( escape( text ), false );
	    }

	}
    }


    /**
     * Returns the suitable entity reference for this character value,
     * or null if no such entity exists. Calling this method with <tt>'&amp;'</tt>
     * will return <tt>"&amp;amp;"</tt>.
     *
     * @param ch Character value
     * @return Character entity name, or null
     */
    protected abstract String getEntityRef( char ch );


    /**
     * Called to printe the DOM element. The element is printed based on
     * the printer's method (XML, HTML, XHTML).
     *
     * @param elem The element to print
     */
    protected abstract void printElement( Element elem );


    /**
     * Comments and PIs cannot be printed before the root element,
     * because the root element prints the document type, which
     * generally comes first. Instead such PIs and comments are
     * accumulated inside a vector and printed by calling this
     * method. Will be called when the root element is printed
     * and when the document finished printing.
     */
    protected void printPreRoot()
    {
	int i;

	if ( _preRoot != null ) {
	    for ( i = 0 ; i < _preRoot.size() ; ++i ) {
		printText( (String) _preRoot.elementAt( i ), true );
		breakLine();
	    }
	    _preRoot.removeAllElements();
	}
    }


    //---------------------------------------------//
    // Text pretty printing and formatting methods //
    //---------------------------------------------//


    /**
     * Called to print additional text. Each time this method is called
     * it accumulates more text. When a space is printed ({@link
     * #printSpace}) all the accumulated text becomes one part and is
     * added to the accumulate line. When a line is long enough, it can
     * be broken at its text boundary.
     *
     * @param text The text to print
     */
    protected final void printText( String text )
    {
	// Add this text to the accumulated text which will not be
	// printed until the next space break.
	_text.append( text );
    }


    protected final void printText( char[] chars, int start, int end )
    {
	_text.append( chars, start, end );
    }


    /**
     * Called to print additional text with whitespace handling.
     * If spaces are preserved, the text is printed as if by calling
     * {@link #printText(String)} with a call to {@link #breakLine}
     * for each new line. If spaces are not preserved, the text is
     * broken at space boundaries if longer than the line width;
     * Multiple spaces are printed as such, but spaces at beginning
     * of line are removed.
     *
     * @param text The text to print
     * @param preserveSpace Space preserving flag
     */
    protected final void printText( String text, boolean preserveSpace )
    {
	int index;
	char ch;

        if ( preserveSpace ) {
	    // Preserving spaces: the text must print exactly as it is,
	    // without breaking when spaces appear in the text and without
	    // consolidating spaces. If a line terminator is used, a line
	    // break will occur.
	    for ( index = 0 ; index < text.length() ; ++index ) {
		ch = text.charAt( index );
		if ( ch == '\n' || ch == '\r' )
		    breakLine();
		else
		    _text.append( ch );
	    }
        }
        else
        {
	    // Not preserving spaces: print one part at a time, and
	    // use spaces between parts to break them into different
	    // lines. Spaces at beginning of line will be stripped
	    // by printing mechanism. Line terminator is treated
	    // no different than other text part.
	    for ( index = 0 ; index < text.length() ; ++index ) {
		ch = text.charAt( index );
		if ( ch == ' ' || ch == '\f' || ch == '\t' || ch == '\n' || ch == '\r' )
		    printSpace();
		else
		    _text.append( ch );		    
	    }
        }
    }


    protected final void printText( StringBuffer text, boolean preserveSpace )
    {
	int index;
	char ch;

        if ( preserveSpace ) {
	    // Preserving spaces: the text must print exactly as it is,
	    // without breaking when spaces appear in the text and without
	    // consolidating spaces. If a line terminator is used, a line
	    // break will occur.
	    for ( index = 0 ; index < text.length() ; ++index ) {
		ch = text.charAt( index );
		if ( ch == '\n' || ch == '\r' )
		    breakLine();
		else
		    _text.append( ch );
	    }
        }
        else
        {
	    // Not preserving spaces: print one part at a time, and
	    // use spaces between parts to break them into different
	    // lines. Spaces at beginning of line will be stripped
	    // by printing mechanism. Line terminator is treated
	    // no different than other text part.
	    for ( index = 0 ; index < text.length() ; ++index ) {
		ch = text.charAt( index );
		if ( ch == ' ' || ch == '\f' || ch == '\t' || ch == '\n' || ch == '\r' )
		    printSpace();
		else
		    _text.append( ch );		    
	    }
        }
    }


    /**
     * Called to print a single space between text parts that may be
     * broken into separate lines. Must not be called to print a space
     * when preserving spaces. The text accumulated so far with {@link
     * #printText} will be added to the accumulated line, and a space
     * separator will be counted. If the line accumulated so far is
     * long enough, it will be printed.
     */
    protected final void printSpace()
    {
	// The line consists of the text accumulated in _line,
	// followed by one or more spaces as counted by _spaces,
	// followed by more space accumulated in _text:
	// -  Text is printed and accumulated into _text.
	// -  A space is printed, so _text is added to _line and
	//    a space is counted.
	// -  More text is printed and accumulated into _text.
	// -  A space is printed, the previous spaces are added
	//    to _line, the _text is added to _line, and a new
	//    space is counted.

	// If text was accumulated with printText(), then the space
	// means we have to move that text into the line and
	// start accumulating new text with printText().
	if ( _text.length() > 0 ) {

	    // If the text breaks a line bounary, wrap to the next line.
	    // The printed line size consists of the indentation we're going
	    // to use next, the accumulated line so far, some spaces and the
	    // accumulated text so far.
	    if ( _format.getLineWidth() > 0 &&
		 _thisIndent + _line.length() + _spaces + _text.length() > _format.getLineWidth() )
		flushLine();

	    // Add as many spaces as we accumulaed before.
	    // At the end of this loop, _spaces is zero.
	    while ( _spaces > 0 ) {
		_line.append( ' ' );
		--_spaces;
	    }
	    _line.append( _text );
	    _text = new StringBuffer( 20 );
	}
	// Starting a new word: accumulate the text between the line
	// and this new word; not a new word: just add another space.
	++_spaces;
    }


    /**
     * Called to print a line consisting of the text accumulated so
     * far. This is equivalent to calling {@link #printSpace} but
     * forcing the line to print and starting a new line ({@link
     * #printSpace} will only start a new line if the current line
     * is long enough).
     */
    protected final void breakLine()
    {
	// Equivalent to calling printSpace and forcing a flushLine.
	if ( _text.length() > 0 ) {
	    while ( _spaces > 0 ) {
		_line.append( ' ' );
		--_spaces;
	    }	    
	    _line.append( _text );
	    _text = new StringBuffer( 20 );
	}
        flushLine();
    }


    /**
     * Flushes the line accumulated so far to the writer and get ready
     * to accumulate the next line. This method is called by {@link
     * #printText} and {@link #printSpace} when the accumulated line plus
     * accumulated text are two long to fit on a given line. At the end of
     * this method {@link #_line} is empty and {@link #_spaces} is zero.
     */
    private void flushLine()
    {
        int     indent;

	if ( _line.length() > 0 ) {
	    try {

		if ( _format.getIndenting() ) {
		    // Make sure the indentation does not blow us away.
		    indent = _thisIndent;
		    if ( ( 2 * indent ) > _format.getLineWidth() && _format.getLineWidth() > 0 )
			indent = _format.getLineWidth() / 2;
		    // Print the indentation as spaces and set the current
		    // indentation to the next expected indentation.
		    while ( indent > 0 ) {
			_writer.write( ' ' );
			--indent;
		    }
		}
		_thisIndent = _nextIndent;

		// There is no need to print the spaces at the end of the line,
		// they are simply stripped and replaced with a single line
		// separator.
		_spaces = 0;

		// Print line and new line, then zero the line contents.
		_writer.write( _line.toString() );
		_writer.write( _format.getLineSeparator() );
		_line = new StringBuffer( 40 );
	    } catch ( IOException except ) {
		// We don't throw an exception, but hold it
		// until the end of the document.
		if ( _exception == null )
		    _exception = except;
	    }
	}
    }


    /**
     * Flush the output stream. Must be called when done printing
     * the document, otherwise some text might be buffered.
     */
    protected void flush()
    {
	breakLine();
	flushLine();
	try {
	    _writer.flush();
	} catch ( IOException except ) {
	    // We don't throw an exception, but hold it
	    // until the end of the document.
	    if ( _exception == null )
		_exception = except;
	}
    }


    /**
     * Increment the indentation for the next line.
     */
    protected void indent()
    {
	_nextIndent += _format.getIndent();
    }


    /**
     * Decrement the indentation for the next line.
     */
    protected void unindent()
    {
	_nextIndent -= _format.getIndent();
	if ( _nextIndent < 0 )
	    _nextIndent = 0;
	// If there is no current line and we're de-identing then
	// this indentation level is actually the next level.
	if ( ( _line.length() + _spaces + _text.length() ) == 0 )
	    _thisIndent = _nextIndent;
    }


    /**
     * Prints a document type public or system identifier URL.
     * Encapsulates the URL in double quotes, escapes non-printing
     * characters and prints it equivalent to {@link #printText}.
     *
     * @param url The document type url to append
     */
    protected void printDoctypeURL( String url )
    {
        StringBuffer    result;
        int                i;

        _text.append( '"' );
        for( i = 0 ; i < url.length() ; ++i ) {
            if ( url.charAt( i ) == '"' ||  url.charAt( i ) < 0x20 || url.charAt( i ) > 0x7F )
                _text.append( "%" ).append( Integer.toHexString( url.charAt( i ) ) );
            else
                _text.append( url.charAt( i ) );
        }
        _text.append( '"' );
    }


    /**
     * Escapes a string so it may be printed as text content or attribute
     * value. Non printable characters are escaped using character references.
     * Where the format specifies a deault entity reference, that reference
     * is used (e.g. <tt>&amp;lt;</tt>).
     *
     * @param source The string to escape
     * @return The escaped string
     */
    protected String escape( String source )
    {
        StringBuffer    result;
        int             i;
        char            ch;
        String          charRef;

        result = new StringBuffer( source.length() );
        for ( i = 0 ; i < source.length() ; ++i )  {
            ch = source.charAt( i );
	    // If the character is not printable, print as character reference.
	    // Non printables are below ASCII space but not tab or line
	    // terminator, ASCII delete, or above a certain Unicode threshold.
	    if ( ( ch < ' ' && ch != '\t' && ch != '\n' && ch != '\r' ) ||
		 ch > _lastPrintable || ch == 0xF7 )
		    result.append( "&#" ).append( Integer.toString( ch ) ).append( ';' );
	    else {
		    // If there is a suitable entity reference for this
		    // character, print it. The list of available entity
		    // references is almost but not identical between
		    // XML and HTML.
		    charRef = getEntityRef( ch );
		    if ( charRef == null )
			result.append( ch );
		    else
			result.append( '&' ).append( charRef ).append( ';' );
	    }
        }
        return result.toString();
    }


    /**
     * Return the state of the current element, or null
     * if not within any element (e.g. before entering
     * root element).
     *
     * @return Current element state, or null
     */
    protected ElementState getElementState()
    {
	if ( _elementStateCount == 0 )
	    return null;
	else
	    return _elementStates[ _elementStateCount - 1 ];
    }


    //--------------------------------//
    // Element state handling methods //
    //--------------------------------//


    /**
     * Enter a new element state for the specified element.
     * Tag name and space preserving is specified, element
     * state is initially empty.
     *
     * @return Current element state, or null
     */
    protected ElementState enterElementState( String tagName, boolean preserveSpace )
    {
	ElementState state;

	if ( _elementStateCount == _elementStates.length ) {
	    ElementState[] newStates;
	    int            i;

	    // Need to create a larger array of states.
	    // This does not happen often, unless the document
	    // is really deep.
	    newStates = new ElementState[ _elementStates.length + 5 ];
	    System.arraycopy( _elementStates, 0, newStates, 0, _elementStates.length );
	    _elementStates = newStates;
	    for ( i = _elementStateCount ; i < _elementStates.length ; ++i )
		_elementStates[ i ] = new ElementState();
	}
	state = _elementStates[ _elementStateCount ];
	state.tagName = tagName;
	state.preserveSpace = preserveSpace;
	state.empty = true;
	state.afterElement = false;
	++_elementStateCount;
	return state;
    }


    /**
     * Leave the current element state and return to the
     * state of the parent element, or no state if this
     * is the root element.
     *
     * @return Previous element state, or null
     */
    protected ElementState leaveElementState()
    {
	if ( _elementStateCount > 1 ) {
	    -- _elementStateCount;
	    return _elementStates[ _elementStateCount - 1 ];
	} else if ( _elementStateCount == 1 ) {
	    -- _elementStateCount;
	    return null;
	} else
	    return null;
    }


    //--------------------------------//
    // Constructor and initialization //
    //--------------------------------//


    /**
     * Initialize the printer with the specified writer and output format.
     * Must be called before calling any of the print methods.
     *
     * @param writer The writer to use
     * @param format The output format
     */
    public synchronized void init( Writer writer, OutputFormat format )
    {
	if ( format == null )
	    throw new NullPointerException( "Argument 'format' is null." );
	_format = format;
	if ( writer == null )
	    throw new NullPointerException( "Argument 'format' is null." );
	_writer = new BufferedWriter( writer );

	// Determine the last printable character based on the output format
	_lastPrintable = _format.getLastPrintable();

	// Initialize everything for a first/second run.
	_line = new StringBuffer( 80 );
	_text = new StringBuffer( 20 );
	_spaces = 0;
	_thisIndent = _nextIndent = 0;
	_exception = null;
	_elementStateCount = 0;
	_started = false;
    }


    /**
     * Initialize the printer with the specified output stream and output format.
     * Must be called before calling any of the print methods.
     *
     * @param output The output stream to use
     * @param format The output format
     * @throws UnsupportedEncodingException The encoding specified
     *   in the output format is not supported
     */
    public synchronized void init( OutputStream output, OutputFormat format )
        throws UnsupportedEncodingException
    {
	String encoding;

	encoding = ( format.getEncoding() == null ? "ASCII" : format.getEncoding() );
	init( new OutputStreamWriter( output, encoding ), format );
    }


    /**
     * Protected constructor can only be used by derived class.
     * Must initialize the printer before printing any document,
     * see {@link #init}.
     */
    protected BasePrinter()
    {
	int i;

	for ( i = 0 ; i < _elementStates.length ; ++i )
	    _elementStates[ i ] = new ElementState();
    }


    /**
     * Identifies the last printable character in the Unicode range
     * that is supported by the encoding used with this printer.
     * For 8-bit encodings this will be either 0x7E or 0xFF.
     * For 16-bit encodings this will be 0xFFFF. Characters that are
     * not printable will be escaped using character references.
     */
    private int              _lastPrintable = 0x7E;


    /**
     * The output format associated with this printer. This will never
     * be a null reference. If no format was passed to the constructor,
     * the default one for this document type will be used. The format
     * object is never changed by the printer.
     */
    protected OutputFormat  _format;


    /**
     * The writer to which the document is written.
     */
    private Writer          _writer;


    /**
     * Holds the currently accumulating text line. This buffer will constantly
     * be reused by deleting its contents instead of reallocating it.
     */
    private StringBuffer    _line;


    /**
     * Holds the currently accumulating text that follows {@link #_line}.
     * When the end of the part is identified by a call to {@link #printSpace}
     * or {@link #printBreak}, this part is added to the accumulated line.
     */
    private StringBuffer    _text;


    /**
     * Counts how many white spaces come between the accumulated line and the
     * current accumulated text. Multiple spaces at the end of the a line
     * will not be printed.
     */
    private int             _spaces;


    /**
     * Holds the indentation for the current line that is now accumulating in
     * memory and will be sent for printing shortly.
     */
    private int             _thisIndent;
    
    
    /**
     * Holds the indentation for the next line to be printed. After this line is
     * printed, {@link #_nextIndent} is assigned to {@link #_thisIndent}.
     */
    private int             _nextIndent;


    /**
     * Holds the exception thrown by the printer.  Exceptions do not cause
     * the printer to quit, but are held and one is thrown at the end.
     */
    protected IOException   _exception;


    /**
     * Holds array of all element states that have been entered.
     * The array is automatically resized. When leaving an element,
     * it's state is not removed but reused when later returning
     * to the same nesting level.
     */
    private ElementState[]  _elementStates = new ElementState[ 5 ];


    /**
     * The index of the next state to place in the array,
     * or one plus the index of the current state. When zero,
     * we are in no state.
     */
    private int             _elementStateCount;


    /**
     * Vector holding comments and PIs that come before the root
     * element (even after it), see {@link #printPreRoot}.
     */
    private Vector          _preRoot;


    /**
     * If the document has been started (header printed), this
     * flag is set to true so it's not started twice.
     */
    protected boolean       _started;

    
}
