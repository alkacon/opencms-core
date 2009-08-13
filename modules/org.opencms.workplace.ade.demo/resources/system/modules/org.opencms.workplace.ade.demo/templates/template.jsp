<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html><title>my first advanced direct edit template</title></html>
<body>
	<h1 class="theHeader">header</h1>
	<div class="theNavigation">navigation</div>
<div class="theLeftColumn">
  <cms:container name="leftcol" type="leftColumn" maxElements="2" />
</div>

<div class="theRightColumn">
  <cms:container name="rightcol" type="rightColumn" maxElements="4" />
</div>

<div class="theMainContent">
  <cms:container name="D-main-content" type="mainContent" maxElements="1" />
</div>

<div class="thePromoFooter">
  <cms:container name="C-promo-footer" type="promoFooter" maxElements="3" />
</div>

</body>
</html>
