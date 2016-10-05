package org.pdnk.ufeed.eSport.model;

import java.util.Date;
import java.util.List;

/**
 * Esports feed. Extends BaseFeed with additional metadata, such as date of last update
 */
public class EsportFeed extends BaseFeed<EsportFeed.Entry>
{
    private Date lastUpdate;

    public EsportFeed(String title, Date lastUpdate, List<Entry> entries)
    {
        super(title, entries);
        this.lastUpdate = lastUpdate;
    }

    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    /**
     * Entry model used in Esport feed model
     */
    public static class Entry extends BaseEntry
    {
        private final Date lastUpdate;
        private final HrefDescriptor icon;

        public Entry(String title,
                     Date lastUpdate,
                     String summary,
                     HrefDescriptor link,
                     HrefDescriptor icon)
        {
            super(title, link, summary);
            this.lastUpdate = lastUpdate;
            this.icon = icon;
        }


        public Date getLastUpdate()
        {
            return lastUpdate;
        }

        public HrefDescriptor getIcon()
        {
            return icon;
        }
    }
}
