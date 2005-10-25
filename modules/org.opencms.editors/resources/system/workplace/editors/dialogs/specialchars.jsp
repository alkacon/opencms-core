<%@ page import="org.opencms.workplace.*" %><%	

	// initialize the workplace class
	CmsDialog wp = new CmsDialog(pageContext, request, response);
	wp.setParamIsPopup("true");
	int buttonStyle = 0;
	
	String dialogTitle = wp.key("label.specialchar");
	
%><%= wp.htmlStart(null, dialogTitle) %>

<script type="text/javascript">
<!--
function insertChar(thechar) {
	window.opener.insertHtml(thechar);
	window.close();
}
//-->
</script>


<style type="text/css">
<!--
	a {
		color: #000000;
		text-decoration: none;
		font-family: Arial, Helvetica, Sans-Serif; 
		font-size: 14px;
		font-weight: bold;
	}
	
	a:hover {	
		color: #ff0000;
	}
	
	span {
		font-family: Arial;
		font-size: 12px;
		font-weight: bold;
	}
	
	table.chartable {
		background-color: #ffffff;
		border-collapse: collapse;
		border: 1px solid #000000;
		margin: 0;
		padding: 0;
		empty-cells: show;
	}
	
	td.char {
		border: 1px solid #000000;
		text-align: center;
		padding: 4px;
	}
	
	td.charover {
		border: 1px solid #000000;
		text-align: center;
		padding: 4px;
		background-color: #E0E0E0;
	}
	
//-->
</style>    

<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(dialogTitle) %>

<table border="0" cellspacing="0"  cellpadding="0" class="chartable">
	<colgroup>
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">
		<col width="25">          
	</colgroup>
	<tr>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&quot; ')">&quot;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&amp; ')">&amp;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&lt; ')">&lt;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&gt; ')">&gt;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&iexcl; ')">&iexcl;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&cent; ')">&cent;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&pound; ')">&pound;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&curren; ')">&curren;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&euro; ')">&euro;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&yen; ')">&yen;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&brvbar; ')">&brvbar;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&sect; ')">&sect;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&uml; ')">&uml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&copy; ')">&copy;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ordf; ')">&ordf;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&laquo; ')">&laquo;</a></td>
	</tr>
	<tr>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&not; ')">&not;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&reg; ')">&reg;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&macr; ')">&macr;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&deg; ')">&deg;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&plusmn; ')">&plusmn;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&sup2; ')">&sup2;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&sup3; ')">&sup3;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&acute; ')">&acute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&micro; ')">&micro;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&para; ')">&para;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&middot; ')">&middot;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&cedil; ')">&cedil;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&sup1; ')">&sup1;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ordm; ')">&ordm;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&raquo; ')">&raquo;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&frac14; ')">&frac14;</a></td>
	</tr>
	<tr>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&frac12; ')">&frac12;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&frac34; ')">&frac34;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&iquest; ')">&iquest;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Agrave; ')">&Agrave;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Aacute; ')">&Aacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Acirc; ')">&Acirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Atilde; ')">&Atilde;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Auml; ')">&Auml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Aring; ')">&Aring;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&AElig; ')">&AElig;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Ccedil; ')">&Ccedil;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Egrave; ')">&Egrave;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Eacute; ')">&Eacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Ecirc; ')">&Ecirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Euml; ')">&Euml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Igrave; ')">&Igrave;</a></td>
	</tr>
	<tr>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Iacute; ')">&Iacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Icirc; ')">&Icirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Iuml; ')">&Iuml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ETH; ')">&ETH;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Ntilde; ')">&Ntilde;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Ograve; ')">&Ograve;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Oacute; ')">&Oacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Ocirc; ')">&Ocirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Otilde; ')">&Otilde;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Ouml; ')">&Ouml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&times; ')">&times;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Oslash; ')">&Oslash;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Ugrave; ')">&Ugrave;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Uacute; ')">&Uacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Ucirc; ')">&Ucirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Uuml; ')">&Uuml;</a></td>
	</tr>
	<tr>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Yacute; ')">&Yacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&THORN; ')">&THORN;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&szlig; ')">&szlig;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&agrave; ')">&agrave;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&aacute; ')">&aacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&acirc; ')">&acirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&atilde; ')">&atilde;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&auml; ')">&auml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&aring; ')">&aring;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&aelig; ')">&aelig;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ccedil; ')">&ccedil;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&egrave; ')">&egrave;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&eacute; ')">&eacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ecirc; ')">&ecirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&euml; ')">&euml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&igrave; ')">&igrave;</a></td>
	</tr>
	<tr>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&iacute; ')">&iacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&icirc; ')">&icirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&iuml; ')">&iuml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&eth; ')">&eth;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ntilde; ')">&ntilde;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ograve; ')">&ograve;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&oacute; ')">&oacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ocirc; ')">&ocirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&otilde; ')">&otilde;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ouml; ')">&ouml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&divide; ')">&divide;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&oslash; ')">&oslash;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ugrave; ')">&ugrave;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&uacute; ')">&uacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ucirc; ')">&ucirc;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&uuml; ')">&uuml;</a></td>
	</tr>
	<tr>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&yacute; ')">&yacute;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&thorn; ')">&thorn;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&yuml; ')">&yuml;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&alpha; ')">&alpha;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&beta; ')">&beta;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&gamma; ')">&gamma;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&delta; ')">&delta;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&epsilon; ')">&epsilon;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&mu; ')">&mu;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&pi; ')">&pi;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&rho; ')">&rho;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&Omega; ')">&Omega;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&omega; ')">&omega;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&prod; ')">&prod;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&sum; ')">&sum;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&radic; ')">&radic;</a></td>
	</tr>
	<tr>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&infin; ')">&infin;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&int; ')">&int;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&asymp; ')">&asymp;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ne; ')">&ne;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&le; ')">&le;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&ge; ')">&ge;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&loz; ')">&loz;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&bull; ')">&bull;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&prime; ')">&prime;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&trade; ')">&trade;</a></td>
		<td class="char" onmouseover="className='charover';" onmouseout="className='char';"><a href="javascript:insertChar('&permil; ')">&permil;</a></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
</table>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsClose("onclick=\"window.close();\"") %>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>