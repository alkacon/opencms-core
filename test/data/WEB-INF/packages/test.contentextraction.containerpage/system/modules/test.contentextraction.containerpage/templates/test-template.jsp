<%@page
    pageEncoding="UTF-8"
    buffer="none"
    session="false"
    trimDirectiveWhitespaces="true"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<html>
<head>
<cms:enable-ade/>
</head>
<body>
	<div style="height:60px;">&nbsp;</div>
	<div>
		<cms:container name="aside" type="test,noindex">
		<div>
			Empty aside container (test,noindex)
		</div>
	</cms:container>
	</div>
	<div>
		<cms:container name="main" type="test,nestable">
			<div>
				Empty main container (test,nestable)
			</div>
		</cms:container>
	</div>
</body>
</html>