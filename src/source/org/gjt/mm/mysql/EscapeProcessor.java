/*
 * MM JDBC Drivers for MySQL
 *
 * $Id: EscapeProcessor.java,v 1.1 2000/01/24 14:35:59 m.emmerich Exp $
 *
 * Copyright (C) 1998 Mark Matthews <mmatthew@worldserver.com>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * See the COPYING file located in the top-level-directory of
 * the archive of this library for complete text of license.
 */

/**
 * EscapeProcessor performs all escape code processing as outlined
 * in the JDBC spec by JavaSoft.
 *
 * @author Mark Matthews <mmatthew@worldserver.com>
 * @version $Id: EscapeProcessor.java,v 1.1 2000/01/24 14:35:59 m.emmerich Exp $
 */

package source.org.gjt.mm.mysql;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Stack;

public class EscapeProcessor
{	
    /**
     * Escape process one string
     *
     * @param SQL the SQL to escape process.
     * @return the SQL after it has been escape processed.      
     */
    
    public synchronized String escapeSQL(String SQL) throws java.sql.SQLException
    {
	boolean replaceEscapeSequence = false;
	String EscapeSequence = null;
	StringBuffer NewSQL = new StringBuffer();
	
	boolean inBraces = false;
	boolean inQuotes = false;

        boolean escaped = false;
        boolean unrecognizedEscape = false;
        String OpeningBrace="";
	
	if (SQL == null) {
	    return null;
	}

	/*
	 * Short circuit this code if we don't have a matching pair of
	 * "{}". - Suggested by Ryan Gustafason
	 */
	
	int begin_brace = SQL.indexOf("{");
	int next_end_brace = SQL.indexOf("}", begin_brace);
	
	if (next_end_brace == -1) {
		return SQL;
	}
		            
	PushBackTokenizer PBT = new PushBackTokenizer(SQL, "{}'", true);

	while (PBT.hasMoreTokens()) {
	    String Token = PBT.nextToken();
	    
	    if (Token.startsWith("{")) {
                if (escaped) {
		    NewSQL.append(Token);
		    escaped = false;
                } else {
		    NewSQL.append(OpeningBrace);
		    inBraces = true;
		    OpeningBrace = Token;
                }
	    }
	    else if (Token.startsWith("}")) {
                if (escaped) {
		    NewSQL.append(Token);
		    escaped = false;
                } else {
		    if (inBraces == false) {
			NewSQL.append(Token);
		    } else {
			inBraces = false;
			if (!OpeningBrace.equals("")) {
			    NewSQL.append(OpeningBrace);
			    NewSQL.append(Token);
			    OpeningBrace="";
			}
			if (unrecognizedEscape) {
			    NewSQL.append(Token);
			    unrecognizedEscape = false;
			}
		    }
                }
	    }
	    else if (Token.startsWith("'")) {
		if (escaped) {
		    NewSQL.append(Token);
		    escaped = false;
		}
		else {
		    NewSQL.append(Token);
		    inQuotes = !inQuotes;
		}
	    }
	    else {
		if (inBraces && !inQuotes) {
		    
		    /*
		     * Process the escape code
		     */
		    
		    if (Token.startsWith("escape")) { 
			try {
			    StringTokenizer ST = new StringTokenizer(Token, " '");
			    ST.nextToken(); // eat the "escape" token
			    EscapeSequence = ST.nextToken();
			    if (EscapeSequence.length() < 3) {
				throw new java.sql.SQLException("Syntax error for escape sequence '" + Token + "'", "42000"); 
			    }
			    EscapeSequence = EscapeSequence.substring( 1, EscapeSequence.length() - 1);
			    
			    replaceEscapeSequence = true;
			}
			catch (java.util.NoSuchElementException E) {
			    throw new java.sql.SQLException("Syntax error for escape sequence '" + Token + "'", "42000");
			}                                                     
		    }
		    else if (Token.startsWith("fn")) {
			
			// just pass functions right to the DB
			int start_pos = Token.indexOf("fn ") + 3;   
                        int end_pos = Token.length();               
			
                        NewSQL.append(Token.substring(start_pos, end_pos));
			
			try {
			    NewSQL.append(parseComplexArgument(PBT));
			}
			catch (java.util.NoSuchElementException NSE) {
			    throw new java.sql.SQLException("Syntax error for FN escape code", "42000");
			}
		    }
		    else if (Token.startsWith("d")) {
			
			String Argument = "";
			
			try {
			    Argument = parseArgument(PBT);
			}
			catch (java.util.NoSuchElementException NSE) {
			    throw new java.sql.SQLException("Illegal argument for DATE escape code '" + Argument + "'", "42000");
			}
			
			try {
			    StringTokenizer ST = new StringTokenizer(Argument, " -");
			    
			    String YYYY = ST.nextToken();
			    String MM   = ST.nextToken();
			    String DD   = ST.nextToken();
			    
			    String DateString = "'" + YYYY + "-" + MM + "-" + DD + "'";
			    
			    NewSQL.append(DateString);
			}
			catch (java.util.NoSuchElementException E) {
			    throw new java.sql.SQLException("Syntax error for DATE escape sequence '" + Argument + "'", "42000");
			}
		    }
		    else if (Token.startsWith("ts")) {

			String Argument = "";
			
			try {
			    Argument = parseArgument(PBT);
			}
			catch (java.util.NoSuchElementException NSE) {
			    throw new java.sql.SQLException("Illegal argument for TIMESTAMP escape code '" + Argument + "'", "42000");
			}
			
			try {
			    StringTokenizer ST = new StringTokenizer(Argument, " .-:");
			    
			    String YYYY = ST.nextToken();
			    String MM   = ST.nextToken();                     
			    String DD   = ST.nextToken();                      
			    String HH   = ST.nextToken();                     
			    String Mm   = ST.nextToken(); 	    
			    String SS   = ST.nextToken();
		
				
			    /*
			     * For now, we get the fractional seconds
			     * part, but we don't use it, as MySQL doesn't
			     * support it in it's TIMESTAMP data type
			     */

			    String F = "";
							
			    if (ST.hasMoreTokens()) {
				F = ST.nextToken();
			    }
							
			    /*
			     * Use the full format because number format
			     * will not work for "between" clauses.
			     *
			     * Ref. Mysql Docs
			     * 
			     * You can specify DATETIME, DATE and TIMESTAMP values 
			     * using any of a common set of formats: 
			     * 
			     * As a string in either 'YYYY-MM-DD HH:MM:SS' or
			     * 'YY-MM-DD HH:MM:SS' format. 
			     *
			     * Thanks to Craig Longman for pointing out this bug
			     */							
			    NewSQL.append("'").append(YYYY).append("-").append(MM).append("-").append(DD).append(" ").append(HH).append(":").append(Mm).append(":").append(SS).append("'");
			}
			catch (java.util.NoSuchElementException E) {
			    throw new java.sql.SQLException("Syntax error for TIMESTAMP escape sequence '" + Argument + "'", "42000");
			}
		    }
		    else if (Token.startsWith("t")) {

			String Argument = "";
       
			try {
			    Argument = parseArgument(PBT);
			}
			catch (java.util.NoSuchElementException NSE) {
			    throw new java.sql.SQLException("Illegal argument for TIME escape code '" + Argument + "'", "42000");
			}

			try {
			    StringTokenizer ST = new StringTokenizer(Argument, " ':");
			                                                 
			    String HH   = ST.nextToken();                     
			    String MM   = ST.nextToken();      
			    String SS   = ST.nextToken();		    
			    
			    String TimeString = "'" + HH + ":" + MM + ":" +SS + "'";
							                      
			    NewSQL.append(TimeString);
			}
			catch (java.util.NoSuchElementException E) {
			    throw new java.sql.SQLException("Syntax error for escape sequence '" + Argument + "'", "42000");
			}
		    }
		    else if (Token.startsWith("call") ||
			     Token.startsWith("? = call")) {
			throw new java.sql.SQLException("Stored procedures not supported: " + Token, "S1C00");
		    }
		    else if (Token.startsWith("oj")) {
			// MySQL already handles this escape sequence
			// because of ODBC. Cool.

			try {
			    NewSQL.append(parseComplexArgument(PBT));
			}
			catch (java.util.NoSuchElementException NSE) {
			    throw new java.sql.SQLException("Syntax error for OJ escape code", "42000");
			}
		    }
		    else {
                        NewSQL.append(OpeningBrace);
                        NewSQL.append(Token);
                        unrecognizedEscape = true;
                        OpeningBrace = "";
		    }  	
                    OpeningBrace="";
		}
		else {
		    NewSQL.append(Token);
		}
	    }

	    if (Token.endsWith("\\")) {
		escaped = true;
	    }
	    
	}
        NewSQL.append(OpeningBrace);

	String EscapedSQL = NewSQL.toString();
		            
	if (replaceEscapeSequence) {
	    String CurrentSQL = EscapedSQL;
	    while (CurrentSQL.indexOf(EscapeSequence) != -1) {
		int escapePos = CurrentSQL.indexOf(EscapeSequence);
		String LHS = CurrentSQL.substring(0, escapePos);
		String RHS = CurrentSQL.substring(escapePos + 1, CurrentSQL.length());
		CurrentSQL = LHS + "\\" + RHS;
	    }
	    EscapedSQL = CurrentSQL;      
	}

	// Do we need to do the concatenation operator?
	if (EscapedSQL.indexOf("||") != -1) {
	    EscapedSQL = doConcat(EscapedSQL);
	}

	return EscapedSQL;
    }
          
