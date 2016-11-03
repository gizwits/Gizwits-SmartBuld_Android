/**
 * 
 */
package com.gizwits.opensource.devicecontrol.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.opensource.appkit.CommonModule.GosDeploy;

import android.content.Context;
import android.util.Log;

/**
 * @author Refon
 *
 */
public class GosScheduleSiteTool {

	private GizWifiDevice device;

	private String token;

	private String APPID;

	private RequestQueue mRequestQueue;

	private List<ConcurrentHashMap<String, Object>> dataList;

	/**
	 * @param context
	 */
	public GosScheduleSiteTool(Context context, GizWifiDevice device, String token) {
		super();
		this.device = device;
		this.token = token;
		mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
		APPID = GosDeploy.setAppID();

	}

	public interface OnResponListener {
		public void OnRespon(int result, String arg0);
	}

	public interface OnResponseGetDeviceDate {
		public void onReceviceDate(List<ConcurrentHashMap<String, Object>> dataList);
	}

	// ------------------下面是云端预约功能实现--------------------------

	private HashMap<String, String> getHeaderWithToken() {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("X-Gizwits-Application-Id", APPID);
		headers.put("X-Gizwits-User-token", token);
		return headers;
	}

	public void deleteTimeOnSite(String id, final OnResponListener reponse) {
		String httpurl = "http://api.gizwits.com/app/scheduler/" + id;
		StringRequest stringRequest = new StringRequest(Method.DELETE, httpurl, new Response.Listener<String>() {
			@Override
			public void onResponse(String arg0) {
				reponse.OnRespon(0, "OK");
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (error.networkResponse != null) {
					if (error.networkResponse.statusCode == 404) {// 404：云端无法找到该条目，表示该条目已被删除
						reponse.OnRespon(0, "OK");
					}
				}
				reponse.OnRespon(1, error.toString());
				error.printStackTrace();
				Log.i("onSite", "删除失败" + error.toString());

			}
		}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				return getHeaderWithToken();
			}

		};
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(2500, 3, 0));
		mRequestQueue.add(stringRequest);

	}

	/**
	 * <p>
	 * Description:
	 * </p>
	 */
	public void getTimeOnSite(final OnResponseGetDeviceDate response) {

		String httpurl = "http://api.gizwits.com/app/scheduler";
		StringRequest stringRequest = new StringRequest(Method.GET, httpurl, new Response.Listener<String>() {
			@Override
			public void onResponse(String arg0) {
				Log.i("onSite", "-------------");
				Log.i("onSite", arg0);
				dataList = new ArrayList<ConcurrentHashMap<String, Object>>();
				try {
					JSONArray js = new JSONArray(arg0);
					for (int i = 0; i < js.length(); i++) {
						JSONObject jo = js.getJSONObject(i);
						ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
						map.put("date", jo.optString("date"));
						map.put("time", jo.optString("time"));
						map.put("repeat", jo.optString("repeat"));
						map.put("did", getDidFromJsonObject(jo));
						map.put("dataMap", getDateFromJsonObject(jo));
						map.put("ruleID", jo.optString("id"));
						dataList.add(map);
					}
					response.onReceviceDate(dataList);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				response.onReceviceDate(null);
				error.printStackTrace();
				Log.i("onSite", "获取设备状态请求失败" + error.toString() + error.getNetworkTimeMs());
			}
		}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				return getHeaderWithToken();
			}

		};
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(2500, 4, 0));
		mRequestQueue.add(stringRequest);
	}
	
	private String getDidFromJsonObject(JSONObject jo) throws JSONException {
		String date = jo.optJSONArray("task").getJSONObject(0).optString("did");
		return date;
	}

	/**
	 * Description:
	 * 
	 * @param jo
	 * @return
	 * @throws JSONException
	 */
	protected ConcurrentHashMap<String, Object> getDateFromJsonObject(JSONObject jo) throws JSONException {
		ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
		JSONObject date = jo.optJSONArray("task").getJSONObject(0).optJSONObject("attrs");
		Iterator it = date.keys();
		// 遍历jsonObject数据，添加到Map对象
		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			map.put(key, date.get(key));
		}
		return map;
	}

	// ------------------创建规则--------------------------

	/**
	 * Description:向云端创建定时任务
	 * 
	 * @param date
	 *            执行日期，格式为："2015-01-01"。
	 * @param time
	 *            执行时间，格式为："10:10", 注意：该时间为 UTC 时间！
	 * @param repeat
	 *            重复类型通过 repeat
	 *            参数进行设置，不重复设置为"none"，默认执行日期为当天，并且执行时间不能早于当前时间；重复设置为 "mon",
	 *            "tue", "wed", "thu", "fri", "sat", "sun"
	 *            的组合，组合之间用逗号分隔，如每周一和周二重复为 "mon,tue"。
	 * @param attrs
	 *            定时任务中需要更改的数据点及值，格式为"attr1": val；
	 * @param succeed
	 *            创建成功回调函数
	 * @param failed
	 *            创建失败回调函数
	 */
	public void setCommadOnSite(String date, String time, String repeat, ConcurrentHashMap<String, Object> attrs,
			OnResponListener respon) {

		String httpurl = "http://api.gizwits.com/app/scheduler";

		JSONObject jsonsend = new JSONObject();
		try {

			JSONObject jsonCammad = new JSONObject();
			for (String key : attrs.keySet()) {
				jsonCammad.put(key, attrs.get(key));
			}

			JSONObject task = new JSONObject();
			task.put("did", device.getDid());
			task.put("product_key", device.getProductKey());
			task.put("attrs", jsonCammad);
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(task);

			jsonsend.put("date", date);
			jsonsend.put("time", time);
			jsonsend.put("repeat", repeat);
			jsonsend.put("task", jsonArray);
			jsonsend.put("retry_count", 3);
			jsonsend.put("retry_task", "failed");
			Log.i("onSite", jsonsend.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendDateToSite(httpurl, jsonsend, respon);
	}

	private void sendDateToSite(String httpurl, JSONObject jsonObject, final OnResponListener respon) {
		JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Method.POST, httpurl, jsonObject,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.i("onSite", "response -> " + response.toString());
						respon.OnRespon(0, response.optString("id"));
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						respon.OnRespon(1, error.toString());
						error.printStackTrace();
						Log.i("onSite", "sendDateToSite请求失败" + error.toString());
					}
				}) {

			@Override
			public Map<String, String> getHeaders() {
				return getHeaderWithToken();
			}
		};
		mRequestQueue.add(jsonRequest);
	}
}
