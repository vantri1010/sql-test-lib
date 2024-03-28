package com.common.xmlFile;

public class SampleNamedQueryXMLTest extends XMLTestBase {

	public SampleNamedQueryXMLTest() {
		super(new XMLNamedQueriesLoader(), "src/test/resources/mssql/queries.xml");
	}
}
