package com.common.xmlFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class AbstractXMLParser {

	protected List<Query> getParseDataList(final String fileName, final String rootElementName, final String attributeName, final String sql, final String sqlType) {
		try {
			final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(fileName));
			final NodeList list = document.getElementsByTagName(rootElementName);
			final List<Node> nodeList = new ArrayList<>();

			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				nodeList.add(node);
			}

			return nodeList.stream().map(node -> {
				Query namedQuery = new Query();
				Element element = (Element) node;
				namedQuery.setName(element.getAttribute(attributeName));
				namedQuery.setSql(element.getElementsByTagName(sql).item(0).getTextContent());
				namedQuery.setSqlType(sqlType != null ? Optional.of(element.getAttribute(sqlType)).orElse("") : "");
				return namedQuery;
			}).collect(Collectors.toList());
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
