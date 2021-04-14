# Android Elf WS Client

Android Elf WS Client is a small library to simplify the communication with REST web services in
Android Apps.

## Installation

Clone or download the zip, put the `elfws` module in the root of your project and add the module
to the dependencies of your App.

# Test notes
The git contains:
 * Elf Ws Client module
 * Elf Ws Client Test App
 * a small php rest ws to test the App
   * ws.php - plain rest ws
   * ws-basic.php - rest ws requiring basic auth (user: elfws, password: test)
   * ws-custom-auth.php - rest ws requiring a custom header as authentication.

To try the library you can:
1. clone the git
2. build and install the test App
3. put the `php-ws-test` directory in some WAMP/LAMP/XAMP server
4. try the App calling the WS of point 3

## Usage
Here an usage example. See [USAGE](https://gitlab.com/zagonico/elf-ws-client/-/blob/master/USAGE.md) for more examples.

```java
ElfWsClient client = new ElfWsClient(url);

final Activity local = this;
client.setCallback(response -> {
    if (response.getType() == ElfWsResponse.TYPE_JSON) {
         try {
              Log.d("TEST", response.getJsonObject().getString("message"));
         }
         catch (Exception e) {

         }
    }
    else {
        Log.d("TEST", response.getFilename()+": "+response.getMime());
    }
});

Map<String,String> map = new ArrayMap<>();
map.put("getparam", paramGet);
client.addGet(map);

map = new ArrayMap<>();
map.put("param1", paramPost);
client.addPost(map);

Uri file1 = null /* get a file somehow */;
client.addFile(getContext(), file1, null, "file1");

client.executeRequest();
```

If you need to call several you can extend `ElfWsClient` as in `ElfWsTest` example.

## Library overview
The library as 4 element:
 * ElfWsClient: the client class that manages requests data, performs the request and manages the
response.
 * ElfWsResponse: an object to wrap the http response
 * ElfWsCallback: an interface to allow user to define a way to elaborate the response
 * ElfWsUtil: some useful method when working with WSs.


## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## Authors

* **Nicola Zago** - *Initial work*

See also the list of [contributors](https://gitlab.com/zagonico/elf-ws-client/contributors) who participated in this project.

## License
[MIT](https://choosealicense.com/licenses/mit/)