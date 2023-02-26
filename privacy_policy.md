# busboy privacy
the only personal information busboy will ever access is your location, if you've given it permission. this information is included in api calls to ac transit (for determining nearby stops) and google (for determining walking directions to nearby stops). your location is accessed when you search for a nearby stop [here](https://github.com/taitsmith/busboy/blob/38be64cf5dcce2ddd4e2e7591d942e6787b9d7af/app/src/main/java/com/taitsmith/busboy/di/LocationRepository.kt#L30), and is updated every ten minutes if the app is running in the foreground, or when you request a new search. this information is not stored by busboy and is forgotten [here](https://github.com/taitsmith/busboy/blob/38be64cf5dcce2ddd4e2e7591d942e6787b9d7af/app/src/main/java/com/taitsmith/busboy/di/LocationRepository.kt#L45) when the 'nearby' screen is closed.
  
No other user data is accessible by busboy, and the app will function normally (without the use of 'nearby' search) if the permission is denied or location access is turned off.  

[Location use with google api call](https://github.com/taitsmith/busboy/blob/38be64cf5dcce2ddd4e2e7591d942e6787b9d7af/app/src/main/java/com/taitsmith/busboy/api/ApiInterface.kt#L58)  
[Location use with ac transit api call](https://github.com/taitsmith/busboy/blob/38be64cf5dcce2ddd4e2e7591d942e6787b9d7af/app/src/main/java/com/taitsmith/busboy/api/ApiInterface.kt#L20)  

