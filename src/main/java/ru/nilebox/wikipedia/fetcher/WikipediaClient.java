package ru.nilebox.wikipedia.fetcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.nilebox.wikipedia.fetcher.config.WikipediaConfig;
import ru.nilebox.wikipedia.fetcher.filter.WikipediaFilter;
import ru.nilebox.wikipedia.fetcher.utils.XMLUtils;

/**
 *
 * @author nile
 */
public class WikipediaClient {

	private static final Logger logger = LoggerFactory.getLogger(WikipediaClient.class);
	private WikipediaConfig config;
	private WikipediaFilter filter;

	public WikipediaConfig getConfig() {
		return config;
	}

	public void setConfig(WikipediaConfig config) {
		this.config = config;
	}

	public WikipediaFilter getFilter() {
		return filter;
	}

	public void setFilter(WikipediaFilter filter) {
		this.filter = filter;
	}

	public String findDescription(String name) {
		if (name == null) {
			return null;
		}

		String description = null;
		try {
			description = findValidDescription(name, true);
			if (description != null) {
				return description;
			}
			String stopChars = ";.,:«»-\"";
			String[] parts = StringUtils.split(name, stopChars);
			if (parts.length == 1) {
				return null;
			}
			for (String part : parts) {
				if (part.length() < 3) {
					continue;
				}
				part = cleanQuery(part);
				description = findValidDescription(part, true);
				if (description != null) {
					return description;
				}
			}
		} catch (Exception ex) {
			logger.error("Error retrieving data from Wikipedia", ex);
		}

		return null;
	}

	private boolean isReferPage(Node descriptionNode) {
		if (descriptionNode == null) {
			return false;
		}

		String innerXml = XMLUtils.getInnerXml(descriptionNode).toLowerCase();
		int boldStartIndex = innerXml.indexOf("<b>");
		if (boldStartIndex >= 0) {
			int boldEndIndex = innerXml.indexOf("</b>");
			if (boldEndIndex == innerXml.length() - 1 - "</b>".length()) {
				return true;
			}
		}
		return false;
	}

	private String getQueryFromReferPage(Document doc) throws XPathExpressionException, UnsupportedEncodingException {
		NodeList nodes = (NodeList) config.getReferencesPath().evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String text = node.getTextContent();
			if (filter.validatePageReference(text)) {
				String url = (String) config.getReferenceUrlPath().evaluate(node, XPathConstants.STRING);
				int lastIndex = url.lastIndexOf('/');
				if (lastIndex >= 0) {
					String query = URLDecoder.decode(url.substring(lastIndex + 1).replace("_", " "), "UTF-8");
					return query;
				}
			}
		}
		return null;
	}

	private String findValidDescription(String name, boolean allowAlternatives) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		Document doc = findPage(name);
		if (doc == null) {
			return null;
		}
		String description = parseDescription(name, doc);
		if (description != null || !allowAlternatives) {
			return description;
		}
		if (name.endsWith("(значения)")) {
			return null;
		}
		name = name + " (значения)";
		doc = findPage(name);
		if (doc == null) {
			return null;
		}
		description = parseDescription(name, doc);
		if (description != null) {
			return description;
		}
		return null;
	}

	public Document findPage(String query) throws IOException, ParserConfigurationException, SAXException {
		if (query == null) {
			return null;
		}
		query = query.trim();
		if (query.length() == 0) {
			return null;
		}
		String encodedQuery = URLEncoder.encode(query, "UTF-8").replace("+", "%20");
		String url = config.getBaseUrl() + encodedQuery;
		return fetch(url);
	}

	public String parseDescription(String name, Document doc) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		String content = (String) config.getContentPath().evaluate(doc, XPathConstants.STRING);
		String html = "<html><body>" + StringEscapeUtils.unescapeHtml(content) + "</body></html>";

		TagNode tagNode = new HtmlCleaner().clean(html);
		Document contentDoc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);

		if (html.contains("#REDIRECT")) {
			String url = (String) config.getRedirectPath().evaluate(contentDoc, XPathConstants.STRING);
			String[] parts = StringUtils.split(url, '/');
			String pageName = URLDecoder.decode(parts[parts.length - 1].replace('_', ' '), "UTF-8");
			String description = findValidDescription(pageName, false);
			if (description != null) {
				return description;
			}
			doc = findPage(name);
			if (doc == null) {
				return null;
			}
			return parseDescription(name, doc);
		}

		Node descriptionNode = (Node) config.getDescriptionPath().evaluate(contentDoc, XPathConstants.NODE);
		if (isReferPage(descriptionNode)) {
			String query = getQueryFromReferPage(contentDoc);
			return findValidDescription(query, false);
		}

		if (!filter.validatePage(doc, contentDoc)) {
			return null;
		}

		NodeList descriptionNodes = (NodeList) config.getDescriptionPath().evaluate(contentDoc, XPathConstants.NODESET);
		for (int i = 0; i < descriptionNodes.getLength(); i++) {
			Node node = descriptionNodes.item(i);
			String description = node.getTextContent();
			description = removeRemarks(description);
			description = description.trim();
			if (description.length() > 10) {
				return description;
			}
		}
		return null;
	}

	private static String removeRemarks(String text) {
		if (text == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		boolean isRemarkOpen = false;
		for (char c : text.toCharArray()) {
			if (isRemarkOpen) {
				if (c == ']') {
					isRemarkOpen = false;
					continue;
				}
				continue;
			}
			if (c == '[') {
				isRemarkOpen = true;
				continue;
			}
			result.append(c);
		}
		return result.toString();
	}

	private static String cleanQuery(String query) {
		if (query == null) {
			return null;
		}
		query = query.toLowerCase();
		StringBuilder builder = new StringBuilder();
		for (char c : query.toCharArray()) {
			if (c == '-' || StringUtils.isWhitespace(new String(new char[]{c}))) {
				builder.append(" ");
			} else if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	private Document fetch(String url) throws IOException, ParserConfigurationException, SAXException {
		logger.info("Fetching Wikipedia url: " + url);

		try {
			Document doc = XMLUtils.fetch(url);
			return doc;
		} catch (Exception ex) {
			throw new RuntimeException("Error fetching document from url: " + url, ex);
		}
	}
}
