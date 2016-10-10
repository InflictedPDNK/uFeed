package org.pdnk.ufeed.eSport.api;


import org.pdnk.ufeed.api.ApiHelper;
import org.pdnk.ufeed.api.RawResponse;
import org.pdnk.ufeed.api.RequestRetryHandle;
import org.pdnk.ufeed.eSport.model.EsportCollection;
import org.pdnk.ufeed.eSport.model.EsportFeed;
import org.pdnk.ufeed.util.ParametricRunnable;


/**
 * Helper class encapsulating Esports REST API Data Provider negotiation.
 */
@SuppressWarnings("SameParameterValue")
public final class EsportsApi
{
    /**
     * Experimental. Currently turned off.
     */
    private static final boolean USE_SINGLE_INSTANCE = false;

    private static EsportsApi self;
    private final ApiHelper apiHelper;

    /**
     * Create instance. Clients should use this call wherever they need. Depending on implementation, it can produce
     * unique instance or singleton.
     * @param apiManager instance of ApiManager handling the underlying networking
     * @return new or existing instance of EsportsApi
     */
    public static EsportsApi create(ApiHelper apiManager)
    {
        if(USE_SINGLE_INSTANCE)
        {
            if (self == null)
                self = new EsportsApi(apiManager);

            return self;
        }

        return new EsportsApi(apiManager);
    }

    private EsportsApi(ApiHelper apiHelper)
    {
        this.apiHelper = apiHelper;
    }

    /**
     * Cancel all active requests as per {@link ApiHelper#cancelAllRequests(boolean)}.
     *
     */
    public void cancelAllRequests()
    {
        apiHelper.cancelAllRequests(true);
    }

    /**
     * Load Esport collection. If either or both handlers are null, default action handlers will be used if they are
     * installed in the provided {@link ApiHelper}
     * @param onSuccess pass action handler accepting  {@link EsportCollection} record upon successful response or null to ignore
     * @param onFailure pass action handler accepting {@link RequestRetryHandle} upon failure or null to ignore
     *                  @see ApiHelper#setGenericErrorHandler(ParametricRunnable)
     *                  @see ApiHelper#setNetworkErrorHandler(ParametricRunnable)
     */
    public void loadCollections(ParametricRunnable<EsportCollection> onSuccess, ParametricRunnable<RequestRetryHandle> onFailure)
    {
        apiHelper.requestGET("http://feed.esportsreader.com/reader/sports?v=11", EsportCollection.class, onSuccess, onFailure);
    }

    /**
     * Load Esport feed. If either or both handlers are null, default action handlers will be used if they are
     * installed in the provided {@link ApiHelper}
     * @param onSuccess pass action handler accepting  {@link EsportFeed} record upon successful response or null to ignore
     * @param onFailure pass action handler accepting {@link RequestRetryHandle} upon failure or null to ignore
     *                  @see ApiHelper#setGenericErrorHandler(ParametricRunnable)
     *                  @see ApiHelper#setNetworkErrorHandler(ParametricRunnable)
     */
    public void loadFeed(String feedUrl, ParametricRunnable<EsportFeed> onSuccess, ParametricRunnable<RequestRetryHandle> onFailure)
    {
        apiHelper.requestGET(String.format("%s?v=11", feedUrl), EsportFeed.class, onSuccess, onFailure);
    }

    /**
     * Load HTML. If either or both handlers are null, default action handlers will be used if they are
     * installed in the provided {@link ApiHelper}
     * <br/>
     * Currently HTML load follows all standard GET configurations set in the provided ApiHelper (such as encoding, caching, etc.)
     * @param onSuccess pass action handler accepting RawResponse containing unmodified HTML response upon success or null to ignore
     * @param onFailure pass action handler accepting {@link RequestRetryHandle} upon failure or null to ignore
     *                  @see ApiHelper#setGenericErrorHandler(ParametricRunnable)
     *                  @see ApiHelper#setNetworkErrorHandler(ParametricRunnable)
     */
    public void loadHtml(String htmlUrl, ParametricRunnable<RawResponse> onSuccess, ParametricRunnable<RequestRetryHandle> onFailure)
    {
        apiHelper.requestGET(htmlUrl, RawResponse.class, onSuccess, onFailure);
    }

}
