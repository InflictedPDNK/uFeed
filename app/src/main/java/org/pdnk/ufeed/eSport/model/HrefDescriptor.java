package org.pdnk.ufeed.eSport.model;

/**
 * Describes Href used in the Esport entries.
 */
public final class HrefDescriptor
{
    public static final String TYPE_HTML = "text/html";
    public static final String TYPE_ATOM = "application/atom+xml";
    public static final String TYPE_VND_PNG = "application/vnd.esportsreader+png";

    public static String DefaultType = TYPE_ATOM;

    private final String url;
    private final String urlType;

    public HrefDescriptor(String url, String urlType)
    {
        this.url = url;
        this.urlType = urlType == null ? DefaultType : urlType;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUrlType()
    {
        return urlType;
    }

    public boolean ofType(String s)
    {
        return urlType != null && urlType.equals(s);
    }
}
