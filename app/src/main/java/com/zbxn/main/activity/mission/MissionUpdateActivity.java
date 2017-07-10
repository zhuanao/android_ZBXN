package com.zbxn.main.activity.mission;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pub.base.BaseActivity;
import com.pub.base.BaseApp;
import com.pub.common.EventCustom;
import com.pub.http.HttpCallBack;
import com.pub.http.HttpRequest;
import com.pub.http.ResultData;
import com.pub.http.uploadfile.BaseAsyncTaskInterface;
import com.pub.http.uploadfile.Result;
import com.pub.utils.ConfigUtils;
import com.pub.utils.FileAccessor;
import com.pub.utils.IsEmptyUtil;
import com.pub.utils.MyToast;
import com.pub.utils.PreferencesUtils;
import com.pub.utils.StringUtils;
import com.pub.widget.MyGridView;
import com.pub.widget.multi_image_selector.MultiImageSelector;
import com.pub.widget.slidedatetimepicker.NewSlideDateTimeDialogFragment;
import com.pub.widget.slidedatetimepicker.SlideDateTimeListener;
import com.pub.widget.slidedatetimepicker.SlideDateTimePicker;
import com.zbxn.R;
import com.zbxn.main.activity.contacts.ContactsChoseActivity;
import com.zbxn.main.activity.login.LoginActivity;
import com.zbxn.main.activity.mission.uploadfile.BaseAsyncTaskFile;
import com.zbxn.main.adapter.PhotoListAdapter;
import com.zbxn.main.entity.Contacts;
import com.zbxn.main.entity.MissionAttachmentEntity;
import com.zbxn.main.entity.MissionDetailEntity;
import com.zbxn.main.entity.MissionEntity;
import com.zbxn.main.entity.MissionGetOkrEntity;
import com.zbxn.main.entity.PhotosEntity;
import com.zbxn.main.listener.ICustomListener;

import org.simple.eventbus.EventBus;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;

/**
 * @author: ysj
 * @date: 2016-11-25 17:51
 */
public class MissionUpdateActivity extends BaseActivity {

