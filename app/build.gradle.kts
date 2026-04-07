plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.prediction_score_gp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.prediction_score_gp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Émulateur Android Studio → 10.0.2.2 est le localhost de la machine hôte.
            // Sur un vrai téléphone, modifier depuis Profile → API URL dans l'app.
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
        }
        release {
            isMinifyEnabled = false
            // ⚠️ Remplacer par l'URL du serveur de production avant le déploiement.
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // Accélération matérielle activée globalement
    buildFeatures {
        viewBinding = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Retrofit + OkHttp + Gson
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Lifecycle / ViewModel / LiveData
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    // Glide (chargement d'images pilotes)
    implementation(libs.glide)

    // CardView / RecyclerView
    implementation(libs.cardview)
    implementation(libs.recyclerview)

    // SwipeRefreshLayout (pull-to-refresh)
    implementation(libs.swiperefreshlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)



}
