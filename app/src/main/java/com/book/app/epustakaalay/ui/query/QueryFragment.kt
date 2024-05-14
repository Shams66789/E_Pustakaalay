package com.book.app.epustakaalay.ui.query

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.book.app.epustakaalay.R
import com.book.app.epustakaalay.databinding.FragmentQueryBinding
import com.book.app.epustakaalay.others.QueryAdapter

class QueryFragment : Fragment() {

private var _binding: FragmentQueryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val queryViewModel = ViewModelProvider(this).get(QueryViewModel::class.java)
        queryViewModel.fetchDataFromFirebase() // Use fetchDataFromFirebase instead of fetchRequests

        _binding = FragmentQueryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        queryViewModel.data.observe(viewLifecycleOwner, Observer { requests ->
            // Update UI or pass data to adapter
            // For example:
            val recyclerView = binding.rv
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            val adapter = QueryAdapter(requests)
            recyclerView.adapter = adapter
        })
        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}