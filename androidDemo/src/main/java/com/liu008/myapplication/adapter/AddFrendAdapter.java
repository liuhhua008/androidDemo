package com.liu008.myapplication.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.liu008.myapplication.R;
import com.liu008.myapplication.entity.CNitemInfo;
import com.liu008.myapplication.entity.IMuserInfo;
import com.liu008.myapplication.sortlist.SortModel;
import com.liu008.myapplication.view.ImageTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来处理集合中数据的显示与排序
 * @author Angus
 *
 */
public class AddFrendAdapter extends BaseAdapter  {
	private List<CNitemInfo> list = null;
	private Context mContext;

	public AddFrendAdapter(Context mContext, List<CNitemInfo> list) {
		this.mContext = mContext;
		if (list==null){
			this.list=new ArrayList<CNitemInfo>();
		}else {
			this.list = list;
		}

	}
	
	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * @param list
	 */
	public void updateListView(List<CNitemInfo> list){
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}


	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final CNitemInfo mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.item_lv_constacts, null);
			viewHolder.tvName = (TextView) view.findViewById(R.id.tv_constact_item_name);
			viewHolder.tvMessage = (TextView) view.findViewById(R.id.tv_constact_item_message);
			viewHolder.tvFlag = (TextView) view.findViewById(R.id.tv_constact_item_flag);
			viewHolder.icon = (ImageView) view.findViewById(R.id.ivConstact_item_icon);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		viewHolder.tvName.setText(this.list.get(position).getNickname());
		viewHolder.tvMessage.setText(this.list.get(position).getMessage());
		if (this.list.get(position).getStatusCode().equals("aa")){
			viewHolder.tvFlag.setText("已添加");
		}else if (this.list.get(position).getStatusCode().equals("bc")){
			//我请求别人
			viewHolder.tvFlag.setText("待对方验证");
//			viewHolder.tvFlag.setTextColor(mContext.getResources().getColor(R.color.white));
//			viewHolder.tvFlag.setBackgroundResource(R.drawable.bt_shap_primary);
		}else if (this.list.get(position).getStatusCode().equals("cb")){
			//对方向我请求
			viewHolder.tvFlag.setText("对方请求添加");
			viewHolder.tvFlag.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
//			viewHolder.tvFlag.setBackgroundResource(R.drawable.bt_shap_primary);
		}
		String s=this.list.get(position).getPortraitUri();
		Glide.with(view).load(this.list.get(position).getPortraitUri()).into(viewHolder.icon);
		return view;

	}


	final static class ViewHolder {
		TextView tvName;
		TextView tvMessage;
		TextView tvFlag;
		ImageView icon;
		
	}




}