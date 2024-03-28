package com.common.sqlFile;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface SQLLoader {

	String getSqlContent(String name);

	Map<String, Object> initTestCollection(final Class<?> clazzTest, final Class<? extends Annotation> annotation);

}
