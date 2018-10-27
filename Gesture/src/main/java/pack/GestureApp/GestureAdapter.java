package pack.GestureApp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class GestureAdapter extends ArrayAdapter<GestureHolder> {

    private static List<GestureHolder> mGestureList;
    private Context mContext;

    GestureAdapter(ArrayList<GestureHolder> gestureList, Context context) {
        super(context, R.layout.gestures_list, gestureList);
        mGestureList = gestureList;
        this.mContext = context;

    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        GestureViewHolder holder = new GestureViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.gesture_list_item, null);

            TextView idView = v.findViewById(R.id.gesture_id);
            TextView nameView = v.findViewById(R.id.gesture_name);
            ImageView gestureImageView = v.findViewById(R.id.gesture_image);
            TextView nameViewRef = v.findViewById(R.id.gesture_name_ref);

            holder.gestureId = idView;
            holder.gestureName = nameView;
            holder.gestureImage = gestureImageView;
            holder.gestureNameRef = nameViewRef;

            final ImageView mMenuItemButton =  v.findViewById(R.id.menu_item_options);
            mMenuItemButton.setClickable(true);

            v.setTag(holder);
        }
        else
            holder = (GestureViewHolder) v.getTag();

        GestureHolder gestureHolder = mGestureList.get(position);
        holder.gestureId.setText(String.valueOf(gestureHolder.getGesture().getID()));
        holder.gestureName.setText(gestureHolder.getNaam());
        holder.gestureNameRef.setText(gestureHolder.getNaam());

        try {
            holder.gestureImage.setImageBitmap(gestureHolder.getGesture().toBitmap(30, 30, 3, Color.YELLOW));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }

    private class GestureViewHolder {
        TextView gestureId;
        TextView gestureName;
        ImageView gestureImage;
        TextView gestureNameRef;

    }
}
