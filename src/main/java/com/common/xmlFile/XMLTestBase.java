package com.common.xmlFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.common.DefaultSqlSyntaxVerifier;
import com.common.sqlFile.SQLSyntaxVerifier;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

abstract public class XMLTestBase {

	private static final Logger log = Logger.getLogger(XMLTestBase.class.getName());

	private final SQLSyntaxVerifier sqlSyntaxVerifier;
	protected final XMLLoader xmlLoader;
	protected final String fileName;

	protected XMLTestBase() {
		this(null);
	}

	protected XMLTestBase(XMLLoader xmlLoader, String fileName, SQLSyntaxVerifier sqlSyntaxVerifier) {
		if (sqlSyntaxVerifier == null) {
			this.sqlSyntaxVerifier = new DefaultSqlSyntaxVerifier();
		}
		else {
			this.sqlSyntaxVerifier = sqlSyntaxVerifier;
		}
		this.xmlLoader = xmlLoader;
		this.fileName = fileName;
	}

	protected XMLTestBase(XMLLoader xmlLoader) {
		this(xmlLoader, null, null);
	}

	protected XMLTestBase(XMLLoader xmlLoader, String fileName) {
		this(xmlLoader, fileName, null);
	}

	@TestFactory
	Collection<DynamicTest> testXMLMssql() {
		return generateTestCasesInXML(xmlLoader.loadSQLFromXML(fileName));
	}

	protected Collection<DynamicTest> generateTestCasesInXML(Map<String, Query> sqlMSSQL) {
		Function<Entry<String, Query>, DynamicTest> function = entry -> DynamicTest.dynamicTest(String.format("Test %s ", entry.getKey()), () -> verifyTemplate(entry.getKey(), entry.getValue()));
		return sqlMSSQL.entrySet().stream().map(function).collect(Collectors.toList());
	}

	protected void verifyTemplate(String name, Query query) {
		log.info("Verify template: " + name);
		assertNotNull(name, "Name of query statement must be existed ");
		sqlSyntaxVerifier.verifySqlSyntax(query.getSql());
		log.info("Passed: " + name);
	}
}
