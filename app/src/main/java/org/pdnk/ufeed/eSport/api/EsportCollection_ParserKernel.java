package org.pdnk.ufeed.eSport.api;

import android.support.annotation.Nullable;

import org.pdnk.ufeed.api.XmlParserKernel;
import org.pdnk.ufeed.eSport.model.EsportCollection;
import org.pdnk.ufeed.eSport.model.HrefDescriptor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Parser Kernel for translating Esports XML to {@link EsportCollection} model
 */
@SuppressWarnings("FieldCanBeLocal")
public class EsportCollection_ParserKernel implements XmlParserKernel
{
    private final String TAG_COLLECTION = "collection";
    private final String TAG_TITLE = "atom:title";
    private final String ATTR_HREF = "href";

    private LinkedList<EsportCollection.Entry> entries;
    private String collectionTitle;

    @Override
    public void parseStartDoc(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        entries = new LinkedList<>();
    }

    @Override
    public void parseEndDoc(XmlPullParser parser) throws XmlPullParserException, IOException
    {

    }

    @Override
    public void parseStartTag(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        String title = null;
        String href;

        String name = parser.getName();
        if(name.equals(TAG_COLLECTION))
        {
            href = parser.getAttributeValue(null, ATTR_HREF);
            parser.next();
            int currentDepth = parser.getDepth();
            while (parser.next() != XmlPullParser.END_TAG || parser.getDepth() != currentDepth)
            {
                if (TAG_TITLE.equals(parser.getName()) && parser.getEventType() == XmlPullParser.START_TAG)
                {
                    parser.next();
                    title = parser.getText();
                }
            }

            //validate only title and href as this is the minimum we need. Skip entry otherwise.
            if(isStringValid(title) && isStringValid(href))
            {
                entries.addLast(new EsportCollection.Entry(title, new HrefDescriptor(href, null)));
            }
        }else if(name.equals(TAG_TITLE))
        {
            parser.next();
            if(parser.getEventType() == XmlPullParser.TEXT)
                collectionTitle = parser.getText();
        }
    }

    @Override
    public void parseEndTag(XmlPullParser parser) throws XmlPullParserException, IOException
    {
    }

    @Override
    public Object retrieveObject()
    {
        if(entries != null && !entries.isEmpty())
            return new EsportCollection(collectionTitle, entries);

        return null;
    }

    private boolean isStringValid(@Nullable String s)
    {
        return s != null && !s.isEmpty();
    }
}
