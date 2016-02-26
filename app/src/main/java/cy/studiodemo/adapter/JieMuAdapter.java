package cy.studiodemo.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cy.studiodemo.R;


public class JieMuAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<String> mdata;

	public JieMuAdapter(Context context, ArrayList<String> data) {
		this.mContext = context;
		this.mdata = data;
	}

	@Override
	public int getCount() {
		return mdata.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = View.inflate(mContext, R.layout.item_jiemu, null);
		TextView jiemu_item_tv = (TextView) convertView.findViewById(R.id.jiemu_item_tv);
		jiemu_item_tv.setText(mdata.get(position));
		return convertView;
	}
}
