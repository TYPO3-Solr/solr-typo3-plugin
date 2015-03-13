
![Travis Build Status](https://travis-ci.org/TYPO3-Solr/solr-typo3-plugin.svg?branch=master)

# Solr TYPO3 Plugin

The Solr TYPO3 plugin provides the AccessFilterQParserPlugin, which is a 
org.apache.solr.search.QParserPlugin that allows to restrict access to Solr 
documents indexed by TYPO3 on a document level.

It filters the documents returned from a query by performing set operations
between a particular string field in the index and the specified value. These
values are comma separated lists of user group IDs, the index field stores the
groups that are allowed to access a particular document while the groups the
user is a member of / has access to are specified through a filter parameter.

## Example:

http://localhost:8983/path/to/solr/select/?fq={!typo3access field=fieldname}permittedUserGroups&q=...
http://localhost:8983/solr/collection1/select/?q=title:Hello&fq={!typo3access}0,1,4,10

* The field parameter is optional and defaults to "access"
* fieldname is the name of the string field holding the access restrictions for a document
* permittedUserGroups is the comma separated list of groups the user has access to

To use this plugin, simply copy the jar file containing the plugin classes into your
$SOLR_HOME/lib directory and then add the following to your solrconfig.xml file:

```xml
<queryParser name="typo3access" class="org.typo3.solr.search.AccessFilterQParserPlugin" />
```

In your schema.xml add a field like this:

```xml
<field name="access" type="string" indexed="false" stored="true" docValues="true" default="c:0" />
```

It is important to turn on DocValues. The field does not need to be stored, 
but it helps with debugging.


Finally, restart your servlet container.

