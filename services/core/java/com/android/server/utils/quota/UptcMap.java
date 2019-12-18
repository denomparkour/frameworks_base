/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.utils.quota;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.util.ArrayMap;
import android.util.SparseArrayMap;

import java.util.function.Consumer;

/**
 * A SparseArrayMap of ArrayMaps, which is suitable for holding userId-packageName-tag combination
 * (UPTC)->object associations. Tags are any desired String.
 *
 * @see Uptc
 */
class UptcMap<T> {
    private final SparseArrayMap<ArrayMap<String, T>> mData = new SparseArrayMap<>();

    public void add(int userId, @NonNull String packageName, @Nullable String tag,
            @Nullable T obj) {
        ArrayMap<String, T> data = mData.get(userId, packageName);
        if (data == null) {
            data = new ArrayMap<>();
            mData.add(userId, packageName, data);
        }
        data.put(tag, obj);
    }

    public void clear() {
        mData.clear();
    }

    public boolean contains(int userId, @NonNull String packageName) {
        return mData.contains(userId, packageName);
    }

    public boolean contains(int userId, @NonNull String packageName, @Nullable String tag) {
        // This structure never inserts a null ArrayMap, so if get(userId, packageName) returns
        // null, the UPTC was never inserted.
        ArrayMap<String, T> data = mData.get(userId, packageName);
        return data != null && data.containsKey(tag);
    }

    /** Removes all the data for the user, if there was any. */
    public void delete(int userId) {
        mData.delete(userId);
    }

    /** Removes the data for the user, package, and tag, if there was any. */
    public void delete(int userId, @NonNull String packageName, @Nullable String tag) {
        final ArrayMap<String, T> data = mData.get(userId, packageName);
        if (data != null) {
            data.remove(tag);
            if (data.size() == 0) {
                mData.delete(userId, packageName);
            }
        }
    }

    /** Removes the data for the user and package, if there was any. */
    public ArrayMap<String, T> delete(int userId, @NonNull String packageName) {
        return mData.delete(userId, packageName);
    }

    /**
     * Returns the set of tag -> object mappings for the given userId and packageName
     * combination.
     */
    @Nullable
    public ArrayMap<String, T> get(int userId, @NonNull String packageName) {
        return mData.get(userId, packageName);
    }

    /** Returns the saved object for the given UPTC. */
    @Nullable
    public T get(int userId, @NonNull String packageName, @Nullable String tag) {
        final ArrayMap<String, T> data = mData.get(userId, packageName);
        return data != null ? data.get(tag) : null;
    }

    /**
     * Returns the index for which {@link #getUserIdAtIndex(int)} would return the specified userId,
     * or a negative number if the specified userId is not mapped.
     */
    public int indexOfUserId(int userId) {
        return mData.indexOfKey(userId);
    }

    /**
     * Returns the index for which {@link #getPackageNameAtIndex(int, int)} would return the
     * specified userId, or a negative number if the specified userId and packageName are not mapped
     * together.
     */
    public int indexOfUserIdAndPackage(int userId, @NonNull String packageName) {
        return mData.indexOfKey(userId, packageName);
    }

    /** Returns the userId at the given index. */
    public int getUserIdAtIndex(int index) {
        return mData.keyAt(index);
    }

    /** Returns the package name at the given index. */
    @NonNull
    public String getPackageNameAtIndex(int userIndex, int packageIndex) {
        return mData.keyAt(userIndex, packageIndex);
    }

    /** Returns the tag at the given index. */
    @NonNull
    public String getTagAtIndex(int userIndex, int packageIndex, int tagIndex) {
        // This structure never inserts a null ArrayMap, so if the indices are valid, valueAt()
        // won't return null.
        return mData.valueAt(userIndex, packageIndex).keyAt(tagIndex);
    }

    /** Returns the size of the outer (userId) array. */
    public int userCount() {
        return mData.numMaps();
    }

    /** Returns the number of packages saved for a given userId. */
    public int packageCountForUser(int userId) {
        return mData.numElementsForKey(userId);
    }

    /** Returns the number of tags saved for a given userId-packageName combination. */
    public int tagCountForUserAndPackage(int userId, @NonNull String packageName) {
        final ArrayMap data = mData.get(userId, packageName);
        return data != null ? data.size() : 0;
    }

    /** Returns the value T at the given user, package, and tag indices. */
    @Nullable
    public T valueAt(int userIndex, int packageIndex, int tagIndex) {
        final ArrayMap<String, T> data = mData.valueAt(userIndex, packageIndex);
        return data != null ? data.valueAt(tagIndex) : null;
    }

    public void forEach(Consumer<T> consumer) {
        mData.forEach((tagMap) -> {
            for (int i = tagMap.size() - 1; i >= 0; --i) {
                consumer.accept(tagMap.valueAt(i));
            }
        });
    }
}
