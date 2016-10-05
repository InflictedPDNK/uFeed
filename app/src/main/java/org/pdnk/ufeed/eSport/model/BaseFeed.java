package org.pdnk.ufeed.eSport.model;

import org.pdnk.ufeed.api.BaseResponse;

import java.util.List;

/**
 * Base Esports Feed model parametrised by type of entry. At minimum contains list of Esport entries
 * @see BaseEntry
 */
public class BaseFeed<EntryType extends BaseEntry> extends BaseResponse
{
    private final String title;
    private final List<EntryType> entries;

    public BaseFeed(String title, List<EntryType> entries)
    {
        this.title = title;
        this.entries = entries;
    }

    public String getTitle()
    {
        return title;
    }

    public List<EntryType> getEntries()
    {
        return entries;
    }
}
