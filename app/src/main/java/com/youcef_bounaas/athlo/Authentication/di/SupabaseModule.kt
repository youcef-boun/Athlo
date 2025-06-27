package com.youcef_bounaas.athlo.Authentication.di






import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.koin.dsl.module





val SupabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://bvyastzmsybpaxynyflg.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2eWFzdHptc3licGF4eW55ZmxnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk5NjQyNzEsImV4cCI6MjA2NTU0MDI3MX0.PFoLliGChIzVhm3LjsNVr1lyS3cfHV97N2zJqZedr1w"
        ) {
            install(Storage)
            install(Auth)
            install(Postgrest)




        }
    }
}
