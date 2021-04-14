# CHANGELOG

## V. 1.0.0 - 2021-04- - Initial version
Elf WS Client:
* Manage GET, POST as Map<String, String> and FILE from Uri, File, InputStream and String.
* Addictional headers.
* ElfWsAuth interface with ElfAuthBasic and ElfOAuth2 classes for authentication.
* ElfWsCallback to manage the response, which is encapsuled in an ElfWsResponse object (with headers,
  response code and content of the http response).

Other material:
1. Elf WS Client Test App, with several working examples using php test server of point 3.
2. some documentation
3. A php test ws without auth, with basic auth, with custom authentication.