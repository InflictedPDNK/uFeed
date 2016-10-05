package org.pdnk.ufeed.api;

import java.lang.reflect.Type;

/**
 * Factory which provides a respective {@link XmlParserKernel} for provided data type. Kernels are used in the parsing
 * pipeline as part of API request process
 */
public interface XmlParserKernelFactory
{
    /**
     * Select parser for the provided type of data model
     * @param outputType type of model
     * @return instance of XmlParserKernel or null if no kernel available
     */
    XmlParserKernel selectParser(Type outputType);
}
