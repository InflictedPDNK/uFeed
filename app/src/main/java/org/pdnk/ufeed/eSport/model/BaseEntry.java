package org.pdnk.ufeed.eSport.model;

import org.pdnk.ufeed.api.BaseResponse;

/**
 * Base Esports Entry model
 */
public class BaseEntry extends BaseResponse
{
    private final String title;
    private final HrefDescriptor link;
    private final String summary;

    public BaseEntry(String title, HrefDescriptor link, String summary)
    {
        this.title = title;
        this.link = link;
        this.summary = summary;
    }

    public String getTitle()
    {
        return title;
    }

    public HrefDescriptor getLink()
    {
        return link;
    }

    public String getSummary()
    {
        return summary;
    }
}
