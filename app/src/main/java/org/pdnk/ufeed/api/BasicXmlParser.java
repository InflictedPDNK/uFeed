package org.pdnk.ufeed.api;

import android.support.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Bridge class encapsulating general parsing pipeline from XML to application data models.
 * Utilises parsing kernels for translation to respective data types.
 */
public final class BasicXmlParser implements ResponseParser
{

    private final XmlParserKernelFactory kernelFactory;

    public BasicXmlParser(XmlParserKernelFactory kernelFactory)
    {
        this.kernelFactory = kernelFactory;
    }

    @Override
    public Object parse(Reader rawData, @NonNull Type outputType) throws IOException
    {
        if(rawData == null)
            return null;

        XmlParserKernel parserKernel = kernelFactory.selectParser(outputType);

        //if no kernel available, throw a runtime error
        if(parserKernel == null)
            throw new UnsupportedOperationException(String.format("Parsing to object of type %s not supported by parser %s", outputType.toString(), getClass().getSimpleName()));

        XmlPullParser pullParser;

        //prepare parser. Handle instantiation errors separately
        try
        {
            pullParser = XmlPullParserFactory.newInstance().newPullParser();
            pullParser.setInput(rawData);
        } catch (XmlPullParserException e)
        {
            throw  new IOException("Failed to instantiate XmlPullParser", e);
        }

        //main parsing loop. Read elements and pass over to the associated kernel
        try
        {
            if(pullParser.getEventType() == XmlPullParser.START_DOCUMENT)
                parserKernel.parseStartDoc(pullParser);

            while(pullParser.next() != XmlPullParser.END_DOCUMENT)
            {
                switch(pullParser.getEventType())
                {
                    case XmlPullParser.START_DOCUMENT:
                    {
                        parserKernel.parseStartDoc(pullParser);
                    }break;
                    case XmlPullParser.START_TAG:
                    {
                        parserKernel.parseStartTag(pullParser);

                    }break;
                    case XmlPullParser.END_TAG:
                    {
                        parserKernel.parseEndTag(pullParser);
                    }break;
                }
            }

            parserKernel.parseEndDoc(pullParser);

        } catch (XmlPullParserException | IOException e)
        {
            throw  new IOException("Bad request or input data malformed", e);
        }


        return parserKernel.retrieveObject();
    }
}
