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
 * $Id: Printer.java,v 1.1 2000/01/13 13:44:20 a.lucas Exp $
 */


package source.org.openxml.printer;


import java.io.Writer;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.DocumentHandler;


/**
 * Interface for a DOM printer implementation, factory for DOM and SAX
 * printers, and static methods for printing DOM documents.
 * <p>
 * To print a document using SAX events, create a compatible printer
 * using {@link #makeSAXPrinter} and pass it around as a {@link
 * DocumentHandler}. If an I/O error occurs while printing, it will
 * be thrown by {@link DocumentHandler#endDocument}.
 * <p>
 * To print a DOM document or DOM element, create a compatible printer
 * using {@link #makePrinter} and call it's {@link #print(Document)}
 * or {@link #print(Element)} methods. Both methods would produce a
 * full XML document, to print only the portion of the document use
 * {@link OutputFormat#setOmitXMLDeclaration} and specify no document
 * type.
 * <p>
 * The convenience method {@link #print(Document,Writer,OutputFormat)}
 * creates a printer and calls {@link #print(Document)} on that printer.
 * <p>
 * The {@link OutputFormat} dictates what underlying printer is used
 * to print the document based on the specified method. If the output
 * format or method are missing, the default is an XML printer with
 * UTF8 encoding.
 * 
 *
 * @version $Revision: 1.1 $ $Date: 2000/01/13 13:44:20 $
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see DocumentHandler
 * @see OutputFormat
 */
public abstract class Printer
{


    /**
     * Prints the DOM element. Throws an exception only if
     * an I/O exception occured while printing.
     *
     * @param elem The element to print
     * @throws IOException An I/O exception occured while
     *   printing
     */
    public abstract void print( Element elem )
        throws IOException;


    /**
     * Prints the DOM document. Throws an exception only if
     * an I/O exception occured while printing.
     *
     * @param doc The document to print
     * @throws IOException An I/O exception occured while
     *   printing
     */
    public abstract void print( Document doc )
        throws IOException;


    /**
     * Creates a compatible printer for the specified writer
     * and output format. If the output format is missing,
     * the default is an XML format with UTF8 encoding.
     *
     * @param writer The writer
     * @param format The output format
     * @return A compatible printer
     */
    public static Printer makePrinter( Writer writer, OutputFormat format )
    {
	BasePrinter printer;

	printer = makeBasePrinter( format );
	printer.init( writer, format );
	return printer;
    }


    /**
     * Creates a compatible printer for the specified output stream
     * and output format. If the output format is missing, the default
     * is an XML format with UTF8 encoding.
     *
     * @param output The output stream
     * @param format The output format
     * @return A compatible printer
     * @throws UnsupportedEncodingException Encoding specified
     *   in the output format is not supported
     */
    public static Printer makePrinter( OutputStream output, OutputFormat format )
        throws UnsupportedEncodingException
    {
	BasePrinter printer;

	printer = makeBasePrinter( format );
	printer.init( output, format );
	return printer;
    }


    /**
     * Creates a compatible SAX printer for the specified writer
     * and output format. If the output format is missing, the default
     * is an XML format with UTF8 encoding.
     *
     * @param writer The writer
     * @param format The output format
     * @return A compatible SAX printer
     */
    public static DocumentHandler makeSAXPrinter( Writer writer, OutputFormat format )
    {
	BasePrinter printer;

	printer = makeBasePrinter( format );
	printer.init( writer, format );
	return printer;
    }


    /**
     * Creates a compatible SAX printer for the specified output stream
     * and output format. If the output format is missing, the default
     * is an XML format with UTF8 encoding.
     *
     * @param output The output stream
     * @param format The output format
     * @return A compatible SAX printer
     * @throws UnsupportedEncodingException Encoding specified
     *   in the output format is not supported
     */
    public static DocumentHandler makeSAXPrinter( OutputStream output, OutputFormat format )
        throws UnsupportedEncodingException
    {
	BasePrinter printer;

	printer = makeBasePrinter( format );
	printer.init( output, format );
	return printer;
    }


    /**
     * Convenience method prints the specified document to
     * the writer using the specified output format.
     * <p>
     * Equivalent to calling {@link #print(Document)} on
     * a compatible DOM printer.
     *
     * @param doc The document to print
     * @param writer The writer
     * @param format The output format
     * @throws IOException An I/O exception occured while printing
     * @throws UnsupportedEncodingException Encoding specified
     *   in the output format is not supported
     */
    public static void print( Document doc, Writer writer, OutputFormat format )
        throws IOException
    {
	BasePrinter printer;

	if ( format == null )
	    format = new OutputFormat( doc );
	printer = makeBasePrinter( format );
	printer.init( writer, format );
	printer.print( doc );
    }


    /**
     * Convenience method prints the specified document to
     * the output stream using the specified output format.
     * <p>
     * Equivalent to calling {@link #print(Document)} on
     * a compatible DOM printer.
     *
     * @param doc The document to print
     * @param output The output stream
     * @param format The output format
     * @throws IOException An I/O exception occured while printing
     */
    public static void print( Document doc, OutputStream output, OutputFormat format )
        throws UnsupportedEncodingException, IOException
    {
	BasePrinter printer;

	if ( format == null )
	    format = new OutputFormat( doc );
	printer = makeBasePrinter( format );
	printer.init( output, format );
	printer.print( doc );
    }


    private static BasePrinter makeBasePrinter( OutputFormat format )
    {
	BasePrinter printer;

	if ( format == null ) {
	    format = new OutputFormat( "xml", "UTF8", false );
	    printer = new XMLPrinter();
	} else {
	    // if ( format.getMethod().equalsIgnoreCase( "html" ) )
		// printer = new HTMLPrinter( false );
	    // else
	    // if ( format.getMethod().equalsIgnoreCase( "xhtml" ) )
		// printer = new HTMLPrinter( true );
	    // else
		printer = new XMLPrinter();
	}
	return printer;
    }


}





