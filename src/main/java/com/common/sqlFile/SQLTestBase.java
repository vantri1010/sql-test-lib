package com.common.sqlFile;

import com.common.DefaultSqlSyntaxVerifier;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SQLTestBase {

	private static final Logger log = Logger.getLogger(SQLTestBase.class.getName());

	private final SQLSyntaxVerifier sqlSyntaxVerifier;
	private final SQLLoader sqlLoader;

	protected SQLTestBase() {
		this(null);
	}

	protected SQLTestBase(SQLLoader sqlLoader) {
		this(sqlLoader, null);
	}

	protected SQLTestBase(SQLLoader sqlLoader, SQLSyntaxVerifier sqlSyntaxVerifier) {
		if (sqlSyntaxVerifier == null) {
			this.sqlSyntaxVerifier = new DefaultSqlSyntaxVerifier();
		}
		else {
			this.sqlSyntaxVerifier = sqlSyntaxVerifier;
		}
		this.sqlLoader = sqlLoader;
	}

	@TestFactory
	Collection<DynamicTest> testMssql() {
		return generateMssqlTestcases(this.getClass(), sqlLoader);
	}

	protected Collection<DynamicTest> generateMssqlTestcases(Class<?> clazz, SQLLoader sqlLoader) {
		log.info("***********************Verifying test cases ***********************");
		Map<String, Object> collectionCheck = sqlLoader.initTestCollection(clazz, SQLAnnotatedLoader.Testable.class);
		Function<Map.Entry<String, Object>, DynamicTest> function = entry -> DynamicTest.dynamicTest(String.format("Test %s ", entry.getKey()), () -> sqlSyntaxVerifier.verifySqlSyntax(sqlLoader.getSqlContent((String) entry.getValue())));
		return collectionCheck.entrySet().stream().map(function).collect(Collectors.toList());
	}
}
