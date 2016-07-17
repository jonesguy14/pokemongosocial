package io.wandr_app.pokemongosocial.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import io.wandr_app.pokemongosocial.R;
import io.wandr_app.pokemongosocial.model.Team;

public final class CommonUtils {

    public static final String IMAGE_URL_BASE = "http://wandr-app.io/pokemon/images/";

    private CommonUtils() {
    }

    public static void setTeamTextViewColor(TextView teamTextView, Context context, Team team) {
        switch (team) {
            case INSTINCT:
                teamTextView.setTextColor(ContextCompat.getColor(context, R
                        .color.Instinct));
                break;
            case MYSTIC:
                teamTextView.setTextColor(ContextCompat.getColor(context, R.color.Mystic));
                break;
            case VALOR:
                teamTextView.setTextColor(ContextCompat.getColor(context, R.color.Valor));
                break;
        }
    }

    /**
     * Used to make a list view dialog fully scrollable.
     * Needed so that the listview of comments expands as needed.
     *
     * @param listView listview affected
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount())) + 100;
        listView.setLayoutParams(params);
    }

    public static String getTimeDisplayString(int minutesAgo) {
        if (minutesAgo > 2880) {
            // Use days instead
            return minutesAgo/1440 + " days ago";
        } else if (minutesAgo > 120) {
            // Use hours instead
            return minutesAgo/60 + " hours ago";
        } else {
            return minutesAgo > 1 ? minutesAgo + " min ago" : "Just now";
        }
    }

    public static String getNumLikesString(int numLikes) {
        return numLikes >= 0 ? "+" + numLikes : String.valueOf(numLikes);
    }
}
