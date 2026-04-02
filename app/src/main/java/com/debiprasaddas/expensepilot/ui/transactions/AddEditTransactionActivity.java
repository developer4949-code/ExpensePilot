package com.debiprasaddas.expensepilot.ui.transactions;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.databinding.ActivityAddEditTransactionBinding;
import com.debiprasaddas.expensepilot.ui.common.SecureActivity;
import com.debiprasaddas.expensepilot.ui.common.FinanceViewModel;
import com.debiprasaddas.expensepilot.util.FormatterUtils;
import com.debiprasaddas.expensepilot.util.TransactionCategories;

import java.util.Calendar;

public class AddEditTransactionActivity extends SecureActivity {

    public static final String EXTRA_TRANSACTION_ID = "extra_transaction_id";

    private ActivityAddEditTransactionBinding binding;
    private FinanceViewModel viewModel;
    private long selectedDate = System.currentTimeMillis();
    private TransactionEntity existingTransaction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        binding.toolbar.setLogo(R.drawable.app_logo_toolbar);
        binding.toolbar.setLogoDescription(getString(R.string.app_name));
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.editDate.setText(FormatterUtils.fullDate(selectedDate));
        binding.editDate.setOnClickListener(v -> showDatePicker());
        binding.toggleType.check(R.id.button_type_expense);
        binding.buttonSave.setOnClickListener(v -> saveTransaction());
        binding.buttonDelete.setOnClickListener(v -> confirmDelete());
        binding.editCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TransactionCategories.ALL));

        long transactionId = getIntent().getLongExtra(EXTRA_TRANSACTION_ID, -1L);
        if (transactionId != -1L) {
            binding.toolbar.setTitle(R.string.edit_transaction);
            binding.buttonDelete.setVisibility(android.view.View.VISIBLE);
            viewModel.loadTransaction(transactionId, transactionEntity -> runOnUiThread(() -> bindTransaction(transactionEntity)));
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_slide_out_right);
    }

    private void bindTransaction(TransactionEntity transactionEntity) {
        if (transactionEntity == null) {
            return;
        }
        existingTransaction = transactionEntity;
        binding.editAmount.setText(String.valueOf(transactionEntity.getAmount()));
        binding.editCategory.setText(transactionEntity.getCategory(), false);
        binding.editNote.setText(transactionEntity.getNote());
        selectedDate = transactionEntity.getDate();
        binding.editDate.setText(FormatterUtils.fullDate(selectedDate));
        binding.toggleType.check("INCOME".equalsIgnoreCase(transactionEntity.getType()) ? R.id.button_type_income : R.id.button_type_expense);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(year, month, dayOfMonth, 12, 0, 0);
            chosenDate.set(Calendar.MILLISECOND, 0);
            selectedDate = chosenDate.getTimeInMillis();
            binding.editDate.setText(FormatterUtils.fullDate(selectedDate));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaction() {
        String amountText = binding.editAmount.getText() == null ? "" : binding.editAmount.getText().toString().trim();
        String category = binding.editCategory.getText() == null ? "" : binding.editCategory.getText().toString().trim();
        String note = binding.editNote.getText() == null ? "" : binding.editNote.getText().toString().trim();

        if (amountText.isEmpty()) {
            binding.editAmount.setError("Enter amount");
            return;
        }
        if (category.isEmpty()) {
            binding.editCategory.setError("Enter category");
            return;
        }

        double amount = Double.parseDouble(amountText);
        String type = binding.toggleType.getCheckedButtonId() == R.id.button_type_income ? "INCOME" : "EXPENSE";
        if (existingTransaction == null) {
            viewModel.insertTransaction(new TransactionEntity(amount, type, category, selectedDate, note));
            Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show();
        } else {
            existingTransaction.setAmount(amount);
            existingTransaction.setType(type);
            existingTransaction.setCategory(category);
            existingTransaction.setDate(selectedDate);
            existingTransaction.setNote(note);
            viewModel.updateTransaction(existingTransaction);
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        if (existingTransaction == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete transaction?")
                .setMessage("This action removes the entry from the local database.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTransaction(existingTransaction);
                    finish();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
