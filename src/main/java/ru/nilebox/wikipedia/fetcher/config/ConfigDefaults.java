package ru.nilebox.wikipedia.fetcher.config;

/**
 *
 * @author nile
 */
public class ConfigDefaults {
	public static final String BASE_URL = "http://en.wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&rvprop=content&rvsection=0&rvparse&titles=";
	public static final String XPATH_CONTENT = "/api/query/pages/page/revisions/rev[1]";
	public static final String XPATH_DESCRIPTION = "/html/body/p";
	public static final String XPATH_REDIRECT_URL = "/html/body/div/span/a/@href";
	public static final String XPATH_REFERENCES = "//ul/li";
	public static final String XPATH_REFERENCE_URL = ".//a[1]/@href";
	
	
}
