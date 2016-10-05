package org.pdnk.ufeed.api;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Parser interface. Serves as a translator between API response and Application's model types. Implementations follow
 * the protocols specified by respective Data Provider APIs.
 */
public interface ResponseParser
{
    /**
     * Parse response data
     * @param rawData incoming response data in a raw format.
     * @param outputType type of object to translate response to
     * @return translated data or null
     * @throws IOException if incoming data is malformed or output type not supported by the implementation
     */
    Object parse(Reader rawData, Type outputType) throws IOException;
}
