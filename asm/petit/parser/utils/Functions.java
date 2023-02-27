package petit.parser.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

import petit.parser.PetitParser;

/**
 * Constructor and utility methods for functions.
 */
public interface Functions {

  /**
   * Returns a function that returns the first value of a list.
   */
  static <T> Function<List<T>, T> firstOfList() {
    return list -> list.get(0);
  }

  /**
   * Returns a function that returns the last value of a list.
   */
  static <T> Function<List<T>, T> lastOfList() {
    return list -> list.get(list.size() - 1);
  }

  /**
   * Returns a function that returns the value at the given index.
   * Negative indexes are counted from the end of the list.
   */
  static <T> Function<List<T>, T> nthOfList(int index) {
    return list -> list.get(index < 0 ? list.size() + index : index);
  }

  /**
   * Returns a function that returns the permutation of a given list.
   * Negative indexes are counted from the end of the list.
   */
  static <T> Function<List<T>, List<T>> permutationOfList(int... indexes) {
    return list -> {
      var result = new ArrayList<T>(indexes.length);
      for (var index : indexes) {
        result.add(list.get(index < 0 ? list.size() + index : index));
      }
      return result;
    };
  }

  /**
   * Returns a function that skips the separators of a given list, see {@link PetitParser#separatedBy(PetitParser)}.
   */
  static <T> Function<List<T>, List<T>> withoutSeparators() {
    return list -> {
      var result = new ArrayList<T>();
      for (var i = 0; i < list.size(); i += 2) {
        result.add(list.get(i));
      }
      return result;
    };
  }

  /**
   * Returns a function that returns a constant value.
   */
  static <T> Function<Object, T> constant(T output) {
    return input -> output;
  }

}
