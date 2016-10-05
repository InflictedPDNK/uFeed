package org.pdnk.ufeed.api;

/**
 * Unmodified (not parsed) response. Used to bypass any external parsers installed in the API helper
 */
public class RawResponse extends BaseResponse
{
    private final String rawData;

    public RawResponse(String rawData)
    {
        this.rawData = rawData;
    }

    public String getRawData()
    {
        return rawData;
    }
}
