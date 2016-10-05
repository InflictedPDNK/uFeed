package org.pdnk.ufeed;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import org.pdnk.ufeed.api.ApiHelper;
import org.pdnk.ufeed.api.BasicXmlParser;
import org.pdnk.ufeed.api.OkHttpApiHelper;
import org.pdnk.ufeed.api.RawResponse;
import org.pdnk.ufeed.eSport.api.EsportKernelFactory;
import org.pdnk.ufeed.eSport.api.EsportsApi;
import org.pdnk.ufeed.eSport.model.BaseEntry;
import org.pdnk.ufeed.eSport.model.BaseFeed;
import org.pdnk.ufeed.eSport.model.EsportCollection;
import org.pdnk.ufeed.eSport.model.EsportFeed;
import org.pdnk.ufeed.eSport.model.HrefDescriptor;
import org.pdnk.ufeed.util.DialogManager;
import org.pdnk.ufeed.util.ParametricRunnable;

import java.text.DateFormat;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener
{

    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.feedContainer)
    RecyclerView feedContainer;

    @BindView(R.id.webView)
    WebView webView;

    @BindView(R.id.lastUpdate)
    TextView lastUpdateText;

    private EsportsApi api;
    private DialogManager dialogManager;
    private FeedAdapter feedAdapter;
    private boolean isRefreshing;

    /*
    This is a crude cache of loaded data used for navigation purposes.
     */
    private Stack<BaseFeed> modelCache = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        prepareApi();
        prepareUi();
    }

    /**
     * Prepare API related objects
     */
    private void prepareApi()
    {
        ApiHelper apiHelper = new OkHttpApiHelper((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE),
                                                  getExternalCacheDir(),
                                                  new BasicXmlParser(new EsportKernelFactory()));
        apiHelper.setGenericErrorHandler(standardErrorHandler);
        apiHelper.setNetworkErrorHandler(networkErrorHandler);

        api = EsportsApi.create(apiHelper);
    }


    /**
     * Prepare UI related objects
     */
    private void prepareUi()
    {
        ButterKnife.bind(this);
        dialogManager = new DialogManager(this);

        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setDistanceToTriggerSync(500);
        refreshLayout.setNestedScrollingEnabled(true);
        refreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        feedContainer.setHasFixedSize(true);
        feedContainer.setLayoutManager(gridLayoutManager);

        feedAdapter = new FeedAdapter(null);
        feedAdapter.setOnItemClickListener(onContainerItemClick);

        feedContainer.setAdapter(feedAdapter);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        loadCollections();
    }

    @Override
    public void onRefresh()
    {
        loadCollections();
    }

    /**
     * Load main collections feed
     */
    void loadCollections()
    {
        feedAdapter.updateItems(null);
        modelCache.clear();

        setRefreshingState(true);
        api.loadCollections(new ParametricRunnable<EsportCollection>()
        {
            @Override
            public void run(EsportCollection param)
            {
                handleLoadDataResponse(param);
            }
        }, null);
    }


    /**
     * Load feed from the selected entry
     * @param entry source entry
     */
    void loadFeed(final BaseEntry entry)
    {
        api.loadFeed(entry.getLink().getUrl(), new ParametricRunnable<EsportFeed>()
        {
            @Override
            public void run(EsportFeed param)
            {
                handleLoadDataResponse(param);
            }
        }, null);
    }

    /**
     * Load HTML page from the selected entry
     * @param entry source entry
     */
    void loadHtml(final BaseEntry entry)
    {
        api.loadHtml(entry.getLink().getUrl(), new ParametricRunnable<RawResponse>()
        {
            @Override
            public void run(RawResponse param)
            {
                webView.loadData(param.getRawData(), entry.getContentType(), null);
                webView.setVisibility(View.VISIBLE);
                setRefreshingState(false);
            }
        }, null);
    }

    /**
     * Handle load feed successful response
     * @param loadedFeed incoming feed data
     */
    void handleLoadDataResponse(BaseFeed loadedFeed)
    {
        setRefreshingState(false);
        modelCache.push(loadedFeed);
        refreshPresentation();
    }

    /**
     * Refresh UI presentation with the updated data
     */
    void refreshPresentation()
    {
        closeWebview();

        if(!modelCache.isEmpty())
        {
            BaseFeed topFeed = modelCache.peek();
            setTitle(String.format("%s - %s", getString(R.string.app_name), topFeed.getTitle()));
            if(topFeed instanceof EsportFeed)
            {
                lastUpdateText.setText(String.format("Updated: %s", DateFormat.getDateTimeInstance().format(((EsportFeed) topFeed).getLastUpdate())));
                lastUpdateText.setVisibility(View.VISIBLE);
            }else
            {
                lastUpdateText.setVisibility(View.GONE);
            }
            feedAdapter.updateItems(topFeed.getEntries());
        }else
        {
            setTitle(getString(R.string.app_name));
            lastUpdateText.setVisibility(View.GONE);
        }
    }

    /**
     * Close webview
     */
    void closeWebview()
    {
        webView.loadUrl("about:blank");
        webView.setVisibility(View.GONE);
    }

    /**
     * Set refreshing state of the UI. This essentially shows Progress spinner and enables/disables view interactions
     * @param loading set loading state
     */
    private synchronized void setRefreshingState(boolean loading)
    {
        isRefreshing = loading;
        refreshLayout.setRefreshing(loading);
        feedContainer.setEnabled(!loading);
    }

    /**
     * Action handler for container item click
     */
    private ParametricRunnable<BaseEntry> onContainerItemClick = new ParametricRunnable<BaseEntry>()
    {
        @Override
        public void run(BaseEntry param)
        {
            //ignore clicks if refreshing
            if(!isRefreshing)
            {
                setRefreshingState(true);
                HrefDescriptor link = param.getLink();

                //detect type of source link and act accordingly
                if (link.ofType(HrefDescriptor.TYPE_ATOM))
                {
                    feedAdapter.updateItems(null);
                    loadFeed(param);
                }
                else if (link.ofType(HrefDescriptor.TYPE_HTML))
                {
                    loadHtml(param);
                }
            }
        }
    };

    /**
     * General error action handler. Supports retry
     */
    ParametricRunnable<OkHttpApiHelper.RequestRetryHandle> standardErrorHandler = new ParametricRunnable<OkHttpApiHelper.RequestRetryHandle>()
    {
        @Override
        public void run(final OkHttpApiHelper.RequestRetryHandle param)
        {
            setRefreshingState(false);

            dialogManager.buildMessageRetryDialog(getString(R.string.error_title),
                                                  param.getLastAssociatedException().getMessage(),
                                                  param.isRetryAvailable() ? new Runnable()
            {
                @Override
                public void run()
                {
                    setRefreshingState(true);
                    param.executeRetry();

                }
            } : null, false);
        }
    };

    /**
     * Network error action handler. Supports retry
     */
    ParametricRunnable<OkHttpApiHelper.RequestRetryHandle> networkErrorHandler = new ParametricRunnable<OkHttpApiHelper.RequestRetryHandle>()
    {
        @Override
        public void run(final OkHttpApiHelper.RequestRetryHandle param)
        {
            setRefreshingState(false);
            dialogManager.buildNoNetworkDialog(param.isRetryAvailable() ? new Runnable()
            {
                @Override
                public void run()
                {
                    setRefreshingState(true);
                    param.executeRetry();
                }
            } : null);
        }
    };


    /**
     * Handling native Back button.
     */
    @Override
    public void onBackPressed()
    {
        //Crude implementation of back stack. Webview removal is a priority, then popping the stacked data, then super
        if(webView.getVisibility() == View.VISIBLE)
        {
            closeWebview();
        }else if(modelCache.size() <= 1)
        {
            super.onBackPressed();
        }
        else
        {
            modelCache.pop();
            refreshPresentation();
        }
    }
}
