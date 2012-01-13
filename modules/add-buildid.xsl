<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"  cdata-section-elements="nicename description authorname authoremail value" />
  <xsl:param name="gwt.buildid" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <!--  This is to remove the gwt.buildid parameter if it already exists. -->
  <xsl:template match="param[@name='gwt.buildid']"></xsl:template>

  <!--  This code requires that there is an empty parameters tag in the manifest. -->
  <xsl:template match="//export/module/parameters">
  	<xsl:copy>
  		<xsl:apply-templates/>
  		<param><xsl:attribute name="name">gwt.buildid</xsl:attribute><xsl:value-of select="$gwt.buildid"/></param>
 	</xsl:copy>
  </xsl:template>
</xsl:stylesheet>