    /**
     * Do concatenation for the SQL operator "||" because
     * MySQL doesn't support it, but some IDEs (i.e. VisualAge)
     * use it.
     *
     * @param SQL A String to do concatenation operations on
     */

    static String doConcat(String SQL)
    {
	Vector TokenList = new Vector();
	StringTokenizer ST = new StringTokenizer(SQL, " '", true);
	
	boolean inquotes = false;
	StringBuffer QuotedString = null;

	while (ST.hasMoreTokens()) {
	    String T = ST.nextToken();
	    if (T.equals("'")) {
		if (inquotes == true) {
		    inquotes = false;
		    Token Tok = new Token(QuotedString.toString(), true);
		    TokenList.addElement(Tok);
		}
		else {
		    inquotes = true;
		    QuotedString = new StringBuffer();
		}
	    }
	    else {
		if (inquotes) {
		    QuotedString.append(T);
		}
		else {
		    Token Tok = new Token(T, false);

		    TokenList.addElement(Tok);
		}
	    }
	}

	// Now go through and find what we need to concatenate

	int pos = 0;

	int length = TokenList.size();
	Stack ToDo = new Stack();

	while (pos < length) {
	    Token T1 = (Token)TokenList.elementAt(pos);
	    if (T1.Value.equals("||")) {
		Token Pre = (Token)ToDo.pop();
		pos++;
		Token Post = (Token)TokenList.elementAt(pos);
	
		Token Concat = new Token(Pre.Value + Post.Value, true);
		ToDo.push(Concat);
		pos++;
	    }
	    else {
		ToDo.push(T1);
		pos++;
	    }
		    
	}

	length = ToDo.size();
	StringBuffer NewQuery = new StringBuffer();

	for (int i = 0; i < length; i++) {
	    Token T = (Token)ToDo.elementAt(i);
	    if (T.quoted) {
		NewQuery.append("'");
		NewQuery.append(T.Value);
		NewQuery.append("'");
	    }
	    else {
		NewQuery.append(T.Value);
	    }
	}
	
	return NewQuery.toString();
    }


