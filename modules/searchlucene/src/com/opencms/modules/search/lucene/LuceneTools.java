package com.opencms.modules.search.lucene;

/*

Lucene-Highlighting – Lucene utilities to highlight terms in texts
Copyright (C) 2001 Maik Schreiber

This library is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2.1 of the License, or
(at your option) any later version.

This library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

*/

import java.io.*;
import java.util.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;


/**
 * Contains miscellaneous utility methods for use with Lucene.
 *
 * @version $Id: LuceneTools.java,v 1.1 2002/03/01 13:30:35 g.huhn Exp $
 * @author Maik Schreiber (mailto: bZ@iq-computing.de)
 */
public final class LuceneTools
{
  /** LuceneTools must not be instantiated directly. */
  private LuceneTools() {}


  /**
   * Highlights a text in accordance to a given query.
   *
   * @param text        text to highlight terms in
   * @param highlighter TermHighlighter to use to highlight terms in the text
   * @param query       Query which contains the terms to be highlighted in the text
   * @param analyzer    Analyzer used to construct the Query
   *
   * @return highlighted text
   */
  public static final String highlightTerms(String text, TermHighlighter highlighter, Query query,
    Analyzer analyzer) throws IOException
  {
    StringBuffer newText = new StringBuffer();
    TokenStream stream = null;

    try
    {
      HashSet terms = new HashSet();
      org.apache.lucene.analysis.Token token;
      String tokenText;
      int startOffset;
      int endOffset;
      int lastEndOffset = 0;

      // get terms in query
      getTerms(query, terms, false);

      stream = analyzer.tokenStream(new StringReader(text));
      while ((token = stream.next()) != null)
      {
        startOffset = token.startOffset();
        endOffset = token.endOffset();
        tokenText = text.substring(startOffset, endOffset);

        // append text between end of last token (or beginning of text) and start of current token
        if (startOffset > lastEndOffset)
          newText.append(text.substring(lastEndOffset, startOffset));

        // does query contain current token?
        if (terms.contains(token.termText()))
          newText.append(highlighter.highlightTerm(tokenText));
        else
          newText.append(tokenText);

        lastEndOffset = endOffset;
      }

      // append text after end of last token
      if (lastEndOffset < text.length())
        newText.append(text.substring(lastEndOffset));

      return newText.toString();
    }
    finally
    {
      if (stream != null)
      {
        try
        {
          stream.close();
        }
        catch (Exception e) {}
      }
    }
  }

  /**
   * Extracts all term texts of a given Query. Term texts will be returned in lower-case.
   *
   * @param query      Query to extract term texts from
   * @param terms      HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   */
  public static final void getTerms(Query query, HashSet terms, boolean prohibited)
    throws IOException
  {
    if (query instanceof BooleanQuery)
      getTermsFromBooleanQuery((BooleanQuery) query, terms, prohibited);
    else if (query instanceof PhraseQuery)
      getTermsFromPhraseQuery((PhraseQuery) query, terms);
    else if (query instanceof TermQuery)
      getTermsFromTermQuery((TermQuery) query, terms);
    else if (query instanceof PrefixQuery)
      getTermsFromPrefixQuery((PrefixQuery) query, terms, prohibited);
    else if (query instanceof RangeQuery)
      getTermsFromRangeQuery((RangeQuery) query, terms, prohibited);
    else if (query instanceof MultiTermQuery)
      getTermsFromMultiTermQuery((MultiTermQuery) query, terms, prohibited);
  }

  /**
   * Extracts all term texts of a given BooleanQuery. Term texts will be returned in lower-case.
   *
   * @param query      BooleanQuery to extract term texts from
   * @param terms      HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   */
  private static final void getTermsFromBooleanQuery(BooleanQuery query, HashSet terms,
    boolean prohibited) throws IOException
  {
    BooleanClause[] queryClauses = query.getClauses();
    int i;

    for (i = 0; i < queryClauses.length; i++)
    {
      if (prohibited || !queryClauses[i].prohibited)
        getTerms(queryClauses[i].query, terms, prohibited);
    }
  }

  /**
   * Extracts all term texts of a given PhraseQuery. Term texts will be returned in lower-case.
   *
   * @param query PhraseQuery to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   */
  private static final void getTermsFromPhraseQuery(PhraseQuery query, HashSet terms)
  {
    Term[] queryTerms = query.getTerms();
    int i;

    for (i = 0; i < queryTerms.length; i++)
      terms.add(getTermsFromTerm(queryTerms[i]));
  }

  /**
   * Extracts all term texts of a given TermQuery. Term texts will be returned in lower-case.
   *
   * @param query TermQuery to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   */
  private static final void getTermsFromTermQuery(TermQuery query, HashSet terms)
  {
    terms.add(getTermsFromTerm(query.getTerm()));
  }

  /**
   * Extracts all term texts of a given MultiTermQuery. Term texts will be returned in lower-case.
   *
   * @param query      MultiTermQuery to extract term texts from
   * @param terms      HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   */
  private static final void getTermsFromMultiTermQuery(MultiTermQuery query, HashSet terms,
    boolean prohibited) throws IOException
  {
    getTerms(query.getQuery(), terms, prohibited);
  }

  /**
   * Extracts all term texts of a given PrefixQuery. Term texts will be returned in lower-case.
   *
   * @param query      PrefixQuery to extract term texts from
   * @param terms      HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   */
  private static final void getTermsFromPrefixQuery(PrefixQuery query, HashSet terms,
    boolean prohibited) throws IOException
  {
    getTerms(query.getQuery(), terms, prohibited);
  }

  /**
   * Extracts all term texts of a given RangeQuery. Term texts will be returned in lower-case.
   *
   * @param query      RangeQuery to extract term texts from
   * @param terms      HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   */
  private static final void getTermsFromRangeQuery(RangeQuery query, HashSet terms,
    boolean prohibited) throws IOException
  {
    getTerms(query.getQuery(), terms, prohibited);
  }

  /**
   * Extracts the term of a given Term. The term will be returned in lower-case.
   *
   * @param term Term to extract term from
   *
   * @return the Term's term text
   */
  private static final String getTermsFromTerm(Term term)
  {
    return term.text().toLowerCase();
  }
}