package org.pdnk.ufeed.api;

import org.pdnk.ufeed.eSport.api.EsportCollection_ParserKernel;
import org.pdnk.ufeed.eSport.api.EsportFeed_ParserKernel;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Implementation delegates for XML parser bridge. Implementations are kernels used in API response parsing and
 * must maintain the translation between API protocol and destination type of Model.
 * @see EsportCollection_ParserKernel
 * @see EsportFeed_ParserKernel
 */
@SuppressWarnings({"EmptyMethod", "RedundantThrows", "UnusedParameters"})
public interface XmlParserKernel
{
    /**
     * Called for XML Start Document element
     * @param parser instance of parser with the current state
     * @throws XmlPullParserException if malformed data
     * @throws IOException if malformed data
     */
    void parseStartDoc(XmlPullParser parser) throws XmlPullParserException, IOException;

    /**
     * Called for XML End Document element
     * @param parser instance of parser with the current state
     * @throws XmlPullParserException if malformed data
     * @throws IOException if malformed data
     */
    void parseEndDoc(XmlPullParser parser) throws XmlPullParserException, IOException;

    /**
     * Called for XML Start Tag element
     * @param parser instance of parser with the current state
     * @throws XmlPullParserException if malformed data
     * @throws IOException if malformed data
     */
    void parseStartTag(XmlPullParser parser) throws XmlPullParserException, IOException;

    /**
     * Called for XML End Tag element
     * @param parser instance of parser with the current state
     * @throws XmlPullParserException if malformed data
     * @throws IOException if malformed data
     */
    void parseEndTag(XmlPullParser parser) throws XmlPullParserException, IOException;

    /**
     * @return parsed object
     */
    Object retrieveObject();

}