    @BindView(R.id.et_mission_name)
    EditText etMissionName;
    @BindView(R.id.ll_mission_name)
    LinearLayout llMissionName;
    @BindView(R.id.tv_end_time)
    TextView tvEndTime;
    @BindView(R.id.ll_time)
    LinearLayout llTime;
    @BindView(R.id.et_work_hours)
    EditText etWorkHours;
    @BindView(R.id.ll_work_hours)
    LinearLayout llWorkHours;
    @BindView(R.id.tv_charge_people)
    TextView tvChargePeople;
    @BindView(R.id.ll_charge_people)
    LinearLayout llChargePeople;
    @BindView(R.id.tv_executor_people)
    TextView tvExecutorPeople;
    @BindView(R.id.ll_executor_people)
    LinearLayout llExecutorPeople;
    @BindView(R.id.tv_checker_people)
    TextView tvCheckerPeople;
    @BindView(R.id.ll_checker_people)
    LinearLayout llCheckerPeople;
    @BindView(R.id.tv_copy_people)
    TextView tvCopyPeople;
    @BindView(R.id.ll_copy_people)
    LinearLayout llCopyPeople;
    @BindView(R.id.tv_difficulty)
    TextView tvDifficulty;
    @BindView(R.id.ll_difficulty)
    LinearLayout llDifficulty;
    @BindView(R.id.tv_level)
    TextView tvLevel;
    @BindView(R.id.ll_level)
    LinearLayout llLevel;
    @BindView(R.id.et_sub)
    EditText etSub;
    @BindView(R.id.bt_save)
    TextView btSave;
    @BindView(R.id.iv_charge)
    ImageView ivCharge;
    @BindView(R.id.iv_checker)
    ImageView ivChecker;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.tv_score)
    TextView tvScore;

    @BindView(R.id.gridview)
    MyGridView gridview;

    /**
     * 选择接收人回调
     */
    private static final int Flag_Callback_Charge = 1001;//负责人
    private static final int Flag_Callback_Execute = 1002;//执行人
    private static final int Flag_Callback_Check = 1003;//审核人
    private static final int Flag_Callback_Copy = 1004;//抄送人
    private int type;
    //难易程度
    String[] diffItems = new String[]{"普通", "困难"};
    //选中的难易程度
    private String choosedDiff;
    //选中的难易程度(角标)
    private int choosedDiffNum = 0;
    //选择的时间
    private String choosedTime;
    //等级
    String[] levelItems = new String[]{"正常", "紧急"};
    //选中的等级
    private String choosedLevel;
    //选中的等级(角标)
    private int choosedLevelNum = 0;
    String[] times = new String[]{"0", "10", "20", "30", "40", "50"};
    //选择的时间索引
    private int choosedTimePosition;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //执行人数组
    private String[] mExecuterArray;
    //所有执行人Id
    private String executeIds;
    //所有抄送人Id
    private String[] mCopyArray;
    //所有抄送人id字符串
    private String copysId;
    //负责人id
    private String chargeId;
    //负责人姓名
    private String chargeName;
    //审核人id
    private String checkerId;
    //审核人姓名
    private String checkerName;
    //父任务ID
    private String parentId;
    //图片路径的集合
    private ArrayList<String> mSelectPath = new ArrayList<>();
    //当前任务ID
    private String ID;
    //任务状态
    private String mTaskState;
    //任务完成进度
    private String mDoneProgress;

    private String mGuid = "";
    private String endTime;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private Date endDate;
    private Date childEndDate;

    //空的加号
    private PhotosEntity entityEmpty = null;
    private ArrayList<PhotosEntity> lists = new ArrayList<PhotosEntity>();

    //获取图片
    private static final int REQUEST_IMAGE = 2002;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    public static final String  SUCCESS="Success";
    //提交数据map
    Map<String, String> mMap = new HashMap<String, String>();
    //是否是子任务
    private boolean mIsChild;
    private String missionName;

    //选择人员负责人
    private ArrayList<Contacts> mListContactsCharge = new ArrayList<>();
    //选择人员执行人
    private ArrayList<Contacts> mListContactsExecuters = new ArrayList<>();
    //选择人员审核人
    private ArrayList<Contacts> mListContactsChecker = new ArrayList<>();

    private boolean mIsLock;//是否锁定 true  false
    private int mActionType;//操作类型 0存草稿  1创建任务
    private boolean mEnableCaoGao;//是否能存草稿

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_update);
        ButterKnife.bind(this);
        setTitle("修改任务");

        MissionDetailEntity entity = (MissionDetailEntity) getIntent().getSerializableExtra("mission");

        mGuid = entity.getAttachmentGUID();

        ID = entity.getTaskId() + "";
        etMissionName.setText(entity.getTitle());
        tvEndTime.setText(entity.getEndTime());
        etWorkHours.setText(entity.getWorkHours() + "");
        tvTime.setText(entity.getWorkMins() + "");
        choosedTime = entity.getWorkMins() + "";
        getPrewOKR();
        tvChargePeople.setText(entity.getChargerName());
        etSub.setText(entity.getInfo());

        //执行人
        executeIds = "";
        String sb = "";
        for (int i = 0; i < entity.getTransactors().size(); i++) {
            sb += entity.getTransactors().get(i).getName() + "、";
            executeIds += entity.getTransactors().get(i).getId() + ",";
        }
        if (sb.length() > 0) {
            sb = sb.substring(0, sb.length() - 1);
        }
        if (sb.length() > 0) {
            executeIds = executeIds.substring(0, executeIds.length() - 1);
        }
        tvExecutorPeople.setText(sb);
        if (StringUtils.isEmpty(sb)) {
            tvExecutorPeople.setText(" ");
        }
        mTaskState = entity.getTaskState() + "";
        mDoneProgress = entity.getProgress() + "";
        tvCheckerPeople.setText(entity.getReviewerName());
        choosedDiffNum = entity.getDifferentlyLevel();
        if (entity.getDifferentlyLevel() == 0) {
            tvDifficulty.setText("普通");
        } else if (entity.getDifferentlyLevel() == 1) {
            tvDifficulty.setText("困难");
        }

        choosedLevelNum = entity.getTaskLevel();
        if (entity.getTaskLevel() == 0) {//级别
            tvLevel.setText("正常");
        } else if (entity.getTaskLevel() == 1) {
            tvLevel.setText("紧急");
        }

        chargeId = entity.getChargerId() + "";//负责人id
        checkerId = entity.getReviewerId() + "";//审核人id

        //获取传入的parentId值,默认为0
        Intent intent = getIntent();
        parentId = intent.getStringExtra("parentId");
        //获取截止时间
        endTime = intent.getStringExtra("endTime");
        if (TextUtils.isEmpty(parentId)) {
            parentId = "0";
        } else {
            //子任务的审核人是父任务的负责人,不可修改
            checkerId = intent.getStringExtra("chargeId");
            checkerName = intent.getStringExtra("chargeName");
            tvCheckerPeople.setText(checkerName);
            ivChecker.setVisibility(View.INVISIBLE);
            llCheckerPeople.setClickable(false);
        }

