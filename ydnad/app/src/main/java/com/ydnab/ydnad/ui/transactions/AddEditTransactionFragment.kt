package com.ydnab.ydnad.ui.transactions

import android.app.DatePickerDialog
import android.widget.Toast
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ydnab.ydnad.data.Transaction
import com.ydnab.ydnad.databinding.FragmentAddEditTransactionBinding
import com.ydnab.ydnad.util.Categories
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddEditTransactionFragment : Fragment() {

    private var _binding: FragmentAddEditTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private val args: AddEditTransactionFragmentArgs by navArgs()

    private var selectedDate: LocalDate = LocalDate.now()
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private var editingTransaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePicker()
        binding.btnSave.setOnClickListener { saveTransaction() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }

        viewLifecycleOwner.lifecycleScope.launch {
            Categories.load()
            setupCategorySpinner()
            if (args.transactionId != -1L) {
                loadTransaction(args.transactionId)
            } else {
                updateDateDisplay()
            }
        }
    }

    private fun setupCategorySpinner() {
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Categories.names
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerCategory.adapter = categoryAdapter
        binding.spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, position: Int, id: Long) {
                updateSubCategorySpinner(Categories.names[position])
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun updateSubCategorySpinner(category: String, selectValue: String? = null) {
        val subs = Categories.subCategories(category)
        val subAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            subs
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerSubCategory.adapter = subAdapter

        if (selectValue != null) {
            val idx = subs.indexOf(selectValue)
            if (idx >= 0) binding.spinnerSubCategory.setSelection(idx)
        }
    }

    private fun setupDatePicker() {
        binding.btnDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate = LocalDate.of(year, month + 1, day)
                    updateDateDisplay()
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            ).show()
        }
    }

    private fun updateDateDisplay() {
        binding.btnDate.text = selectedDate.format(displayFormatter)
    }

    private fun loadTransaction(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val transaction = viewModel.getById(id) ?: return@launch
            editingTransaction = transaction
            selectedDate = transaction.date
            updateDateDisplay()

            val catIdx = Categories.names.indexOf(transaction.category)
            if (catIdx >= 0) {
                binding.spinnerCategory.setSelection(catIdx)
                updateSubCategorySpinner(transaction.category, transaction.subCategory)
            }

            binding.etMemo.setText(transaction.memo)
            binding.etAmount.setText(transaction.amount.toString())
        }
    }

    private fun saveTransaction() {
        val category = binding.spinnerCategory.selectedItem?.toString() ?: ""
        val subCategory = binding.spinnerSubCategory.selectedItem?.toString() ?: ""
        val memo = binding.etMemo.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()

        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Amount is required"
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Enter a valid positive amount"
            return
        }

        val transaction = Transaction(
            id = editingTransaction?.id ?: 0,
            date = selectedDate,
            category = category,
            subCategory = subCategory,
            memo = memo,
            amount = amount,
            remoteId = editingTransaction?.remoteId ?: ""
        )

        if (editingTransaction == null) {
            binding.btnSave.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.createEntry(transaction)
                    findNavController().navigateUp()
                } catch (e: Exception) {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), e.message ?: "Failed to save", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            binding.btnSave.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.updateEntry(transaction)
                    findNavController().navigateUp()
                } catch (e: Exception) {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), e.message ?: "Failed to save", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
