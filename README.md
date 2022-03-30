# busboy
Don't you hate it when you wait twenty minutes for the 51A, and then three show up at the same time? Busboy doesn't prevent that unfortunately, but it does allow you see that all three will be arriving simultaneously. 

Busboy uses Retrofit and Kotlin Coroutines to make its network calls, and Hilt for dependency injection. It's built on MVVM design patterns and uses LiveData and all the standard fancy tools.

Already at a bus stop? Busboy can use your stop's ID to tell you what buses are coming.  
<img src="https://www.taitsmith.com/images/ss_main_busflag.png" width=250>
<img src="https://www.taitsmith.com/images/ss_by_id_55555" width=250>  
  
Bad experience with bus predictions in the past? Pick a upcoming bus from the list, and Busboy will show you exactly where on its route that bus is. This information comes right from the bus itself:  
<img src="https://www.taitsmith.com/images/ss_bus_route.png" width=250>  
  
Not at a bus stop? If you give Busboy permission to see your location, it can show you nearby stops, as well as which lines service them. Don't know where that stop is? Busboy can show you how to get there:  
<img src="https://www.taitsmith.com/images/ss_nearby.png" width=250>
<img src="https://www.taitsmith.com/images/ss_directions.png" width=250>
