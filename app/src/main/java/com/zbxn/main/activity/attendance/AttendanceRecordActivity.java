package com.zbxn.main.activity.attendance;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pub.base.BaseActivity;
import com.pub.base.BaseApp;
import com.pub.http.HttpCallBack;
import com.pub.http.HttpRequest;
import com.pub.http.ResultData;
import com.pub.utils.MyToast;
import com.pub.utils.PreferencesUtils;
import com.pub.utils.StringUtils;
import com.pub.widget.MyListView;
import com.zbxn.R;
import com.zbxn.main.adapter.AttendanceRecordAdapter;
import com.zbxn.main.entity.AttendanceRecordEntity;
import com.zbxn.main.listener.ICustomListener;
import com.zbxn.main.widget.calendar.ScrollLayout;
import com.zbxn.main.widget.calendar.bizs.calendars.Lauar;
import com.zbxn.main.widget.calendar.bizs.calendars.ZSSChineseCalendar;
import com.zbxn.main.widget.calendar.views.MonthView;
import com.zbxn.main.widget.calendar.views.WeekView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;

/**
 * 项目名称：考勤详情页面
 * Created by Administrator on 2016/12/19.
 */
public class AttendanceRecordActivity extends BaseActivity implements MonthView.OnMonthChangeEventListener, MonthView.OnSelectDayEventListener,
        View.OnClickListener, ScrollLayout.OnWeekMonthStyleChangeEventListener, AdapterView.OnItemClickListener {
    /**
     * 回调处理
     */
    private static final int Flag_Callback_Record = 1002;
    private static final int Flag_Callback_Add = 1001;
    @BindView(R.id.main_title)
    TextView mainTitle;
    @BindView(R.id.fragmentschedule_titleDateLinearLayout)
    LinearLayout fragmentscheduleTitleDateLinearLayout;
    @BindView(R.id.month_calendar)
    MonthView monthCalendar;
    @BindView(R.id.content_layout)
    LinearLayout contentLayout;
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    @BindView(R.id.week_calendar)
    WeekView weekCalendar;
    @BindView(R.id.main_scrolllayout)
    ScrollLayout mainScrolllayout;
    @BindView(R.id.mListView)
    MyListView mListView;
    @BindView(R.id.image_line)
    View imageLine;
    @BindView(R.id.mCalendar)
    TextView mCalendar;//农历
    private Calendar now;
    private List<AttendanceRecordEntity> listDay;
    private AttendanceRecordAdapter mAdapter;

    private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private List<AttendanceRecordEntity> listMonth;

    private String date = "";
    private String selectData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);
        setTitle("考勤详情");
        listMonth = new ArrayList<>();
        initView();
        initData();
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        selectData = sf1.format(new Date());
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM");//设置日期格式
        date = sf.format(new Date());
        dataList(this, date, mICustomListener);
    }

    @Override
    public void initRight() {
        setRight1Show(false);
        setRight2Show(false);
        super.initRight();
    }

    private void initView() {
        listDay = new ArrayList<>();
        if (listDay != null) {
            mAdapter = new AttendanceRecordAdapter(this, listDay, mICustomListener);
            mListView.setAdapter(mAdapter);
        }
        mListView.setOnItemClickListener(this);

        monthCalendar.setTodayDisplay(true);
        monthCalendar.setOnMonthChangeEventListener(this);
        monthCalendar.setOnDatePickedListener(this);

        weekCalendar.setTodayDisplay(true);
        weekCalendar.setOnSelectDayEventListener(this);

        mainScrolllayout.setOnWeekMonthStyleChangeEventListener(this);

        fragmentscheduleTitleDateLinearLayout.setOnClickListener(this);

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initData() {
        now = Calendar.getInstance();
        mainTitle.setText(now.get(Calendar.YEAR) + "年" + (now.get(Calendar.MONTH) + 1) + "月");
        getToolbarHelper().setTitle("考勤详情   " + now.get(Calendar.YEAR) + "年" + (now.get(Calendar.MONTH) + 1) + "月");

        monthCalendar.setDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
        weekCalendar.setDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
    }

    @OnClick({R.id.fragmentschedule_titleDateLinearLayout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragmentschedule_titleDateLinearLayout://标题
                break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onMonthChangeEvent(int year, int month) {
        Calendar c = Calendar.getInstance();
        int nowYear = c.get(Calendar.YEAR);
        int nowMonth = c.get(Calendar.MONTH) + 1;
        /*if ((nowYear == year) && (nowMonth == month)) {
            schedule_topbar_comenow_button.setVisibility(View.INVISIBLE);
        } else {
            schedule_topbar_comenow_button.setVisibility(View.VISIBLE);
        }*/
        mainTitle.setText(year + "年" + month + "月");
        getToolbarHelper().setTitle("考勤详情     " + year + "年" + month + "月");
//        Toast.makeText(this, "切换月啦", Toast.LENGTH_LONG).show();

        listDay.clear();
        mAdapter.notifyDataSetChanged();

        date = year + "-" + (month < 10 ? "0" + month : month);
        dataList(this, date, mICustomListener);
    }

    @Override
    public void onSelectDayEvent(String date) {
        date = StringUtils.convertDate(date.replace(".", "-"));
        try {
            if (TextUtils.isEmpty(date)) {
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date1 = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            mCalendar.setText("农历" + new Lauar(calendar).toString());
            listDay.clear();
            for (int i = 0; i < listMonth.size(); i++) {
//                if (listMonth.get(i).getCheckTime().startsWith(date)) {
//                    listDay.add(listMonth.get(i));
//                }
            }
            mAdapter.notifyDataSetChanged();
            //Toast.makeText(this, "" + date.replace(".", "-"), Toast.LENGTH_LONG).show();
            System.out.println("当前选择的天：" + date.replace(".", "-"));
            selectData = date;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWeekMonthStyleChangeEvent(int style) {
    }

    private ICustomListener mICustomListener = new ICustomListener() {
        @Override
        public void onCustomListener(int obj0, Object obj1, int position) {
            switch (obj0) {
                case 0:
                    List<AttendanceRecordEntity> list = (List<AttendanceRecordEntity>) obj1;
                    listMonth.clear();
                    listMonth.addAll(list);
                    List<String> listTemp = new ArrayList<>();
                    int year = 0;
                    int month = 0;
                    for (int i = 0; i < list.size(); i++) {
                        try {
//                            Date date = sf.parse(list.get(i).getCheckintime());
//                            int day = date.getDate();
//                            year = date.getYear();
//                            month = date.getMonth();
//                            listTemp.add(day + "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ZSSChineseCalendar.SCHEDULE.clear();
                    ZSSChineseCalendar.SCHEDULE.put(month + "", listTemp);

                    monthCalendar.setInvalidate();
                    weekCalendar.setInvalidate();

                    onSelectDayEvent(selectData);
                    break;
                case 1:
                    AttendanceRecordEntity entity1 = (AttendanceRecordEntity) obj1;

                    Intent intent1 = new Intent(AttendanceRecordActivity.this, GrievanceActivity.class);
                    intent1.putExtra("item", entity1);
                    intent1.putExtra("type", "1");//1迟到  2早退
                    startActivityForResult(intent1, Flag_Callback_Record);
                    break;
                case 2:
                    AttendanceRecordEntity entity = (AttendanceRecordEntity) obj1;
                    Intent intent = new Intent(AttendanceRecordActivity.this, GrievanceActivity.class);
                    intent.putExtra("item", entity);
                    intent.putExtra("type", "2");//1迟到  2早退
                    startActivityForResult(intent, Flag_Callback_Record);
                    break;
            }
        }
    };

    /**
     * 回调需要接收的人员
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Flag_Callback_Add) {//参与人
            if (data != null) {
                if (resultCode == RESULT_OK) {
                    dataList(this, date, mICustomListener);
                }
            } else {
                return;
            }
        }
        if (requestCode == Flag_Callback_Record) {
            if (resultCode == RESULT_OK) {
                dataList(this, selectData, mICustomListener);
            } else {

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 查看考勤记录
     *
     * @param context
     * @param date
     * @param listener
     */
    public void dataList(Context context, String date, final ICustomListener listener) {
        Map<String, String> map = new HashMap<String, String>();

        String ssid = PreferencesUtils.getString(BaseApp.getContext(), "ssid");
        map.put("tokenid", ssid);
        map.put("date", date);

        Call call = HttpRequest.getIResourceOA().SearchDataListRecord(ssid, date);
        callRequest(call, new HttpCallBack(AttendanceRecordEntity.class, this, true) {
            @Override
            public void onSuccess(ResultData mResult) {
                if ("0".equals(mResult.getSuccess())) {//0成功
                    List<AttendanceRecordEntity> list = mResult.getRows();
                    listener.onCustomListener(0, list, 0);
//                    mController.loading().hide();
                } else {
                    String message = mResult.getMsg();
//                    MyToast.showToast( message);
//                    mController.loading().hide();
                }
            }

            @Override
            public void onFailure(String string) {
                MyToast.showToast("获取网络数据失败");
            }
        });
    }

}
