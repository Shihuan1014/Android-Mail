package edu.hnu.mail.page.contact;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.data.source.local.LocalContactDataSource;
import edu.hnu.mail.ui.mails.ContactAdapter;

public class AddContactFragment extends Fragment implements ContactContract.View{

    //mvp
    private ContactPresenter contactPresenter;

    private ImageView buttonBack;
    private TextView buttonAdd;

    private EditText name;
    private EditText mailAddress;

    private boolean canAdd = false;
    private boolean isNameNotEmpty = false;
    private boolean isMailNotEmpty = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_contact,container,false);
        initView(root);
        initListener();
        return root;
    }

    private void initView(View root){
        buttonBack = root.findViewById(R.id.button_back);
        buttonAdd = root.findViewById(R.id.button_add);
        name = root.findViewById(R.id.name);
        mailAddress = root.findViewById(R.id.mail_address);
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
                if(canAdd){
                    Contact c = new Contact();
                    c.setUserName(name.getText().toString());
                    c.setMailAddress(mailAddress.getText().toString());
                    contactPresenter.addContact(c);
                }
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0){
                    isNameNotEmpty = true;
                    if(isMailNotEmpty){
                        canAdd = true;
                    }
                }else{
                    isNameNotEmpty = false;
                    canAdd = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0){
                    isMailNotEmpty = true;
                    if(isNameNotEmpty){
                        canAdd = true;
                    }
                }else{
                    isMailNotEmpty = false;
                    canAdd = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void showContactList(List<Contact> list) {

    }

    @Override
    public void showLoadError() {

    }

    @Override
    public void showUpdateError() {
        Toast.makeText(getContext(),"添加失败",Toast.LENGTH_SHORT);
    }

    @Override
    public void showSuccess() {
        Toast.makeText(getContext(),"添加成功",Toast.LENGTH_SHORT);
        getActivity().getSupportFragmentManager().popBackStack();
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
