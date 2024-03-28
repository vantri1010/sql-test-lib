package com.common.xmlFile;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class XMLJdbcTemplateQueriesLoader extends AbstractXMLParser implements XMLLoader {

	@Override
	public Map<String, Query> loadSQLFromXML(final String fileName) {
		return super.getParseDataList(fileName, "template", "name", "query", null).stream().collect(Collectors.toMap(query -> query.asKey().trim(), Function.identity()));
	}
}
