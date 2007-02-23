package org.marketcetera.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * ID Factory that knows how to connect to an oustside URL and grab a block of IDs from it
 * If connecting to an outside provider fails we default to the in-memory id factory.
 * The expected output is to be formatted like this:
 * <pre>
 *  <id>
 *      <next>1</next>
 *      <num>1000</num>
 *  </id>
 * </pre>
 * @author Graham Miller
 * @version $Id$
 */
@ClassVersion("$Id$")
public class HttpDatabaseIDFactory extends DBBackedIDFactory {
	private URL url;
	private DocumentBuilder parser;
    private Reader inputReader = null;

    public HttpDatabaseIDFactory(URL url) {
		this.url = url;
        try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LoggerAdapter.error(MessageKey.ERROR_DBFACTORY_HTTP_PARSER_INIT.getLocalizedMessage(),e, this);
		}
	}

    protected void performIDRequest() throws IOException, SAXException, NoMoreIDsException {
        // Connect to the remote host and read in the data

        inputReader = getInputReader();
        Document document = parser.parse(new InputSource(inputReader));
        Node nextIDNode = document.getElementsByTagName("next").item(0);
        int nextID = Integer.parseInt(nextIDNode.getTextContent());
        Node numAllowedNode = document.getElementsByTagName("num").item(0);
        int numAllowed = Integer.parseInt(numAllowedNode.getTextContent());
        setNextID(nextID);
        setMaxAllowedID(nextID + numAllowed);
    }

    /** Close the input reader */
    protected void postRequestCleanup() {
        if (inputReader != null){
            try {
                inputReader.close();
            } catch (IOException e) {
                // ignored
            }
        }
    }

    protected void factoryValidityCheck() throws NoMoreIDsException {
        if (parser == null){
            throw new NoMoreIDsException(MessageKey.ERROR_DBFACTORY_MISSING_PARSER.getLocalizedMessage());
        }
    }

    protected Reader getInputReader() throws IOException {
		InputStreamReader inputReader;
		URLConnection connection = url.openConnection();

		inputReader = new InputStreamReader(connection.getInputStream());
		return inputReader;
	}
}
