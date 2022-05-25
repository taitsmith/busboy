# busboy
Don't you hate it when you wait twenty minutes for the 51A, and then three show up at the same time? Busboy doesn't prevent that unfortunately, but it does allow you see that all three will be arriving simultaneously. 

Busboy uses Retrofit and Kotlin Coroutines to make its network calls, and Hilt for dependency injection. It's built on MVVM design patterns and uses LiveData and all the standard fancy tools.
  
Already at a bus stop? Busboy can use your stop's ID to tell you what buses are coming.  
![ss_main_busflag](https://user-images.githubusercontent.com/18432394/162337113-0e7d8ba2-41af-4bb2-b3dc-104b3892f1ae.png)
![ss_by_id_55555](https://user-images.githubusercontent.com/18432394/162337116-d62bea26-b86c-4ee9-a495-edfb6a5ea7d3.png)


Bad experience with bus predictions in the past? Pick a upcoming bus from the list, and Busboy will show you exactly where on its route that bus is. This information comes right from the bus itself:  
 ![ss_bus_route](https://user-images.githubusercontent.com/18432394/162337127-0e3a9afc-b158-46d8-8057-9bccafffdf6d.png)

Not at a bus stop? If you give Busboy permission to see your location, it can show you nearby stops, as well as which lines service them. Don't know where that stop is? Busboy can show you how to get there:  
![ss_nearby](https://user-images.githubusercontent.com/18432394/162337136-8b3108f3-1586-4561-a613-31d31fb26b78.png)
![ss_directions](https://user-images.githubusercontent.com/18432394/162337188-68d3a3d0-1294-404b-b473-f0f1f4f807c8.png)

  
Install from an [.apk](https://www.taitsmith.com/apks/busboy_debug_2022-05-25.apk) and verify with a [.sig](https://www.taitsmith.com/apks/busboy_debug_2022-05-25.apk.sig) and [this key](https://www.taitsmith.com/apks/ts_public.key). You can also build it yourself from the source, but you'll need to supply AC Transit and Google API keys.
