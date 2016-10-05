package org.pdnk.ufeed;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.pdnk.ufeed.eSport.model.BaseEntry;
import org.pdnk.ufeed.util.ParametricRunnable;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Simple adapter for our recycler view
 */
class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder>
{
    private List<? extends BaseEntry> items;

    private ParametricRunnable<BaseEntry> onItemClickListener;

    FeedAdapter(List<? extends BaseEntry> items)
    {
        this.items = items;
    }

    void setOnItemClickListener(ParametricRunnable<BaseEntry> onItemClickListener)
    {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tile, parent, false);

        view.getLayoutParams().height = parent.getMeasuredWidth()/2;
        view.getLayoutParams().width = parent.getMeasuredWidth()/2;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        final BaseEntry item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getSummary());

        holder.cachedIcon.setVisibility(item.isCached()? View.VISIBLE : View.GONE);

        //TODO: as icons currently not available, generate color based on hash value of the title
        holder.tileBackground.setBackgroundColor((item.getTitle().hashCode() & 0x00ffffff) | 0xff000000);

        holder.tileBackground.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {

                //double click prevention
                view.setEnabled(false);
                view.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        view.setEnabled(true);
                    }
                }, 300);

                if(onItemClickListener != null)
                    onItemClickListener.run(item);
            }
        });

    }

    void updateItems(List<? extends BaseEntry> newItemList)
    {
        items = newItemList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount()
    {
        return items == null ? 0 : items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        public final TextView title;
        public final TextView description;
        public final View tileBackground;
        public final ImageView cachedIcon;

        ViewHolder(View view)
        {
            super(view);
            title = ButterKnife.findById(view, R.id.titleText);
            description = ButterKnife.findById(view, R.id.descriptionText);
            tileBackground = ButterKnife.findById(view, R.id.tileBackground);
            cachedIcon = ButterKnife.findById(view, R.id.cachedIcon);
        }
    }

}
