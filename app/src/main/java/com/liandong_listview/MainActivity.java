package com.liandong_listview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.left_listview)
    ListView leftListview;
    @BindView(R.id.pinnedListView)
    PinnedHeaderListView pinnedListView;
    private boolean isScroll = true;
    private LeftListAdapter adapter;

    private List<Data.DataBean.CategoriesBean> list;

    private String urlPath = "http://api.eleteam.com/v1/category/list-with-product";

    private String[] leftStr = null;
    private boolean[] flagArray = null;
    private String[][] rightStr = null;
    private String[][] image = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        LoadData();
    }

    private void init() {
        pinnedListView = (PinnedHeaderListView) findViewById(R.id.pinnedListView);
        final MainSectionedAdapter sectionedAdapter = new MainSectionedAdapter(this, leftStr, rightStr,image);
        pinnedListView.setAdapter(sectionedAdapter);
        adapter = new LeftListAdapter(this, leftStr, flagArray);
        leftListview.setAdapter(adapter);
        leftListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                isScroll = false;

                for (int i = 0; i < leftStr.length; i++) {
                    if (i == position) {
                        flagArray[i] = true;
                    } else {
                        flagArray[i] = false;
                    }
                }
                adapter.notifyDataSetChanged();
                int rightSection = 0;
                for (int i = 0; i < position; i++) {
                    rightSection += sectionedAdapter.getCountForSection(i) + 1;
                }
                pinnedListView.setSelection(rightSection);

            }

        });

        pinnedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                switch (scrollState) {
                    // 当不滚动时
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (pinnedListView.getLastVisiblePosition() == (pinnedListView.getCount() - 1)) {
                            leftListview.setSelection(ListView.FOCUS_DOWN);
                        }

                        // 判断滚动到顶部
                        if (pinnedListView.getFirstVisiblePosition() == 0) {
                            leftListview.setSelection(0);
                        }

                        break;
                }
            }

            int y = 0;
            int x = 0;
            int z = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isScroll) {
                    for (int i = 0; i < rightStr.length; i++) {
                        if (i == sectionedAdapter.getSectionForPosition(pinnedListView.getFirstVisiblePosition())) {
                            flagArray[i] = true;
                            x = i;
                        } else {
                            flagArray[i] = false;
                        }
                    }
                    if (x != y) {
                        adapter.notifyDataSetChanged();
                        y = x;
                        //左侧ListView滚动到最后位置
                        if (y == leftListview.getLastVisiblePosition()) {
                            leftListview.setSelection(z);
                        }
                        //左侧ListView滚动到第一个位置
                        if (x == leftListview.getFirstVisiblePosition()) {
                            leftListview.setSelection(z);
                        }
                        if (firstVisibleItem + visibleItemCount == totalItemCount - 1) {
                            leftListview.setSelection(ListView.FOCUS_DOWN);
                        }
                    }
                } else {
                    isScroll = true;
                }
            }
        });


    }

    private void LoadData() {

        RequestParams entity = new RequestParams(urlPath);
        x.http().get(entity, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                Data data = new Gson().fromJson(result,Data.class);
                list = new ArrayList<Data.DataBean.CategoriesBean>();
                list.addAll(data.getData().getCategories());

                leftStr = new String[list.size()];
                flagArray = new boolean[list.size()];
                rightStr = new String[list.size()][];
                image = new String[list.size()][];


                for(int i=0;i<list.size();i++)
                {
                    leftStr[i] = list.get(i).getName();
                    if(i==0)
                    {
                        flagArray[i] = true;
                    }else{
                        flagArray[i] = false;
                    }
                        String[] aa = new String[list.get(i).getProducts().size()];
                    String[] bb = new String[list.get(i).getProducts().size()];

                    for(int j = 0; j<list.get(i).getProducts().size(); j++)
                    {
                        aa[j] = list.get(i).getProducts().get(j).getName();
                        bb[j] = list.get(i).getProducts().get(j).getApp_long_image1();
                    }
                    rightStr[i] = aa;
                    image[i] = bb;
                }
                        init();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });


    }


}
