/**
 * Copyright 2011 Ingo Renner <ingo@typo3.org>
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

package org.typo3.solr.handler.admin;

/**
 * Enum to differentiate between read and write mode for the configuration
 * request handler.
 *
 * @author Ingo Renner <ingo@typo3.org>
 */
public enum ConfigurationHandlerMode {

  /**
   * write mode.
   */
  WRITE,

  /**
   * read mode.
   */
  READ

}
