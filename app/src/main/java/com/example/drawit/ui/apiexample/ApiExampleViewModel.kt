package com.example.drawit.ui.apiexample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawit.data.remote.ChuckNorrisApiService
import com.example.drawit.domain.model.JokeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiExampleViewModel : ViewModel() {
    private val api : ChuckNorrisApiService = Retrofit.Builder()
        .baseUrl("https://api.chucknorris.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ChuckNorrisApiService::class.java)

    private val _response = MutableLiveData<JokeResponse?>()
    val response: LiveData<JokeResponse?> = _response

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    fun fetchJoke(cat: String) {
        // prevent spamming
        if (_isLoading.value == true) {
            return
        }

        // start a new coroutine
        // Dispatchers.IO - "IO-intensive blocking operations (like file I/O and blocking socket I/O)."
        // dealing with socket aka network
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            _error.postValue(null)

            // we take the latest observer update in fragment, don't have to set every "state"
            // to default
            try {
                val joke : JokeResponse
                System.err.println(cat)
                if (cat.isEmpty() || cat == "No Category"){
                    joke = api.getRandomJoke()
                } else {
                    joke = api.getJokeByCategory(cat)
                }
                _response.postValue(joke)
            } catch (_: java.net.UnknownHostException) {
                _error.postValue("No internet connection.")
            } catch (e: Exception) {
                _error.postValue("err: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchCategories() {
        if (_isLoading.value == true) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                val cats = api.getCategories()
                _categories.postValue(cats)
            } catch (e: Exception) {
                _error.postValue("Failed to load categories: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }

        }

    }
}