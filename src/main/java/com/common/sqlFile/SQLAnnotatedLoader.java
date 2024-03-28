package com.common.sqlFile;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.common.FileProcessingUtils;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import org.apiguardian.api.API;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.params.provider.CsvSource;

public final class SQLAnnotatedLoader implements SQLLoader {
	private static final Logger log = Logger.getLogger(SQLAnnotatedLoader.class.getName());


	private static final Map<String, String> mssqlSqlTemplates = new HashMap<>();
	private static final String SCENARIO_NAME_REGEX = "(.+/)(.+)(\\.sql)";

	public SQLAnnotatedLoader(String filePath) {
		if (filePath != null) {
			SQLBaseTemplates sqlTemplates = this.loadSqlTemplateFromXml(filePath);
			sqlTemplates.getSqlTemplates().forEach(sql -> mssqlSqlTemplates.put(sql.getName(), sql.getValue().trim()));
		}
	}

	public SQLAnnotatedLoader() {
	}

	public String getSqlContent(String name) {
		return name;
	}

	public static String getSqlTemplate(String name) {
		return mssqlSqlTemplates.getOrDefault(name, "");
	}

	private SQLBaseTemplates loadSqlTemplateFromXml(String filePath) {
		try {
			InputStream inputStream = SQLAnnotatedLoader.class.getClassLoader().getResourceAsStream(filePath);
			JAXBContext jaxbContext = JAXBContext.newInstance(SQLBaseTemplates.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			return (SQLBaseTemplates) unmarshaller.unmarshal(inputStream);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Failed to load sql templates from " + filePath + " to factory", e);
		}
	}

	@Override
	public Map<String, Object> initTestCollection(Class<?> clazzTest, Class<? extends Annotation> annotation) {
		return getMethodsAnnotatedWith(clazzTest);
	}

	private Map<String, Object> getMethodsAnnotatedWith(final Class<?> type) {
		final List<Method> methods = new ArrayList<>();
		Class<?> klass = type;
		while (klass != Object.class) {
			for (final Method method : klass.getDeclaredMethods()) {
				if (method.isAnnotationPresent(Testable.class)) {
					methods.add(method);
				}
			}
			klass = klass.getSuperclass();
		}

		return methods.stream().collect(Collectors.toMap(
				Method::getName, method -> this.processTestMethod(method, type)));
	}

	private String processTestMethod(Method method, Class<?> testClass) {
		try {
			Testable testScenario = method.getAnnotation(Testable.class);
			CsvSource csvSource = method.getAnnotation(CsvSource.class);
			String actualSql;
			if (csvSource != null && method.getParameterCount() >= 0) {
				actualSql = generateActualSqlWithParam(testScenario.targetClass(), method, testScenario.targetMethod(), csvSource);
			}
			else {
				actualSql = generateActualSqlNoParam(testScenario.targetClass(), testScenario.targetMethod());
			}
			verifyGeneratedSql(actualSql, testClass ,method.getName() + ".sql", testScenario.isSave());
			return actualSql;
		}
		catch (Exception e) {
			throw new RuntimeException("Fail Test", e);
		}
	}

	private String generateActualSqlNoParam(Class<?> sourceClass, String sourceMethodName)
			throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		return sourceClass.getMethod(sourceMethodName)
				.invoke(sourceClass.newInstance())
				.toString();
	}

