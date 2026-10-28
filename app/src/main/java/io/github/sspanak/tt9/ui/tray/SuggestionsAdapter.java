package io.github.sspanak.tt9.ui.tray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
	private final Consumer<Integer> onItemClick;
	private final Consumer<Integer> onItemLongClick;
	private final int layout;
	private final int textViewResourceId;
	private final LayoutInflater mInflater;
	private final List<String> mSuggestions;
	private float textSize;

	private int colorDefault;
	private int colorHighlight;
	private int backgroundHighlight;
	private int selectedIndex = 0;
	private int gridRowHeight;
	private int expandedPadding;
	private float expandedBaseTextSize;
	private int expandedSeparatorWidth;
	private PackedCandidateNavigation expandedNavigation;
	private CandidateNumbering numbering;


	public SuggestionsAdapter(Context context, @NonNull Consumer<Integer> onItemClick, @NonNull Consumer<Integer> onItemLongClick, int layout, int textViewResourceId, List<String> suggestions) {
		this.onItemClick = onItemClick;
		this.onItemLongClick = onItemLongClick;
		this.layout = layout;
		this.textViewResourceId = textViewResourceId;
		this.mInflater = LayoutInflater.from(context);
		this.mSuggestions = suggestions;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(mInflater.inflate(layout, parent, false));
	}


	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (gridRowHeight > 0) {
			// Keep GridLayoutManager's span metadata when a visible holder is rebound.
			ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
			params.width = Math.max(1, expandedNavigation.getItemWidth(position) - expandedSeparatorWidth);
			params.height = gridRowHeight;
			holder.itemView.setLayoutParams(params);
			holder.suggestionItem.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
			holder.suggestionItem.setMinimumWidth(0);
			holder.suggestionItem.setPadding(expandedPadding, 0, expandedPadding, 0);
			holder.suggestionItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, expandedBaseTextSize);
			holder.suggestionItem.setSingleLine(true);
			holder.suggestionItem.setEllipsize(TextUtils.TruncateAt.END);
		}
		int number = gridRowHeight > 0 && numbering != null ? numbering.getNumber(position) : 0;
		String prefix = number > 0 ? number + " " : "";
		SpannableString scaledText = new SpannableString(prefix + mSuggestions.get(position));
		scaledText.setSpan(new RelativeSizeSpan(textSize), 0, scaledText.length(), 0);
		if (number > 0) {
			scaledText.setSpan(new RelativeSizeSpan(0.65f), 0, prefix.length(), 0);
		}
		holder.suggestionItem.setText(scaledText);

		holder.suggestionItem.setTag(position);
		holder.suggestionItem.setTextColor(selectedIndex == position ? colorHighlight : colorDefault);
		holder.suggestionItem.setBackgroundColor(selectedIndex == position ? backgroundHighlight : Color.TRANSPARENT);
		holder.suggestionItem.setOnClickListener(v -> onItemClick.accept((int) v.getTag()));
		holder.suggestionItem.setOnLongClickListener(v -> {
			onItemLongClick.accept((int) v.getTag());
			return true;
		});
	}


	@Override
	public int getItemCount() {
		return mSuggestions.size();
	}

	@Override
	public int getItemViewType(int position) {
		return gridRowHeight > 0 ? 1 : 0;
	}

	public void setGridLayout(PackedCandidateNavigation navigation, int height, int padding, float baseTextSize, int separatorWidth, boolean numbered) {
		expandedNavigation = navigation;
		gridRowHeight = height;
		expandedPadding = padding;
		expandedBaseTextSize = baseTextSize;
		expandedSeparatorWidth = separatorWidth;
		numbering = numbered && navigation != null ? new CandidateNumbering(navigation) : null;
	}

	public int getNumberedIndex(int number) {
		return numbering != null ? numbering.getIndex(number, selectedIndex) : -1;
	}


	public void setSelection(int newIndex) {
		notifyItemChanged(selectedIndex);
		notifyItemChanged(selectedIndex = newIndex);
	}


	@SuppressLint("NotifyDataSetChanged")
	public void resetItems(int newIndex) {
		selectedIndex = newIndex;
		notifyDataSetChanged();
	}


	public void setTextSize(float size) {
		textSize = size;
	}


	public void setColorDefault(int colorDefault) {
		this.colorDefault = colorDefault;
	}


	public void setColorHighlight(int colorHighlight) {
		this.colorHighlight = colorHighlight;
	}


	public void setBackgroundHighlight(int backgroundHighlight) {
		this.backgroundHighlight = backgroundHighlight;
	}


	public class ViewHolder extends RecyclerView.ViewHolder {
		final TextView suggestionItem;

		ViewHolder(View itemView) {
			super(itemView);
			suggestionItem = itemView.findViewById(textViewResourceId);
		}
	}
}
