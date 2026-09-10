package com.nanz.musify

import android.app.Application
import com.nanz.musify.data.db.AppDatabase
import com.nanz.musify.data.innertube.InnerTubeClient
import com.nanz.musify.data.repository.MusicRepository
import com.nanz.musify.player.PlaybackManager

class NanzMusifyApp : Application() {

    lateinit var innerTubeClient: InnerTubeClient
        private set

    lateinit var database: AppDatabase
        private set

    lateinit var repository: MusicRepository
        private set

    lateinit var playbackManager: PlaybackManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        innerTubeClient = InnerTubeClient()
        database = AppDatabase.getDatabase(this)
        repository = MusicRepository(innerTubeClient, database.songDao())
        playbackManager = PlaybackManager(this, innerTubeClient)
    }

    companion object {
        lateinit var instance: NanzMusifyApp
            private set
    }
}
