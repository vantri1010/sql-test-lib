package com.common.xmlFile;

public class SampleJdbcQueriesXMLTest extends XMLTestBase {

	public SampleJdbcQueriesXMLTest() {
		super(new XMLJdbcQueriesLoader(), "src/test/resources/mssql/jdbc-queries.xml");
	}
}
