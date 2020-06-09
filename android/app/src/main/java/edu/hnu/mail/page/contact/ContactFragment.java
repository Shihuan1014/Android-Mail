package edu.hnu.mail.page.contact;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.data.source.local.LocalContactDataSource;
import edu.hnu.mail.page.edit.EditMailActivity;
import edu.hnu.mail.ui.mails.ContactAdapter;
import edu.hnu.mail.ui.mails.OnItemClickListener;

public class ContactFragment extends Fragment implements ContactContract.View{

    //mvp
    private ContactPresenter contactPresenter;

    private ImageView buttonBack;
    private ImageView buttonAdd;
    private RecyclerView contactRecycleView;
    private ContactAdapter contactAdapter;
    private AlertDialog dialog;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contact,container,false);
        initView(root);
        initListener();
        return root;
    }

    private void initView(View root) {
        buttonAdd = root.findViewById(R.id.button_add);
        buttonBack = root.findViewById(R.id.button_back);
        contactRecycleView = root.findViewById(R.id.contact_list);
        contactAdapter = new ContactAdapter();
        contactAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showOptionDialog(position);
            }
        });
        contactRecycleView.setAdapter(contactAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        contactRecycleView.setLayoutManager(linearLayoutManager);
    }

    private void showOptionDialog(int position){
        if(dialog==null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.dialog_contact_option, null);
            LinearLayout buttonWrite = view.findViewById(R.id.button_write);
            buttonWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), EditMailActivity.class);
                    Contact contact = contactAdapter.getContact(position);
                    intent.putExtra("receiver",contact.getMailAddress());
                    getContext().startActivity(intent);
                    dialog.dismiss();
                }
            });
            LinearLayout buttonDelete = view.findViewById(R.id.button_delete);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contactPresenter.deleteContact(contactAdapter.getContact(position).getMailAddress());
                    contactAdapter.deleteContact(position);
                    dialog.dismiss();
                }
            });
// 加载自定义的布局文件
            dialog = new AlertDialog.Builder(getContext()).create();
            dialog.show();
            Window window = dialog.getWindow();
            WindowManager windowManager = getActivity().getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = display.getWidth();
            window.setAttributes(lp);
            window.setContentView(view);
            window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
            window.setWindowAnimations(R.style.bottomDialogAnim);  //添加动画
        }else {
            dialog.show();
        }
    }

    private void initListener(){
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().
                        beginTransaction();
                transaction.replace(R.id.fragment,new AddContactFragment());
                transaction.addToBackStack(getActivity().getClass().getName());
                transaction.commit();
            }
        });
    }

    @Override
    public void showContactList(List<Contact> list) {
        contactAdapter.initContactList(list);
    }

    @Override
    public void showLoadError() {
        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUpdateError() {
        Toast.makeText(getContext(),"更新失败",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccess() {
        Toast.makeText(getContext(),"操作成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(contactPresenter == null){
            contactPresenter = new ContactPresenter(new LocalContactDataSource(getContext()),this);
        }
        contactPresenter.getContactList();
    }
}
