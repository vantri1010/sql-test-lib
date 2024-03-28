package com.common.sqlFile;

import com.common.FileProcessingUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SQLFileLoader implements SQLLoader {

	private final List<File> filesInFolder;

	public SQLFileLoader(String sqlFileFolder, List<String> blackListSQLFilesName) {
		if (sqlFileFolder != null) {
			filesInFolder = this.getFileFromFolder(sqlFileFolder, blackListSQLFilesName);
		}
		else {
			filesInFolder = new ArrayList<>();
		}
	}

	public SQLFileLoader(String sqlFileFolder) {
		this(sqlFileFolder, new ArrayList<>());
	}

	private List<File> getFileFromFolder(String sqlFileFolder, List<String> blackListSQLFilesName) {
		try {
			Path path = Paths.get(sqlFileFolder);
			return Files.walk(path).filter(Files::isRegularFile).map(Path::toFile).filter(file -> !blackListSQLFilesName.contains(file.getName())).collect(Collectors.toList());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getSqlContent(String name) {
		return name;
	}

	private String getSqlContentFromFile(List<File> filesInFolder, String name) {
		return filesInFolder.stream().filter(file -> file.getName().equals(name)).map(FileProcessingUtils::loadFile).findFirst().orElse("");
	}

	@Override
	public Map<String, Object> initTestCollection(final Class<?> clazz, Class<? extends Annotation> annotation) {
		return filesInFolder.stream().collect(Collectors.toMap(File::getName, file -> getSqlContentFromFile(filesInFolder, file.getName())));
	}
}
