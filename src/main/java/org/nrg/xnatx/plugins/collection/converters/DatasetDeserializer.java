package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xft.security.UserI;

@Slf4j
public abstract class DatasetDeserializer<T> extends StdDeserializer<T> {
    protected DatasetDeserializer(final Class<T> datasetClass) {
        super(datasetClass);

        if (XnatExperimentdata.class.isAssignableFrom(datasetClass)) {
            final String methodName = "get" + datasetClass.getSimpleName() + "ById";
            final Optional<Method> getter = Iterables.tryFind(Arrays.asList(_valueClass.getMethods()), new Predicate<Method>() {
                @Override
                public boolean apply(final Method method) {
                    return StringUtils.equals(method.getName(), methodName) && method.getReturnType().equals(datasetClass) && Arrays.equals(GET_PARAMS, method.getParameterTypes()) && Modifier.isStatic(method.getModifiers());
                }
            });
            try {
                _getter = getter.or(XnatExperimentdata.class.getMethod("getXnatExperimentdatasById", GET_PARAMS));
            } catch (NoSuchMethodException e) {
                log.error("An error occurred trying to get the method XnatExperimentdata.getXnatExperimentdatasById()", e);
                throw new RuntimeException(e);
            }
        } else {
            _getter = null;
        }
    }

    protected T getInstance(final JsonNode node) {
        return node.has("id") ? getInstance(node.get("id").textValue()) : getInstance();
    }

    protected T getInstance() {
        return getInstance("");
    }

    @SuppressWarnings("unchecked")
    protected T getInstance(final String id) {
        if (StringUtils.isBlank(id) || _getter == null) {
            try {
                return (T) _valueClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("An error occurred trying to create a new instance of the {} type", _valueClass.getName(), e);
                throw new RuntimeException(e);
            }
        }
        try {
            return (T) _getter.invoke(null, id, null, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("An error occurred trying to invoke the method {}.{}() with the ID {}", _getter.getDeclaringClass().getName(), _getter.getName(), id, e);
            throw new RuntimeException(e);
        }
    }

    private static final Class<?>[] GET_PARAMS = new Class<?>[]{Object.class, UserI.class, boolean.class};

    private final Method _getter;
}
