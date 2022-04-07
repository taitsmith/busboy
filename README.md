# busboy
Don't you hate it when you wait twenty minutes for the 51A, and then three show up at the same time? Busboy doesn't prevent that unfortunately, but it does allow you see that all three will be arriving simultaneously. 

Busboy uses Retrofit and Kotlin Coroutines to make its network calls, and Hilt for dependency injection. It's built on MVVM design patterns and uses LiveData and all the standard fancy tools.
  
Already at a bus stop? Busboy can use your stop's ID to tell you what buses are coming.  
![ss_main_busflag](https://user-images.githubusercontent.com/18432394/162332692-c1d788ca-9938-4842-b88e-9a65161ce74b.png)
![ss_by_id_55555](https://user-images.githubusercontent.com/18432394/162332699-1b3e5736-7693-408f-8efc-bf0ab4536865.png)



Bad experience with bus predictions in the past? Pick a upcoming bus from the list, and Busboy will show you exactly where on its route that bus is. This information comes right from the bus itself:  
![ss_bus_route](https://user-images.githubusercontent.com/18432394/162332715-bcf522b3-6af0-4a2b-9a44-ce7dbc8d41bc.png)

Not at a bus stop? If you give Busboy permission to see your location, it can show you nearby stops, as well as which lines service them. Don't know where that stop is? Busboy can show you how to get there:  
![ss_nearby](https://user-images.githubusercontent.com/18432394/162332731-56e7b0d9-4508-47f5-8164-5705162649fb.png)
![ss_directions](https://user-images.githubusercontent.com/18432394/162332794-ad6e2e9d-3dc7-4610-b38e-3b2ac1bbe6ba.png)



Install from an [.apk](https://www.taitsmith.com/apks/busboy_debug_2022-04-07.apk) and verify with a [.sig](https://www.taitsmith.com/apks/busboy_debug_2022-04-07.apk.sig) and [this key](https://www.taitsmith.com/apks/ts_public.key). You can also build it yourself from the source, but you'll need to supply AC Transit and Google API keys.
