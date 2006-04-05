<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);


if (cms.getLayout().equals("common")) {
	if (cms.template("1")) {
		%></td>
		<%
	} else if (cms.template("2")) {
		%></tr></table>
		</div>
		<%
	} else if (cms.template("3")) {
		%></body>
		</html>
		<%
	}
} else if (cms.getLayout().equals("accessible")) {
	if (cms.template("1")) {
		%></div>
		<%
	} else if (cms.template("2")) {
		%><div style="visibility:hidden; clear:both; font-size:1px; line-height:0px">&nbsp;</div>
		</div>
		<%
	} else if (cms.template("3")) {
		%></div>
		</body>
		</html>
		<%
	}
} else if (cms.getLayout().equals("print")) {
	if (cms.template("1")) {
		%></td>
		</tr></table>
		<%
	} else if (cms.template("2")) {
		%></div>
		<%
	} else if (cms.template("3")) {
		%></body>
		</html>
		<%
	}
}
%>