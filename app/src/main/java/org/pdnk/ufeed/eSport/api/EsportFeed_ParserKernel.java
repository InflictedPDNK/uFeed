package org.pdnk.ufeed.eSport.api;

import android.support.annotation.Nullable;
import android.text.Html;

import org.pdnk.ufeed.api.XmlParserKernel;
import org.pdnk.ufeed.eSport.model.EsportFeed;
import org.pdnk.ufeed.eSport.model.HrefDescriptor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Parser Kernel for translating Esports XML to {@link EsportFeed} model
 */
public class EsportFeed_ParserKernel implements XmlParserKernel
{
    private final static String TAG_ENTRY = "entry";
    private final static String TAG_TITLE = "title";
    private final static String TAG_UPDATED = "updated";
    private final static String TAG_SUMMARY = "summary";
    private final static String TAG_LINK = "link";

    private final static String ATTR_TYPE = "type";
    private final static String ATTR_REL = "rel";
    private final static String ATTR_VAL_RELATED = "related";
    private final static String ATTR_VAL_ALTERNATE = "alternate";
    private final static String ATTR_VAL_ICON = "icon";
    private final static String ATTR_HREF = "href";

    private LinkedList<EsportFeed.Entry> entries;
    private String feedTitle;
    private Date lastUpdate;


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
        String name = parser.getName();
        switch (name)
        {
            case TAG_ENTRY:
                String title = null;
                String summary = null;
                Date updated = null;
                String href = null;
                String hrefType = null;
                String iconUrl = null;
                String iconType = null;

                parser.next();
                int prevDepth = parser.getDepth();
                int currentDepth = prevDepth;
                while (parser.next() != XmlPullParser.END_TAG || (currentDepth = parser.getDepth()) != prevDepth)
                {
                    name = parser.getName();
                    if (parser.getEventType() == XmlPullParser.START_TAG && currentDepth == prevDepth + 1)
                    {
                        if (TAG_TITLE.equals(name) && parser.next() == XmlPullParser.TEXT )
                        {
                            title = parser.getText();
                        }
                        else if (TAG_UPDATED.equals(name) && parser.next() == XmlPullParser.TEXT)
                        {
                            updated = readDate(parser.getText());
                        }
                        else if (TAG_SUMMARY.equals(name) && parser.next() == XmlPullParser.TEXT )
                        {
                            summary = Html.fromHtml(parser.getText()).toString();
                        }
                        else if (TAG_LINK.equals(name))
                        {
                            String rel = parser.getAttributeValue(null, ATTR_REL);
                            if (ATTR_VAL_RELATED.equals(rel) || ATTR_VAL_ALTERNATE.equals(rel))
                            {
                                hrefType = parser.getAttributeValue(null, ATTR_TYPE);
                                href = parser.getAttributeValue(null, ATTR_HREF);
                            }
                            else if (ATTR_VAL_ICON.equals(rel))
                            {
                                iconType = parser.getAttributeValue(null, ATTR_TYPE);
                                iconUrl = parser.getAttributeValue(null, ATTR_HREF);
                            }
                        }
                    }

                }

                //validate only title and href as this is the minimum we need. Skip entry otherwise.
                if (isStringValid(title) && isStringValid(href))
                {
                    HrefDescriptor link = new HrefDescriptor(href, hrefType);
                    HrefDescriptor icon = new HrefDescriptor(iconUrl, iconType);
                    entries.addLast(new EsportFeed.Entry(title, updated, summary, link, icon));
                }

                break;
            case TAG_TITLE:
                parser.next();
                if (parser.getEventType() == XmlPullParser.TEXT)
                    feedTitle = parser.getText();
                break;
            case TAG_UPDATED:
                parser.next();
                lastUpdate = readDate(parser.getText());
                break;
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
            return new EsportFeed(feedTitle, lastUpdate, entries);

        return null;
    }

    private Date readDate(String date)
    {
        try
        {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(date);
        } catch (ParseException e)
        {
            //TODO: for now leave it be...
            return null;
        }
    }

    private boolean isStringValid(@Nullable String s)
    {
        return s != null && !s.isEmpty();
    }
}
