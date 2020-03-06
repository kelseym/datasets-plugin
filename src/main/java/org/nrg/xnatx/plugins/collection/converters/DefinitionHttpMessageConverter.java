package org.nrg.xnatx.plugins.collection.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.commons.io.IOUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

/**
 * Handles converting XFT classes
 */
@Component
public class DefinitionHttpMessageConverter extends AbstractHttpMessageConverter<SetsDefinition> {
    public DefinitionHttpMessageConverter(final SerializerService serializer) {
        // For JSON, should be able to use the serializer/deserializer implementations,
        // but see if this needs to be restored in here.
        // super(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
        super(MediaType.APPLICATION_XML);
        _serializer = serializer;
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return SetsDefinition.class.isAssignableFrom(clazz);
    }

    @Override
    protected SetsDefinition readInternal(final Class<? extends SetsDefinition> clazz, final HttpInputMessage message) throws IOException, HttpMessageNotReadableException {
        return message.getHeaders().getContentType() == MediaType.APPLICATION_JSON ? readJson(message) : readXml(message);
    }

    @Override
    protected void writeInternal(final SetsDefinition definition, final HttpOutputMessage message) throws HttpMessageNotWritableException {
        if (message.getHeaders().getContentType() == MediaType.APPLICATION_JSON) {
            writeJson(definition, message);
        } else {
            writeXml(definition, message);
        }
    }

    private SetsDefinition readJson(final HttpInputMessage message) throws IOException {
        try (final InputStream input = message.getBody()) {
            return _serializer.getObjectMapper().readValue(input, SetsDefinition.class);
        }
    }

    private SetsDefinition readXml(final HttpInputMessage message) throws IOException {
        try (final InputStreamReader input = new InputStreamReader(message.getBody())) {
            final String value = IOUtils.toString(input);
            return new SetsDefinition(XFTItem.NewItem(value, null));
        } catch (ElementNotFoundException e) {
            throw new HttpMessageNotReadableException("Couldn't create the requested type of object: " + e.ELEMENT, e);
        } catch (XFTInitException e) {
            throw new HttpMessageNotReadableException("An error occurred trying to access XFT", e);
        }
    }

    @SuppressWarnings("unused")
    private void writeJson(final SetsDefinition definition, final HttpOutputMessage message) {

    }

    private void writeXml(final SetsDefinition definition, final HttpOutputMessage message) {
        try (final Writer output = new OutputStreamWriter(message.getBody())) {
            definition.toXML(output, true);
        } catch (Exception e) {
            throw new HttpMessageNotReadableException("An error occurred writing the provided object", e);
        }
    }

    private final SerializerService            _serializer;
}

