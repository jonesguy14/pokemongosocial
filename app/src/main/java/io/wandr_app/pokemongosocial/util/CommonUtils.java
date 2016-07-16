package io.wandr_app.pokemongosocial.util;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import io.wandr_app.pokemongosocial.R;
import io.wandr_app.pokemongosocial.model.Team;

public final class CommonUtils {
    private CommonUtils() {
    }

    public static String getNumLikesString(int numLikes) {
        return numLikes > 0 ? "+" + numLikes : String.valueOf(numLikes);
    }

    public static void setTeamTextViewColor(TextView teamTextView, Resources resources, Team team) {
        switch (team) {
            case INSTINCT:
                teamTextView.setTextColor(resources.getColor(R
                        .color.Instinct));
                break;
            case MYSTIC:
                teamTextView.setTextColor(resources.getColor(R.color.Mystic));
                break;
            case VALOR:
                teamTextView.setTextColor(resources.getColor(R.color.Valor));
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
        return minutesAgo > 1 ? minutesAgo + "min ago" : "Just now";
    }
}