	private String generateActualSqlWithParam(Class<?> sourceClass, Method method, String sourceMethodName, CsvSource csvSource)
			throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, JSONException {
		Object[] paramValues = Optional.ofNullable(csvSource.value()[0]).orElse("")
				.split(String.valueOf(csvSource.delimiter()));
		List<Class<?>> paramTypeClasses = Arrays.stream(method.getParameters())
				.filter(p -> FALSE.equals(p.isAnnotationPresent(
						SqlTemplate.class)))
				.map(Parameter::getType)
				.collect(Collectors.toList());
		List<Parameter> paramTypeClassWithSqlTemplates = Arrays.stream(method.getParameters())
				.filter(p -> TRUE.equals(p.isAnnotationPresent(
						SqlTemplate.class)))
				.collect(Collectors.toList());
		List<Object> objectList = new ArrayList<>();

		paramTypeClassWithSqlTemplates.forEach(p -> objectList.add(
				getSqlTemplate(p.getAnnotation(SqlTemplate.class).value())));

		for (int i = 0; i < paramTypeClasses.size(); i++) {
			Class<?> paramClass = paramTypeClasses.get(i);
			if (isCustomPoJo(paramClass)) {
				objectList.add(assignValue(paramClass, new JSONObject((String) paramValues[i])));
			} else if (paramClass.getName().equals(String.class.getName())) {
				objectList.add(paramValues[i]);
			} else {
				objectList.add(assignPrimitiveValue(paramClass, (String) paramValues[i]));
			}
		}
		return sourceClass.getMethod(sourceMethodName, method.getParameterTypes())
				.invoke(sourceClass.newInstance(), objectList.toArray())
				.toString();
	}

	private static Object assignPrimitiveValue(Class<?> type, String value) {
		PropertyEditor editor = PropertyEditorManager.findEditor(type);
		editor.setAsText(value);
		return editor.getValue();
	}

	private static Object assignValue(Class<?> clazz, JSONObject jsonObject)
			throws InstantiationException, IllegalAccessException {
		Map<String, Field> map = Stream.of(clazz.getDeclaredFields()).collect(Collectors.toMap(
				Field::getName, f -> f));
		Object initObj = clazz.newInstance();
		jsonObject.keys().forEachRemaining(key -> {
			Field field = map.get(key);
			field.setAccessible(true);
			try {
				if (Boolean.TRUE.equals(isCustomPoJo(field.getType()))) {
					field.set(initObj, assignValue(field.getType(), jsonObject.getJSONObject(key)));
				} else {
					field.set(initObj, jsonObject.get(key));
				}
			} catch (IllegalArgumentException | IllegalAccessException | JSONException | InstantiationException e) {
				throw new IllegalArgumentException("Can not init " + clazz.getName() + " from @CsvSource", e);
			}
		});
		return initObj;
	}

	private static boolean isCustomPoJo(Class<?> type) {
		return !type.isPrimitive() && !type.getName().equals("java.lang.String");
	}

	private void verifyGeneratedSql(String actualSql, Class<?> sourceClass, String dataFile, boolean isSaveFile) {
		String scenarioName = "";
		if (dataFile.matches(SCENARIO_NAME_REGEX)) {
			scenarioName = dataFile.replaceAll(SCENARIO_NAME_REGEX, "$2");
		}
		log.info("Actual " + scenarioName + " Sql: " + actualSql);
		if (isSaveFile) {
			FileProcessingUtils.saveFile(sourceClass.getName(), dataFile, actualSql);
		}
		String expectedSql = FileProcessingUtils.loadFile(sourceClass.getName(), dataFile);
		assertEquals(expectedSql, actualSql);
	}

	@XmlRootElement(name = "sql-templates")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class SQLBaseTemplates {

		@XmlElement(name = "sql-template")
		private List<SQLBaseTemplate> sqlTemplates;

		List<SQLBaseTemplate> getSqlTemplates() {
			return this.sqlTemplates;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class SQLBaseTemplate {

		@XmlAttribute(name = "name")
		private String name;

		@XmlValue
		private String value;

		String getName() {
			return name;
		}

		String getValue() {
			return value;
		}
	}

	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	@API(status = API.Status.STABLE, since = "1.0")
	public @interface Testable {
		Class<?> targetClass();

		String targetMethod() default "";

		boolean isSave() default false;
	}

	@Target({ElementType.PARAMETER, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	@API(status = API.Status.STABLE, since = "1.0")
	public @interface SqlTemplate {
		String value();
	}
}
