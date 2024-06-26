# busboy  


<a href='https://play.google.com/store/apps/details?id=com.taitsmith.busboy'><img alt='busboy on google play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width=200 height=auto/></a>  

(jenkins is broken and direct apk downloads from there will be back soon, probably)

Don't you hate it when you wait twenty minutes for the 51A, and then three show up at the same time? Busboy doesn't prevent that unfortunately, but it does allow you see that all three will be arriving simultaneously. 
  
Already at a bus stop? Busboy can use your stop's ID to tell you what buses are coming.  
![ss_main_busflag](https://user-images.githubusercontent.com/18432394/224452432-4e0dc5fe-926e-4466-99dd-9307277e1aea.png)
![ss_by_id](https://user-images.githubusercontent.com/18432394/224452496-86ba6e14-d040-4391-9624-98901561b004.png)  



Use the same stops frequently? Add them to your favorites instead of trying to remember the stop ID:  
![ss_favorites](https://user-images.githubusercontent.com/18432394/224452517-bc353196-aa76-46c1-b2b3-5615cbf66c95.png)

Bad experience with bus predictions in the past? Pick an upcoming bus from the list, and Busboy will show you exactly where on its route that bus is. This information comes right from the bus itself:  
![image](https://user-images.githubusercontent.com/18432394/224452532-1fdb89b5-c077-4d83-9877-b74b306dce5f.png)

Not at a bus stop? If you give Busboy permission to see your location, it can show you nearby stops, as well as which lines service them. Don't know where that stop is? Busboy can show you how to get there:  
![image](https://user-images.githubusercontent.com/18432394/224452561-29ab2a39-dccd-4467-93ed-8c3c9093c7de.png)
![image](https://user-images.githubusercontent.com/18432394/224452579-5fba99c6-e5d4-4dc6-91ef-4c8380b94a21.png)  

New in 1.2.0: Drag and drop marker to search for nearby stops without letting Busboy see your location:  
![Screenshot_20230629_143121](https://github.com/taitsmith/busboy/assets/18432394/07db5414-c536-4107-a14a-2352c055e250). 

Don't care about stops that don't serve the 51A? Tell Busboy what lines you're looking for:  
![image](https://user-images.githubusercontent.com/18432394/224452590-a796598b-d14a-449f-b92d-02306923cb19.png)  


Busboy uses Retrofit and Kotlin Coroutines to make its network calls, Hilt for DI and Room for offline storage. It's built on MVVM design patterns and uses LiveData and all the standard fancy tools.
