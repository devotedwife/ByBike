package com.example.bybike.marker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.ab.activity.AbActivity;
import com.ab.global.AbConstant;
import com.ab.http.AbHttpUtil;
import com.ab.http.AbRequestParams;
import com.ab.http.AbStringHttpResponseListener;
import com.ab.util.AbFileUtil;
import com.ab.util.AbStrUtil;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.bybike.R;
import com.example.bybike.exercise.ApplyAcitivity;
import com.example.bybike.user.LoginActivity;
import com.example.bybike.user.RegisterActivity;
import com.example.bybike.util.BitmapUtil;
import com.example.bybike.util.Constant;
import com.example.bybike.util.NetUtil;
import com.example.bybike.util.SharedPreferencesUtil;
import com.example.bybike.util.Utils;

public class AddMarkerActivity extends AbActivity {

	// http请求帮助类
	private AbHttpUtil mAbHttpUtil = null;

	// 基础地图相关
	MapView mMapView = null;
	BaiduMap mBaidumap = null;
	private View mAvatarView = null;
	/* 用来标识请求照相功能的activity */
	private static final int CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	private static final int PHOTO_PICKED_WITH_DATA = 3021;
	/* 拍照的照片存储位置 */
	private File PHOTO_DIR = null;

	private static final int GO_TO_LOCATION_MARKER = 1001;
	// 照相机拍照得到的图片
	private File mCurrentPhotoFile;
	private String mFileName;

	private int currentPhotoId;
	private File[] photoFiles = new File[4];
	private HashMap<Integer, Bitmap> photoIdAndBitMap = new HashMap<Integer, Bitmap>();
	private int[] photosIds = new int[] { R.id.photo1, R.id.photo2,
			R.id.photo3, R.id.photo4 };
	private int[] markerTypeIds = new int[] { R.id.type1, R.id.type2,
			R.id.type3, R.id.type4, R.id.type5, R.id.type6, R.id.type7,
			R.id.type8 };
	private int[] markerTypeTextIds = new int[] { R.id.type1text,
			R.id.type2text, R.id.type3text, R.id.type4text, R.id.type5text,
			R.id.type6text, R.id.type7text, R.id.type8text };

	/**
	 * 友好点
	 */
	private LatLng currentPt;
	private String markerId;
	private boolean saveSucceed = false;
	private Marker marker;

	private String markerType = "RantCar";
	BitmapDescriptor marker_icon_bikestore = BitmapDescriptorFactory
			.fromResource(R.drawable.marker_icon_bikestore);
	BitmapDescriptor marker_icon_meals = BitmapDescriptorFactory
			.fromResource(R.drawable.marker_icon_meals);
	BitmapDescriptor marker_icon_others = BitmapDescriptorFactory
			.fromResource(R.drawable.marker_icon_others);
	BitmapDescriptor marker_icon_parking = BitmapDescriptorFactory
			.fromResource(R.drawable.marker_icon_parking);
	BitmapDescriptor marker_icon_rentbike = BitmapDescriptorFactory
			.fromResource(R.drawable.marker_icon_rentbike);
	BitmapDescriptor marker_icon_repair = BitmapDescriptorFactory
			.fromResource(R.drawable.marker_icon_repair);
	BitmapDescriptor marker_icon_scenery = BitmapDescriptorFactory
			.fromResource(R.drawable.marker_icon_scenery);
	BitmapDescriptor marker_icon_washroom = BitmapDescriptorFactory
			.fromResource(R.drawable.marker_icon_washroom);
	List<BitmapDescriptor> bitMapDescriptorList = new ArrayList<BitmapDescriptor>();

