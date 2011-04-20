<%@ page taglibs="c,cms" %>
<cms:formatter var="content">
	<div>
		<c:set var="background"><cms:elementsetting name="background" default="43c43f"/></c:set>
		<iframe name="twitter" src="<cms:link>/system/modules/org.opencms.frontend.template3.demo/elements/twitterFrame.jsp?title=${content.value['Title']}&amp;subtitle=${content.value['Subtitle']}&amp;channel=${content.value['Channel']}&amp;background=${background}</cms:link>" style="margin: 0; border: none;" height="340" width="228" ></iframe>
	</div>
</cms:formatter>