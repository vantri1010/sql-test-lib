package com.common.xmlFile;

import java.util.Map;

public interface XMLLoader {

	Map<String, Query> loadSQLFromXML(final String fileName);

}
