package com.common.xmlFile;

public class Query {

	private String sql;

	private String name;

	private String sqlType;

	public String asKey() {
		return this.name;
	}

	public String getSql() {
		return sql;
	}

	public String getSqlType() {
		return this.sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setName(String name) {
		this.name = name;
	}
}
