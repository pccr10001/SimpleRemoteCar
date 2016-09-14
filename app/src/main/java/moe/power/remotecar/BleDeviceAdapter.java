package moe.power.remotecar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by PowerLi on 2016/7/17.
 */
public class BleDeviceAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private List<BleDevice> devList;

    public BleDeviceAdapter(Context context, List<BleDevice> devList) {
        this.myInflater = LayoutInflater.from(context);
        this.devList = devList;
    }

    @Override
    public int getCount() {
        return devList.size();
    }

    @Override
    public Object getItem(int i) {
        return devList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return devList.indexOf(getItem(i));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = myInflater.inflate(R.layout.ble_list, null);
            holder = new ViewHolder(
                    (TextView) view.findViewById(R.id.devName),
                    (TextView) view.findViewById(R.id.address)
            );
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        BleDevice dev = (BleDevice) getItem(i);

        holder.txtName.setText(dev.getName());
        holder.txtAddress.setText(dev.getAddress());
        return view;
    }

    private class ViewHolder {
        TextView txtName;
        TextView txtAddress;

        public ViewHolder(TextView txtName, TextView txtAddress) {
            this.txtName = txtName;
            this.txtAddress = txtAddress;
        }
    }
}
