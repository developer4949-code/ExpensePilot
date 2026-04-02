package com.debiprasaddas.expensepilot.ui.transactions;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.databinding.FragmentTransactionsBinding;
import com.debiprasaddas.expensepilot.ui.adapter.TransactionAdapter;
import com.debiprasaddas.expensepilot.ui.common.FinanceViewModel;

public class TransactionsFragment extends Fragment implements TransactionAdapter.Listener {

    private FragmentTransactionsBinding binding;
    private FinanceViewModel viewModel;
    private TransactionAdapter adapter;
    private String selectedType = "ALL";

    public TransactionsFragment() {
        super(R.layout.fragment_transactions);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);
        adapter = new TransactionAdapter(this);
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTransactions.setAdapter(adapter);

        binding.toggleFilters.check(R.id.button_all);
        binding.toggleFilters.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.button_income) {
                selectedType = "INCOME";
            } else if (checkedId == R.id.button_expense) {
                selectedType = "EXPENSE";
            } else {
                selectedType = "ALL";
            }
            pushFilters();
        });

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pushFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        viewModel.getFilteredTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            boolean isEmpty = transactions == null || transactions.isEmpty();
            binding.textEmptyTransactions.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
        pushFilters();
    }

    private void pushFilters() {
        String query = binding == null || binding.editSearch.getText() == null ? "" : binding.editSearch.getText().toString();
        viewModel.setFilters(query, selectedType);
    }

    @Override
    public void onTransactionClick(TransactionEntity transactionEntity) {
        Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
        intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, transactionEntity.getId());
        startActivity(intent);
    }

    @Override
    public void onTransactionLongClick(View anchor, TransactionEntity transactionEntity) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenu().add(0, 1, 0, "Edit");
        popupMenu.getMenu().add(0, 2, 1, "Delete");
        popupMenu.setOnMenuItemClickListener(item -> handleMenuClick(item, transactionEntity));
        popupMenu.show();
    }

    private boolean handleMenuClick(MenuItem item, TransactionEntity transactionEntity) {
        if (item.getItemId() == 1) {
            onTransactionClick(transactionEntity);
            return true;
        }
        if (item.getItemId() == 2) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete transaction?")
                    .setMessage("This entry will be removed from your local finance history.")
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteTransaction(transactionEntity))
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
