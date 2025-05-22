package com.example.gravitfit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val selectedAthleteId = MutableLiveData<Int>()
}
