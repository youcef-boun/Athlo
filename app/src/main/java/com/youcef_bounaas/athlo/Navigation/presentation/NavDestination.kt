package com.youcef_bounaas.athlo.Navigation.presentation

sealed class NavDestination(val route: String) {
    data object Auth : NavDestination("auth")
    data object SignUp : NavDestination("signup")
    data object Login : NavDestination("login")
    data object UserInfo : NavDestination("userinfo")
    data object ConfirmEmail : NavDestination("confirmemail")




    data object Home : NavDestination("home")
    data object Maps : NavDestination("maps")
    data object Record : NavDestination("record")
   data object Stats : NavDestination("stats")



}