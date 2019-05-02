<%@page buffer="none" session="false" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<cms:formatter var="content" val="value">
  <div>
    <div style="width:200px;float:right">
      <img style="width:100%" src="${value.Image.toImage}"/>
    </div>
    <div>
      <h2>${value.Title}</h2>
      <p>${value.Text}</p>
    </div>
    <div>
      <a href="<cms:link>${value.LinkURI}</cms:link>">Link</a>
    </div>
    <div style="clear:right;"></div>
  </div>
</cms:formatter>