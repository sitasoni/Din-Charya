package com.ss.dincharya.Login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ss.dincharya.DbHandler.DatabaseHelper
import com.ss.dincharya.Todo.TodoAdapter
import com.ss.dincharya.Todo.TodoInterface
import com.ss.dincharya.Todo.TodoPojo
import com.ss.dincharya.Todo.TodoRepo
import com.ss.dincharya.DbHandler.TodoViewModel
import com.ss.dincharya.R
import com.ss.dincharya.Utils.UtilClass
import com.ss.dincharya.databinding.ActivityHomeBinding
import com.ss.dincharya.databinding.CustomAddDialogLayoutBinding
import java.util.Calendar
import java.util.Locale

class HomePage : AppCompatActivity(), TodoInterface {
    private lateinit var aBinding : ActivityHomeBinding
    private val todoList = ArrayList<TodoPojo>()
    private lateinit var adapter : TodoAdapter
    private lateinit var tts: TextToSpeech
    private lateinit var clBinding : CustomAddDialogLayoutBinding
    private lateinit var viewModel : TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bindView()
        onClick()
    }
    override fun onStart() {  // Activity becomes visible Register listeners / observers if required
        super.onStart()
    }
    override fun onResume() { // Activity enters foreground Resume animations, sensors, media, or TTS usage
        super.onResume()
    }
    override fun onPause() {  // Pause ongoing work that should not run in background
        super.onPause()
    }
    override fun onStop() {  // Release heavy resources or unregister receivers
        super.onStop()
    }
    override fun onRestart() { // Activity restarting after stop Optional: refresh UI or data
        super.onRestart()
    }
    override fun onDestroy() {   // Final cleanup (critical)
        if(::tts.isInitialized){
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun bindView(){
        aBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(aBinding.root)
    }

    private fun onClick(){
        textToVoiceInit()
        setRvTodo()
        getAllTodosFromDb()

        aBinding.btnAdd.setOnClickListener {
            addItemDialog()
        }
        aBinding.etSearchbar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(str: Editable?) {
            }
            override fun beforeTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.getFilteredItem(str.toString())
            }
        })
        aBinding.ivMenu.setOnClickListener{
            if(aBinding.drawerLayout.isDrawerOpen(GravityCompat.START))
                aBinding.drawerLayout.closeDrawer(GravityCompat.START)
            else aBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setRvTodo(){
        aBinding.rvList.layoutManager = LinearLayoutManager(this)
        adapter = TodoAdapter(this,todoList, this)
        aBinding.rvList.adapter = adapter

        val dbInstance = DatabaseHelper().getDatabaseInstance(this)
        val todoDao = dbInstance.todoDao()
        val todoRepo = TodoRepo(todoDao)
        viewModel = ViewModelProvider(this)[TodoViewModel::class.java]
        viewModel.setRepository(todoRepo)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllTodosFromDb(){
        viewModel.getAllTodos { list ->
            runOnUiThread {
                todoList.clear()
                todoList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("DefaultLocale", "ResourceAsColor")
    private fun addItemDialog(){
        clBinding = CustomAddDialogLayoutBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.setContentView(clBinding.root)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        UtilClass.setCustomParams(this, dialog, 30)

        clBinding.ivMic.setOnClickListener(){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            }
            else {
                startSpeechToText2()
            }
        }
        clBinding.tvSelectTime.setOnClickListener(){
            val calender = Calendar.getInstance()
            val hour = calender.get(Calendar.HOUR_OF_DAY)
            val minute = calender.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(this,{_, selectedHour, selectedMinute ->
            val amPm = if(selectedHour >= 12) "PM" else "AM"
            val hour12 = when {
                selectedHour == 0 -> 12         // Midnight
                selectedHour > 12 -> selectedHour - 12
                else -> selectedHour
            }
            val time = String.format("%02d:%02d %s", hour12, selectedMinute, amPm)
                clBinding.tvSelectTime.text = time
            },hour,minute,false)  // true = 24-hour, false = 12-hour format
            timePicker.setOnShowListener{
                timePicker.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(
                    R.color.red
                ))
                timePicker.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(
                    R.color.black
                ))
            }
            timePicker.show()
        }

        clBinding.btnAddItem.setOnClickListener {
            val title = clBinding.etTitle.text.toString().trim()
            val description = clBinding.etDescription.text.toString().trim()
            val time = clBinding.tvSelectTime.text.toString().trim()

            if(title.isEmpty()) clBinding.etTitle.error = "Title is required!"
            else if(time.isEmpty()) clBinding.tvSelectTime.error = "Time is required!"
            else {
                viewModel.todoInsert(TodoPojo(title, description,time))
                getAllTodosFromDb()
                dialog.dismiss()
            }
        }
        clBinding.ivCancel.setOnClickListener{ dialog.dismiss() }
    }

    private fun textToVoiceInit() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Operational risk: language not supported
                }
            }
        }
    }

    private fun speakOutAuto(text: String) {
        detectLanguage(text) { langCode ->
            val locale = mapLanguageCodeToLocale(langCode ?: "")
            tts.language = locale
            tts.setPitch(1.1f) // speech
            tts.setSpeechRate(0.95f)  // speed
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID")
        }
    }

    private fun detectLanguage(text: String, callback: (String?) -> Unit) {
        val languageIdentifier = com.google.mlkit.nl.languageid.LanguageIdentification.getClient()

        languageIdentifier.identifyLanguage(text).addOnSuccessListener { langCode ->
            if (langCode == "und") {
                callback(null)     // undefined or unsupported
            } else {
                callback(langCode) // e.g., "hi", "en", "ta", "bn"
            }
        }
        .addOnFailureListener {
            callback(null)
        }
    }

    private fun mapLanguageCodeToLocale(code: String): Locale {
        return when (code) {
            "hi" -> Locale("hi", "IN")   // Hindi
            "en" -> Locale("en", "US")   // English (US)
            "bn" -> Locale("bn", "IN")   // Bengali
            "gu" -> Locale("gu", "IN")   // Gujarati
            "ta" -> Locale("ta", "IN")   // Tamil
            "te" -> Locale("te", "IN")   // Telugu
            "mr" -> Locale("mr", "IN")   // Marathi
            else -> Locale.getDefault()  // Default fallback
        }
    }

    private val speechToTextLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val list = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = list?.get(0) ?: "Not spoken"
            clBinding.etDescription.setText(spokenText)
        }
    }

    private fun startSpeechToText2() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now…")
        speechToTextLauncher.launch(intent)
    }

    override fun onItemClick(position: Int) {
//        speakOut("कृपया थोड़ी देर इंतज़ार कीजिए।")
//        speakOutAuto("You have set a task of ".plus(list[position].title).plus(" its details is : ").plus(list[position].description).plus("at ").plus(list[position].time))
        speakOutAuto(todoList[position].title.plus(" ").plus(todoList[position].description).plus("at ").plus(todoList[position].time))
        Toast.makeText(this,"Clicked",Toast.LENGTH_LONG).show()
    }

    override fun onItemInsert(pojo: TodoPojo) {
        viewModel.todoInsert(TodoPojo(pojo.title,pojo.description,pojo.time))
    }

    override fun onItemEdit(pojo: TodoPojo) {

    }

    override fun onItemDelete(position: Int) {
        if (position >= todoList.size) return // safety

        val item = todoList[position]
        viewModel.todoDelete(item)

        todoList.removeAt(position)
        adapter.notifyItemRemoved(position)
    }
}