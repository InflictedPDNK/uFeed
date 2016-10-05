package org.pdnk.ufeed.eSport.api;

import org.pdnk.ufeed.api.XmlParserKernel;
import org.pdnk.ufeed.api.XmlParserKernelFactory;
import org.pdnk.ufeed.eSport.model.EsportCollection;
import org.pdnk.ufeed.eSport.model.EsportFeed;

import java.lang.reflect.Type;

/**
 * Esport Data Provider parsing kernels factory.
 * Currently support the following:
 * <ul>
 *     <li>{@link EsportCollection}</li>
 *     <li>{@link EsportFeed}</li>
 * </ul>
 */
public final class EsportKernelFactory implements XmlParserKernelFactory
{
    public XmlParserKernel selectParser(Type outputType)
    {
        if(outputType == EsportCollection.class)
        {
            return new EsportCollection_ParserKernel();
        }else if(outputType == EsportFeed.class)
        {
            return new EsportFeed_ParserKernel();
        }

        return null;
    }
}