	/**
	 * 地址字符串
	 */
	private String address;
	private EditText addressText;
	private EditText markerName;
	private EditText description;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_create_marker);
		getTitleBar().setVisibility(View.GONE);

		// 获取Http工具类
		mAbHttpUtil = AbHttpUtil.getInstance(this);
		mAbHttpUtil.setDebug(false);

		bitMapDescriptorList.clear();
		bitMapDescriptorList.add(marker_icon_bikestore);
		bitMapDescriptorList.add(marker_icon_meals);
		bitMapDescriptorList.add(marker_icon_others);
		bitMapDescriptorList.add(marker_icon_parking);
		bitMapDescriptorList.add(marker_icon_rentbike);
		bitMapDescriptorList.add(marker_icon_repair);
		bitMapDescriptorList.add(marker_icon_scenery);
		bitMapDescriptorList.add(marker_icon_washroom);

		// ===============初始化地图========================
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaidumap = mMapView.getMap();
		// 默认初始地图放大级别为17级
		MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(17f);
		mBaidumap.animateMapStatus(u);
		// 隐藏自带的地图缩放控件
		int childCount = mMapView.getChildCount();
		View zoom = null;
		for (int i = 0; i < childCount; i++) {
			View child = mMapView.getChildAt(i);
			if (child instanceof ZoomControls) {
				zoom = child;
				break;
			}
		}
		zoom.setVisibility(View.GONE);
		// 隐藏指南针
		mBaidumap.getUiSettings().setCompassEnabled(false);
		mBaidumap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(AddMarkerActivity.this,
						LocationMarkerActivity.class);
				i.putExtra("markerType", markerType);
				i.putExtra("address", addressText.getText().toString());
				if (currentPt != null) {
					double[] latlng = new double[2];
					latlng[0] = currentPt.latitude;
					latlng[1] = currentPt.longitude;
					i.putExtra("latlng", latlng);
				}
				startActivityForResult(i, GO_TO_LOCATION_MARKER);
				overridePendingTransition(R.anim.fragment_in,
						R.anim.fragment_out);
			}
		});
		// ===============================================

		// 初始化图片保存路径
		String photo_dir = AbFileUtil.getFullImageDownPathDir();
		if (AbStrUtil.isEmpty(photo_dir)) {
			showToast("存储卡不存在");
		} else {
			PHOTO_DIR = new File(photo_dir);
		}

		mAvatarView = mInflater.inflate(R.layout.choose_avatar, null);
		Button albumButton = (Button) mAvatarView
				.findViewById(R.id.choose_album);
		Button camButton = (Button) mAvatarView.findViewById(R.id.choose_cam);
		Button cancelButton = (Button) mAvatarView
				.findViewById(R.id.choose_cancel);

		albumButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(AbConstant.DIALOGBOTTOM);
				// 从相册中去获取
				try {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
					intent.setType("image/*");
					startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
				} catch (ActivityNotFoundException e) {
					showToast("没有找到照片");
				}
			}

		});

		camButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(AbConstant.DIALOGBOTTOM);
				doPickPhotoAction();
			}

		});

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(AbConstant.DIALOGBOTTOM);
			}

		});

		/**
		 * 初始化友好点显示
		 */
		changeAlpher(1);

		Bundle b = getIntent().getExtras();
		if (null != b) {
			double latitude = b.getDouble("latitude");
			double longitude = b.getDouble("longitude");
			currentPt = new LatLng(latitude, longitude);
			OverlayOptions ooA = new MarkerOptions().position(currentPt)
					.icon(marker_icon_rentbike).zIndex(9).draggable(true);
			marker = (Marker) (mBaidumap.addOverlay(ooA));
			MapStatusUpdate ut = MapStatusUpdateFactory.newLatLng(currentPt);
			mBaidumap.animateMapStatus(ut);
		}

		addressText = (EditText) findViewById(R.id.markerAddress);
		markerName = (EditText) findViewById(R.id.markerName);
		description = (EditText) findViewById(R.id.description);

	}

	/**
	 * 接受调用
	 * 
	 * @param source
	 */
	public void clickHandler(View source) {

		switch (source.getId()) {
		case R.id.goBack:
			goBack();
			break;
		case R.id.photo1:
			currentPhotoId = 1;
			showDialog(1, mAvatarView);
			break;
		case R.id.photo2:
			currentPhotoId = 2;
			showDialog(1, mAvatarView);
			break;
		case R.id.photo3:
			currentPhotoId = 3;
			showDialog(1, mAvatarView);
			break;
		case R.id.photo4:
			currentPhotoId = 4;
			showDialog(1, mAvatarView);
			break;
		case R.id.type1:
			changeAlpher(1);
			break;
		case R.id.type2:
			changeAlpher(2);
			break;
		case R.id.type3:
			changeAlpher(3);
			break;
		case R.id.type4:
			changeAlpher(4);
			break;
		case R.id.type5:
			changeAlpher(5);
			break;
		case R.id.type6:
			changeAlpher(6);
			break;
		case R.id.type7:
			changeAlpher(7);
			break;
		case R.id.type8:
			changeAlpher(8);
			break;
		case R.id.cancel:
			goBack();
			break;
		case R.id.submit:
			addMarker();
			break;
		default:
			break;
		}
	}

	private void addMarker() {

		if (!NetUtil.isConnnected(this)) {
			showDialog("温馨提示", "网络不可用，请设置您的网络后重试");
			return;
		}
		String markerNameString = markerName.getText().toString().trim();
		if ("".equals(markerNameString)) {
			showDialog("温馨提示", "友好点名称不能为空，请重新输入");
			return;
		}
		address = addressText.getText().toString().trim();
		if ("".equals(address)) {
			showDialog("温馨提示", "地址不能为空，请重新输入");
			return;
		}
		if (marker == null) {
			showDialog("温馨提示", "请在地图上选择友好点位置");
			return;
		}
		String remarks = description.getText().toString().trim();
		if ("".equals(remarks)) {
			showDialog("温馨提示", "请简单描述一下该友好点再重新保存");
			return;
		}
		if (!SharedPreferencesUtil.getSharedPreferences_b(
				AddMarkerActivity.this, Constant.ISLOGINED)) {

			showDialog("温馨提示", "您还未登录或者登录已过期，请重新登录后再试",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent i = new Intent(AddMarkerActivity.this,
									LoginActivity.class);
							startActivity(i);
							overridePendingTransition(R.anim.fragment_in,
									R.anim.fragment_out);
							dialog.dismiss();
						}
					});
			return;
		}
		String urlString = Constant.serverUrl + Constant.addMarkerUrl;
		urlString += ";jsessionid=";
		urlString += SharedPreferencesUtil.getSharedPreferences_s(this,
				Constant.SESSION);
		AbRequestParams p = new AbRequestParams();
		p.put("markerType", markerType, "multipart/form-data");
		p.put("lat", String.valueOf(marker.getPosition().latitude));
		p.put("lng", String.valueOf(marker.getPosition().longitude));
		p.put("remarks", remarks);
		p.put("address", address);
		p.put("name", markerNameString);
		// p.put("takeCare", "");
		// p.put("operatingType", "");
		// p.put("phone", "");
		// p.put("motorcycleType", "");
		// p.put("allowPark", "");
		// p.put("price", "");
		// p.put("allowEnter", "");
		for (int i = 0; i < 4; i++) {
			if (photoFiles[i] != null) {
				String tmp = "img" + String.valueOf(i + 1);
				p.put(tmp, photoFiles[i], "multipart/form-data");
			}
		}
		// 绑定参数
		mAbHttpUtil.post(urlString, p, new AbStringHttpResponseListener() {

			// 获取数据成功会调用这里
			@Override
			public void onSuccess(int statusCode, String content) {

				processResult(content);
			};

			// 开始执行前
			@Override
			public void onStart() {
				showProgressDialog("正在保存，请稍后...");
			}

			// 失败，调用
			@Override
			public void onFailure(int statusCode, String content,
					Throwable error) {
				showToast("上传失败，请稍后重试...");
			}

			// 完成后调用，失败，成功
			@Override
			public void onFinish() {
				removeProgressDialog();
			};

		});
	}

	/**
	 * processResult(这里用一句话描述这个方法的作用)
	 * 
	 * @param content
	 */
	protected void processResult(String content) {
		// TODO Auto-generated method stub
		try {
			JSONObject resultObject = new JSONObject(content);
			String code = resultObject.getString("code");
			if ("0".equals(code)) {

				saveSucceed = true;
				JSONObject dataObj = resultObject.getJSONObject("data");
				markerId = dataObj.getString("id");
				showDialog("温馨提示", "保存成功",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								Intent intent = getIntent();
								intent.putExtra("lat", currentPt.latitude);
				                intent.putExtra("lng", currentPt.longitude);
				                intent.putExtra("markerId", markerId);
				                intent.putExtra("markerType", markerType);
				                intent.putExtra("markerName", markerName.getText().toString().trim());

				                setResult(RESULT_OK, intent);
				                AddMarkerActivity.this.finish();
							}
						});

			} else if ("3".equals(code)) {

				showDialog("温馨提示",
						"保存失败：\n" + resultObject.getString("message"),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								Intent i = new Intent(AddMarkerActivity.this,
										LoginActivity.class);
								startActivity(i);
								overridePendingTransition(R.anim.fragment_in,
										R.anim.fragment_out);

								dialog.dismiss();
							}
						});

			} else {
				showDialog("温馨提示",
						"保存失败：\n" + resultObject.getString("message")
								+ "\n请稍后重试");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 修改button背景透明度
	 */
	private void changeAlpher(int target) {

		switch (target) {
		case 1:
			markerType = "RantCar";
			break;
		case 2:
			markerType = "CarShop";
			break;
		case 3:
			markerType = "Parking";
			break;
		case 4:
			markerType = "FeatureSpot";
			break;
		case 5:
			markerType = "Repair";
			break;
		case 6:
			markerType = "Catering";
			break;
		case 7:
			markerType = "Washroom";
			break;
		case 8:
			markerType = "Other";
			break;
		default:
			break;
		}

		for (int i = 1; i <= markerTypeIds.length; i++) {
			Button b = (Button) findViewById(markerTypeIds[i - 1]);
			TextView t = (TextView) findViewById(markerTypeTextIds[i - 1]);
			if (i != target) {
				b.setAlpha(0.5f);
				t.setAlpha(0.5f);
			} else {
				b.setAlpha(1f);
				t.setAlpha(1f);
			}
		}

		updateMarkerIcon();
	}

	/**
	 * 拍照获取图片
	 */
	protected void doTakePhoto() {
		try {
			mFileName = System.currentTimeMillis() + ".jpg";
			mCurrentPhotoFile = new File(PHOTO_DIR, mFileName);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(mCurrentPhotoFile));
			startActivityForResult(intent, CAMERA_WITH_DATA);
		} catch (Exception e) {
			showToast("未找到系统相机程序");
		}
	}

	/**
	 * 描述：从照相机获取
	 */
	private void doPickPhotoAction() {
		String status = Environment.getExternalStorageState();
		// 判断是否有SD卡,如果有sd卡存入sd卡在说，没有sd卡直接转换为图片
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			doTakePhoto();
		} else {
			showToast("没有可用的存储卡");
		}
	}

	/**
	 * 描述：因为调用了Camera和Gally所以要判断他们各自的返回情况, 他们启动时是这样的startActivityForResult
	 */
	protected void onActivityResult(int requestCode, int resultCode,
			Intent mIntent) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case PHOTO_PICKED_WITH_DATA:
			Uri uri = mIntent.getData();
			String currentFilePath = getPath(uri);
			if (!AbStrUtil.isEmpty(currentFilePath)) {
				Bitmap currentBitMap = BitmapUtil.compressPhotoFileToBitmap(
						currentFilePath, 640, 480);
				ImageView image = (ImageView) findViewById(photosIds[currentPhotoId - 1]);
				image.setImageBitmap(currentBitMap);

				String picFileName = System.currentTimeMillis() + ".jpg";
				File picFile = new File(PHOTO_DIR, picFileName);
				// 压缩图片
				BitmapUtil.compressBmpToFile(currentBitMap, picFile);
				photoFiles[currentPhotoId - 1] = picFile;
			} else {
				showToast("未在存储卡中找到这个文件");
			}
			break;
		case CAMERA_WITH_DATA:
			// if(D)Log.d(TAG, "将要进行裁剪的图片的路径是 = " +
			// mCurrentPhotoFile.getPath());
			String currentFilePath2 = mCurrentPhotoFile.getPath();
			Bitmap currentBitMap = BitmapUtil.compressPhotoFileToBitmap(
					currentFilePath2, 640, 480);
			ImageView image = (ImageView) findViewById(photosIds[currentPhotoId - 1]);
			image.setImageBitmap(currentBitMap);

			String picFileName = System.currentTimeMillis() + ".jpg";
			File picFile = new File(PHOTO_DIR, picFileName);
			// 压缩图片
			BitmapUtil.compressBmpToFile(currentBitMap, picFile);
			photoFiles[currentPhotoId - 1] = picFile;
			break;
		// case CAMERA_CROP_DATA:
		// String path = mIntent.getStringExtra("PATH");
		// // if(D)Log.d(TAG, "裁剪后得到的图片的路径是 = " + path);
		// break;
		case GO_TO_LOCATION_MARKER:
			Bundle b = mIntent.getExtras();
			if (b != null) {
				double[] latlngs = b.getDoubleArray("latlng");
				if (latlngs != null) {
					currentPt = new LatLng(latlngs[0], latlngs[1]);
					if (marker == null) {

						OverlayOptions ooA = new MarkerOptions()
								.position(currentPt).icon(marker_icon_rentbike)
								.zIndex(9).draggable(true);
						marker = (Marker) (mBaidumap.addOverlay(ooA));

					} else {
						marker.setPosition(currentPt);
					}
					updateMarkerIcon();
					MapStatusUpdate u = MapStatusUpdateFactory
							.newLatLng(currentPt);
					mBaidumap.animateMapStatus(u);
				}
			}
			break;
		default:
			break;
		}
	}

	private void updateMarkerIcon() {
		if (marker == null)
			return;
		if ("RantCar".equalsIgnoreCase(markerType)) {
			marker.setIcon(marker_icon_rentbike);
		} else if ("Repair".equalsIgnoreCase(markerType)) {
			marker.setIcon(marker_icon_repair);
		} else if ("FeatureSpot".equalsIgnoreCase(markerType)) {
			marker.setIcon(marker_icon_scenery);
		} else if ("Catering".equalsIgnoreCase(markerType)) {
			marker.setIcon(marker_icon_meals);
		} else if ("Washroom".equalsIgnoreCase(markerType)) {
			marker.setIcon(marker_icon_washroom);
		} else if ("Parking".equalsIgnoreCase(markerType)) {
			marker.setIcon(marker_icon_parking);
		} else if ("Other".equalsIgnoreCase(markerType)) {
			marker.setIcon(marker_icon_others);
		} else {
			marker.setIcon(marker_icon_others);
		}
	}

	/**
	 * 从相册得到的url转换为SD卡中图片路径
	 */
	public String getPath(Uri uri) {
		if (AbStrUtil.isEmpty(uri.getAuthority())) {
			return null;
		}
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		return path;
	}

	/**
	 * 退出页面
	 */
	private void goBack() {
		
		if(!saveSucceed){
			showDialog("温馨提示", "确认取消并退出本页面吗？退出后本友好点信息将不再保存。",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							AddMarkerActivity.this.finish();
						}
					});
		}else{
			
			Intent intent = getIntent();
			intent.putExtra("lat", currentPt.latitude);
            intent.putExtra("lng", currentPt.longitude);
            intent.putExtra("markerId", markerId);
            intent.putExtra("markerType", markerType);
            intent.putExtra("markerName", markerName.getText().toString().trim());

            setResult(RESULT_OK, intent);
            AddMarkerActivity.this.finish();
			
		}
		
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 退出时销毁定位
		mMapView.onDestroy();
		for (BitmapDescriptor b : bitMapDescriptorList) {
			b.recycle();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

}
