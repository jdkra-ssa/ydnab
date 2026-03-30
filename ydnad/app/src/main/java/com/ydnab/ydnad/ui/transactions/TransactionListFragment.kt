package com.ydnab.ydnad.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ydnab.ydnad.databinding.FragmentTransactionListBinding
import kotlinx.coroutines.launch

class TransactionListFragment : Fragment() {

    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var adapter: TransactionListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TransactionListAdapter { transaction ->
            val action = TransactionListFragmentDirections
                .actionTransactionListToAddEdit(transaction.id)
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Swipe to delete
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val transaction = adapter.currentList[viewHolder.adapterPosition]
                viewModel.delete(transaction)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerView)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactions.collect { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSyncing.collect { syncing ->
                binding.progressBar.visibility = if (syncing) View.VISIBLE else View.GONE
                binding.tvEmpty.visibility = if (syncing) View.GONE else binding.tvEmpty.visibility
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.syncError.collect { error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Sync error: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.syncFromApi()

        binding.fabAdd.setOnClickListener {
            val action = TransactionListFragmentDirections
                .actionTransactionListToAddEdit(-1L)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