//        String tempDate = sdf.format(new Date());
//        tvEndTime.setText(tempDate);
        entityEmpty = new PhotosEntity();
        entityEmpty.setAppname("");
        entityEmpty.setId("");
        entityEmpty.setImgurl("tzs_paizhao");

        lists.clear();

        final String webServer = ConfigUtils.getConfig(ConfigUtils.KEY.webServer);
        PhotosEntity photosEntity = null;
        lists.clear();
        if (!StringUtils.isEmpty(entity.getAttachmentList())) {
            for (MissionAttachmentEntity p : entity.getAttachmentList()) {
                photosEntity = new PhotosEntity();
                photosEntity.setAppname("");
                photosEntity.setId(p.getId());
                photosEntity.setImgurl(webServer + p.getAttachmentUrl());
                photosEntity.setImgurlNet(webServer + p.getAttachmentUrl());
                lists = updatePhotos(lists, photosEntity);
            }
        }
        lists = updatePhotos(lists, null);

        PhotoListAdapter adapterPhotos = new PhotoListAdapter(this, lists, R.layout.list_item_select_photos, listener, true);
        gridview.setAdapter(adapterPhotos);

        //任务名称不能超过200字
        limitMissionName();
        btSave.setVisibility(View.GONE);
        btSave.setText("保存");

        for (int i = 0; i < entity.getButtons().size(); i++) {
            if ("锁定".equals(entity.getButtons().get(i).getOperateName())) {
                mIsLock = false;
                continue;
            }
            if ("解锁".equals(entity.getButtons().get(i).getOperateName())) {
                mIsLock = true;
                continue;
            }
            if ("保存草稿".equals(entity.getButtons().get(i).getOperateName())) {
                mEnableCaoGao = true;
                continue;
            }
        }
        etWorkHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                getPrewOKR();
            }
        });
    }


    @Override
    public void initRight() {
        super.initRight();
        setRight1("完成");
        setRight1Icon(R.mipmap.complete2);
        setRight1Show(true);
        setRight2("存草稿");
        setRight2Icon(0);
        if (mEnableCaoGao) {
            setRight2Show(true);
        } else {
            setRight2Show(false);
        }
    }

    @Override
    public void actionRight1(MenuItem menuItem) {
        super.actionRight1(menuItem);
        mActionType = 1;
        //保存修改的任务
        addNewMission(true);
    }
