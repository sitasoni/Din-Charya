package com.ss.dincharya.Todo

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.ss.dincharya.R
import com.ss.dincharya.databinding.ActivitySingleRowLayoutBinding

class TodoAdapter(private val ctx : Context, private var list : List<TodoPojo>, private val tdInterface : TodoInterface) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {
    private var filterList : List<TodoPojo> = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ActivitySingleRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
//       return list.size
       return filterList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = list[position]
        val item = filterList[position]  // ✔ Use filterList here

//        holder.slBinding.tvItemNumber.text = formatId(item.id)
        holder.slBinding.tvTitle.text = item.title
        holder.slBinding.tvTime.text = item.date.plus(" " ).plus(item.time)
        holder.slBinding.tvDescription.text = item.description

        holder.slBinding.cvItem.setOnClickListener{
            tdInterface.onItemClick(position)
        }
        holder.slBinding.ivMoreOption.setOnClickListener{
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                popUpOptions(holder.slBinding.ivMoreOption, position)
            }
        }
    }

    inner class ViewHolder(val slBinding: ActivitySingleRowLayoutBinding) : RecyclerView.ViewHolder(slBinding.root){
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getFilteredItem(str : String){
        val text = str.lowercase()
        filterList = if(text.isEmpty()){list}
        else list.filter { item ->
            item.title.lowercase().contains(text) ||
                    item.description.lowercase().contains(text)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("DefaultLocale")
    private fun formatId(id : Int) : String{
        return when{
            id < 10 -> String.format("%02d", id)
            id < 100 -> String.format("%02d", id)
            else ->  String.format("%03d", id)
        }
    }

    private fun popUpOptions(btnView : AppCompatImageView, position: Int){
        val popUpMenu = PopupMenu(ctx, btnView)
        popUpMenu.menuInflater.inflate(R.menu.item_popup_menu,popUpMenu.menu)
        popUpMenu.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.edit -> {
                    Toast.makeText(ctx, "Edit clicked",Toast.LENGTH_LONG).show()
                    true
                }
                R.id.delete ->{
                    tdInterface.onItemDelete(position)
                    true
                }
                else -> false
            }
        }
        popUpMenu.show()
    }
}


