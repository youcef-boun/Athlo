package com.youcef_bounaas.athlo.Stats.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL

data class LatLng(val latitude: Double, val longitude: Double)

suspend fun fetchAndParseGpx(gpxUrl: String): List<LatLng> = withContext(Dispatchers.IO) {
    val points = mutableListOf<LatLng>()
    val urlStream = URL(gpxUrl).openStream()
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(urlStream, null)
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "trkpt") {
            val lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
            val lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
            if (lat != null && lon != null) {
                points.add(LatLng(lat, lon))
            }
        }
        eventType = parser.next()
    }
    urlStream.close()
    points
}