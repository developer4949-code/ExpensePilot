package com.debiprasaddas.expensepilot.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.util.FormatterUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<TransactionEntity> items = new ArrayList<>();
    private final Listener listener;

    public TransactionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<TransactionEntity> transactions) {
        items.clear();
        if (transactions != null) {
            items.addAll(transactions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView category;
        private final TextView note;
        private final TextView date;
        private final TextView amount;
        private final TextView type;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.text_category);
            note = itemView.findViewById(R.id.text_note);
            date = itemView.findViewById(R.id.text_date);
            amount = itemView.findViewById(R.id.text_amount);
            type = itemView.findViewById(R.id.text_type);
        }

        void bind(TransactionEntity transaction) {
            category.setText(transaction.getCategory());
            note.setText(transaction.getNote() == null || transaction.getNote().trim().isEmpty() ? "No notes" : transaction.getNote());
            date.setText(FormatterUtils.fullDate(transaction.getDate()));
            boolean incomeTransaction = "INCOME".equalsIgnoreCase(transaction.getType());
            amount.setText((incomeTransaction ? "+ " : "- ") + FormatterUtils.currency(transaction.getAmount()));
            amount.setTextColor(itemView.getContext().getColor(incomeTransaction ? R.color.positive : R.color.negative));
            type.setText(incomeTransaction ? "Income" : "Expense");
            type.setBackgroundResource(incomeTransaction ? R.drawable.bg_tag_income : R.drawable.bg_tag_expense);
            itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));
            itemView.setOnLongClickListener(v -> {
                listener.onTransactionLongClick(v, transaction);
                return true;
            });
        }
    }

    public interface Listener {
        void onTransactionClick(TransactionEntity transactionEntity);

        void onTransactionLongClick(View anchor, TransactionEntity transactionEntity);
    }
}
