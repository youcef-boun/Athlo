package com.youcef_bounaas.athlo.Maps.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MapsViewModel : ViewModel() {
    
    // Camera position state
    private val _cameraPosition = MutableStateFlow<CameraOptions?>(null)
    val cameraPosition: StateFlow<CameraOptions?> = _cameraPosition.asStateFlow()
    
    // Route creation state
    private val _isCreatingRoute = MutableStateFlow(false)
    val isCreatingRoute: StateFlow<Boolean> = _isCreatingRoute.asStateFlow()
    
    private val _routeWaypoints = MutableStateFlow<List<Point>>(emptyList())
    val routeWaypoints: StateFlow<List<Point>> = _routeWaypoints.asStateFlow()
    
    private val _snappedRoute = MutableStateFlow<List<Point>>(emptyList())
    val snappedRoute: StateFlow<List<Point>> = _snappedRoute.asStateFlow()
    
    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    // Save camera position
    fun saveCameraPosition(cameraOptions: CameraOptions) {
        _cameraPosition.value = cameraOptions
    }
    
    // Route creation methods
    fun startRouteCreation() {
        _isCreatingRoute.value = true
        _routeWaypoints.value = emptyList()
        _snappedRoute.value = emptyList()
    }
    
    fun addRouteWaypoint(point: Point) {
        _routeWaypoints.update { currentWaypoints ->
            currentWaypoints + point
        }
    }
    
    fun finishRouteCreation() {
        _isCreatingRoute.value = false
    }
    
    fun setSnappedRoute(points: List<Point>) {
        _snappedRoute.value = points
    }
    
    fun clearRoute() {
        _routeWaypoints.value = emptyList()
        _snappedRoute.value = emptyList()
    }
    
    // Search methods
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSearching(searching: Boolean) {
        _isSearching.value = searching
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
    }
} 