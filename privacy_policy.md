# busboy privacy
the only personal information busboy will ever access is your location, if you've given it permission. this information is included in api calls to ac transit (for determining nearby stops) and google (for determining walking directions to nearby stops), and only accessed when the app is running in the foreground a search for nearby stops or walking directions is requested. it is never stored by busboy and is forgotten when the app is closed.
  
No other user data is accessible by busboy, and the app will function normally (without the use of 'nearby' search) if the permission is denied or location access is turned off.  

[Location request](https://github.com/taitsmith/busboy/blob/19a4fd8ead9b5376fbb89c1ca670c3f92b464aa5/app/src/main/java/com/taitsmith/busboy/ui/MainActivity.java#L227)  
[Location use with google api call](https://github.com/taitsmith/busboy/blob/19a4fd8ead9b5376fbb89c1ca670c3f92b464aa5/app/src/main/java/com/taitsmith/busboy/utils/ApiInterface.kt#L62)  
[Location use with ac transit api call](https://github.com/taitsmith/busboy/blob/19a4fd8ead9b5376fbb89c1ca670c3f92b464aa5/app/src/main/java/com/taitsmith/busboy/utils/ApiInterface.kt#L31)  
