package org.pdnk.ufeed.eSport.model;

import java.util.List;

/**
 * Esports collection. Currently, simply a wrapper around BaseFeed
 */
public class EsportCollection extends BaseFeed<EsportCollection.Entry>
{
    public EsportCollection(String title, List<Entry> entriesList)
    {
        super(title, entriesList);
    }

    /**
     * Entry model used in Esports Collection feed model.
     */
    public static class Entry extends BaseEntry
    {
        public Entry(String title, HrefDescriptor href)
        {
            super(title, href, null);
        }
    }

}
