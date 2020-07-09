package com.krakenrs.spade.guice;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;

/**
 * A general {@link Scope}, aka a {@link Provider} provider that uses the given type parameter as an extra degree of
 * freedom for scoping object instances.
 * <p>
 * This class is intended to be used by framework to manage the runtime lifecycle of objects in given contexts (scopes).
 * This is done by calling the {@link #enter(C)} and {@link #exit(C)} methods before and after any injections
 * are done by the Guice framework.
 * 
 * @author Bilal Tariq
 *
 * @param <C> The context type, i.e. the differentiator between different instances of an object in a class(see Guice
 *     Servlet scoping for examples)
 */
public class ContextScope<C> implements Scope {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextScope.class);

    /**
     * A mapping from essentially (currentThread, currentContext, {@link Key}) to a given instance of an object that is
     * reprsented by the key.
     */
    private final ThreadLocal<Map<C, Map<Key<?>, Object>>> values = new ThreadLocal<>();
    /**
     * A {@link ThreadLocal} storage for getting the context of the current calling thread
     */
    private final ThreadLocal<C> context = new ThreadLocal<>();

    /**
     * Gets the context of the current thread
     * 
     * @return The current context
     */
    public C getCurrentContext() {
        LOGGER.trace("Requested current scope: {}" + context.get());
        return context.get();
    }

    /**
     * Begins a new context, see the Javadoc for {@link ContextScope}
     * 
     * @param newContext
     */
    public void enter(C newContext) {
        LOGGER.trace("Entering scope: {}", newContext);
        Map<C, Map<Key<?>, Object>> contextValues = values.get();

        if (context.get() != null) {
            throw new IllegalStateException("Context is currently active on this thread");
        }
        values.set(contextValues = new HashMap<>());

        if (contextValues.containsKey(newContext)) {
            throw new UnsupportedOperationException();
        } else {
            context.set(newContext);
            contextValues.put(newContext, new HashMap<>());
        }
    }

    /**
     * Exits the current/given, see the Javadoc for {@link ContextScope}
     * 
     * @param currentContext Must match the current context that is being exited
     */
    public void exit(C currentContext) {
        LOGGER.trace("Exiting scope: {}, currentActual: ", currentContext, context.get());
        Map<C, Map<Key<?>, Object>> contextValues = values.get();

        if (contextValues == null) {
            throw new IllegalStateException("No context set");
        }

        if (!contextValues.containsKey(currentContext)) {
            throw new UnsupportedOperationException();
        } else {
            context.set(null);
            contextValues.remove(currentContext);
        }
    }

    /**
     * Provides a pre initialised object to be put in the cache for the current context.
     * 
     * @param <T> The type of the object
     * @param key A key representing the type of the object, possibly with extra parameters
     * @param value The object itself
     */
    public <T> void seed(Key<T> key, T value) {
        LOGGER.trace("Seeding key: {} with value:", key, value);
        Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);
        checkState(!scopedObjects.containsKey(key),
                "A value for the key %s was " + "already seeded in this scope. Old value: %s New value: %s", key,
                scopedObjects.get(key), value);
        scopedObjects.put(key, value);
        LOGGER.trace(" After seeding: {}", scopedObjects);
    }

    /**
     * Provides a pre initialised object to be put in the cache for the current context.
     * See {@link #seed(Key, Object)}
     * 
     * @param <T> The type of the object
     * @param clazz A Class representing the type of the object
     * @param value The object itself
     */
    public <T> void seed(Class<T> clazz, T value) {
        seed(Key.get(clazz), value);
    }

    @Override
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        LOGGER.trace("Making scoped provider for: {}", key);
        return new Provider<T>() {
            public T get() {
                LOGGER.trace("Scoped provider get: {}", key);
                Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

                @SuppressWarnings("unchecked")
                T current = (T) scopedObjects.get(key);
                if (current == null && !scopedObjects.containsKey(key)) {
                    LOGGER.trace(" Cache miss... getting");
                    current = unscoped.get();
                    LOGGER.trace(" Got: {}", current);

                    // don't remember proxies; these exist only to serve circular dependencies
                    if (Scopes.isCircularProxy(current)) {
                        LOGGER.trace(" Was circular, not caching");
                        return current;
                    }

                    scopedObjects.put(key, current);
                } else {
                    LOGGER.trace(" Cache hit: {}", current);
                }
                return current;
            }
            
            @Override
            public String toString() {
                return unscoped.toString();
            }
        };
    }

    private <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
        Map<C, Map<Key<?>, Object>> scopedObjects = values.get();
        if (scopedObjects == null || context.get() == null) {
            throw new OutOfScopeException("Cannot access " + key + " outside of a scoping block");
        }
        Map<Key<?>, Object> cache = scopedObjects.get(context.get());
        LOGGER.trace(" Finding {}", key);
        LOGGER.trace("     in: {}", cache);
        return cache;
    }
}
