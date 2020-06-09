package edu.hnu.mail.page.manager;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LogInfoFragment extends Fragment {

    private ImageView buttonBack;
    private TextView buttonClear;
    private RecyclerView logListView;
    private LogListAdapter logListAdapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_log,container,false);
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    getActivity().getSupportFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        });
        initView(root);
        initData();
        return root;
    }

    private void initView(View root) {
        logListView = root.findViewById(R.id.log_list);
        logListView.setLayoutManager(new LinearLayoutManager(getContext()));
        logListAdapter = new LogListAdapter();
        logListView.setAdapter(logListAdapter);
        buttonBack = root.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
//        buttonClear = root.findViewById(R.id.button_clear);
//        buttonClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    private void initData(){
        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/log", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"网络错误",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    Gson gson = new Gson();
                    if (jsonObject.getInt("status") == 200){
                        List<Log> logList = gson.fromJson(jsonObject.getString("loglist"),
                                new TypeToken<List<Log>>(){}.getType());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logListAdapter.setLogList(logList);
                            }
                        });
                    }else{
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),"服务器错误",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
