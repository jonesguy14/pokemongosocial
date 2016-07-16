package io.wandr_app.pokemongosocial;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * List Array Adapter for the menu that pops up when a user clicks on the fab.
 * Created by Achi Jones on 7/15/2016.
 */
public class MenuListArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public MenuListArrayAdapter(Context context, String[] values) {
        super(context, R.layout.fab_menu_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.fab_menu_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.textViewFabMenu);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewFabMenu);
        textView.setText(values[position]);

        // Change icon based on name
        String s = values[position];

        if (s.equals(context.getString(R.string.myLocation))) {
            imageView.setImageResource(R.drawable.ic_person_pin_black_24dp);
        } else if (s.equals(context.getString(R.string.refreshMap))) {
            imageView.setImageResource(R.drawable.ic_refresh_black_24dp);
        } else if (s.equals(context.getString(R.string.newPost))) {
            imageView.setImageResource(R.drawable.ic_add_location_black_24dp);
        } else if (s.equals(context.getString(R.string.changeMapRange))) {
            imageView.setImageResource(R.drawable.ic_satellite_black_24dp);
        }
        imageView.setColorFilter(Color.GRAY);

        return rowView;
    }

}
