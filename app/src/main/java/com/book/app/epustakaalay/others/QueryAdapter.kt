package com.book.app.epustakaalay.others

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.book.app.epustakaalay.R

    class QueryAdapter(private val requests: List<Request>) : RecyclerView.Adapter<QueryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val firstNameTextView: TextView = itemView.findViewById(R.id.textView2)
            private val emailTextView: TextView = itemView.findViewById(R.id.textView3)
            private val bookDetailsTextView: TextView = itemView.findViewById(R.id.textView4)

            fun bind(request: Request) {
                // Bind data to views
                firstNameTextView.text = "${request.firstName} ${request.lastName}"
                emailTextView.text = "Email: ${request.email}"
                bookDetailsTextView.text = request.bookDetails
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.query_template, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(requests[position])
        }

        override fun getItemCount(): Int {
            return requests.size
        }
    }
