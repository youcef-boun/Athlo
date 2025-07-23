package com.youcef_bounaas.athlo.Authentication.di






import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.koin.dsl.module





val SupabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "Add your own url here !!",
            supabaseKey = "Add your own key here !!"
        ) {
            install(Storage)
            install(Auth)
            install(Postgrest)




        }
    }
}
