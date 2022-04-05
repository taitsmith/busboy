# busboy
Don't you hate it when you wait twenty minutes for the 51A, and then three show up at the same time? Busboy doesn't prevent that unfortunately, but it does allow you see that all three will be arriving simultaneously. 

Busboy uses Retrofit and Kotlin Coroutines to make its network calls, and Hilt for dependency injection. It's built on MVVM design patterns and uses LiveData and all the standard fancy tools.
  
Already at a bus stop? Busboy can use your stop's ID to tell you what buses are coming.  
![ss_main_busflag](https://user-images.githubusercontent.com/18432394/161662192-66eb91c7-e204-4cff-9349-86239f3ca722.png)
![ss_by_id_55555](https://user-images.githubusercontent.com/18432394/161662245-25eeab72-ed06-419c-885e-dec87b3f8dde.png)

  
Bad experience with bus predictions in the past? Pick a upcoming bus from the list, and Busboy will show you exactly where on its route that bus is. This information comes right from the bus itself:  
 ![ss_bus_route](https://user-images.githubusercontent.com/18432394/161662287-150af353-df5c-4854-907e-eb04529a9095.png)
 
Not at a bus stop? If you give Busboy permission to see your location, it can show you nearby stops, as well as which lines service them. Don't know where that stop is? Busboy can show you how to get there:  
![ss_nearby](https://user-images.githubusercontent.com/18432394/161665876-e8664647-2891-4b45-9f89-e4ce29bf4574.png)
![ss_directions](https://user-images.githubusercontent.com/18432394/161662322-2f6549ed-71f8-4fb8-b4bb-631d73f8b6f6.png)


Install from an [.apk](https://www.taitsmith.com/apks/busboy_debug_2022-03-31.apk) and verify with a [.sig](https://www.taitsmith.com/apks/busboy_debug_2022-03-31.apk.sig) and [this key](https://www.taitsmith.com/apks/ts_public.key). You can also build it yourself from the source, but you'll need to supply AC Transit and Google API keys.
