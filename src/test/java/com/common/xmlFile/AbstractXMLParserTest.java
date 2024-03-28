package com.common.xmlFile;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AbstractXMLParserTest {

	@Test
	public void getParseDataListTest() {
		List<Query> expectedQueryList = new ArrayList<>();
		Query expectedQuery = new Query();
		expectedQuery.setName("sample test");
		expectedQuery.setSql("SELECT 1 ;");
		expectedQuery.setSqlType("");
		expectedQueryList.add(expectedQuery);

		final String rootElementName = "query";
		final String attributeName = "name";
		final String sql = "sql";
		final String sqlType = "";
		AbstractXMLParser abstractXMLParser = Mockito.mock(AbstractXMLParser.class, Mockito.CALLS_REAL_METHODS);
		List<Query> actualQueryList = abstractXMLParser.getParseDataList(
				"src/test/resources/mssql/jdbc-queries.xml", rootElementName, attributeName, sql, sqlType);

		assertEquals(expectedQueryList.size(), actualQueryList.size());
		assertEquals(expectedQueryList.get(0).asKey(), actualQueryList.get(0).asKey());
		assertEquals(expectedQueryList.get(0).getSqlType(), actualQueryList.get(0).getSqlType());
		assertNotNull(actualQueryList.get(0).getSql());
	}
}
