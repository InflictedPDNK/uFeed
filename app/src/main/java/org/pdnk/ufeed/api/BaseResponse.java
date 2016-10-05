package org.pdnk.ufeed.api;

/**
 * Base response containing information such as content type and caching
 */

public abstract class BaseResponse
{
    private String contentType;
    private boolean cached;

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public boolean isCached()
    {
        return cached;
    }

    public void setCached(boolean cached)
    {
        this.cached = cached;
    }
}
