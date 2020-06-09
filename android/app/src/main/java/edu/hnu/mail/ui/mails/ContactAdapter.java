package edu.hnu.mail.ui.mails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.data.entity.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contact>  contactList;
    private OnItemClickListener onItemClickListener;

    public Contact getContact(int position){
        return contactList.get(position);
    }

    public void deleteContact(int position){
        contactList.remove(position);
        notifyItemRangeRemoved(position,1);
    }

    public void initContactList(List<Contact>  contactList){
        this.contactList = contactList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact,parent,false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Contact contact = contactList.get(position);
        holder.userName.setText(contact.getUserName());
        holder.mailAddress.setText(contact.getMailAddress());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList!=null?contactList.size():0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout layout;
        private ImageView avatar;
        private TextView userName;
        private TextView mailAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            userName = itemView.findViewById(R.id.username);
            mailAddress = itemView.findViewById(R.id.mail_address);
        }
    }
}
