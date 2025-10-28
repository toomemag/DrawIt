package com.example.drawit.ui.apiexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.drawit.databinding.FragmentApiexampleBinding

class ApiExampleFragment : Fragment() {
    private var _binding: FragmentApiexampleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        // get this view's viewmodel
        val vm = ViewModelProvider(this)[ApiExampleViewModel::class.java]

        // set this fragment as active fragment in parent view
        _binding = FragmentApiexampleBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val spinner = binding.mySpinner
        val textView: TextView = binding.outputText



        // state listeners
        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                textView.text = "Loading..."
                textView.setTextColor(0xFF999999.toInt())
            }
        }

        vm.response.observe(viewLifecycleOwner) { response ->
            response?.let {
                textView.text = it.value
                textView.setTextColor(0xFFADADAD.toInt())
            }
        }

        vm.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                textView.text = it
                textView.setTextColor(0xFFE16666.toInt())
            }
        }


        // Spinner
        vm.categories.observe(viewLifecycleOwner) { categories ->
            if (!categories.isNullOrEmpty()) {
                // "No Category" option
                val categoriesWithNone = mutableListOf("No Category")
                categoriesWithNone.addAll(categories)

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categoriesWithNone
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter


            }
        }
        vm.fetchCategories()
        vm.fetchJoke("")

        binding.buttonFetchData.setOnClickListener {
            val selectedCategory = binding.mySpinner.selectedItem as? String ?: ""
            vm.fetchJoke(selectedCategory)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}