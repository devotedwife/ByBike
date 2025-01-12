package com.example.bybike.marker;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ab.activity.AbActivity;
import com.ab.http.AbHttpUtil;
import com.ab.http.AbRequestParams;
import com.ab.http.AbStringHttpResponseListener;
import com.ab.view.pullview.AbPullToRefreshView;
import com.example.bybike.R;
import com.example.bybike.adapter.MarkerListAdapter;
import com.example.bybike.db.model.MarkerBean;
import com.example.bybike.util.Constant;
import com.example.bybike.util.SharedPreferencesUtil;

public class MarkerListActivity extends AbActivity {

	// http请求帮助类
	private AbHttpUtil mAbHttpUtil = null;

	private List<MarkerBean> myMarkerListData = null;
	private MarkerListAdapter myMarkerListAdapter = null;
	
	private AbPullToRefreshView mAbPullToRefreshView = null;
	private ListView mListView = null;
	
	private final int GO_TO_MARKERDETAIL = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_marker_list);
		getTitleBar().setVisibility(View.GONE);

		mAbHttpUtil = AbHttpUtil.getInstance(this);

		mAbPullToRefreshView = (AbPullToRefreshView) findViewById(R.id.mPullRefreshView);
		mListView = (ListView) findViewById(R.id.mListView);
		// 打开关闭下拉刷新加载更多功能
		mAbPullToRefreshView.setPullRefreshEnable(false);
		mAbPullToRefreshView.setLoadMoreEnable(false);
		myMarkerListData = new ArrayList<MarkerBean>();
		myMarkerListAdapter = new MarkerListAdapter(MarkerListActivity.this,
				myMarkerListData);
		mListView.setAdapter(myMarkerListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent i = new Intent();
				i.setClass(MarkerListActivity.this, MarkerDetailActivity.class);
				i.putExtra("id", myMarkerListData.get(position)
						.getMarkerId());
				startActivityForResult(i, GO_TO_MARKERDETAIL);
				overridePendingTransition(R.anim.fragment_in,
						R.anim.fragment_out);
			}
		});

		String dataString = getIntent().getExtras().getString("markerList");
		try {
			JSONArray dataArray = new JSONArray(dataString);
			for (int i = 0; i < dataArray.length(); i++) {
				JSONObject jo = dataArray.getJSONObject(i);
				MarkerBean mb = new MarkerBean();
				mb.setMarkerId(jo.getString("Id"));
				mb.setAddress(jo.getString("address"));
				mb.setCollectCount(jo.getString("collectCount"));
				mb.setLikeCount(jo.getString("likeCount"));
				mb.setCommentCount(jo.getString("commentCount"));
				mb.setMarkerName(jo.getString("name"));
				mb.setMarkerType(jo.getString("markerType"));
				
				//复用表，保存likeStatus和collectStatus
				mb.setImgurl1(jo.getString("likeStatus"));
				mb.setImgurl2(jo.getString("collectStatus"));

				myMarkerListData.add(mb);
			}

			myMarkerListAdapter.notifyDataSetChanged();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goBack:
			MarkerListActivity.this.finish();
			break;

		default:
			break;
		}
	}

	/**
	 * onLikeClicked(这里用一句话描述这个方法的作用)
	 */
	public void onLikeClicked(String id) {
		// TODO Auto-generated method stub
		String urlString = Constant.serverUrl + Constant.markerLikeClicked;
		String jsession = SharedPreferencesUtil.getSharedPreferences_s(this, Constant.SESSION);
		AbRequestParams p = new AbRequestParams();
		p.put("id", id);
		// 绑定参数
		mAbHttpUtil.post(urlString, p, new AbStringHttpResponseListener() {

			// 获取数据成功会调用这里
			@Override
			public void onSuccess(int statusCode, String content) {

			};

			// 开始执行前
			@Override
			public void onStart() {
			}

			// 失败，调用
			@Override
			public void onFailure(int statusCode, String content,
					Throwable error) {
			}

			// 完成后调用，失败，成功
			@Override
			public void onFinish() {
			};

		}, jsession);

	}

	/**
	 * 收藏/取消收藏 queryDetail(这里用一句话描述这个方法的作用)
	 */
	public void collectButtonClicked(String id) {

		String urlString = Constant.serverUrl + Constant.markerCollectClicked;
		String jsession = SharedPreferencesUtil.getSharedPreferences_s(this,Constant.SESSION);
		AbRequestParams p = new AbRequestParams();
		p.put("id", id);
		// 绑定参数
		mAbHttpUtil.post(urlString, p, new AbStringHttpResponseListener() {

			// 获取数据成功会调用这里
			@Override
			public void onSuccess(int statusCode, String content) {

			};

			// 开始执行前
			@Override
			public void onStart() {
			}

			// 失败，调用
			@Override
			public void onFailure(int statusCode, String content,
					Throwable error) {
			}

			// 完成后调用，失败，成功
			@Override
			public void onFinish() {
			};

		}, jsession);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
            case GO_TO_MARKERDETAIL:           
                String markerData = data.getStringExtra("markerData");
                Intent intent = getIntent();
                intent.putExtra("markerData", markerData);
                setResult(RESULT_OK, intent);
                MarkerListActivity.this.finish();
                break;

            default:
                break;
            }

        }
        
	}

}