    /**
     * Given the current tokenizer, assemble an escape code argument
     */

    private final static String parseArgument(PushBackTokenizer Tokenizer)
    throws java.util.NoSuchElementException
    {
	StringBuffer Argument = new StringBuffer();
	boolean done_parsing = false;
	boolean seen_one_quote = false;
	
	while (!done_parsing) {
	    String Tok = Tokenizer.nextToken();
	    if (Tok.equals("'")) {
		if (seen_one_quote) {
		    done_parsing = true;
		}
		else {
		    seen_one_quote = true;
		}
	    }
	    else {
		Argument.append(Tok);
	    }
	}
	
	return Argument.toString();
    }

    
    /**
     * Given the current tokenizer, assemble an escape code argument
     */

    private final static String parseComplexArgument(PushBackTokenizer Tokenizer)
	throws java.util.NoSuchElementException
    {
	StringBuffer Argument = new StringBuffer();

	boolean done_parsing = false;
	boolean in_quotes = false;
	
	while (!done_parsing) {
	    String Tok = Tokenizer.nextToken();
    
	    if (Tok.equals("'")) {
		in_quotes = !in_quotes;
		Argument.append(Tok);
	    }
	    else {
		if (!in_quotes && Tok.equals("}")) {
		    done_parsing = true;
		    Tokenizer.pushBack();
		}
		else {
		    Argument.append(Tok);
		}
	    }
	}
	
	return Argument.toString();
    }
};
