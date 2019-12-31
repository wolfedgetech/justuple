package bdkosher.justuple;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Set;

/**
 * A Map containing only one entry.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
class SingleEntryMap<K, V> extends AbstractMap<K, V> {

    private final Entry<K, V> tupleEntry;

    SingleEntryMap(Entry<K, V> tupleEntry) {
        this.tupleEntry = tupleEntry;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.singleton(tupleEntry);
    }
}
