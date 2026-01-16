package com.ss.dincharya.Login

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var viewModel : TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        aBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(aBinding.root)

        initializeUI()
        initializeViewModel()
        initTextToSpeech()
        setUpOnClick()
    }
    override fun onStart() {  // Activity becomes visible Register listeners / observers if required
        super.onStart()
        fetchTodos()
    }
    override fun onResume() { // Activity enters foreground Resume animations, sensors, media, or TTS usage
        super.onResume()
    }
    override fun onPause() {  // Pause ongoing work that should not run in background
        super.onPause()
        if (::tts.isInitialized) tts.stop()
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

    private fun initializeUI() {
        adapter = TodoAdapter(this, todoList, this)
        aBinding.rvList.layoutManager = LinearLayoutManager(this)
        aBinding.rvList.adapter = adapter
        aBinding.rvList.setHasFixedSize(true)
    }

    private fun initializeViewModel() {
        val db = DatabaseHelper().getDatabaseInstance(this)
        val repo = TodoRepo(db.todoDao())
        viewModel = ViewModelProvider(this)[TodoViewModel::class.java]
        viewModel.setRepository(repo)
    }

    private fun setUpOnClick() {
        aBinding.btnAdd.setOnClickListener { showAddTodoDialog() }

        aBinding.ivMenu.setOnClickListener {
            if (aBinding.drawerLayout.isDrawerOpen(GravityCompat.START))
                aBinding.drawerLayout.closeDrawer(GravityCompat.START)
            else aBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

        aBinding.etSearchbar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.getFilteredItem(s.toString())
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchTodos() {
        viewModel.getAllTodos { list ->
            runOnUiThread {
                todoList.clear()
                todoList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddTodoDialog() {
        val dialogBinding = CustomAddDialogLayoutBinding.inflate(layoutInflater)
        val dialog = Dialog(this).apply {
            setContentView(dialogBinding.root)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }

        UtilClass.setCustomParams(this, dialog, 30)
        dialogBinding.ivCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.tvSelectTime.setOnClickListener {
            openTimePicker(dialogBinding)
        }

        dialogBinding.btnAddItem.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val desc = dialogBinding.etDescription.text.toString().trim()
            val time = dialogBinding.tvSelectTime.text.toString()

            if (title.isEmpty()) {
                dialogBinding.etTitle.error = "Title required"
            } else {
                viewModel.todoInsert(TodoPojo(title, desc, time))
                fetchTodos()
                dialog.dismiss()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun openTimePicker(binding: CustomAddDialogLayoutBinding) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
                val amPm = if (hour >= 12) "PM" else "AM"
                val hour12 = if (hour % 12 == 0) 12 else hour % 12
                binding.tvSelectTime.text = String.format("%02d:%02d %s", hour12, minute, amPm)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false
        ).show()
    }

    private fun initTextToSpeech() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setPitch(1.1f)
                tts.setSpeechRate(0.95f)
            }
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
    }


    override fun onItemClick(position: Int) {
        val item = todoList[position]
        speak("${item.title} ${item.description} at ${item.time}")
//        speakOutAuto("You have set a task of ".plus(list[position].title).plus(" its details is : ").plus(list[position].description).plus("at ").plus(list[position].time))
//        speakOutAuto(todoList[position].title.plus(" ").plus(todoList[position].description).plus("at ").plus(todoList[position].time))
    }

    override fun onItemInsert(pojo: TodoPojo) {
        viewModel.todoInsert(pojo)
        adapter.notifyItemInserted(todoList.size)
    }

    override fun onItemEdit(pojo: TodoPojo) {

    }

    override fun onItemDelete(position: Int) {
        val item = todoList[position]
        viewModel.todoDelete(item)
        todoList.removeAt(position)
        adapter.notifyItemRemoved(position)
    }
}