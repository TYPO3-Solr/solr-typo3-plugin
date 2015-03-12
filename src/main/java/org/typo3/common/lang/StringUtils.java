/**
 * Copyright 2010-2015 Ingo Renner <ingo@typo3.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typo3.common.lang;

import java.util.Collection;
import java.util.HashSet;

/**
 * String Utilities.
 *
 * @author Ingo Renner <ingo@typo3.org>
 * @author Phuong Doan <phuong.doan@dkd.de>
 */
public final class StringUtils {

  /**
   * Disallowing instantiation for the utility class.
   */
  private StringUtils() {
  }

  /**
   * Explodes / splits a String at delimiter and returns an Array of String
   * tokens.
   *
   * Behaves like PHP's explode() or
   * org.apache.commons.lang.StringUtils.split(), we just don't want to
   * include commons lang for just one method.
   *
   * Yes, as you might have guessed by the name of this method,
   * we usually do PHP ;)
   *
   * @param str String to split
   * @param delim Delimiter
   * @return String[] Array of String tokens
   */
  public static String[] explode(String str, String delim) {
    return org.apache.commons.lang3.StringUtils.split(str, delim);
  }

  /**
   * Implodes a collection by concatenating its elements using the specified
   * separator.
   *
   * @param collection Collection to implode
   * @param glue Glue / separator string.
   * @return String collection elements concatenated with glue.
   */
  public static String implode(Collection collection, String glue) {
	  return org.apache.commons.lang3.StringUtils.join(collection, glue);
  }

  /**
   * Converts a comma separated list of integers into a integer HashSet.
   *
   * @param list comma separated list of integers
   * @return  HashSet<Integer>  HashSet of the list's integers
   */
  public static HashSet<Integer> commaSeparatedListToIntegerHashSet(String list) {
    HashSet<Integer> integerHashSet = new HashSet<Integer>();

    String[] values = explode(list, ",");

    for (String value : values) {
      integerHashSet.add(Integer.parseInt(value));
    }

    return integerHashSet;
  }

}
