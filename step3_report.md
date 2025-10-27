# Step 3: API Integration Report

API chosen: https://api.chucknorris.io/  
Simple API, no authentication required  
  
Endpoint used:  
`GET` `https://api.chucknorris.io/jokes/random`  
```json
{
    "icon_url" : "https://api.chucknorris.io/img/avatar/chuck-norris.png",
    "id" : "pvfBgfZBTiKhJUwYMuedtg",
    "url" : "https://api.chucknorris.io/jokes/pvfBgfZBTiKhJUwYMuedtg",
    "value" : "Chuck Norris once lapped his opponent, in a drag race."
} 
```
  
Errors are handled by multiple `try-except` blocks to catch network issues and JSON parsing errors.  
```kotlin
try {
    // try and fetch joke
    val joke = api.getRandomJoke()
    _response.postValue(joke)
} catch (_: java.net.UnknownHostException) {
    // set error for no internet
    _error.postValue("No internet connection.")
} catch (e: Exception) {
    // set generic error
    _error.postValue("err: ${e.message}")
} finally {
    // stop loading indicator
    _isLoading.postValue(false)
}
```