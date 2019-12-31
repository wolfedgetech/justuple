package bdkosher.justuple;

import java.util.Map;

/**
 * A Map.Entry that has direct references to its key and value. Not thread safe.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
class SimpleMapEntry<K, V> implements Map.Entry<K, V> {

    private final K key;
    private V value;

    SimpleMapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V original = this.value;
        this.value = value;
        return original;
    }
}
