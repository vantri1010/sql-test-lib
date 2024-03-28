package com.common;

import com.common.sqlFile.SQLSyntaxVerifier;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultSqlSyntaxVerifier implements SQLSyntaxVerifier {

	private static final Logger log = Logger.getLogger(DefaultSqlSyntaxVerifier.class.getName());

	public void verifySqlSyntax(String sql) {
		// we need to replace "Place Holder, eg: :?abcxyz" in dynamic prepare statement
		// since it is not standard SQL string
		sql = sql.replaceAll("\\:\\?\\w+", "?");

		log.info("Verify Syntax SQL: " + sql);

		// verify sql
		Validation validation = new Validation(Collections.singletonList(DatabaseType.SQLSERVER), sql);
		validation.getFeatureConfiguration().setValue(Feature.allowSquareBracketQuotation, true);
		List<ValidationError> errors = validation.validate();

		// Print errors if any.
		if (!errors.isEmpty()) {
			log.info(errors.toString());
		}

		assertEquals(0, errors.size());
	}
}
