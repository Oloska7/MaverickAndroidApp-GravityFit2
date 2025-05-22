package com.example.gravitfit

import androidx.lifecycle.ViewModel
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

data class VelocityData(
    val dt: Double,
    val jointVelocities: Map<String, List<Double?>> // List can contain null for undefined initial velocity
)
data class SelectionData(
    val athleteId: String,
    val jumpType: String
)
class OpenPoseViewModel(application: Application) : AndroidViewModel(application) {

    private val _velocityData = MutableLiveData<VelocityData?>()
    val velocityData: LiveData<VelocityData?> = _velocityData
    // LiveData for the selection
    private val _currentSelection = MutableLiveData<SelectionData?>()
    val currentSelection: LiveData<SelectionData?> = _currentSelection

    private var currentLoadedFile: String? = null

    // Function in ViewModel to set the selection (called by Activity)
    fun setSelection(athleteId: String, jumpType: String) {
        val newSelection = SelectionData(athleteId, jumpType)
        if (_currentSelection.value != newSelection) { // Only update if different
            _currentSelection.value = newSelection
            loadDataForSelection(newSelection)
        }
    }
    private fun loadDataForSelection(selection: SelectionData) {
        val fileName = "calculated_angles/${selection.athleteId}_${selection.jumpType}_filtered_angles.json"
        if (fileName == currentLoadedFile && _velocityData.value != null) {
            return // Already loaded
        }
        currentLoadedFile = fileName
        viewModelScope.launch {
            _velocityData.postValue(null) // Clear old data
            val result = loadVelocitiesFromJson(getApplication(), fileName) // Your existing function
            _velocityData.postValue(result)
        }
    }

    private suspend fun loadVelocitiesFromJson(context: Context, fileName: String): VelocityData? {
        return withContext(Dispatchers.IO) {
            val jsonString: String
            try {
                val inputStream = context.assets.open(fileName)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                jsonString = String(buffer, Charset.defaultCharset())
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                return@withContext null
            }

            try {
                val rootObject = JSONObject(jsonString)
                val dt = rootObject.getDouble("dt")
                val velocitiesObject = rootObject.getJSONObject("joint_angles_filtered")

                val jointVelocitiesMap = mutableMapOf<String, List<Double?>>()
                val keys = velocitiesObject.keys()

                while (keys.hasNext()) {
                    val jointName = keys.next()
                    val velocitiesArray = velocitiesObject.getJSONArray(jointName)
                    val velocitiesForJoint = mutableListOf<Double?>()
                    for (i in 0 until velocitiesArray.length()) {
                        if (velocitiesArray.isNull(i)) {
                            velocitiesForJoint.add(null)
                        } else {
                            velocitiesForJoint.add(velocitiesArray.getDouble(i))
                        }
                    }
                    jointVelocitiesMap[jointName] = velocitiesForJoint
                }
                VelocityData(dt, jointVelocitiesMap)
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    fun loadVelocityData(fileName: String) {
        viewModelScope.launch {
            _velocityData.postValue(loadVelocitiesFromJson(getApplication(), fileName))
        }
    }
}