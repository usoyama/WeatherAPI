package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.Data.WeatherModel
import com.example.myapplication.screens.DialogSearch
import com.example.myapplication.screens.MainCard
import com.example.myapplication.screens.TabLayout
import org.json.JSONObject

const val API_KEY = "d88e8be9b3fc4e1fbac65258231810"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val daysList = remember {
                mutableStateOf(listOf<WeatherModel>())
            }
            val dialogState = remember {
                mutableStateOf(false)
            }
            val currentDay = remember {
                mutableStateOf(WeatherModel(
                    "",
                    "",
                    "0.0",
                    "",
                    "",
                    "0.0",
                    "0.0",
                    ""
                )
                )
            }
            if (dialogState.value) {
                DialogSearch(dialogState, onSubmit = {
                    getData(it, this, daysList, currentDay)
                })
            }
            getData("Moscow", this, daysList, currentDay)
            Image(
                painter = painterResource(
                    id = R.drawable.sky
                ),
                contentDescription = "image",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f),
                contentScale = ContentScale.FillBounds
            )
            Column{
                MainCard(currentDay, onClickSync = {
                    getData("Moscow", this@MainActivity, daysList, currentDay)
                }, onClickSearch = {
                    dialogState.value = true
                }
                )
                TabLayout(daysList, currentDay)
            }

        }
    }
}


private fun getData(city:String, context: Context,
                    daysList: MutableState<List<WeatherModel>>,
                    currentDay: MutableState<WeatherModel>){
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" +
            "&q=$city" +
            "&days=" +
            "3" +
            "&aqi=no&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val sRequest = StringRequest(
        Request.Method.GET,
        url,
        {
                response->
            val list = getWeatherByDays(response)
            daysList.value = list
        },
        {
                error ->
            Log.d("MyLog", "Error $error")
        }
    )
    queue.add(sRequest)
}
private fun getWeatherByDays(response: String): List<WeatherModel>{
    if(response.isEmpty())
        return listOf()
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()){
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()

            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c"),
    )
    return list
}

