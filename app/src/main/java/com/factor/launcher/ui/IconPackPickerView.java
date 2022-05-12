package com.factor.launcher.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.bouncy.BouncyRecyclerView;
import com.factor.launcher.R;
import com.factor.launcher.models.IconPackProvider;
import com.factor.launcher.util.IconPackManager;

import java.util.ArrayList;


public class IconPackPickerView extends CardView
{
    private IconPackProvider selectedIconPack;

    private final ArrayList<IconPackProvider> iconPacks = new ArrayList<>();

    private final IconPackPickerAdapter adapter = new IconPackPickerAdapter();

    private OnIconPackClickedListener listener;

    public IconPackPickerView(Context context)
    {
        super(context);
        init(context);
    }

    public IconPackPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconPackPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setOnIconPackClickedListener(OnIconPackClickedListener listener)
    {
        this.listener = listener;
    }


    public void setCurrentIconPack(String packageName)
    {
        Log.d("icon_pack", "current selected: " + packageName);
        if (packageName.isEmpty())
            return;
        selectedIconPack = new IconPackProvider();
        selectedIconPack.setPackageName(packageName);
        selectedIconPack.setCurrentIconPack(true);

        for (IconPackProvider provider : iconPacks)
        {
            if (provider.getPackageName().equals(packageName))
            {
                provider.setCurrentIconPack(true);
                adapter.notifyItemChanged(iconPacks.indexOf(provider));
            }

        }
    }
    public IconPackProvider getCurrentIconPack()
    {
        PackageManager pm = getContext().getPackageManager();
        IconPackManager ip = new IconPackManager();
        ip.setContext(getContext());

        ArrayList<IconPackManager.IconPack> iconPackArrayList = ip.getAvailableIconPacks(true);

        for (IconPackManager.IconPack iconPack : iconPackArrayList)
        {
            try
            {
                if (iconPack.packageName.equals(selectedIconPack.getPackageName()))
                {
                    selectedIconPack.setIcon(pm.getApplicationIcon(selectedIconPack.getPackageName()));
                    selectedIconPack.setLabelNew(iconPack.name);
                }
            }
            catch (PackageManager.NameNotFoundException ignored){}

        }
        return selectedIconPack;
    }


    public String getCurrentIconPackPackageName()
    {
        for (IconPackProvider provider : iconPacks)
        {
            if (provider.isCurrentIconPack())
                return provider.getPackageName();
        }
        return "";
    }

    private void init(Context context)
    {
        View.inflate(getContext(), R.layout.icon_pack_picker_view, this);
        BouncyRecyclerView rc = findViewById(R.id.icon_pack_recyclerview);
        rc.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rc.setAdapter(adapter);

        PackageManager pm = context.getPackageManager();
        IconPackManager iconPackManager = new IconPackManager();
        iconPackManager.setContext(context);

        ArrayList<IconPackManager.IconPack> iconPackArrayList = iconPackManager.getAvailableIconPacks(true);
        Log.d("IconPack", "icon packs: " + iconPackArrayList.size());

        if (iconPackArrayList.size() < 1)
        {
            this.setVisibility(GONE);
            return;
        }
        else
            this.setVisibility(VISIBLE);
        for (IconPackManager.IconPack iconPack : iconPackArrayList)
        {
            try
            {
                IconPackProvider iconPackProvider = new IconPackProvider();
                iconPackProvider.setPackageName(iconPack.packageName);
                iconPackProvider.setLabelNew(iconPack.name);
                iconPackProvider.setIcon(pm.getApplicationIcon(iconPackProvider.getPackageName()));
                if (!iconPacks.contains(iconPackProvider))
                {
                    iconPacks.add(iconPackProvider);
                    adapter.notifyItemInserted(iconPacks.indexOf(iconPackProvider));
                }
            }
            catch (PackageManager.NameNotFoundException ignored){}

        }
    }


    protected class IconPackPickerAdapter extends RecyclerView.Adapter<IconPackPickerAdapter.IconPackViewHolder>
    {

        @NonNull
        @Override
        public IconPackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_pack_picker, parent, false);
            return new IconPackViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IconPackViewHolder holder, int position)
        {
            holder.bind(iconPacks.get(position));
        }

        @Override
        public int getItemCount()
        {
            return iconPacks.size();
        }

        private class IconPackViewHolder extends RecyclerView.ViewHolder
        {

            private final AppCompatTextView label;

            private final AppCompatImageView icon;

            private final CardView base;

            public IconPackViewHolder(@NonNull View itemView)
            {
                super(itemView);
                label = itemView.findViewById(R.id.icon_pack_label);
                icon = itemView.findViewById(R.id.icon_pack_icon);
                base = itemView.findViewById(R.id.icon_pack_base);
            }

            public void bind(IconPackProvider provider)
            {
                try
                {
                    label.setText(provider.getLabelNew());
                    icon.setImageDrawable(provider.getIcon());
                    if (provider.isCurrentIconPack())
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {
                            base.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorHalo, null));
                        }
                        else base.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorHalo));
                    }
                    else
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {
                            base.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorDarker, null));
                        }
                        else base.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorDarker));
                    }

                    base.setOnClickListener(view ->
                    {
                        if (!provider.isCurrentIconPack()) //select as current icon pack
                        {
                            provider.setCurrentIconPack(true);
                            selectedIconPack = provider;
                            notifyItemChanged(iconPacks.indexOf(selectedIconPack));
                            for (IconPackProvider iconPack : iconPacks)
                            {
                                if (!iconPack.getPackageName().equals(selectedIconPack.getPackageName()) && iconPack.isCurrentIconPack())
                                {
                                    iconPack.setCurrentIconPack(false);
                                    notifyItemChanged(iconPacks.indexOf(iconPack));
                                }
                            }
                        }
                        else // clear icon pack
                        {
                            provider.setCurrentIconPack(false);
                            selectedIconPack = provider;
                            if (iconPacks.contains(selectedIconPack))
                            {
                                notifyItemChanged(iconPacks.indexOf(selectedIconPack));
                            }
                        }

                        if (listener != null)
                            listener.onIconPackClicked(selectedIconPack);
                    });
                }
                catch (NullPointerException | ActivityNotFoundException ignored){}
            }
        }
    }

    public abstract static class OnIconPackClickedListener
    {
        public void onIconPackClicked(IconPackProvider clickedIconPack)
        {

        }
    }

}