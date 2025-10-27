package com.example.drawit.domain.model

/*
{
"icon_url" : "https://api.chucknorris.io/img/avatar/chuck-norris.png",
"id" : "83tXCyE7TV63xDyoGNMDdw",
"url" : "",
"value" : "Chuck Norris once had a street named after him, but they had to change the name because nobody crosses Chuck Norris and lives"
}
 */
data class JokeResponse(
    var icon_url: String,
    var id: String,
    var url: String,
    var value: String
)