package pg.autyzm.przyjazneemocje.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import pg.autyzm.przyjazneemocje.R;


public class CheckboxGridAdapter extends ArrayAdapter{

    private ArrayList dataSet;
    Context context;

    private static class ViewHolder {
        TextView txtName;
        CheckBox checkbox;
    }

    public CheckboxGridAdapter(ArrayList data, Context context) {
        super(context, R.layout.grid_checkbox, data);
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public CheckboxGridBean getItem(int position) {
        return (CheckboxGridBean) dataSet.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_checkbox, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.grid_checkbox);
            viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.grid_checkbox);

            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        final CheckboxGridBean item = getItem(position);

        viewHolder.txtName.setText(item.getName());
        viewHolder.checkbox.setChecked(item.isChecked());

        viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean isChecked = ((CheckBox)arg0).isChecked();
                item.setChecked(isChecked);
            }
        });

        return result;
    }
}
