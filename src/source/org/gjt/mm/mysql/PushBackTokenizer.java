package source.org.gjt.mm.mysql;

import java.util.StringTokenizer;

public class PushBackTokenizer extends StringTokenizer
{
	private boolean _push_back = false; // are we pushed back?
	
	private String  _CurrentToken = null;
	
	/**
	 * Create a new StringTokenizer that allows for push back.
	 */
	
	public PushBackTokenizer(String S, String Tokens, boolean return_tokens)
	{
		super(S, Tokens, return_tokens);
	}
	
	/**
	 * The next token to be read will
	 * be the current token
	 */
	
	public synchronized void pushBack()
	{
		_push_back = true;
	}
	
	/**
	 * Does this tokenizer contain any more
	 * tokens?
	 */
	
	public synchronized boolean hasMoreTokens()
	{
		if (_push_back) {
			return true;
		}
		else {
			return super.hasMoreTokens();
		}
	}
	
	/**
	 * Does this tokenizer have any more elements?
	 */
	
	public synchronized boolean hasMoreElements()
	{
		if (_push_back) {
			return true;
		}
		else {
			return super.hasMoreElements();
		}
	}
	
	/**
	 * Retrieve the next token from this
	 * tokenizer.
	 */
	
	public synchronized String nextToken()
	{
		if (_push_back) {
			_push_back = false;
			return _CurrentToken;
		}
		else {
			_CurrentToken = super.nextToken();
			
			return _CurrentToken;
		}
	}
};