/*
* last update: 2008-03-07
*/

editAreaLoader.load_syntax["jsp"] = {
	'DISPLAY_NAME' : 'JSP'
	,'COMMENT_SINGLE' : {1: '//'}
	,'COMMENT_MULTI' : {'<!--' : '-->', '<%--' : '--%>', '/**' : '*/'}
	,'QUOTEMARKS' : {1: "'", 2: '"'}
	,'KEYWORD_CASE_SENSITIVE' : true
	,'KEYWORDS' : {
 		'keywords' : [
			'abstract', 'continue', 'for', 'new', 'switch',
			'assert', 'default', 'goto', 'package', 'synchronized',
			'boolean', 'do', 'if', 'private', 'this',
			'break', 'double', 'implements', 'protected', 'throw',
			'byte', 'else', 'import', 'public', 'throws',
			'case', 'enum', 'instanceof', 'return', 'transient',
			'catch', 'extends', 'int', 'short', 'try',
			'char', 'final', 'interface', 'static', 'void',
			'class', 'finally', 'long', 'strictfp', 'volatile',
			'float', 'native', 'super', 'while'
		]
    	,'functions' : [
			'toString', 'equals', 'equalsIgnoreCase', 'compareTo', 'compareToIgnoreCare',
			'hashCode', 'wait', 'indexOf', 'lastIndexOf', 'trim', 'valueOf',
			'startsWith', 'endsWith', 'substring', 'get', 'put', 'add', 'addAll'
		]
		,'literals' : [
			'null', 'true', 'false'
		]
	}
	,'OPERATORS' :[
		'+', '-', '/', '*', '=', '<', '>', '%', '!'
	]
	,'DELIMITERS' :[
		'{', '}'
	]
	,'REGEXPS' : {
		'doctype' : {
			'search' : '()(<!DOCTYPE[^>]*>)()'
			,'class' : 'doctype'
			,'modifiers' : ''
			,'execute' : 'before' // before or after
		}
		,'tags' : {
			'search' : '(<)(/?[a-z][^ \r\n\t>]*)([^>]*>)'
			,'class' : 'tags'
			,'modifiers' : 'gi'
			,'execute' : 'before' // before or after
		}
		,'attributes' : {
			'search' : '( |\n|\r|\t)([^ \r\n\t=]+)(=)'
			,'class' : 'attributes'
			,'modifiers' : 'g'
			,'execute' : 'before' // before or after
		}
	}
	,'STYLES' : {
		'COMMENTS': 'color: #AAAAAA;'
		,'QUOTESMARKS': 'color: #6381F8;'
		,'KEYWORDS' : {
			'keywords' : 'color: #48BDDF;'
			,'functions' : 'color: #2B60FF;'
			,'literals' : 'color: #60CA00;'
		}
		,'OPERATORS' : 'color: #E775F0;'
		,'DELIMITERS' : ''
		,'REGEXPS' : {
			'attributes': 'color: #B1AC41;'
			,'tags': 'color: #E62253;'
			,'doctype': 'color: #8DCFB5;'
			,'test': 'color: #00FF00;'
		}
	}
};