//刷新任务页面
    private void setEventBus() {
        EventCustom eventCustom = new EventCustom();
        eventCustom.setTag(MissionUpdateActivity.SUCCESS);
        EventBus.getDefault().post(eventCustom);
    }

    @Override
    public void actionRight2(MenuItem menuItem) {
        super.actionRight2(menuItem);
        mActionType = 0;
        //保存修改的任务
        addNewMission(true);
    }

    /**
     * 任务名称不能超过200字
     */
    private void limitMissionName() {
        etMissionName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //限制200字
                if (etMissionName.getText().toString().length() >= 200) {
                    MyToast.showToast("任务名称不能超过200字");
                }

            }
        });
    }

    /**
     * 更新照片list
     *
     * @param photoList
     * @param entity
     * @return
     */
    private ArrayList<PhotosEntity> updatePhotos(ArrayList<PhotosEntity> photoList, PhotosEntity entity) {
        if (!StringUtils.isEmpty(photoList)) {
            photoList.remove(entityEmpty);
        } else {
            photoList = new ArrayList<PhotosEntity>();
        }
        List<PhotosEntity> listPhotosTemp = new ArrayList<PhotosEntity>();
        listPhotosTemp.addAll(photoList);
        photoList.clear();
        if (null != entity) {
            listPhotosTemp.add(entity);
        }

        photoList.addAll(listPhotosTemp);
        photoList.add(entityEmpty);
        return photoList;
    }

    /**
     * 回调需要接收的人员
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Flag_Callback_Charge) { //负责人
            if (data != null) {
                if (resultCode == RESULT_OK) {
                    chargeId = data.getIntExtra("id", 0) + "";
                    chargeName = data.getStringExtra("name");
                    Contacts entity = new Contacts();
                    entity.setId(data.getIntExtra("id", 0));
                    entity.setUserName(checkerName);
                    mListContactsCharge.clear();
                    mListContactsCharge.add(entity);
                    tvChargePeople.setText(chargeName);
                }

            } else {
                return;
            }
        } else if (requestCode == Flag_Callback_Execute) { //执行人
            if (data != null) {
                if (resultCode == RESULT_OK) {
                    mListContactsExecuters = (ArrayList<Contacts>) data.getExtras().getSerializable(ContactsChoseActivity.Flag_Output_Checked);
                    mExecuterArray = new String[mListContactsExecuters.size()];
                    String content = "";
                    //所有执行人id字符串
                    executeIds = "";
                    for (int i = 0; i < mListContactsExecuters.size(); i++) {
                        mExecuterArray[i] = mListContactsExecuters.get(i).getId() + "";
                        content += mListContactsExecuters.get(i).getUserName() + "、";
                        executeIds += mListContactsExecuters.get(i).getId() + ",";
                    }
                    if (!StringUtils.isEmpty(executeIds)) {
                        content = content.substring(0, content.length() - 1);
                        executeIds = executeIds.substring(0, executeIds.length() - 1);
                    }
                    if (mListContactsExecuters.size() >= 2) {
                        content = mListContactsExecuters.get(0).getUserName() + "、" + mListContactsExecuters.get(1).getUserName() + "等" + mListContactsExecuters.size() + "人";
                    }
                    tvExecutorPeople.setText(content);
                }

            } else {
                return;
            }
        } else if (requestCode == Flag_Callback_Check) { //审核人
            if (data != null) {
                if (resultCode == RESULT_OK) {
                    checkerId = data.getIntExtra("id", 0) + "";
                    checkerName = data.getStringExtra("name");
                    Contacts entity = new Contacts();
                    entity.setId(data.getIntExtra("id", 0));
                    entity.setUserName(checkerName);
                    mListContactsChecker.clear();
                    mListContactsChecker.add(entity);
                    tvCheckerPeople.setText(checkerName);
                }

            } else {
                return;
            }
        } else if (requestCode == Flag_Callback_Copy) { //抄送人
            if (data != null) {
                if (resultCode == RESULT_OK) {
                    List<Contacts> list = (ArrayList<Contacts>) data.getExtras().getSerializable(ContactsChoseActivity.Flag_Output_Checked);
                    //所有抄送人Id
                    mCopyArray = new String[list.size()];
                    String content = "";
                    //所有抄送人id字符串
                    copysId = "";
                    for (int i = 0; i < list.size(); i++) {
                        mCopyArray[i] = list.get(i).getId() + "";
                        copysId += list.get(i).getId() + ",";
                        content += list.get(i).getUserName() + "、";
                    }
                    if (!StringUtils.isEmpty(copysId)) {
                        content = content.substring(0, content.length() - 1);
                        copysId = copysId.substring(0, copysId.length() - 1);
                    }
                    if (list.size() >= 2) {
                        content = list.get(0).getUserName() + "、" + list.get(1).getUserName() + "等" + list.size() + "人";
                    }
                    tvCopyPeople.setText(content);
                }

            }
        } else if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
//                mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                StringBuilder sb = new StringBuilder();
                PhotosEntity entity = null;
                lists.clear();
                for (String p : mSelectPath) {
                    sb.append(p);
                    sb.append("\n");

                    entity = new PhotosEntity();
                    entity.setAppname("");
                    entity.setId("");
                    entity.setImgurl(p);
                    entity.setImgurlNet(p);
                    lists = updatePhotos(lists, entity);
                }
                PhotoListAdapter photoListAdapter = new PhotoListAdapter(this, lists, R.layout.list_item_select_photos, listener, true);
                gridview.setAdapter(photoListAdapter);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 上传图片
     */
    public void uploadFile(final Context context, ArrayList<PhotosEntity> list) {
        Map<String, String> map = new HashMap<String, String>();
        //附件集合
        Map<String, File> mapFile = new HashMap<>();

        String ids = "";
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getImgurl().contains("http://")) {
                ids += list.get(i).getId() + ",";
            }
        }
        if (ids.length() > 0) {
            ids = ids.substring(0, ids.length() - 1);
        }

        String ssid = PreferencesUtils.getString(BaseApp.getContext(), "ssid");
        map.put("tokenid", ssid);
        map.put("guId", mGuid);
        map.put("ids", ids);
        for (int i = 0; i < list.size() - 1; i++) {
            File file = new File(list.get(i).getImgurl());
            if (file == null || !file.exists()) {
                break;
            }
            File fileSmall = null;
            try {
                String filePathStr = FileAccessor.getSmallPicture(file.getPath()); // 压缩文件
                fileSmall = new File(filePathStr);
            } catch (Exception e) {
                System.out.println("上传图片错误：" + e.toString());
                e.printStackTrace();
                fileSmall = file;
            }
            mapFile.put("image" + i, fileSmall);
        }

        String server = ConfigUtils.getConfig(ConfigUtils.KEY.server);

        new BaseAsyncTaskFile(context, false, 0, server + "/common/doUpLoads.do", new BaseAsyncTaskInterface() {
            @Override
            public void dataSubmit(int funId) {

            }

            @Override
            public Object dataParse(int funId, String json) throws Exception {
                return Result.parse(json);
            }

            @Override
            public void dataSuccess(int funId, Object result) {
                Result mResult = (Result) result;
                if ("0".equals(mResult.getSuccess())) {//0成功
                    updateMission();
                } else {
                    String message = mResult.getMsg();
                    MyToast.showToast(message);
                }
            }

            @Override
            public void dataError(int funId) {
            }
        }, mapFile).execute(map);
    }

    /**
     * 点击事件
     */
    @OnClick({R.id.ll_time,
            R.id.ll_charge_people, R.id.ll_executor_people, R.id.ll_checker_people,
            R.id.ll_copy_people, R.id.ll_difficulty, R.id.ll_level, R.id.bt_save,R.id.tvTime})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.ll_time:
                //截止时间
                String time = StringUtils.getEditText(tvEndTime);
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(new SlideDateTimeListener() {
                            @Override
                            public void onDateTimeSet(Date date) {
                                if (TextUtils.isEmpty(parentId) || "0".equals(parentId)) {
                                    //主任务直接赋值
                                    tvEndTime.setText(format.format(date));
                                } else {
                                    //子任务判断,不能超过主任务
                                    String childEndTime = format.format(date);
                                    try {
                                        endDate = sdf.parse(endTime);
                                        childEndDate = sdf.parse(childEndTime);
//                                        if (endDate.before(childEndDate)) {
//                                            MyToast.showToast("不能超过主任务截止时间");
//                                        } else {
                                        tvEndTime.setText(format.format(date));
//                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        })
                        .setInitialDate(StringUtils.convertToDate(format, time))
                        .setIs24HourTime(true)
                        .setIsHaveTime(NewSlideDateTimeDialogFragment.Have_Date_Time)
                        .build()
                        .show();
                break;
            case R.id.ll_charge_people:
                //负责人
                intent = new Intent(this, ContactsChoseActivity.class);
                intent.putExtra("list", mListContactsCharge);
                intent.putExtra("type", 2);//0-查看 1-多选 2-单选
                startActivityForResult(intent, Flag_Callback_Charge);
                break;
            case R.id.ll_executor_people:
                //执行人
                intent = new Intent(this, ContactsChoseActivity.class);
                intent.putExtra("list", mListContactsExecuters);
                intent.putExtra("type", 1);//0-查看 1-多选 2-单选
                startActivityForResult(intent, Flag_Callback_Execute);
                break;
            case R.id.ll_checker_people:
                //审核人
                intent = new Intent(this, ContactsChoseActivity.class);
                intent.putExtra("list", mListContactsChecker);
                intent.putExtra("type", 2);//0-查看 1-多选 2-单选
                startActivityForResult(intent, Flag_Callback_Check);
                break;
            case R.id.ll_copy_people:
                //抄送人
                intent = new Intent(this, ContactsChoseActivity.class);
                intent.putExtra("type", 1);//0-查看 1-多选 2-单选
                startActivityForResult(intent, Flag_Callback_Copy);
                break;
            case R.id.ll_difficulty:
                //难易程度
                openDifficulityDialog();
                break;
            case R.id.ll_level:
                //等级
                openLevelDialog();
                break;
            case R.id.bt_save:
                //保存修改的任务
                addNewMission(true);
                break;
            case R.id.tvTime:
                //工作量 分
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请选择时间(分钟)");
                builder.setSingleChoiceItems(times, choosedTimePosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choosedTime = times[which];
                        choosedTimePosition = which;
                        tvTime.setText(choosedTime);
                        dialog.dismiss();
                        getPrewOKR();
                    }
                });
                builder.show();
                break;
        }
    }

    /**
     * 打开选择难易程度的单选框
     */
    private void openDifficulityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择难易程度");
        builder.setSingleChoiceItems(diffItems, choosedDiffNum, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                choosedDiff = diffItems[which];
                choosedDiffNum = which;
                tvDifficulty.setText(choosedDiff);
                dialog.dismiss();
                getPrewOKR();
            }
        });
        builder.show();
    }

    /**
     * 打开选择等级的单选框
     */
    private void openLevelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择等级");
        builder.setSingleChoiceItems(levelItems, choosedLevelNum, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                choosedLevel = levelItems[which];
                choosedLevelNum = which;
                tvLevel.setText(choosedLevel);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 修改任务
     */
    public void addNewMission(final boolean isChild) {
        mIsChild = isChild;
        //任务名
        if (!IsEmptyUtil.isEmpty(etMissionName, "请输入任务名称")) {
            return;
        }
//        //截止时间
//        if (!IsEmptyUtil.isEmpty(tvEndTime, "请输入截止时间")) {
//            return;
//        }
        //工作量
        if (!IsEmptyUtil.isEmpty(etWorkHours.getText().toString(), "请输入工作量")) {
            return;
        }
        //负责人
        if (!IsEmptyUtil.isEmpty(tvChargePeople.getText().toString(), "请选择负责人")) {
            return;
        }
//        //审核人
//        if (!IsEmptyUtil.isEmpty(tvCheckerPeople.getText().toString(), "请选择审核人")) {
//            return;
//        }

        //创建任务时间和截止任务时间，任务所用时间的一个判断
        /*try {
            if (etWorkHours.getText().toString().length() < 5) {
                String newDate = sdf.format(new Date());
                String endDate = StringUtils.getEditText(tvEndTime);
                String wh = StringUtils.getEditText(etWorkHours);
                long newTime = sdf.parse(newDate).getTime() + Integer.parseInt(wh) * 3600000;
                long localTime = sdf.parse(endDate).getTime();
                if (newTime > localTime) {
                    MyToast.showToast("任务时长不能超过截止时间");
                    return;
                }
            } else {
                MyToast.showToast("你输入的时间过长");
                return;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        if (lists.size() > 1) {
            uploadFile(this, lists);
        } else {
            updateMission();
        }

        setEventBus();
    }


    private ICustomListener listener = new ICustomListener() {

        @Override
        public void onCustomListener(int obj0, Object obj1, int position) {
            switch (obj0) {
                case 0://刪除
                    lists.remove(position);
                    Log.e("tt:", mSelectPath.toString());
                    mSelectPath.remove(lists.get(position).getImgurl());
                    Log.e("tt11111:", mSelectPath.toString());
                    lists = updatePhotos(lists, null);
                    PhotoListAdapter photoListAdapter = new PhotoListAdapter(MissionUpdateActivity.this, lists, R.layout.list_item_select_photos, listener, true);
                    gridview.setAdapter(photoListAdapter);
                    break;
                case 1://添加
                    pickImage();
                    break;
                case 3://显示大图
                    ArrayList<String> list_Ads = new ArrayList<>();
                    for (int j = 0; j < lists.size() - 1; j++) {
                        list_Ads.add(lists.get(j).getImgurl());
                    }
                    Intent intent = new Intent(MissionUpdateActivity.this, PhotoDetailActivity.class);
                    intent.putExtra("list", list_Ads);
                    intent.putExtra("position", position);
                    startActivity(intent);
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 选择图片
     */
    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            boolean showCamera = true;//是否显示相机
            int maxNum = 9;//最大选择9张
            boolean isMulti = true;//是否多选

            MultiImageSelector selector = MultiImageSelector.create(MissionUpdateActivity.this);
            selector.showCamera(showCamera);
            selector.count(maxNum);
            if (isMulti) {
                selector.multi();
            } else {
                selector.single();
            }
            selector.origin(mSelectPath);
            selector.start(MissionUpdateActivity.this, REQUEST_IMAGE);
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MissionUpdateActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    /**
     * 修改任务
     */
    public void updateMission() {
        String ssid = PreferencesUtils.getString(this, LoginActivity.FLAG_SSID);
        String currentCompanyId = PreferencesUtils.getString(this, LoginActivity.FLAG_ZMSCOMPANYID, "");
        Call call = HttpRequest.getIResourceOANetAction().postUpdateMission(ssid, ID, parentId, StringUtils.getEditText(etMissionName), StringUtils.getEditText(tvEndTime), StringUtils.getEditText(etWorkHours)
                , StringUtils.getEditText(tvTime), chargeId, executeIds, checkerId, choosedDiffNum + "", choosedLevelNum + "", etSub.getText().toString(), mGuid, currentCompanyId
                , mIsLock + "", mActionType + "");
        callRequest(call, new HttpCallBack(MissionEntity.class, this, true) {
            @Override
            public void onSuccess(ResultData mResult) {
                if ("0".equals(mResult.getSuccess())) {
                    MyToast.showToast("修改成功");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String message = mResult.getMsg();
                    MyToast.showToast(message);
                }
            }

            @Override
            public void onFailure(String string) {
                MyToast.showToast(R.string.NETWORKERROR);
            }
        });
    }

    public void getPrewOKR() {
        String ssid = PreferencesUtils.getString(this, LoginActivity.FLAG_SSID);
        String currentCompanyId = PreferencesUtils.getString(this, LoginActivity.FLAG_ZMSCOMPANYID, "");
        String hours = "0";
        if (!StringUtils.isEmpty(etWorkHours)) {
            hours = StringUtils.getEditText(etWorkHours);
        }
        Call call = HttpRequest.getIResourceOANetAction().getPrewOKR(ssid, currentCompanyId, choosedDiffNum + "", hours, choosedTime);
        callRequest(call, new HttpCallBack(MissionGetOkrEntity.class, this, false) {
            @Override
            public void onSuccess(ResultData mResult) {
                if ("0".equals(mResult.getSuccess())) {
                    MissionGetOkrEntity entity = (MissionGetOkrEntity) mResult.getData();
                    tvScore.setText(entity.getOkr() + "");
                } else {
//                    String message = mResult.getMsg();
//                    MyToast.showToast(message);
                }
            }

            @Override
            public void onFailure(String string) {
                MyToast.showToast(R.string.NETWORKERROR);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        Intent intent = getIntent();
        intent.putExtra("refresh", true);
        super.onDestroy();
    }
}