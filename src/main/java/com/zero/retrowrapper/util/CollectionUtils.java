package com.zero.retrowrapper.util;

import java.util.Collection;

public final class CollectionUtils {
    @SafeVarargs
    public static <T> void addNonNullToCollection(Collection<T> collection, T... toAdd) {
        for (final T entryToAdd : toAdd) {
            if (entryToAdd != null) {
                collection.add(entryToAdd);
            }
        }
    }

    private CollectionUtils() {
        // As this is a helper class, there should be no reason to instantiate an instance of it.
    }
}
