package golo.compiler.utils;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

public interface Register<K, V> extends Map<K, Set<V>> {
  void add(K key, V value);

  void addAll(K key, Collection<V> values);

  void updateKey(K oldKey, K newKey);
}
