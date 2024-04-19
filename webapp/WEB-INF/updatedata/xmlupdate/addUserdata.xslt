<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xalan/java"
	exclude-result-prefixes="java">
	<xsl:strip-space elements="*" />
	<xsl:output method="xml"
		doctype-system="http://www.opencms.org/dtd/6.0/opencms-system.dtd"
		indent="yes" />


	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:param name="userdataIndex"
		select="java:org.opencms.setup.xml.CmsXmlConfigUpdater.getSystemConfigPosition('userdata')" />

	<xsl:template match="system[not(userdata)]">
		<xsl:copy>
			<xsl:for-each select="*">
				<xsl:if
					test="java:org.opencms.setup.xml.CmsXmlConfigUpdater.getSystemConfigPosition(name()) &lt; $userdataIndex">
					<xsl:copy-of select="." />
				</xsl:if>
			</xsl:for-each>
			<userdata autoload="true">
				<userdata-domain
					class="org.opencms.jsp.userdata.CmsUserDataHeader" />
				<userdata-domain
					class="org.opencms.jsp.userdata.CmsDefaultUserDataDomain" />
			</userdata>
			<xsl:for-each select="*">
				<xsl:if
					test="java:org.opencms.setup.xml.CmsXmlConfigUpdater.getSystemConfigPosition(name()) &gt; $userdataIndex">
					<xsl:copy-of select="." />
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
