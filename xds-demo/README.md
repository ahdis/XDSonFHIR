# XDS DEMO
This projects contains experiments to get familiar with the [eHealthConnector](https://sourceforge.net/p/ehealthconnector/wiki/Home/) library.

In particular, it shows how to:
* Query an XDS repository for a patient's documents
* Upload a document

## The XDS demo has 4 main methods

## `doDemo()`
`doDemo()` is the main method where you set the destination repository/registry as well as the affinity domain and you create your convenience communication. It calls `queryDocuments()` and `uploadDocument()`.

## `queryDocuments()`
Using the convenience communication created above, `queryDocuments()` retrieves the last three documents that were uploaded in the specified repository and prints them in the shell.

## `uploadDocument()`
Uploads a document (from the XML file `test.xml`) with some metadata.

## `printXdsResponse()`
Convenience method for printing error messages from the provided `XDSStatusType`.
