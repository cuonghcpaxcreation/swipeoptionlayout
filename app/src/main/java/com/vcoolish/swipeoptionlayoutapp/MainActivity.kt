package com.vcoolish.swipeoptionlayoutapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vcoolish.swipeoptionlayout.SwipeOptionLayout
import com.vcoolish.swipeoptionlayoutapp.databinding.ActivityMainBinding
import com.vcoolish.swipeoptionlayoutapp.databinding.ItemListBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val leftSlideListener = object: SwipeOptionLayout.SwipeCallback {
        override fun onSlide(view: View) {
            val position = binding.list.getChildAdapterPosition(view)
            Toast.makeText(this@MainActivity, "$position left selected", Toast.LENGTH_SHORT).show()
        }
    }
    private val rightSlideListener = object: SwipeOptionLayout.SwipeCallback {
        override fun onSlide(view: View) {
            val position = binding.list.getChildAdapterPosition(view)
            Toast.makeText(this@MainActivity, "$position right selected", Toast.LENGTH_SHORT).show()
        }
    }
    private val adapter: MainAdapter by lazy { MainAdapter(leftSlideListener, rightSlideListener) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.list.adapter = adapter
        val list = mutableListOf<String>()
        (0..15).forEach { list.add(it.toString()) }
        adapter.submitList(list)
    }
}

class MainAdapter(
        private val onLeftSelected: SwipeOptionLayout.SwipeCallback,
        private val onRightSelected: SwipeOptionLayout.SwipeCallback
) : ListAdapter<String, MainAdapter.ViewHolder>(diff) {

    private lateinit var binding: ItemListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val view: ItemListBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(item: String) = with(view) {
            contentTv.text = "Item #$item"
            swipeLayout.setLeftSwipeCallback(onLeftSelected)
            swipeLayout.setRightSwipeCallback(onRightSelected)
            content.setOnClickListener { Toast.makeText(swipeLayout.context, "$item content selected", Toast.LENGTH_SHORT).show() }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    }
}