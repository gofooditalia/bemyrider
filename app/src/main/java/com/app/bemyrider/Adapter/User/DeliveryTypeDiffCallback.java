package com.app.bemyrider.Adapter.User;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.app.bemyrider.model.user.ProviderItem;

public class DeliveryTypeDiffCallback extends DiffUtil.ItemCallback<ProviderItem> {

    @Override
    public boolean areItemsTheSame(@NonNull ProviderItem oldItem, @NonNull ProviderItem newItem) {
        return oldItem.getProviderId().equals(newItem.getProviderId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull ProviderItem oldItem, @NonNull ProviderItem newItem) {
        return oldItem.equals(newItem);
    }
}
