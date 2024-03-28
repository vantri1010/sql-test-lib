package com.common.xmlFile;

public class SampleJdbcTemplateQueriesXMLTest extends XMLTestBase {

	public SampleJdbcTemplateQueriesXMLTest() {
		super(new XMLJdbcTemplateQueriesLoader(), "src/test/resources/mssql/jdbc-template-queries.xml");
	}
}
