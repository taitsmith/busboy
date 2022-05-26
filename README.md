# busboy
Don't you hate it when you wait twenty minutes for the 51A, and then three show up at the same time? Busboy doesn't prevent that unfortunately, but it does allow you see that all three will be arriving simultaneously. 

Busboy uses Retrofit and Kotlin Coroutines to make its network calls, and Hilt for dependency injection. It's built on MVVM design patterns and uses LiveData and all the standard fancy tools.
  
Already at a bus stop? Busboy can use your stop's ID to tell you what buses are coming.  
![ss_main_busflag](https://user-images.githubusercontent.com/18432394/170372038-3955226e-4f29-4bfe-a9bf-8ddd0f3bf5a9.png)
![ss_by_id_55555](https://user-images.githubusercontent.com/18432394/170372053-3da6c2cd-0e96-4388-befa-531bc3514d93.png)

Use the same stops frequently? Add them to your favorites instead of trying to remember the stop ID:
![ss_favorites](https://user-images.githubusercontent.com/18432394/170372574-38015540-f1e4-4cc5-938a-63a93e355fa1.png)


Bad experience with bus predictions in the past? Pick a upcoming bus from the list, and Busboy will show you exactly where on its route that bus is. This information comes right from the bus itself:  
![ss_bus_route](https://user-images.githubusercontent.com/18432394/170372066-cd3222cf-83b1-4a5b-b3d0-20d1257e5c88.png)

Not at a bus stop? If you give Busboy permission to see your location, it can show you nearby stops, as well as which lines service them. Don't know where that stop is? Busboy can show you how to get there:  
![ss_nearby](https://user-images.githubusercontent.com/18432394/170372122-4a5700e9-1125-42f4-a704-1ba84f8cdffb.png)
![ss_directions](https://user-images.githubusercontent.com/18432394/162337188-68d3a3d0-1294-404b-b473-f0f1f4f807c8.png)

Don't care about stops that don't serve the 57? Tell Busboy what lines you're looking for:  
![ss_nearby_57](https://user-images.githubusercontent.com/18432394/170372414-13a6871f-5176-44d7-be5c-c18c4f852c48.png)  

Install from an [.apk](https://www.taitsmith.com/apks/busboy_debug_2022-05-25.apk) and verify with a [.sig](https://www.taitsmith.com/apks/busboy_debug_2022-05-25.apk.sig) and [this key](https://www.taitsmith.com/apks/ts_public.key). You can also build it yourself from the source, but you'll need to supply AC Transit and Google API keys.
