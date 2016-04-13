/*
 * Copyright (C) 2015 IXA Taldea, University of the Basque Country UPV/EHU

   This file is part of ixa-pipe-wikify.

   ixa-pipe-wikify is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   ixa-pipe-wikify is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with ixa-pipe-wikify.  If not, see <http://www.gnu.org/licenses/>.
*/


package ixa.pipe.wikify;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;


import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.Text;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URLEncoder;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import org.xml.sax.InputSource;

import org.apache.log4j.Logger;

/**
 * Simple web service-based annotation client for DBpedia Spotlight.
 *
 * @author pablomendes, Joachim Daiber
 */

public class DBpediaSpotlightClient {
    public Logger LOG = Logger.getLogger(this.getClass());

    private static final double CONFIDENCE = 0.0;
    private static final int SUPPORT = 0;
    private static final boolean COREFERENCE = false;

    // Create an instance of HttpClient.
    private static HttpClient client = new HttpClient();


    public String request(HttpMethod method) throws AnnotationException {

        String response = null;

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                LOG.error("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            // // Deal with the response.
            // // Use caution: ensure correct character encoding and is not binary data
        InputStream responseBody =  method.getResponseBodyAsStream();
        response = IOUtils.toString(responseBody, "UTF-8");

        } catch (HttpException e) {
            LOG.error("Fatal protocol violation: " + e.getMessage());
            throw new AnnotationException("Protocol error executing HTTP request.",e);
        } catch (IOException e) {
            LOG.error("Fatal transport error: " + e.getMessage());
            LOG.error(method.getQueryString());
            throw new AnnotationException("Transport error executing HTTP request.",e);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return response;

    }



    public Document extract(Text text, String host, String port) throws AnnotationException{

        LOG.info("Querying API.");
    String spotlightResponse = "";
    Document doc = null;
    try {
        String url = host + ":" + port +"/rest/annotate";
        PostMethod method = new PostMethod(url);
        method.setRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
        NameValuePair[] params = {new NameValuePair("text",text.text()), new NameValuePair("confidence",Double.toString(CONFIDENCE)), new NameValuePair("support",Integer.toString(SUPPORT)), new NameValuePair("coreferenceResolution",Boolean.toString(COREFERENCE))};
        method.setRequestBody(params);
        method.setRequestHeader(new Header("Accept", "text/xml"));
        spotlightResponse = request(method);
        doc = loadXMLFromString(spotlightResponse);
    }
    catch (javax.xml.parsers.ParserConfigurationException ex) {
    }
    catch (org.xml.sax.SAXException ex) {
    }
    catch (java.io.IOException ex) {
    }

    return doc;
    }

    public static Document loadXMLFromString(String xml)  throws org.xml.sax.SAXException, java.io.IOException, javax.xml.parsers.ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

}
